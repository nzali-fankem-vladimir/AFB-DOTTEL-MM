# Fiche de liaison DSI — Module DOTTEL (Dotations Téléphoniques Mensuelles)

**Destinataire** : DSI Afriland First Bank
**Objet** : État de livraison de la copie modulaire du module DOTTEL et
points techniques en attente d'arbitrage avant mise en production.
**Date** : 2026-08-24

---

## 1. État de livraison

### 1.1 Fait

- Backend Spring Boot 4 organisé en monolithe modulaire (Spring Modulith,
  7 modules métier + 2 transverses), frontières vérifiées à chaque build
  par `ModularityTests`.
- Frontend React 19, charte visuelle BAOBAB, accessibilité auditée.
- Workflow complet ARH → CRH → DRH pour le processus mensuel et pour les
  grilles tarifaires (workflow à trois acteurs depuis MM.12), avec
  séparation des tâches vérifiée (RG-08), y compris sur le scénario réel
  de promotion de rôle en cours de circuit.
- Authentification Keycloak réelle (Authorization Code + PKCE), plus
  aucune simulation JWT locale.
- Enrôlement, gestion des bénéficiaires (import/export Excel), gestion
  des grilles tarifaires et des fonctions éligibles, rattrapage sur
  période close, tableau de bord et journal d'audit.
- Génération et signature progressive du PDF de l'état mensuel (iText),
  événement Kafka de clôture consommé par le module comptable.
- Tests d'intégration bout en bout (Sprint 7.1) et recette fonctionnelle
  des 5 rôles (Sprint 7.2) — voir `docs/chantier-livraison-mm/`.
- Dockerfile multi-stage et manifests Kubernetes de base (Deployment,
  Service, ConfigMap, Secret).

### 1.2 Hors périmètre de ce projet de stage

- Écritures comptables (génération du fichier d'écritures, validation
  analyste, imputation CBS) : le module publie un événement de clôture,
  la suite est assurée par le module comptable existant, non touché ici.
- Intégration EHR réelle, realm Keycloak réel, canal de notification réel :
  voir section 2.
- Pipeline CI/CD et paramétrage définitif du cluster Kubernetes cible :
  voir section 3.

---

## 2. Points en attente de la DSI

Chaque point ci-dessous a été vérifié dans le code au moment de la
rédaction de cette fiche, pas recopié d'une liste générique.

### 2.1 Realm Keycloak réel de la banque

Le realm actuellement utilisé (`dottel-dev`) est un **Keycloak local,
provisoire**, déclaré dans `docker-compose.yml` et importé depuis
`keycloak/realm-dottel-dev.json` — il tourne uniquement sur un poste de
développement, avec 5 comptes de test.

**Confirmé au Sprint MM.7, par observation directe du fonctionnement de
BAOBAB** : le realm réel de la banque est fédéré à l'Active Directory,
l'identifiant de connexion est la partie locale de l'email professionnel
(convention `prenom_nom@afrilandfirstbank.com`).

**Restent à fournir par la DSI** : URL du realm réel, nom du client
OAuth2 à créer, et confirmation du format exact de l'identifiant sur un
cas de prénom composé (non vérifié — voir
`docs/monolithe-modulaire/MM.7_keycloak_provisoire.md` section 3.3).

**Impact sur la mise en production** : la bascule est conçue pour se
limiter à la variable d'environnement `DOTTEL_KEYCLOAK_ISSUER_URI`
(aucune valeur de repli codée en dur, voir `k8s/configmap.yaml`) — sous
réserve que le format des claims du jeton (rôles dans `realm_access.roles`,
email dans `email`) soit identique à celui du realm local. Si le realm
réel expose les rôles ou l'identité différemment, `RoleJwtAuthenticationConverter`
et `AuthenticatedUserService` devront être adaptés — pas seulement
reconfigurés.

### 2.2 Accès EHR réel

`EhrIntegrationServiceStub` simule actuellement une dizaine d'employés
avec des données camerounaises cohérentes (matricule, fonction, unité,
compte courant). Il n'a jamais été connecté à un EHR réel. Le contrat
d'interface (`EhrIntegrationService`) est en place et prêt à recevoir une
implémentation réelle sans changement côté appelants.

**Impact sur la mise en production** : bloquant pour l'enrôlement — sans
EHR réel, aucun nouvel employé ne peut être vérifié ni enrôlé. Prévoir
également la correspondance entre les libellés de fonction de l'EHR réel
et les codes `fonction_eligible` (25 fonctions actuellement).

### 2.3 Solution de stockage PDF persistant

`DocumentService` écrit le PDF de l'état mensuel sur le système de
fichiers local du pod (`DOTTEL_DOCUMENTS_CHEMIN_STOCKAGE`, `/app/documents/`
en configuration Kubernetes actuelle). **Vérifié dans
`k8s/deployment.yaml` : aucun volume persistant n'est monté.** Un
redémarrage ou une réplication du pod perd les PDF déjà générés.

**Impact sur la mise en production** : bloquant si le pod redémarre
après une clôture de processus — le seul PDF signé (RG-06) disparaîtrait.
Nécessite un volume Kubernetes persistant ou un stockage objet (S3-compatible
ou équivalent interne banque), à arbitrer avec la DSI avant le premier
déploiement réel.

### 2.4 Broker Kafka réel

`KAFKA_BOOTSTRAP_SERVERS` pointe actuellement vers un conteneur Kafka
local (`docker-compose.yml`, `dottel-kafka`, sans authentification). Le
`ConfigMap` de production porte un placeholder
(`kafka-service.dottel.svc.cluster.local:9092`) à remplacer par
l'adresse réelle du broker Kafka de la banque.

**Impact sur la mise en production** : bloquant pour la clôture d'un
processus mensuel — DOTTEL est producteur uniquement de l'événement de
clôture, le module comptable en dépend pour démarrer les écritures.
Vérifier également si le broker réel exige une authentification
(SASL/SSL), non implémentée dans `KafkaConfig` actuel (conçu pour un
broker local sans authentification).

### 2.5 Canal de notification réel (Outlook)

`NotificationServiceSmtp` (Sprint MM.13) est prêt mais désactivé par
défaut (`DOTTEL_NOTIFICATIONS_ENABLED=false`) : en son absence,
`NotificationServiceStub` journalise seulement. Serveur SMTP, port,
identifiants et adresse d'expédition réels ne sont pas connus — tous
marqués « À CONFIRMER DSI » dans `k8s/configmap.yaml` /
`k8s/secret.yaml`.

**Impact sur la mise en production** : non bloquant. Une notification
est un confort (retour de motif, rejet de grille), jamais une condition
de fonctionnement du workflow — décision actée au Sprint MM.13.

### 2.6 Pipeline CI/CD Harbor et cluster Kubernetes

**Vérifié dans le dépôt** : aucun pipeline CI/CD n'existe (pas de
`.github/workflows`, pas de `Jenkinsfile`). `k8s/deployment.yaml`
référence une image Harbor en placeholder
(`harbor.afrilandfirstbank.cm/baobab/dottel-backend:latest`) à publier
manuellement ou via un pipeline à construire.

**Impact sur la mise en production** : bloquant pour un déploiement
automatisé — en son absence, la mise en production nécessiterait un
`docker build` + push manuel vers Harbor, puis un déploiement manuel des
manifests. Namespace, conventions de nommage définitives des
Deployments/Services et méthode de gestion des Secrets (Vault, Sealed
Secrets, etc.) restent également à confirmer avec la DSI — actuellement
notés en commentaire dans chaque manifest.

---

## 3. Procédure de déploiement Kubernetes

**État réel vérifié** (pas supposé) :

| Élément | Présent | Détail |
|---|---|---|
| `backend/Dockerfile` | Oui | Multi-stage (build Maven puis JRE Alpine), utilisateur non-root, `HEALTHCHECK` intégré, aucune variable déclarée en `ENV` (pour que l'absence d'une variable obligatoire fasse échouer le démarrage plutôt que de le masquer) |
| `k8s/deployment.yaml` | Oui | 1 réplique, probes liveness/readiness sur `/api/actuator/health`, `envFrom` ConfigMap + Secret, limites de ressources définies (256Mi/250m → 512Mi/500m) |
| `k8s/service.yaml` | Oui | `ClusterIP`, port 8080 |
| `k8s/configmap.yaml` | Oui | 10 variables non sensibles, chacune commentée avec son statut (placeholder local vs à confirmer DSI) |
| `k8s/secret.yaml` | Oui | 4 variables sensibles (`DB_USER`, `DB_PASSWORD`, `DOTTEL_MAIL_USERNAME`, `DOTTEL_MAIL_PASSWORD`), valeurs `CHANGEME` en base64, jamais de vraie valeur committée. `DOTTEL_JWT_SECRET` retirée au Sprint MM.14 (n'a plus d'usage depuis MM.7) |
| Volume persistant pour les PDF | **Non** | Voir point 2.3 — à ajouter avant le premier déploiement réel |
| Pipeline CI/CD | **Non** | Voir point 2.6 |

---

## 4. Procédure de première mise en service

### 4.1 Création du premier compte ADMIN

**Vérifié dans le code** : il n'existe aucun mécanisme de première
création d'ADMIN "en libre-service" — `POST /admin/utilisateurs` exige
déjà d'être authentifié en tant qu'ADMIN (`@PreAuthorize("hasRole('ADMIN')")`),
et il n'y a plus de login local depuis MM.7 permettant un compte de
secours applicatif.

**Mécanisme réel pour amorcer un environnement neuf** : insérer
directement une ligne dans la table `utilisateurs` (`role = 'ADMIN'`,
`actif = true`) dont l'**email correspond exactement** à un compte réel
du realm Keycloak cible — c'est cet email, pas le matricule, qui relie
le jeton Keycloak à l'identité DOTTEL (décision I-2, section 13 de
`CLAUDE.md`). La migration `V3__insertion_utilisateurs_test.sql` fait
exactement cela pour l'environnement de développement (5 comptes de
test) ; en production, elle doit être adaptée avec l'email réel d'un
premier administrateur DOTTEL désigné par la banque avant le premier
déploiement, ou remplacée par un script d'amorçage équivalent, hors
Flyway.

### 4.2 Initialisation des grilles tarifaires

**Déjà couverte** : la migration `V2__insertion_donnees_reference.sql`
insère les 25 fonctions éligibles et leur grille tarifaire initiale
(statut `ACTIVE`), appliquée automatiquement par Flyway au premier
démarrage. Aucune action manuelle requise pour ce point.

---

## 5. Contacts et références

- Rapport de recette fonctionnelle complet : `docs/chantier-livraison-mm/7.2_rapport_recette.md`
- Contrat API détaillé : `docs/reference/contrats_api_dotations_v3.md`
- Historique du chantier de modularisation : `docs/monolithe-modulaire/`
- `CLAUDE.md` — référence technique complète du module
