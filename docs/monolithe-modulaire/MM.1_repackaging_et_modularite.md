# SPRINT MM.1

## Repackaging par domaine et mise en place de Spring Modulith

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Déplacer les classes des couches techniques vers les modules métier actés en MM.0, et poser le test d'architecture qui servira de boussole pour tout le reste du chantier |
| **Livrable** | Nouveaux packages métier, `spring-modulith` dans `pom.xml`, `ModularityTests.java` |
| **Durée** | Une à deux journées |
| **Prérequis** | **MM.0 validé**, ses 4 blocs de décision remplis et datés |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Ajout Spring Modulith et `ModularityTests` (étapes 2-3) | Sonnet 5 | Medium |
| Déplacement des fichiers (étapes 4-9) | Sonnet 5 | Low à Medium |
| Analyse du premier rapport de violations (étape 10) | Sonnet 5 | Medium |

Sprint volontairement mécanique. Le déplacement de fichiers et la correction d'imports ne demandent pas de raisonnement profond — l'effort Low suffit et évite de surpayer un travail répétitif. Seule l'étape 10 (lecture du rapport de violations) mérite Medium.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

À relancer **en fin de sprint également** : le graphe repose sur les chemins de fichiers, il sera entièrement périmé après le repackaging.

## 1. Contexte

Aujourd'hui, `controller/BeneficiaireController.java`, `service/BeneficiaireService.java` et `repository/BeneficiaireRepository.java` vivent dans trois packages différents alors qu'ils forment un seul domaine. Ce sprint les regroupe.

Deux principes gouvernent MM.1 :

**Aucun changement de comportement.** On déplace des fichiers et on corrige des `import`. Aucune signature de méthode, aucune règle métier, aucun contrat API ne change. Les 205 tests doivent rester verts **en permanence**, pas seulement à la fin.

**`ModularityTests` arrive maintenant, pas à la fin.** Introduit dès l'étape 3, il échouera massivement au début — c'est normal et voulu. Il devient le compteur de dette du chantier : rouge en MM.1, il se rapproche du vert à chaque couplage cassé en MM.2, MM.3 et MM.4, et passe vert en MM.6. Sans lui dès maintenant, les sprints suivants avancent à l'aveugle.

## 2. Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT :
1. Lis CLAUDE.md dans son integralite.
2. Lis docs/monolithe-modulaire/PLAN_MONOLITHE_MODULAIRE.md
3. Lis docs/monolithe-modulaire/MM.0_cadrage.md et confirme-moi le
   decoupage ACTE (pas celui propose). Si un bloc de decision est
   encore vide, arrete-toi et dis-le-moi.
4. Lance /graphify . --update

CONTEXTE : Sprint MM.1. Repackaging mecanique des classes vers les
modules metier, ajout de Spring Modulith, creation de ModularityTests.

METHODE DE TRAVAIL :
- Aucun changement de comportement metier. Uniquement des deplacements
  de fichiers et des corrections d'import.
- mvn test complet apres CHAQUE deplacement de module, pas seulement
  a la fin. Commande :
    $env:DB_PASSWORD="admin"
    $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
    cd backend ; .\mvnw.cmd test
- Reference de non-regression : 205 tests, 0 echec. Si le nombre
  DESCEND sous 205, tu t'arretes immediatement et tu me previens --
  un refactoring qui fait disparaitre des tests a supprime de la
  couverture.
- Utilise `git mv` plutot que supprimer/recreer, pour que git detecte
  les renommages et garde l'historique lisible.

PREMIERE ACTION : confirme le decoupage acte en MM.0, puis passe a
l'etape 2 (ajout de Spring Modulith au pom.xml).
```

## 3. Étape 2. Ajouter Spring Modulith au `pom.xml`

État actuel vérifié de `backend/pom.xml` : parent `spring-boot-starter-parent` version `4.1.0`, Java 21, **aucun bloc `<dependencyManagement>`**.

```
Ajoute Spring Modulith a backend/pom.xml.

ATTENTION a la gestion de version : le pom n'a AUCUN bloc
<dependencyManagement> aujourd'hui, il herite tout de
spring-boot-starter-parent 4.1.0.

1. Verifie d'abord si le parent Spring Boot 4.1.0 gere deja une
   propriete `spring-modulith.version`. Si oui, ajoute simplement les
   deux dependances SANS version.
2. Si non, ajoute un bloc <dependencyManagement> important le BOM
   spring-modulith-bom avec une version compatible Spring Boot 4.x,
   puis les deux dependances sans version.

Ne devine pas la version : verifie ce que Maven resout reellement.
Si la resolution echoue, montre-moi l'erreur avant de tenter une
autre version.

Les deux dependances a ajouter :
- org.springframework.modulith:spring-modulith-starter-core
- org.springframework.modulith:spring-modulith-starter-test (scope test)

Place-les apres le bloc "Kafka" existant, avec un commentaire dans le
meme style que les autres blocs du fichier (ex: "<!-- Spring Modulith
(frontieres de modules, chantier MM, Sprint MM.1) -->").

Puis lance `.\mvnw.cmd dependency:resolve` et montre-moi le resultat.
```

## 4. Étape 3. Créer `ModularityTests` avant tout déplacement

```
Cree backend/src/test/java/com/afriland/dottel/ModularityTests.java :

package com.afriland.dottel;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    static final ApplicationModules MODULES =
            ApplicationModules.of(DottelApplication.class);

    @Test
    void verifieLesFrontieresDeModules() {
        MODULES.verify();
    }
}

Ajoute un commentaire d'en-tete en francais expliquant que ce test est
VOLONTAIREMENT rouge pendant tout le chantier MM, qu'il sert de
compteur de dette architecturale, et qu'il ne doit passer vert qu'en
MM.6.

Lance-le une premiere fois MAINTENANT, avant tout deplacement de
fichier, et montre-moi la sortie complete. Elle constitue notre point
de reference : c'est l'etat de couplage AVANT le chantier.

Si ce test fait echouer `mvn test` en entier et bloque les 205 autres
tests, dis-le-moi -- on decidera ensemble s'il faut le desactiver
temporairement (@Disabled avec un commentaire explicite) et le
reactiver en MM.6, ou le garder actif. NE TRANCHE PAS SEUL.
```

**Point d'attention.** Selon la configuration de Surefire, un `ModularityTests` rouge peut faire échouer l'ensemble du build et masquer l'état réel des 205 autres tests. Si c'est le cas, l'option `@Disabled` temporaire avec un commentaire daté est acceptable — mais c'est une décision à prendre explicitement, pas un contournement silencieux.

## 5. Étape 4. Créer la structure de packages cible

```
Cree les packages vides (avec un .gitkeep, comme le fait deja ce
projet dans controller/, service/, repository/, etc.) correspondant
EXACTEMENT au decoupage acte en MM.0.

Ne cree aucun package qui ne figure pas dans la decision C de MM.0.
Si tu constates qu'un service ou une entite n'a pas de module
d'accueil dans la decision actee, arrete-toi et signale-le : c'est un
trou dans le cadrage, pas a toi de l'improviser.

Montre-moi l'arborescence creee avant de deplacer quoi que ce soit.
```

## 6. Étape 5 à 9. Déplacer module par module

Ordre recommandé : **du moins couplé au plus couplé**. Cela permet de rencontrer les cas simples d'abord et d'arriver sur `processus` avec la méthode rodée.

| Ordre | Module | Pourquoi à ce rang |
|---|---|---|
| 1 | `audit` | 2 services, 2 repositories, 1 entité. `AuditServiceImpl` n'appelle aucun autre service. |
| 2 | `integration` | `EhrIntegrationService` et `EvenementClotureService` sont déjà sans dépendance (voir `PLAN_MONOLITHE_MODULAIRE.md` §1.5). |
| 3 | `utilisateurs` | `AuthService` et `UtilisateurAdminService` ne sortent pas de leur domaine, hors appel à `AuditService`. |
| 4 | `referentiel` | Couplage interne fort mais légitime (décision B de MM.0). |
| 5 | `beneficiaires` | Accède au référentiel — les violations commenceront à apparaître ici. |
| 6 | `reporting` | Lit 3 repositories étrangers. Violations attendues. |
| 7 | `processus` | Le plus gros et le plus couplé. En dernier. |

Prompt à répéter pour chaque module, en remplaçant `<MODULE>` :

```
Deplace maintenant le module <MODULE>, et LUI SEUL.

1. Utilise `git mv` pour chaque fichier (controleur, services,
   repositories, entites, DTO) vers son package cible.
2. Corrige tous les imports impactes dans TOUT le projet -- y compris
   dans les fichiers de test.
3. Lance la suite complete :
     $env:DB_PASSWORD="admin"
     $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
     cd backend ; .\mvnw.cmd test
4. Montre-moi : le nombre de tests (doit rester >= 205, 0 echec), et
   le `git status --short` du deplacement.

Ne passe PAS au module suivant tant que la suite n'est pas verte.
Si un test echoue, corrige-le AVANT de continuer -- ne cumule jamais
deux modules deplaces avec un test rouge entre les deux.

Rappel : aucun changement de comportement. Si tu es tente de modifier
une signature de methode ou une regle metier pour "faire passer" le
deplacement, arrete-toi et explique-moi pourquoi -- c'est le signe
d'un couplage a traiter en MM.2 ou MM.3, pas ici.
```

**Cas particulier des DTO.** `model/dto/` est déjà découpé par sous-domaine (`beneficiaire/`, `grille/`, `processus/`…). Ce découpage se transpose presque directement. Deux exceptions à surveiller : `model/dto/ehr/` (à rattacher au module qui consomme l'EHR) et `model/dto/importexcel/` (rattaché à `beneficiaires` si l'import y vit).

**Cas particulier des 32 exceptions.** Selon la décision C de MM.0, elles se répartissent par module ou restent transverses. Si elles se répartissent, attention : `security/GlobalExceptionHandler.java` importe **les 32**. Il restera donc un point de convergence transverse qui référence tous les modules — c'est attendu pour un `@RestControllerAdvice` global, mais Spring Modulith le signalera. À documenter en MM.5.

## 7. Étape 10. Relire le rapport de violations

```
Relance ModularityTests et produis-moi un tableau structure des
violations restantes :

| Module source | Module cible | Type d'acces | Fichier:ligne |

Classe chaque violation dans l'une de ces trois familles :
(a) sera traitee en MM.2 (couplages de ProcessusMensuelService)
(b) sera traitee en MM.3 (C2 a C5 du plan maitre)
(c) sera traitee en MM.4 (appels a AuditService)
(d) AUCUNE des trois -- couplage non identifie par l'analyse initiale

Les violations de la famille (d) sont les plus importantes a me
signaler : elles signifient que le cadrage a rate quelque chose, et
il faudra peut-etre completer MM.3.

Ne corrige AUCUNE violation dans ce sprint. MM.1 ne fait que
deplacer et mesurer.
```

## 8. Étape 11. Régénérer le graphe

```
Relance /graphify . (cartographie COMPLETE, pas --update) : tous les
chemins de fichiers Java ont change, un update incrementiel donnerait
un graphe incoherent.
```

## 9. Critères de validation

| Élément | Statut attendu |
|---|---|
| `spring-modulith-starter-core` et `spring-modulith-starter-test` résolus par Maven | Vérifié |
| `ModularityTests.java` créé et exécuté au moins une fois | Fait |
| Sortie de la première exécution de `ModularityTests` conservée comme point de référence | Fait |
| Tous les modules actés en MM.0 créés, aucun package hors décision | Vérifié |
| Chaque classe de `controller/`, `service/`, `repository/`, `model/entity/` déplacée dans son module | Vérifié |
| Suite de tests verte après **chaque** déplacement de module, pas seulement à la fin | Vérifié |
| Nombre de tests final ≥ 205, 0 échec, 0 erreur | Vérifié |
| Aucune signature de méthode publique modifiée | Vérifié |
| Aucun contrat API modifié (34 endpoints inchangés) | Vérifié |
| Aucune migration Flyway ajoutée | Vérifié |
| Tableau des violations restantes produit et classé en familles (a)/(b)/(c)/(d) | Fait |
| Violations de famille (d) signalées explicitement | Fait |
| Graphe Graphify régénéré en complet | Fait |

## Commit

```bash
git add .
git commit -m "mm.1: repackaging par domaine metier et mise en place de Spring Modulith"
```

---

**Fin du Sprint MM.1** — *en attente de validation avant MM.2*