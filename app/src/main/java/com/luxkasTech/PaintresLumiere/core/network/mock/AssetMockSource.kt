package com.luxkasTech.PaintresLumiere.core.network.mock

import android.content.Context
import java.io.FileNotFoundException

/** Reads mocks from `src/main/assets/mocks/`. */
class AssetMockSource(private val context: Context) : MockAssetSource {
    override fun read(path: String): String? = try {
        context.assets.open("mocks/$path").bufferedReader().use { it.readText() }
    } catch (e: FileNotFoundException) {
        null
    }
}
