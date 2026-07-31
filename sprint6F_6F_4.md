# SPRINT 6F.4

## Écrans d'enrôlement

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Parcours de l'employé : vérification puis confirmation d'enrôlement |
| **Livrable** | Pages de vérification et confirmation d'enrôlement, formulaire d'import Excel |
| **Durée** | Une journée |
| **Prérequis** | Sprint 6F.3 validé |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Vérification et confirmation (étapes 2-3) | Sonnet 5 | Medium |
| Import Excel (étape 4) | Sonnet 5 | Medium |

## 0. Nouvelle session : rappel Graphify

```
py -3.14 -m graphify update .
```

## 1. Contexte

Premier domaine fonctionnel du frontend : le parcours EMPLOYE, le plus simple des rôles (deux écrans). Consomme `GET /enrolement/verifier` et `POST /enrolement/confirmer` (Sprint 2.2/2.3), déjà stables et documentés. Ajoute aussi la page d'import Excel en masse pour l'ARH (`POST /beneficiaires/import`, Sprint 2.4).

Rappel important côté UX : `GET /enrolement/verifier` retourne `200` avec `eligible:false` en cas de non-éligibilité — ce n'est **pas une erreur HTTP**, l'interface doit donc afficher un message informatif, pas une bannière d'erreur technique.

## 2. Objectifs

- `VerifierMatriculePage.jsx` : saisie du matricule, affichage des infos EHR préremplies, message clair si non éligible
- `ConfirmerEnrolementPage.jsx` : bouton de confirmation, gestion des erreurs (403, 404, 409, 400)
- `ImporterBeneficiairesPage.jsx` : upload de fichier Excel, affichage du rapport (insérés/rejetés avec motifs)
- Tests visuels manuels

## 3. Étapes d'implémentation

### Étape 1. Ouvrir une session Claude Code

```
Tu es mon assistant de developpement pour le projet de digitalisation
des dotations telephoniques mensuelles d'Afriland First Bank.

AVANT TOUT : lis CLAUDE.md, en particulier la section 8 (contrat API,
groupe Enrolement et Beneficiaires/import). Confirme via graphify que
AuthContext, ProtectedRoute et apiClient (Sprint 6F.3) existent deja.

CONTEXTE : Sprint 6F.4, premier domaine fonctionnel du frontend.

METHODE DE TRAVAIL :
- Un fichier a la fois. Tu montres, j'approuve, tu continues.
- Si un choix n'est pas couvert par CLAUDE.md, tu poses la question
  plutot que de supposer.

PREMIERE ACTION : cree VerifierMatriculePage.jsx : formulaire de
saisie du matricule (reutilise FormField), appelle
GET /enrolement/verifier via apiClient, affiche les informations
EHR preremplies (nom, fonction, unite) si la reponse revient, avec
un message clair et non alarmant si eligible=false (pas une couleur
d'erreur rouge agressive -- un message informatif neutre). Montre
le fichier.
```

### Étape 2. ConfirmerEnrolementPage

```
Cree ConfirmerEnrolementPage.jsx, accessible seulement apres une
verification reussie et eligible=true (transmet les infos via etat
de navigation ou re-verification). Bouton de confirmation qui appelle
POST /enrolement/confirmer. Gere explicitement les 4 codes d'erreur
possibles (400, 403, 404, 409) avec un message specifique a chacun,
pas un message d'erreur generique. Montre le fichier.
```

### Étape 3. Vérification du parcours complet

```
Teste manuellement le parcours complet avec un matricule EHR de
test connu (ex: 4275, ATANGANA Sylvie, CONSEILLER) : verification
puis confirmation. Verifie aussi le cas d'un matricule deja enrole
(409 attendu a la confirmation).
```

### Étape 4. ImporterBeneficiairesPage

```
Cree ImporterBeneficiairesPage.jsx (route ARH) : zone de depot de
fichier .xlsx, appelle POST /beneficiaires/import (multipart,
verifie comment axios gere le FormData dans apiClient), affiche le
rapport retourne (inseres, rejetes, liste des erreurs avec ligne et
motif) dans un tableau clair. Montre le fichier.
```

## 4. Critères de validation

| Élément | Statut attendu |
|---|---|
| Vérification affiche les infos EHR, message neutre si non éligible | Vérifié |
| Confirmation gère les 4 codes d'erreur distinctement | Vérifié |
| Import Excel affiche le rapport complet (insérés/rejetés/motifs) | Vérifié |
| Parcours complet testé avec des données réelles | Vérifié |

## Commit

```bash
git add .
git commit -m "sprint-6F.4: ecrans d'enrolement et import Excel"
```

---

**Fin du Sprint 6F.4** — *en attente de validation avant le Sprint 6F.5*
