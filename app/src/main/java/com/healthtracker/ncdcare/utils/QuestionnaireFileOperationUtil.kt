package com.healthtracker.ncdcare.utils

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import java.io.File

suspend fun getQuestionnaireJsonStringFromAssets(
    context: Context,
    backgroundContext: CoroutineContext,
    fileName: String,
): String? {
    return withContext(backgroundContext) {
        if (fileName.isNotEmpty()) {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } else {
            null
        }
    }
}

suspend fun getQuestionnaireJsonStringFromFileUri(
    context: Context,
    backgroundContext: CoroutineContext,
    uri: Uri,
): String {
    return withContext(backgroundContext) {
        val reader = BufferedReader(context.contentResolver.openInputStream(uri)?.reader())
        reader.use { reader -> reader.readText() }
    }
}

/**
 * Returns a [Uri] pointing to a temporary file containing the content of the asset file with
 * [filename].
 */
suspend fun createUri(context: Context, backgroundContext: CoroutineContext, fileName: String) =
    withContext(backgroundContext) {
        val outputFile = File(context.externalCacheDir, fileName)
        context.assets.open(fileName).use { inputStream ->
            outputFile.outputStream().use { outputStream -> IOUtils.copy(inputStream, outputStream) }
        }
        Uri.fromFile(outputFile)
    }