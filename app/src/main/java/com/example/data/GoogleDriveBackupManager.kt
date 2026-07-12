package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GoogleDriveBackupManager(private val context: Context) {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(BackupData::class.java)

    private val scope = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/drive.appdata"

    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
        try {
            // First try with the Account object if available
            val acc = account.account
            if (acc != null) {
                GoogleAuthUtil.getToken(context, acc, scope)
            } else {
                // Fallback to the account email address string
                val email = account.email ?: return@withContext null
                GoogleAuthUtil.getToken(context, email, scope)
            }
        } catch (e: Exception) {
            Log.e("BackupManager", "Error getting token", e)
            null
        }
    }

    suspend fun backupData(backupData: BackupData): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken() ?: return@withContext false
        val jsonContent = backupAdapter.toJson(backupData)

        try {
            val fileId = getBackupFileId(token)
            if (fileId != null) {
                updateFileContent(token, fileId, jsonContent)
            } else {
                val newId = createBackupFile(token)
                if (newId != null) {
                    updateFileContent(token, newId, jsonContent)
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("BackupManager", "Error backing up data", e)
            false
        }
    }

    suspend fun restoreData(): BackupData? = withContext(Dispatchers.IO) {
        val token = getAccessToken() ?: return@withContext null
        try {
            val fileId = getBackupFileId(token) ?: return@withContext null
            val content = getFileContent(token, fileId) ?: return@withContext null
            backupAdapter.fromJson(content)
        } catch (e: Exception) {
            Log.e("BackupManager", "Error restoring data", e)
            null
        }
    }

    private fun getBackupFileId(token: String): String? {
        val url = "https://www.googleapis.com/drive/v3/files?q=name='backup.json'+and+'appDataFolder'+in+parents&spaces=appDataFolder"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val responseBody = response.body?.string() ?: return null
            val json = JSONObject(responseBody)
            val files = json.getJSONArray("files")
            if (files.length() > 0) {
                return files.getJSONObject(0).getString("id")
            }
        }
        return null
    }

    private fun createBackupFile(token: String): String? {
        val url = "https://www.googleapis.com/drive/v3/files"
        val bodyJson = JSONObject().apply {
            put("name", "backup.json")
            put("parents", listOf("appDataFolder"))
        }.toString()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val responseBody = response.body?.string() ?: return null
            val json = JSONObject(responseBody)
            return json.getString("id")
        }
    }

    private fun updateFileContent(token: String, fileId: String, content: String): Boolean {
        val url = "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .patch(content.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }

    private fun getFileContent(token: String, fileId: String): String? {
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            return response.body?.string()
        }
    }
}
