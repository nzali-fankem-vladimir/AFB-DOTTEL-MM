# afb-dotations-telephoniques
projet de digitalisation du processus de dotation téléphonique mensuelle des RH

## Authentification (Sprint MM.7)

L'authentification passe par un **Keycloak local et provisoire**, déclaré
dans `docker-compose.yml` (service `keycloak`, port 8180) et importé depuis
`keycloak/realm-dottel-dev.json`. Ce Keycloak tourne uniquement sur le poste
de développement — le realm réel de la DSI reste hors périmètre et non
confirmé à ce jour (voir `docs/monolithe-modulaire/MM.7_keycloak_provisoire.md`
et `docs/monolithe-modulaire/MM.7_bascule_realm_dsi.md`).
