package com.smartwifi.switcher.ui.screens

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource

object MapTileSources {

    // High-tech Deep Dark Matter (Pitch-black background, glowing subtle road network)
    val CARTO_DARK: OnlineTileSourceBase = XYTileSource(
        "CartoDarkMatter",
        0, 19, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/rastertiles/dark_all/",
            "https://b.basemaps.cartocdn.com/rastertiles/dark_all/",
            "https://c.basemaps.cartocdn.com/rastertiles/dark_all/"
        )
    )

    // Voyager Edition (High contrast, modern technical cartography)
    val CARTO_VOYAGER: OnlineTileSourceBase = XYTileSource(
        "CartoVoyager",
        0, 19, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
        )
    )

    // Standard OpenStreetMap Fallback
    val OSM_STANDARD: OnlineTileSourceBase = TileSourceFactory.MAPNIK
}
