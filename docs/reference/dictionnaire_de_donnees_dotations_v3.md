**DICTIONNAIRE DE DONNÉES**

Module : Digitalisation des Dotations Téléphoniques Mensuelles

_Projet AFRILAND HORIZON 2030_

| **Référence** | **AFB_DD_DOTTEL_V3.0_2026** |
| --- | --- |
| Version | 3.0 |
| --- | --- |
| Date | Juillet 2026 |
| --- | --- |
| Périmètre | Module INTRA — Dotations Téléphoniques (hors comptabilité CBS) |
| --- | --- |
| Tables couvertes | 10 tables + 5 énumérations |
| --- | --- |

# **0\. Introduction et conventions**

Le dictionnaire de données est le référentiel technique de toutes les données manipulées par le module de gestion des dotations téléphoniques mensuelles. Il décrit chaque champ de chaque table avec son type SQL, sa contrainte de nullité et sa définition fonctionnelle.

Version 3.0 : ajout de la table grille_tarifaire, suppression de montant_dotation dans beneficiaires, ajout du rôle ADMIN dans RoleEnum, ajout de StatutGrilleEnum, 25 fonctions éligibles au lieu de 6 (22 postes de la note de service NS 69/17 + 3 corps assimilés : CONTROLEUR_GESTION, CONTROLEUR_COMPTABLE, COMPTABLE).

| **Légende** | **Signification** |
| --- | --- |
| PK  | Clé primaire |
| --- | --- |
| FK  | Clé étrangère |
| --- | --- |
| UQ  | Contrainte d'unicité |
| --- | --- |
| NN  | Non null |
| --- | --- |
| N   | Nullable |
| --- | --- |

## **1\. Table : utilisateurs**

Stocke tous les acteurs du système. Le champ role détermine les droits dans le module. Version 3.0 : ajout de la valeur ADMIN dans RoleEnum.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| matricule | VARCHAR(20) UQ | NN  | Matricule professionnel unique. Identifiant métier principal. |
| --- | --- | --- | --- |
| nom | VARCHAR(100) | NN  | Nom de famille tel qu'enregistré dans l'EHR. |
| --- | --- | --- | --- |
| prenom | VARCHAR(100) | NN  | Prénom(s) de l'utilisateur. |
| --- | --- | --- | --- |
| email | VARCHAR(150) UQ | N   | Adresse email professionnelle. Utilisée pour les notifications. |
| --- | --- | --- | --- |
| role | VARCHAR(30) | NN  | Rôle dans le module. Valeurs : EMPLOYE, ARH, CRH, DRH, ADMIN. |
| --- | --- | --- | --- |
| mot_de_passe_hash | VARCHAR(255) | NN  | Mot de passe chiffré avec BCrypt. Jamais en clair. |
| --- | --- | --- | --- |
| actif | BOOLEAN | NN  | Compte actif (true) ou désactivé (false). Défaut : true. |
| --- | --- | --- | --- |
| date_creation | TIMESTAMP | NN  | Date de création du compte. |
| --- | --- | --- | --- |
| id_beneficiaire | BIGINT FK | N   | Référence vers beneficiaires.id. Renseignée si l'utilisateur est enrôlé. |
| --- | --- | --- | --- |

## **2\. Table : beneficiaires**

Employés enrôlés dans le module. Version 3.0 : suppression de montant_dotation — le montant est calculé à la volée depuis grille_tarifaire (RG-04). La colonne fonction référence désormais fonction_eligible.code.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| matricule | VARCHAR(20) UQ | NN  | Matricule de l'employé. Unicité garantie (RG-03). |
| --- | --- | --- | --- |
| nom_prenoms | VARCHAR(200) | NN  | Nom et prénoms tels que retournés par l'EHR. |
| --- | --- | --- | --- |
| fonction | VARCHAR(50) FK | NN  | Code de la fonction. Référence fonction_eligible.code. |
| --- | --- | --- | --- |
| grade | VARCHAR(100) | N   | Grade de l'employé. Obligatoire pour les Corps de Contrôle (RG-02). |
| --- | --- | --- | --- |
| unite_rattachement | VARCHAR(150) | NN  | Unité administrative de rattachement. |
| --- | --- | --- | --- |
| code_unite | VARCHAR(20) | NN  | Code comptable de l'unité. Utilisé pour les écritures. |
| --- | --- | --- | --- |
| num_compte_courant | VARCHAR(30) | NN  | Numéro de compte courant pour le crédit des dotations. |
| --- | --- | --- | --- |
| date_enrolement | DATE | NN  | Date d'enrôlement dans le module. |
| --- | --- | --- | --- |
| actif | BOOLEAN | NN  | Bénéficiaire actif. Défaut : true. |
| --- | --- | --- | --- |

## **3\. Table : fonction_eligible**

Référentiel stable des 24 postes éligibles à la dotation. Version 3.0 : cette table remplace FonctionEnum codée en dur. Les montants sont dans grille_tarifaire. L'ADMIN peut activer ou désactiver une fonction sans toucher au code.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| code | VARCHAR(50) UQ | NN  | Code unique du poste. Ex : GFC, DIRECTEUR, CORPS_CONTROLE_IG. |
| --- | --- | --- | --- |
| libelle | VARCHAR(150) | NN  | Intitulé complet du poste. Ex : Gestionnaire de Fonds de Commerce. |
| --- | --- | --- | --- |
| actif | BOOLEAN | NN  | Poste éligible (true) ou suspendu (false). Défaut : true. |
| --- | --- | --- | --- |

Les 24 postes insérés par Flyway V2 (tous actifs par défaut) :

| **Code** | **Libellé** | **Note** |
| --- | --- | --- |
| ADG | Administrateur Directeur Général |     |
| --- | --- | --- |
| DGA | Directeur Général Adjoint |     |
| --- | --- | --- |
| CORPS_CONTROLE_IG | Inspecteur Général | Soumis à RG-02 |
| --- | --- | --- |
| DIRECTEUR_GROUPE | Directeur Groupe |     |
| --- | --- | --- |
| CORPS_CONTROLE_IGA | Inspecteur Général Adjoint | Soumis à RG-02 |
| --- | --- | --- |
| DIRECTEUR | Directeur Central/Succursale/Régional |     |
| --- | --- | --- |
| DCA | Directeur Central Adjoint |     |
| --- | --- | --- |
| DA  | Directeur d'Agence |     |
| --- | --- | --- |
| CONSEILLER | Conseiller/Chargé de Mission |     |
| --- | --- | --- |
| DA_ADJOINT | Directeur d'Agence Adjoint |     |
| --- | --- | --- |
| CHEF_DEPARTEMENT | Chef de Département |     |
| --- | --- | --- |
| CHEF_ANTENNE | Chef d'Antenne |     |
| --- | --- | --- |
| GFC | Gestionnaire de Fonds de Commerce |     |
| --- | --- | --- |
| ASSISTANTE_PCA | Assistante du PCA |     |
| --- | --- | --- |
| ASSISTANTE_ADG | Assistante de l'ADG |     |
| --- | --- | --- |
| ASSISTANTE_DGA | Assistante du DGA |     |
| --- | --- | --- |
| CHEF_DIVISION | Chef de Division |     |
| --- | --- | --- |
| CHEF_PRODUIT | Chef de Produit |     |
| --- | --- | --- |
| COORDONNATEUR | Coordonnateur |     |
| --- | --- | --- |
| JURISTE | Agent de Recouvrement |     |
| --- | --- | --- |
| CONTROLEUR_GESTION | Contrôleur de Gestion | Soumis à RG-02 — ajout métier |
| --- | --- | --- |
| CONTROLEUR_COMPTABLE | Contrôleur Comptable | Soumis à RG-02 — ajout métier |
| --- | --- | --- |
| COMPTABLE | Comptable | Soumis à RG-02 — ajout métier |
| --- | --- | --- |
| ASSISTANT_CHEF_PROD | Assistant Chef de Produit |     |
| --- | --- | --- |
| ATTACHE_COMMERCIAL | Attaché Commercial |     |
| --- | --- | --- |

## **4\. Table : grille_tarifaire**

Historique des montants de dotation par fonction, avec workflow de validation DRH. Version 3.0 : table nouvelle, remplace la colonne montant_dotation de beneficiaires et les codes MONTANT_\* de regle_eligibilite. id_createur et id_validateur sont nullable pour les grilles initiales insérées par migration Flyway.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| id_fonction_eligible | BIGINT FK | NN  | Référence vers fonction_eligible.id. |
| --- | --- | --- | --- |
| montant_fcfa | INTEGER | NN  | Montant de la dotation en FCFA pour cette grille. |
| --- | --- | --- | --- |
| date_debut | DATE | NN  | Date de début d'application de cette grille. |
| --- | --- | --- | --- |
| date_fin | DATE | N   | Date de fin. NULL si grille courante. |
| --- | --- | --- | --- |
| statut_validation | VARCHAR(20) | NN  | Statut du workflow. Valeurs : BROUILLON, EN_ATTENTE_DRH, ACTIVE, REJETEE. |
| --- | --- | --- | --- |
| id_createur | BIGINT FK | N   | Utilisateur qui a créé la grille. NULL pour les grilles initiales Flyway. |
| --- | --- | --- | --- |
| id_validateur | BIGINT FK | N   | Utilisateur DRH qui a validé. NULL si non encore validé. |
| --- | --- | --- | --- |
| date_creation | TIMESTAMP | NN  | Date de création de la grille. |
| --- | --- | --- | --- |
| date_validation | TIMESTAMP | N   | Date de validation ou de rejet par la DRH. |
| --- | --- | --- | --- |
| motif_rejet | TEXT | N   | Motif de rejet si statut_validation = REJETEE. |
| --- | --- | --- | --- |

Contrainte d'unicité partielle (index partiel PostgreSQL) : une seule grille ACTIVE avec date_fin IS NULL par fonction à un instant donné.

CREATE UNIQUE INDEX idx_grille_active_unique  
ON grille_tarifaire(id_fonction_eligible)  
WHERE statut_validation = 'ACTIVE' AND date_fin IS NULL;

## **5\. Table : ligne_etat_mensuel**

Table pivot N-N entre processus_mensuel et beneficiaires. Porte le montant et la fonction retenus pour un mois donné. Ces valeurs peuvent différer de celles du bénéficiaire si l'ARH ajuste l'état avant validation.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| id_processus | BIGINT FK | NN  | Référence vers processus_mensuel.id. |
| --- | --- | --- | --- |
| id_beneficiaire | BIGINT FK | NN  | Référence vers beneficiaires.id. |
| --- | --- | --- | --- |
| montant_applique | INTEGER | NN  | Montant retenu pour ce bénéficiaire et ce mois. |
| --- | --- | --- | --- |
| inclus_dans_etat | BOOLEAN | NN  | True si le bénéficiaire est inclus dans l'état du mois. |
| --- | --- | --- | --- |
| fonction_retenue | VARCHAR(50) | NN  | Code de la fonction retenue au moment du calcul. |
| --- | --- | --- | --- |
| UNIQUE | (id_processus, id_beneficiaire) | —   | Un bénéficiaire ne peut figurer qu'une fois par processus. |
| --- | --- | --- | --- |

## **6\. Table : processus_mensuel**

Représente un cycle de paiement pour un mois et une année donnés. Un seul processus par période.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| mois_paiement | INTEGER | NN  | Mois du cycle (1 à 12). |
| --- | --- | --- | --- |
| annee_paiement | INTEGER | NN  | Année du cycle. |
| --- | --- | --- | --- |
| statut | VARCHAR(30) | NN  | Statut du workflow. Valeurs : voir StatutEnum. |
| --- | --- | --- | --- |
| date_creation | TIMESTAMP | NN  | Date de déclenchement du processus par l'ARH. |
| --- | --- | --- | --- |
| date_cloture | TIMESTAMP | N   | Date de clôture. NULL si non encore clôturé. |
| --- | --- | --- | --- |
| id_createur | BIGINT FK | NN  | ARH qui a déclenché le processus. |
| --- | --- | --- | --- |
| UNIQUE | (mois_paiement, annee_paiement) | —   | Un seul processus par mois/année. |
| --- | --- | --- | --- |

## **7\. Table : etape_workflow**

Enregistre chaque étape de validation du processus mensuel. Un processus peut avoir plus de 3 étapes en cas de retour.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| id_processus | BIGINT FK | NN  | Référence vers processus_mensuel.id. |
| --- | --- | --- | --- |
| id_acteur | BIGINT FK | NN  | Utilisateur ayant effectué l'action. |
| --- | --- | --- | --- |
| ordre_etape | INTEGER | NN  | Numéro d'ordre de l'étape dans le processus. |
| --- | --- | --- | --- |
| nom_etape | VARCHAR(30) | NN  | Nom de l'étape. Valeurs : VALIDATION_ARH, VALIDATION_CRH, VALIDATION_DRH. |
| --- | --- | --- | --- |
| statut_etape | VARCHAR(20) | NN  | Statut. Valeurs : EN_ATTENTE, VALIDEE, RETOURNEE. |
| --- | --- | --- | --- |
| date_action | TIMESTAMP | N   | Date de l'action effectuée. |
| --- | --- | --- | --- |
| motif_retour | TEXT | N   | Motif obligatoire si statut_etape = RETOURNEE (RG-07). |
| --- | --- | --- | --- |
| signature_numerique | TEXT | N   | Signature numérique de l'acteur apposée sur le PDF. |
| --- | --- | --- | --- |

## **8\. Table : piece_jointe**

Un seul document PDF par processus, enrichi progressivement des signatures des trois validateurs. La contrainte UNIQUE garantit l'unicité.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| id_processus | BIGINT FK UQ | NN  | Référence vers processus_mensuel.id. Contrainte UNIQUE. |
| --- | --- | --- | --- |
| nom_fichier | VARCHAR(255) | NN  | Nom du fichier PDF généré. |
| --- | --- | --- | --- |
| chemin_stockage | VARCHAR(500) | NN  | Chemin complet sur le système de fichiers. |
| --- | --- | --- | --- |
| date_generation_initiale | TIMESTAMP | NN  | Date de génération par l'ARH. |
| --- | --- | --- | --- |
| date_derniere_mise_a_jour | TIMESTAMP | NN  | Date du dernier ajout de signature. |
| --- | --- | --- | --- |
| nombre_signatures | INTEGER | NN  | Nombre de signatures apposées (1 ARH, puis 2 et 3 après CRH et DRH). |
| --- | --- | --- | --- |

## **9\. Table : regle_eligibilite**

Paramètres de règles métier autres que les fonctions et les montants. Ex : seuils, durées, codes de référence. Ne pas supprimer cette table.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| code_regle | VARCHAR(50) UQ | NN  | Code unique de la règle. Ex : SEUIL_VALIDATION_CRH. |
| --- | --- | --- | --- |
| description | VARCHAR(255) | NN  | Description fonctionnelle de la règle. |
| --- | --- | --- | --- |
| type_regle | VARCHAR(30) | NN  | Type : SEUIL, LISTE, DUREE, FLAG. |
| --- | --- | --- | --- |
| valeur_condition | VARCHAR(100) | N   | Valeur de condition si applicable. |
| --- | --- | --- | --- |
| valeur_reponse | VARCHAR(500) | NN  | Valeur de réponse ou paramètre de la règle. |
| --- | --- | --- | --- |
| active | BOOLEAN | NN  | Règle active ou non. Défaut : true. |
| --- | --- | --- | --- |

## **10\. Table : audit_log**

Journal complet et immuable de toutes les actions significatives dans le système (RG-09). Le champ detail_json stocke un delta JSON avant/après pour les modifications.

| **Champ** | **Type SQL** | **Null ?** | **Description** |
| --- | --- | --- | --- |
| id  | BIGINT NN PK | NN  | Identifiant technique auto-incrémenté |
| --- | --- | --- | --- |
| id_utilisateur | BIGINT FK | NN  | Utilisateur ayant effectué l'action. |
| --- | --- | --- | --- |
| action | VARCHAR(100) | NN  | Action effectuée. Ex : ENROLEMENT, VALIDATION_ARH, MODIFICATION_GRILLE. |
| --- | --- | --- | --- |
| entite_cible | VARCHAR(50) | NN  | Table ou entité ciblée. Ex : beneficiaires, grille_tarifaire. |
| --- | --- | --- | --- |
| id_entite | BIGINT | N   | Identifiant de la ligne ciblée. |
| --- | --- | --- | --- |
| date_action | TIMESTAMP | NN  | Date et heure de l'action. |
| --- | --- | --- | --- |
| adresse_ip | VARCHAR(45) | N   | Adresse IP de l'utilisateur. |
| --- | --- | --- | --- |
| detail_json | TEXT | N   | Delta JSON {avant:{...}, apres:{...}} pour les modifications d'attributs métier. |
| --- | --- | --- | --- |

# **11\. Énumérations (5)**

| **Énumération** | **Valeurs** |
| --- | --- |
| StatutEnum | EN_COURS_ARH \| EN_ATTENTE_CRH \| EN_ATTENTE_DRH \| RETOURNE \| CLOTURE |
| --- | --- |
| RoleEnum | EMPLOYE \| ARH \| CRH \| DRH \| ADMIN (v3.0 : ajout ADMIN) |
| --- | --- |
| NomEtapeEnum | VALIDATION_ARH \| VALIDATION_CRH \| VALIDATION_DRH |
| --- | --- |
| StatutEtapeEnum | EN_ATTENTE \| VALIDEE \| RETOURNEE |
| --- | --- |
| StatutGrilleEnum | BROUILLON \| EN_ATTENTE_DRH \| ACTIVE \| REJETEE (v3.0 : nouvelle) |
| --- | --- |

**FonctionEnum n'existe plus dans ce projet. Les fonctions sont dans la table fonction_eligible.**

**TypeDocumentEnum n'existe pas dans ce projet.**