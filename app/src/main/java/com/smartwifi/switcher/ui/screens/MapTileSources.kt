package com.smartwifi.switcher.ui.screens

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.MapTileIndex

object MapTileSources {

    // CARTO Basemaps Official API Key (Cuenta k3v1bvoLabs - 5M peticiones/mes)
    const val CARTO_API_KEY: String = "cb1_4mth_1_6594809bcefc062849f9ce07"

    // High-tech Deep Dark Matter (Fondo negro OLED puro, red de calles tenue, 100% autenticado sin marcas de agua)
    val CARTO_DARK: OnlineTileSourceBase = object : OnlineTileSourceBase(
        "CartoDarkMatterAuth",
        0, 19, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/rastertiles/dark_all/",
            "https://b.basemaps.cartocdn.com/rastertiles/dark_all/",
            "https://c.basemaps.cartocdn.com/rastertiles/dark_all/"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            return baseUrl +
                MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) +
                mImageFilenameEnding + "?key=" + CARTO_API_KEY
        }
    }

    // Voyager Edition (Alto contraste, cartografía técnica nítida, 100% autenticado sin marcas de agua)
    val CARTO_VOYAGER: OnlineTileSourceBase = object : OnlineTileSourceBase(
        "CartoVoyagerAuth",
        0, 19, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            return baseUrl +
                MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) +
                mImageFilenameEnding + "?key=" + CARTO_API_KEY
        }
    }

    // Standard OpenStreetMap Fallback Oficial
    val OSM_STANDARD: OnlineTileSourceBase = TileSourceFactory.MAPNIK
}
