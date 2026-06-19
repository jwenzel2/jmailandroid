package com.jmail.android.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

class JmailApi(private val session: SessionStore) {
    var onSessionExpired: ((String) -> Unit)? = null

    var darkTheme: Boolean
        get() = session.darkTheme
        set(value) {
            session.darkTheme = value
        }

    var notificationsEnabled: Boolean
        get() = session.notificationsEnabled
        set(value) {
            session.notificationsEnabled = value
        }

    val configuredServerUrl: String?
        get() = session.serverUrl

    private val serverUrl: String
        get() = session.serverUrl ?: error("Server URL is not configured.")

    private val accessToken: String
        get() {
            if (session.isAccessTokenExpired) {
                expireSession()
            }
            return session.accessToken ?: error("You are not signed in.")
        }

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

    fun renewMobileToken(): JSONObject =
        request("/api/v1/mobile/token", "POST").also {
            val token = it.optString("accessToken")
            if (token.isBlank()) expireSession()
            session.saveAccessToken(token, it.optString("expiresAt").takeIf { value -> value.isNotBlank() })
        }

    fun me(): JSONObject = request("/api/v1/me")
    fun accounts(): JSONArray = arrayField(request("/api/v1/accounts"), "accounts")
    fun folders(): JSONArray = requestArray("/api/mail/folders")
    fun inbox(): JSONArray =
        messages("INBOX")
    fun messages(folder: String): JSONArray =
        arrayField(messagePage(folder, 1, 50), "messages")
    fun messagePage(folder: String, page: Int, pageSize: Int = 50): JSONObject =
        request("/api/mail/messages?folder=${encode(folder)}&page=$page&pageSize=$pageSize")
    fun searchMessages(folder: String, query: String): JSONArray =
        arrayField(request("/api/mail/search?folder=${encode(folder)}&q=${encode(query)}"), "messages")
    fun message(folder: String, uid: Int): JSONObject =
        request("/api/mail/message/${encode(folder)}/$uid")
    fun downloadAttachment(folder: String, uid: Int, partId: String): ByteArray =
        requestBytes("/api/mail/message/${encode(folder)}/$uid/attachment/${encode(partId)}")
    fun action(folder: String, uid: Int, action: String, targetFolder: String? = null): JSONObject =
        action(folder, listOf(uid), action, targetFolder)
    fun action(folder: String, uids: List<Int>, action: String, targetFolder: String? = null): JSONObject {
        val validUids = uids.filter { it > 0 }
        if (validUids.isEmpty()) error("Select at least one message.")
        return request(
            "/api/mail/actions",
            "POST",
            JSONObject()
                .put("folder", folder)
                .put("uids", JSONArray().apply { validUids.forEach { put(it) } })
                .put("action", action)
                .apply {
                    if (!targetFolder.isNullOrBlank()) put("targetFolder", targetFolder)
                },
        )
    }

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
        arrayField(request("/api/contacts?q=${encode(query)}"), "contacts")
    fun createContact(name: String, email: String, phone: String, company: String, notes: String): JSONObject =
        request(
            "/api/contacts",
            "POST",
            JSONObject()
                .put("displayName", name)
                .put("email", email)
                .put("phone", phone.ifBlank { JSONObject.NULL })
                .put("company", company.ifBlank { JSONObject.NULL })
                .put("notes", notes.ifBlank { JSONObject.NULL })
                .put("favorite", false),
        )
    fun updateContact(id: String, name: String, email: String, phone: String, company: String, notes: String): JSONObject =
        request(
            "/api/contacts/${encode(id)}",
            "PATCH",
            JSONObject()
                .put("displayName", name)
                .put("email", email)
                .put("phone", phone.ifBlank { JSONObject.NULL })
                .put("company", company.ifBlank { JSONObject.NULL })
                .put("notes", notes.ifBlank { JSONObject.NULL }),
        )
    fun deleteContact(id: String): JSONObject = request("/api/contacts/${encode(id)}", "DELETE")

    fun events(from: String, to: String): JSONArray =
        arrayField(request("/api/calendar/events?from=${encode(from)}&to=${encode(to)}"), "events")
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
            "/api/calendar/events/${encode(id)}",
            "PATCH",
            JSONObject()
                .put("title", title)
                .put("location", location.ifBlank { JSONObject.NULL })
                .put("startsAt", startsAt)
                .put("endsAt", endsAt)
                .put("allDay", false),
        )
    fun deleteEvent(id: String): JSONObject = request("/api/calendar/events/${encode(id)}", "DELETE")

    fun addAccount(body: JSONObject): JSONObject = request("/api/v1/accounts", "POST", body)
    fun updateAccount(id: String, body: JSONObject): JSONObject = request("/api/v1/accounts/${encode(id)}", "PATCH", body)
    fun deleteAccount(id: String): JSONObject = request("/api/v1/accounts/${encode(id)}", "DELETE")

    fun registerDevice(installationId: String, token: String) {
        request(
            "/api/v1/mobile/devices",
            "PUT",
            JSONObject().put("installationId", installationId).put("fcmToken", token)
                .put("deviceName", android.os.Build.MODEL).put("notificationsEnabled", session.notificationsEnabled),
        )
    }

    fun unregisterDevice() {
        request("/api/v1/mobile/devices/${encode(session.installationId)}", "DELETE")
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8)

    private fun arrayField(body: JSONObject, key: String): JSONArray = body.optJSONArray(key) ?: JSONArray()

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
        val text = requestText(path, method, body, authenticated).trim()
        if (text.isBlank()) return JSONObject()
        return runCatching { JSONObject(text) }.getOrElse { error("Server returned invalid JSON.") }
    }

    private fun requestArray(path: String): JSONArray {
        val text = requestText(path).trim()
        if (text.isBlank()) return JSONArray()
        return runCatching { JSONArray(text) }.getOrElse { error("Server returned invalid JSON.") }
    }

    private fun requestBytes(path: String): ByteArray {
        renewMobileTokenIfNeeded()
        val connection = openConnection(path)
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        val bytes = responseBytes(connection)
        if (connection.responseCode !in 200..299) {
            if (connection.responseCode == 401) expireSession()
            error(apiError(connection.responseCode, bytes.toString(Charsets.UTF_8)))
        }
        return bytes
    }

    private fun requestText(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        authenticated: Boolean = true,
    ): String {
        if (authenticated && path != "/api/v1/mobile/token") renewMobileTokenIfNeeded()
        val connection = openConnection(path)
        connection.requestMethod = method
        connection.setRequestProperty("Accept", "application/json")
        if (authenticated) connection.setRequestProperty("Authorization", "Bearer $accessToken")
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
        }
        val text = responseBytes(connection).toString(Charsets.UTF_8)
        if (connection.responseCode !in 200..299) {
            if (connection.responseCode == 401) expireSession()
            if (connection.responseCode == 404 && connection.url.toString().endsWith("/api/v1/compatibility")) {
                error("This server does not expose the jmail mobile API yet. Deploy the updated jmail-api and proxy /api/v1/* to it.")
            }
            error(apiError(connection.responseCode, text))
        }
        return text
    }

    private fun responseBytes(connection: HttpURLConnection): ByteArray {
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        return stream?.use { it.readBytes() } ?: ByteArray(0)
    }

    private fun renewMobileTokenIfNeeded() {
        if (!session.isAccessTokenExpiringSoon()) return
        renewMobileToken()
    }

    private fun openConnection(path: String): HttpURLConnection {
        val url = if (path.startsWith("http://") || path.startsWith("https://")) path else "$serverUrl$path"
        return (URI.create(url).toURL().openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
        }
    }

    private fun expireSession(): Nothing {
        val message = "Your mobile session expired. Sign in again."
        session.clearAccessToken()
        onSessionExpired?.invoke(message)
        error(message)
    }

    private fun apiError(statusCode: Int, text: String): String {
        val trimmed = text.trim()
        val serverMessage = runCatching {
            JSONObject(trimmed).let { body ->
                body.optString("message")
                    .ifBlank { body.optString("error") }
                    .ifBlank { body.optString("code") }
            }
        }.getOrNull()
        return "HTTP $statusCode: ${serverMessage?.takeIf { it.isNotBlank() } ?: trimmed.ifBlank { "Request failed" }}"
    }
}
