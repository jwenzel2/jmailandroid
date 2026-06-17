package com.jmail.android.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

class JmailApi(private val session: SessionStore) {
    var darkTheme: Boolean
        get() = session.darkTheme
        set(value) {
            session.darkTheme = value
        }

    val configuredServerUrl: String?
        get() = session.serverUrl

    private val serverUrl: String
        get() = session.serverUrl ?: error("Server URL is not configured.")

    private val accessToken: String
        get() = session.accessToken ?: error("You are not signed in.")

    fun loginUrl(): URI = URI.create(
        "$serverUrl/api/v1/mobile/login?redirect_uri=" +
            URLEncoder.encode("jmail://auth", Charsets.UTF_8),
    )

    fun compatibility(serverUrl: String): JSONObject =
        request("$serverUrl/api/v1/compatibility", authenticated = false)

    fun exchange(code: String): JSONObject = request(
        "$serverUrl/api/v1/mobile/exchange",
        "POST",
        JSONObject().put("code", code),
        authenticated = false,
    )

    fun me(): JSONObject = request("/api/v1/me")
    fun accounts(): JSONArray = request("/api/v1/accounts").getJSONArray("accounts")
    fun folders(): JSONArray = requestArray("/api/mail/folders")
    fun inbox(): JSONArray =
        messages("INBOX")
    fun messages(folder: String): JSONArray =
        request("/api/mail/messages?folder=${encode(folder)}&page=1&pageSize=50").getJSONArray("messages")
    fun searchMessages(folder: String, query: String): JSONArray =
        request("/api/mail/search?folder=${encode(folder)}&q=${encode(query)}").getJSONArray("messages")
    fun message(folder: String, uid: Int): JSONObject =
        request("/api/mail/message/${encode(folder)}/$uid")
    fun downloadAttachment(folder: String, uid: Int, partId: String): ByteArray =
        requestBytes("/api/mail/message/${encode(folder)}/$uid/attachment/${encode(partId)}")
    fun action(folder: String, uid: Int, action: String, targetFolder: String? = null): JSONObject =
        action(folder, listOf(uid), action, targetFolder)
    fun action(folder: String, uids: List<Int>, action: String, targetFolder: String? = null): JSONObject =
        request(
            "/api/mail/actions",
            "POST",
            JSONObject()
                .put("folder", folder)
                .put("uids", JSONArray().apply { uids.forEach { put(it) } })
                .put("action", action)
                .apply {
                    if (!targetFolder.isNullOrBlank()) put("targetFolder", targetFolder)
                },
        )

    fun send(
        to: String,
        cc: String,
        bcc: String,
        subject: String,
        text: String,
        inReplyToUid: Int? = null,
        inReplyToFolder: String? = null,
    ): JSONObject =
        request(
            "/api/mail/send",
            "POST",
            JSONObject()
                .put("to", recipients(to))
                .put("cc", recipients(cc))
                .put("bcc", recipients(bcc))
                .put("subject", subject)
                .put("text", text)
                .put("html", JSONObject.NULL)
                .put("inReplyToUid", inReplyToUid ?: JSONObject.NULL)
                .put("inReplyToFolder", inReplyToFolder ?: JSONObject.NULL),
        )

    fun contacts(query: String = ""): JSONArray =
        request("/api/contacts?q=${encode(query)}").getJSONArray("contacts")
    fun createContact(name: String, email: String, phone: String): JSONObject =
        request(
            "/api/contacts",
            "POST",
            JSONObject()
                .put("displayName", name)
                .put("email", email)
                .put("phone", phone.ifBlank { JSONObject.NULL })
                .put("company", JSONObject.NULL)
                .put("notes", JSONObject.NULL)
                .put("favorite", false),
        )
    fun updateContact(id: String, name: String, email: String, phone: String): JSONObject =
        request(
            "/api/contacts/$id",
            "PATCH",
            JSONObject()
                .put("displayName", name)
                .put("email", email)
                .put("phone", phone.ifBlank { JSONObject.NULL }),
        )
    fun deleteContact(id: String): JSONObject = request("/api/contacts/$id", "DELETE")

    fun events(from: String, to: String): JSONArray =
        request("/api/calendar/events?from=$from&to=$to").getJSONArray("events")
    fun createEvent(title: String, startsAt: String, endsAt: String, location: String): JSONObject =
        request(
            "/api/calendar/events",
            "POST",
            JSONObject()
                .put("title", title)
                .put("description", JSONObject.NULL)
                .put("location", location.ifBlank { JSONObject.NULL })
                .put("startsAt", startsAt)
                .put("endsAt", endsAt)
                .put("allDay", false),
        )
    fun updateEvent(id: String, title: String, startsAt: String, endsAt: String, location: String): JSONObject =
        request(
            "/api/calendar/events/$id",
            "PATCH",
            JSONObject()
                .put("title", title)
                .put("location", location.ifBlank { JSONObject.NULL })
                .put("startsAt", startsAt)
                .put("endsAt", endsAt)
                .put("allDay", false),
        )
    fun deleteEvent(id: String): JSONObject = request("/api/calendar/events/$id", "DELETE")

    fun addAccount(body: JSONObject): JSONObject = request("/api/v1/accounts", "POST", body)

    fun registerDevice(installationId: String, token: String) {
        request(
            "/api/v1/mobile/devices",
            "PUT",
            JSONObject().put("installationId", installationId).put("fcmToken", token)
                .put("deviceName", android.os.Build.MODEL).put("notificationsEnabled", true),
        )
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8)

    private fun recipients(value: String): JSONArray {
        val result = JSONArray()
        value
            .split(',', ';')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { result.put(it) }
        return result
    }

    private fun request(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        authenticated: Boolean = true,
    ): JSONObject {
        return JSONObject(requestText(path, method, body, authenticated))
    }

    private fun requestArray(path: String): JSONArray = JSONArray(requestText(path))

    private fun requestBytes(path: String): ByteArray {
        val connection = openConnection(path)
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val bytes = stream.use { it.readBytes() }
        if (connection.responseCode !in 200..299) {
            error("HTTP ${connection.responseCode}: ${bytes.toString(Charsets.UTF_8)}")
        }
        return bytes
    }

    private fun requestText(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        authenticated: Boolean = true,
    ): String {
        val connection = openConnection(path)
        connection.requestMethod = method
        connection.setRequestProperty("Accept", "application/json")
        if (authenticated) connection.setRequestProperty("Authorization", "Bearer $accessToken")
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
        }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val text = stream.bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299) {
            if (connection.responseCode == 404 && connection.url.toString().endsWith("/api/v1/compatibility")) {
                error("This server does not expose the jmail mobile API yet. Deploy the updated jmail-api and proxy /api/v1/* to it.")
            }
            error("HTTP ${connection.responseCode}: $text")
        }
        return text
    }

    private fun openConnection(path: String): HttpURLConnection {
        val url = if (path.startsWith("http")) path else "$serverUrl$path"
        return URI.create(url).toURL().openConnection() as HttpURLConnection
    }
}
