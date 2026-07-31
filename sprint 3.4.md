# SPRINT 3.4

## Validation ARH et génération du PDF initial

*Module Dotations Téléphoniques Mensuelles*

| | |
|---|---|
| **Objet** | Validation ARH de l'état mensuel, génération du PDF, transfert au CRH (US-11) |
| **Livrable** | DocumentService.genererInitiale(), POST /processus/{id}/valider (portée ARH) |
| **Durée** | Une journée à une journée et demie |
| **Prérequis** | Sprint 3.3 validé |

## Configuration recommandée pour ce sous-sprint

| Étape | Modèle | Effort |
|---|---|---|
| Migration Flyway + entités (étape 2) | Sonnet 5 | Medium |
| Mapping colonnes, EHR, logo (étape 3) | Sonnet 5 | Medium |
| Calcul écart M-1/M (étape 3bis) | Sonnet 5 | Medium |
| DocumentService (génération PDF, étape 5) | Sonnet 5 ou Opus 4.8 | High |
| Service de validation, contrôleur, tests (étapes 6-7) | Sonnet 5 | High |

C'est le sous-sprint le plus dense du Sprint 3 : génération de fichier, manipulation d'iText 8, orchestration de plusieurs entités en une seule transaction, plus une migration rétroactive touchant le Sprint 2.2. Conforme à la recommandation du document de configuration (Sprint 3 = effort élevé). Ne redescendez pas sous Medium/High pour les étapes 2, 3bis, 5, 6 et 7.

## 0. Nouvelle session : rappel Graphify

```
py -3.14 -m graphify update .
```

Puis, pour vérifier ponctuellement qu'un composant n'existe pas déjà (DocumentService, PieceJointe, EtapeWorkflow, NotificationService — aucun n'a été créé jusqu'ici) :

```
py -3.14 -m graphify explain "DocumentService"
```

## 1. Contexte et périmètre exact de ce sous-sprint

Ce sous-sprint couvre US-11 côté ARH uniquement. Le contrat API (docs/reference/contrats_api_dotations_v3.md) décrit `POST /processus/{id}/valider` comme un endpoint unique partagé par les trois rôles (ARH si `EN_COURS_ARH`, CRH si `EN_ATTENTE_CRH`, DRH si `EN_ATTENTE_DRH`). Ce sous-sprint n'implémente que la branche ARH : transition `EN_COURS_ARH → EN_ATTENTE_CRH`, génération initiale du PDF. Les branches CRH et DRH, ainsi que `DocumentService.ajouterSignature()`, sont hors périmètre et reportées au Sprint 5, conformément à l'ordre des sprints déjà établi.

**Point de vigilance signalé avant de commencer** : le contrat API précise que `/valider` "vérifie RG-08 avant toute validation" (séparation des tâches), sans distinguer les branches. RG-08 et `SeparationTachesService` sont explicitement prévus pour le Sprint 5 dans l'ordre des sprints du projet. Ne pas implémenter RG-08 maintenant serait cohérent avec le découpage prévu, mais laisserait ce sous-sprint incomplet par rapport à la lettre du contrat. Ne pas trancher seul ce point de méthode.

### Décisions actées avec le métier avant ce sous-sprint (à ne pas rouvrir)

- **Signature** : DOTTEL gère la trace de validation en autonomie pour l'instant (nom de l'acteur + horodatage), pas une signature électronique certifiée. Une intégration future à un service de signature type INTRA (signatures pré-enrôlées, insérées automatiquement) est prévue plus tard — la logique doit donc être isolée derrière une interface dédiée (`SignatureService`), remplaçable sans toucher au reste du code, sur le même principe que le stub EHR.
- **Stockage du PDF** : chemin local configurable, explicitement documenté comme limitation temporaire dans le code. Le métier a confirmé que le document doit être archivé et consultable à tout moment durant et après le cycle de validation — un stockage local au pod ne survit pas à un redémarrage Kubernetes. Ce point reste en attente DSI (solution de stockage persistant), ne pas le considérer comme résolu par ce sous-sprint.
- **Écritures comptables** : confirmées hors périmètre. La section correspondante du fichier de référence (FORMAT_DOT_TEL.xlsx) n'est PAS générée par DOTTEL — elle est ajoutée séparément par le module comptable après réception de l'événement de clôture. Le PDF généré ici ne contient que la section "État récapitulatif".
- **AGENCE et COMPTE** : `AGENCE <- beneficiaires.code_unite` (format réel banque numérique, ex. "00001" ; nos données de test actuelles utilisent un format différent type "DLA-AKW" — écart de réalisme à corriger plus tard, non bloquant). `COMPTE <- beneficiaires.num_compte_courant`.
- **CHAPITRE** : **revu depuis la première version de ce guide.** Le métier avait d'abord confirmé une valeur fixe ("64310000, ça ne change pas"), mais la relecture du fichier de référence FORMAT_DOT_TEL.xlsx montre que la colonne CHAPITRE de la section État récapitulatif **varie réellement** selon les lignes (valeurs observées : 37210100, 37210110), et que "64310000" n'apparaît nulle part dans ce fichier. Le métier n'étant pas disponible pour lever cette contradiction, la décision retenue est de traiter CHAPITRE comme une **donnée par bénéficiaire, alimentée par l'EHR**, exactement sur le même principe que `code_unite` et `num_compte_courant` (Sprint 2.2), avec une valeur de repli configurable en attendant que le stub/l'EHR réel fournisse une donnée fiable. Voir étape 2 et 3 ci-dessous pour le détail d'implémentation. **Ne pas coder de constante fixe.**

### Contenu du PDF

Référence exacte : fichier FORMAT_DOT_TEL.xlsx fourni par le métier. Colonnes de la section "État récapitulatif" : AGENCE, CHAPITRE, COMPTE, NOM ET PRENOM, FONCTION, M-1, M, ÉCART, avec une ligne TOTAL et trois blocs de signature (ARH, CRH, DRH — seul le bloc ARH est renseigné à ce stade).

## 2. Objectifs

- Migration Flyway ajoutant `chapitre` à `beneficiaires`
- Entités `PieceJointe` et `EtapeWorkflow` (+ `NomEtapeEnum`, `StatutEtapeEnum` si absents — déjà créés si le Sprint 3.4 a été entamé avant cette révision, sinon à créer avec les valeurs de la section 6 du CLAUDE.md)
- Mise à jour d'`EmployeEhrDto`, `Beneficiaire`, `EhrIntegrationServiceStub` et `EnrolementService.confirmer()` pour propager `chapitre` depuis l'EHR (rétroactif sur le Sprint 2.2, sans en changer le comportement fonctionnel)
- `SignatureService` (interface + implémentation autonome)
- `DocumentService.genererInitiale()` via iText 8, avec calcul de l'écart M-1/M
- `NotificationService` (stub log)
- `ProcessusMensuelService.valider()` — portée ARH
- `POST /processus/{id}/valider`
- Tests JUnit

## 3. Rappel des règles concernées

- **RG-06** : un seul PDF par processus. `genererInitiale()` le crée ; l'enrichissement par CRH/DRH (`ajouterSignature`) est hors périmètre ici (Sprint 5).
- **RG-09** : audit systématique de la validation.
- `piece_jointe.id_processus` est UNIQUE en base (script V1) : un appel de `genererInitiale()` sur un processus qui a déjà un PDF doit être empêché en amont par le contrôle de statut (`EN_COURS_ARH` uniquement), pas laissé à la contrainte SQL pour échouer en 500.

## 4. Étapes d'implémentation

### Étape 1. Ouvrir une session Claude Code

```
Tu es mon assistant de developpement pour le projet de digitalisation
des dotations telephoniques mensuelles d'Afriland First Bank.

AVANT TOUT :
1. Lis le fichier CLAUDE.md a la racine du projet, en particulier
   RG-06, RG-09, et la section 4 (tables piece_jointe et
   etape_workflow).
2. Confirme via graphify (deja mis a jour) qu'aucun DocumentService,
   PieceJointe, EtapeWorkflow ni NotificationService n'existe encore
   dans le code.

Confirme en 3 lignes ce que tu y as trouve.

CONTEXTE : Sprint 3.4, dernier sous-sprint du Sprint 3. Ce sous-sprint
couvre UNIQUEMENT la branche ARH de POST /processus/{id}/valider
(transition EN_COURS_ARH -> EN_ATTENTE_CRH). Les branches CRH/DRH,
RG-08 et DocumentService.ajouterSignature() sont hors perimetre,
reportees au Sprint 5.

METHODE DE TRAVAIL :
- Un fichier a la fois. Tu montres, j'approuve, tu continues.
- Si un choix n'est pas couvert par CLAUDE.md, tu poses la question
  plutot que de supposer.

PREMIERE ACTION : verifie la presence de la dependance iText 8 dans
backend/pom.xml (prevue depuis le Sprint 0.2). Si absente, ajoute-la
et montre le changement avant de continuer.
```

### Étape 2. Migration CHAPITRE + entités PieceJointe et EtapeWorkflow

```
Deux chantiers independants dans cette etape.

A. Ajout du champ chapitre (donnee EHR, pas une constante) :
1. Cree une migration Flyway (prochain numero de version disponible)
   ajoutant une colonne chapitre VARCHAR(20) NULLABLE a beneficiaires.
2. Ajoute le champ chapitre a EmployeEhrDto (model/dto/ehr).
3. Ajoute le champ chapitre a l'entite Beneficiaire.
4. Dans EhrIntegrationServiceStub, ajoute une valeur de chapitre
   plausible pour chaque employe fictif deja present (tu peux
   reutiliser un motif du type "3721" + code agence, coherent avec
   les valeurs observees dans le fichier de reference : 37210100,
   37210110).
5. Dans EnrolementService.confirmer(), propage employeEhr.getChapitre()
   vers Beneficiaire au meme endroit que codeUnite/numCompteCourant.

Montre les 5 fichiers un a un, dans cet ordre.

B. Entites du workflow documentaire :
Cree PieceJointe dans model/entity, conforme a la section 4 : id,
id_processus (UNIQUE), nom_fichier, chemin_stockage,
date_generation_initiale, date_derniere_mise_a_jour,
nombre_signatures. Meme convention Lombok que les entites existantes.

Cree EtapeWorkflow dans model/entity : id, id_processus, id_acteur,
ordre_etape, nom_etape (VARCHAR + NomEtapeEnum), statut_etape
(VARCHAR + StatutEtapeEnum), date_action, motif_retour (nullable),
signature_numerique (nullable). Si NomEtapeEnum/StatutEtapeEnum
n'existent pas encore, cree-les directement avec les valeurs de la
section 6 du CLAUDE.md (VALIDATION_ARH/VALIDATION_CRH/VALIDATION_DRH
et EN_ATTENTE/VALIDEE/RETOURNEE), rien a clarifier sur ce point.

Cree les repositories associes (PieceJointeRepository,
EtapeWorkflowRepository), simples JpaRepository.

Montre les fichiers.
```

### Étape 3. Mapping des colonnes, SignatureService et logo

```
Applique directement ces decisions, deja confirmees (20 juillet
2026), sans les remettre en question :

1. Mapping des colonnes de l'etat recapitulatif :
   - AGENCE <- beneficiaires.code_unite
   - COMPTE <- beneficiaires.num_compte_courant
   - CHAPITRE <- beneficiaires.chapitre (nouveau champ de l'etape 2,
     alimente par l'EHR). Si null (donnee EHR manquante), utilise une
     valeur de repli configurable via une propriete application.yml
     (ex: dottel.documents.chapitre-defaut), avec un commentaire
     explicite precisant que c'est un filet de securite temporaire
     en attendant une donnee EHR fiable pour tous les beneficiaires.

2. Stockage du PDF : chemin local configurable via une propriete
   application.yml (ex: dottel.documents.chemin-stockage, valeur par
   defaut ./documents/ en dev). Ajoute un commentaire explicite dans
   le code precisant que c'est une limitation temporaire : le metier
   exige un archivage persistant et consultable durant ET apres le
   cycle de validation, ce qui necessitera une solution de stockage
   persistante (volume Kubernetes ou object storage) non encore
   fournie par la DSI.

3. Signature ARH (champ signature_numerique) : cree une interface
   SignatureService avec une methode signer(Utilisateur acteur) qui
   retourne une chaine tracable (identifiant utilisateur, nom,
   horodatage). Implementation autonome pour l'instant
   (SignatureServiceAutonome ou nom similaire), remplacable plus tard
   par une integration reelle a un service type INTRA (signatures
   pre-enrolees, insertion automatique sur les documents) -- meme
   principe que le stub EHR. Precise dans un commentaire que ce n'est
   pas une signature legale reelle.

4. Logo : le fichier sera place dans
   backend/src/main/resources/images/logo-afriland-first-bank.png.
   Prevois le chargement via
   getClass().getResourceAsStream("/images/logo-afriland-first-bank.png"),
   positionne en haut du document, centre horizontalement, taille
   raisonnable (largeur environ 120px), au-dessus du titre "ETAT
   RECAPITULATIF DES BENEFICIAIRES DES DOTATIONS TELEPHONIQUES".

Montre le mapping, la propriete de repli CHAPITRE, l'interface
SignatureService et le chargement du logo -- pas encore la
generation complete du PDF (etape suivante).
```

### Étape 3bis. Calcul de l'écart M-1 / M

Confirmé avec le métier : l'écart n'est pas un delta de montant générique, il ne s'affiche que si la fonction du bénéficiaire a changé entre le mois précédent et le mois courant (exemple donné : un GFC devenu Directeur d'Agence).

```
Avant de generer le PDF, pour chaque ligne incluse du processus
courant (inclus_dans_etat=true), recherche la ligne du meme
beneficiaire dans le processus du mois precedent :
ProcessusMensuelRepository pour trouver le processus de mois-1/annee
(attention au changement d'annee si mois=1), puis
LigneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire() pour
recuperer sa ligne.

Si aucun processus precedent n'existe, ou si le beneficiaire n'y
figurait pas (premier mois) : pas d'ecart affiche (colonne vide ou 0,
a me montrer avant de generaliser).

Si un processus precedent existe : compare fonctionRetenue entre les
deux mois. Si identique, ecart = 0 (ou vide, meme choix que le cas
precedent). Si differente, ecart = montantApplique(mois courant) -
montantApplique(mois precedent).

Montre uniquement la methode de calcul (pas encore integree au PDF),
avec un test rapide sur les 3 cas (pas de processus precedent,
fonction identique, fonction differente) avant de continuer.
```

### Étape 4. NotificationService (stub)

```
Cree l'interface NotificationService avec une methode
notifier(Utilisateur destinataire, String sujet, String message).
Cree une implementation NotificationServiceStub qui journalise
l'appel (log INFO) sans envoi reel (pas d'integration email/SMS
prevue a ce stade). Meme esprit que le stub EHR : contrat clair,
implementation temporaire remplacable plus tard. Montre les deux
fichiers.
```

### Étape 5. DocumentService.genererInitiale()

```
Utilise le mapping, la propriete de repli CHAPITRE, SignatureService
et le logo de l'etape 3, et la methode de calcul d'ecart de l'etape
3bis. Cree DocumentService avec genererInitiale(ProcessusMensuel
processus, List<LigneEtatMensuel> lignes) :
1. Genere un PDF via iText 8 : logo en haut centre, titre "ETAT
   RECAPITULATIF DES BENEFICIAIRES DES DOTATIONS TELEPHONIQUES",
   mois/annee concernes
2. Tableau avec les colonnes AGENCE, CHAPITRE, COMPTE, NOM ET PRENOM,
   FONCTION, M-1, M, ECART (dans cet ordre, conforme au fichier de
   reference du metier), une ligne par beneficiaire
   inclus_dans_etat=true uniquement
3. Ligne TOTAL en bas du tableau
4. Trois blocs de signature (ARH, CRH, DRH) -- seul le bloc ARH est
   renseigne a ce stade (nom + horodatage via SignatureService), les
   deux autres restent vides jusqu'au Sprint 5
5. Sauvegarde le fichier a l'emplacement configure
6. Cree et sauvegarde l'entite PieceJointe (nombre_signatures=1)
7. Retourne l'entite PieceJointe creee

Rappel : la section ecritures comptables du fichier de reference
N'EST PAS incluse dans ce PDF (hors perimetre, ajoutee separement
par le module comptable). Montre le fichier.
```

### Étape 6. ProcessusMensuelService.valider()

```
Cree la methode valider(Long idProcessus, Long idUtilisateurConnecte,
String commentaire) :
1. Recupere le ProcessusMensuel -> 404 si absent
   (ProcessusMensuelIntrouvableException, deja creee)
2. Verifie statut == EN_COURS_ARH -> sinon 409 (reutilise le pattern
   d'exception deja etabli au Sprint 3.3 pour 'processus non
   modifiable')
3. Recupere les lignes inclus_dans_etat=true du processus
4. Appelle DocumentService.genererInitiale()
5. Cree une EtapeWorkflow (nom_etape=VALIDATION_ARH,
   statut_etape=VALIDEE, id_acteur=idUtilisateurConnecte,
   signature_numerique via SignatureService)
6. Met a jour processus.statut = EN_ATTENTE_CRH
7. Appelle NotificationService pour notifier le ou les utilisateurs
   ayant le role CRH (question : comment identifier le destinataire
   CRH ? Pas de notion d'affectation CRH->processus dans le modele
   actuel. Propose une solution simple -- ex: notifier tous les
   utilisateurs actifs de role CRH -- et demande confirmation avant
   de generaliser)
8. AuditService.enregistrer() pour la validation
9. Retourne id, statut, etapeValidee, idPieceJointe (conforme au
   contrat API)

Montre le service.
```

### Étape 7. Contrôleur et tests

```
Cree POST /processus/{id}/valider dans le controleur existant, role
ARH, corps optionnel {commentaire}. Retourne 200. Montre le
controleur.

Puis cree ProcessusMensuelValidationServiceTest (ou complete
ProcessusMensuelServiceTest) avec au moins :
1. valider_casNominal_genereLePdfEtChangeLeStatut
2. valider_processusDejaValide_leve409
3. valider_processusIntrouvable_leve404
4. valider_appelleNotificationService_uneFoisParDestinataireCRH
5. valider_pieceJointeUniquePourLeProcessus_verifieContrainte
6. valider_chapitreAbsentDeLehr_utiliseValeurDeRepliConfiguree

Donnees de test camerounaises. Montre le fichier.
```

## 5. Tests et vérifications

```bash
cd backend
mvn test
```

Test manuel : déclenchez un processus (mois inédit), puis validez-le :

```powershell
curl -X POST http://localhost:8080/api/processus/1/valider `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer VOTRE_TOKEN_ARH" `
  -d '{"commentaire":"Etat verifie et conforme"}'

# Attendu : 200 avec statut EN_ATTENTE_CRH et idPieceJointe
```

Vérifiez ensuite que le fichier PDF existe bien à l'emplacement configuré (étape 3), que les colonnes AGENCE/CHAPITRE/COMPTE/ÉCART sont correctes, et qu'une seconde tentative de validation sur le même processus retourne 409.

## 6. Critères de validation

| Élément | Statut attendu |
|---|---|
| Migration Flyway ajoutant beneficiaires.chapitre | Fait |
| EHR (stub) fournit chapitre, propagé à l'enrôlement | Fait |
| Mapping AGENCE/CHAPITRE/COMPTE appliqué (CHAPITRE via EHR + repli configurable) | Fait |
| SignatureService isolé derrière une interface dédiée | Fait |
| Logo chargé et positionné en haut, centré | Vérifié |
| Écart M-1/M affiché uniquement si changement de fonction | Vérifié |
| PDF généré et lisible (colonnes conformes au fichier de référence) | Vérifié |
| Écritures comptables absentes du PDF (hors périmètre) | Vérifié |
| Statut passe à EN_ATTENTE_CRH | Vérifié |
| Seconde validation sur le même processus → 409 | Vérifié |
| NotificationService appelé (log visible) | Vérifié |
| mvn test : BUILD SUCCESS | Vérifié |

## Commit

```bash
git add .
git commit -m "sprint-3.4: validation ARH et generation du PDF initial (US-11, portee ARH, chapitre via EHR)"
```

---

**Fin du Sprint 3.4**

*Fin du Sprint 3 — en attente de validation avant de démarrer le Sprint 4*