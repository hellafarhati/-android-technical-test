# Soumission du test technique

## Fonctionnalités

- Liste des photos **groupées par album**, avec en-têtes collants (`stickyHeader`).
- **Recherche** par titre (debounce 250 ms) et filtre **Favoris**.
- **Favoris persistés localement**, conservés après une resynchronisation réseau.
- **Écran de détail** recevant réellement l'identifiant de la photo (navigation type-safe).
- **Hors ligne** : la base Room est la source de vérité, l'application affiche son cache
  même sans réseau et après redémarrage.
- **États explicites** : chargement, erreur avec bouton *Réessayer*, vide, contenu.
  Une erreur réseau alors que du cache existe se limite à un *snackbar* non bloquant.
- Rotation, mode sombre, RTL, `contentDescription` et `stateDescription` pour les
  lecteurs d'écran, textes en français et en anglais.

## Stack

| Domaine | Choix | Pourquoi |
| --- | --- | --- |
| UI | Jetpack Compose + Spark (design system leboncoin) | Composants maison, cohérence visuelle |
| Présentation | MVVM, `StateFlow`, `SavedStateHandle` | État unique, survit rotation et process death |
| DI | Hilt (KSP) | Standard Android, portées gérées, testable |
| Réseau | Retrofit 3 + OkHttp + kotlinx.serialization | Contrat typé, parsing sans réflexion |
| Persistance | Room | Requêtes vérifiées à la compilation, `Flow` réactif |
| Images | Coil 3 | Compose-first, partage du client OkHttp, cache disque |
| Navigation | Navigation Compose type-safe | Arguments vérifiés à la compilation |
| Tests | JUnit4, coroutines-test, Turbine, *fakes* écrits à la main | Déterministe, sans mocks fragiles |

## Architecture en un coup d'œil

```
:app  ──────────────►  :feature:albums   ─┐
      └─────────────►  :feature:albumdetails
                              │
                              ▼
                          :data   (réseau, Room, repository, use cases)
                              │
                              ▼
                          :core   (modèles, dispatchers, design system)
```

5 modules Gradle : `:core` regroupe tout ce qui est partagé et stable (modèles, dispatchers,
composants Compose), `:data` toute la logique d'accès aux données (réseau, Room,
repository, use cases), `:feature:albums`/`:feature:albumdetails` sont les deux écrans,
`:app` ne fait que le câblage (Hilt, navigation). Détails, raisons du découpage et
alternative envisagée dans [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).
