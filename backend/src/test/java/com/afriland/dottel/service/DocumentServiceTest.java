package com.afriland.dottel.service;

import com.afriland.dottel.model.entity.Beneficiaire;
import com.afriland.dottel.model.entity.FonctionEligible;
import com.afriland.dottel.model.entity.LigneEtatMensuel;
import com.afriland.dottel.model.entity.PieceJointe;
import com.afriland.dottel.model.entity.ProcessusMensuel;
import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.NomEtapeEnum;
import com.afriland.dottel.model.enums.StatutEnum;
import com.afriland.dottel.repository.BeneficiaireRepository;
import com.afriland.dottel.repository.FonctionEligibleRepository;
import com.afriland.dottel.repository.PieceJointeRepository;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormCreator;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @Mock
    private FonctionEligibleRepository fonctionEligibleRepository;

    @Mock
    private PieceJointeRepository pieceJointeRepository;

    @Mock
    private EcartMensuelService ecartMensuelService;

    @Mock
    private SignatureService signatureService;

    @TempDir
    Path dossierTemporaire;

    @Test
    void genererInitiale_chapitreAbsentDeLehr_utiliseValeurDeRepliConfiguree() throws IOException {
        DocumentService documentService = new DocumentService(beneficiaireRepository, fonctionEligibleRepository,
                pieceJointeRepository, ecartMensuelService, signatureService);
        ReflectionTestUtils.setField(documentService, "cheminStockage", dossierTemporaire.toString() + "/");
        ReflectionTestUtils.setField(documentService, "chapitreDefaut", "37210199");

        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).build();

        // Beneficiaire enrole avant le Sprint 3.4 : aucune valeur de chapitre
        // fournie par l'EHR a l'epoque -> le repli configure doit s'appliquer.
        Beneficiaire ndongo = Beneficiaire.builder().id(510L).nomPrenoms("NDONGO Béatrice")
                .codeUnite("YDE-IG01").numCompteCourant("10019720002").chapitre(null).build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder().idProcessus(900L).idBeneficiaire(510L)
                .montantApplique(65000).inclusDansEtat(true).fonctionRetenue("CORPS_CONTROLE_IGA").build();

        Utilisateur arh = Utilisateur.builder().id(10L).nom("MBARGA").prenom("Jean-Paul").matricule("2201").build();

        when(beneficiaireRepository.findById(510L)).thenReturn(Optional.of(ndongo));
        when(fonctionEligibleRepository.findByCode("CORPS_CONTROLE_IGA")).thenReturn(Optional.of(
                FonctionEligible.builder().code("CORPS_CONTROLE_IGA").libelle("Inspecteur Général Adjoint").build()));
        when(ecartMensuelService.rechercherResultatMensuel(any(), any()))
                .thenReturn(new EcartMensuelService.ResultatEcartMensuel(null, null));
        when(signatureService.signer(arh)).thenReturn("Jean-Paul MBARGA (matricule 2201) - 22/07/2026 10:00:00");
        when(pieceJointeRepository.save(any(PieceJointe.class))).thenAnswer(invocation -> {
            PieceJointe pieceJointe = invocation.getArgument(0);
            pieceJointe.setId(1L);
            return pieceJointe;
        });

        PieceJointe pieceJointe = documentService.genererInitiale(processus, List.of(ligne), arh);

        Path fichierGenere = Path.of(pieceJointe.getCheminStockage());
        assertThat(fichierGenere).exists();

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(fichierGenere.toString()))) {
            String texte = PdfTextExtractor.getTextFromPage(pdfDocument.getFirstPage());
            assertThat(texte).contains("37210199");
        }
    }

    @Test
    void genererInitiale_libelleEtNomAccentues_rendusCorrectementDansLePdf() throws IOException {
        // Police par defaut Helvetica/StandardEncoding ne rend pas les accents
        // francais -> verifie que le fix CP1252 (PdfFontFactory.createFont)
        // preserve "Contrôleur Comptable" et "NDONGO Béatrice" a l'extraction.
        DocumentService documentService = new DocumentService(beneficiaireRepository, fonctionEligibleRepository,
                pieceJointeRepository, ecartMensuelService, signatureService);
        ReflectionTestUtils.setField(documentService, "cheminStockage", dossierTemporaire.toString() + "/");
        ReflectionTestUtils.setField(documentService, "chapitreDefaut", "37210100");

        ProcessusMensuel processus = ProcessusMensuel.builder().id(901L).moisPaiement(8).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).build();

        Beneficiaire beneficiaire = Beneficiaire.builder().id(520L).nomPrenoms("NDONGO Béatrice")
                .codeUnite("YDE-IG01").numCompteCourant("10019720002").chapitre("37210170").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder().idProcessus(901L).idBeneficiaire(520L)
                .montantApplique(35000).inclusDansEtat(true).fonctionRetenue("CONTROLEUR_COMPTABLE").build();

        Utilisateur arh = Utilisateur.builder().id(10L).nom("MBARGA").prenom("Jean-Paul").matricule("2201").build();

        when(beneficiaireRepository.findById(520L)).thenReturn(Optional.of(beneficiaire));
        when(fonctionEligibleRepository.findByCode("CONTROLEUR_COMPTABLE")).thenReturn(Optional.of(
                FonctionEligible.builder().code("CONTROLEUR_COMPTABLE").libelle("Contrôleur Comptable").build()));
        when(ecartMensuelService.rechercherResultatMensuel(any(), any()))
                .thenReturn(new EcartMensuelService.ResultatEcartMensuel(null, null));
        when(signatureService.signer(arh)).thenReturn("Jean-Paul MBARGA (matricule 2201) - 22/07/2026 10:00:00");
        when(pieceJointeRepository.save(any(PieceJointe.class))).thenAnswer(invocation -> {
            PieceJointe pieceJointe = invocation.getArgument(0);
            pieceJointe.setId(2L);
            return pieceJointe;
        });

        PieceJointe pieceJointe = documentService.genererInitiale(processus, List.of(ligne), arh);

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(pieceJointe.getCheminStockage()))) {
            String texte = PdfTextExtractor.getTextFromPage(pdfDocument.getFirstPage());
            // "Contrôleur Comptable" est verifie mot par mot plutot qu'en phrase
            // contigue : la colonne FONCTION est etroite et le libelle est
            // retourne a la ligne, ce qui reordonne son extraction par
            // PdfTextExtractor (artefact de mise en page, sans lien avec
            // l'encodage des accents).
            assertThat(texte).contains("Contrôleur");
            assertThat(texte).contains("Comptable");
            assertThat(texte).contains("NDONGO Béatrice");
        }
    }

    @Test
    void genererInitiale_pieceJointeExistante_laRemplaceEnPlaceSansDoublon() throws IOException {
        // Sprint 5.4 (RG-06) : lors de la revalidation ARH d'un processus
        // RETOURNE apres une validation CRH/DRH, un document existe deja pour
        // ce processus (id_processus est UNIQUE en base). genererInitiale()
        // doit le remplacer en place -- meme id -- plutot que d'en creer un
        // second, qui violerait la contrainte d'unicite.
        DocumentService documentService = new DocumentService(beneficiaireRepository, fonctionEligibleRepository,
                pieceJointeRepository, ecartMensuelService, signatureService);
        ReflectionTestUtils.setField(documentService, "cheminStockage", dossierTemporaire.toString() + "/");
        ReflectionTestUtils.setField(documentService, "chapitreDefaut", "37210199");

        ProcessusMensuel processus = ProcessusMensuel.builder().id(903L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.RETOURNE).build();

        Beneficiaire beneficiaire = Beneficiaire.builder().id(540L).nomPrenoms("ONANA Serge")
                .codeUnite("DLA-AG05").numCompteCourant("10033450009").chapitre("37210170").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder().idProcessus(903L).idBeneficiaire(540L)
                .montantApplique(50000).inclusDansEtat(true).fonctionRetenue("DA").build();

        Utilisateur arh = Utilisateur.builder().id(10L).nom("MBARGA").prenom("Jean-Paul").matricule("2201").build();

        // Document existant : nombre_signatures=3 (ARH+CRH+DRH), retourne
        // ensuite par la DRH -> le processus repasse RETOURNE puis l'ARH corrige
        // et revalide, ce qui declenche cette regeneration.
        PieceJointe pieceJointeExistante = PieceJointe.builder().id(50L).idProcessus(903L)
                .nomFichier("dotations-telephoniques-6-2026.pdf").cheminStockage("ancien-chemin.pdf")
                .nombreSignatures(3).build();

        when(beneficiaireRepository.findById(540L)).thenReturn(Optional.of(beneficiaire));
        when(fonctionEligibleRepository.findByCode("DA")).thenReturn(Optional.of(
                FonctionEligible.builder().code("DA").libelle("Directeur d'Agence").build()));
        when(ecartMensuelService.rechercherResultatMensuel(any(), any()))
                .thenReturn(new EcartMensuelService.ResultatEcartMensuel(null, null));
        when(signatureService.signer(arh)).thenReturn("Jean-Paul MBARGA (matricule 2201) - 24/07/2026 11:00:00");
        when(pieceJointeRepository.findByIdProcessus(903L)).thenReturn(Optional.of(pieceJointeExistante));
        when(pieceJointeRepository.save(any(PieceJointe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PieceJointe pieceJointeRegeneree = documentService.genererInitiale(processus, List.of(ligne), arh);

        assertThat(pieceJointeRegeneree.getId()).isEqualTo(50L);
        assertThat(pieceJointeRegeneree.getNombreSignatures()).isEqualTo(1);
        assertThat(pieceJointeRegeneree.getNomFichier()).isEqualTo("dotations-telephoniques-7-2026.pdf");

        ArgumentCaptor<PieceJointe> captor = ArgumentCaptor.forClass(PieceJointe.class);
        verify(pieceJointeRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(50L);
    }

    private PieceJointe genererPieceJointeInitiale(DocumentService documentService, Utilisateur arh) {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(902L).moisPaiement(9).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).build();

        Beneficiaire beneficiaire = Beneficiaire.builder().id(530L).nomPrenoms("ESSAMA Paul")
                .codeUnite("DLA-AG03").numCompteCourant("10023410005").chapitre("37210170").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder().idProcessus(902L).idBeneficiaire(530L)
                .montantApplique(40000).inclusDansEtat(true).fonctionRetenue("GFC").build();

        when(beneficiaireRepository.findById(530L)).thenReturn(Optional.of(beneficiaire));
        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(
                FonctionEligible.builder().code("GFC").libelle("Gestionnaire de Fonds de Commerce").build()));
        when(ecartMensuelService.rechercherResultatMensuel(any(), any()))
                .thenReturn(new EcartMensuelService.ResultatEcartMensuel(null, null));
        when(signatureService.signer(arh)).thenReturn("Jean-Paul MBARGA (matricule 2201) - 22/07/2026 10:00:00");
        when(pieceJointeRepository.save(any(PieceJointe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        return documentService.genererInitiale(processus, List.of(ligne), arh);
    }

    @Test
    void ajouterSignature_casNominal_incrementeNombreSignatures() throws IOException {
        DocumentService documentService = new DocumentService(beneficiaireRepository, fonctionEligibleRepository,
                pieceJointeRepository, ecartMensuelService, signatureService);
        ReflectionTestUtils.setField(documentService, "cheminStockage", dossierTemporaire.toString() + "/");
        ReflectionTestUtils.setField(documentService, "chapitreDefaut", "37210199");

        Utilisateur arh = Utilisateur.builder().id(10L).nom("MBARGA").prenom("Jean-Paul").matricule("2201").build();
        Utilisateur crh = Utilisateur.builder().id(20L).nom("NKOLO").prenom("Alphonse").matricule("3305").build();

        PieceJointe pieceJointe = genererPieceJointeInitiale(documentService, arh);
        pieceJointe.setId(3L);
        assertThat(pieceJointe.getNombreSignatures()).isEqualTo(1);

        when(signatureService.signer(crh)).thenReturn("Alphonse NKOLO (matricule 3305) - 23/07/2026 09:00:00");

        PieceJointe pieceJointeMiseAJour = documentService.ajouterSignature(pieceJointe, crh, NomEtapeEnum.VALIDATION_CRH);

        assertThat(pieceJointeMiseAJour.getNombreSignatures()).isEqualTo(2);

        // Le bloc de signature CRH est aplati (partialFormFlattening) apres
        // avoir recu sa valeur : il devient du texte fixe dans le flux de
        // contenu de la page, au meme titre que le bloc ARH, au lieu de rester
        // un widget AcroForm modifiable. PdfTextExtractor le voit donc
        // directement, sans passer par le champ de formulaire.
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(pieceJointeMiseAJour.getCheminStockage()))) {
            String texte = PdfTextExtractor.getTextFromPage(pdfDocument.getFirstPage());
            assertThat(texte).contains("NKOLO");

            PdfAcroForm formulaire = PdfFormCreator.getAcroForm(pdfDocument, false);
            assertThat(formulaire.getField("signatureCrh")).isNull();
        }
    }

    @Test
    void ajouterSignature_neCreePasUnNouveauFichier() {
        DocumentService documentService = new DocumentService(beneficiaireRepository, fonctionEligibleRepository,
                pieceJointeRepository, ecartMensuelService, signatureService);
        ReflectionTestUtils.setField(documentService, "cheminStockage", dossierTemporaire.toString() + "/");
        ReflectionTestUtils.setField(documentService, "chapitreDefaut", "37210199");

        Utilisateur arh = Utilisateur.builder().id(10L).nom("MBARGA").prenom("Jean-Paul").matricule("2201").build();
        Utilisateur drh = Utilisateur.builder().id(30L).nom("ATANGANA").prenom("Marie").matricule("4102").build();

        PieceJointe pieceJointe = genererPieceJointeInitiale(documentService, arh);
        pieceJointe.setId(4L);
        String cheminAvant = pieceJointe.getCheminStockage();

        when(signatureService.signer(drh)).thenReturn("Marie ATANGANA (matricule 4102) - 23/07/2026 09:30:00");

        PieceJointe pieceJointeMiseAJour = documentService.ajouterSignature(pieceJointe, drh, NomEtapeEnum.VALIDATION_DRH);

        assertThat(pieceJointeMiseAJour.getCheminStockage()).isEqualTo(cheminAvant);
        assertThat(Path.of(cheminAvant)).exists();
        assertThat(Path.of(cheminAvant + ".tmp")).doesNotExist();
    }
}