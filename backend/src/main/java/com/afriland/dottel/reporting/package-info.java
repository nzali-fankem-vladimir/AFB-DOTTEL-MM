/**
 * Module reporting (chantier MM, Sprint MM.3, couplage C5 -- description
 * ajoutee en MM.5).
 *
 * Tableaux de bord et rapports agreges en lecture seule sur les processus
 * mensuels et le journal d'audit -- aucune entite ni repository en propre,
 * par nature (voir C.2 de MM.0).
 *
 * EXCEPTION ARCHITECTURALE ASSUMEE (decision R-2, actee avec l'utilisateur) :
 * ReportingService lit directement 3 repositories d'autres modules
 * (BeneficiaireRepository, ProcessusMensuelRepository, LigneEtatMensuelRepository)
 * plutot que de passer par des projections dediees exposees par chaque module
 * proprietaire (option R-1, ecartee). C'est le couplage le plus structurel du
 * chantier : un module de reporting a, par nature, besoin de donnees qu'il ne
 * possede pas.
 *
 * Condition de securite verifiee avant d'acter ce choix : ReportingService et
 * HistoriqueExportService ne font QUE de la lecture agregee sur ces trois
 * repositories (countByActifTrue, sumMontantAppliqueByIdProcessus, et diverses
 * requetes findBy/countBy) -- aucun save() ni delete() sur les trois, et les
 * deux methodes publiques de ReportingService sont deja annotees en lecture
 * seule (readOnly = true). Sans mutation, l'exception ne peut pas faire de
 * reporting un point d'ecriture cache dans un module etranger.
 *
 * "audit" reste dans la liste : ReportingController lit AuditService.rechercher()
 * pour GET /reporting/audit (lecture du journal), sans rapport avec les appels
 * enregistrer() traites en MM.4 (famille de violation (c)).
 *
 * Cette declaration rend l'exception EXPLICITE plutot que de la laisser
 * invisible dans le code : si reporting tente un jour d'acceder a un autre
 * module non liste ici, ModularityTests le signalera comme une nouvelle
 * violation, pas comme une extension silencieuse de cette exception.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "beneficiaires :: repository",
                "processus :: repository",
                "processus :: entity",
                "processus :: enums",
                "audit :: api"
        }
)
package com.afriland.dottel.reporting;
