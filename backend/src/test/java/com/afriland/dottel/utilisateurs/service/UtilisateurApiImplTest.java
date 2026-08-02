package com.afriland.dottel.utilisateurs.service;

import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import com.afriland.dottel.utilisateurs.exception.UtilisateurIntrouvableException;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Couverture de l'API du module utilisateurs (Sprint MM.2).
 *
 * Le point sensible est destinataireParId() : il porte le 404 leve lorsqu'un ARH
 * createur n'existe plus, assertion qui vivait auparavant dans
 * ProcessusMensuelService lui-meme.
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurApiImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UtilisateurApiImpl utilisateurApi;

    @Test
    void destinatairesParRole_neRemonteQueLEmailEtLeRole() {
        Utilisateur crh = Utilisateur.builder()
                .id(20L).matricule("3001").nom("ATANGANA").prenom("Paul")
                .email("p.atangana@afrilandfirstbank.cm").role(RoleEnum.CRH).actif(true).build();

        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.CRH)).thenReturn(List.of(crh));

        assertThat(utilisateurApi.destinatairesParRole(RoleEnum.CRH)).containsExactly(
                new DestinataireNotificationDto("p.atangana@afrilandfirstbank.cm", RoleEnum.CRH));
    }

    // Aucun CRH actif n'est un cas legitime : la validation ARH reste valide, seule
    // la notification n'a pas de destinataire. Jamais d'exception ici.
    @Test
    void destinatairesParRole_aucunUtilisateurActif_retourneUneListeVide() {
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.DRH)).thenReturn(List.of());

        assertThat(utilisateurApi.destinatairesParRole(RoleEnum.DRH)).isEmpty();
    }

    @Test
    void destinataireParId_casNominal_retourneLeDestinataire() {
        Utilisateur arh = Utilisateur.builder()
                .id(10L).matricule("1847").nom("MBARGA").prenom("Jean-Paul")
                .email("jp.mbarga@afrilandfirstbank.cm").role(RoleEnum.ARH).actif(true).build();

        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(arh));

        assertThat(utilisateurApi.destinataireParId(10L)).isEqualTo(
                new DestinataireNotificationDto("jp.mbarga@afrilandfirstbank.cm", RoleEnum.ARH));
    }

    // Contrairement a destinatairesParRole(), l'absence est ici une anomalie :
    // GlobalExceptionHandler transforme cette exception en 404.
    @Test
    void destinataireParId_utilisateurIntrouvable_leve404() {
        when(utilisateurRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurApi.destinataireParId(9999L))
                .isInstanceOf(UtilisateurIntrouvableException.class)
                .hasMessageContaining("9999");
    }
}
