# QuickDrop — Android App

QuickDrop is an Android delivery application with role-based flows for
clients, suppliers, delivery drivers, and administrators.

## Features

- User authentication and profile management
- Product browsing, cart, and order flows
- Supplier order confirmation
- Delivery driver route + live tracking, with map and turn-by-turn directions
- Client-facing "where's my delivery" live tracking
- Admin screens for users, suppliers, clients, products, and orders

## Tech stack

- Java
- Android Gradle Plugin
- AndroidX, Material Components, ViewBinding
- Retrofit, Gson, OkHttp
- **osmdroid** (OpenStreetMap) for maps, **OSRM** for routing — both free and
  open-source, no API key required (see [Maps & routing](#maps--routing) below)
- Glide and Picasso

## Project structure

The codebase is organized by feature/role rather than by file type, so related
screens, fragments, and adapters live together:

```text
app/src/main/java/com/example/QuickDrop/
  MyDeliveryApplication.java   App entry point (notification channel, osmdroid init)

  core/
    network/        ApiClient, ApiService (Retrofit), RetrofitClientInstance
    network/dto/    Request/response DTOs
    model/          Domain models
    map/            Map & routing helpers (osmdroid + OSRM wrappers)
    util/           Shared utility classes (FileUtils, ImageColorChanger)

  ui/
    auth/           Welcome, login, and registration screens
    client/         Client home, cart, delivery address, order tracking
    livreur/        Delivery-person home, route map, task list
    fournisseur/    Supplier home, article/inventory management
    admin/          Admin dashboard, user/client/supplier/order management
    common/         Screens and components shared across roles
                    (profile, settings, security, about, shared adapters)
```

Each `ui/<role>/` package has its own `fragments/` and `adapters/` sub-packages
where relevant.

## Maps & routing

- **osmdroid** renders OpenStreetMap tiles for all in-app maps.
- **OSRM** (`core/map/RoutingClient.java`) computes routes/directions. It
  points at the public demo instance `router.project-osrm.org` by default —
  that server is meant for light/evaluation use, not production traffic. For
  a real deployment, self-host OSRM (free, open-source) or point
  `RoutingClient` at another OSRM-compatible provider, and update `BASE_URL`
  accordingly.
- Device location uses Android's native `LocationManager`.
- Address search / reverse geocoding uses Android's built-in `Geocoder`
  class — part of the Android platform itself, no API key or extra
  dependency required.

None of this requires a Google Maps API key.

## Setup

1. Open the project in Android Studio.
2. Copy `local.properties.example` to `local.properties` and set your Android
   SDK path — no map API key is required.
3. Sync Gradle and run the `app` configuration.

## GitHub notes

Do not commit `local.properties`, build outputs, APK/AAB files, or signing
keys. They are ignored by `.gitignore`.
