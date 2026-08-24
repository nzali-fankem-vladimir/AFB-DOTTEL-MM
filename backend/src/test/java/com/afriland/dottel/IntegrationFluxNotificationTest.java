package com.afriland.dottel;

import com.afriland.dottel.audit.model.entity.AuditLog;
import com.afriland.dottel.audit.repository.AuditLogRepository;
import com.afriland.dottel.notifications.service.NotificationService;
import com.afriland.dottel.processus.api.EvenementClotureDto;
import com.afriland.dottel.processus.model.entity.LigneEtatMensuel;
import com.afriland.dottel.processus.model.entity.PieceJointe;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import com.afriland.dottel.processus.repository.EtapeWorkflowRepository;
import com.afriland.dottel.processus.repository.LigneEtatMensuelRepository;
import com.afriland.dottel.processus.repository.PieceJointeRepository;
import com.afriland.dottel.processus.repository.ProcessusMensuelRepository;
import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.afriland.dottel.SupportAuthentificationTest.CompteTest.ARH_MBARGA;
import static com.afriland.dottel.SupportAuthentificationTest.CompteTest.CRH_ESSAMA;
import static com.afriland.dottel.SupportAuthentificationTest.jetonDe;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flux 7 — notifications (Sprint MM.13), et pourquoi ce test vit à part.
 *
 * <h2>Le piège AFTER_COMMIT</h2>
 *
 * {@code NotificationEventListener} écoute en
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} — décision N-1 :
 * une notification est un confort, l'envoyer avant le commit ferait partir un
 * mail pour un changement qu'un rollback annulerait ensuite.
 *
 * <p>Conséquence directe pour les tests : un test {@code @Transactional}
 * classique <b>ne commite jamais</b>, donc ce listener <b>ne se déclenche
 * jamais</b>. Vérifier la notification depuis
 * {@link IntegrationFluxMetierTest} produirait un test vert qui ne prouve rien,
 * ou rouge sans que le code de production soit en cause. D'où une classe
 * distincte, délibérément <b>non transactionnelle</b> : la transaction du
 * service commite réellement, exactement comme en production.</p>
 *
 * <h2>Contrepartie : nettoyage explicite</h2>
 *
 * Sans rollback, les écritures persistent. {@link #nettoyer()} supprime tout ce
 * que le test a produit — y compris les entrées d'audit, écrites elles en
 * {@code BEFORE_COMMIT} — dans un ordre inverse des dépendances. Le nettoyage
 * est en {@code @AfterEach} plutôt qu'en fin de méthode : il s'exécute même si
 * une assertion échoue en cours de route.
 *
 * <h2>Effet de bord utile</h2>
 *
 * Le commit étant réel, ce test est aussi le seul à observer l'écriture
 * effective du journal d'audit (RG-09) — invisible partout ailleurs, puisque
 * {@code AuditEventListener} est lui aussi transactionnel.
 */
@SpringBootTest(properties =
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + SupportAuthentificationTest.ISSUER_URI_DE_TEST)
@AutoConfigureMockMvc
@Import(SupportAuthentificationTest.JwtDecoderDeTest.class)
class IntegrationFluxNotificationTest {

    private static final int MOIS = 7;
    private static final int ANNEE = 2025;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private AuditLogRepository auditLogRepository;

    /**
     * Frontière légitime (section 1.3 du guide) : l'implémentation par défaut
     * est déjà {@code NotificationServiceStub}, qui se contente de journaliser.
     * La substituer par un mock est la manière la plus directe de prouver
     * qu'un événement métier a bien déclenché un envoi, et vers qui.
     */
    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean(name = "kafkaTemplateEvenementCloture")
    private KafkaTemplate<String, EvenementClotureDto> kafkaTemplateEvenementCloture;

    @Value("${dottel.documents.chemin-stockage}")
    private String cheminStockageDocuments;

    private Long idProcessus;
    private Set<String> documentsPreexistants = Set.of();

    @BeforeEach
    void memoriserLesDocumentsPreexistants() throws IOException {
        documentsPreexistants = listerDocuments();
    }

    @Test
    @DisplayName("Flux 7 : la validation ARH, une fois réellement commitée, notifie le CRH — et laisse une trace d'audit")
    void flux7_notificationApresCommitReel() throws Exception {
        // 1. Déclenchement. La transaction du service commite ici pour de bon :
        //    la ligne est visible depuis une lecture ultérieure indépendante.
        String reponseDeclenchement = mockMvc.perform(post("/processus/declencher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "moisPaiement", MOIS, "anneePaiement", ANNEE, "rattrapage", false)))
                        .with(jetonDe(ARH_MBARGA)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        idProcessus = objectMapper.readTree(reponseDeclenchement).get("id").asLong();

        assertThat(processusMensuelRepository.findById(idProcessus))
                .as("hors transaction de test : la donnée est réellement en base, pas seulement en mémoire")
                .isPresent();

        // 2. Validation ARH. C'est le commit de CETTE transaction qui doit
        //    déclencher le listener AFTER_COMMIT.
        mockMvc.perform(post("/processus/" + idProcessus + "/valider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(jetonDe(ARH_MBARGA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(StatutEnum.EN_ATTENTE_CRH.name()));

        // 3. La notification est bien partie, vers le bon destinataire.
        ArgumentCaptor<DestinataireNotificationDto> destinataire =
                ArgumentCaptor.forClass(DestinataireNotificationDto.class);
        ArgumentCaptor<String> sujet = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        verify(notificationService, atLeastOnce())
                .notifier(destinataire.capture(), sujet.capture(), message.capture());

        List<String> crhActifs = utilisateurRepository.findByRoleAndActifTrue(RoleEnum.CRH).stream()
                .map(utilisateur -> utilisateur.getEmail())
                .toList();
        assertThat(crhActifs)
                .as("préalable du flux : au moins un CRH actif doit exister pour être notifié")
                .contains(CRH_ESSAMA.email());

        assertThat(destinataire.getAllValues())
                .as("la validation ARH s'adresse aux CRH, seuls acteurs de l'étape suivante")
                .allSatisfy(cible -> assertThat(cible.role()).isEqualTo(RoleEnum.CRH))
                .extracting(DestinataireNotificationDto::email)
                .containsExactlyInAnyOrderElementsOf(crhActifs);

        assertThat(sujet.getAllValues()).allSatisfy(s -> assertThat(s).isEqualTo("Etat mensuel a valider"));
        assertThat(message.getAllValues())
                .allSatisfy(m -> assertThat(m).contains(MOIS + "/" + ANNEE));

        // 4. Effet de bord observable seulement ici : le commit étant réel,
        //    AuditEventListener (BEFORE_COMMIT) a écrit ses entrées (RG-09).
        assertThat(auditLogRepository.findAll().stream()
                .filter(entree -> "processus_mensuel".equals(entree.getEntiteCible())
                        && idProcessus.equals(entree.getIdEntite()))
                .map(AuditLog::getAction))
                .contains("DECLENCHEMENT_PROCESSUS", "VALIDATION_PROCESSUS_ARH");
    }

    /**
     * Suppression de tout ce que le test a réellement écrit, dans l'ordre
     * inverse des dépendances. Aucun {@code deleteAll} : seules les lignes
     * rattachées au processus de ce test sont visées.
     */
    @AfterEach
    void nettoyer() throws IOException {
        if (idProcessus != null) {
            auditLogRepository.deleteAll(auditLogRepository.findAll().stream()
                    .filter(entree -> idProcessus.equals(entree.getIdEntite())
                            && "processus_mensuel".equals(entree.getEntiteCible()))
                    .toList());

            List<LigneEtatMensuel> lignes = ligneEtatMensuelRepository.findByIdProcessus(idProcessus);
            auditLogRepository.deleteAll(auditLogRepository.findAll().stream()
                    .filter(entree -> "ligne_etat_mensuel".equals(entree.getEntiteCible())
                            && lignes.stream().anyMatch(ligne -> ligne.getId().equals(entree.getIdEntite())))
                    .toList());

            etapeWorkflowRepository.deleteAll(
                    etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(idProcessus));
            pieceJointeRepository.findByIdProcessus(idProcessus).ifPresent(pieceJointeRepository::delete);
            ligneEtatMensuelRepository.deleteAll(lignes);
            processusMensuelRepository.findById(idProcessus).ifPresent(processusMensuelRepository::delete);

            idProcessus = null;
        }

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
            return new HashSet<>(fichiers.map(fichier -> fichier.getFileName().toString()).toList());
        }
    }
}
