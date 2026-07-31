# SPRINT MM.6

## Vérification finale et documentation générée

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Faire passer `ModularityTests` au vert de bout en bout, générer la documentation d'architecture, et clore le chantier par un récapitulatif |
| **Livrable** | `ModularityTests` vert, diagrammes et documentation Asciidoc générés, récapitulatif de fin de chantier |
| **Durée** | Une demi-journée |
| **Prérequis** | MM.5 terminé |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Résorption des dernières violations (étape 2) | Sonnet 5 | Medium à High |
| Génération de la documentation (étape 3) | Sonnet 5 | Low |
| Vérification finale et récapitulatif (étapes 4-6) | Sonnet 5 | Medium |

## 0. Nouvelle session : rappel Graphify

```
/graphify .
```

Cartographie **complète**, pas `--update` : c'est la photo finale du projet après six sprints de déplacements. Elle sert de référence pour la suite du projet.

## 1. Contexte

`ModularityTests` a été créé rouge en MM.1 et sert de compteur de dette depuis. Ce sprint le fait passer vert, puis transforme la structure obtenue en documentation exploitable par le reste de l'équipe.

Rappel de l'objectif posé en `PLAN_MONOLITHE_MODULAIRE.md` §2 — trois propriétés doivent être vraies :

1. Aucun service n'injecte un repository appartenant à un autre module.
2. Toute communication inter-modules passe par une API publique déclarée ou par un événement.
3. Un test automatisé échoue si l'une des deux règles ci-dessus est violée.

## 2. Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT :
1. Lis CLAUDE.md dans son integralite.
2. Lis docs/monolithe-modulaire/PLAN_MONOLITHE_MODULAIRE.md
3. Lis docs/monolithe-modulaire/MM.0_cadrage.md (decoupage acte)
4. Lance /graphify .  (cartographie COMPLETE, pas --update)

CONTEXTE : Sprint MM.6, cloture du chantier monolithe modulaire.
Objectif : ModularityTests vert, documentation generee, recapitulatif.

METHODE DE TRAVAIL :
- Aucun changement de comportement metier dans ce sprint.
- Si une violation restante exige un vrai refactoring, ARRETE-TOI :
  cela signifie qu'un sprint precedent est incomplet, et il vaut
  mieux y revenir que de la masquer par une declaration complaisante.

PREMIERE ACTION : etape 2, etat des violations.
```

## 3. Étape 2. Résorber les dernières violations

```
Lance ModularityTests et donne-moi l'etat exact.

Pour chaque violation restante, classe-la :

  (1) VRAIE violation -> un couplage a reellement ete oublie en
      MM.2/MM.3/MM.4. Montre-la-moi : on decide si on la corrige ici
      ou si on rouvre le sprint concerne.

  (2) Dependance LEGITIME non encore declaree -> ajoute la
      declaration @ApplicationModule(allowedDependencies = {...})
      correspondante, comme prevu en MM.5 etape 6.

Regle stricte : on ne declare comme legitime QUE ce qui a ete
explicitement arbitre. Declarer une dependance uniquement pour faire
passer le test au vert transformerait ModularityTests en decoration,
ce qui viderait de son sens tout le chantier.

Si le test etait @Disabled depuis MM.1 (voir MM.1 etape 3),
REACTIVE-LE maintenant et montre-moi qu'il passe reellement.
```

## 4. Étape 3. Générer la documentation d'architecture

```
Ajoute un test de generation de documentation, par exemple dans
backend/src/test/java/com/afriland/dottel/DocumentationTests.java :

  @Test
  void genereLaDocumentation() {
      new Documenter(ModularityTests.MODULES)
              .writeModulesAsPlantUml()
              .writeIndividualModulesAsPlantUml()
              .writeModuleCanvases();
  }

(Documenter vient de org.springframework.modulith.docs, fourni par
spring-modulith-starter-test deja ajoute en MM.1.)

Lance-le, puis :
1. Montre-moi ou les fichiers ont ete generes (par defaut sous
   target/spring-modulith-docs/).
2. Copie les diagrammes et les canvas produits vers
   docs/monolithe-modulaire/architecture/ pour qu'ils soient
   versionnes et consultables sans relancer le build.
3. Montre-moi le diagramme d'ensemble des modules : c'est le
   livrable le plus parlant du chantier.

Si les diagrammes revelent une dependance a laquelle personne
n'avait pense, signale-la -- une image rend visible ce qu'un test
booleen ne montre pas.
```

## 5. Étape 4. Vérification finale complète

```
Verification de bout en bout avant cloture.

BACKEND :
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
  cd backend ; .\mvnw.cmd test
Attendu : >= 205 tests, 0 echec, 0 erreur, BUILD SUCCESS.

FRONTEND (doit etre INCHANGE par ce chantier) :
  cd frontend
  npx oxlint
  npm run build
Attendu : build reussi. Le frontend n'a ete touche par AUCUN sprint
MM -- si git montre une modification sous frontend/, c'est une erreur
a signaler.

DEMARRAGE REEL :
  cd backend ; .\mvnw.cmd spring-boot:run
Attendu : "Started DottelApplication". Le repackaging a deplace des
@Component, @Service, @Repository et @Entity -- seul un demarrage
reel prouve que le scan de composants et le scan d'entites JPA
fonctionnent toujours.

TEST MANUEL MINIMAL (le backend etant demarre) :
  - POST /api/auth/login avec 1847/Test1234 -> 200, role ARH
  - GET /api/beneficiaires avec le token -> 200
  - GET /api/actuator/health -> UP
Ces trois appels traversent respectivement les modules utilisateurs,
beneficiaires et l'infrastructure. Montre-moi les reponses reelles.
```

**Pourquoi le démarrage réel est indispensable ici.** Un déplacement de packages peut casser le scan de composants Spring (`@ComponentScan` part du package de `DottelApplication`) ou le scan d'entités JPA sans qu'aucun test unitaire Mockito ne le remarque. `DottelApplicationTests` charge bien le contexte complet, mais un démarrage réel avec base et requêtes HTTP reste le seul contrôle de bout en bout.

## 6. Étape 5. Récapitulatif de fin de chantier

```
Produis un recapitulatif final, dans
docs/monolithe-modulaire/RECAPITULATIF_CHANTIER.md, contenant :

1. Tableau AVANT / APRES :
   | Indicateur | Avant (commit d9be38c) | Apres |
   - Packages : couches techniques / modules metier
   - Modules Spring Modulith declares : 0 / N
   - Tests d'architecture : 0 / 1 (+1 documentation)
   - Dependances de ProcessusMensuelService : 16 / N
   - Repositories etrangers injectes (tous services) : compte avant/apres
   - Implementations de la resolution de grille (RG-04) : 3 / 1
   - Services injectant AuditService : 8 / 0
   - Nombre de tests : 205 / N
   - Violations ModularityTests : mesure MM.1 / 0

2. Liste des decisions d'architecture actees pendant le chantier,
   avec leur sprint d'origine :
   - decoupage des modules (MM.0, questions A/B/C)
   - espace de travail et base (MM.0, question D)
   - mode transactionnel de l'audit (MM.4)
   - approche reporting R-1 ou R-2 (MM.3)
   - cascade de renommage B-1 ou B-2 (MM.3)

3. Liste des dependances inter-modules DECLAREES comme legitimes,
   avec leur justification.

4. Points laisses ouverts pour plus tard, s'il y en a.

Format : le meme que docs/audit_securite_owasp_v1.md, qui sert de
reference de style pour les rapports de ce projet.
```

## 7. Étape 6. Mettre à jour `CLAUDE.md`

```
CLAUDE.md section 3 decrit encore la structure en couches techniques
("controller/", "service/", "repository/", "model/entity/"...). Elle
est desormais FAUSSE.

Propose-moi une reecriture de cette section 3 refletant le decoupage
modulaire reel, et ajoute une regle a la section 18 (erreurs a ne
jamais commettre) du type :

  "Acceder au repository ou a l'entite d'un autre module au lieu de
   passer par son API publique -- ModularityTests echouera."

MONTRE-MOI la proposition, ne modifie pas CLAUDE.md sans mon accord :
c'est le document normatif du projet, pas un fichier de travail.
```

## 8. Critères de validation

| Élément | Statut attendu |
|---|---|
| `ModularityTests` **vert**, et réactivé s'il avait été `@Disabled` en MM.1 | Vérifié |
| Zéro violation non déclarée | Vérifié |
| Chaque dépendance déclarée légitime a une justification écrite | Fait |
| Documentation Spring Modulith générée (PlantUML + module canvases) | Fait |
| Diagrammes copiés sous `docs/monolithe-modulaire/architecture/` et versionnés | Fait |
| `mvn test` ≥ 205 tests, 0 échec, 0 erreur, `BUILD SUCCESS` | Vérifié |
| Frontend inchangé — aucun fichier modifié sous `frontend/` | Vérifié |
| Frontend : `oxlint` et `npm run build` toujours OK | Vérifié |
| Backend démarre réellement (`Started DottelApplication`) | Vérifié |
| Test manuel : login ARH, liste bénéficiaires, actuator health | Vérifié |
| `RECAPITULATIF_CHANTIER.md` produit avec le tableau avant/après | Fait |
| Réécriture de `CLAUDE.md` section 3 **proposée** (pas appliquée sans accord) | Fait |
| Aucun contrat API modifié sur les 34 endpoints | Vérifié |
| Aucune migration Flyway ajoutée sur l'ensemble du chantier | Vérifié |
| Graphe Graphify régénéré en complet | Fait |

## Commit

```bash
git add .
git commit -m "mm.6: modularite verifiee, documentation d'architecture generee, chantier clos"
```

---

**Fin du Sprint MM.6 — Fin du chantier monolithe modulaire**

*Le passage en microservices reste hors périmètre et exige une validation DSI explicite (CLAUDE.md sections 14 et 18). Voir `PLAN_MONOLITHE_MODULAIRE.md` §6.*