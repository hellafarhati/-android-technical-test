# Bugs, pièges et correctifs

L'énoncé annonce des « pièges et bugs cachés ». En voici l'inventaire, classé par gravité,
avec le correctif appliqué.

| # | Problème | Gravité | Correctif |
| --- | --- | --- | --- |
| 1 | `GlobalScope.launch` dans le ViewModel | 🔴 Critique | `viewModelScope` |
| 2 | `MutableSharedFlow` sans `replay` → écran vide après rotation | 🔴 Critique | `StateFlow` + `stateIn(WhileSubscribed)` |
| 3 | Singleton d'analytics conservant un `Context` d'`Activity` | 🔴 Critique | Supprimé ; `@ApplicationContext` si besoin |
| 4 | `DetailsActivity` déclarée `LAUNCHER` → deux icônes dans le lanceur | 🔴 Critique | Activité unique + Navigation Compose |
| 5 | Navigation vers le détail sans passer d'identifiant | 🔴 Critique | Route type-safe avec argument `photoId` |
| 6 | Logs HTTP `BODY` activés **en release** (condition inversée) | 🔴 Critique | Activés uniquement en debug, via `NetworkConfig` |
| 7 | Aucune persistance : rien ne survit au redémarrage | 🔴 Critique | Room comme source de vérité |
| 8 | Test unitaire qui ne compile pas (mauvais constructeur) | 🔴 Critique | Suite de tests réécrite (30 tests unitaires) |
| 9 | `catch (_: Exception) {}` silencieux : ni erreur, ni chargement | 🟠 Majeur | `RefreshResult` + états `Loading`/`Error`/`Empty` |
| 10 | Aucun framework d'injection de dépendances | 🟠 Majeur | Hilt |
| 11 | DTO réseau utilisé directement dans les composables | 🟠 Majeur | 4 modèles, un par couche |
| 12 | `LaunchedEffect(Unit)` déclenchant le chargement depuis l'UI | 🟠 Majeur | Chargement piloté par le ViewModel (`init`) |
| 13 | `modifier` du parent réappliqué à l'image enfant | 🟠 Majeur | Chaque composant reçoit son propre `Modifier` |
| 14 | `collectAsStateWithLifecycle(emptyList())` sur un `SharedFlow` | 🟠 Majeur | `StateFlow` typé avec valeur initiale explicite |
| 15 | Thème clair forcé, pas de variante nuit | 🟡 Mineur | `values-night` + couleurs de barres transparentes |
| 16 | `compileSdk = 35` incompatible avec `core-ktx 1.17` / `activity 1.11` | 🟡 Mineur | `compileSdk`/`targetSdk` = 36 |
| 17 | Bloc `dependencies {}` imbriqué dans `android {}` | 🟡 Mineur | Remis au niveau du projet |
| 18 | `consumerProguardFiles` pointant sur un fichier absent | 🟡 Mineur | Déclaration supprimée |
| 19 | Imports et dépendances de test inutilisés | 🟡 Mineur | Nettoyés |
| 20 | URLs d'images potentiellement injoignables → trous blancs | 🟡 Mineur | Placeholder et image de repli dans `AlbumThumbnail` |

---

## Les cinq pièges qui coûtent le plus cher

### 1. `GlobalScope` dans un ViewModel

`GlobalScope` n'est lié à aucun cycle de vie. La coroutine continue après la destruction du
ViewModel, garde une référence sur lui, et un aller-retour rapide sur l'écran empile des
requêtes réseau qui ne seront jamais annulées.

```kotlin
// avant
GlobalScope.launch { /* chargement */ }

// après
viewModelScope.launch { /* annulé automatiquement dans onCleared() */ }
```

### 2. `MutableSharedFlow` sans `replay` — le bug de rotation

Un `SharedFlow` sans `replay` ne rejoue rien à un nouvel abonné. Après une rotation, le
composable se réabonne… et n'obtient plus rien : liste vide, alors que la donnée avait bien
été chargée. Le `collectAsStateWithLifecycle(emptyList())` renvoyait alors sa valeur
initiale, ce qui masquait le symptôme derrière un écran « normalement » vide.

Correctif : un `StateFlow` construit avec `stateIn(viewModelScope,
SharingStarted.WhileSubscribed(5_000), AlbumsUiState())`. L'état est conservé, et la fenêtre
de 5 secondes évite de relancer requête SQL et regroupement à chaque rotation.

### 3. La fuite mémoire

Le projet initial embarquait **LeakCanary** : c'était l'indice. Un singleton d'analytics
détenait un `Context` d'`Activity` pour toute la durée de vie du processus — chaque rotation
fuyait une `Activity` entière, avec sa hiérarchie de vues.

Règle appliquée : un objet à portée application ne détient jamais qu'un
`@ApplicationContext`. La dépendance à LeakCanary est conservée, mais en `debugImplementation`
uniquement.

### 4. Deux icônes de lanceur, et un écran de détail sans données

`DetailsActivity` était déclarée dans le manifeste avec un `intent-filter` `MAIN` +
`LAUNCHER` : deux icônes de l'application apparaissaient sur l'écran d'accueil. Elle était
par ailleurs lancée sans le moindre extra, donc affichait un placeholder « work in progress ».

Correctif : une seule `MainActivity`, et Navigation Compose avec des destinations type-safe :

```kotlin
@Serializable
data class AlbumDetailsDestination(val photoId: Int)
```

L'argument est vérifié à la compilation ; il ne peut plus être oublié.

### 5. Les logs HTTP inversés

```kotlin
// avant : BODY en production, rien en debug
if (!BuildConfig.DEBUG) { addInterceptor(HttpLoggingInterceptor().apply { level = BODY }) }
```

Sérialiser 5 000 éléments dans le Logcat d'un build de production, c'est un coût CPU inutile
**et** une fuite potentielle de données. Le correctif ne se limite pas à retirer le `!` :
`BuildConfig.DEBUG` d'un module bibliothèque ne reflète pas le type de build de
l'application. C'est désormais `:app` qui fournit un objet `NetworkConfig(baseUrl,
isLoggingEnabled)` à `:data`.

---

## Autres corrections notables

**Le `modifier` réutilisé.** Dans la ligne de liste, le `modifier` reçu par le composable
parent (contenant `fillMaxWidth`, un padding, parfois une hauteur) était réappliqué à
l'`AsyncImage` enfant. L'image héritait donc de contraintes prévues pour la carte. Chaque
enfant construit maintenant son propre `Modifier`, et le `modifier` du parent n'est appliqué
qu'à la racine du composable — la convention Compose.

**Le déclenchement du chargement depuis l'UI.** `LaunchedEffect(Unit) { viewModel.load() }`
se relance à chaque entrée dans la composition. Le chargement appartient au ViewModel : il
est déclenché dans `init` et protégé contre les appels concurrents.

**Les erreurs avalées.** Un `catch (_: Exception) {}` rendait tout échec invisible :
l'utilisateur voyait un écran vide sans savoir s'il devait attendre, réessayer ou vérifier sa
connexion. Le flux d'erreur est maintenant typé de bout en bout
(`RemoteException` → `DataError` → message localisé), avec deux comportements distincts :
erreur bloquante avec bouton *Réessayer* s'il n'y a aucune donnée, simple *snackbar* si le
cache permet de continuer.

**Les images manquantes.** Le jeu de données de test référence un service d'images qui peut
être injoignable. Plutôt que d'afficher des rectangles blancs, `AlbumThumbnail` fournit un
`placeholder`, une image d'`error` et un `fallback`.

**`compileSdk = 35`.** Le projet déclarait `core-ktx 1.17.0` et `activity-compose 1.11.0`,
qui exigent une compilation contre l'API 36 : le build échoue avec un message
« requires libraries and applications that depend on it to compile against version 36 ».
`compileSdk` et `targetSdk` sont passés à 36.
