"""
Free, open-source replacement for the Google Maps Distance Matrix API
(and for the openrouteservice calls that used a hardcoded API key).

Uses OSRM (Open Source Routing Machine) -- https://project-osrm.org/
No API key required.

NOTE: router.project-osrm.org is OSRM's public demo server, meant for light /
non-commercial use. For production traffic, self-host OSRM (free, open-source)
with your own map data and point OSRM_BASE_URL at it.
"""
import requests

OSRM_BASE_URL = "https://router.project-osrm.org"


def geocode_address(address_string):
    """
    Free, open-source replacement for OpenCage/Google geocoding.

    Uses Nominatim (OpenStreetMap's free geocoding search) -- no API key required.
    Returns (lat, lng) as floats, or (None, None) if the address could not be found.

    NOTE: Nominatim's public usage policy asks for a max of 1 request/second and a
    descriptive User-Agent -- both respected here. For heavy production use,
    self-host Nominatim or use another provider.
    """
    url = "https://nominatim.openstreetmap.org/search"
    params = {"q": address_string, "format": "json", "limit": 1}
    headers = {"User-Agent": "my_delivery-app"}

    try:
        response = requests.get(url, params=params, headers=headers, timeout=10)
        response.raise_for_status()
        results = response.json()
        if not results:
            return None, None
        return float(results[0]["lat"]), float(results[0]["lon"])
    except (requests.RequestException, KeyError, IndexError, ValueError):
        return None, None


NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org"


def _coord(lat, lng):
    # OSRM expects "lon,lat" order.
    return f"{lng},{lat}"


def get_distance_duration(origin_lat, origin_lng, dest_lat, dest_lng):
    """
    Drop-in replacement for a single Google Distance Matrix API "element".

    Returns a dict shaped like Google's response so existing code reading
    ['distance']['value'] / ['text'] (etc.) keeps working unchanged:

        {
            'distance': {'value': <meters>, 'text': '12.3 km'},
            'duration': {'value': <seconds>, 'text': '15 mins'},
        }

    Returns None if the route could not be computed.
    """
    try:
        origin_lat = float(origin_lat)
        origin_lng = float(origin_lng)
        dest_lat = float(dest_lat)
        dest_lng = float(dest_lng)
    except (TypeError, ValueError):
        return None

    coords = f"{_coord(origin_lat, origin_lng)};{_coord(dest_lat, dest_lng)}"
    url = f"{OSRM_BASE_URL}/route/v1/driving/{coords}?overview=false"

    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        data = response.json()
        routes = data.get("routes")
        if not routes:
            return None
        route = routes[0]
        distance_m = route["distance"]
        duration_s = route["duration"]
        return {
            'distance': {'value': distance_m, 'text': f"{distance_m / 1000:.1f} km"},
            'duration': {'value': duration_s, 'text': f"{round(duration_s / 60)} mins"},
        }
    except (requests.RequestException, KeyError, IndexError, ValueError, TypeError):
        return None


def get_distance_matrix_km(coordinate_strings):
    """
    coordinate_strings: list of "lat,lng" strings (same format already used
    throughout this app, e.g. f"{obj.lat},{obj.lng}").

    Returns an NxN matrix of driving distances in whole kilometers (int),
    matching the previous Google-based build_distance_matrix() output, so the
    OR-Tools solver in GetLivreurRoad keeps working unchanged.

    Raises requests.RequestException on network/API failure.
    """
    points = []
    for c in coordinate_strings:
        lat_str, lng_str = c.split(",")
        points.append((float(lat_str), float(lng_str)))

    coords_param = ";".join(_coord(lat, lng) for lat, lng in points)
    url = f"{OSRM_BASE_URL}/table/v1/driving/{coords_param}?annotations=distance"

    response = requests.get(url, timeout=30)
    response.raise_for_status()
    data = response.json()
    distances = data["distances"]  # meters, NxN, may contain None for unreachable pairs

    return [
        [int((d or 0) // 1000) for d in row]
        for row in distances
    ]
