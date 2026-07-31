-- V3__insertion_utilisateurs_test.sql
-- Module DOTTEL - Dotations Telephoniques Mensuelles - AFRILAND HORIZON 2030
-- Insertion des utilisateurs de test, un par role (RoleEnum)
-- Mot de passe pour tous : "Test1234" (hash BCrypt)

INSERT INTO utilisateurs (matricule, nom, prenom, email, role, mot_de_passe_hash, actif, date_creation)
VALUES
    ('1847', 'MBARGA', 'Jean Paul', 'jeanpaul.mbarga@afrilandfirstbank.com', 'ARH', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW()),
    ('2093', 'ESSAMA', 'Marie Claire', 'marieclaire.essama@afrilandfirstbank.com', 'CRH', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW()),
    ('1562', 'ATANGANA', 'Paul', 'paul.atangana@afrilandfirstbank.com', 'DRH', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW()),
    ('2201', 'NKOLO', 'Sylvie', 'sylvie.nkolo@afrilandfirstbank.com', 'EMPLOYE', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW()),
    ('1734', 'TCHINDA', 'Marc', 'marc.tchinda@afrilandfirstbank.com', 'ADMIN', '$2b$10$78r4j2I8BeuwWrdkq4oIOuAMOvr4GAoMSYzPkVm/twkthwMUKSuGq', TRUE, NOW())
ON CONFLICT (matricule) DO NOTHING;