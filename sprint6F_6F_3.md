# SPRINT 6F.3

## Authentification frontend

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Page de connexion, contexte d'authentification, protection des routes |
| **Livrable** | LoginPage, AuthContext, ProtectedRoute, client axios avec intercepteur JWT |
| **Durée** | Une journée |
| **Prérequis** | Sprint 6F.2 validé |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| AuthContext et abstraction (étapes 2-3) | Sonnet 5 | High |
| Login, protection de routes, client API (étapes 4-6) | Sonnet 5 | Medium |

L'étape 2 mérite High : c'est ici que se construit l'abstraction qui permettra une future bascule vers Keycloak réel sans tout réécrire — même enjeu que les stubs déjà réussis côté backend (EHR, notification, signature).

## 0. Nouvelle session : rappel Graphify

```
py -3.14 -m graphify update .
```

## 1. Contexte

Décision actée : simulation Keycloak locale côté frontend, comme côté backend, architecturée pour faciliter une intégration future. Concrètement, l'authentification frontend consomme `POST /auth/login` (déjà construit, Sprint 0.4/2.2) exactement comme le ferait un vrai flux Keycloak côté client — mais l'implémentation est isolée derrière une interface, pas dispersée dans les composants.

Rappel de la décision de sécurité du Sprint 6F.1 (stockage du token) : appliquée ici concrètement.

Richard utilise `next-auth` (`SessionProviderWrapper.tsx`, `useSession`) — non transposable, DOTTEL n'a pas de Server Components. Le pattern à retenir de son code n'est pas la lib, mais l'idée d'un contexte global exposant l'utilisateur courant et ses rôles à toute l'application.

## 2. Objectifs

- `AuthProvider` interface (abstraction, remplaçable par un vrai provider Keycloak plus tard)
- `AuthProviderLocal` : implémentation actuelle, appelle `POST /auth/login`
- `AuthContext` (React Context) exposant `user`, `roles`, `login()`, `logout()`
- `LoginPage.jsx`
- `ProtectedRoute.jsx` : redirige vers `/connexion` si non authentifié, filtre par rôle si nécessaire
- Client axios avec intercepteur ajoutant `Authorization: Bearer {token}`

## 3. Rappel de la règle de sécurité (Sprint 6F.1)

```
Applique la decision de stockage du token prise au Sprint 6F.1 (a
rappeler ici si la session a change entre-temps -- confirme-la avec
moi avant de coder si un doute existe sur ce qui avait ete tranche).
```

## 4. Étapes d'implémentation

### Étape 1. Ouvrir une session Claude Code

```
Tu es mon assistant de developpement pour le projet de digitalisation
des dotations telephoniques mensuelles d'Afriland First Bank.

AVANT TOUT : lis CLAUDE.md. Confirme via graphify que Sidebar,
AppLayout et le routage de base (Sprint 6F.2) existent deja.
Confirme aussi la decision de stockage du token prise au Sprint 6F.1.

CONTEXTE : Sprint 6F.3. Simulation Keycloak locale, architecturee
pour faciliter une future integration reelle -- meme principe que le
stub EHR cote backend (Sprint 2.2) : une interface claire,
implementation remplacable sans toucher au reste du code.

METHODE DE TRAVAIL :
- Un fichier a la fois. Tu montres, j'approuve, tu continues.
- Si un choix n'est pas couvert par CLAUDE.md, tu poses la question
  plutot que de supposer.

PREMIERE ACTION : propose la forme de l'interface AuthProvider
(methodes attendues : login(matricule, motDePasse), logout(),
utilisateurCourant()) avant d'ecrire l'implementation locale. Montre
uniquement la structure proposee, pas encore le code complet.
```

### Étape 2. AuthProvider et implémentation locale

```
Une fois la structure validee, cree :
1. AuthProviderLocal.js : implemente login() en appelant
   POST /auth/login, stocke le token selon la decision du Sprint
   6F.1, expose utilisateurCourant() (decode le token ou garde les
   infos retournees par login en memoire/contexte)
2. AuthContext.jsx : Context React + hook useAuth(), s'appuie sur
   AuthProviderLocal pour l'instant (le jour d'une vraie integration
   Keycloak, seul ce fichier changera d'implementation sous-jacente)

Montre les deux fichiers.
```

### Étape 3. Client API avec intercepteur JWT

```
Cree apiClient.js (axios), intercepteur de requete ajoutant
Authorization: Bearer {token} depuis AuthContext, intercepteur de
reponse qui redirige vers /connexion si 401 recu (token expire ou
invalide). Montre le fichier.
```

### Étape 4. LoginPage

```
Cree LoginPage.jsx : formulaire matricule/mot de passe (reutilise
FormField du Sprint 6F.1), appelle login() du AuthContext,
redirige vers la page adaptee au role apres connexion reussie.
Affiche les erreurs (401 identifiants incorrects, 403 compte
desactive) de facon claire, sans jargon technique. Montre le
fichier.
```

### Étape 5. ProtectedRoute

```
Cree ProtectedRoute.jsx : composant wrapper qui verifie
utilisateurCourant() via useAuth(), redirige vers /connexion si
absent, verifie optionnellement une liste de roles autorises et
redirige vers une page "acces refuse" sinon. Applique-le aux routes
deja configurees au Sprint 6F.2. Montre le fichier et la
configuration de routes mise a jour.
```

### Étape 6. Vérification

```
Lance npm run dev, teste une connexion reelle avec un utilisateur de
test (ex: matricule 1847, mot de passe Test1234), verifie la
redirection, verifie qu'un acces direct a une route protegee sans
connexion redirige bien vers /connexion, verifie le comportement
apres un 401 simule (token expire).
```

## 5. Critères de validation

| Élément | Statut attendu |
|---|---|
| AuthProvider isolé derrière une interface remplaçable | Fait |
| Connexion réelle fonctionnelle contre le backend | Vérifié |
| Routes protégées, redirection correcte | Vérifié |
| Intercepteur JWT et gestion du 401 fonctionnels | Vérifié |
| Décision de stockage du token appliquée sans écart | Vérifié |

## Commit

```bash
git add .
git commit -m "sprint-6F.3: authentification frontend (simulation Keycloak locale)"
```

---

**Fin du Sprint 6F.3** — *en attente de validation avant le Sprint 6F.4*
