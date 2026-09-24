# QuickDrop — Backend

Django REST API for the QuickDrop delivery platform: clients, suppliers,
delivery drivers, products, carts, orders, delivery pricing, and route
optimization.

## Features

- Token/JWT authentication, registration, and profile management
- Product catalogue with images, availability per supplier
- Cart and order (delivery) management
- Delivery address management
- Delivery pricing based on real road distance
- Delivery-route optimization for drivers with multiple stops (OR-Tools)
- Distance/duration calculation between clients, suppliers, and drivers
- Admin dashboard endpoints

## Tech stack

- Django 5 / Django REST Framework
- `djangorestframework-simplejwt` (JWT auth) + DRF token auth
- **OSRM** for routing/distance calculation and **Nominatim** for geocoding —
  both free, open-source (OpenStreetMap-based), and require no API key
- **OR-Tools** (Google) for the delivery route optimization solver
- **Leaflet** + OpenStreetMap tiles for the admin web map
- MySQL

## Project structure

```
QuickDrop/
├── manage.py
├── requirements.txt
├── docs/
│   └── BACKEND_NOTES.md       # maps/routing setup details, production notes
├── QuickDrop_project/          # Django project config
│   ├── settings.py
│   ├── urls.py
│   ├── wsgi.py
│   └── asgi.py
├── QuickDrop_app/               # main application
│   ├── models.py
│   ├── serializers.py
│   ├── forms.py
│   ├── admin.py
│   ├── validations.py
│   ├── optimisation.py         # route optimization solver (OR-Tools)
│   ├── routing_client.py       # OSRM / Nominatim client
│   ├── urls.py
│   ├── migrations/
│   └── views/                  # views split by domain:
│       ├── auth_views.py           # register, login, logout, profile
│       ├── client_views.py
│       ├── article_views.py
│       ├── produit_views.py
│       ├── panier_views.py         # cart & delivery pricing
│       ├── adresse_views.py        # delivery addresses
│       ├── livraison_views.py      # deliveries
│       ├── fournisseur_views.py    # suppliers
│       ├── livreur_views.py        # delivery drivers & route optimization
│       ├── distance_views.py       # distance/duration endpoints
│       ├── admin_views.py
│       └── home_views.py
├── static/
├── templates/
└── media/
    └── uploads/
```

## Setup

```bash
python -m venv venv
source venv/bin/activate            # Windows: venv\Scripts\activate
pip install -r requirements.txt

# Create the MySQL database (settings.py expects a database named "database")
# CREATE DATABASE database;

python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

Use `0.0.0.0:8000` (not just `8000`) so the Android emulator can reach the
server via `10.0.2.2:8000`.

> **Note on the public OSRM/Nominatim servers:** `router.project-osrm.org`
> and `nominatim.openstreetmap.org` are free public services meant for
> light/evaluation use, not high-volume production traffic. For production,
> self-host OSRM and/or Nominatim (both free, open-source), or use another
> OSRM/Nominatim-compatible provider — just update `OSRM_BASE_URL` /
> `NOMINATIM_BASE_URL` in `QuickDrop_app/routing_client.py`.

See `docs/BACKEND_NOTES.md` for more detail on the mapping/routing setup.

## GitHub notes

Do not commit `db.sqlite3`, `__pycache__/`, virtual environments, or any
`local_settings.py` with real credentials. See `.gitignore`.
