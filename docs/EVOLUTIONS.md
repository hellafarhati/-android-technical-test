# Planification des évolutions

*(bonus de l'énoncé : « documenter la planification des évolutions à venir »)*

Priorisation par rapport de valeur sur coût, telle que je la proposerais en revue d'équipe.

## Court terme — avant une mise en production

| Sujet | Pourquoi | Coût estimé |
| --- | --- | --- |
| **Tests instrumentés Room** | Le SQL est aujourd'hui validé uniquement à la compilation. Un test de la jointure albums/favoris et de la première migration sécurise la persistance. | 0,5 j |
| **Tests d'UI Compose** | `AlbumsScreen` est déjà sans état : quelques tests sur les états chargement / erreur / vide / contenu, plus l'accessibilité. | 1 j |
| **Pull-to-refresh** | Geste attendu par les utilisateurs ; le bouton d'actualisation de la barre est un pis-aller. | 0,5 j |
| **Détection de connectivité** | Distinguer « hors ligne » de « serveur en panne » et relancer automatiquement la synchronisation au retour du réseau. | 1 j |
| **Politique de fraîcheur du cache** | Aujourd'hui on synchronise à chaque ouverture. Stocker un `lastSyncAt` permettrait de ne rafraîchir qu'au-delà d'un seuil, et d'économiser data et batterie. | 0,5 j |

## Moyen terme — passage à l'échelle

| Sujet | Pourquoi | Coût estimé |
| --- | --- | --- |
| **Paging 3** | Nécessaire dès que le volume dépasse ce qui tient confortablement en mémoire, ou dès que l'API pagine. `PagingSource` s'appuierait directement sur le DAO existant : la couche UI change peu. | 2 j |
| **Convention plugins Gradle (`build-logic`)** | La configuration (`compileSdk`, Java 17, Compose, Hilt) est répétée dans chaque `build.gradle.kts`. Des plugins `leboncoin.android.library` / `leboncoin.android.feature` supprimeraient cette duplication et rendraient la création d'un module triviale. Se justifie surtout à partir d'une dizaine de modules. | 1,5 j |
| **`WorkManager` pour la synchronisation** | Synchronisation en arrière-plan, avec backoff et contraintes réseau, plutôt qu'au premier plan. | 1 j |
| **Screenshot testing** | Verrouiller le rendu (Paparazzi ou Roborazzi) pour détecter les régressions visuelles, y compris en mode sombre et en RTL. | 1,5 j |
| **Baseline Profiles + Macrobenchmark** | Mesurer le démarrage à froid et le scroll sur 5 000 éléments, puis réduire le jank via un profil de référence. | 2 j |
| **Éclater `:data` en network / database / domain** | Tant qu'il n'y a qu'un seul consommateur de données, `:data` reste facile à naviguer. Si un second client (ex. widget, module Wear) apparaît, ou si l'équipe grossit, séparer ces couches redonne des frontières de build indépendantes. Le refactor est mécanique : les packages sont déjà cloisonnés (`network`, `database`, `repository`, `usecase`). | 1 j |

## Long terme — produit et industrialisation

- **Analytics et monitoring** : interface `Analytics` dans `:core`, implémentations
  injectées, et suivi des erreurs (Crashlytics / Sentry) avec les `DataError` déjà typés
  comme dimensions.
- **Feature flags** pour livrer en continu sans branche longue.
- **Kotlin Multiplatform** : aujourd'hui `:core` et `:data` sont des modules Android (le
  premier à cause de Compose, le second à cause de Room/Hilt), donc pas directement
  éligibles au KMP. C'est un compromis assumé de la structure à 5 modules : si le partage
  avec iOS devient un sujet réel, la première étape serait d'extraire à nouveau `Album`,
  `RefreshResult` et `DispatcherProvider` dans un module Kotlin pur dédié — un sous-ensemble
  de l'ancien `:core:model`/`:core:common` — sans toucher au reste.
- **Migration `androidx.compose.material3` → Spark complet** au fil de la couverture du
  design system, pour ne plus mélanger deux bibliothèques de composants.
- **CI enrichie** : la [pipeline actuelle](../.github/workflows/ci.yml) lance tests, lint et
  APK de debug. À compléter par un rapport de couverture (Kover), `ktlint`/`detekt`, un
  contrôle de la taille d'APK, et les tests instrumentés sur émulateur ou ferme d'appareils.

## Dette technique connue et assumée

1. Pas de suppression des lignes disparues côté serveur : la synchronisation fait un
   `UPSERT` sans purge. Un `DELETE … WHERE id NOT IN (…)` sur 5 000 identifiants dépasserait
   la limite de variables liées de SQLite ; la solution propre est une table temporaire ou
   un marqueur de génération. Sans pagination ni suppression côté API, le cas ne se présente
   pas aujourd'hui.
2. Le `debounce` de la recherche est une constante en dur dans le ViewModel ; à externaliser
   si le produit veut l'ajuster.
3. `:core` expose Material 3 **et** Spark : acceptable à deux écrans, à trancher avant
   d'ajouter des features.
4. `FakeAlbumsRepository` et consorts sont dupliqués entre `:feature:albums` et
   `:feature:albumdetails` plutôt que centralisés dans un module de test partagé. Assumé
   tant qu'il n'y a que deux consommateurs (voir [`TESTS.md`](TESTS.md)) ; le jour où un
   troisième module de test apparaît, extraire un `:core:testing` redevient rentable.
