package com.afriland.dottel;

import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import com.afriland.dottel.processus.api.EvenementClotureDto;
import com.afriland.dottel.processus.model.entity.EtapeWorkflow;
import com.afriland.dottel.processus.model.entity.LigneEtatMensuel;
import com.afriland.dottel.processus.model.entity.PieceJointe;
import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.processus.model.enums.NomEtapeEnum;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import com.afriland.dottel.processus.model.enums.StatutEtapeEnum;
import com.afriland.dottel.processus.repository.EtapeWorkflowRepository;
import com.afriland.dottel.processus.repository.LigneEtatMensuelRepository;
import com.afriland.dottel.processus.repository.PieceJointeRepository;
import com.afriland.dottel.processus.repository.ProcessusMensuelRepository;
import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import com.afriland.dottel.referentiel.repository.GrilleTarifaireRepository;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.afriland.dottel.SupportAuthentificationTest.CompteTest.ADMIN_TCHINDA;
import static com.afriland.dottel.SupportAuthentificationTest.CompteTest.ARH_MBARGA;
import static com.afriland.dottel.SupportAuthentificationTest.CompteTest.CRH_ESSAMA;
import static com.afriland.dottel.SupportAuthentificationTest.CompteTest.DRH_ATANGANA;
import static com.afriland.dottel.SupportAuthentificationTest.CompteTest.EMPLOYE_NKOLO;
import static com.afriland.dottel.SupportAuthentificationTest.jetonDe;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration bout en bout des flux critiques (Sprint 7.1 adapté MM).
 *
 * <h2>Ce qui est réel ici</h2>
 *
 * PostgreSQL réel (migrations Flyway V1 à V7 comprises), contrôleurs, services,
 * repositories, contraintes SQL, {@code @PreAuthorize}, conversion des rôles
 * Keycloak, {@code GlobalExceptionHandler}. Aucun mock sur la logique métier.
 *
 * <h2>Les trois seules frontières substituées</h2>
 *
 * <ul>
 *   <li>{@code JwtDecoder} — voir {@link SupportAuthentificationTest} : Keycloak
 *       n'est pas la responsabilité de ce module, et le décodeur réel ferait un
 *       appel réseau dès la création du bean.</li>
 *   <li>{@code KafkaTemplate} — le projet n'embarque pas
 *       {@code spring-kafka-test}. Vérifier {@code send(topic, clé, payload)}
 *       prouve la publication de l'événement de clôture sans exiger un broker
 *       pendant {@code mvn test}.</li>
 *   <li>{@code NotificationService} — substitué au seul flux 7, qui vit dans une
 *       classe distincte parce qu'il exige un commit réel (piège
 *       {@code AFTER_COMMIT}).</li>
 * </ul>
 *
 * <h2>Isolation</h2>
 *
 * {@code @Transactional} au niveau de la classe : tout est annulé en fin de
 * test, la base de développement n'est pas polluée. Les périodes utilisées sont
 * en 2025, hors de toute donnée existante, pour que RG-12 ne dépende jamais de
 * l'état préalable de la base.
 */
@SpringBootTest(properties =
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + SupportAuthentificationTest.ISSUER_URI_DE_TEST)
@AutoConfigureMockMvc
@Import(SupportAuthentificationTest.JwtDecoderDeTest.class)
@Transactional
class IntegrationFluxMetierTest {

    /**
     * Matricule connu du stub EHR et NON enrôlé par les jeux de données de
     * développement : MBARGA Jean-Paul, GFC, Agence Douala Akwa.
     * Fonction sans grade requis (hors corps de contrôle) — RG-02 ne s'applique
     * pas, l'éligibilité ne dépend donc que de RG-01.
     */
    private static final String MATRICULE_EHR_NON_ENROLE = "1847";

    private static final String TOPIC_CLOTURE = "dottel.processus.cloture";

    /**
     * Fonction support du flux grille tarifaire : une seule grille ACTIVE
     * ouverte, aucune grille en attente au moment d ecrire ce test. Les
     * assertions ne s appuient malgre tout sur aucune valeur figee, la
     * date de debut etant recalculee depuis la base.
     */
    private static final String FONCTION_GRILLE_FLUX4 = "ASSISTANTE_PCA";

    /** Fonction support du volet grille du flux RG-08, distincte du flux 4. */
    private static final String FONCTION_GRILLE_FLUX6 = "ASSISTANTE_ADG";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BeneficiaireRepository beneficiaireRepository;

    @Autowired
    private ProcessusMensuelRepository processusMensuelRepository;

    @Autowired
    private LigneEtatMensuelRepository ligneEtatMensuelRepository;

    @Autowired
    private EtapeWorkflowRepository etapeWorkflowRepository;

    @Autowired
    private PieceJointeRepository pieceJointeRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private GrilleTarifaireRepository grilleTarifaireRepository;

    @Autowired
    private FonctionEligibleRepository fonctionEligibleRepository;

    @MockitoBean(name = "kafkaTemplateEvenementCloture")
    private KafkaTemplate<String, EvenementClotureDto> kafkaTemplateEvenementCloture;

    @Value("${dottel.documents.chemin-stockage}")
    private String cheminStockageDocuments;

    /** Noms de fichiers presents dans le repertoire de documents AVANT le test. */
    private Set<String> documentsPreexistants = Set.of();

    @BeforeEach
    void memoriserLesDocumentsPreexistants() throws IOException {
        documentsPreexistants = listerDocuments();
    }

    /**
     * Supprime les PDF produits par les tests, et EUX SEULS.
     *
     * <p>Le rollback de {@code @Transactional} ne couvre que la base : la
     * generation du document est une ecriture sur disque, hors transaction.</p>
     *
     * <p>La suppression procede par difference avec l'instantane pris avant le
     * test, jamais par filtre sur le nom. Un filtre serait fragile : le
     * repertoire de developpement contient deja des documents reels dont la
     * periode peut coincider avec celle d'un test. Par difference, un fichier
     * qui existait avant ne peut structurellement pas etre supprime.</p>
     *
     * <p>Apres CHAQUE test plutot qu'une fois pour toutes : le repertoire reste
     * propre meme si un test ulterieur echoue avant la fin de la classe.</p>
     */
    @AfterEach
    void supprimerLesDocumentsProduitsParLesTests() throws IOException {
        for (String nom : listerDocuments()) {
            if (!documentsPreexistants.contains(nom)) {
                Files.deleteIfExists(Path.of(cheminStockageDocuments).resolve(nom));
            }
        }
    }

    private Set<String> listerDocuments() throws IOException {
        Path repertoire = Path.of(cheminStockageDocuments);
        if (!Files.isDirectory(repertoire)) {
            return Set.of();
        }
        try (var fichiers = Files.list(repertoire)) {
            return new HashSet<>(fichiers.map(f -> f.getFileName().toString()).toList());
        }
    }

    // ------------------------------------------------------------------
    // Flux 1 — enrôlement complet (UC01/UC02)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Flux 1 : un EMPLOYE vérifie son matricule auprès de l'EHR puis confirme son enrôlement")
    void flux1_enrolementComplet() throws Exception {
        assertThat(beneficiaireRepository.existsByMatricule(MATRICULE_EHR_NON_ENROLE))
                .as("préalable du flux : le matricule ne doit pas déjà être enrôlé (RG-03)")
                .isFalse();

        // 1. Vérification : le stub EHR répond, RG-01 est évaluée, rien n'est
        //    encore écrit en base.
        mockMvc.perform(get("/enrolement/verifier")
                        .param("matricule", MATRICULE_EHR_NON_ENROLE)
                        .with(jetonDe(EMPLOYE_NKOLO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricule").value(MATRICULE_EHR_NON_ENROLE))
                .andExpect(jsonPath("$.nom").value("MBARGA"))
                .andExpect(jsonPath("$.fonction").value("GFC"))
                .andExpect(jsonPath("$.libelleFonction").value("Gestionnaire de Fonds de Commerce"))
                .andExpect(jsonPath("$.eligible").value(true));

        assertThat(beneficiaireRepository.existsByMatricule(MATRICULE_EHR_NON_ENROLE))
                .as("la vérification est une lecture pure : elle n'enrôle pas")
                .isFalse();

        // 2. Confirmation.
        mockMvc.perform(post("/enrolement/confirmer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("matricule", MATRICULE_EHR_NON_ENROLE)))
                        .with(jetonDe(EMPLOYE_NKOLO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matricule").value(MATRICULE_EHR_NON_ENROLE))
                .andExpect(jsonPath("$.fonction").value("GFC"));

        // 3. Vérification EN BASE, sans mock : tous les attributs recopiés
        //    depuis l'EHR, y compris ceux qui n'apparaissent dans aucune
        //    réponse d'API (codeAgence, chapitre) et alimentent l'événement de
        //    clôture destiné à la comptabilité.
        Beneficiaire beneficiaire = beneficiaireRepository.findByMatricule(MATRICULE_EHR_NON_ENROLE).orElseThrow();
        assertThat(beneficiaire.getNomPrenoms()).isEqualTo("MBARGA Jean-Paul");
        assertThat(beneficiaire.getFonction()).isEqualTo("GFC");
        assertThat(beneficiaire.getUniteRattachement()).isEqualTo("Agence Douala Akwa");
        assertThat(beneficiaire.getCodeUnite()).isEqualTo("1102");
        assertThat(beneficiaire.getCodeAgence()).isEqualTo("00002");
        assertThat(beneficiaire.getNumCompteCourant()).isEqualTo("10011847002");
        assertThat(beneficiaire.getChapitre()).isEqualTo("37210100");
        assertThat(beneficiaire.getDateEnrolement()).isEqualTo(LocalDate.now());
        assertThat(beneficiaire.isActif()).isTrue();

        // 4. RG-03 : un second enrôlement du même matricule est refusé (409).
        mockMvc.perform(post("/enrolement/confirmer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("matricule", MATRICULE_EHR_NON_ENROLE)))
                        .with(jetonDe(EMPLOYE_NKOLO)))
                .andExpect(status().isConflict());
    }

    // ------------------------------------------------------------------
    // Flux 2 — processus mensuel complet ARH → CRH → DRH
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Flux 2 : ARH déclenche, ajuste et valide, CRH valide, DRH clôture — un seul PDF, 3 signatures, événement publié")
    void flux2_processusMensuelComplet() throws Exception {
        Long idProcessus = declencher(3, 2025, false);

        ProcessusMensuel processus = processusMensuelRepository.findById(idProcessus).orElseThrow();
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.EN_COURS_ARH);
        assertThat(processus.getRattrapage()).isFalse();
        assertThat(processus.getIdCreateur()).isEqualTo(idUtilisateur(ARH_MBARGA.email()));

        List<LigneEtatMensuel> lignes = ligneEtatMensuelRepository.findByIdProcessus(idProcessus);
        assertThat(lignes)
                .as("une ligne d'état mensuel par bénéficiaire actif — la table pivot est peuplée au déclenchement")
                .isNotEmpty();
        assertThat(ligneEtatMensuelRepository.countByIdProcessusAndInclusDansEtatTrue(idProcessus))
                .as("le flux exige au moins une ligne incluse pour produire un état signable")
                .isPositive();

        // Ajustement ARH : exclusion délibérée d'un bénéficiaire de l'état du
        // mois. Exclure n'est jamais revalidé (retirer quelqu'un est toujours
        // sûr) — c'est bien inclusDansEtat qui porte la décision.
        LigneEtatMensuel ligneAExclure = lignes.stream()
                .filter(LigneEtatMensuel::getInclusDansEtat)
                .findFirst()
                .orElseThrow();
        long lignesInclusesAvantAjustement =
                ligneEtatMensuelRepository.countByIdProcessusAndInclusDansEtatTrue(idProcessus);

        mockMvc.perform(patch("/processus/" + idProcessus)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("ajustements", List.of(
                                Map.of("idBeneficiaire", ligneAExclure.getIdBeneficiaire(),
                                        "inclusDansEtat", false)))))
                        .with(jetonDe(ARH_MBARGA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultats[0].applique").value(true));

        assertThat(ligneEtatMensuelRepository
                .findByIdProcessusAndIdBeneficiaire(idProcessus, ligneAExclure.getIdBeneficiaire())
                .orElseThrow()
                .getInclusDansEtat())
                .as("l'ajustement ARH est bien persisté sur la ligne d'état mensuel")
                .isFalse();
        assertThat(ligneEtatMensuelRepository.countByIdProcessusAndInclusDansEtatTrue(idProcessus))
                .isEqualTo(lignesInclusesAvantAjustement - 1);

        // Validation ARH : RG-06, le document est créé UNE fois, avec 1 signature.
        Long idPieceJointe = valider(idProcessus, ARH_MBARGA, StatutEnum.EN_ATTENTE_CRH, "VALIDATION_ARH");

        PieceJointe pieceJointe = pieceJointeRepository.findById(idPieceJointe).orElseThrow();
        assertThat(pieceJointe.getIdProcessus()).isEqualTo(idProcessus);
        assertThat(pieceJointe.getNombreSignatures()).isEqualTo(1);
        assertThat(pieceJointe.getNomFichier()).isEqualTo("dotations-telephoniques-3-2025.pdf");

        // Validation CRH : la MÊME PieceJointe est enrichie, jamais un second document.
        Long idPieceJointeApresCrh = valider(idProcessus, CRH_ESSAMA, StatutEnum.EN_ATTENTE_DRH, "VALIDATION_CRH");
        assertThat(idPieceJointeApresCrh)
                .as("RG-06 : un seul document par processus, enrichi progressivement")
                .isEqualTo(idPieceJointe);
        assertThat(pieceJointeRepository.findById(idPieceJointe).orElseThrow().getNombreSignatures()).isEqualTo(2);

        // Validation DRH : clôture.
        Long idPieceJointeApresDrh = valider(idProcessus, DRH_ATANGANA, StatutEnum.CLOTURE, "VALIDATION_DRH");
        assertThat(idPieceJointeApresDrh).isEqualTo(idPieceJointe);
        assertThat(pieceJointeRepository.findById(idPieceJointe).orElseThrow().getNombreSignatures()).isEqualTo(3);

        ProcessusMensuel processusClos = processusMensuelRepository.findById(idProcessus).orElseThrow();
        assertThat(processusClos.getStatut()).isEqualTo(StatutEnum.CLOTURE);
        assertThat(processusClos.getDateCloture()).isNotNull();

        assertThat(pieceJointeRepository.findByIdProcessus(idProcessus))
                .as("UNIQUE(id_processus) : une seule ligne piece_jointe, celle du départ")
                .get()
                .extracting(PieceJointe::getId)
                .isEqualTo(idPieceJointe);

        // Les trois étapes sont tracées, dans l'ordre, par trois acteurs
        // distincts (RG-05 et RG-08 vus depuis leur résultat en base).
        List<EtapeWorkflow> etapes = etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(idProcessus);
        assertThat(etapes).hasSize(3);
        assertThat(etapes).extracting(EtapeWorkflow::getNomEtape)
                .containsExactly(NomEtapeEnum.VALIDATION_ARH, NomEtapeEnum.VALIDATION_CRH,
                        NomEtapeEnum.VALIDATION_DRH);
        assertThat(etapes).extracting(EtapeWorkflow::getStatutEtape).containsOnly(StatutEtapeEnum.VALIDEE);
        assertThat(etapes).extracting(EtapeWorkflow::getIdActeur).doesNotHaveDuplicates();

        // Événement de clôture : bon topic, clé = id du processus, payload
        // cohérent avec l'état mensuel réellement validé.
        ArgumentCaptor<EvenementClotureDto> payload = ArgumentCaptor.forClass(EvenementClotureDto.class);
        verify(kafkaTemplateEvenementCloture)
                .send(eq(TOPIC_CLOTURE), eq(String.valueOf(idProcessus)), payload.capture());

        EvenementClotureDto evenement = payload.getValue();
        assertThat(evenement.getIdProcessus()).isEqualTo(idProcessus);
        assertThat(evenement.getMoisPaiement()).isEqualTo(3);
        assertThat(evenement.getAnneePaiement()).isEqualTo(2025);
        assertThat(evenement.getMontantTotal())
                .isEqualTo(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(idProcessus));
        assertThat(evenement.getLignes())
                .as("une entrée par bénéficiaire INCLUS — un exclu n'a pas été payé, aucune écriture ne le concerne")
                .hasSize((int) ligneEtatMensuelRepository.countByIdProcessusAndInclusDansEtatTrue(idProcessus));
        assertThat(evenement.getLignes())
                .extracting(ligne -> ligne.getMontantAttribue())
                .doesNotContain(0L);
    }

    // ------------------------------------------------------------------
    // Flux 3 — retour CRH, correction ARH, revalidation
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Flux 3 : le CRH retourne avec motif, l'ARH corrige et revalide — la pièce jointe est remplacée en place, jamais dupliquée")
    void flux3_retourEtReprise() throws Exception {
        Long idProcessus = declencher(4, 2025, false);
        Long idPieceJointe = valider(idProcessus, ARH_MBARGA, StatutEnum.EN_ATTENTE_CRH, "VALIDATION_ARH");
        assertThat(pieceJointeRepository.findById(idPieceJointe).orElseThrow().getNombreSignatures()).isEqualTo(1);

        // RG-07 : un retour sans motif est refusé avant toute mutation d'état.
        mockMvc.perform(post("/processus/" + idProcessus + "/retourner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("motif", "   ")))
                        .with(jetonDe(CRH_ESSAMA)))
                .andExpect(status().isBadRequest());

        assertThat(processusMensuelRepository.findById(idProcessus).orElseThrow().getStatut())
                .as("RG-07 : le refus ne doit rien avoir changé au statut")
                .isEqualTo(StatutEnum.EN_ATTENTE_CRH);

        // Retour effectif.
        mockMvc.perform(post("/processus/" + idProcessus + "/retourner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("motif",
                                "Montant erroné sur une ligne, à corriger avant validation")))
                        .with(jetonDe(CRH_ESSAMA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(StatutEnum.RETOURNE.name()));

        assertThat(processusMensuelRepository.findById(idProcessus).orElseThrow().getStatut())
                .isEqualTo(StatutEnum.RETOURNE);

        // Décision S-1 : le compteur décrit l'état du CYCLE de validation, pas
        // le contenu du fichier — il retombe à 0 pour tout retour, y compris un
        // retour CRH où aucune signature CRH n'existait encore.
        assertThat(pieceJointeRepository.findById(idPieceJointe).orElseThrow().getNombreSignatures())
                .as("état intermédiaire lu dans ProcessusMensuelService.retourner(), pas supposé")
                .isZero();

        // Correction ARH : RETOURNE reste ajustable, exactement comme EN_COURS_ARH.
        Long idBeneficiaireACorriger = ligneEtatMensuelRepository
                .findByIdProcessusAndInclusDansEtatTrue(idProcessus)
                .getFirst()
                .getIdBeneficiaire();

        mockMvc.perform(patch("/processus/" + idProcessus)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("ajustements", List.of(
                                Map.of("idBeneficiaire", idBeneficiaireACorriger, "inclusDansEtat", false)))))
                        .with(jetonDe(ARH_MBARGA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultats[0].applique").value(true));

        // Revalidation ARH : même circuit qu'une première validation.
        Long idPieceJointeApresReprise =
                valider(idProcessus, ARH_MBARGA, StatutEnum.EN_ATTENTE_CRH, "VALIDATION_ARH");

        // Le cœur du flux : upsert, pas doublon.
        assertThat(idPieceJointeApresReprise)
                .as("RG-06 : genererInitiale() remplace la PieceJointe existante en place (UNIQUE(id_processus))")
                .isEqualTo(idPieceJointe);
        assertThat(pieceJointeRepository.findAll().stream()
                .filter(pj -> pj.getIdProcessus().equals(idProcessus))
                .count())
                .as("une seule ligne piece_jointe pour ce processus, après un cycle complet retour/reprise")
                .isEqualTo(1);
        assertThat(pieceJointeRepository.findById(idPieceJointe).orElseThrow().getNombreSignatures())
                .as("le compteur repart à 1 : les signatures antérieures portaient sur un contenu qui a changé")
                .isEqualTo(1);

        // EtapeWorkflow est append-only : le retour et les deux validations ARH
        // coexistent, ce qui est précisément le cas que SeparationTachesService
        // doit savoir démêler (anomalie MM.8).
        List<EtapeWorkflow> etapes = etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(idProcessus);
        assertThat(etapes)
                .filteredOn(e -> e.getNomEtape() == NomEtapeEnum.VALIDATION_ARH
                        && e.getStatutEtape() == StatutEtapeEnum.VALIDEE)
                .hasSize(2);
        assertThat(etapes)
                .filteredOn(e -> e.getStatutEtape() == StatutEtapeEnum.RETOURNEE)
                .singleElement()
                .satisfies(etape -> assertThat(etape.getMotifRetour()).isNotBlank());
    }

    // ------------------------------------------------------------------
    // Flux 4 — grille tarifaire, workflow à trois acteurs (MM.12)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Flux 4 : ARH crée une grille, le CRH décide, la DRH valide — l'ancienne grille est fermée, la nouvelle devient ACTIVE")
    void flux4_grilleTarifaireTroisActeurs() throws Exception {
        FonctionEligible fonction = fonctionEligibleRepository.findByCode(FONCTION_GRILLE_FLUX4).orElseThrow();
        GrilleTarifaire ancienneGrilleActive = grilleTarifaireRepository
                .findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(fonction.getId(), StatutGrilleEnum.ACTIVE)
                .orElseThrow();
        LocalDate dateDebut = prochaineDateDebutValide(fonction);

        // 1. Création ARH — RG-10 : jamais ACTIVE directement.
        Long idGrille = creerGrille(FONCTION_GRILLE_FLUX4, 45000, dateDebut);

        GrilleTarifaire grille = grilleTarifaireRepository.findById(idGrille).orElseThrow();
        assertThat(grille.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_CRH);
        assertThat(grille.getIdCreateur()).isEqualTo(idUtilisateur(ARH_MBARGA.email()));
        assertThat(grille.getIdDecideurCrh()).isNull();
        assertThat(grille.getIdValidateur()).isNull();

        // 2. Décision CRH — première étape du circuit à trois acteurs.
        deciderGrille(idGrille, CRH_ESSAMA, "VALIDER");

        GrilleTarifaire apresCrh = grilleTarifaireRepository.findById(idGrille).orElseThrow();
        assertThat(apresCrh.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_DRH);
        assertThat(apresCrh.getIdDecideurCrh()).isEqualTo(idUtilisateur(CRH_ESSAMA.email()));
        assertThat(apresCrh.getDateDecisionCrh()).isNotNull();
        assertThat(apresCrh.getIdValidateur())
                .as("une décision CRH ne met aucune grille en vigueur — RG-10 ne concerne que l'étape DRH")
                .isNull();
        assertThat(grilleTarifaireRepository.findById(ancienneGrilleActive.getId()).orElseThrow().getDateFin())
                .as("l'ancienne grille reste ouverte tant que la DRH n'a pas statué")
                .isNull();

        // 3. Validation DRH — RG-10 : bascule effective.
        deciderGrille(idGrille, DRH_ATANGANA, "VALIDER");

        GrilleTarifaire apresDrh = grilleTarifaireRepository.findById(idGrille).orElseThrow();
        assertThat(apresDrh.getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);
        assertThat(apresDrh.getDateFin()).isNull();
        assertThat(apresDrh.getIdValidateur()).isEqualTo(idUtilisateur(DRH_ATANGANA.email()));
        assertThat(apresDrh.getDateValidation()).isNotNull();

        assertThat(grilleTarifaireRepository.findById(ancienneGrilleActive.getId()).orElseThrow().getDateFin())
                .as("périodes strictement disjointes : date_fin = dateDebut de la nouvelle moins un jour")
                .isEqualTo(dateDebut.minusDays(1));

        // L'invariant que protège l'index partiel unique idx_grille_active_unique.
        assertThat(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(fonction.getId()))
                .filteredOn(g -> g.getStatutValidation() == StatutGrilleEnum.ACTIVE && g.getDateFin() == null)
                .as("une seule grille ACTIVE sans date_fin par fonction à un instant donné")
                .hasSize(1);
    }

    // ------------------------------------------------------------------
    // Flux 5 — RG-12 évoluée : doublon normal refusé, rattrapage autorisé
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Flux 5 : un second déclenchement normal donne 409, un rattrapage sur période clôturée donne 201")
    void flux5_rattrapageEtUniciteRg12() throws Exception {
        Long idProcessusNormal = declencher(5, 2025, false);

        // RG-12, volet historique : l'unicité mois/année sur les processus normaux.
        mockMvc.perform(post("/processus/declencher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("moisPaiement", 5, "anneePaiement", 2025, "rattrapage", false)))
                        .with(jetonDe(ARH_MBARGA)))
                .andExpect(status().isConflict());

        // Un rattrapage exige un processus normal CLOTURE : sans lui, il n'y a
        // rien à rattraper (400, pas 201).
        mockMvc.perform(post("/processus/declencher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("moisPaiement", 5, "anneePaiement", 2025, "rattrapage", true)))
                        .with(jetonDe(ARH_MBARGA)))
                .andExpect(status().isBadRequest());

        // On clôture le processus normal par le circuit réel.
        valider(idProcessusNormal, ARH_MBARGA, StatutEnum.EN_ATTENTE_CRH, "VALIDATION_ARH");
        valider(idProcessusNormal, CRH_ESSAMA, StatutEnum.EN_ATTENTE_DRH, "VALIDATION_CRH");
        valider(idProcessusNormal, DRH_ATANGANA, StatutEnum.CLOTURE, "VALIDATION_DRH");

        // MM.11 : ce n'est PAS un cas d'échec.
        Long idRattrapage = declencher(5, 2025, true);

        ProcessusMensuel rattrapage = processusMensuelRepository.findById(idRattrapage).orElseThrow();
        assertThat(rattrapage.getRattrapage()).isTrue();
        assertThat(rattrapage.getIdProcessusOriginal()).isEqualTo(idProcessusNormal);
        assertThat(rattrapage.getStatut()).isEqualTo(StatutEnum.EN_COURS_ARH);

        // L'index partiel idx_processus_mensuel_normal_unique, vu par son effet :
        // deux lignes mois/année identiques coexistent dès que l'une est un
        // rattrapage — ce qu'une contrainte UNIQUE globale interdirait.
        List<ProcessusMensuel> periode = processusDeLaPeriode(5, 2025);
        assertThat(periode).hasSize(2);
        assertThat(periode).filteredOn(p -> !p.getRattrapage()).hasSize(1);

        // Plusieurs rattrapages successifs sur la même période sont autorisés.
        Long idSecondRattrapage = declencher(5, 2025, true);
        assertThat(idSecondRattrapage).isNotEqualTo(idRattrapage);
        assertThat(processusDeLaPeriode(5, 2025)).hasSize(3);

        // Le processus normal dont découle le rattrapage n'est jamais modifié.
        ProcessusMensuel normalApresRattrapage = processusMensuelRepository.findById(idProcessusNormal).orElseThrow();
        assertThat(normalApresRattrapage.getStatut()).isEqualTo(StatutEnum.CLOTURE);
        assertThat(normalApresRattrapage.getRattrapage()).isFalse();
        assertThat(normalApresRattrapage.getIdProcessusOriginal()).isNull();

        // Les bénéficiaires déjà payés par le processus normal sont exclus par
        // défaut du rattrapage (décision A2) — aucun double paiement possible.
        Set<Long> dejaPayes = ligneEtatMensuelRepository
                .findByIdProcessusAndInclusDansEtatTrue(idProcessusNormal).stream()
                .map(LigneEtatMensuel::getIdBeneficiaire)
                .collect(Collectors.toSet());
        assertThat(dejaPayes).isNotEmpty();
        assertThat(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(idRattrapage))
                .extracting(LigneEtatMensuel::getIdBeneficiaire)
                .doesNotContainAnyElementsOf(dejaPayes);
    }

    // ------------------------------------------------------------------
    // Flux 6 — RG-08 face à une PROMOTION de rôle
    // ------------------------------------------------------------------

    /**
     * Le risque réel identifié au Sprint MM.12 n'est pas « deux rôles portés par
     * la même personne au même instant » — structurellement impossible, un
     * utilisateur DOTTEL n'a qu'un rôle, et
     * {@code RoleJwtAuthenticationConverter} refuse tout cumul. C'est une
     * PROMOTION entre deux étapes d'un même circuit.
     *
     * <p>Ce test est plus exigeant qu'un contrôle de rôle : au moment de la
     * seconde tentative, {@code @PreAuthorize} passe, et
     * {@code verifierRoleAttendu()} (RG-05) passe aussi, puisque l'acteur porte
     * désormais bel et bien le rôle attendu par l'étape. Seule la comparaison
     * d'ACTEUR peut encore refuser. Le 403 prouve donc que le garde-fou survit
     * à un changement de rôle.</p>
     *
     * <p>Le contre-exemple final est ce qui rend le 403 concluant : le circuit
     * s'achève normalement avec un autre acteur du même rôle. Sans lui, un 403
     * dû à une cause parasite serait indiscernable du résultat attendu.</p>
     */
    @Test
    @DisplayName("Flux 6a : promu CRH après avoir validé l'étape ARH, le même acteur ne peut pas valider l'étape CRH du même processus (403)")
    void flux6a_separationDesTachesApresPromotionSurUnProcessus() throws Exception {
        Long idProcessus = declencher(6, 2025, false);
        valider(idProcessus, ARH_MBARGA, StatutEnum.EN_ATTENTE_CRH, "VALIDATION_ARH");

        Long idActeurArh = idUtilisateur(ARH_MBARGA.email());
        assertThat(etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(idProcessus))
                .filteredOn(e -> e.getNomEtape() == NomEtapeEnum.VALIDATION_ARH)
                .singleElement()
                .satisfies(e -> assertThat(e.getIdActeur()).isEqualTo(idActeurArh));

        // L'ADMIN promeut l'acteur de l'étape ARH au rôle CRH.
        promouvoir(idActeurArh, RoleEnum.CRH);
        assertThat(utilisateurRepository.findById(idActeurArh).orElseThrow().getRole())
                .isEqualTo(RoleEnum.CRH);

        // Keycloak émettrait désormais un jeton portant CRH pour cette identité.
        mockMvc.perform(post("/processus/" + idProcessus + "/valider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(jetonDe(ARH_MBARGA.email(), RoleEnum.CRH.name())))
                .andExpect(status().isForbidden());

        assertThat(processusMensuelRepository.findById(idProcessus).orElseThrow().getStatut())
                .as("le refus RG-08 ne doit avoir produit aucune transition")
                .isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(idProcessus))
                .as("aucune étape CRH n'a été écrite")
                .filteredOn(e -> e.getNomEtape() == NomEtapeEnum.VALIDATION_CRH)
                .isEmpty();

        // Contre-exemple : un AUTRE acteur, de rôle CRH lui aussi, valide sans
        // difficulté. Le 403 ci-dessus tient donc bien à l'identité de l'acteur.
        valider(idProcessus, CRH_ESSAMA, StatutEnum.EN_ATTENTE_DRH, "VALIDATION_CRH");
    }

    @Test
    @DisplayName("Flux 6b : promue DRH après avoir statué en CRH, la même actrice ne peut pas valider la même grille (403)")
    void flux6b_separationDesTachesApresPromotionSurUneGrille() throws Exception {
        FonctionEligible fonction = fonctionEligibleRepository.findByCode(FONCTION_GRILLE_FLUX6).orElseThrow();
        Long idGrille = creerGrille(FONCTION_GRILLE_FLUX6, 42000, prochaineDateDebutValide(fonction));

        deciderGrille(idGrille, CRH_ESSAMA, "VALIDER");

        Long idDecideurCrh = idUtilisateur(CRH_ESSAMA.email());
        assertThat(grilleTarifaireRepository.findById(idGrille).orElseThrow().getIdDecideurCrh())
                .isEqualTo(idDecideurCrh);

        // L'ADMIN promeut la décideuse CRH au rôle DRH.
        promouvoir(idDecideurCrh, RoleEnum.DRH);
        assertThat(utilisateurRepository.findById(idDecideurCrh).orElseThrow().getRole())
                .isEqualTo(RoleEnum.DRH);

        mockMvc.perform(post("/grilles-tarifaires/" + idGrille + "/valider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("decision", "VALIDER")))
                        .with(jetonDe(CRH_ESSAMA.email(), RoleEnum.DRH.name())))
                .andExpect(status().isForbidden());

        GrilleTarifaire apresRefus = grilleTarifaireRepository.findById(idGrille).orElseThrow();
        assertThat(apresRefus.getStatutValidation())
                .as("le refus RG-08 ne doit avoir produit aucune transition")
                .isEqualTo(StatutGrilleEnum.EN_ATTENTE_DRH);
        assertThat(apresRefus.getIdValidateur()).isNull();
        assertThat(apresRefus.getDateValidation()).isNull();

        // Contre-exemple : la DRH d'origine valide sans difficulté.
        deciderGrille(idGrille, DRH_ATANGANA, "VALIDER");
        assertThat(grilleTarifaireRepository.findById(idGrille).orElseThrow().getStatutValidation())
                .isEqualTo(StatutGrilleEnum.ACTIVE);
    }

    // ------------------------------------------------------------------
    // Utilitaires partagés par les flux
    // ------------------------------------------------------------------

    private String corpsJson(Object valeur) throws Exception {
        return objectMapper.writeValueAsString(valeur);
    }

    /** POST /processus/declencher en tant qu'ARH, retourne l'id créé. */
    private Long declencher(int mois, int annee, boolean rattrapage) throws Exception {
        String reponse = mockMvc.perform(post("/processus/declencher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("moisPaiement", mois,
                                "anneePaiement", annee,
                                "rattrapage", rattrapage)))
                        .with(jetonDe(ARH_MBARGA)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(reponse).get("id").asLong();
    }

    /**
     * POST /processus/{id}/valider : vérifie le statut atteint et l'étape
     * validée, retourne l'id de la pièce jointe portée par la réponse.
     */
    private Long valider(Long idProcessus, SupportAuthentificationTest.CompteTest compte,
                         StatutEnum statutAttendu, String etapeAttendue) throws Exception {
        String reponse = mockMvc.perform(requeteValidation(idProcessus, compte))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(statutAttendu.name()))
                .andExpect(jsonPath("$.etapeValidee").value(etapeAttendue))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode corps = objectMapper.readTree(reponse);
        return corps.get("idPieceJointe").asLong();
    }

    private MockHttpServletRequestBuilder requeteValidation(Long idProcessus,
                                                            SupportAuthentificationTest.CompteTest compte) {
        return post("/processus/" + idProcessus + "/valider")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(jetonDe(compte));
    }

    /** POST /grilles-tarifaires en tant qu'ARH, retourne l'id créé. */
    private Long creerGrille(String codeFonction, int montantFcfa, LocalDate dateDebut) throws Exception {
        String reponse = mockMvc.perform(post("/grilles-tarifaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("codeFonction", codeFonction,
                                "montantFcfa", montantFcfa,
                                "dateDebut", dateDebut.toString())))
                        .with(jetonDe(ARH_MBARGA)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(reponse).get("id").asLong();
    }

    /** POST /grilles-tarifaires/{id}/valider, endpoint partagé par le CRH et la DRH. */
    private void deciderGrille(Long idGrille, SupportAuthentificationTest.CompteTest compte, String decision)
            throws Exception {
        mockMvc.perform(post("/grilles-tarifaires/" + idGrille + "/valider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("decision", decision)))
                        .with(jetonDe(compte)))
                .andExpect(status().isOk());
    }

    /**
     * Première date de début acceptable pour une nouvelle grille de cette
     * fonction : lendemain de la borne de la dernière grille non rejetée.
     *
     * <p>Calculée depuis la base plutôt que codée en dur, pour que le test ne
     * dépende pas de l'historique tarifaire du poste sur lequel il tourne.</p>
     */
    private LocalDate prochaineDateDebutValide(FonctionEligible fonction) {
        LocalDate borne = grilleTarifaireRepository
                .findByIdFonctionEligibleOrderByDateDebutDesc(fonction.getId()).stream()
                .filter(g -> g.getStatutValidation() != StatutGrilleEnum.REJETEE)
                .findFirst()
                .map(g -> g.getDateFin() != null ? g.getDateFin() : g.getDateDebut())
                .orElse(LocalDate.now());
        return borne.plusDays(1);
    }

    private List<ProcessusMensuel> processusDeLaPeriode(int mois, int annee) {
        return processusMensuelRepository.findAll().stream()
                .filter(p -> p.getMoisPaiement() == mois && p.getAnneePaiement() == annee)
                .toList();
    }

    /** PATCH /admin/utilisateurs/{id}/role en tant qu'ADMIN. */
    private void promouvoir(Long idUtilisateur, RoleEnum nouveauRole) throws Exception {
        mockMvc.perform(patch("/admin/utilisateurs/" + idUtilisateur + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsJson(Map.of("role", nouveauRole.name())))
                        .with(jetonDe(ADMIN_TCHINDA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(nouveauRole.name()));
    }

    private Long idUtilisateur(String email) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findByEmail(email);
        assertThat(utilisateur)
                .as("compte de test %s attendu en base (Flyway V3)", email)
                .isPresent();
        return utilisateur.get().getId();
    }
}
