# SPRINT MM.13

## Notifications Outlook provisoires et événement Kafka enrichi

*Module Dotations Téléphoniques Mensuelles — modules `processus` et `beneficiaires`*

| | |
|---|---|
| **Objet** | Notifier les acteurs par Outlook aux étapes de validation et de retour, et enrichir l'événement de clôture destiné à la comptabilité |
| **Livrable** | `NotificationService` provisoire (Outlook), `EvenementClotureDto` détaillé par bénéficiaire |
| **Durée** | Une journée et demie à deux jours |
| **Prérequis** | **MM.10 obligatoire** (le code agence est requis par l'événement enrichi), MM.12 validé |
| **Origine** | `M.5_notifications_et_kafka.md` |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Conception des notifications (étape 2) | Sonnet 5 | Medium à High |
| Implémentation notifications (étape 3) | Sonnet 5 | Medium à High |
| Extension de `BeneficiaireApi` (étape 4) | Sonnet 5 ou Opus 4.8 | **High** |
| Schéma et implémentation Kafka (étapes 5-6) | Sonnet 5 ou Opus 4.8 | **High** |

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

---

## 1. Contexte

### 1.1 Notifications — la version modulaire est *mieux* préparée

`NotificationService` et `NotificationServiceStub` existent déjà dans `processus/service/` mais ne font rien de réel. L'objectif est une implémentation **provisoire** (envoi Outlook direct), en attendant l'intégration avec le service de notification interne de la banque.

**Bénéfice inattendu du chantier MM** : le volet notifications a besoin des adresses e-mail des acteurs par rôle. Or `utilisateurs/api/UtilisateurApi` expose **déjà**, depuis MM.2 :

```java
List<DestinataireNotificationDto> destinatairesParRole(RoleEnum role);
DestinataireNotificationDto destinataireParId(Long idUtilisateur);
```

avec `DestinataireNotificationDto(String email, RoleEnum role)`. C'est **exactement** ce dont ce sprint a besoin, et la dépendance `processus → utilisateurs :: api` est déjà déclarée. **Aucune plomberie à créer** — l'ancienne version aurait dû aller lire `UtilisateurRepository` directement.

Point en attente DSI : les paramètres réels du canal Outlook (serveur SMTP ou API Microsoft Graph, identifiants, adresse d'expédition) ne sont pas connus. Développement en configuration externalisée, avec un mode local désactivable.

### 1.2 ⚠️ Kafka enrichi — exige d'étendre une API inter-modules

Aujourd'hui `processus/api/EvenementClotureDto` porte un montant **total agrégé**. Le métier veut le **détail par bénéficiaire** : code unité, code agence, numéro de compte courant, chapitre, nom et prénom, fonction retenue, montant attribué.

**Le point que le guide M.5 d'origine ne pouvait pas anticiper** : `EvenementClotureService` vit dans `processus` et **ne peut plus lire `BeneficiaireRepository`** — retiré de ses dépendances en MM.2. Il devra passer par `beneficiaires/api/BeneficiaireApi`.

État réel de cette API, vérifié :

| Méthode existante | Retourne |
|---|---|
| `listerActifsPourDotation()` | `List<BeneficiaireDotationDto>` — `(id, matricule, nomPrenoms, fonction)` |
| `identitesParId(Collection<Long>)` | `Map<Long, BeneficiaireIdentiteDto>` — `(id, matricule, nomPrenoms)` |
| `donneesDocumentParId(Collection<Long>)` | `Map<Long, BeneficiaireDocumentDto>` — `(id, nomPrenoms, codeUnite, numCompteCourant, …)` |
| `gradeDe(Long)` | `Optional<String>` |
| `compterActifsParFonction(String)` | `long` |
| `renommerFonction(String, String)` | `void` |

`BeneficiaireDocumentDto` est **le plus proche du besoin** (il porte déjà code unité et compte courant, pour le PDF). Mais il lui manque le **code agence** — créé en MM.10 — et peut-être le chapitre. D'où la dépendance de sprint : **MM.10 avant MM.13, jamais l'inverse.**

### 1.3 Points déjà actés à ne pas casser

- **Kafka** : DOTTEL est **producteur uniquement**, topic `dottel.processus.cloture`, `KAFKA_BOOTSTRAP_SERVERS` externalisé (Sprint 5.3). L'enrichissement ne change que le **contenu du payload**, jamais ce principe.
- **Montants en FCFA entier** (`long`, jamais `BigDecimal`) — cohérence déjà établie dans `EvenementClotureDto`, à conserver pour les montants par bénéficiaire.
- **`tools.jackson` uniquement**, jamais `com.fasterxml.jackson` — `EvenementClotureSerializer` et `AuditServiceImpl` utilisent déjà `tools.jackson`.
- **Décision D de M.0, actée** : schéma Kafka provisoire proposé par DOTTEL, **clairement marqué « à valider avec la comptabilité »**. Ne bloque pas ce sprint.
- **Données de test camerounaises** obligatoires (`CLAUDE.md` §9).

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
1. Lis CLAUDE.md dans son integralite, en particulier la section 11
   (perimetre : les ecritures comptables sont HORS PERIMETRE, DOTTEL
   publie seulement un evenement) et la section 17 point 9.
2. Lis docs/chantier-ajout-metier-mm/PLAN_AJOUTS_METIER_MM.md
3. Lis docs/chantier-ajout-metier-mm/MM.13_notifications_et_kafka.md
   EN ENTIER -- surtout la section 1.2 (extension de BeneficiaireApi).
4. Lance /graphify . --update

PREREQUIS A VERIFIER AVANT DE COMMENCER : MM.10 doit etre fait, car
l'evenement enrichi a besoin du code agence cree la. Verifie que la
colonne code_agence existe bien sur l'entite Beneficiaire. Si elle
n'existe pas, ARRETE-TOI : MM.13 ne peut pas etre fait avant MM.10.

CONTEXTE : Sprint MM.13. Deux volets : notifications Outlook provisoires
(module processus) et evenement Kafka enrichi par beneficiaire
(processus + extension de beneficiaires::api).

DECISION DEJA ACTEE : decision D de M.0 -- schema Kafka provisoire
propose par DOTTEL, marque "a valider avec la comptabilite". Ne bloque
pas ce sprint.

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"

METHODE DE TRAVAIL :
- Un fichier a la fois. Tu montres, j'approuve, tu continues.
- Reference de non-regression : total de MM.12, 0 echec.
- L'etape 4 touche une API inter-modules : lance ModularityTests
  apres, et verifie que les allowedDependencies existants suffisent.

PREMIERE ACTION : la verification d'espace de travail et du prerequis
MM.10, puis l'etape 2 (conception des notifications).
```

---

## 3. Étape 2. Notifications — conception

```
Traite ces trois points avec moi AVANT de coder.

POINT 1 -- Canal technique.

SMTP classique (JavaMail / spring-boot-starter-mail) ou API Microsoft
Graph ? Les deux sont provisoires.
  - SMTP : plus simple a stubber en local, dependance legere.
  - Graph : plus proche de ce que la DSI utilisera peut-etre, mais
    exige un enregistrement d'application Azure -- donc un point DSI
    supplementaire, alors qu'il y en a deja trois en attente (EHR,
    Keycloak, Kafka).

Recommande-en un. Dans les DEUX cas, l'abstraction NotificationService
existante doit etre PRESERVEE pour un remplacement futur par le service
interne de la banque -- meme principe que EhrIntegrationService /
EhrIntegrationServiceStub, qui est le modele de reference du projet
pour une frontiere externe propre.

POINT 2 -- Matrice acteur / evenement.

Qui doit etre notifie a quel moment ? Propose-moi la matrice complete,
je la valide. Points a couvrir au minimum :
  - validation ARH        -> qui ?
  - validation CRH        -> qui ?
  - validation DRH (cloture) -> qui ?
  - retour CRH            -> qui ?
  - retour DRH            -> qui ?
  - et, si MM.12 a ete fait : les etapes du workflow des GRILLES
    (soumission ARH, validation/rejet CRH, validation/rejet DRH) --
    le metier a demande des notifications "aux etapes de validation et
    de retour", ce qui peut couvrir les deux workflows. POSE-MOI LA
    QUESTION plutot que de supposer.

Rappel utile : ProcessusMensuelService appelle DEJA notificationService
.notifier(...) a plusieurs endroits (validation ARH -> destinataires
CRH ; validation CRH -> destinataires DRH ; retour -> ARH createur).
Analyse ces appels existants AVANT de proposer la matrice : une partie
du cablage existe peut-etre deja et il ne faut pas le dupliquer.

POINT 3 -- Configuration et comportement transactionnel.

- Parametres Outlook (serveur, identifiants, expediteur) en VARIABLES
  D'ENVIRONNEMENT, jamais en dur -- meme regle que DOTTEL_JWT_SECRET et
  DB_PASSWORD (CLAUDE.md sections 17.6 et 18.10, correctifs E7d/E7e de
  l'audit 6F.9). Un mode DESACTIVE PAR DEFAUT en local, pour ne pas
  bloquer le developpement sans serveur mail.
- Comportement transactionnel : une notification qui echoue ne doit
  JAMAIS annuler une validation metier deja committee.
  Deux facons de le garantir, presente-les :
    N-1) envoi APRES commit (@TransactionalEventListener AFTER_COMMIT,
         ou equivalent)
    N-2) echec de notification simplement avale et journalise
         (try/catch + log)
  ATTENTION : ce point est le miroir exact du piege traite en MM.4 pour
  l'audit. Relis la decision T-1 prise a ce moment-la (ecouteur
  synchrone, meme transaction) et dis-moi si la meme logique s'applique
  ici ou si les notifications justifient un traitement DIFFERENT --
  l'audit est une obligation reglementaire (RG-09), une notification
  est un confort. Je pense que la reponse differe, mais je veux ton
  analyse, pas ma supposition.
```

---

## 4. Étape 3. Notifications — implémentation

```
Une fois la conception validee, implemente derriere l'abstraction
NotificationService existante (processus/service/).

- Garde NotificationServiceStub fonctionnel : c'est lui qui doit
  s'activer quand le mode est desactive en local. Ne le supprime pas.
- Branche les appels selon la matrice validee a l'etape 2, en
  REUTILISANT UtilisateurApi.destinatairesParRole() et
  destinataireParId() -- deja disponibles, dependance deja declaree.
  N'introduis AUCUN acces a UtilisateurRepository.
- Si MM.12 a ete fait et que la matrice couvre le workflow des grilles :
  attention, GrilleTarifaireService vit dans referentiel, qui n'a PAS
  de dependance declaree vers processus (ou vit NotificationService).
  => C'est le MEME piege architectural que RG-08 en MM.12 : notifier
     depuis referentiel creerait un cycle referentiel -> processus.
     ARRETE-TOI et signale-le-moi si la matrice l'exige. Options
     possibles a me presenter : deplacer NotificationService dans un
     module partage, le dupliquer, ou notifier via un evenement
     applicatif ecoute par processus.

TESTS : NotificationService mocke, verification que les BONS acteurs
sont notifies aux BONS evenements (une assertion par ligne de la
matrice), et qu'un echec d'envoi ne fait pas echouer la transaction
metier.
```

---

## 5. Étape 4. Étendre `BeneficiaireApi` pour l'événement enrichi

```
C'est le volet INTER-MODULES de ce sprint. Traite-le avant le Kafka
lui-meme.

L'evenement enrichi a besoin, PAR BENEFICIAIRE INCLUS :
  code unite, code agence, numero de compte courant, chapitre,
  nom et prenom, fonction retenue, montant attribue.

Origine de chaque donnee -- verifie-la dans le code, ne suppose pas :
  - fonction retenue et montant attribue : portes par
    processus/model/entity/LigneEtatMensuel (fonctionRetenue,
    montantApplique). INTRA-MODULE, aucun probleme -- et ce sont les
    valeurs FIGEES du processus, pas les valeurs courantes du
    beneficiaire. Ne les recalcule PAS.
  - nom/prenom, code unite, code agence, compte courant, chapitre :
    appartiennent au module beneficiaires. Doivent passer par
    BeneficiaireApi.

QUESTION A ME POSER, NE TRANCHE PAS SEUL :
faut-il ETENDRE BeneficiaireDocumentDto (qui porte deja nomPrenoms,
codeUnite, numCompteCourant et sert au PDF) ou creer un DTO DEDIE a
l'evenement de cloture ?
  - ETENDRE : moins de code, mais couple le contrat du PDF a celui de
    la comptabilite -- deux consommateurs aux besoins differents qui
    evolueront separement.
  - DTO DEDIE : deux DTO proches, mais chaque consommateur a son
    contrat propre. Coherent avec la discipline de MM.2 ("expose le
    MINIMUM necessaire", "deux DTO plutot qu'un superset").
Presente les deux avec ton avis, attends ma reponse.

Rappel de la regle absolue : aucune methode de BeneficiaireApi ne doit
retourner l'entite JPA Beneficiaire (CLAUDE.md section 18 point 4).

Une fois la decision prise, implemente cote beneficiaires/api/ et
beneficiaires/service/BeneficiaireApiImpl.java, puis lance
ModularityTests : verifie qu'aucune nouvelle violation n'apparait et
que la dependance processus -> beneficiaires::api existante suffit.
```

---

## 6. Étapes 5 et 6. Événement Kafka enrichi

### 6.1 Schéma

```
Selon la decision D de M.0 (schema provisoire propose par DOTTEL,
marque "a valider avec la comptabilite") :

Propose un processus/api/EvenementClotureDto enrichi contenant :
  - les champs AGREGES actuels, CONSERVES pour ne rien casser :
    idProcessus, moisPaiement, anneePaiement, montantTotal, dateCloture
  - une LISTE de lignes, une entree par beneficiaire INCLUS
    (inclusDansEtat = true uniquement -- verifie ce point, un exclu ne
    doit pas partir en comptabilite)

Chaque ligne : code unite, code agence, numero de compte courant,
chapitre, nom et prenom, fonction retenue, montant attribue.

Montants en FCFA ENTIER (long), coherent avec l'existant.

MARQUE LE DTO EN COMMENTAIRE comme "schema provisoire, a valider avec
la comptabilite" -- exigence de la decision D. Sans ce marquage, un
futur intervenant le prendra pour un contrat figé.

Rappel du schema d'ecritures comptables cible (CLAUDE.md section 11),
qui explique POURQUOI ces champs sont demandes :
  DEBIT  : CODE_UNITE - 64310090002 - montant - DOT TEL MM/AAAA
  CREDIT : AGENCE - N_COMPTE_COURANT - montant - DOT TEL MM/AAAA
=> code unite alimente le DEBIT, code agence + compte courant
   alimentent le CREDIT. Verifie que ta proposition couvre bien les
   deux lignes d'ecriture.

Montre le DTO et un exemple de payload JSON complet AVANT de modifier
le service.
```

### 6.2 Implémentation

```
Adapte processus/service/EvenementClotureService.java pour construire
le payload detaille a partir des lignes du processus cloture.

- Une entree par beneficiaire INCLUS. Recupere les donnees beneficiaire
  via l'API etendue a l'etape 4, en UN SEUL appel par lot (methode
  ...ParId(Collection<Long>)) -- pas un appel par ligne, sinon N+1.
- Le CHAPITRE vient de la donnee EHR par beneficiaire, avec le repli
  configurable deja prevu (dottel.documents.chapitre-defaut). Verifie
  comment DocumentService le resout aujourd'hui et fais PAREIL -- ne
  reimplemente pas une seconde logique de repli.
- DOTTEL reste PRODUCTEUR UNIQUEMENT : aucun @KafkaListener, aucun
  consumer (CLAUDE.md section 11 et section 17 point 9). L'envoi reste
  fire-and-forget, la cle du message reste idProcessus.
- Adapte config/EvenementClotureSerializer.java si necessaire :
  tools.jackson UNIQUEMENT, jamais com.fasterxml.jackson.

VERIFICATION REELLE EXIGEE : lance le Kafka local
(docker compose up -d, service dottel-kafka deja present depuis le
Sprint 5.3), declenche un cycle complet jusqu'a la cloture DRH, et LIS
le message publie avec :
  docker exec -it dottel-kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic dottel.processus.cloture --from-beginning
Montre-moi le JSON reellement publie, pas une conclusion.

TESTS : adapte les tests de publication existants pour couvrir le
payload detaille (un beneficiaire inclus, un exclu -> seul l'inclus
figure dans le payload ; montants en long ; champs agreges conserves).
```

---

## 7. Étape 7. Vérification

```
BACKEND :
  cd backend ; .\mvnw.cmd test
Attendu : total de MM.12 + les nouveaux tests, 0 echec.

MODULARITE :
  ModularityTests VERT, TOUJOURS UNE SEULE exception filtree (cycle
  beneficiaires <-> referentiel, G-2). Si l'etape 3 a exige de notifier
  depuis referentiel, verifie qu'aucun second cycle n'est apparu.

KAFKA REEL :
  docker compose up -d, puis cycle complet jusqu'a cloture, puis
  lecture du topic (voir etape 6.2). Le JSON publie doit contenir les
  lignes par beneficiaire ET les champs agreges.

NOTIFICATIONS :
  En mode local desactive : verifier qu'aucun envoi n'est tente et
  qu'aucune erreur ne remonte.
  Si un serveur SMTP de test est disponible : verifier qu'un mail part
  bien aux bons destinataires.

FRONTEND :
  Aucune modification attendue dans ce sprint. Si git montre un fichier
  modifie sous frontend/, c'est une erreur a signaler.

VERIFICATION MANUELLE (liste a donner a l'utilisateur) :
1. Cycle complet ARH -> CRH -> DRH sur un processus, avec au moins un
   beneficiaire EXCLU volontairement.
2. Lire le message Kafka publie : verifier que le beneficiaire exclu
   n'y figure PAS, et que l'inclus porte bien code unite (4 chiffres),
   code agence (5 chiffres), compte courant, chapitre, nom, fonction
   retenue et montant.
3. Verifier que les champs agreges (montantTotal, dateCloture) sont
   toujours presents et corrects.
4. Verifier qu'un echec de notification (couper le serveur mail, ou
   mettre une config invalide) ne fait PAS echouer la validation.
```

---

## 8. Critères de validation

| Élément | Statut attendu |
|---|---|
| **Prérequis MM.10 vérifié** : colonne `code_agence` existante | Vérifié |
| Canal technique (SMTP / Graph) tranché **avec l'utilisateur** | Fait |
| Matrice acteur/événement validée **avec l'utilisateur**, appels existants analysés avant d'en ajouter | Fait |
| Notifications sur le workflow des **grilles** : question posée, cycle de modules évalué | Fait |
| Comportement transactionnel (N-1 / N-2) tranché, comparé à la décision T-1 de MM.4 | Fait |
| Paramètres Outlook en **variables d'environnement**, jamais en dur | Vérifié |
| Mode local **désactivé par défaut**, `NotificationServiceStub` conservé | Vérifié |
| Échec de notification **non bloquant** pour la transaction métier | Vérifié par test |
| `UtilisateurApi` réutilisée, **aucun accès à `UtilisateurRepository`** | Vérifié |
| **Extension vs DTO dédié** pour l'événement : tranché avec l'utilisateur | Fait |
| Aucune méthode d'API ne retourne l'entité JPA `Beneficiaire` | Vérifié |
| Événement enrichi : **une entrée par bénéficiaire INCLUS uniquement** | Vérifié sur payload réel |
| `fonctionRetenue` et `montantApplique` pris de `LigneEtatMensuel` (valeurs figées), **non recalculés** | Vérifié |
| Chapitre résolu avec le **même** mécanisme de repli que `DocumentService` | Vérifié |
| Champs agrégés (`idProcessus`, mois, année, `montantTotal`, `dateCloture`) **conservés** | Vérifié sur payload réel |
| Montants en **FCFA entier** (`long`) | Vérifié |
| `tools.jackson` uniquement, jamais `com.fasterxml.jackson` | Vérifié |
| DTO marqué **« schéma provisoire, à valider avec la comptabilité »** (décision D) | Vérifié |
| Payload couvre les deux lignes d'écriture comptable (DÉBIT code unité / CRÉDIT agence + compte) | Vérifié |
| Pas de `@KafkaListener` : DOTTEL reste **producteur uniquement** | Vérifié |
| Aucun appel N+1 (récupération par lot) | Vérifié |
| **JSON réellement publié** lu sur le topic local | Vérifié |
| `ModularityTests` vert, toujours une seule exception filtrée | Vérifié |
| Aucune modification frontend | Vérifié |
| Suite backend : ≥ total MM.12, 0 échec | Vérifié |

---

## Commit

```bash
git add .
git commit -m "mm.13: notifications outlook provisoires et evenement kafka enrichi par beneficiaire"
```

---

**Fin du Sprint MM.13 — Fin du lot d'ajouts métier, version monolithe modulaire**

*Points restant en attente externe : canal Outlook réel (DSI), schéma Kafka définitif (comptabilité), intégration EHR réelle (DSI), realm Keycloak (DSI, voir MM.7).*
