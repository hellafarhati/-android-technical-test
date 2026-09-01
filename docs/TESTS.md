# Stratégie de test

```bash
./gradlew test
```

30 tests unitaires, avec des fakes plutôt que des mocks (typés, pas de framework de mock).

- `:data` (use cases) — filtrage, recherche, regroupement par album
- `:data` (repository) — cache vs réseau, erreurs, favoris qui survivent à un refresh
- `:data` (réseau) — parsing du JSON (clés inconnues, valeurs nulles)
- `:feature:albums` — états UI, recherche, favoris, rotation et process death
- `:feature:albumdetails` — navigation, photo absente, favori

Pas testé, faute de temps : DAO Room réel, composables, Retrofit réel, navigation de bout
en bout — nécessitent un émulateur ou Robolectric.