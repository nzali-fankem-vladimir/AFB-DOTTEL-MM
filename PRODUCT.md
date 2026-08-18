# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Agents internes d'Afriland First Bank, répartis en 5 rôles : EMPLOYE,
ARH, CRH, DRH, ADMIN. Usage quotidien à mensuel, sur poste de travail
bancaire. Ce ne sont pas des utilisateurs grand public : ils reviennent,
ils connaissent le domaine métier des dotations téléphoniques, et ils
veulent aller vite plutôt que d'être guidés.

## Product Purpose

DOTTEL digitalise le paiement des dotations téléphoniques mensuelles
d'Afriland First Bank, en remplaçant un circuit papier manuel par un
workflow électronique multi-niveaux. Le parcours central est un
workflow à 3 signatures (ARH puis CRH puis DRH) sur un état mensuel de
dotations. Le succès se mesure à la clôture sans erreur d'un processus
mensuel, dans les délais, avec une traçabilité complète.

## Positioning

Outil métier interne de validation multi-niveaux, intégré à la
plateforme BAOBAB (SSO Keycloak partagé). Il ne s'agit pas d'un produit
grand public ni d'une vitrine : c'est un composant d'un système
d'information bancaire existant, dont la valeur repose sur la
fiabilité du circuit de validation et non sur l'attrait visuel.

## Operating Context

Cycle mensuel : enrôlement des bénéficiaires, déclenchement du
processus, ajustement de l'état mensuel par l'ARH, validation
séquentielle ARH → CRH → DRH, génération et signature progressive d'un
document unique, clôture avec publication d'un événement consommé par
le module comptable. Gestion parallèle des grilles tarifaires
(création ARH, validation DRH) et de l'administration des comptes
utilisateurs.

## Capabilities and Constraints

- Les écrans engagent des paiements réels : une ambiguïté sur un
  montant ou un statut a un coût financier direct.
- Séparation stricte des tâches (RG-08) : un même acteur ne peut pas
  valider deux étapes consécutives d'un même processus.
- Un seul document PDF par processus, enrichi de signatures
  successives (jamais trois documents distincts).
- Terminologie métier à respecter dans l'interface : ARH, CRH, DRH,
  grille tarifaire, état mensuel, ligne d'état mensuel, fonction
  éligible, rattrapage.
- Aucun test frontend automatisé (pas de suite Vitest/Jest) : la
  validation reste visuelle, faite dans le navigateur.

## Brand Commitments

Identité visuelle Afriland First Bank imposée et non négociable, voir
DESIGN.md : rouge institutionnel #E30613, navigation latérale verticale
à fond sombre, logo AFB en haut de la navigation, filigrane discret sur
le contenu uniquement. Voix : sobre, factuelle, en français,
vouvoiement. Aucun ton promotionnel, aucune familiarité.

## Evidence on Hand

Aucun contenu marketing, aucun témoignage, aucun cas d'usage à
inventer : DOTTEL n'a pas de vitrine commerciale. Les seules données
réelles sont les 29 écrans applicatifs existants, les tokens de
couleur déjà définis dans frontend/src/index.css, et les 15 composants
UI déjà en place. Ne jamais fabriquer de contenu promotionnel ou de
preuve sociale.

## Product Principles

1. La clarté prime sur l'esthétique : un montant ou un statut ne doit
   jamais être ambigu.
2. L'outil sert des utilisateurs récurrents et experts du domaine — la
   densité d'information et la rapidité d'exécution priment sur la
   découvrabilité pour un nouvel utilisateur.
3. Chaque amélioration visuelle doit rester dans le périmètre du soin
   et de la finition, jamais dans celui de la refonte ou de l'ajout de
   fonctionnalité.
4. La charte graphique de la banque est une contrainte fixe, pas un
   point de départ à interpréter.

## Accessibility & Inclusion

Application entièrement en français ; les lecteurs d'écran doivent
recevoir la langue correcte (`lang="fr"`). Contrastes et focus visibles
attendus dans le cadre du chantier de finition (voir plan D.1-D.4).
Aucune exigence d'accessibilité spécifique au-delà des standards
usuels n'a été formulée par le métier à ce jour.
