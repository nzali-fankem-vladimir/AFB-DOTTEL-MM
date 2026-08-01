-- V3__insertion_utilisateurs_test.sql
-- Module DOTTEL - Dotations Telephoniques Mensuelles - AFRILAND HORIZON 2030
-- Insertion des utilisateurs de test, un par role (RoleEnum)
-- Mot de passe pour tous : "Test1234" (hash BCrypt)
--
-- Emails au format prenom_nom@afrilandfirstbank.com (tiret bas), pour
-- correspondre a la convention d'identifiant Active Directory reelle
-- confirmee au chantier MM.7 (Keycloak provisoire) : l'identifiant AD
-- est la partie locale de l'email professionnel (ex. vladimir_nzali).
-- Corrige par rapport a la version d'origine, qui utilisait un point.

INSERT INTO utilisateurs (matricule, nom, prenom, email, role, mot_de_passe_hash, actif, date_creation)
VALUES
    ('1847', 'MBARGA', 'Jean Paul', 'jeanpaul_mbarga@afrilandfirstbank.com', 'ARH', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW()),
    ('2093', 'ESSAMA', 'Marie Claire', 'marieclaire_essama@afrilandfirstbank.com', 'CRH', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW()),
    ('1562', 'ATANGANA', 'Paul', 'paul_atangana@afrilandfirstbank.com', 'DRH', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW()),
    ('2201', 'NKOLO', 'Sylvie', 'sylvie_nkolo@afrilandfirstbank.com', 'EMPLOYE', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW()),
    ('1734', 'TCHINDA', 'Marc', 'marc_tchinda@afrilandfirstbank.com', 'ADMIN', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW())
ON CONFLICT (matricule) DO NOTHING;