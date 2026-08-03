package com.afriland.dottel.referentiel.service;

import com.afriland.dottel.referentiel.api.GrilleTarifaireApi;
import com.afriland.dottel.referentiel.api.ResolutionGrilleDto;
import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import com.afriland.dottel.referentiel.repository.GrilleTarifaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
class GrilleTarifaireApiImpl implements GrilleTarifaireApi {

    private final FonctionEligibleRepository fonctionEligibleRepository;
    private final GrilleTarifaireRepository grilleTarifaireRepository;

    // Corps repris a l'identique de ProcessusMensuelService.resoudreGrillePourFonction()
    // avant MM.2 : meme ordre de verification, memes motifs, meme requete.
    // Seule difference : le retour est un DTO portant le montant, plus un record
    // transportant l'entite GrilleTarifaire.
    @Override
    public ResolutionGrilleDto resoudrePourFonction(String codeFonction) {
        Optional<FonctionEligible> fonctionEligible = fonctionEligibleRepository.findByCode(codeFonction);

        if (fonctionEligible.isEmpty()) {
            return ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_FONCTION_INCONNUE);
        }
        if (!fonctionEligible.get().isActif()) {
            return ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_FONCTION_DESACTIVEE);
        }

        Optional<GrilleTarifaire> grilleActive = grilleTarifaireRepository
                .findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                        fonctionEligible.get().getId(), StatutGrilleEnum.ACTIVE);

        return grilleActive
                .map(grille -> ResolutionGrilleDto.resolue(grille.getMontantFcfa()))
                .orElseGet(() -> ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_GRILLE_INTROUVABLE));
    }
}
