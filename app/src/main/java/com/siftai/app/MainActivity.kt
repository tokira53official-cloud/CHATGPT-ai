
package com.siftai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val prefs by lazy {
        getSharedPreferences("siftai", MODE_PRIVATE)
    }
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var chat: LinearLayout
    private lateinit var input: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 12)
            setBackgroundColor(Color.rgb(8, 11, 18))
        }

        val title = TextView(this).apply {
            text = "✦ SiftAI"
            textSize = 28f
            setTextColor(Color.rgb(88, 166, 255))
        }
        root.addView(title)

        status = TextView(this).apply {
            text = "Your personal AI assistant"
            setTextColor(Color.LTGRAY)
        }
        root.addView(status)

        val scroll = ScrollView(this)
        chat = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(chat)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val settings = Button(this).apply {
            text = "⚙ Settings"
            setOnClickListener { settings() }
        }
        root.addView(settings)

        val row = LinearLayout(this)
        input = EditText(this).apply {
            hint = "Ask SiftAI anything..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setBackgroundColor(Color.rgb(25, 32, 45))
        }
        row.addView(input, LinearLayout.LayoutParams(0, -2, 1f))

        val send = Button(this).apply {
            text = "Send"
            setOnClickListener { sendMessage() }
        }
        row.addView(send)
        root.addView(row)

        setContentView(root)
        addMessage("SiftAI", "Welcome! Open Settings to connect your AI provider.")
    }

    private fun addMessage(who: String, message: String) {
        val view = TextView(this).apply {
            text = "$who\n$message"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(12, 12, 12, 12)
            setBackgroundColor(Color.rgb(20, 28, 42))
        }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.setMargins(0, 8, 0, 8)
        chat.addView(view, params)
    }

    private fun settings() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 0)
        }

        fun field(hintText: String, value: String): EditText {
            return EditText(this).apply {
                hint = hintText
                setText(value)
                textSize = 14f
            }
        }

        val endpoint = field(
            "API endpoint",
            prefs.getString(
                "endpoint",
                "https://api.openai.com/v1/chat/completions"
            ) ?: ""
        )
        val model = field(
            "Model ID",
            prefs.getString("model", "gpt-4o-mini") ?: ""
        )
        val key = field(
            "API key",
            prefs.getString("key", "") ?: ""
        )

        box.addView(endpoint)
        box.addView(model)
        box.addView(key)

        AlertDialogBuilder.show(
            this, box
        ) {
            prefs.edit()
                .putString("endpoint", endpoint.text.toString().trim())
                .putString("model", model.text.toString().trim())
                .putString("key", key.text.toString().trim())
                .apply()
            status.text = "Settings saved"
        }
    }

    private fun sendMessage() {
        val question = input.text.toString().trim()
        if (question.isEmpty()) return

        val key = prefs.getString("key", "") ?: ""
        if (key.isBlank()) {
            addMessage("SiftAI", "Please configure your API key in Settings.")
            return
        }

        val endpoint = prefs.getString("endpoint", "") ?: ""
        val model = prefs.getString("model", "") ?: ""

        input.setText("")
        addMessage("You", question)
        status.text = "Thinking..."

        executor.execute {
            try {
                val messages = JSONArray()
                messages.put(
                    JSONObject()
                        .put("role", "system")
                        .put("content", "You are SiftAI, a helpful AI assistant.")
                )
                messages.put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", question)
                )

                val body = JSONObject()
                    .put("model", model)
                    .put("messages", messages)

                val connection =
                    URL(endpoint).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 60000
                connection.setRequestProperty(
                    "Content-Type", "application/json"
                )
                connection.setRequestProperty(
                    "Authorization", "Bearer $key"
                )
                connection.doOutput = true

                connection.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val code = connection.responseCode
                val stream = if (code in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream.bufferedReader().use {
                    it.readText()
                }
                connection.disconnect()

                if (code !in 200..299) {
                    throw Exception("HTTP $code: ${response.take(200)}")
                }

                val answer = JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .optString("content", "No answer returned.")

                runOnUiThread {
                    addMessage("SiftAI", answer)
                    status.text = "Ready"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    addMessage(
                        "SiftAI",
                        "Request failed: ${e.message}"
                    )
                    status.text = "Connection error"
                }
            }
        }
    }
}

private object AlertDialogBuilder {
    fun show(
        activity: Activity,
        content: LinearLayout,
        save: () -> Unit
    ) {
        android.app.AlertDialog.Builder(activity)
            .setTitle("SiftAI Settings")
            .setView(content)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ -> save() }
            .show()
    }
}
