package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.TaoMeditation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    data class GeminiMeditationResponse(
        val title: String,
        val verse: String,
        val commentary: String
    )

    private val responseAdapter = moshi.adapter(GeminiMeditationResponse::class.java)

    suspend fun generateMeditation(day: Int): TaoMeditation? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is not configured. Falling back to seeded database.")
            return null
        }

        val systemInstruction = "You are a wise, poetic Taoist sage and translator. Your task is to generate an authentic daily Taoist meditation and commentary for Day $day of 365. You must return your response in strict JSON format with exactly three fields: 'title' (a short serene poetic title), 'verse' (a beautiful 3-6 line spiritual verse inspired by the Tao Te Ching, Chuang Tzu, or Lieh Tzu, formatted with newlines), and 'commentary' (a deep, comforting commentary exploring its meaning and how to apply it to modern hectic life, composed of 2-4 tranquil paragraphs). Do NOT wrap the JSON in markdown code blocks or backticks. Return the RAW JSON only."

        val prompt = "Generate daily meditation for Day $day. Ensure it is unique, poetic, and contains high-quality spiritual insights."

        try {
            // Build the standard request payload manually using JSONObject
            val requestJson = JSONObject()
            
            val contentsArray = JSONArray()
            val contentObject = JSONObject()
            val partsArray = JSONArray()
            val partObject = JSONObject()
            partObject.put("text", prompt)
            partsArray.put(partObject)
            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)
            requestJson.put("contents", contentsArray)

            val systemInstructionObject = JSONObject()
            val systemPartsArray = JSONArray()
            val systemPartObject = JSONObject()
            systemPartObject.put("text", systemInstruction)
            systemPartsArray.put(systemPartObject)
            systemInstructionObject.put("parts", systemPartsArray)
            requestJson.put("systemInstruction", systemInstructionObject)

            val generationConfig = JSONObject()
            generationConfig.put("responseMimeType", "application/json")
            generationConfig.put("temperature", 0.7)
            requestJson.put("generationConfig", generationConfig)

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to fetch from Gemini API. Code: ${response.code}, Message: ${response.message}")
                return null
            }

            val responseBodyString = response.body?.string() ?: return null
            Log.d(TAG, "Gemini Response received.")

            // Parse response json
            val jsonResponse = JSONObject(responseBodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            val rawText = firstPart?.optString("text") ?: return null

            // Clean up backticks if any were generated
            val cleanedText = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedResponse = responseAdapter.fromJson(cleanedText) ?: return null
            return TaoMeditation(
                day = day,
                title = parsedResponse.title,
                verse = parsedResponse.verse,
                commentary = parsedResponse.commentary,
                isFavorite = false,
                userNote = "",
                isCompleted = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content from Gemini: ${e.message}", e)
            return null
        }
    }
}
