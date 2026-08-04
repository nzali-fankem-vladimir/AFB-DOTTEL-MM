# MM.7 — Ce qu'il faudra changer pour basculer vers le realm Keycloak réel de la DSI

Le Keycloak local (`docker-compose.yml`, `keycloak/realm-dottel-dev.json`) est
**provisoire**. Cette page liste, honnêtement, ce qui se limite à des
variables d'environnement et ce qui pourrait exiger du code, au moment où la
DSI fournit les paramètres réels du realm.

## 1. Ce qui se limite réellement à des variables d'environnement

| Variable | Aujourd'hui (local) | Le jour de la bascule |
|---|---|---|
| `DOTTEL_KEYCLOAK_ISSUER_URI` (backend) | `http://localhost:8180/realms/dottel-dev` | URL du realm réel DSI |
| `VITE_KEYCLOAK_URL` (frontend, au build) | `http://localhost:8180` | URL du serveur Keycloak réel |
| `VITE_KEYCLOAK_REALM` (frontend, au build) | `dottel-dev` | Nom du realm réel |
| `VITE_KEYCLOAK_CLIENT_ID` (frontend, au build) | `dottel-frontend` | Nom du client réel (à créer côté DSI) |

`SecurityConfig` (backend) et `AuthProviderKeycloak` (frontend) ne changent
pas : ils lisent ces variables, jamais de valeur en dur.

## 2. Ce qui devra être confirmé avec la DSI, et pourrait exiger du code

Ces points ne sont **pas** de simples variables — ils sont des hypothèses
prises pour le realm local, à vérifier avant la bascule.

1. **Forme du claim de rôle.** `RoleJwtAuthenticationConverter` lit
   `realm_access.roles` (rôles de *realm*). Si le realm réel place les 5
   rôles DOTTEL en rôles de *client* (`resource_access.<client>.roles`), le
   convertisseur devra être adapté.

2. **Claim `matricule`.** Le realm local expose `matricule` via un attribut
   utilisateur + un protocol mapper ajoutés spécifiquement pour ce sprint
   (aucun champ Keycloak natif ne le porte). Rien ne garantit que le realm
   réel, fédéré à l'Active Directory, expose un claim équivalent. S'il est
   absent, le garde-fou frontend « un administrateur ne peut pas se
   désactiver lui-même » (`UtilisateursListPage.jsx`) perd sa comparaison
   fiable — une solution de repli sera à définir (le garde-fou serveur,
   dans `UtilisateurAdminService`, reste lui intact dans tous les cas).

3. **Forme exacte du claim `email`.** Section 3.3 du guide MM.7 (question 3,
   décision I-2) laissait deux inconnues non tranchées :
   - le comportement de la convention `prenom_nom` sur un **prénom composé**
     (ex. « Jean Paul MBARGA ») n'est pas confirmé ;
   - le realm peut exposer `preferred_username` comme l'identifiant seul
     (`vladimir_nzali`) ou comme l'email complet.
   `AuthenticatedUserService` suppose que le claim `email` du jeton est
   identique, caractère pour caractère, à `utilisateurs.email` en base.

4. **Configuration du client côté realm réel.** Le client doit y être créé
   avec PKCE actif (`S256`), le flux Authorization Code activé, et l'URL de
   callback de production (`https://<domaine-prod>/auth/callback`)
   enregistrée dans les redirect URIs. Ce n'est pas une variable
   d'environnement : c'est une configuration à demander explicitement à la
   DSI, préalable au déploiement.

5. **Fédération Active Directory.** La section 2.3 du guide MM.7 documente
   un fonctionnement observé (pas une garantie contractuelle) : formulaire
   Keycloak classique avec identifiants Windows. Si le realm réel utilise en
   réalité un mécanisme d'authentification intégrée (SSO Kerberos/NTLM), le
   flux Authorization Code + PKCE reste valide côté DOTTEL, mais
   l'expérience de connexion (pas de formulaire Keycloak visible) différera
   de ce qui a été testé ici.

## 3. Ce qui ne change pas et n'est pas concerné par la bascule

- Les 34 endpoints REST du contrat API (hors `POST /auth/login`, supprimé en
  MM.7 — voir décision de portée P-2).
- `motDePasseHash` sur `Utilisateur` reste `NOT NULL` mais sans usage réel
  (Keycloak porte l'authentification) — non traité ce sprint (acté en
  section 3.3 du guide MM.7), sujet pour un futur sprint.
- Le jeton reste en mémoire seule côté frontend (jamais `localStorage`/
  `sessionStorage`) — décision Sprint 6F.1, inchangée.

**Conclusion honnête :** l'objectif annoncé du sprint — que la bascule se
limite à des variables d'environnement — est **atteint pour le mécanisme
d'échange de jeton lui-même** (issuer, URLs Keycloak). Il ne l'est **pas**
pour la totalité du sprint : les points 1 à 3 ci-dessus dépendent de
paramètres du realm réel non confirmés à ce jour, et pourraient exiger un
ajustement de code, pas seulement de configuration.
