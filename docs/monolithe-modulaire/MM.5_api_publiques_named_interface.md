# SPRINT MM.5

## Déclarer les API publiques et fermer les modules

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Marquer explicitement ce que chaque module expose via `@NamedInterface`, et rendre tout le reste inaccessible depuis l'extérieur du module |
| **Livrable** | Une API publique déclarée par module ; repositories, entités et détails internes devenus package-private |
| **Durée** | Une journée |
| **Prérequis** | MM.3 et MM.4 terminés, seules subsistent d'éventuelles violations assumées |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Inventaire du public existant (étape 2) | Sonnet 5 | Medium |
| Déclaration des `@NamedInterface` (étapes 3-4) | Sonnet 5 | Medium |
| Fermeture progressive (étape 5) | Sonnet 5 | Medium à High |

L'effort monte à High sur l'étape 5 uniquement : passer une classe en package-private casse la compilation partout où elle était utilisée illégitimement, et il faut distinguer un usage à corriger d'un usage à autoriser.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

## 1. Contexte

MM.1 à MM.4 ont supprimé les accès illégitimes. Ce sprint pose la serrure : jusqu'ici, rien n'empêche techniquement un développeur de réintroduire demain un `import` vers le repository d'un autre module. Après MM.5, ce code ne compilera plus, ou fera échouer `ModularityTests`.

Trois notions à distinguer, souvent confondues :

| Notion | Portée | Rôle |
|---|---|---|
| `public` / package-private | Compilateur Java | Empêche l'accès, période. |
| `@NamedInterface` | Spring Modulith | Déclare qu'un sous-package est une API exposée, consultable par les autres modules. |
| `ModularityTests` | Test | Vérifie que personne ne contourne les deux règles ci-dessus. |

Les trois sont complémentaires. `@NamedInterface` seul ne ferme rien ; package-private seul ne documente rien.

## 2. Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT :
1. Lis CLAUDE.md dans son integralite (section 9, conventions de code).
2. Lis docs/monolithe-modulaire/PLAN_MONOLITHE_MODULAIRE.md
3. Lis docs/monolithe-modulaire/MM.0_cadrage.md (decoupage acte)
4. Lance /graphify . --update

CONTEXTE : Sprint MM.5. Declarer les API publiques par module et
fermer tout le reste.

REGLE ABSOLUE DE CE SPRINT : aucun changement de comportement. On
change UNIQUEMENT des modificateurs de visibilite et on ajoute des
annotations. Si une methode doit changer de signature pour etre
fermee, c'est le signe d'un couplage non traite en MM.2/MM.3 --
arrete-toi et signale-le.

METHODE DE TRAVAIL :
- Un module a la fois.
- mvn test complet apres chaque module. Reference : >= 205 tests.

PREMIERE ACTION : etape 2, inventaire.
```

## 3. Étape 2. Inventorier ce qui est public aujourd'hui

```
Pour CHAQUE module acte en MM.0, produis un tableau :

| Classe | Visibilite actuelle | Utilisee hors du module ? | Par qui ? | Verdict propose |

Verdicts possibles :
  - API   : doit rester publique et etre declaree @NamedInterface
  - FERME : doit passer package-private
  - DOUTE : utilisee hors du module, mais peut-etre a tort -- a
            examiner avec moi

Points d'attention a ne pas rater dans l'inventaire :

1. Les 9 entites JPA. En principe, AUCUNE ne doit sortir de son
   module (CLAUDE.md section 18 point 4 : jamais d'entite JPA dans
   une reponse API). Verifie-le et signale toute exception.

2. Les 10 repositories. Aucun ne doit etre visible hors de son
   module apres MM.2/MM.3/MM.4.

3. Les 5 enums de model/enums/ (RoleEnum, StatutEnum, StatutEtapeEnum,
   StatutGrilleEnum, NomEtapeEnum). Ceux-la sont partages par
   plusieurs modules par nature -- RoleEnum est utilise par la
   securite ET les utilisateurs ET les controleurs. Ils sont
   probablement transverses, mais confirme-le explicitement.

4. Les 32 classes d'exception. Selon la decision C de MM.0, elles
   sont reparties par module ou transverses. ATTENTION :
   security/GlobalExceptionHandler.java les importe TOUTES. C'est
   attendu pour un @RestControllerAdvice global, mais Spring Modulith
   le verra comme un module dependant de tous les autres. Signale-le,
   on decidera comment le declarer.

5. BeneficiaireService.resoudreMontantCourant() : etait publique avant
   le chantier, appelee par BeneficiaireExportService. Selon la
   decision prise en MM.3 (couplage C2), elle est peut-etre devenue
   inutile ou reste une simple delegation. Verifie son statut reel.

Ne modifie rien a cette etape.
```

## 4. Étapes 3 et 4. Déclarer les API

```
Pour chaque module, cree un sous-package d'API et declare-le.

Convention proposee (a valider avec moi si tu veux en changer) :
  com.afriland.dottel.<module>.api

avec un package-info.java portant :

  @org.springframework.modulith.NamedInterface("api")
  package com.afriland.dottel.<module>.api;

Y placer UNIQUEMENT :
  - les interfaces d'API creees en MM.2 et MM.3
  - les DTO qu'elles echangent
  - les evenements publies par le module (MM.4)

Tout le reste (services d'implementation, repositories, entites)
reste hors de ce sous-package et deviendra package-private a
l'etape 5.

Ajoute aussi, pour chaque module, un package-info.java a la racine du
module avec une courte description en francais de sa responsabilite
-- Spring Modulith la reprendra dans la documentation generee en MM.6.

Montre-moi la structure complete avant de fermer quoi que ce soit.
```

## 5. Étape 5. Fermer, module par module

```
Ferme maintenant le module <MODULE>, et LUI SEUL.

1. Passe en package-private (retire `public`) toutes les classes
   marquees FERME dans l'inventaire de l'etape 2.
2. Compile. Chaque erreur de compilation revele un usage externe.
   Pour chacune, DEUX cas seulement :
     - usage illegitime -> il aurait du etre traite en MM.2/MM.3/MM.4.
       Signale-le-moi, c'est un trou dans les sprints precedents.
     - usage legitime -> la classe appartient a l'API, corrige
       l'inventaire et remets-la publique dans le sous-package api.
   Ne "resous" JAMAIS une erreur en remettant simplement `public`
   sans classer le cas dans l'une des deux categories.
3. Lance la suite complete :
     $env:DB_PASSWORD="admin"
     $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
     cd backend ; .\mvnw.cmd test
4. Montre-moi le diff et le resultat.

PIEGE TECHNIQUE CONNU : Spring et JPA ont besoin d'acceder a certaines
classes par reflexion. Une entite JPA package-private fonctionne, mais
Hibernate exige un constructeur sans argument accessible -- les
entites du projet utilisent deja @NoArgsConstructor de Lombok, donc
ce devrait etre bon. Si une erreur de proxy ou d'instanciation
apparait au demarrage, montre-la-moi plutot que de tout rouvrir en
public.
```

## 6. Étape 6. Déclarer les dépendances légitimes restantes

```
Il restera probablement des dependances inter-modules LEGITIMES qui
font echouer ModularityTests. Les candidates identifiees :

  - security/GlobalExceptionHandler qui importe les 32 exceptions
  - les enums partages (RoleEnum, StatutEnum...)
  - le module reporting, si l'option R-2 a ete retenue en MM.3
  - config/ et security/, transverses par nature

Pour chacune, propose-moi la facon de la DECLARER explicitement a
Spring Modulith (via @ApplicationModule(allowedDependencies = {...})
sur le package-info du module concerne) plutot que de la laisser en
violation silencieuse.

Regle : une dependance declaree est une decision d'architecture
assumee et visible. Une violation ignoree est de la dette invisible.
On veut zero violation NON DECLAREE a la fin de MM.6.

Montre-moi la liste avec ta justification pour chacune, j'arbitre.
```

## 7. Critères de validation

| Élément | Statut attendu |
|---|---|
| Chaque module a un sous-package d'API annoté `@NamedInterface` | Vérifié |
| Chaque module a un `package-info.java` décrivant sa responsabilité | Vérifié |
| Aucun repository n'est visible hors de son module | Vérifié |
| Aucune entité JPA n'est visible hors de son module | Vérifié |
| Aucune API publique ne retourne une entité JPA | Vérifié |
| Chaque erreur de compilation rencontrée a été classée (illégitime / légitime), jamais résolue par un `public` réflexe | Vérifié |
| Dépendances inter-modules légitimes **déclarées** via `allowedDependencies`, pas ignorées | Vérifié |
| Cas de `GlobalExceptionHandler` (32 exceptions) explicitement traité | Fait |
| Cas des 5 enums partagés explicitement traité | Fait |
| Suite complète ≥ 205 tests, 0 échec, 0 erreur | Vérifié |
| Aucun changement de signature de méthode, aucun changement de comportement | Vérifié |
| Aucun contrat API modifié | Vérifié |

## Commit

```bash
git add .
git commit -m "mm.5: api publiques declarees par @NamedInterface et fermeture des modules"
```

---

**Fin du Sprint MM.5** — *en attente de validation avant MM.6*