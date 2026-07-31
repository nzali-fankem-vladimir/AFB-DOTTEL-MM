# SPRINT MM.4

## Découpler l'audit par événements applicatifs

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Remplacer les appels directs à `AuditService` depuis 8 services par la publication d'événements applicatifs, et faire du module `audit` un simple écouteur |
| **Livrable** | Aucun service métier n'injecte plus `AuditService` ; le module `audit` écoute des événements |
| **Durée** | Une journée |
| **Prérequis** | MM.3 terminé, seules subsistent les violations de famille (c) |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Décision sur le mode transactionnel (étape 2) | Opus 4.8 ou Sonnet 5 | **High** |
| Conception des événements (étape 3) | Sonnet 5 | Medium |
| Migration service par service (étapes 4-11) | Sonnet 5 | Medium |
| Vérification RG-09 (étape 12) | Sonnet 5 | **High** |

L'effort High sur les étapes 2 et 12 est justifié par une raison précise : ce sprint peut, s'il est mal fait, **casser silencieusement RG-09** sans qu'aucun test ne le voie. Voir section 1.2.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

## 1. Contexte

### 1.1 État réel du couplage

`AuditService` (interface) et `AuditServiceImpl` sont appelés **en synchrone par 8 services** appartenant à 5 modules différents :

| Service appelant | Module d'appartenance (selon MM.0) |
|---|---|
| `AuthService` | `utilisateurs` |
| `UtilisateurAdminService` | `utilisateurs` |
| `EnrolementService` | `beneficiaires` |
| `BeneficiaireService` | `beneficiaires` |
| `BeneficiaireImportService` | `beneficiaires` |
| `FonctionEligibleService` | `referentiel` |
| `GrilleTarifaireService` | `referentiel` |
| `ProcessusMensuelService` | `processus` |

C'est le couplage le plus répandu du code, et c'est aussi le plus facile à découpler proprement : l'audit est par nature un **observateur**, il n'a rien à rendre à l'appelant.

`CLAUDE.md` section 9 impose : « `AuditService.enregistrer()` en fin de chaque méthode de service ». Cette exigence reste satisfaite après le refactor — c'est la *mécanique* qui change, pas la règle.

### 1.2 Deux pièges vérifiés dans le code, à traiter absolument

Lecture de `backend/src/main/java/com/afriland/dottel/service/AuditServiceImpl.java` :

**Piège n° 1 — la méthode `enregistrer()` n'a AUCUNE annotation `@Transactional`** (ligne 34-48). Elle rejoint donc la transaction de l'appelant, qui est lui `@Transactional`. Conséquence actuelle : **si l'action métier échoue et rollback, l'entrée d'audit disparaît avec elle.** C'est cohérent — on ne journalise pas une modification qui n'a jamais eu lieu.

Or `@ApplicationModuleListener` de Spring Modulith équivaut à `@Async` + `@TransactionalEventListener` + `@Transactional(propagation = REQUIRES_NEW)`. L'écouteur s'exécute donc **après le commit, dans une transaction séparée, sur un autre thread**. Le comportement change : plus de rollback conjoint. Est-ce acceptable pour RG-09 ? **C'est la question centrale de ce sprint.**

**Piège n° 2 — `extraireAdresseIpCourante()` lit `RequestContextHolder.currentRequestAttributes()`** (lignes 77-85). Ce contexte est **lié au thread de la requête HTTP**. Si l'écouteur devient asynchrone, il s'exécute sur un thread du pool, où `RequestContextHolder` est vide : la méthode tombera dans son `catch (IllegalStateException)` et retournera **`null`**.

Résultat : **`audit_log.adresse_ip` deviendrait `null` pour toutes les entrées**, silencieusement, sans qu'aucun test n'échoue. C'est exactement le type de régression que ce document existe pour empêcher.

## 2. Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT :
1. Lis CLAUDE.md dans son integralite, en particulier RG-09 (section
   7) et la section 9 ("AuditService.enregistrer() en fin de chaque
   methode de service", "passer le delta JSON pour toute modification").
2. Lis docs/monolithe-modulaire/PLAN_MONOLITHE_MODULAIRE.md
3. Lis docs/monolithe-modulaire/MM.4_audit_evenementiel.md en entier,
   SURTOUT la section 1.2 (deux pieges verifies).
4. Lance /graphify . --update

CONTEXTE : Sprint MM.4. Decoupler l'audit : les 8 services qui
appellent AuditService.enregistrer() doivent publier un evenement a
la place, et le module audit doit l'ecouter.

ATTENTION PARTICULIERE : ce sprint peut casser RG-09 SILENCIEUSEMENT
(voir section 1.2 du guide). Deux points sont a trancher AVEC MOI
avant toute ligne de code :
  - le mode transactionnel de l'ecouteur
  - la capture de l'adresse IP

METHODE DE TRAVAIL :
- Tu NE TRANCHES PAS ces deux points seul. Tu me presentes les
  options, j'arbitre, puis tu codes.
- Un service migre a la fois, mvn test complet apres chacun.
- Reference : >= 205 tests, 0 echec.

PREMIERE ACTION : etape 2, analyse des deux pieges. Ne code rien.
```

## 3. Étape 2. Trancher le mode transactionnel — **arrêt obligatoire**

```
Analyse les deux pieges de la section 1.2 du guide MM.4 et
presente-moi les options. NE TRANCHE PAS.

PIEGE 1 -- transactionnalite.
Aujourd'hui AuditServiceImpl.enregistrer() n'a pas de @Transactional :
elle rejoint la transaction de l'appelant, donc un rollback metier
annule aussi l'audit.

Options a me presenter, avec leurs consequences concretes sur RG-09 :

  T-1) Ecouteur SYNCHRONE dans la meme transaction
       (@EventListener simple, ou @TransactionalEventListener avec
       phase BEFORE_COMMIT). Comportement STRICTEMENT identique a
       aujourd'hui. Decouplage de compilation obtenu, mais pas de
       decouplage d'execution.

  T-2) @ApplicationModuleListener de Spring Modulith (asynchrone,
       apres commit, transaction separee). Decouplage maximal, mais
       CHANGEMENT DE COMPORTEMENT : une action metier commitee dont
       l'audit echoue ensuite laisserait une modification non tracee.
       Est-ce compatible avec RG-09, qui dit "TOUTE modification
       d'attribut metier DOIT etre tracee" ?

  T-3) T-2 + Event Publication Registry de Spring Modulith
       (spring-modulith-starter-jpa). Les evenements non traites sont
       persistes et rejoues, donc la garantie "rien ne se perd" est
       retablie -- mais cela AJOUTE UNE TABLE en base, ce qui sort du
       perimetre annonce du chantier (aucune migration Flyway prevue,
       voir PLAN section 5) et modifie le modele de donnees a 10
       tables fige par CLAUDE.md section 4.

Mon avis a titre indicatif : T-1 suffit pour l'objectif de ce
chantier (casser le couplage de compilation) sans toucher a une
garantie metier. Mais c'est TOI qui decides -- RG-09 est une regle
metier, pas un detail technique.

PIEGE 2 -- adresse IP.
Si l'ecouteur devient asynchrone (T-2 ou T-3),
extraireAdresseIpCourante() retournera null car RequestContextHolder
est lie au thread HTTP.

Option unique viable dans ce cas : capturer l'adresse IP AU MOMENT DE
LA PUBLICATION (cote appelant, thread HTTP) et la transporter DANS
l'evenement. Confirme-moi si tu vois une alternative.

Si l'option T-1 est retenue, ce piege disparait -- mais verifie-le
explicitement plutot que de le supposer.
```

**Ne pas passer à l'étape 3 tant que ces deux points ne sont pas tranchés par écrit.**

## 4. Étape 3. Concevoir les événements

```
Une fois le mode transactionnel acte, concois les evenements.

Deux approches possibles, propose-moi la tienne avec ses raisons :

  E-1) UN evenement generique unique
       (ex: EvenementAudit(idUtilisateur, action, entiteCible,
       idEntite, avant, apres, adresseIp)) -- transposition directe
       de la signature actuelle d'AuditService.enregistrer(), qui
       prend deja exactement ces 6 parametres. Migration triviale,
       mais l'evenement ne dit rien du metier.

  E-2) UN evenement PAR action metier
       (BeneficiaireModifie, GrilleValidee, ProcessusCloture...).
       Plus expressif, reutilisable par d'autres modules plus tard.
       Mais ~30 classes d'evenements a creer, pour un benefice qui
       n'existe pas encore aujourd'hui.

Contrainte a respecter dans les deux cas : RG-09 impose un delta JSON
avant/apres au format {"avant": {...}, "apres": {...}}. La
construction de ce delta doit rester dans le module audit
(construireDeltaJson, lignes 87-106 d'AuditServiceImpl), pas remonter
dans les modules metier.

Montre-moi les classes d'evenement AVANT de migrer le premier service.
```

## 5. Étapes 4 à 11. Migrer les 8 services

Ordre recommandé, du plus simple au plus complexe :

| Étape | Service | Nombre d'appels à `enregistrer()` | Remarque |
|---|---|---|---|
| 4 | `AuthService` | faible | Connexion/déconnexion, pas de delta métier |
| 5 | `UtilisateurAdminService` | moyen | Changements de rôle et de statut |
| 6 | `GrilleTarifaireService` | moyen | RG-10 |
| 7 | `FonctionEligibleService` | moyen | Attention : le delta du nombre de bénéficiaires impactés à la désactivation est tracé (contrat API §3bis) |
| 8 | `BeneficiaireImportService` | faible | Rapport d'import, RG-11 |
| 9 | `EnrolementService` | faible | RG-03 |
| 10 | `BeneficiaireService` | élevé | RG-09 au sens strict : delta avant/après sur modification d'attribut |
| 11 | `ProcessusMensuelService` | le plus élevé | Toutes les transitions de workflow |

```
Migre maintenant <SERVICE>, et LUI SEUL.

1. Remplace chaque appel a auditService.enregistrer(...) par la
   publication de l'evenement, via ApplicationEventPublisher.
2. Retire AuditService des dependances injectees de ce service.
3. Lance la suite complete :
     $env:DB_PASSWORD="admin"
     $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
     cd backend ; .\mvnw.cmd test
4. Montre-moi le diff et le resultat.

ATTENTION AUX TESTS EXISTANTS : plusieurs tests verifient aujourd'hui
l'appel a l'audit via un mock Mockito (par exemple
BeneficiaireServiceTest contient un test nomme
appelleAuditAvecDelta()). Ces tests devront verifier la PUBLICATION
DE L'EVENEMENT au lieu de l'appel au service.

C'est une adaptation legitime -- mais montre-moi chaque test modifie.
Un test d'audit qu'on affaiblit au passage (par exemple en ne
verifiant plus le contenu du delta) est une perte de couverture
deguisee, pas une adaptation.
```

## 6. Étape 12. Vérifier RG-09 de bout en bout

```
Verification finale de RG-09. Ce n'est pas une formalite : c'est la
regle que ce sprint risque le plus de casser.

1. Lance le backend en local et effectue reellement, via l'API :
   - une modification de beneficiaire (PATCH /beneficiaires/{id})
   - une validation de grille tarifaire (RG-10)
   - une transition de workflow

2. Pour chacune, verifie EN BASE dans audit_log :
   - la ligne existe bien
   - detail_json contient le delta au format
     {"avant": {...}, "apres": {...}}
   - adresse_ip est RENSEIGNEE et non null   <-- piege n 2
   - id_utilisateur, action, entite_cible, id_entite sont corrects

3. Teste le cas de rollback : provoque une erreur metier (par exemple
   un 409 sur un doublon) et verifie qu'AUCUNE ligne d'audit parasite
   n'a ete creee pour une action qui n'a pas eu lieu.

Montre-moi les resultats reels (contenu des lignes), pas une
conclusion. Si adresse_ip est null quelque part, on a reproduit le
piege n 2 et il faut corriger avant de committer.
```

## 7. Étape 13. Mesurer

```
Relance ModularityTests. A ce stade, TOUTES les violations de famille
(c) doivent avoir disparu.

Donne-moi le compte final :
| Famille | MM.1 | MM.2 | MM.3 | MM.4 |

S'il reste des violations, liste-les precisement : elles devront etre
soit traitees, soit declarees explicitement en MM.5.
```

## 8. Critères de validation

| Élément | Statut attendu |
|---|---|
| Mode transactionnel (T-1 / T-2 / T-3) tranché **avec l'utilisateur**, par écrit | Fait |
| Traitement de l'adresse IP tranché **avec l'utilisateur**, par écrit | Fait |
| Aucun des 8 services métier n'injecte plus `AuditService` | Vérifié |
| Le module `audit` écoute des événements | Vérifié |
| Construction du delta JSON restée dans le module `audit` | Vérifié |
| RG-09 — `detail_json` au format `{"avant": …, "apres": …}` sur une modification réelle | Vérifié en base |
| RG-09 — `adresse_ip` **non nulle** sur une action réelle via HTTP | Vérifié en base |
| Aucune ligne d'audit créée pour une action métier ayant échoué | Vérifié en base |
| Tests d'audit existants adaptés sans perte de couverture (delta toujours vérifié) | Vérifié |
| Suite complète ≥ 205 tests, 0 échec, 0 erreur | Vérifié |
| Aucune migration Flyway ajoutée (sauf si T-3 explicitement retenue) | Vérifié |
| Violations `ModularityTests` de famille (c) résorbées | Vérifié |

## Commit

```bash
git add .
git commit -m "mm.4: audit decouple par evenements applicatifs, module audit en ecouteur"
```

---

**Fin du Sprint MM.4** — *en attente de validation avant MM.5*