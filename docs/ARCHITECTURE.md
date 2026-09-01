# Architecture

- MVVM + flux de données unidirectionnel : UI → ViewModel → `StateFlow<UiState>`.
- Room est la source de vérité, le réseau ne fait qu'alimenter Room.
- Un modèle par couche : DTO réseau → Entity Room → `Album` (métier) → `AlbumUi` (présentation).

## Modules

- `:core` — modèles, dispatchers, design system
- `:data` — réseau, Room, repository, use cases
- `:feature:albums` / `:feature:albumdetails` — les deux écrans
- `:app` — câblage Hilt et navigation, rien d'autre

5 modules plutôt que d'éclater `:data` en network/database/domain séparés : avec deux
écrans et un seul consommateur de données, ce découpage plus fin n'apporterait rien
aujourd'hui.

## Gestion de la rotation

`StateFlow` + `stateIn(WhileSubscribed)` garde l'état en mémoire, `SavedStateHandle` pour
la recherche et le filtre, la position de scroll est restaurée.

## Performance

Tri et jointure en SQL, filtrage/regroupement sur `Dispatchers.Default`, debounce sur la
recherche, `LazyColumn` avec `key` stable, `UPSERT` plutôt que `DELETE` + `INSERT`.

## Librairies

- **Hilt** — injection de dépendances demandée par l'énoncé, vérifiée à la compilation
- **Room** — persistance avec requêtes, tri, jointure et observation réactive
- **Retrofit + kotlinx.serialization** — déjà en place, pas de réflexion à l'exécution
- **Coil** — pensé pour Compose, partage l'`OkHttpClient` existant
- **Navigation Compose type-safe** — argument vérifié à la compilation
- **Spark** — design system leboncoin, déjà présent dans le projet de départ
- **Turbine** — tester les `Flow` sans tests instables
- **Fakes plutôt que mocks** — typés, pas de dépendance supplémentaire