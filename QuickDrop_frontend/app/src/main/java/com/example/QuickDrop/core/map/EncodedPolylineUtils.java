package com.example.QuickDrop.core.map;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Decodes an "encoded polyline" string (the standard Google/Mapbox/OSRM polyline
 * algorithm, precision 5) into a list of {@link GeoPoint}s.
 *
 * OSRM (used by {@link RoutingClient}) returns geometries in this exact format when
 * requested with {@code geometries=polyline}, so this decoder is a drop-in replacement
 * for the one that used to decode Google Directions API responses.
 */
public final class EncodedPolylineUtils {

    private EncodedPolylineUtils() {
    }

    public static List<GeoPoint> decode(String encoded) {
        List<GeoPoint> points = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) {
            return points;
        }

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            points.add(new GeoPoint(lat / 1E5, lng / 1E5));
        }
        return points;
    }
}
