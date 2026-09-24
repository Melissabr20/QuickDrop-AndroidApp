package com.example.QuickDrop.core.map;

import android.content.Context;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import java.io.File; // add this import at the top


/**
 * One-time osmdroid setup. osmdroid (OpenStreetMap for Android) is the free,
 * open-source map SDK used throughout the app instead of the Google Maps SDK.
 *
 * Call {@link #init(Context)} once, e.g. from {@code MyDeliveryApplication.onCreate()}.
 */



public final class OsmMapConfig {

    private OsmMapConfig() {
    }

    public static final ITileSource OPEN_TOPO_MAP = new XYTileSource(
        "OpenTopoMap", 0, 17, 256, ".png",
        new String[]{
                "https://a.tile.opentopomap.org/",
                "https://b.tile.opentopomap.org/",
                "https://c.tile.opentopomap.org/"
        }
);

    public static void init(Context context) {
        Context appContext = context.getApplicationContext();

        Configuration.getInstance().load(
                appContext,
                appContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        );

        Configuration.getInstance().setUserAgentValue("MyDeliveryApp/1.0");

        File osmdroidBasePath = new File(appContext.getExternalFilesDir(null), "osmdroid");
        File osmdroidTileCache = new File(osmdroidBasePath, "tiles");
        osmdroidTileCache.mkdirs();
        Configuration.getInstance().setOsmdroidBasePath(osmdroidBasePath);
        Configuration.getInstance().setOsmdroidTileCache(osmdroidTileCache);
    }
}
