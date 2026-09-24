package com.example.QuickDrop.core.map;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.HttpURLConnection;

/**
 * Fetches driving directions from the public OSRM (Open Source Routing Machine) demo
 * server -- a free, open-source alternative to the Google Directions API. No API key
 * is required.
 *
 * OSRM's public demo instance (router.project-osrm.org) is meant for light,
 * non-commercial use. For production traffic, self-host OSRM (it is free, open-source
 * software you can run on your own server with your own map data) or point
 * {@link #BASE_URL} to another OSRM-compatible endpoint.
 */
public final class RoutingClient {

    private static final String TAG = "RoutingClient";

    /** Change this if you self-host OSRM or use another OSRM-compatible provider. */
    private static final String BASE_URL = "https://router.project-osrm.org";

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private RoutingClient() {
    }

    public interface RouteCallback {
        void onSuccess(RouteResult result);
        void onFailure(Exception error);
    }

    public static final class RouteResult {
        public final List<GeoPoint> points;
        public final double distanceMeters;
        public final double durationSeconds;

        public RouteResult(List<GeoPoint> points, double distanceMeters, double durationSeconds) {
            this.points = points;
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
        }
    }

    /**
     * Fetches the driving route passing through the given waypoints (in visiting order)
     * and returns it via {@code callback} on the main thread.
     */
    public static void fetchRoute(List<GeoPoint> waypoints, RouteCallback callback) {
        if (waypoints == null || waypoints.size() < 2) {
            postFailure(callback, new IllegalArgumentException("Need at least 2 waypoints"));
            return;
        }

        EXECUTOR.execute(() -> {
            try {
                String url = buildRouteUrl(waypoints);
                String json = httpGet(url);
                RouteResult result = parseRouteResponse(json);
                postSuccess(callback, result);
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch route", e);
                postFailure(callback, e);
            }
        });
    }

    private static String buildRouteUrl(List<GeoPoint> waypoints) {
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            GeoPoint p = waypoints.get(i);
            if (i > 0) coords.append(';');
            coords.append(String.format(Locale.US, "%.6f,%.6f", p.getLongitude(), p.getLatitude()));
        }
        return BASE_URL + "/route/v1/driving/" + coords
                + "?overview=full&geometries=polyline&continue_straight=true";
    }

    private static String httpGet(String urlString) throws IOException {

        Log.d(TAG, "Request URL: " + urlString);

        URL url = new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

     
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);
        connection.setUseCaches(false);
        // OSRM's public demo server (fronted by nginx) returns 403 for requests without
        // a recognizable User-Agent -- Java/Android's default UA gets blocked as bot traffic.
        connection.setRequestProperty("User-Agent", "MyDeliveryApp/1.0 (Android)");

        try {

            int responseCode = connection.getResponseCode();

            Log.d(TAG, "OSRM HTTP response code: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {

                InputStream errorStream = connection.getErrorStream();

                String errorBody = "";

                if (errorStream != null) {
                    Scanner scanner =
                            new Scanner(errorStream, "UTF-8");

                    scanner.useDelimiter("\\A");

                    errorBody =
                            scanner.hasNext()
                                    ? scanner.next()
                                    : "";

                    scanner.close();
                }

                throw new IOException(
                        "OSRM HTTP " +
                                responseCode +
                                " - " +
                                errorBody
                );
            }

            InputStream in = connection.getInputStream();

            Scanner scanner =
                    new Scanner(in, "UTF-8");

            scanner.useDelimiter("\\A");

            String response =
                    scanner.hasNext()
                            ? scanner.next()
                            : "";

            scanner.close();

            return response;

        } finally {
            connection.disconnect();
        }
    }

    private static RouteResult parseRouteResponse(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray routes = root.optJSONArray("routes");
        if (routes == null || routes.length() == 0) {
            throw new IOException("OSRM returned no routes");
        }
        JSONObject route = routes.getJSONObject(0);
        String encodedGeometry = route.getString("geometry");
        double distance = route.optDouble("distance", 0);
        double duration = route.optDouble("duration", 0);
        List<GeoPoint> points = EncodedPolylineUtils.decode(encodedGeometry);
        return new RouteResult(points, distance, duration);
    }

    private static void postSuccess(RouteCallback callback, RouteResult result) {
        if (callback == null) return;
        MAIN_HANDLER.post(() -> callback.onSuccess(result));
    }

    private static void postFailure(RouteCallback callback, Exception error) {
        if (callback == null) return;
        MAIN_HANDLER.post(() -> callback.onFailure(error));
    }
}
