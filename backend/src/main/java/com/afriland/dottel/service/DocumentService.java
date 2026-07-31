package com.afriland.dottel.service;

import com.afriland.dottel.model.entity.Beneficiaire;
import com.afriland.dottel.model.entity.FonctionEligible;
import com.afriland.dottel.model.entity.LigneEtatMensuel;
import com.afriland.dottel.model.entity.PieceJointe;
import com.afriland.dottel.model.entity.ProcessusMensuel;
import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.NomEtapeEnum;
import com.afriland.dottel.repository.BeneficiaireRepository;
import com.afriland.dottel.repository.FonctionEligibleRepository;
import com.afriland.dottel.repository.PieceJointeRepository;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormCreator;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.form.element.TextArea;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final String[] NOMS_MOIS = {
            "JANVIER", "FEVRIER", "MARS", "AVRIL", "MAI", "JUIN",
            "JUILLET", "AOUT", "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DECEMBRE"
    };

    // Noms des champs AcroForm places dans le bloc de signature a la generation
    // initiale (voir ajouterBlocsSignature), et remplis en place par
    // ajouterSignature() sans jamais recreer le fichier (RG-06).
    private static final String CHAMP_SIGNATURE_CRH = "signatureCrh";
    private static final String CHAMP_SIGNATURE_DRH = "signatureDrh";

    // Table de 3 colonnes egales sur page A4 paysage, marges par defaut 36pt :
    // (841.89 - 72) / 3 ~= 256pt de large par colonne, padding de cellule
    // deduit. Valeurs choisies pour laisser respirer un texte de signature sur
    // 2-3 lignes sans jamais toucher les bords de la colonne.
    private static final float LARGEUR_ZONE_SIGNATURE = 230f;
    private static final float HAUTEUR_ZONE_SIGNATURE = 60f;

    private final BeneficiaireRepository beneficiaireRepository;
    private final FonctionEligibleRepository fonctionEligibleRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final EcartMensuelService ecartMensuelService;
    private final SignatureService signatureService;

    @Value("${dottel.documents.chemin-stockage}")
    private String cheminStockage;

    @Value("${dottel.documents.chapitre-defaut}")
    private String chapitreDefaut;

    // Note : le contrat CLAUDE.md decrit genererInitiale(processus, lignes) a
    // deux parametres, mais le bloc de signature ARH (etape 5, point 4) exige
    // l'identite de l'acteur qui valide. Ce 3e parametre est ajoute pour cette
    // raison plutot que de faire dependre DocumentService du contexte de
    // securite (AuthenticatedUserService) : il reste ainsi un service pur,
    // facilement testable, l'appelant (ProcessusMensuelService.valider())
    // possede deja l'entite Utilisateur de l'acteur connecte.
    @Transactional
    public PieceJointe genererInitiale(ProcessusMensuel processus, List<LigneEtatMensuel> lignes, Utilisateur acteurValidation) {
        String nomFichier = "dotations-telephoniques-" + processus.getMoisPaiement() + "-" + processus.getAnneePaiement() + ".pdf";
        String cheminComplet = cheminStockage + nomFichier;

        try {
            Files.createDirectories(Path.of(cheminStockage));

            // Police par defaut standard = Helvetica en encodage StandardEncoding
            // (ASCII), qui ne supporte pas les caracteres accentues francais. On
            // force l'encodage CP1252 (Windows-1252), qui couvre les accents
            // francais utilises dans les libelles de fonction et les noms
            // camerounais (é, è, à, ô, ...).
            PdfFont police = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.CP1252);

            try (PdfWriter writer = new PdfWriter(cheminComplet);
                 PdfDocument pdfDocument = new PdfDocument(writer);
                 Document document = new Document(pdfDocument, PageSize.A4.rotate())) {

                document.setFont(police);

                ajouterEnTete(document, processus);
                ajouterTableau(document, processus, lignes);
                ajouterBlocsSignature(document, acteurValidation, police);

                // Lecture seule des la generation : Chrome/Edge/Adobe surlignent
                // en bleu tout champ AcroForm interactif encore vide, meme sans
                // bordure ni fond visibles -- un simple effet d'affichage du
                // lecteur, pas du contenu du PDF. Un champ ReadOnly n'est plus
                // signale comme "a remplir" par ces lecteurs, sans empecher
                // notre propre code (ajouterSignature) d'y ecrire puis de le
                // supprimer au moment voulu.
                PdfAcroForm formulaire = PdfFormCreator.getAcroForm(pdfDocument, false);
                formulaire.getField(CHAMP_SIGNATURE_CRH).setReadOnly(true);
                formulaire.getField(CHAMP_SIGNATURE_DRH).setReadOnly(true);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Echec de la generation du PDF pour le processus " + processus.getId(), e);
        }

        // RG-06 : au plus un enregistrement PieceJointe par processus
        // (UNIQUE(id_processus)). Si un document existe deja (revalidation ARH
        // apres un retour CRH/DRH -- Sprint 5.4), on le remplace en place plutot
        // que d'en creer un second, qui violerait la contrainte d'unicite. Le
        // compteur de signatures redemarre a 1 : les signatures CRH/DRH
        // anterieures portaient sur un contenu qui vient de changer.
        PieceJointe pieceJointe = pieceJointeRepository.findByIdProcessus(processus.getId())
                .orElseGet(() -> PieceJointe.builder().idProcessus(processus.getId()).build());

        pieceJointe.setNomFichier(nomFichier);
        pieceJointe.setCheminStockage(cheminComplet);
        pieceJointe.setDateGenerationInitiale(LocalDateTime.now());
        pieceJointe.setDateDerniereMiseAJour(LocalDateTime.now());
        pieceJointe.setNombreSignatures(1);

        return pieceJointeRepository.save(pieceJointe);
    }

    // Edition en place du PDF existant (RG-06 : jamais de nouveau fichier).
    // Le champ AcroForm CRH/DRH (voir ajouterBlocsSignature) ne sert que
    // d'ancre de position dans le PDF deja genere -- iText ne permet pas
    // d'inserer un Paragraph a un endroit arbitraire d'un fichier existant
    // (Document.add() ne fonctionne qu'a la construction initiale). Ici, on
    // recupere le rectangle exact du champ, on y ecrit un Paragraph par-dessus
    // avec Canvas -- exactement la meme construction que le bloc ARH
    // (new Paragraph(signatureService.signer(...))) -- puis on supprime le
    // champ : il ne reste plus aucune trace de widget de formulaire, seulement
    // du texte fixe, identique au rendu ARH.
    @Transactional
    public PieceJointe ajouterSignature(PieceJointe pieceJointe, Utilisateur acteur, NomEtapeEnum etape) {
        String nomChamp = switch (etape) {
            case VALIDATION_CRH -> CHAMP_SIGNATURE_CRH;
            case VALIDATION_DRH -> CHAMP_SIGNATURE_DRH;
            default -> throw new IllegalArgumentException("ajouterSignature() ne s'applique qu'aux etapes CRH ou DRH, recu : " + etape);
        };

        String cheminOriginal = pieceJointe.getCheminStockage();
        Path cheminTemporaire = Path.of(cheminOriginal + ".tmp");

        try (PdfReader reader = new PdfReader(cheminOriginal);
             PdfWriter writer = new PdfWriter(cheminTemporaire.toString());
             PdfDocument pdfDocument = new PdfDocument(reader, writer)) {

            PdfAcroForm formulaire = PdfFormCreator.getAcroForm(pdfDocument, false);
            PdfFormField champSignature = formulaire != null ? formulaire.getField(nomChamp) : null;
            if (champSignature == null) {
                throw new IllegalStateException("Champ de signature introuvable dans le PDF : " + nomChamp);
            }

            PdfWidgetAnnotation widget = champSignature.getWidgets().get(0);
            Rectangle zone = widget.getRectangle().toRectangle();
            PdfPage page = widget.getPage();

            PdfFont police = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.CP1252);
            try (Canvas canvas = new Canvas(page, zone)) {
                canvas.add(new Paragraph(signatureService.signer(acteur)).setFont(police));
            }

            formulaire.removeField(nomChamp);
        } catch (IOException e) {
            throw new IllegalStateException("Echec de l'ajout de signature au PDF pour la piece jointe " + pieceJointe.getId(), e);
        }

        try {
            Files.move(cheminTemporaire, Path.of(cheminOriginal), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Echec du remplacement du PDF pour la piece jointe " + pieceJointe.getId(), e);
        }

        pieceJointe.setNombreSignatures(pieceJointe.getNombreSignatures() + 1);
        pieceJointe.setDateDerniereMiseAJour(LocalDateTime.now());

        return pieceJointeRepository.save(pieceJointe);
    }

    private void ajouterEnTete(Document document, ProcessusMensuel processus) throws IOException {
        try (InputStream logoStream = getClass().getResourceAsStream("/images/logo-afriland-first-bank.png")) {
            if (logoStream != null) {
                Image logo = new Image(ImageDataFactory.create(logoStream.readAllBytes()));
                logo.setWidth(120);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            }
        }

        Paragraph titre = new Paragraph("ETAT RECAPITULATIF DES BENEFICIAIRES DES DOTATIONS TELEPHONIQUES")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(14);
        document.add(titre);

        String libelleMois = NOMS_MOIS[processus.getMoisPaiement() - 1];
        Paragraph periode = new Paragraph(libelleMois + " " + processus.getAnneePaiement())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(11);
        document.add(periode);
    }

    private void ajouterTableau(Document document, ProcessusMensuel processus, List<LigneEtatMensuel> lignes) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 10, 12, 20, 15, 10, 10, 10}))
                .useAllAvailableWidth();

        for (String entete : new String[]{"AGENCE", "CHAPITRE", "COMPTE", "NOM ET PRENOM", "FONCTION", "M-1", "M", "ECART"}) {
            table.addHeaderCell(new Cell().add(new Paragraph(entete).setBold()));
        }

        int totalMontant = 0;
        for (LigneEtatMensuel ligne : lignes) {
            if (!Boolean.TRUE.equals(ligne.getInclusDansEtat())) {
                continue;
            }

            Beneficiaire beneficiaire = beneficiaireRepository.findById(ligne.getIdBeneficiaire())
                    .orElseThrow(() -> new IllegalStateException("Beneficiaire introuvable : " + ligne.getIdBeneficiaire()));

            String chapitre = beneficiaire.getChapitre() != null ? beneficiaire.getChapitre() : chapitreDefaut;
            String libelleFonction = fonctionEligibleRepository.findByCode(ligne.getFonctionRetenue())
                    .map(FonctionEligible::getLibelle)
                    .orElse(ligne.getFonctionRetenue());

            EcartMensuelService.ResultatEcartMensuel resultatMensuel = ecartMensuelService.rechercherResultatMensuel(processus, ligne);
            String montantMoisPrecedent = resultatMensuel.montantMoisPrecedent() != null
                    ? String.valueOf(resultatMensuel.montantMoisPrecedent()) : "";
            String ecartAffiche = resultatMensuel.ecart() != null ? String.valueOf(resultatMensuel.ecart()) : "";

            table.addCell(new Cell().add(new Paragraph(beneficiaire.getCodeUnite())));
            table.addCell(new Cell().add(new Paragraph(chapitre)));
            table.addCell(new Cell().add(new Paragraph(beneficiaire.getNumCompteCourant())));
            table.addCell(new Cell().add(new Paragraph(beneficiaire.getNomPrenoms())));
            table.addCell(new Cell().add(new Paragraph(libelleFonction)));
            table.addCell(new Cell().add(new Paragraph(montantMoisPrecedent)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(ligne.getMontantApplique()))));
            table.addCell(new Cell().add(new Paragraph(ecartAffiche)));

            totalMontant += ligne.getMontantApplique();
        }

        table.addCell(new Cell(1, 6).add(new Paragraph("TOTAL").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(totalMontant)).setBold()));
        table.addCell(new Cell().add(new Paragraph("")));

        document.add(table);
    }

    private void ajouterBlocsSignature(Document document, Utilisateur acteurValidation, PdfFont police) {
        Table blocs = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();

        blocs.addCell(new Cell()
                .add(new Paragraph("ARH").setBold())
                .add(new Paragraph(signatureService.signer(acteurValidation))));
        blocs.addCell(new Cell()
                .add(new Paragraph("CRH").setBold())
                .add(champSignatureVierge(CHAMP_SIGNATURE_CRH, police)));
        blocs.addCell(new Cell()
                .add(new Paragraph("DRH").setBold())
                .add(champSignatureVierge(CHAMP_SIGNATURE_DRH, police)));

        document.add(blocs);
    }

    // Champ AcroForm indispensable pour localiser et remplir la signature plus
    // tard, sans jamais recreer le fichier (RG-06 -- ajouterSignature() rouvre
    // ce meme fichier en stamping). TextArea (multiligne) plutot qu'InputField :
    // InputField est une ligne unique et recalcule toujours sa hauteur sur la
    // police, en ignorant .setHeight() -- ce qui tronquait le texte de
    // signature au lieu de l'accueillir en entier. Depouille de toute
    // bordure/fond et cale sur la police du document : une fois rempli
    // (ajouterSignature()), son rendu est identique au bloc ARH (Paragraph).
    private TextArea champSignatureVierge(String nomChamp, PdfFont police) {
        TextArea champ = new TextArea(nomChamp)
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(null)
                .setFont(police);
        // Largeur/hauteur fixees genereusement : au moment de la generation
        // initiale, le texte de signature CRH/DRH n'est pas encore connu, donc
        // impossible de laisser le champ s'auto-dimensionner sur son contenu
        // (comme le fait la Cell du bloc ARH). Une zone trop juste tronquait le
        // texte au lieu de l'accueillir en entier lors de ajouterSignature().
        champ.setWidth(LARGEUR_ZONE_SIGNATURE);
        champ.setHeight(HAUTEUR_ZONE_SIGNATURE);
        champ.setInteractive(true);
        return champ;
    }
}