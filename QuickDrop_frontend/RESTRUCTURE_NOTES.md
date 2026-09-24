# Notes de restructuration — QuickDrop

Ce document résume les changements apportés au projet.

## 1. Restructuration du projet

Le code, auparavant organisé "par type" (`activities/`, `adapters/`, `fragments/`,
`models/`... en vrac, avec ~150 fichiers au même niveau), est maintenant organisé
**par fonctionnalité** :

```
com.example.QuickDrop/
├── MyDeliveryApplication.java
├── core/
│   ├── network/        (ApiClient, ApiService, RetrofitClientInstance, dto/)
│   ├── map/             (NOUVEAU — osmdroid + OSRM, voir section 2)
│   ├── model/            (tous les modèles de données, renommés)
│   └── util/
└── ui/
    ├── auth/            (WelcomeActivity, LoginActivity, RegisterActivity)
    ├── client/          (écrans et fragments du rôle client)
    ├── livreur/         (écrans et fragments du livreur)
    ├── fournisseur/     (écrans et fragments du fournisseur)
    ├── admin/           (écrans et fragments de l'administrateur)
    └── common/          (écrans/fragments partagés entre plusieurs rôles :
                          profil, paramètres, sécurité, à propos, historique)
```

## 2. Remplacement de l'API Google Maps

Toute dépendance à Google Maps Platform (payante, nécessite une clé API) a été
retirée et remplacée par des alternatives **open-source et gratuites, sans clé
API** :

| Avant (Google) | Après (open-source) |
|---|---|
| Google Maps SDK (`com.google.android.gms.maps`) | **osmdroid** (cartes OpenStreetMap) |
| API Directions de Google | **OSRM** (Open Source Routing Machine, serveur public gratuit) |
| Google Play Services Location (`FusedLocationProviderClient`) | `android.location.LocationManager` (natif Android) |
| `com.google.android.libraries.places` (dépendance inutilisée) | supprimée |

Nouveau module `core/map/` :
- `OsmMapConfig.java` — initialise osmdroid (appelé dans `MyDeliveryApplication`)
- `RoutingClient.java` — calcule un itinéraire via l'API publique OSRM
- `EncodedPolylineUtils.java` — décode le tracé (même format que l'ancien code)

Fichiers réécrits pour utiliser osmdroid/OSRM :
- `ui/livreur/LiveTrackingMapActivity.java`
- `ui/client/DeliveryAddressActivity.java`
- `ui/client/fragments/DeliveryTrackingFragment.java`
- `ui/livreur/fragments/LivreurRouteMapFragment.java`
- `ui/auth/LoginActivity.java` (localisation au login)
- `ui/client/OrderDetailsActivity.java` (code mort/commenté nettoyé)

Layouts mis à jour (`<fragment SupportMapFragment>` → `<org.osmdroid.views.MapView>`) :
`activity_map.xml`, `activity_delivery_address.xml`, `fragment_dashboard.xml`,
`order_details.xml`.

`build.gradle.kts` : suppression de `play-services-maps`, `play-services-location`,
`places`, et de la dépendance AR/Sceneform inutilisée ; ajout d'`osmdroid-android`.
Plus besoin de `MAPS_API_KEY` dans `local.properties`.

⚠️ **Note production** : le serveur public OSRM (`router.project-osrm.org`) est
destiné à un usage léger/de démonstration. Pour de la production, il est
recommandé d'auto-héberger OSRM (gratuit, open-source) ou d'utiliser un autre
fournisseur compatible — changez simplement `BASE_URL` dans `RoutingClient.java`.

## 3. Fichiers renommés (extraits significatifs)

| Ancien nom | Nouveau nom | Raison |
|---|---|---|
| `ModelClass` | `DeliveryTask` | représente une tâche de livraison du livreur |
| `NodesClass` | `RoutingNode` | nœud d'un itinéraire |
| `LocationClass` | `DeliveryStop` | arrêt (retrait/livraison) sur la tournée |
| `ArticleDiscClass` | `CartArticleItem` | ligne d'article dans le panier |
| `Home` | `ClientHomeActivity` | écran d'accueil du client |
| `Administrateur` | `AdminHomeActivity` | écran d'accueil admin |
| `MyAdapter` / `MyAdapterHis` | `LivreurTaskAdapter` / `LivreurHistoryAdapter` | adaptateurs du livreur |
| `AdminLigneAdapter` | `OrderLineAdapter` | en fait utilisé par admin **et** client → déplacé en commun |
| `MapActivity` | `LiveTrackingMapActivity` | plus explicite |
| `TheMapFragment` | `LivreurRouteMapFragment` | carte de tournée du livreur |

Voir l'historique complet dans les imports Java — chaque classe garde un nom
clair et cohérent avec son rôle réel dans l'app.

## 4. Nettoyage

- Suppression de `TrackActivity.java` et `fragment_track.xml` : code mort,
  jamais référencé nulle part, doublon quasi identique de `DeliveryTrackingFragment`.
- Suppression de la dépendance `com.google.ar.sceneform:filament-android` :
  jamais utilisée dans le code.

## 5. Points non traités (hors périmètre demandé)

- `LivreurListActivity` utilise par erreur le layout `activity_fournisseur_list`
  au lieu d'un layout dédié (bug pré-existant, non lié à la demande).
- `OrderDetailsActivity` contient un gros bloc de code mort commenté
  (fonctionnalité jamais terminée) — conservé tel quel, imports Google Maps
  retirés uniquement.
