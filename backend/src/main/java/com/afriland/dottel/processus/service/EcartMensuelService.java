package com.afriland.dottel.processus.service;

import com.afriland.dottel.processus.model.entity.LigneEtatMensuel;
import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.processus.repository.LigneEtatMensuelRepository;
import com.afriland.dottel.processus.repository.ProcessusMensuelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EcartMensuelService {

    private final ProcessusMensuelRepository processusMensuelRepository;
    private final LigneEtatMensuelRepository ligneEtatMensuelRepository;

    // Confirme avec le metier : l'ecart n'est pas un delta de montant generique,
    // il ne s'affiche que si la fonction retenue du beneficiaire a change entre
    // le mois precedent et le mois courant. Retourne null quand aucun ecart ne
    // doit etre affiche (pas de processus precedent, beneficiaire absent du mois
    // precedent, ou fonction inchangee) -- colonne vide dans le PDF.
    // Conservee telle quelle (signature et comportement inchanges, deja testee) ;
    // delegue desormais a rechercherResultatMensuel() pour eviter de dupliquer
    // la recherche du processus/ligne precedents.
    public Integer calculerEcart(ProcessusMensuel processusCourant, LigneEtatMensuel ligneCourante) {
        return rechercherResultatMensuel(processusCourant, ligneCourante).ecart();
    }

    // Recherche en un seul appel le montant du mois precedent (colonne M-1 du
    // PDF) et l'ecart (colonne ECART) : evite a DocumentService de chercher la
    // ligne precedente puis de rappeler ce service une seconde fois pour l'ecart.
    public ResultatEcartMensuel rechercherResultatMensuel(ProcessusMensuel processusCourant, LigneEtatMensuel ligneCourante) {
        Optional<LigneEtatMensuel> lignePrecedente = rechercherLignePrecedente(processusCourant, ligneCourante);

        if (lignePrecedente.isEmpty()) {
            return new ResultatEcartMensuel(null, null);
        }

        Integer montantMoisPrecedent = lignePrecedente.get().getMontantApplique();
        boolean fonctionInchangee = lignePrecedente.get().getFonctionRetenue().equals(ligneCourante.getFonctionRetenue());
        Integer ecart = fonctionInchangee ? null : ligneCourante.getMontantApplique() - montantMoisPrecedent;

        return new ResultatEcartMensuel(montantMoisPrecedent, ecart);
    }

    private Optional<LigneEtatMensuel> rechercherLignePrecedente(ProcessusMensuel processusCourant, LigneEtatMensuel ligneCourante) {
        int moisPrecedent = processusCourant.getMoisPaiement() - 1;
        int anneePrecedente = processusCourant.getAnneePaiement();
        if (moisPrecedent == 0) {
            moisPrecedent = 12;
            anneePrecedente = anneePrecedente - 1;
        }

        // Sprint MM.11 : compare toujours contre le processus NORMAL du mois
        // precedent -- un rattrapage n'entre jamais dans la comparaison M-1
        // du PDF, qui reflete le cycle de paiement standard.
        Optional<ProcessusMensuel> processusPrecedent = processusMensuelRepository
                .findByMoisPaiementAndAnneePaiementAndRattrapageFalse(moisPrecedent, anneePrecedente);
        if (processusPrecedent.isEmpty()) {
            return Optional.empty();
        }

        return ligneEtatMensuelRepository
                .findByIdProcessusAndIdBeneficiaire(processusPrecedent.get().getId(), ligneCourante.getIdBeneficiaire());
    }

    public record ResultatEcartMensuel(Integer montantMoisPrecedent, Integer ecart) {
    }
}