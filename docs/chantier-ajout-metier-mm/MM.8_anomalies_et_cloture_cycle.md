# SPRINT MM.8

## Anomalies du cycle de retour, et clôture formelle du cycle de modules

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Corriger deux anomalies du cycle retour/resoumission détectées en test manuel, et clore formellement la question du cycle `beneficiaires` ↔ `referentiel` laissée ouverte par MM.6 |
| **Livrable** | `DocumentService.invaliderSignatureCrh()`, correctif RG-08, `ModularityTests` et documentation mis à jour |
| **Durée** | Une journée |
| **Prérequis** | Chantier MM.0 → MM.6 clos (`ModularityTests` vert, 226 tests) |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Clôture du cycle de modules (étape 2) | Sonnet 5 | Low |
| Anomalie 1 — invalidation signature CRH (étapes 3-5) | Sonnet 5 | **High** |
| Anomalie 2 — RG-08 ligne obsolète (étape 6) | Sonnet 5 | Medium à High |
| Vérification de bout en bout (étape 7) | Sonnet 5 | High |

L'effort High sur les étapes 3 à 5 se justifie par un point précis : c'est **la première fois** que `DocumentService` combine l'API `Document` (layout) et le *stamping* `PdfReader`+`PdfWriter` dans une même méthode — `ajouterSignature()` n'utilise que du `Canvas` bas niveau. Une erreur y corromprait un PDF déjà signé.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

---

## 1. Contexte

Trois sujets indépendants, regroupés parce qu'ils touchent la même zone (cycle retour/resoumission) ou closent une question de fin de chantier.

### 1.1 Anomalie 1 — la signature CRH reste visible après un retour DRH

**Signalée par test manuel, confirmée par lecture du code.** Après un retour effectué par la DRH, le PDF téléchargé porte toujours la signature CRH, sans aucune marque d'invalidation — alors que le circuit ARH → CRH → DRH doit repartir intégralement de zéro après correction par l'ARH.

`ProcessusMensuelService.retourner()` ne touche jamais à la `PieceJointe` lors d'un retour. Ce n'est corrigé qu'**indirectement**, à la revalidation ARH, quand `genererInitiale()` reconstruit le PDF depuis zéro — effet de bord, pas une action volontaire au moment du retour.

**Décision produit déjà validée** : ne pas effacer la signature CRH (RG-09 impose la trace d'audit), mais **la marquer explicitement comme invalidée dès l'instant du retour DRH**.

### 1.2 Anomalie 2 — RG-08 peut comparer contre une ligne de workflow obsolète

Découverte pendant l'investigation de l'anomalie 1, incluse ici car même zone de code.

`EtapeWorkflow` est une table *append-only*, jamais nettoyée. Après un cycle retour + resoumission, plusieurs lignes peuvent porter le même `nomEtape` : deux `VALIDATION_ARH` en `VALIDEE` (une par soumission), ou une `VALIDATION_CRH` en `RETOURNEE` suivie d'une autre en `VALIDEE` au cycle suivant.

`SeparationTachesService.verifier()` ne filtre pas sur `statutEtape` et fait `.findFirst()` sans tri. Il peut donc comparer l'acteur courant contre une validation obsolète d'un cycle antérieur, ou pire contre une ligne `RETOURNEE` — dont l'acteur est celui qui a **retourné**, pas validé.

### 1.3 Clôture du cycle `beneficiaires` ↔ `referentiel` — décision G-2

MM.6 a laissé ce point ouvert : `ModularityTests` est vert avec **une exception filtrée**, et le récapitulatif annonçait MM.8 comme « prérequis réel de correction ».

**L'analyse menée avant ce sprint a montré que ce plan ne tenait pas.** Le cycle ne vient pas d'un seul appel d'écriture, mais de **6 appels** de `FonctionEligibleService` (module `referentiel`) vers `BeneficiaireApi` (module `beneficiaires`) :

| Méthode de `FonctionEligibleService` | Appel | Nature |
|---|---|---|
| `desactiver()` | `compterActifsParFonction()` | Lecture — garde-fou avant désactivation |
| `listerToutes()` | `compterActifsParFonction()` | Lecture — affichage ADMIN |
| `reactiver()` | `compterActifsParFonction()` | Lecture — affichage |
| `modifier()` | `compterActifsParFonction()` **×2** | Lecture — garde-fou « au plus 1 bénéficiaire actif » |
| `modifier()` | `renommerFonction()` | **Écriture** — cascade de renommage |

Spring Modulith détecte un cycle au niveau du **module entier** : tant qu'**un seul** de ces 6 appels subsiste, le cycle reste détecté. Convertir la seule écriture (`renommerFonction`) en événement — le plan initial — **ne casserait donc pas le cycle**.

Et `compterActifsParFonction()` **ne peut pas** devenir un événement asynchrone : c'est une lecture synchrone qui conditionne une décision immédiate (*« si plus d'1 bénéficiaire actif, refuser le renommage »* → 409). Un événement *fire-and-forget* ne peut pas répondre avant que la méthode ne continue.

**Décision actée : G-2 — le cycle est structurellement légitime, on ne cherche plus à le fermer.**

`referentiel` a une vraie raison métier de consulter `beneficiaires` (compter avant de bloquer une action destructrice), exactement symétrique à la raison pour laquelle `beneficiaires` consulte `referentiel` (éligibilité, grille). Ce n'est pas un accident de conception. Les deux alternatives ont été écartées :

- **G-1** (événementer l'écriture seule, déclarer la lecture) : ne casse pas le cycle malgré le travail fait.
- **G-3** (dénormaliser le compteur côté `referentiel`, synchronisé par événements) : ajoute un état dupliqué avec risque de désynchronisation, pour 25 fonctions et quelques dizaines de bénéficiaires par fonction. Coût disproportionné.

---

## 2. Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT -- VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE :
Confirme-moi, en lancant reellement les commandes, que :
  a) ton repertoire de travail se termine bien par "afb-dottel-mm"
     et NON "afb-dottel"
  b) `git log --oneline -1` ne montre PAS d9be38c
  c) `git remote -v` ne retourne AUCUN remote
Si l'un des trois est faux, ARRETE-TOI immediatement.

ENSUITE :
1. Lis CLAUDE.md dans son integralite, en particulier RG-06 (un seul
   PDF par processus), RG-08 (separation des taches) et RG-09
   (historisation).
2. Lis docs/chantier-ajout-metier-mm/PLAN_AJOUTS_METIER_MM.md
3. Lis docs/chantier-ajout-metier-mm/MM.8_anomalies_et_cloture_cycle.md
   EN ENTIER avant de coder quoi que ce soit.
4. Lance /graphify . --update

CONTEXTE : Sprint MM.8. Trois sujets : deux anomalies du cycle
retour/resoumission (signature CRH visible apres un retour DRH ;
RG-08 comparant contre une ligne EtapeWorkflow obsolete) et la
cloture formelle du cycle de modules beneficiaires <-> referentiel
(decision G-2).

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"

METHODE DE TRAVAIL :
- Une anomalie a la fois. Tu montres le diff, j'approuve, tu continues.
- Reference de non-regression : 226 tests, 0 echec. Le nombre de tests
  doit AUGMENTER dans ce sprint (nouveaux tests), jamais diminuer.
- ModularityTests doit rester vert. Aucun de ces changements n'est
  inter-modules (tout se passe dans le module processus), donc aucune
  nouvelle dependance ne devrait apparaitre. Si une violation surgit,
  ARRETE-TOI.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2
(cloture du cycle), la plus rapide et sans risque.
```

---

## 3. Étape 2. Clôturer formellement la question du cycle de modules

```
Applique la decision G-2 : le cycle beneficiaires <-> referentiel est
structurellement legitime, on arrete de le traiter comme une dette a
corriger.

TROIS fichiers a mettre a jour, aucun changement de code applicatif :

1. backend/src/test/java/com/afriland/dottel/ModularityTests.java
   Le commentaire d'en-tete et le message d'assertion annoncent
   aujourd'hui MM.8 comme "PREREQUIS REEL avant la cloture du
   chantier" et disent "s'il a disparu, le prerequis MM.8 est rempli".
   Reecris-les pour refleter G-2 : le cycle est ASSUME DEFINITIVEMENT,
   pas en attente de correction. Explique dans le commentaire les 6
   appels de FonctionEligibleService vers BeneficiaireApi et pourquoi
   compterActifsParFonction() ne peut pas devenir un evenement (lecture
   synchrone conditionnant un 409).

   ATTENTION : ne touche pas a la LOGIQUE du test. Le filtre et les
   deux assertions restent identiques -- c'est toujours une protection
   anti-regression reelle (toute violation nouvelle ou differente le
   fait echouer). Seuls les commentaires et les messages changent.

2. backend/src/main/java/com/afriland/dottel/beneficiaires/package-info.java
   et referentiel/package-info.java
   Meme correction de formulation : le cycle est assume, pas en
   attente de MM.8.

3. docs/monolithe-modulaire/RECAPITULATIF_CHANTIER.md
   Section "Points laisses ouverts" : le point MM.8 "prerequis reel,
   non optionnel" devient "cycle assume, decision G-2 du 2026-08-03",
   avec le motif (les 6 appels, dont 5 en lecture synchrone
   inconvertibles en evenement).
   ATTENTION : la dette `utilisateurs :: entity` listee juste apres
   dans cette meme section reste OUVERTE -- ne la clos pas par
   inadvertance, elle n'a rien a voir avec le cycle.

Puis lance mvn test et confirme que ModularityTests est toujours vert.
```

**Point à ne pas confondre.** La dette `utilisateurs :: entity` (plusieurs modules dépendent du retour entité de `AuthenticatedUserService.utilisateurCourant()` au lieu d'un DTO) est un sujet **distinct**, toujours ouvert. G-2 ne la concerne pas.

---

## 4. Étape 3. Anomalie 1 — décision sur le compteur de signatures

**Arrêt obligatoire avant de coder.** Un point est tranché, l'autre non.

### 4.1 Ce qui est acté

Le plan d'origine ne modifiait pas `nombreSignatures` lors de l'invalidation. **Décision du 2026-08-03 : le compteur repasse à `0` au moment du retour DRH.**

Faits vérifiés qui rendent ce choix sûr :

| Constat | Fichier | Conséquence |
|---|---|---|
| `setNombreSignatures(1)` — **affecte**, n'incrémente pas | `DocumentService.genererInitiale()` ligne 144 | Après revalidation ARH, le compteur vaut `1` **quelle que soit** sa valeur avant. Passer par 0 ne casse donc rien. |
| `setNombreSignatures(getNombreSignatures() + 1)` | `DocumentService.ajouterSignature()` ligne 200 | Jamais appelée pendant le statut `RETOURNE` (seule suite possible : revalidation ARH). Aucun risque d'incrémenter depuis 0. |
| `nombre_signatures INTEGER NOT NULL DEFAULT 0` | `V1__creation_tables.sql` ligne 169 | **`0` est une valeur légale** en base. Aucune migration nécessaire. |

**Sémantique retenue** : `0` signifie « aucune signature valide dans le cycle de validation en cours » — cohérent avec la valeur par défaut d'une `PieceJointe` fraîchement créée, avant toute validation.

**Nuance à connaître** : pendant la fenêtre `RETOURNE`, le PDF contient encore physiquement les signatures ARH et CRH (plus la page d'annulation), alors que le compteur affichera `0`. C'est assumé : le compteur décrit l'état du **cycle de validation**, pas le contenu physique du fichier.

### 4.2 ⏸️ Ce qui n'est PAS tranché — question à poser

```
QUESTION A POSER A L'UTILISATEUR AVANT DE CODER, NE TRANCHE PAS SEUL :

La decision "nombreSignatures repasse a 0" est actee pour le retour
DRH. Mais que doit-il se passer lors d'un retour CRH ?

Contexte : lors d'un retour CRH, le statut est EN_ATTENTE_CRH, donc
seule la signature ARH existe (nombreSignatures = 1). Le plan
d'origine ne prevoit rien pour ce cas cote PDF -- et c'est correct :
il n'y a aucune signature CRH a annuler, donc aucune page d'annulation
n'a de sens.

Mais le COMPTEUR, lui, resterait a 1. Deux lectures possibles :

  S-1) Remettre a 0 dans les DEUX cas de retour (CRH et DRH).
       Coherent avec la semantique retenue : "0 = cycle remis a zero",
       et dans les deux cas l'ARH devra revalider, ce qui regenerera
       le PDF entierement. Le compteur dit la meme chose quelle que
       soit l'origine du retour.

  S-2) Remettre a 0 uniquement au retour DRH.
       Colle strictement au plan d'anomalie (qui ne traite que le
       retour DRH). Mais cree une asymetrie : apres un retour CRH le
       compteur vaut 1, apres un retour DRH il vaut 0, alors que dans
       les deux cas l'etat metier est identique (processus RETOURNE,
       en attente de correction ARH).

Presente les deux, attends la reponse. Ne code pas avant.
```

---

## 5. Étape 4. Anomalie 1 — nouvelle méthode `invaliderSignatureCrh()`

### 5.1 Pourquoi une page annexe et non un tampon superposé

Ce choix est déjà arbitré, mais son motif doit rester visible pour éviter qu'un futur intervenant « améliore » la solution sans comprendre :

`DocumentService.ajouterSignature()` dessine le texte via `Canvas` sur le rectangle du champ AcroForm CRH, **puis supprime le champ** (`formulaire.removeField(nomChamp)`). Il n'existe donc plus aucun moyen de le retrouver par nom pour superposer quoi que ce soit dessus. Sa position n'est de toute façon pas un rectangle fixe : elle dépend du nombre de lignes du tableau de bénéficiaires généré au-dessus. Persister ces coordonnées exigerait une migration de schéma — interdite sans accord explicite (`CLAUDE.md` section 4) — pour un gain purement visuel.

La page annexe, elle : ne dépend d'aucune coordonnée, ne modifie ni `ajouterSignature()` ni `genererInitiale()` (code éprouvé, zéro risque de régression), et **disparaît automatiquement** à la revalidation ARH puisque `genererInitiale()` réécrit tout le fichier. Aucune accumulation possible : un seul retour DRH par cycle avant régénération, la garde de statut dans `retourner()` l'empêche.

### 5.2 Prompt

```
Ajoute la methode invaliderSignatureCrh() dans
backend/src/main/java/com/afriland/dottel/processus/service/DocumentService.java,
entre ajouterSignature() (fin ligne 204) et ajouterEnTete() (ligne 206).

Squelette de reference (a adapter selon la decision de l'etape 3 sur
le compteur) :

    @Transactional
    public PieceJointe invaliderSignatureCrh(PieceJointe pieceJointe,
                                            Utilisateur acteurDrh,
                                            String motifRetour) {
        String cheminOriginal = pieceJointe.getCheminStockage();
        Path cheminTemporaire = Path.of(cheminOriginal + ".tmp");

        try (PdfReader reader = new PdfReader(cheminOriginal);
             PdfWriter writer = new PdfWriter(cheminTemporaire.toString());
             PdfDocument pdfDocument = new PdfDocument(reader, writer);
             Document document = new Document(pdfDocument)) {

            PdfFont police = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.CP1252);
            document.setFont(police);

            document.add(new AreaBreak(PageSize.A4.rotate()));
            document.add(new Paragraph("PROCESSUS RETOURNE PAR LA DRH")
                    .setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(
                    "La validation CRH figurant dans ce document est annulee par ce retour. "
                            + "Le document sera integralement regenere lors de la revalidation par l'ARH."));
            document.add(new Paragraph("Retour effectue par : " + signatureService.signer(acteurDrh)));
            document.add(new Paragraph("Motif du retour : " + motifRetour));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Echec de l'invalidation de la signature CRH pour la piece jointe " + pieceJointe.getId(), e);
        }

        try {
            Files.move(cheminTemporaire, Path.of(cheminOriginal), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Echec du remplacement du PDF pour la piece jointe " + pieceJointe.getId(), e);
        }

        // Compteur : voir decision de l'etape 3 (0 au retour DRH).
        pieceJointe.setNombreSignatures(0);
        pieceJointe.setDateDerniereMiseAJour(LocalDateTime.now());
        return pieceJointeRepository.save(pieceJointe);
    }

Seul import a ajouter : com.itextpdf.layout.element.AreaBreak.
Tout le reste est deja importe dans ce fichier -- VERIFIE-LE plutot que
de me croire, et signale-moi tout import manquant.

Ajoute un commentaire expliquant POURQUOI une page annexe plutot qu'un
tampon superpose (voir section 5.1 du guide) : sans ca, un futur
intervenant tentera le tampon et perdra du temps.
```

---

## 6. Étape 5. Anomalie 1 — appel dans `retourner()`

```
Dans backend/src/main/java/com/afriland/dottel/processus/service/ProcessusMensuelService.java,
methode retourner(), insere l'appel juste apres la determination de
etapeCrh et AVANT la construction de l'EtapeWorkflow -- donc avant
toute mutation d'etat, coherent avec CLAUDE.md section 10 (validation
avant execution metier) :

    boolean etapeCrh = processus.getStatut() == StatutEnum.EN_ATTENTE_CRH;
    NomEtapeEnum nomEtapeRetournee = etapeCrh ? NomEtapeEnum.VALIDATION_CRH : NomEtapeEnum.VALIDATION_DRH;
    int ordreEtape = etapeCrh ? 2 : 3;

    // Un retour DRH invalide la signature CRH deja apposee dans le PDF ;
    // un retour CRH n'a rien a invalider (seule la signature ARH existe).
    if (!etapeCrh) {
        PieceJointe pieceJointe = pieceJointeRepository.findByIdProcessus(processus.getId())
                .orElseThrow(() -> new PieceJointeIntrouvableException(
                        "Aucune piece jointe trouvee pour le processus " + processus.getId()));
        documentService.invaliderSignatureCrh(pieceJointe, utilisateurCourant, motif);
    }

PieceJointeIntrouvableException, pieceJointeRepository et
documentService sont deja presents dans ce fichier (utilises par
validerBrancheCrh()/validerBrancheDrh()) -- aucun nouvel import,
aucune nouvelle dependance injectee. VERIFIE-LE.

Si la decision de l'etape 3 est S-1 (remise a 0 dans les deux cas de
retour), ajoute aussi la remise a 0 pour la branche retour CRH -- sans
page annexe, uniquement le compteur.
```

---

## 7. Étape 6. Anomalie 2 — RG-08 contre la dernière validation réelle

```
Dans backend/src/main/java/com/afriland/dottel/processus/service/SeparationTachesService.java,
methode verifier() : filtre sur statutEtape == VALIDEE et prends la
ligne au dateAction le plus recent.

    public void verifier(Long idProcessus, Long idActeurCourant, NomEtapeEnum etapePrecedente) {
        List<EtapeWorkflow> etapes = etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(idProcessus);

        EtapeWorkflow etapeAnterieure = etapes.stream()
                .filter(etape -> etape.getNomEtape() == etapePrecedente)
                .filter(etape -> etape.getStatutEtape() == StatutEtapeEnum.VALIDEE)
                .max(Comparator.comparing(EtapeWorkflow::getDateAction))
                .orElseThrow(() -> new EtapeWorkflowIntrouvableException(
                        "Aucune étape " + etapePrecedente + " validée trouvée pour le processus " + idProcessus));

        if (etapeAnterieure.getIdActeur().equals(idActeurCourant)) {
            throw new SeparationTachesViolationException(
                    "L'acteur de l'étape " + etapePrecedente
                            + " ne peut pas valider l'étape suivante du même processus");
        }
    }

Imports a ajouter : java.util.Comparator et l'enum StatutEtapeEnum
(verifie son package reel dans l'arborescence modulaire avant
d'ecrire l'import -- ne devine pas).

NON-REGRESSION A VERIFIER EXPLICITEMENT : sur une liste a un seul
element VALIDEE (tous les cas actuels sans retour), .max(...) renvoie
cet unique element -- comportement strictement identique a l'ancien
.findFirst(). verifier() est appele aux deux memes endroits
qu'aujourd'hui (validerBrancheCrh() contre VALIDATION_ARH,
validerBrancheDrh() contre VALIDATION_CRH) : un seul correctif couvre
les deux.
```

---

## 8. Étape 7. Tests

```
Ajoute les tests suivants. Tous les fichiers sont dans le module
processus : backend/src/test/java/com/afriland/dottel/processus/service/

SeparationTachesServiceTest -- 3 nouveaux tests :
  - plusieurs lignes VALIDEE du meme nomEtape, acteurs et dates
    differents -> la comparaison porte sur la PLUS RECENTE par date
    (leve pour l'acteur de la ligne recente, ne leve pas pour celui de
    la ligne ancienne).
  - une ligne RETOURNEE puis une VALIDEE du meme nomEtape -> la ligne
    RETOURNEE est ignoree.
  - seulement une ligne RETOURNEE, aucune VALIDEE -> leve toujours
    EtapeWorkflowIntrouvableException.

DocumentServiceTest -- 3 nouveaux tests (meme style @TempDir que
l'existant) :
  - Cas nominal : genere + signe CRH, appelle invaliderSignatureCrh,
    verifie que le nombre de pages augmente de 1, que le contenu de la
    page portant la signature CRH est INCHANGE, que la nouvelle page
    contient le motif et "RETOURNE PAR LA DRH", et que
    nombreSignatures vaut bien 0 (decision de l'etape 3).
  - Absence de fichier .tmp residuel (meme garde que
    ajouterSignature_neCreePasUnNouveauFichier).
  - Fichier introuvable -> IllegalStateException.

ProcessusMensuelServiceTest :
  - Modifier les tests de retour DRH existants : mocker
    pieceJointeRepository.findByIdProcessus(...) et verifier
    verify(documentService).invaliderSignatureCrh(pieceJointe, drhConnecte, motif).
  - Modifier le test de retour CRH : ajouter
    verify(documentService, never()).invaliderSignatureCrh(any(), any(), any()).
  - Nouveau test : retour DRH avec piece jointe introuvable ->
    PieceJointeIntrouvableException, ET verifier qu'AUCUNE mutation
    d'etat n'a eu lieu (etapeWorkflowRepository et
    processusMensuelRepository jamais sauvegardes).

ORDRE D'EXECUTION IMPOSE : fais passer DocumentServiceTest EN
ISOLATION D'ABORD. C'est la premiere fois que ce fichier combine
Document (API layout) et stamping PdfReader+PdfWriter dans une meme
methode -- confirme que AreaBreak produit bien une page supplementaire
sans corrompre le contenu existant AVANT de cabler
ProcessusMensuelService.
```

---

## 9. Étape 8. Vérification manuelle de bout en bout

```
Le scenario initialement signale, a rejouer en entier :

1. Demarrer le backend avec les TROIS variables d'environnement.
2. Declencher un processus, valider ARH, valider CRH.
3. Retourner en DRH avec un motif.
4. Telecharger le PDF -> verifier que :
   - la signature CRH est toujours LISIBLE en page 1 (non effacee,
     RG-09)
   - une page finale annonce clairement l'annulation, avec le motif
     saisi et l'identite du valideur DRH
5. Verifier EN BASE que piece_jointe.nombre_signatures vaut 0.
6. Faire revalider l'ARH -> telecharger de nouveau -> verifier que :
   - le PDF est reconstruit proprement (signature CRH d'origine ET
     page annexe disparues)
   - nombre_signatures vaut 1
7. Verifier que audit_log a bien trace le retour (RG-09) :
   id_utilisateur renseigne, adresse_ip NON NULLE.

Montre-moi les resultats reels, pas une conclusion.
```

---

## 10. Frontend

**Aucune modification nécessaire.** Vérification à refaire au démarrage du sprint plutôt qu'à me croire : aucun affichage de `nombreSignatures` ni d'état de signature CRH/DRH n'existe dans `frontend/src`. L'écran de détail processus se limite à un bouton de téléchargement et à l'affichage du motif de retour (`motifRetour`/`origineRetour`, déjà en place, non impactés). L'information d'invalidation est portée **entièrement par le contenu du PDF**.

Note : `nombreSignatures` **est** exposé par `PieceJointeMetadonneesResponseDto` (donc sur `GET /processus/{id}/piece-jointe`). Si un consommateur externe de cet endpoint apparaissait un jour, la valeur `0` pendant la fenêtre `RETOURNE` devrait lui être documentée.

---

## 11. Critères de validation

| Élément | Statut attendu |
|---|---|
| **Décision compteur au retour CRH** (S-1 / S-2) tranchée **avec l'utilisateur** | Fait |
| `ModularityTests` : commentaires et messages reflètent G-2, logique du test **inchangée** | Vérifié |
| `beneficiaires/package-info.java` et `referentiel/package-info.java` reformulés (cycle assumé) | Vérifié |
| `RECAPITULATIF_CHANTIER.md` : point MM.8 clos par G-2, **dette `utilisateurs :: entity` toujours ouverte** | Vérifié |
| `ModularityTests` toujours **vert** après ces changements | Vérifié |
| `invaliderSignatureCrh()` ajoutée, seul nouvel import `AreaBreak` | Vérifié |
| Signature CRH **non effacée** (RG-09), page d'annulation ajoutée avec motif et valideur | Vérifié en PDF réel |
| `nombreSignatures` = **0** après retour DRH, = **1** après revalidation ARH | Vérifié en base |
| Retour CRH : `invaliderSignatureCrh()` **jamais** appelée | Vérifié par test |
| Pièce jointe introuvable au retour DRH : exception **et aucune mutation d'état** | Vérifié par test |
| RG-08 compare contre la dernière ligne `VALIDEE` par date, ignore les `RETOURNEE` | Vérifié par test |
| Non-régression RG-08 : liste à un seul élément → comportement identique | Vérifié par test |
| Aucun fichier `.tmp` résiduel | Vérifié par test |
| Aucune migration Flyway ajoutée | Vérifié |
| Aucune modification frontend | Vérifié |
| Suite complète : **> 226 tests**, 0 échec, 0 erreur | Vérifié |
| Scénario manuel de bout en bout rejoué | Vérifié |

---

## Commit

```bash
git add .
git commit -m "mm.8: invalidation signature CRH au retour DRH, correctif RG-08, cloture G-2 du cycle de modules"
```

---

**Fin du Sprint MM.8** — *en attente de validation avant MM.9*
