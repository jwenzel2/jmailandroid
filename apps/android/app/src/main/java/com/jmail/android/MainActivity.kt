package com.jmail.android

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.jmail.android.data.JmailApi
import com.jmail.android.data.SessionStore
import com.jmail.android.data.SessionStore.Companion.normalizeServerUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {
    private lateinit var session: SessionStore
    private lateinit var api: JmailApi
    private var authCode by mutableStateOf<String?>(null)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) registerPushDevice()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionStore(this)
        api = JmailApi(session)
        authCode = intent.data?.getQueryParameter("code")
        setContent {
            var darkTheme by remember { mutableStateOf(session.darkTheme) }
            JmailTheme(darkTheme = darkTheme) {
                JmailApp(session, api, authCode, ::registerPush, onThemeChanged = { darkTheme = it }) {
                    CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(api.loginUrl().toString()))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        authCode = intent.data?.getQueryParameter("code")
    }

    fun registerPush() {
        if (!session.notificationsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        registerPushDevice()
    }

    @Suppress("DEPRECATION")
    private fun registerPushDevice() {
        runCatching {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                Thread {
                    runCatching { api.registerDevice(session.installationId, token) }
                }.start()
            }
        }
    }
}

private enum class Destination(val label: String, val icon: ImageVector) {
    Mail("Mail", Icons.Default.Email),
    Calendar("Calendar", Icons.Default.CalendarMonth),
    Contacts("Contacts", Icons.Default.Contacts),
    Settings("Settings", Icons.Default.Settings),
}

private enum class MailFilter(val label: String) {
    All("All"),
    Unread("Unread"),
    Starred("Starred"),
}

private val LightBlueScheme = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF001B3F),
    secondary = Color(0xFF1976D2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF002F6C),
    tertiary = Color(0xFF42A5F5),
    surface = Color(0xFFFAFCFF),
    background = Color(0xFFFAFCFF),
)

private val DarkBlueScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF002F6C),
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFD7E3FF),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF00315F),
    secondaryContainer = Color(0xFF0B3D75),
    onSecondaryContainer = Color(0xFFD5E3FF),
    tertiary = Color(0xFF2196F3),
    surface = Color(0xFF0B1220),
    background = Color(0xFF050B14),
)

@Composable
private fun JmailTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkBlueScheme else LightBlueScheme,
        content = content,
    )
}

@Composable
private fun JmailApp(
    session: SessionStore,
    api: JmailApi,
    authCode: String?,
    registerPush: () -> Unit,
    onThemeChanged: (Boolean) -> Unit,
    login: () -> Unit,
) {
    var loggedIn by remember { mutableStateOf(session.isSignedIn) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        if (session.accessToken != null && !session.isSignedIn) {
            session.clearAccessToken()
            error = "Your mobile session expired. Sign in again."
            loggedIn = false
        }
    }
    LaunchedEffect(authCode) {
        if (authCode != null && session.serverUrl != null) {
            runCatching { withContext(Dispatchers.IO) { api.exchange(authCode) } }
                .onSuccess {
                    val token = it.optString("accessToken")
                    val expiresAt = it.optString("expiresAt").takeIf { value -> value.isNotBlank() }
                    if (token.isBlank()) {
                        error = "Login completed, but the server did not return a mobile token."
                    } else {
                        session.saveAccessToken(token, expiresAt)
                        loggedIn = true
                        registerPush()
                    }
                }.onFailure { error = it.message }
        }
    }
    if (!loggedIn) {
        Onboarding(session, api, error, login)
    } else {
        LaunchedEffect(Unit) { registerPush() }
        MainShell(api, registerPush, onThemeChanged) {
            session.clear()
            loggedIn = false
        }
    }
}

@Composable
private fun Onboarding(session: SessionStore, api: JmailApi, error: String?, login: () -> Unit) {
    var server by remember { mutableStateOf(session.serverUrl ?: "") }
    var status by remember { mutableStateOf(error) }
    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center) {
        Text("Connect to jmail", style = MaterialTheme.typography.headlineMedium)
        Text("Your organization server provides identity, mail accounts, calendar, contacts, and push.")
        OutlinedTextField(server, { server = it }, Modifier.fillMaxWidth().padding(top = 24.dp), label = { Text("Server URL") })
        Button(onClick = {
            status = null
            Thread {
                runCatching {
                    val normalized = normalizeServerUrl(server) ?: error("Enter a valid server URL.")
                    api.compatibility(normalized)
                    normalized
                }
                    .onSuccess {
                        session.serverUrl = it
                        runOnMain(login)
                    }.onFailure { runOnMain { status = it.message } }
            }.start()
        }, modifier = Modifier.padding(top = 16.dp)) { Text("Continue with Keycloak") }
        status?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp)) }
    }
}

private fun runOnMain(block: () -> Unit) {
    android.os.Handler(android.os.Looper.getMainLooper()).post(block)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(api: JmailApi, registerPush: () -> Unit, onThemeChanged: (Boolean) -> Unit, logout: () -> Unit) {
    var destination by remember { mutableStateOf(Destination.Mail) }
    var composeDraft by remember { mutableStateOf<ComposeDraft?>(null) }
    composeDraft?.let {
        ComposeScreen(api, it) { composeDraft = null }
        return
    }
    Scaffold(
        topBar = {
            if (destination != Destination.Mail) {
                TopAppBar(title = { Text(destination.label) })
            }
        },
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach {
                    NavigationBarItem(destination == it, { destination = it }, { Icon(it.icon, it.label) }, label = { Text(it.label) })
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (destination) {
                Destination.Mail -> AccountScreen(api, compose = { composeDraft = it })
                Destination.Calendar -> CalendarScreen(api)
                Destination.Contacts -> ContactsScreen(api, compose = { composeDraft = it })
                Destination.Settings -> SettingsScreen(api, registerPush, onThemeChanged, logout)
            }
        }
    }
}

@Composable
private fun AccountScreen(api: JmailApi, compose: (ComposeDraft) -> Unit) {
    val accounts = remember { mutableStateListOf<JSONObject>() }
    val folders = remember { mutableStateListOf<JSONObject>() }
    val messages = remember { mutableStateListOf<JSONObject>() }
    var addingAccount by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<JSONObject?>(null) }
    var viewingAccount by remember { mutableStateOf<JSONObject?>(null) }
    var folder by remember { mutableStateOf("INBOX") }
    var drawerOpen by remember { mutableStateOf(false) }
    var refreshNonce by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(1) }
    var hasMoreMessages by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var mailFilter by remember { mutableStateOf(MailFilter.All) }
    var selectingMessages by remember { mutableStateOf(false) }
    var movingSelected by remember { mutableStateOf(false) }
    var selectedMessageUids by remember { mutableStateOf(setOf<Int>()) }
    var selectedAccount by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<JSONObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val activeSearch = searchQuery.trim()
    val visibleMessages = messages.filter { message ->
        when (mailFilter) {
            MailFilter.All -> true
            MailFilter.Unread -> !message.optBoolean("seen")
            MailFilter.Starred -> message.optBoolean("flagged")
        }
    }
    LaunchedEffect(Unit, folder, refreshNonce, activeSearch) {
        loading = true
        error = null
        runCatching { withContext(Dispatchers.IO) { api.accounts() } }.onSuccess { accounts.replace(it) }.onFailure { error = it.message }
        runCatching { withContext(Dispatchers.IO) { api.folders() } }.onSuccess { folders.replace(it) }
        if (activeSearch.isBlank()) {
            runCatching {
                withContext(Dispatchers.IO) { api.messagePage(folder, 1) }
            }.onSuccess { page ->
                val pageMessages = page.getJSONArray("messages")
                messages.replace(pageMessages)
                currentPage = page.optInt("page", 1)
                val total = page.optInt("total", pageMessages.length())
                val pageSize = page.optInt("pageSize", pageMessages.length())
                hasMoreMessages = currentPage * pageSize < total
            }.onFailure { error = it.message }
        } else {
            runCatching { withContext(Dispatchers.IO) { api.searchMessages(folder, activeSearch) } }
                .onSuccess {
                    messages.replace(it)
                    currentPage = 1
                    hasMoreMessages = false
                }.onFailure { error = it.message }
        }
        loading = false
    }
    fun loadMoreMessages() {
        if (loadingMore || activeSearch.isNotBlank() || !hasMoreMessages) return
        loadingMore = true
        Thread {
            runCatching { api.messagePage(folder, currentPage + 1) }
                .onSuccess { page ->
                    runOnMain {
                        val pageMessages = page.getJSONArray("messages")
                        repeat(pageMessages.length()) { messages.add(pageMessages.getJSONObject(it)) }
                        currentPage = page.optInt("page", currentPage + 1)
                        val total = page.optInt("total", messages.size)
                        val pageSize = page.optInt("pageSize", pageMessages.length())
                        hasMoreMessages = currentPage * pageSize < total
                        loadingMore = false
                    }
                }
                .onFailure { runOnMain { error = it.message; loadingMore = false } }
        }.start()
    }
    fun replaceMessage(uid: Int, patch: JSONObject.() -> Unit) {
        val index = messages.indexOfFirst { it.optInt("uid") == uid }
        if (index >= 0) {
            messages[index] = JSONObject(messages[index].toString()).apply(patch)
        }
    }
    fun runMessageAction(message: JSONObject, action: String, removeFromList: Boolean = false, patch: (JSONObject.() -> Unit)? = null) {
        val uid = message.optInt("uid")
        Thread {
            runCatching { api.action(folder, uid, action) }
                .onSuccess {
                    runOnMain {
                        if (removeFromList) messages.remove(message)
                        patch?.let { replaceMessage(uid, it) }
                        refreshNonce++
                    }
                }
                .onFailure { runOnMain { error = it.message } }
        }.start()
    }
    fun runBulkMessageAction(action: String, removeFromList: Boolean = false, targetFolder: String? = null, patch: (JSONObject.() -> Unit)? = null) {
        val uids = selectedMessageUids.toList()
        if (uids.isEmpty()) return
        Thread {
            runCatching { api.action(folder, uids, action, targetFolder) }
                .onSuccess {
                    runOnMain {
                        if (removeFromList) messages.removeAll { selectedMessageUids.contains(it.optInt("uid")) }
                        patch?.let {
                            uids.forEach { uid -> replaceMessage(uid, patch) }
                        }
                        selectedMessageUids = emptySet()
                        selectingMessages = false
                        movingSelected = false
                        refreshNonce++
                    }
                }
                .onFailure { runOnMain { error = it.message } }
        }.start()
    }
    if (addingAccount) {
        AccountEditorScreen(
            api = api,
            close = { addingAccount = false },
            done = { account ->
                accounts.add(account)
                selectedAccount = account.accountLabel()
                addingAccount = false
            },
        )
        return
    }
    editingAccount?.let { account ->
        AccountEditorScreen(
            api = api,
            account = account,
            close = { editingAccount = null },
            done = { updated ->
                val index = accounts.indexOfFirst { it.optString("id") == updated.optString("id") }
                if (index >= 0) accounts[index] = updated
                selectedAccount = updated.accountLabel()
                viewingAccount = updated
                editingAccount = null
            },
        )
        return
    }
    viewingAccount?.let { account ->
        AccountDetailScreen(
            api = api,
            account = account,
            back = { viewingAccount = null },
            edit = {
                editingAccount = account
                viewingAccount = null
            },
            deleted = {
                accounts.removeAll { it.optString("id") == account.optString("id") }
                if (selectedAccount == account.accountLabel()) selectedAccount = null
                viewingAccount = null
            },
        )
        return
    }
    selected?.let {
        MessageDetailScreen(
            api = api,
            summary = it,
            folder = folder,
            folders = folders,
            back = { selected = null },
            compose = { draft ->
                selected = null
                compose(draft)
            },
            afterAction = {
                messages.remove(it)
                selected = null
            },
            afterPatch = { patch ->
                replaceMessage(it.optInt("uid"), patch)
                selected = JSONObject(it.toString()).apply(patch)
            },
        )
        return
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { drawerOpen = !drawerOpen }) {
                        Icon(Icons.Default.Menu, "Mail menu")
                    }
                    Column(Modifier.weight(1f)) {
                        Text(folder, style = MaterialTheme.typography.headlineSmall)
                        selectedAccount?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                    Button(onClick = {
                        selectingMessages = !selectingMessages
                        selectedMessageUids = emptySet()
                        movingSelected = false
                    }) { Text(if (selectingMessages) "Cancel" else "Select") }
                    Button(onClick = { refreshNonce++ }) { Text("Refresh") }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        searchQuery,
                        { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Search this folder") },
                    )
                    if (searchQuery.isNotBlank()) {
                        Button(onClick = { searchQuery = "" }) { Text("Clear") }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MailFilter.entries.forEach { filter ->
                        Button(
                            onClick = {
                                mailFilter = filter
                                selectedMessageUids = emptySet()
                                movingSelected = false
                            },
                            enabled = mailFilter != filter,
                        ) {
                            Text(filter.label)
                        }
                    }
                }
            }
            if (loading) item { Text("Loading mail...") }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (!loading && visibleMessages.isEmpty()) {
                item {
                    Text(
                        when {
                            messages.isEmpty() && activeSearch.isBlank() -> "No messages"
                            messages.isEmpty() -> "No messages match \"$activeSearch\""
                            else -> "No ${mailFilter.label.lowercase()} messages"
                        },
                    )
                }
            }
            if (selectingMessages && selectedMessageUids.isNotEmpty()) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { runBulkMessageAction("markSeen", patch = { put("seen", true) }) }) {
                            Text("Read")
                        }
                        Button(onClick = { runBulkMessageAction("markUnseen", patch = { put("seen", false) }) }) {
                            Text("Unread")
                        }
                        Button(onClick = { runBulkMessageAction("flag", patch = { put("flagged", true) }) }) {
                            Text("Star")
                        }
                        Button(onClick = { runBulkMessageAction("unflag", patch = { put("flagged", false) }) }) {
                            Text("Unstar")
                        }
                        Button(onClick = { movingSelected = !movingSelected }) {
                            Text(if (movingSelected) "Cancel move" else "Move")
                        }
                        Button(onClick = { runBulkMessageAction("delete", removeFromList = true) }) {
                            Text("Delete (${selectedMessageUids.size})")
                        }
                    }
                }
                if (movingSelected) {
                    item { Text("Move selected to", style = MaterialTheme.typography.titleMedium) }
                    items(folders.filter { it.optBoolean("selectable", true) && it.optString("path") != folder }) { target ->
                        val targetPath = target.optString("path")
                        Button(
                            onClick = { runBulkMessageAction("move", removeFromList = true, targetFolder = targetPath) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(target.optString("name").ifBlank { targetPath })
                        }
                    }
                }
            }
            items(visibleMessages) { message ->
                val seen = message.optBoolean("seen")
                val flagged = message.optBoolean("flagged")
                val hasAttachments = message.optBoolean("hasAttachments")
                val uid = message.optInt("uid")
                val selectedForBulk = selectedMessageUids.contains(uid)
                Card(
                    onClick = {
                        if (selectingMessages) {
                            selectedMessageUids =
                                if (selectedForBulk) selectedMessageUids - uid else selectedMessageUids + uid
                        } else {
                            selected = message
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (selectingMessages) {
                                Text(if (selectedForBulk) "✓" else "○", color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                message.optString("subject", "(No subject)"),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (seen) FontWeight.Normal else FontWeight.Bold,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (hasAttachments) Text("ATT", color = MaterialTheme.colorScheme.primary)
                                if (flagged) Text("★", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(message.optJSONArray("from")?.optJSONObject(0)?.optString("address") ?: "")
                        Text(message.optString("preview"))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    runMessageAction(
                                        message,
                                        if (seen) "markUnseen" else "markSeen",
                                        patch = { put("seen", !seen) },
                                    )
                                },
                            ) { Text(if (seen) "Unread" else "Read") }
                            Button(
                                onClick = {
                                    runMessageAction(
                                        message,
                                        if (flagged) "unflag" else "flag",
                                        patch = { put("flagged", !flagged) },
                                    )
                                },
                            ) { Text(if (flagged) "Unstar" else "Star") }
                            Button(onClick = { runMessageAction(message, "markSpam", removeFromList = true) }) {
                                Text("Spam")
                            }
                        }
                    }
                }
            }
            if (hasMoreMessages && activeSearch.isBlank()) {
                item {
                    Button(
                        onClick = { loadMoreMessages() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (loadingMore) "Loading..." else "Load more")
                    }
                }
            }
        }
        if (drawerOpen) {
            MailDrawer(
                accounts = accounts,
                folders = folders,
                selectedFolder = folder,
                onClose = { drawerOpen = false },
                onAccountSelected = {
                    selectedAccount = it.accountLabel()
                    viewingAccount = it
                    drawerOpen = false
                },
                onAddAccount = {
                    drawerOpen = false
                    addingAccount = true
                },
                onFolderSelected = {
                    folder = it
                    selected = null
                    searchQuery = ""
                    selectedMessageUids = emptySet()
                    selectingMessages = false
                    messages.clear()
                    drawerOpen = false
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        FloatingActionButton(
            onClick = { compose(ComposeDraft()) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 40.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "Compose")
        }
    }
}

@Composable
private fun MailDrawer(
    accounts: List<JSONObject>,
    folders: List<JSONObject>,
    selectedFolder: String,
    onClose: () -> Unit,
    onAccountSelected: (JSONObject) -> Unit,
    onAddAccount: () -> Unit,
    onFolderSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Card(Modifier.fillMaxWidth(0.82f).fillMaxSize().padding(8.dp)) {
            LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Menu, "Close mail menu")
                    }
                }
                item { Text("Accounts", style = MaterialTheme.typography.titleLarge) }
                item {
                    Button(onClick = onAddAccount, modifier = Modifier.fillMaxWidth()) {
                        Text("Add mail account")
                    }
                }
                if (accounts.isEmpty()) {
                    item { Text("Current jmail account") }
                } else {
                    items(accounts) { account ->
                        Button(
                            onClick = { onAccountSelected(account) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column {
                                Text(account.optString("displayName").ifBlank { account.optString("email") })
                                Text(
                                    "${account.optString("protocol").uppercase()} · ${account.optString("status")}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
                item { Text("Folders", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp)) }
                items(folders) { entry ->
                    val path = entry.optString("path")
                    Button(
                        onClick = { onFolderSelected(path) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (path == selectedFolder) {
                                "✓ ${entry.optString("name").ifBlank { path }}"
                            } else {
                                entry.optString("name").ifBlank { path }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountDetailScreen(
    api: JmailApi,
    account: JSONObject,
    back: () -> Unit,
    edit: () -> Unit,
    deleted: () -> Unit,
) {
    var confirmingDelete by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    val settings = account.optJSONObject("settings")
    val capabilities = account.optJSONObject("capabilities")

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(back, enabled = !deleting) { Text("Back") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!confirmingDelete) {
                        Button(onClick = edit, enabled = !deleting) { Text("Edit") }
                        Button(onClick = { confirmingDelete = true }, enabled = !deleting) { Text("Remove") }
                    }
                }
            }
        }
        item { Text(account.accountLabel(), style = MaterialTheme.typography.headlineSmall) }
        account.cleanString("email")?.let { item { AccountInfoRow("Email", it) } }
        account.cleanString("displayName")?.let { item { AccountInfoRow("Display name", it) } }
        item { AccountInfoRow("Status", account.optString("status").ifBlank { "unknown" }) }
        account.cleanString("lastError")?.let { item { AccountInfoRow("Last error", it) } }
        item { AccountInfoRow("Protocol", account.optString("protocol").uppercase()) }
        item { AccountInfoRow("Auth", account.optString("authType").ifBlank { "unknown" }) }
        account.cleanString("username")?.let { item { AccountInfoRow("Username", it) } }
        account.cleanString("imapHost")?.let { host ->
            item { AccountInfoRow("Incoming host", hostWithPort(host, account.optInt("imapPort"))) }
        }
        account.cleanString("smtpHost")?.let { host ->
            item { AccountInfoRow("SMTP host", hostWithPort(host, account.optInt("smtpPort"))) }
        }
        if (settings != null) {
            item { Text("Settings", style = MaterialTheme.typography.titleMedium) }
            item { AccountInfoRow("Notifications", if (settings.optBoolean("notifications", true)) "On" else "Off") }
            item { AccountInfoRow("Sync interval", "${settings.optInt("syncIntervalMinutes", 15)} minutes") }
            item { AccountInfoRow("Remote images", if (settings.optBoolean("showRemoteImages")) "Shown" else "Blocked") }
            item { AccountInfoRow("Leave on server", if (settings.optBoolean("leaveOnServer", true)) "Yes" else "No") }
        }
        if (capabilities != null) {
            item { Text("Capabilities", style = MaterialTheme.typography.titleMedium) }
            item {
                Text(
                    listOf("folders", "push", "rules", "vacation")
                        .filter { capabilities.optBoolean(it) }
                        .joinToString(", ")
                        .ifBlank { "None advertised" },
                )
            }
        }
        if (confirmingDelete) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Remove this mail account?", style = MaterialTheme.typography.titleMedium)
                        Text("This removes the account configuration from jmail. It does not delete messages from the mail server.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { confirmingDelete = false }, enabled = !deleting) { Text("Cancel") }
                            Button(
                                enabled = !deleting,
                                onClick = {
                                    deleting = true
                                    status = "Removing account..."
                                    Thread {
                                        runCatching { api.deleteAccount(account.optString("id")) }
                                            .onSuccess { runOnMain { deleted() } }
                                            .onFailure {
                                                runOnMain {
                                                    deleting = false
                                                    status = it.message
                                                }
                                            }
                                    }.start()
                                },
                            ) { Text("Remove account") }
                        }
                    }
                }
            }
        }
        status?.let {
            item {
                Text(
                    it,
                    color = if (it == "Removing account...") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun AccountInfoRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

@Composable
private fun AccountEditorScreen(
    api: JmailApi,
    account: JSONObject? = null,
    close: () -> Unit,
    done: (JSONObject) -> Unit,
) {
    val accountId = account?.cleanString("id")
    val accountSettings = account?.optJSONObject("settings")
    var email by remember(accountId) { mutableStateOf(account?.cleanString("email").orEmpty()) }
    var displayName by remember(accountId) { mutableStateOf(account?.cleanString("displayName").orEmpty()) }
    var protocol by remember(accountId) { mutableStateOf(account?.cleanString("protocol") ?: "imap") }
    var authType by remember(accountId) { mutableStateOf(account?.cleanString("authType") ?: "keycloak") }
    var incomingHost by remember(accountId) { mutableStateOf(account?.cleanString("imapHost") ?: "mail.jwenzel.net") }
    var incomingPort by remember(accountId) { mutableStateOf(account?.optInt("imapPort")?.takeIf { it > 0 }?.toString() ?: "993") }
    var smtpHost by remember(accountId) { mutableStateOf(account?.cleanString("smtpHost") ?: "mail.jwenzel.net") }
    var smtpPort by remember(accountId) { mutableStateOf(account?.optInt("smtpPort")?.takeIf { it > 0 }?.toString() ?: "587") }
    var username by remember(accountId) { mutableStateOf(account?.cleanString("username").orEmpty()) }
    var secret by remember { mutableStateOf("") }
    var notifications by remember(accountId) { mutableStateOf(accountSettings?.optBoolean("notifications", true) ?: true) }
    var leaveOnServer by remember(accountId) { mutableStateOf(accountSettings?.optBoolean("leaveOnServer", true) ?: true) }
    var status by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val editing = accountId != null

    fun selectProtocol(next: String) {
        protocol = next
        incomingPort = if (next == "imap") "993" else "995"
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(close, enabled = !saving) { Text("Cancel") }
                Button(
                    enabled = !saving,
                    onClick = {
                        val emailText = email.trim()
                        val incomingPortNumber = incomingPort.toIntOrNull()
                        val smtpPortNumber = smtpPort.toIntOrNull()
                        val usernameText = username.trim().ifBlank { emailText }
                        when {
                            emailText.isBlank() || !emailText.contains("@") -> {
                                status = "Enter a valid email address."
                                return@Button
                            }
                            incomingHost.isBlank() -> {
                                status = "Enter an incoming mail host."
                                return@Button
                            }
                            incomingPortNumber == null || incomingPortNumber <= 0 -> {
                                status = "Enter a valid incoming port."
                                return@Button
                            }
                            smtpHost.isBlank() -> {
                                status = "Enter an SMTP host."
                                return@Button
                            }
                            smtpPortNumber == null || smtpPortNumber <= 0 -> {
                                status = "Enter a valid SMTP port."
                                return@Button
                            }
                            usernameText.isBlank() -> {
                                status = "Enter a username."
                                return@Button
                            }
                            !editing && authType == "password" && secret.isBlank() -> {
                                status = "Enter the account password or app password."
                                return@Button
                            }
                        }
                        val body = JSONObject()
                            .put("email", emailText)
                            .put("displayName", displayName.trim().ifBlank { JSONObject.NULL })
                            .put("protocol", protocol)
                            .put("authType", authType)
                            .put("imapHost", incomingHost.trim())
                            .put("imapPort", incomingPortNumber)
                            .put("smtpHost", smtpHost.trim())
                            .put("smtpPort", smtpPortNumber)
                            .put("username", usernameText)
                            .put(
                                "settings",
                                JSONObject()
                                    .put("notifications", notifications)
                                    .put("syncIntervalMinutes", 15)
                                    .put("signature", "")
                                    .put("showRemoteImages", false)
                                    .put("leaveOnServer", leaveOnServer)
                                    .put("archiveFolder", JSONObject.NULL)
                                    .put("sentFolder", JSONObject.NULL)
                                    .put("trashFolder", JSONObject.NULL)
                                    .put("junkFolder", JSONObject.NULL),
                            )
                        if (authType == "password" && secret.isNotBlank()) body.put("secret", secret)
                        saving = true
                        status = if (editing) "Saving account..." else "Adding account..."
                        Thread {
                            runCatching {
                                if (editing) api.updateAccount(accountId, body) else api.addAccount(body)
                            }
                                .onSuccess { runOnMain { done(it) } }
                                .onFailure {
                                    runOnMain {
                                        saving = false
                                        status = it.message
                                    }
                                }
                        }.start()
                    },
                ) { Text("Save") }
            }
        }
        item { Text(if (editing) "Edit mail account" else "Add mail account", style = MaterialTheme.typography.headlineSmall) }
        item { OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email address") }) }
        item { OutlinedTextField(displayName, { displayName = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Display name") }) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { selectProtocol("imap") }, enabled = protocol != "imap", modifier = Modifier.weight(1f)) {
                    Text("IMAP")
                }
                Button(onClick = { selectProtocol("pop3") }, enabled = protocol != "pop3", modifier = Modifier.weight(1f)) {
                    Text("POP3")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { authType = "keycloak" }, enabled = authType != "keycloak", modifier = Modifier.weight(1f)) {
                    Text("Keycloak")
                }
                Button(onClick = { authType = "password" }, enabled = authType != "password", modifier = Modifier.weight(1f)) {
                    Text("Password")
                }
            }
        }
        item { OutlinedTextField(incomingHost, { incomingHost = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(if (protocol == "imap") "IMAP host" else "POP3 host") }) }
        item { OutlinedTextField(incomingPort, { incomingPort = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(if (protocol == "imap") "IMAP port" else "POP3 port") }) }
        item { OutlinedTextField(smtpHost, { smtpHost = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("SMTP host") }) }
        item { OutlinedTextField(smtpPort, { smtpPort = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("SMTP port") }) }
        item { OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Username") }, placeholder = { Text("Defaults to email address") }) }
        if (authType == "password") {
            item {
                OutlinedTextField(
                    secret,
                    { secret = it },
                    Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Password or app password") },
                    placeholder = { if (editing) Text("Leave blank to keep current secret") },
                )
            }
        } else {
            item { Text("Keycloak auth uses your signed-in jmail identity. No account password is stored on this device.") }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Enable notifications")
                Switch(checked = notifications, onCheckedChange = { notifications = it })
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Leave mail on server")
                Switch(checked = leaveOnServer, onCheckedChange = { leaveOnServer = it })
            }
        }
        status?.let {
            item {
                Text(
                    it,
                    color = if (it == "Adding account..." || it == "Saving account...") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageDetailScreen(
    api: JmailApi,
    summary: JSONObject,
    folder: String,
    folders: List<JSONObject>,
    back: () -> Unit,
    compose: (ComposeDraft) -> Unit,
    afterAction: () -> Unit,
    afterPatch: (JSONObject.() -> Unit) -> Unit,
) {
    var detail by remember { mutableStateOf<JSONObject?>(null) }
    var moving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var attachmentStatus by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val uid = summary.optInt("uid")

    LaunchedEffect(uid, folder) {
        runCatching { withContext(Dispatchers.IO) { api.message(folder, uid) } }
            .onSuccess { detail = it }
            .onFailure { error = it.message }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.optString("subject") ?: summary.optString("subject", "Message")) },
                navigationIcon = {
                    IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            val message = detail
            if (message == null) {
                item { Text("Loading message...") }
            } else {
                item { Text(addressLine("From", message.optJSONArray("from"))) }
                item { Text(addressLine("To", message.optJSONArray("to"))) }
                item { Text(message.optString("date")) }
                attachmentStatus?.let { item { Text(it, color = MaterialTheme.colorScheme.primary) } }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val seen = message.optBoolean("seen")
                        val flagged = message.optBoolean("flagged")
                        Button(onClick = { compose(replyDraft(message, folder, uid)) }) { Text("Reply") }
                        Button(onClick = { compose(forwardDraft(message)) }) { Text("Forward") }
                        Button(onClick = {
                            Thread {
                                runCatching { api.action(folder, uid, if (seen) "markUnseen" else "markSeen") }
                                    .onSuccess {
                                        runOnMain {
                                            val patch: JSONObject.() -> Unit = { put("seen", !seen) }
                                            detail = JSONObject(message.toString()).apply(patch)
                                            afterPatch(patch)
                                        }
                                    }
                                    .onFailure { runOnMain { error = it.message } }
                            }.start()
                        }) { Text(if (seen) "Unread" else "Read") }
                        Button(onClick = {
                            Thread {
                                runCatching { api.action(folder, uid, if (flagged) "unflag" else "flag") }
                                    .onSuccess {
                                        runOnMain {
                                            val patch: JSONObject.() -> Unit = { put("flagged", !flagged) }
                                            detail = JSONObject(message.toString()).apply(patch)
                                            afterPatch(patch)
                                        }
                                    }
                                    .onFailure { runOnMain { error = it.message } }
                            }.start()
                        }) { Text(if (flagged) "Unstar" else "Star") }
                        Button(onClick = { moving = !moving }) { Text(if (moving) "Cancel move" else "Move") }
                        Button(onClick = {
                            Thread {
                                runCatching { api.action(folder, uid, "delete") }
                                    .onSuccess { runOnMain(afterAction) }
                                    .onFailure { runOnMain { error = it.message } }
                            }.start()
                        }) { Text("Delete") }
                    }
                }
                if (moving) {
                    item { Text("Move to", style = MaterialTheme.typography.titleMedium) }
                    items(folders.filter { it.optBoolean("selectable", true) && it.optString("path") != folder }) { target ->
                        val targetPath = target.optString("path")
                        Button(
                            onClick = {
                                Thread {
                                    runCatching { api.action(folder, uid, "move", targetPath) }
                                        .onSuccess { runOnMain(afterAction) }
                                        .onFailure { runOnMain { error = it.message } }
                                }.start()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(target.optString("name").ifBlank { targetPath })
                        }
                    }
                }
                val attachments = message.optJSONArray("attachments")
                if (attachments != null && attachments.length() > 0) {
                    item { Text("Attachments", style = MaterialTheme.typography.titleMedium) }
                    items(List(attachments.length()) { attachments.getJSONObject(it) }) { attachment ->
                        val partId = attachment.optString("partId")
                        val filename = attachment.optString("filename").ifBlank { "Attachment $partId" }
                        val contentType = attachment.optString("contentType").ifBlank { "application/octet-stream" }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(filename, style = MaterialTheme.typography.titleSmall)
                                Text("$contentType · ${formatBytes(attachment.optLong("size"))}")
                                Button(onClick = {
                                    attachmentStatus = "Downloading $filename..."
                                    Thread {
                                        runCatching { api.downloadAttachment(folder, uid, partId) }
                                            .onSuccess { bytes ->
                                                runOnMain {
                                                    runCatching {
                                                        shareAttachment(context, filename, contentType, bytes)
                                                    }.onSuccess {
                                                        attachmentStatus = "Downloaded $filename (${formatBytes(bytes.size.toLong())})"
                                                    }.onFailure {
                                                        error = it.message
                                                    }
                                                }
                                            }
                                            .onFailure { runOnMain { error = it.message } }
                                    }.start()
                                }) { Text("Download") }
                            }
                        }
                    }
                }
                item {
                    SelectionContainer {
                        Text(message.optString("text").ifBlank { stripHtml(message.optString("html")) })
                    }
                }
            }
        }
    }
}

private data class ComposeDraft(
    val to: String = "",
    val cc: String = "",
    val bcc: String = "",
    val subject: String = "",
    val body: String = "",
    val inReplyToUid: Int? = null,
    val inReplyToFolder: String? = null,
)

@Composable
private fun ComposeScreen(api: JmailApi, draft: ComposeDraft, close: () -> Unit) {
    var to by remember { mutableStateOf(draft.to) }
    var cc by remember { mutableStateOf(draft.cc) }
    var bcc by remember { mutableStateOf(draft.bcc) }
    var subject by remember { mutableStateOf(draft.subject) }
    var body by remember { mutableStateOf(draft.body) }
    var status by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(close, enabled = !sending) { Text("Cancel") }
            Button(
                enabled = !sending,
                onClick = {
                    val toText = to.trim()
                    val subjectText = subject.trim()
                    val bodyText = body.trim()
                    if (toText.isBlank()) {
                        status = "Enter at least one recipient."
                        return@Button
                    }
                    if (subjectText.isBlank() && bodyText.isBlank()) {
                        status = "Enter a subject or message body."
                        return@Button
                    }
                    sending = true
                    status = "Sending..."
                    Thread {
                        runCatching {
                            api.send(
                                to = toText,
                                cc = cc,
                                bcc = bcc,
                                subject = subjectText,
                                text = bodyText,
                                inReplyToUid = draft.inReplyToUid,
                                inReplyToFolder = draft.inReplyToFolder,
                            )
                        }
                            .onSuccess { runOnMain { close() } }
                            .onFailure {
                                runOnMain {
                                    sending = false
                                    status = it.message
                                }
                            }
                    }.start()
                }
            ) { Text("Send") }
        }
        OutlinedTextField(to, { to = it }, Modifier.fillMaxWidth(), label = { Text("To") })
        Text(
            "Separate multiple recipients with commas or semicolons.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(cc, { cc = it }, Modifier.fillMaxWidth(), label = { Text("Cc") })
        OutlinedTextField(bcc, { bcc = it }, Modifier.fillMaxWidth(), label = { Text("Bcc") })
        OutlinedTextField(subject, { subject = it }, Modifier.fillMaxWidth(), label = { Text("Subject") })
        OutlinedTextField(body, { body = it }, Modifier.fillMaxWidth(), minLines = 10, label = { Text("Message") })
        status?.let { Text(it, color = if (it == "Sending...") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun ContactsScreen(api: JmailApi, compose: (ComposeDraft) -> Unit) {
    val rows = remember { mutableStateListOf<JSONObject>() }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<JSONObject?>(null) }
    var selectedContact by remember { mutableStateOf<JSONObject?>(null) }
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var query by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val activeQuery = query.trim()
    LaunchedEffect(activeQuery) {
        selectedIds = emptySet()
        selecting = false
        runCatching { withContext(Dispatchers.IO) { api.contacts(activeQuery) } }
            .onSuccess { rows.replace(it) }
            .onFailure { error = it.message }
    }
    if (adding) {
        ContactEditor(api, close = { adding = false }) {
            adding = false
            rows.add(it)
        }
        return
    }
    editing?.let { contact ->
        ContactEditor(api, contact = contact, close = { editing = null }) { updated ->
            editing = null
            selectedContact = updated
            val index = rows.indexOfFirst { it.optString("id") == updated.optString("id") }
            if (index >= 0) rows[index] = updated
        }
        return
    }
    selectedContact?.let { contact ->
        ContactDetail(
            contact = contact,
            back = { selectedContact = null },
            edit = { editing = contact },
            email = { address -> compose(ComposeDraft(to = address)) },
        )
        return
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Contacts", style = MaterialTheme.typography.headlineSmall)
                    Button(onClick = {
                        selecting = !selecting
                        selectedIds = emptySet()
                    }) { Text(if (selecting) "Cancel" else "Select") }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        query,
                        { query = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Search contacts") },
                    )
                    if (query.isNotBlank()) {
                        Button(onClick = { query = "" }) { Text("Clear") }
                    }
                }
            }
            if (selecting && selectedIds.isNotEmpty()) {
                item {
                    Button(onClick = {
                        val ids = selectedIds
                        Thread {
                            ids.forEach { id -> runCatching { api.deleteContact(id) } }
                            runOnMain {
                                rows.removeAll { ids.contains(it.optString("id")) }
                                selectedIds = emptySet()
                                selecting = false
                            }
                        }.start()
                    }) { Text("Delete selected (${selectedIds.size})") }
                }
            }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (rows.isEmpty()) {
                item { Text(if (activeQuery.isBlank()) "No contacts" else "No contacts match \"$activeQuery\"") }
            }
            items(rows) { contact ->
                val id = contact.optString("id")
                Card(
                    onClick = {
                        if (selecting) {
                            selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
                        } else {
                            selectedContact = contact
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (selecting) Text(if (selectedIds.contains(id)) "✓" else "○")
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(contact.optString("displayName"), style = MaterialTheme.typography.titleMedium)
                            Text(contact.optString("email"))
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 40.dp),
        ) {
            Icon(Icons.Default.Add, "Add contact")
        }
    }
}

@Composable
private fun ContactDetail(contact: JSONObject, back: () -> Unit, edit: () -> Unit, email: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = back) { Text("Back") }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(contact.optString("displayName"), style = MaterialTheme.typography.headlineSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        contact.cleanString("email")?.let { address ->
                            Button(onClick = { email(address) }) { Text("Email") }
                        }
                        Button(onClick = edit) { Text("Edit") }
                    }
                }
                contact.cleanString("email")?.let { Text("Email: $it") }
                contact.cleanString("phone")?.let { Text("Phone: $it") }
                contact.cleanString("company")?.let { Text("Company: $it") }
                contact.cleanString("notes")?.let { Text(it) }
            }
        }
    }
}

@Composable
private fun ContactEditor(api: JmailApi, contact: JSONObject? = null, close: () -> Unit, done: (JSONObject) -> Unit) {
    var name by remember { mutableStateOf(contact?.cleanString("displayName").orEmpty()) }
    var email by remember { mutableStateOf(contact?.cleanString("email").orEmpty()) }
    var phone by remember { mutableStateOf(contact?.cleanString("phone").orEmpty()) }
    var company by remember { mutableStateOf(contact?.cleanString("company").orEmpty()) }
    var notes by remember { mutableStateOf(contact?.cleanString("notes").orEmpty()) }
    var status by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (contact == null) "New contact" else "Edit contact", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") })
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") })
        OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Phone") })
        OutlinedTextField(company, { company = it }, Modifier.fillMaxWidth(), label = { Text("Company") })
        OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), minLines = 3, label = { Text("Notes") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = close) { Text("Cancel") }
            Button(onClick = {
                status = "Saving..."
                Thread {
                    runCatching {
                        if (contact == null) api.createContact(name, email, phone, company, notes)
                        else api.updateContact(contact.optString("id"), name, email, phone, company, notes)
                    }
                        .onSuccess { runOnMain { done(it) } }
                        .onFailure { runOnMain { status = it.message } }
                }.start()
            }) { Text("Save") }
        }
        status?.let { Text(it, color = if (it == "Saving...") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun CalendarScreen(api: JmailApi) {
    val rows = remember { mutableStateListOf<JSONObject>() }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<JSONObject?>(null) }
    var mode by remember { mutableStateOf(CalendarMode.Month) }
    var anchor by remember { mutableStateOf(LocalDate.now()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        runCatching {
            withContext(Dispatchers.IO) {
                api.events(
                    Instant.now().minus(30, ChronoUnit.DAYS).toString(),
                    Instant.now().plus(365, ChronoUnit.DAYS).toString(),
                )
            }
        }.onSuccess { rows.replace(it) }.onFailure { error = it.message }
    }
    if (adding) {
        EventEditor(api, initialDay = anchor, close = { adding = false }) {
            adding = false
            rows.add(it)
        }
        return
    }
    editing?.let { event ->
        EventEditor(api, event = event, close = { editing = null }) { updated ->
            editing = null
            val index = rows.indexOfFirst { it.optString("id") == updated.optString("id") }
            if (index >= 0) rows[index] = updated else rows.add(updated)
        }
        return
    }
    val events = rows.sortedBy { it.optString("startsAt") }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                CalendarHeader(
                    mode = mode,
                    anchor = anchor,
                    onMode = {
                        mode = it
                        if (it == CalendarMode.Agenda) anchor = LocalDate.now()
                    },
                    onPrevious = { anchor = calendarShift(anchor, mode, -1) },
                    onToday = { anchor = LocalDate.now() },
                    onNext = { anchor = calendarShift(anchor, mode, 1) },
                )
            }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (events.isEmpty()) item { Text("No events") }
            when (mode) {
                CalendarMode.Agenda -> eventsForDay(events, LocalDate.now()).forEach { event ->
                    item { CalendarEventCard(event, api, rows, onEdit = { editing = it }) { error = it } }
                }
                CalendarMode.Week -> weekDays(anchor).forEach { day ->
                    item { DayColumn(day, eventsForDay(events, day), api, rows, onEdit = { editing = it }) { error = it } }
                }
                CalendarMode.Month -> item {
                    MonthCalendarGrid(
                        anchor = anchor,
                        events = events,
                        selectDay = { anchor = it; mode = CalendarMode.Agenda },
                        onEdit = { editing = it },
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 40.dp),
        ) {
            Icon(Icons.Default.Add, "Add event")
        }
    }
}

private enum class CalendarMode { Agenda, Week, Month }

@Composable
private fun CalendarHeader(
    mode: CalendarMode,
    anchor: LocalDate,
    onMode: (CalendarMode) -> Unit,
    onPrevious: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(calendarTitle(anchor, mode), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onMode(CalendarMode.Agenda) }) { Text("Agenda") }
            Button(onClick = { onMode(CalendarMode.Week) }) { Text("Week") }
            Button(onClick = { onMode(CalendarMode.Month) }) { Text("Month") }
        }
        if (mode != CalendarMode.Agenda) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onPrevious, modifier = Modifier.weight(1f)) { Text("‹") }
                Button(onClick = onToday, modifier = Modifier.weight(1.4f).padding(horizontal = 8.dp)) { Text("Today") }
                Button(onClick = onNext, modifier = Modifier.weight(1f)) { Text("›") }
            }
        }
    }
}

@Composable
private fun DayColumn(
    day: LocalDate,
    events: List<JSONObject>,
    api: JmailApi,
    rows: MutableList<JSONObject>,
    onEdit: (JSONObject) -> Unit,
    onError: (String?) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(day.format(DateTimeFormatter.ofPattern("EEE, MMM d")), style = MaterialTheme.typography.titleMedium)
            if (events.isEmpty()) Text("No events")
            events.forEach { CalendarEventCard(it, api, rows, onEdit, onError) }
        }
    }
}

@Composable
private fun MonthCalendarGrid(
    anchor: LocalDate,
    events: List<JSONObject>,
    selectDay: (LocalDate) -> Unit,
    onEdit: (JSONObject) -> Unit,
) {
    val month = YearMonth.from(anchor)
    val today = LocalDate.now()
    val divider = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    val dim = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    Column(Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.22f))) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        monthWeeks(anchor).forEach { week ->
            Row(Modifier.fillMaxWidth().height(104.dp).border(width = 0.5.dp, color = divider)) {
                week.forEach { day ->
                    val dayEvents = eventsForDay(events, day)
                    val inMonth = YearMonth.from(day) == month
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(width = 0.5.dp, color = divider)
                            .background(if (inMonth) Color.Transparent else Color.Black.copy(alpha = 0.22f))
                            .clickable { selectDay(day) }
                            .padding(5.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (day == today) {
                            Box(
                                Modifier
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 7.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    day.dayOfMonth.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        } else {
                            Text(
                                dayLabel(day),
                                color = if (inMonth) MaterialTheme.colorScheme.onSurface else dim,
                                fontWeight = if (day.dayOfMonth == 1) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                        dayEvents.take(2).forEach { event ->
                            Text(
                                event.optString("title").ifBlank { "Untitled" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEdit(event) }
                                    .background(Color(0xFF8E1B2C))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                color = Color(0xFFFFC2CD),
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarEventCard(
    event: JSONObject,
    api: JmailApi,
    rows: MutableList<JSONObject>,
    onEdit: (JSONObject) -> Unit,
    onError: (String?) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(event.optString("title"), style = MaterialTheme.typography.titleMedium)
            Text(formatEventTime(event))
            event.optString("location").takeIf { it.isNotBlank() }?.let { Text(it) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onEdit(event) }) { Text("Edit") }
                Button(onClick = {
                    Thread {
                        runCatching { api.deleteEvent(event.optString("id")) }
                            .onSuccess { runOnMain { rows.remove(event) } }
                            .onFailure { runOnMain { onError(it.message) } }
                    }.start()
                }) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun EventEditor(api: JmailApi, event: JSONObject? = null, initialDay: LocalDate? = null, close: () -> Unit, done: (JSONObject) -> Unit) {
    val defaultStart = remember(initialDay) {
        if (initialDay == null) {
            Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MINUTES)
        } else {
            initialDay.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant()
        }
    }
    val eventId = event?.optString("id").orEmpty().takeIf { it.isNotBlank() }
    val initialStartsAt = event?.cleanString("startsAt") ?: defaultStart.toString()
    val initialEndsAt = event?.cleanString("endsAt") ?: defaultStart.plus(1, ChronoUnit.HOURS).toString()
    val initialStartLocal = remember(eventId) { eventLocalDateTime(initialStartsAt, defaultStart) }
    val initialEndLocal = remember(eventId) { eventLocalDateTime(initialEndsAt, defaultStart.plus(1, ChronoUnit.HOURS)) }
    var title by remember(eventId) { mutableStateOf(event?.cleanString("title").orEmpty()) }
    var startDate by remember(eventId) { mutableStateOf(initialStartLocal.toLocalDate().toString()) }
    var startTime by remember(eventId) { mutableStateOf(initialStartLocal.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var endDate by remember(eventId) { mutableStateOf(initialEndLocal.toLocalDate().toString()) }
    var endTime by remember(eventId) { mutableStateOf(initialEndLocal.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var location by remember(eventId) { mutableStateOf(event?.cleanString("location").orEmpty()) }
    var status by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (eventId == null) "New event" else "Edit event", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") })
        Text("Start", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(startDate, { startDate = it }, Modifier.weight(1f), singleLine = true, label = { Text("Date") })
            OutlinedTextField(startTime, { startTime = it }, Modifier.weight(1f), singleLine = true, label = { Text("Time") })
        }
        Text("End", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(endDate, { endDate = it }, Modifier.weight(1f), singleLine = true, label = { Text("Date") })
            OutlinedTextField(endTime, { endTime = it }, Modifier.weight(1f), singleLine = true, label = { Text("Time") })
        }
        OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("Location") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = close) { Text("Cancel") }
            Button(onClick = {
                status = "Saving..."
                Thread {
                    runCatching {
                        val startsAt = eventIsoString(startDate, startTime)
                        val endsAt = eventIsoString(endDate, endTime)
                        if (eventId == null) api.createEvent(title, startsAt, endsAt, location)
                        else api.updateEvent(eventId, title, startsAt, endsAt, location)
                    }
                        .onSuccess { runOnMain { done(it) } }
                        .onFailure { runOnMain { status = it.message } }
                }.start()
            }) { Text("Save") }
        }
        Text("Use date format YYYY-MM-DD and 24-hour time HH:mm.", style = MaterialTheme.typography.bodySmall)
        status?.let { Text(it, color = if (it == "Saving...") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun JsonListScreen(api: JmailApi, emptyText: String, loader: () -> JSONArray) {
    val rows = remember { mutableStateListOf<JSONObject>() }
    LaunchedEffect(api) { runCatching { withContext(Dispatchers.IO) { loader() } }.onSuccess { rows.replace(it) } }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (rows.isEmpty()) item { Text(emptyText) }
        items(rows) { row ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(row.optString("displayName", row.optString("title", "Untitled")), style = MaterialTheme.typography.titleMedium)
                    Text(row.optString("email", row.optString("startsAt", "")))
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(api: JmailApi, registerPush: () -> Unit, onThemeChanged: (Boolean) -> Unit, logout: () -> Unit) {
    var darkTheme by remember { mutableStateOf(api.darkTheme) }
    var notificationsEnabled by remember { mutableStateOf(api.notificationsEnabled) }
    var profile by remember { mutableStateOf<JSONObject?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        runCatching { withContext(Dispatchers.IO) { api.me().optJSONObject("user") } }
            .onSuccess { profile = it }
            .onFailure { status = it.message }
    }
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Account and sync settings", style = MaterialTheme.typography.headlineSmall)
        Text("Server, identity, notification, theme, and session controls.")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Server", style = MaterialTheme.typography.titleMedium)
                Text(api.configuredServerUrl ?: "Not configured")
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Signed in as", style = MaterialTheme.typography.titleMedium)
                val user = profile
                if (user == null) {
                    Text("Loading profile...")
                } else {
                    user.cleanString("name")?.let { Text(it) }
                    user.cleanString("email")?.let { Text(it) }
                    user.cleanString("id")?.let { Text("User ID: $it") }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Push notifications", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Receive new mail notifications")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            notificationsEnabled = enabled
                            api.notificationsEnabled = enabled
                            if (enabled) {
                                registerPush()
                                status = "Notification registration requested."
                            } else {
                                status = "Disabling notifications..."
                                Thread {
                                    runCatching { api.unregisterDevice() }
                                        .onSuccess { runOnMain { status = "Notifications disabled for this device." } }
                                        .onFailure { runOnMain { status = it.message } }
                                }.start()
                            }
                        },
                    )
                }
                Text("Re-register this device if notifications stop arriving or after server changes.")
                Button(onClick = {
                    api.notificationsEnabled = true
                    notificationsEnabled = true
                    registerPush()
                    status = "Notification registration requested."
                }) { Text("Re-register device") }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Dark theme", style = MaterialTheme.typography.titleMedium)
                Text("Use dark blue surfaces and light blue accents.")
            }
            Switch(
                checked = darkTheme,
                onCheckedChange = {
                    darkTheme = it
                    api.darkTheme = it
                    onThemeChanged(it)
                },
            )
        }
        status?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Button(logout, modifier = Modifier.padding(top = 24.dp)) { Text("Sign out") }
    }
}

private fun MutableList<JSONObject>.replace(array: JSONArray) {
    clear()
    repeat(array.length()) { add(array.getJSONObject(it)) }
}

private fun JSONObject.cleanString(key: String): String? {
    if (isNull(key)) return null
    return optString(key).takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
}

private fun JSONObject.accountLabel(): String = cleanString("displayName") ?: cleanString("email") ?: "Mail account"

private fun hostWithPort(host: String, port: Int): String = if (port > 0) "$host:$port" else host

private fun addressLine(label: String, addresses: JSONArray?): String {
    if (addresses == null || addresses.length() == 0) return "$label: "
    val values =
        List(addresses.length()) { index ->
            val address = addresses.optJSONObject(index)
            val name = address?.optString("name").orEmpty()
            val email = address?.optString("address").orEmpty()
            if (name.isBlank()) email else "$name <$email>"
        }
    return "$label: ${values.joinToString(", ")}"
}

private fun formatBytes(bytes: Long): String =
    when {
        bytes >= 1024L * 1024L -> "${bytes / (1024L * 1024L)} MB"
        bytes >= 1024L -> "${bytes / 1024L} KB"
        else -> "$bytes B"
    }

private fun shareAttachment(context: Context, filename: String, contentType: String, bytes: ByteArray) {
    val safeName = filename.replace(Regex("[^A-Za-z0-9._ -]"), "_").ifBlank { "attachment" }
    val dir = File(context.cacheDir, "attachments").apply { mkdirs() }
    val file = File(dir, safeName).apply { writeBytes(bytes) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND)
        .setType(contentType.ifBlank { "application/octet-stream" })
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Open attachment"))
}

private fun stripHtml(value: String): String =
    value
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")

private fun calendarShift(anchor: LocalDate, mode: CalendarMode, amount: Long): LocalDate =
    when (mode) {
        CalendarMode.Agenda -> anchor.plusDays(amount)
        CalendarMode.Week -> anchor.plusWeeks(amount)
        CalendarMode.Month -> anchor.plusMonths(amount)
    }

private fun calendarTitle(anchor: LocalDate, mode: CalendarMode): String =
    when (mode) {
        CalendarMode.Agenda -> "Agenda · ${anchor.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
        CalendarMode.Week -> {
            val days = weekDays(anchor)
            "Week · ${days.first().format(DateTimeFormatter.ofPattern("MMM d"))} - ${days.last().format(DateTimeFormatter.ofPattern("MMM d"))}"
        }
        CalendarMode.Month -> YearMonth.from(anchor).format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

private fun weekDays(anchor: LocalDate): List<LocalDate> {
    val start = anchor.minusDays(((anchor.dayOfWeek.value + 6) % 7).toLong())
    return List(7) { start.plusDays(it.toLong()) }
}

private fun monthWeeks(anchor: LocalDate): List<List<LocalDate>> {
    val first = YearMonth.from(anchor).atDay(1)
    val start = first.minusDays(((first.dayOfWeek.value + 6) % 7).toLong())
    return List(6) { week -> List(7) { day -> start.plusDays((week * 7 + day).toLong()) } }
}

private fun dayLabel(day: LocalDate): String =
    if (day.dayOfMonth == 1) day.format(DateTimeFormatter.ofPattern("MMM d"))
    else day.dayOfMonth.toString()

private fun eventsForDay(events: List<JSONObject>, day: LocalDate): List<JSONObject> =
    events.filter { eventDate(it) == day }

private fun eventDate(event: JSONObject): LocalDate =
    runCatching {
        Instant.parse(event.optString("startsAt"))
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }.getOrDefault(LocalDate.now())

private fun eventLocalDateTime(value: String, fallback: Instant): LocalDateTime =
    runCatching {
        Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }.getOrDefault(fallback.atZone(ZoneId.systemDefault()).toLocalDateTime())

private fun eventIsoString(date: String, time: String): String {
    val localDate = LocalDate.parse(date.trim())
    val localTime = LocalTime.parse(time.trim())
    return LocalDateTime.of(localDate, localTime).atZone(ZoneId.systemDefault()).toInstant().toString()
}

private fun formatEventTime(event: JSONObject): String {
    val start = event.optString("startsAt")
    val end = event.optString("endsAt")
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a")
        val startText = Instant.parse(start).atZone(ZoneId.systemDefault()).format(formatter)
        val endText = Instant.parse(end).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a"))
        "$startText - $endText"
    }.getOrDefault(start)
}

private fun replyDraft(message: JSONObject, folder: String, uid: Int): ComposeDraft {
    val from = message.optJSONArray("from")?.optJSONObject(0)?.optString("address").orEmpty()
    val subject = message.optString("subject").let { if (it.startsWith("Re:", true)) it else "Re: $it" }
    val quoted = message.optString("text").ifBlank { stripHtml(message.optString("html")) }
    return ComposeDraft(
        to = from,
        subject = subject,
        body = "\n\nOn ${message.optString("date")}, ${from.ifBlank { "the sender" }} wrote:\n> " +
            quoted.lines().joinToString("\n> "),
        inReplyToUid = uid,
        inReplyToFolder = folder,
    )
}

private fun forwardDraft(message: JSONObject): ComposeDraft {
    val subject = message.optString("subject").let { if (it.startsWith("Fwd:", true)) it else "Fwd: $it" }
    val body = message.optString("text").ifBlank { stripHtml(message.optString("html")) }
    return ComposeDraft(
        subject = subject,
        body = "\n\n---------- Forwarded message ----------\n" +
            "From: ${addressLine("", message.optJSONArray("from")).removePrefix(": ").trim()}\n" +
            "Date: ${message.optString("date")}\n" +
            "Subject: ${message.optString("subject")}\n\n$body",
    )
}
