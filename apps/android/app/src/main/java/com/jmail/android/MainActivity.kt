package com.jmail.android

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.TimePicker
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.jmail.android.data.JmailApi
import com.jmail.android.data.OutgoingAttachment
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
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

// Browsers known to expose USB/NFC FIDO2 security keys to WebAuthn pages. The
// user's default browser still wins when it supports Custom Tabs; this is the
// preference order when it does not.
private val WEBAUTHN_CAPABLE_BROWSERS = listOf(
    "com.android.chrome",
    "com.chrome.beta",
    "com.chrome.dev",
    "com.brave.browser",
    "com.microsoft.emmx",
    "org.mozilla.firefox",
)

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
                JmailApp(session, api, authCode, ::registerPush, onThemeChanged = { darkTheme = it }, login = ::launchLogin)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        authCode = intent.data?.getQueryParameter("code")
    }

    private fun launchLogin() {
        val uri = Uri.parse(api.loginUrl().toString())
        // Roaming security keys (USB/NFC) only reach the OS FIDO2 stack from a
        // real browser. A Custom Tab qualifies; an in-app WebView does not. Pin
        // the tab to a WebAuthn-capable browser when one is available, falling
        // back to the system browser if no Custom Tabs provider is present.
        val browser = CustomTabsClient.getPackageName(this, WEBAUTHN_CAPABLE_BROWSERS, false)
        val tab = CustomTabsIntent.Builder().build()
        if (browser != null) tab.intent.setPackage(browser)
        runCatching { tab.launchUrl(this, uri) }
            .onFailure { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = if (darkTheme) DarkBlueScheme else LightBlueScheme) {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
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
    var exchangingAuthCode by remember { mutableStateOf(false) }
    DisposableEffect(api) {
        api.onSessionExpired = { message ->
            runOnMain {
                error = message
                loggedIn = false
            }
        }
        onDispose { api.onSessionExpired = null }
    }
    LaunchedEffect(Unit) {
        if (session.accessToken != null && !session.isSignedIn) {
            session.clearAccessToken()
            error = "Your mobile session expired. Sign in again."
            loggedIn = false
        }
    }
    LaunchedEffect(authCode) {
        if (authCode != null && session.serverUrl != null && !exchangingAuthCode) {
            exchangingAuthCode = true
            error = null
            runCatching { withContext(Dispatchers.IO) { api.exchange(authCode) } }
                .onSuccess {
                    val token = it.optString("accessToken")
                    val expiresAt = it.optString("expiresAt").takeIf { value -> value.isNotBlank() }
                    if (token.isBlank()) {
                        error = "Login completed, but the server did not return a mobile token."
                    } else {
                        session.saveAccessToken(token, expiresAt)
                        error = null
                        loggedIn = true
                        registerPush()
                    }
                    exchangingAuthCode = false
                }.onFailure {
                    exchangingAuthCode = false
                    error = it.message
                }
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
    var connecting by remember { mutableStateOf(false) }
    LaunchedEffect(error) {
        if (error != null) {
            connecting = false
            status = error
        }
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(32.dp), verticalArrangement = Arrangement.Center) {
        Text("Connect to jmail", style = MaterialTheme.typography.headlineMedium)
        Text("Your organization server provides identity, mail accounts, calendar, contacts, and push.")
        OutlinedTextField(
            server,
            { server = it },
            Modifier.fillMaxWidth().padding(top = 24.dp),
            enabled = !connecting,
            label = { Text("Server URL") },
        )
        Button(onClick = {
            val normalized = normalizeServerUrl(server)
            if (normalized == null) {
                status = "Enter a valid server URL."
                return@Button
            }
            connecting = true
            status = null
            Thread {
                runCatching {
                    api.compatibility(normalized)
                    normalized
                }
                    .onSuccess {
                        session.serverUrl = it
                        runOnMain {
                            runCatching(login).onFailure { failure ->
                                connecting = false
                                status = failure.message ?: "Unable to open browser for sign-in."
                            }
                        }
                    }.onFailure {
                        runOnMain {
                            connecting = false
                            status = it.message
                        }
                    }
            }.start()
        }, enabled = !connecting, modifier = Modifier.padding(top = 16.dp)) {
            Text(if (connecting) "Checking server..." else "Continue with Keycloak")
        }
        status?.let {
            Text(
                it,
                color = if (connecting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var confirmingBulkDelete by remember { mutableStateOf(false) }
    var bulkActionInFlight by remember { mutableStateOf(false) }
    var messageActionUids by remember { mutableStateOf(setOf<Int>()) }
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
    val mailBusy = loading || loadingMore || bulkActionInFlight || messageActionUids.isNotEmpty()
    LaunchedEffect(Unit, folder, refreshNonce, activeSearch) {
        loading = true
        error = null
        runCatching { withContext(Dispatchers.IO) { api.accounts() } }.onSuccess { accounts.replace(it) }.onFailure { error = it.message }
        runCatching { withContext(Dispatchers.IO) { api.folders() } }.onSuccess { folders.replace(it) }
        if (activeSearch.isBlank()) {
            runCatching {
                withContext(Dispatchers.IO) { api.messagePage(folder, 1) }
            }.onSuccess { page ->
                val pageMessages = page.messagePageItems()
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
                        val pageMessages = page.messagePageItems()
                        messages.addAll(pageMessages.objects())
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
        val index = messages.indexOfFirst { it.messageUid() == uid }
        if (index >= 0) {
            messages[index] = JSONObject(messages[index].toString()).apply(patch)
        }
    }
    fun runMessageAction(message: JSONObject, action: String, removeFromList: Boolean = false, patch: (JSONObject.() -> Unit)? = null) {
        val uid = message.messageUid()
        if (uid == null) {
            error = "Message is missing its UID."
            return
        }
        if (messageActionUids.contains(uid)) return
        messageActionUids = messageActionUids + uid
        error = null
        Thread {
            runCatching { api.action(folder, uid, action) }
                .onSuccess {
                    runOnMain {
                        if (removeFromList) messages.removeAll { it.messageUid() == uid }
                        patch?.let { replaceMessage(uid, it) }
                        messageActionUids = messageActionUids - uid
                        refreshNonce++
                    }
                }
                .onFailure {
                    runOnMain {
                        messageActionUids = messageActionUids - uid
                        error = it.message
                    }
                }
        }.start()
    }
    fun runBulkMessageAction(action: String, removeFromList: Boolean = false, targetFolder: String? = null, patch: (JSONObject.() -> Unit)? = null) {
        if (bulkActionInFlight) return
        val uidSet = selectedMessageUids
        val uids = uidSet.toList()
        if (uids.isEmpty()) return
        bulkActionInFlight = true
        error = null
        Thread {
            runCatching { api.action(folder, uids, action, targetFolder) }
                .onSuccess {
                    runOnMain {
                        if (removeFromList) messages.removeAll { it.messageUid() in uidSet }
                        patch?.let {
                            uids.forEach { uid -> replaceMessage(uid, patch) }
                        }
                        bulkActionInFlight = false
                        selectedMessageUids = emptySet()
                        selectingMessages = false
                        movingSelected = false
                        confirmingBulkDelete = false
                        refreshNonce++
                    }
                }
                .onFailure {
                    runOnMain {
                        bulkActionInFlight = false
                        error = it.message
                    }
                }
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
        val selectedUid = it.messageUid()
        if (selectedUid == null) {
            error = "Message is missing its UID."
            selected = null
            return@let
        }
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
                messages.removeAll { message -> message.messageUid() == selectedUid }
                selected = null
            },
            afterPatch = { patch ->
                replaceMessage(selectedUid, patch)
                selected = JSONObject(it.toString()).apply(patch)
            },
        )
        return
    }
    Box(Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = loading,
            onRefresh = { if (!mailBusy) refreshNonce++ },
            modifier = Modifier.fillMaxSize(),
        ) {
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
                        Text(folderDisplayName(folder), style = MaterialTheme.typography.headlineSmall)
                        selectedAccount?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = {
                        selectingMessages = !selectingMessages
                        selectedMessageUids = emptySet()
                        movingSelected = false
                        confirmingBulkDelete = false
                    }, enabled = !mailBusy) {
                        Icon(
                            if (selectingMessages) Icons.Default.Close else Icons.Default.Checklist,
                            if (selectingMessages) "Cancel selection" else "Select messages",
                        )
                    }
                    IconButton(onClick = { refreshNonce++ }, enabled = !mailBusy) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            }
            item {
                OutlinedTextField(
                    searchQuery,
                    { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !mailBusy,
                    shape = RoundedCornerShape(28.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, enabled = !mailBusy) {
                                Icon(Icons.Default.Close, "Clear search")
                            }
                        }
                    },
                    placeholder = { Text("Search this folder") },
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MailFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = mailFilter == filter,
                            onClick = {
                                mailFilter = filter
                                selectedMessageUids = emptySet()
                                movingSelected = false
                                confirmingBulkDelete = false
                            },
                            enabled = !mailBusy,
                            label = { Text(filter.label) },
                        )
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
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${selectedMessageUids.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            BulkAction(Icons.Default.MarkEmailRead, "Mark read", !bulkActionInFlight) {
                                runBulkMessageAction("markSeen", patch = { put("seen", true) })
                            }
                            BulkAction(Icons.Default.MarkEmailUnread, "Mark unread", !bulkActionInFlight) {
                                runBulkMessageAction("markUnseen", patch = { put("seen", false) })
                            }
                            BulkAction(Icons.Default.Star, "Star", !bulkActionInFlight) {
                                runBulkMessageAction("flag", patch = { put("flagged", true) })
                            }
                            BulkAction(Icons.Default.StarBorder, "Unstar", !bulkActionInFlight) {
                                runBulkMessageAction("unflag", patch = { put("flagged", false) })
                            }
                            BulkAction(Icons.AutoMirrored.Filled.DriveFileMove, "Move", !bulkActionInFlight) {
                                movingSelected = !movingSelected
                            }
                            BulkAction(Icons.Default.Delete, "Delete", !bulkActionInFlight) {
                                confirmingBulkDelete = true
                            }
                        }
                    }
                }
                if (movingSelected) {
                    item {
                        Text(
                            "Move ${selectedMessageUids.size} to",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(folders.filter { it.optBoolean("selectable", true) && it.optString("path") != folder }) { target ->
                        val targetPath = target.optString("path")
                        val targetName = target.optString("name").ifBlank { targetPath }
                        Surface(
                            onClick = { if (!bulkActionInFlight) runBulkMessageAction("move", removeFromList = true, targetFolder = targetPath) },
                            enabled = !bulkActionInFlight,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(folderIcon(targetName, targetPath), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(targetName)
                            }
                        }
                    }
                }
                if (confirmingBulkDelete) {
                    item {
                        AlertDialog(
                            onDismissRequest = { if (!bulkActionInFlight) confirmingBulkDelete = false },
                            icon = { Icon(Icons.Default.Delete, null) },
                            title = { Text("Delete ${selectedMessageUids.size} message(s)?") },
                            text = { Text("This moves the selected message(s) out of the current folder.") },
                            confirmButton = {
                                TextButton(
                                    onClick = { runBulkMessageAction("delete", removeFromList = true) },
                                    enabled = !bulkActionInFlight,
                                ) { Text(if (bulkActionInFlight) "Deleting..." else "Delete") }
                            },
                            dismissButton = {
                                TextButton(onClick = { confirmingBulkDelete = false }, enabled = !bulkActionInFlight) { Text("Cancel") }
                            },
                        )
                    }
                }
            }
            items(visibleMessages) { message ->
                val seen = message.optBoolean("seen")
                val flagged = message.optBoolean("flagged")
                val hasAttachments = message.optBoolean("hasAttachments")
                val uid = message.messageUid()
                val selectedForBulk = uid != null && selectedMessageUids.contains(uid)
                val actionInFlight = uid != null && messageActionUids.contains(uid)
                val rowEnabled = uid != null && !mailBusy && !actionInFlight
                val sender = message.senderDisplay()
                fun toggleSelection() {
                    val rowUid = uid ?: return
                    selectedMessageUids =
                        if (selectedForBulk) selectedMessageUids - rowUid else selectedMessageUids + rowUid
                    if (!selectingMessages) selectingMessages = true
                    confirmingBulkDelete = false
                }
                Surface(
                    onClick = {
                        if (rowEnabled) {
                            if (selectingMessages) toggleSelection() else selected = message
                        }
                    },
                    enabled = rowEnabled,
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedForBulk) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else if (!seen) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        SenderAvatar(
                            label = sender,
                            selected = selectedForBulk,
                            enabled = rowEnabled,
                            onClick = { if (rowEnabled) toggleSelection() },
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    sender,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (seen) FontWeight.Normal else FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (hasAttachments) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        "Has attachments",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    formatMessageDate(message.optString("date")),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (seen) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                )
                            }
                            Text(
                                message.optString("subject").ifBlank { "(No subject)" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (seen) FontWeight.Normal else FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    message.optString("preview"),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                IconButton(
                                    onClick = {
                                        runMessageAction(
                                            message,
                                            if (flagged) "unflag" else "flag",
                                            patch = { put("flagged", !flagged) },
                                        )
                                    },
                                    enabled = rowEnabled,
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        if (flagged) Icons.Default.Star else Icons.Default.StarBorder,
                                        if (flagged) "Unstar" else "Star",
                                        modifier = Modifier.size(20.dp),
                                        tint = if (flagged) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (hasMoreMessages && activeSearch.isBlank()) {
                item {
                    Button(
                        onClick = { loadMoreMessages() },
                        enabled = !loadingMore,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (loadingMore) "Loading..." else "Load more")
                    }
                }
            }
            item { Box(Modifier.height(72.dp)) }
        }
        }
        if (drawerOpen) {
            MailDrawer(
                accounts = accounts,
                folders = folders,
                selectedFolder = folder,
                enabled = !mailBusy,
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
                    confirmingBulkDelete = false
                    messages.clear()
                    drawerOpen = false
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        ExtendedFloatingActionButton(
            onClick = { if (!mailBusy) compose(ComposeDraft()) },
            icon = { Icon(Icons.Default.Edit, null) },
            text = { Text("Compose") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 40.dp),
        )
    }
}

@Composable
private fun MailDrawer(
    accounts: List<JSONObject>,
    folders: List<JSONObject>,
    selectedFolder: String,
    enabled: Boolean,
    onClose: () -> Unit,
    onAccountSelected: (JSONObject) -> Unit,
    onAddAccount: () -> Unit,
    onFolderSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrimInteraction = remember { MutableInteractionSource() }
    Row(modifier) {
        ModalDrawerSheet(
            modifier = Modifier.fillMaxHeight().width(308.dp),
            drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "jmail",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close menu") }
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                DrawerSectionLabel("Accounts")
                accounts.forEach { account ->
                    val label = account.cleanString("displayName") ?: account.cleanString("email") ?: "Mail account"
                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${account.optString("protocol").uppercase()} · ${account.optString("status")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        selected = false,
                        onClick = { if (enabled) onAccountSelected(account) },
                        icon = { SenderAvatar(label = label, selected = false, enabled = false, onClick = {}) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
                if (accounts.isEmpty()) {
                    Text(
                        "Current jmail account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
                    )
                }
                NavigationDrawerItem(
                    label = { Text("Add mail account") },
                    selected = false,
                    onClick = { if (enabled) onAddAccount() },
                    icon = { Icon(Icons.Default.Add, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                DrawerSectionLabel("Folders")
                folders.forEach { entry ->
                    val path = entry.optString("path")
                    val name = entry.optString("name").ifBlank { path }
                    NavigationDrawerItem(
                        label = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = path == selectedFolder,
                        onClick = { if (enabled && path != selectedFolder) onFolderSelected(path) },
                        icon = { Icon(folderIcon(name, path), null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
                Box(Modifier.height(16.dp))
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                .clickable(interactionSource = scrimInteraction, indication = null) { onClose() },
        )
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun BulkAction(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled) { Icon(icon, label) }
}

private fun folderIcon(name: String, path: String): ImageVector {
    val key = name.ifBlank { path }.lowercase()
    return when {
        key.contains("inbox") -> Icons.Default.Inbox
        key.contains("sent") -> Icons.AutoMirrored.Filled.Send
        key.contains("draft") -> Icons.Default.Drafts
        key.contains("trash") || key.contains("deleted") -> Icons.Default.Delete
        key.contains("spam") || key.contains("junk") -> Icons.Default.Report
        key.contains("archive") -> Icons.Default.Archive
        key.contains("star") || key.contains("flag") -> Icons.Default.Star
        else -> Icons.Default.Folder
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
    val accountId = account.cleanString("id")
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
                                    val id = accountId
                                    if (id == null) {
                                        status = "Account is missing its ID."
                                        confirmingDelete = false
                                        return@Button
                                    }
                                    deleting = true
                                    status = "Removing account..."
                                    Thread {
                                        runCatching { api.deleteAccount(id) }
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
    var secret by remember(accountId) { mutableStateOf("") }
    var notifications by remember(accountId) { mutableStateOf(accountSettings?.optBoolean("notifications", true) ?: true) }
    var leaveOnServer by remember(accountId) { mutableStateOf(accountSettings?.optBoolean("leaveOnServer", true) ?: true) }
    var status by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val editing = account != null

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
                        if (editing && accountId == null) {
                            status = "Account is missing its ID."
                            return@Button
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
                                if (editing) api.updateAccount(accountId ?: error("Account is missing its ID."), body) else api.addAccount(body)
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
                ) { Text(if (saving) "Saving..." else "Save") }
            }
        }
        item { Text(if (editing) "Edit mail account" else "Add mail account", style = MaterialTheme.typography.headlineSmall) }
        item { OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), singleLine = true, enabled = !saving, label = { Text("Email address") }) }
        item { OutlinedTextField(displayName, { displayName = it }, Modifier.fillMaxWidth(), singleLine = true, enabled = !saving, label = { Text("Display name") }) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { selectProtocol("imap") }, enabled = !saving && protocol != "imap", modifier = Modifier.weight(1f)) {
                    Text("IMAP")
                }
                Button(onClick = { selectProtocol("pop3") }, enabled = !saving && protocol != "pop3", modifier = Modifier.weight(1f)) {
                    Text("POP3")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { authType = "keycloak" }, enabled = !saving && authType != "keycloak", modifier = Modifier.weight(1f)) {
                    Text("Keycloak")
                }
                Button(onClick = { authType = "password" }, enabled = !saving && authType != "password", modifier = Modifier.weight(1f)) {
                    Text("Password")
                }
            }
        }
        item { OutlinedTextField(incomingHost, { incomingHost = it }, Modifier.fillMaxWidth(), singleLine = true, enabled = !saving, label = { Text(if (protocol == "imap") "IMAP host" else "POP3 host") }) }
        item { OutlinedTextField(incomingPort, { incomingPort = it }, Modifier.fillMaxWidth(), singleLine = true, enabled = !saving, label = { Text(if (protocol == "imap") "IMAP port" else "POP3 port") }) }
        item { OutlinedTextField(smtpHost, { smtpHost = it }, Modifier.fillMaxWidth(), singleLine = true, enabled = !saving, label = { Text("SMTP host") }) }
        item { OutlinedTextField(smtpPort, { smtpPort = it }, Modifier.fillMaxWidth(), singleLine = true, enabled = !saving, label = { Text("SMTP port") }) }
        item { OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), singleLine = true, enabled = !saving, label = { Text("Username") }, placeholder = { Text("Defaults to email address") }) }
        if (authType == "password") {
            item {
                OutlinedTextField(
                    secret,
                    { secret = it },
                    Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !saving,
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
                Switch(checked = notifications, onCheckedChange = { notifications = it }, enabled = !saving)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Leave mail on server")
                Switch(checked = leaveOnServer, onCheckedChange = { leaveOnServer = it }, enabled = !saving)
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
    var confirmingAction by remember { mutableStateOf<String?>(null) }
    var acting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var attachmentStatus by remember { mutableStateOf<String?>(null) }
    var downloadingPartId by remember { mutableStateOf<String?>(null) }
    var showRemoteImages by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uid = summary.optInt("uid")

    fun runDestructiveMessageAction(action: String) {
        acting = true
        error = null
        Thread {
            runCatching { api.action(folder, uid, action) }
                .onSuccess { runOnMain(afterAction) }
                .onFailure {
                    runOnMain {
                        acting = false
                        error = it.message
                    }
                }
        }.start()
    }

    fun runDetailPatchAction(action: String, patch: JSONObject.() -> Unit, message: JSONObject) {
        acting = true
        error = null
        Thread {
            runCatching { api.action(folder, uid, action) }
                .onSuccess {
                    runOnMain {
                        detail = JSONObject(message.toString()).apply(patch)
                        afterPatch(patch)
                        acting = false
                    }
                }
                .onFailure {
                    runOnMain {
                        acting = false
                        error = it.message
                    }
                }
        }.start()
    }

    fun runDetailMoveAction(targetPath: String) {
        acting = true
        error = null
        Thread {
            runCatching { api.action(folder, uid, "move", targetPath) }
                .onSuccess { runOnMain(afterAction) }
                .onFailure {
                    runOnMain {
                        acting = false
                        error = it.message
                    }
                }
        }.start()
    }

    LaunchedEffect(uid, folder) {
        detail = null
        error = null
        attachmentStatus = null
        downloadingPartId = null
        showRemoteImages = false
        moving = false
        confirmingAction = null
        runCatching { withContext(Dispatchers.IO) { api.message(folder, uid) } }
            .onSuccess { detail = it }
            .onFailure { error = it.message }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderDisplayName(folder)) },
                navigationIcon = {
                    IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    detail?.let { message ->
                        val seen = message.optBoolean("seen")
                        val flagged = message.optBoolean("flagged")
                        IconButton(
                            enabled = !acting,
                            onClick = {
                                runDetailPatchAction(
                                    if (flagged) "unflag" else "flag",
                                    { put("flagged", !flagged) },
                                    message,
                                )
                            },
                        ) {
                            Icon(
                                if (flagged) Icons.Default.Star else Icons.Default.StarBorder,
                                if (flagged) "Unstar" else "Star",
                                tint = if (flagged) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        IconButton(onClick = { confirmingAction = "delete" }, enabled = !acting) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                        Box {
                            IconButton(onClick = { menuOpen = true }, enabled = !acting) {
                                Icon(Icons.Default.MoreVert, "More options")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(if (seen) "Mark as unread" else "Mark as read") },
                                    onClick = {
                                        menuOpen = false
                                        runDetailPatchAction(
                                            if (seen) "markUnseen" else "markSeen",
                                            { put("seen", !seen) },
                                            message,
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(if (moving) "Cancel move" else "Move to folder") },
                                    onClick = {
                                        menuOpen = false
                                        moving = !moving
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Mark as spam") },
                                    onClick = {
                                        menuOpen = false
                                        confirmingAction = "markSpam"
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            detail?.let { message ->
                Surface(tonalElevation = 3.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FilledTonalButton(
                            onClick = { compose(replyDraft(message, folder, uid)) },
                            enabled = !acting,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Reply, null, modifier = Modifier.size(18.dp))
                            Text("Reply", modifier = Modifier.padding(start = 8.dp))
                        }
                        FilledTonalButton(
                            onClick = { compose(forwardDraft(message)) },
                            enabled = !acting,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Forward, null, modifier = Modifier.size(18.dp))
                            Text("Forward", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            val message = detail
            if (message == null) {
                item { Text("Loading message...") }
            } else {
                item {
                    Text(
                        message.optString("subject").ifBlank { "(No subject)" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                item {
                    val sender = message.senderDisplay()
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SenderAvatar(label = sender, selected = false, enabled = false, onClick = {})
                        Column(Modifier.weight(1f)) {
                            Text(
                                sender,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                addressLine("To", message.optJSONArray("to")),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            formatMessageDate(message.optString("date")),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item { HorizontalDivider() }
                attachmentStatus?.let { item { Text(it, color = MaterialTheme.colorScheme.primary) } }
                confirmingAction?.let { action ->
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    if (action == "delete") "Delete this message?" else "Mark this message as spam?",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    if (action == "delete") {
                                        "This moves the message out of the current folder."
                                    } else {
                                        "This moves the message to spam and removes it from this list."
                                    },
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { confirmingAction = null }, enabled = !acting) { Text("Cancel") }
                                    Button(
                                        enabled = !acting,
                                        onClick = { runDestructiveMessageAction(if (action == "delete") "delete" else "markSpam") },
                                    ) {
                                        Text(if (acting) "Working..." else if (action == "delete") "Delete" else "Mark spam")
                                    }
                                }
                            }
                        }
                    }
                }
                if (moving) {
                    item { Text("Move to", style = MaterialTheme.typography.titleMedium) }
                    items(folders.filter { it.optBoolean("selectable", true) && it.optString("path") != folder }) { target ->
                        val targetPath = target.optString("path")
                        Button(
                            onClick = {
                                runDetailMoveAction(targetPath)
                            },
                            enabled = !acting,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(target.optString("name").ifBlank { targetPath })
                        }
                    }
                }
                val attachments = message.optJSONArray("attachments")
                if (attachments != null && attachments.length() > 0) {
                    item { Text("Attachments", style = MaterialTheme.typography.titleMedium) }
                    items(attachments.objects()) { attachment ->
                        val partId = attachment.cleanString("partId")
                        val filename = attachment.cleanString("filename") ?: "Attachment"
                        val contentType = attachment.optString("contentType").ifBlank { "application/octet-stream" }
                        val downloading = downloadingPartId == partId
                        val attachmentDownloadInFlight = downloadingPartId != null
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        filename,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "$contentType · ${formatBytes(attachment.optLong("size"))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(enabled = partId != null && !attachmentDownloadInFlight, onClick = {
                                    val attachmentPartId = partId ?: return@TextButton
                                    downloadingPartId = attachmentPartId
                                    error = null
                                    attachmentStatus = "Downloading $filename..."
                                    Thread {
                                        runCatching { api.downloadAttachment(folder, uid, attachmentPartId) }
                                            .onSuccess { bytes ->
                                                runOnMain {
                                                    runCatching {
                                                        shareAttachment(context, filename, contentType, bytes)
                                                    }.onSuccess {
                                                        downloadingPartId = null
                                                        attachmentStatus = "Downloaded $filename (${formatBytes(bytes.size.toLong())})"
                                                    }.onFailure {
                                                        downloadingPartId = null
                                                        error = it.message
                                                    }
                                                }
                                            }
                                            .onFailure {
                                                runOnMain {
                                                    downloadingPartId = null
                                                    error = it.message
                                                }
                                            }
                                    }.start()
                                }) { Text(if (downloading) "Downloading..." else "Download") }
                            }
                        }
                    }
                }
                val html = message.optString("html")
                val text = message.optString("text")
                if (html.isNotBlank()) {
                    if (!showRemoteImages) {
                        item {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Remote images are blocked.",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(onClick = { showRemoteImages = true }) { Text("Show images") }
                            }
                        }
                    }
                    item {
                        MessageHtmlBody(html = html, showRemoteImages = showRemoteImages) { url ->
                            runCatching {
                                CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
                            }
                        }
                    }
                } else {
                    item {
                        SelectionContainer {
                            Text(text.ifBlank { stripHtml(html) })
                        }
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

private data class ComposeAttachment(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mime: String,
)

private const val MAX_SEND_ATTACHMENTS = 10
private const val MAX_SEND_ATTACHMENT_BYTES = 10L * 1024 * 1024
private const val MAX_SEND_ATTACHMENTS_BYTES = 25L * 1024 * 1024

private fun queryAttachment(context: Context, uri: Uri): ComposeAttachment {
    val resolver = context.contentResolver
    var name = "attachment"
    var size = 0L
    runCatching {
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) cursor.getString(nameIdx)?.let { name = it }
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }
    }
    return ComposeAttachment(uri, name, size, resolver.getType(uri) ?: "application/octet-stream")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeScreen(api: JmailApi, draft: ComposeDraft, close: () -> Unit) {
    var to by remember(draft) { mutableStateOf(draft.to) }
    var cc by remember(draft) { mutableStateOf(draft.cc) }
    var bcc by remember(draft) { mutableStateOf(draft.bcc) }
    var subject by remember(draft) { mutableStateOf(draft.subject) }
    var body by remember(draft) { mutableStateOf(draft.body) }
    var status by remember(draft) { mutableStateOf<String?>(null) }
    var sending by remember(draft) { mutableStateOf(false) }
    var showCcBcc by remember(draft) { mutableStateOf(draft.cc.isNotBlank() || draft.bcc.isNotBlank()) }
    val context = LocalContext.current
    val attachments = remember(draft) { mutableStateListOf<ComposeAttachment>() }
    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        for (uri in uris) {
            if (attachments.size >= MAX_SEND_ATTACHMENTS) {
                status = "You can attach up to $MAX_SEND_ATTACHMENTS files."
                break
            }
            if (attachments.any { it.uri == uri }) continue
            val attachment = queryAttachment(context, uri)
            if (attachment.size > MAX_SEND_ATTACHMENT_BYTES) {
                status = "${attachment.name} is larger than ${formatBytes(MAX_SEND_ATTACHMENT_BYTES)}."
                continue
            }
            if (attachments.sumOf { it.size } + attachment.size > MAX_SEND_ATTACHMENTS_BYTES) {
                status = "Attachments exceed ${formatBytes(MAX_SEND_ATTACHMENTS_BYTES)} total."
                continue
            }
            attachments.add(attachment)
        }
    }

    fun submit() {
        val toText = to.trim()
        val ccText = cc.trim()
        val bccText = bcc.trim()
        val subjectText = subject.trim()
        val bodyText = body.trim()
        if (toText.isBlank()) {
            status = "Enter at least one recipient."
            return
        }
        for ((label, value) in listOf("To" to toText, "Cc" to ccText, "Bcc" to bccText)) {
            val invalid = invalidRecipients(value)
            if (invalid.isNotEmpty()) {
                status = "$label has invalid recipient(s): ${invalid.joinToString(", ")}"
                return
            }
        }
        if (subjectText.isBlank() && bodyText.isBlank()) {
            status = "Enter a subject or message body."
            return
        }
        sending = true
        status = "Sending..."
        val pendingAttachments = attachments.toList()
        Thread {
            runCatching {
                val outgoing = pendingAttachments.map { attachment ->
                    val bytes = context.contentResolver.openInputStream(attachment.uri)?.use { it.readBytes() }
                        ?: error("Couldn't read ${attachment.name}.")
                    OutgoingAttachment(
                        filename = attachment.name,
                        contentType = attachment.mime,
                        size = bytes.size.toLong(),
                        contentBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                    )
                }
                api.send(
                    to = toText,
                    cc = ccText,
                    bcc = bccText,
                    subject = subjectText,
                    text = bodyText,
                    inReplyToUid = draft.inReplyToUid,
                    inReplyToFolder = draft.inReplyToFolder,
                    attachments = outgoing,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (draft.inReplyToUid != null) "Reply" else "New message") },
                navigationIcon = {
                    IconButton(onClick = close, enabled = !sending) {
                        Icon(Icons.Default.Close, "Discard")
                    }
                },
                actions = {
                    IconButton(onClick = { pickFiles.launch(arrayOf("*/*")) }, enabled = !sending) {
                        Icon(Icons.Default.AttachFile, "Attach files")
                    }
                    IconButton(onClick = { submit() }, enabled = !sending) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                to,
                { to = it },
                Modifier.fillMaxWidth(),
                enabled = !sending,
                singleLine = true,
                label = { Text("To") },
                trailingIcon = {
                    TextButton(onClick = { showCcBcc = !showCcBcc }, enabled = !sending) {
                        Text(if (showCcBcc) "Hide" else "Cc/Bcc")
                    }
                },
            )
            if (showCcBcc) {
                OutlinedTextField(cc, { cc = it }, Modifier.fillMaxWidth(), enabled = !sending, singleLine = true, label = { Text("Cc") })
                OutlinedTextField(bcc, { bcc = it }, Modifier.fillMaxWidth(), enabled = !sending, singleLine = true, label = { Text("Bcc") })
            }
            OutlinedTextField(subject, { subject = it }, Modifier.fillMaxWidth(), enabled = !sending, singleLine = true, label = { Text("Subject") })
            attachments.forEach { attachment ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f)) {
                            Text(
                                attachment.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                formatBytes(attachment.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { attachments.remove(attachment) }, enabled = !sending) {
                            Icon(Icons.Default.Close, "Remove attachment")
                        }
                    }
                }
            }
            HorizontalDivider()
            OutlinedTextField(
                body,
                { body = it },
                Modifier.fillMaxWidth().heightIn(min = 240.dp),
                enabled = !sending,
                label = { Text("Message") },
            )
            status?.let { Text(it, color = if (it == "Sending...") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsScreen(api: JmailApi, compose: (ComposeDraft) -> Unit) {
    val rows = remember { mutableStateListOf<JSONObject>() }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<JSONObject?>(null) }
    var selectedContact by remember { mutableStateOf<JSONObject?>(null) }
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val activeQuery = query.trim()
    val contactsBusy = loading || deleting
    LaunchedEffect(activeQuery) {
        selectedIds = emptySet()
        selecting = false
        confirmingDelete = false
        loading = true
        error = null
        runCatching { withContext(Dispatchers.IO) { api.contacts(activeQuery) } }
            .onSuccess { rows.replace(it) }
            .onFailure { error = it.message }
        loading = false
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
                    Text("Contacts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        selecting = !selecting
                        selectedIds = emptySet()
                        confirmingDelete = false
                    }, enabled = !contactsBusy) {
                        Icon(
                            if (selecting) Icons.Default.Close else Icons.Default.Checklist,
                            if (selecting) "Cancel selection" else "Select contacts",
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    query,
                    { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !contactsBusy,
                    shape = RoundedCornerShape(28.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }, enabled = !contactsBusy) {
                                Icon(Icons.Default.Close, "Clear search")
                            }
                        }
                    },
                    placeholder = { Text("Search contacts") },
                )
            }
            if (selecting && selectedIds.isNotEmpty()) {
                item {
                    Button(onClick = { confirmingDelete = true }, enabled = !contactsBusy) {
                        Text("Delete selected (${selectedIds.size})")
                    }
                }
                if (confirmingDelete) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Delete selected contacts?", style = MaterialTheme.typography.titleMedium)
                                Text("This deletes ${selectedIds.size} selected contact(s) from the server.")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { confirmingDelete = false }, enabled = !deleting) { Text("Cancel") }
                                    Button(
                                        enabled = !deleting,
                                        onClick = {
                                            val ids = selectedIds
                                            if (ids.isEmpty()) return@Button
                                            deleting = true
                                            error = null
                                            Thread {
                                                val failedIds = ids.filter { id -> runCatching { api.deleteContact(id) }.isFailure }.toSet()
                                                val deletedIds = ids - failedIds
                                                runOnMain {
                                                    deleting = false
                                                    rows.removeAll { deletedIds.contains(it.optString("id")) }
                                                    if (failedIds.isEmpty()) {
                                                        selectedIds = emptySet()
                                                        selecting = false
                                                        confirmingDelete = false
                                                    } else {
                                                        selectedIds = failedIds
                                                        error = "Deleted ${deletedIds.size} contact(s). Failed to delete ${failedIds.size} contact(s)."
                                                    }
                                                }
                                            }.start()
                                        },
                                    ) { Text(if (deleting) "Deleting..." else "Delete selected") }
                                }
                            }
                        }
                    }
                }
            }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (loading) {
                item { Text("Loading contacts...") }
            } else if (rows.isEmpty()) {
                item { Text(if (activeQuery.isBlank()) "No contacts" else "No contacts match \"$activeQuery\"") }
            }
            items(rows) { contact ->
                val id = contact.cleanString("id")
                val selected = id != null && selectedIds.contains(id)
                val rowEnabled = !contactsBusy && (!selecting || id != null)
                val title = contact.contactTitle()
                fun toggleSelection() {
                    val contactId = id ?: return
                    selectedIds = if (selected) selectedIds - contactId else selectedIds + contactId
                    if (!selecting) selecting = true
                    confirmingDelete = false
                }
                Surface(
                    onClick = {
                        if (rowEnabled) {
                            if (selecting) toggleSelection() else selectedContact = contact
                        }
                    },
                    enabled = rowEnabled,
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SenderAvatar(
                            label = title,
                            selected = selected,
                            enabled = rowEnabled,
                            onClick = { if (rowEnabled) toggleSelection() },
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            contact.cleanString("email")?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
            item { Box(Modifier.height(72.dp)) }
        }
        ExtendedFloatingActionButton(
            onClick = { if (!contactsBusy) adding = true },
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("New contact") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 40.dp),
        )
    }
}

@Composable
private fun ContactDetail(contact: JSONObject, back: () -> Unit, edit: () -> Unit, email: (String) -> Unit) {
    val title = contact.contactTitle()
    val emailAddr = contact.cleanString("email")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text(
                "Contact",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = edit) { Icon(Icons.Default.Edit, "Edit contact") }
        }
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(88.dp).clip(CircleShape).background(avatarColor(title)),
                contentAlignment = Alignment.Center,
            ) {
                Text(avatarInitial(title), color = Color.White, style = MaterialTheme.typography.displaySmall)
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            contact.cleanString("company")?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            emailAddr?.let { address ->
                FilledTonalButton(onClick = { email(address) }, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp))
                    Text("Email", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            emailAddr?.let { ContactInfoRow(Icons.Default.Email, "Email", it) }
            contact.cleanString("phone")?.let { ContactInfoRow(Icons.Default.Call, "Phone", it) }
            contact.cleanString("company")?.let { ContactInfoRow(Icons.Default.Business, "Company", it) }
            contact.cleanString("notes")?.let { ContactInfoRow(Icons.AutoMirrored.Filled.Notes, "Notes", it) }
        }
    }
}

@Composable
private fun ContactInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ContactEditor(api: JmailApi, contact: JSONObject? = null, close: () -> Unit, done: (JSONObject) -> Unit) {
    val contactId = contact?.cleanString("id")
    var name by remember(contactId) { mutableStateOf(contact?.cleanString("displayName").orEmpty()) }
    var email by remember(contactId) { mutableStateOf(contact?.cleanString("email").orEmpty()) }
    var phone by remember(contactId) { mutableStateOf(contact?.cleanString("phone").orEmpty()) }
    var company by remember(contactId) { mutableStateOf(contact?.cleanString("company").orEmpty()) }
    var notes by remember(contactId) { mutableStateOf(contact?.cleanString("notes").orEmpty()) }
    var status by remember(contactId) { mutableStateOf<String?>(null) }
    var saving by remember(contactId) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (contact == null) "New contact" else "Edit contact", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), enabled = !saving, label = { Text("Name") })
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), enabled = !saving, label = { Text("Email") })
        OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), enabled = !saving, label = { Text("Phone") })
        OutlinedTextField(company, { company = it }, Modifier.fillMaxWidth(), enabled = !saving, label = { Text("Company") })
        OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), minLines = 3, enabled = !saving, label = { Text("Notes") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = close, enabled = !saving) { Text("Cancel") }
            Button(enabled = !saving, onClick = {
                val nameText = name.trim()
                val emailText = email.trim()
                val phoneText = phone.trim()
                val companyText = company.trim()
                val notesText = notes.trim()
                if (nameText.isBlank() && emailText.isBlank()) {
                    status = "Enter a name or email address."
                    return@Button
                }
                if (emailText.isNotBlank() && !emailText.contains("@")) {
                    status = "Enter a valid email address."
                    return@Button
                }
                if (contact != null && contactId == null) {
                    status = "Contact is missing its ID."
                    return@Button
                }
                saving = true
                status = "Saving..."
                Thread {
                    runCatching {
                        if (contact == null) api.createContact(nameText, emailText, phoneText, companyText, notesText)
                        else api.updateContact(contactId ?: error("Contact is missing its ID."), nameText, emailText, phoneText, companyText, notesText)
                    }
                        .onSuccess { runOnMain { done(it) } }
                        .onFailure {
                            runOnMain {
                                saving = false
                                status = it.message
                            }
                        }
                }.start()
            }) { Text(if (saving) "Saving..." else "Save") }
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
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        loading = true
        error = null
        runCatching {
            withContext(Dispatchers.IO) {
                api.events(
                    Instant.now().minus(30, ChronoUnit.DAYS).toString(),
                    Instant.now().plus(365, ChronoUnit.DAYS).toString(),
                )
            }
        }.onSuccess { rows.replace(it) }.onFailure { error = it.message }
        loading = false
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
                    enabled = !loading,
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
            if (loading) {
                item { Text("Loading calendar...") }
            }
            when (mode) {
                CalendarMode.Agenda -> {
                    val today = LocalDate.now()
                    val upcoming = events.filter { (eventDate(it) ?: today) >= today }
                    if (!loading && upcoming.isEmpty()) {
                        item { Text("No upcoming events", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        val grouped = upcoming.groupBy { eventDate(it) ?: today }.toSortedMap()
                        grouped.forEach { (day, dayEvents) ->
                            item { AgendaDayHeader(day) }
                            dayEvents.forEach { event ->
                                item { CalendarEventCard(event, api, rows, onEdit = { editing = it }) { error = it } }
                            }
                        }
                    }
                }
                CalendarMode.Week -> weekDays(anchor).forEach { day ->
                    item { DayColumn(day, eventsForDay(events, day), api, rows, onEdit = { editing = it }) { error = it } }
                }
                CalendarMode.Month -> item {
                    MonthCalendarGrid(
                        anchor = anchor,
                        events = events,
                        enabled = !loading,
                        selectDay = { anchor = it; mode = CalendarMode.Agenda },
                        onEdit = { editing = it },
                    )
                }
            }
            item { Box(Modifier.height(72.dp)) }
        }
        ExtendedFloatingActionButton(
            onClick = { if (!loading) adding = true },
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("New event") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 40.dp),
        )
    }
}

private enum class CalendarMode { Agenda, Week, Month }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarHeader(
    mode: CalendarMode,
    anchor: LocalDate,
    enabled: Boolean,
    onMode: (CalendarMode) -> Unit,
    onPrevious: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                calendarTitle(anchor, mode),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (mode != CalendarMode.Agenda) {
                IconButton(onClick = onPrevious, enabled = enabled) {
                    Icon(Icons.Default.ChevronLeft, "Previous")
                }
                IconButton(onClick = onNext, enabled = enabled) {
                    Icon(Icons.Default.ChevronRight, "Next")
                }
            }
            IconButton(onClick = onToday, enabled = enabled) {
                Icon(Icons.Default.Today, "Jump to today")
            }
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            CalendarMode.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = mode == entry,
                    onClick = { onMode(entry) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(index, CalendarMode.entries.size),
                ) {
                    Text(entry.name)
                }
            }
        }
    }
}

@Composable
private fun AgendaDayHeader(day: LocalDate) {
    val isToday = day == LocalDate.now()
    Row(
        Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            day.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
        if (isToday) {
            Text(
                "Today",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
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
    val isToday = day == LocalDate.now()
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                day.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            if (isToday) {
                Text("Today", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (events.isEmpty()) {
            Text("No events", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        events.forEach { CalendarEventCard(it, api, rows, onEdit, onError) }
    }
}

@Composable
private fun MonthCalendarGrid(
    anchor: LocalDate,
    events: List<JSONObject>,
    enabled: Boolean,
    selectDay: (LocalDate) -> Unit,
    onEdit: (JSONObject) -> Unit,
) {
    val month = YearMonth.from(anchor)
    val today = LocalDate.now()
    val outline = MaterialTheme.colorScheme.outlineVariant
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, outline, RoundedCornerShape(16.dp)),
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        HorizontalDivider(color = outline)
        monthWeeks(anchor).forEachIndexed { weekIndex, week ->
            if (weekIndex > 0) HorizontalDivider(color = outline)
            Row(Modifier.fillMaxWidth().height(92.dp)) {
                week.forEach { day ->
                    val dayEvents = eventsForDay(events, day)
                    val inMonth = YearMonth.from(day) == month
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (inMonth) Color.Transparent
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            )
                            .clickable(enabled = enabled) { selectDay(day) }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (day == today) {
                            Box(
                                Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    day.dayOfMonth.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        } else {
                            Text(
                                day.dayOfMonth.toString(),
                                color = if (inMonth) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        dayEvents.take(2).forEach { event ->
                            Text(
                                event.optString("title").ifBlank { "Untitled" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable(enabled = enabled) { onEdit(event) }
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        if (dayEvents.size > 2) {
                            Text(
                                "+${dayEvents.size - 2} more",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    var confirmingDelete by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val eventId = event.cleanString("id")
    val location = event.cleanString("location")
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(onClick = { if (!deleting) onEdit(event) }, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary),
                )
                Column(
                    Modifier.weight(1f).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        event.optString("title").ifBlank { "Untitled event" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            Icons.Default.AccessTime,
                            null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            formatEventTime(event),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    location?.let {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                Icons.Default.Place,
                                null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { confirmingDelete = true },
                    enabled = !deleting && eventId != null,
                    modifier = Modifier.padding(end = 4.dp),
                ) {
                    Icon(Icons.Default.Delete, "Delete event", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (confirmingDelete) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Delete this event?", style = MaterialTheme.typography.titleMedium)
                    Text("This removes the event from the server calendar.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { confirmingDelete = false }, enabled = !deleting) { Text("Cancel") }
                        Button(
                            enabled = !deleting,
                            onClick = {
                                val id = eventId ?: return@Button
                                deleting = true
                                onError(null)
                                Thread {
                                    runCatching { api.deleteEvent(id) }
                                        .onSuccess {
                                            runOnMain {
                                                deleting = false
                                                rows.removeAll { it.cleanString("id") == id }
                                            }
                                        }
                                        .onFailure {
                                            runOnMain {
                                                deleting = false
                                                onError(it.message)
                                            }
                                        }
                                }.start()
                            },
                        ) { Text(if (deleting) "Deleting..." else "Delete event") }
                    }
                }
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
    val eventId = event?.cleanString("id")
    val initialStartsAt = event?.cleanString("startsAt") ?: defaultStart.toString()
    val initialEndsAt = event?.cleanString("endsAt") ?: defaultStart.plus(1, ChronoUnit.HOURS).toString()
    val initialStartLocal = remember(eventId) { eventLocalDateTime(initialStartsAt, defaultStart) }
    val initialEndLocal = remember(eventId) { eventLocalDateTime(initialEndsAt, defaultStart.plus(1, ChronoUnit.HOURS)) }
    var title by remember(eventId) { mutableStateOf(event?.cleanString("title").orEmpty()) }
    var startDate by remember(eventId) { mutableStateOf(initialStartLocal.toLocalDate()) }
    var startTime by remember(eventId) { mutableStateOf(initialStartLocal.toLocalTime().truncatedTo(ChronoUnit.MINUTES)) }
    var endDate by remember(eventId) { mutableStateOf(initialEndLocal.toLocalDate()) }
    var endTime by remember(eventId) { mutableStateOf(initialEndLocal.toLocalTime().truncatedTo(ChronoUnit.MINUTES)) }
    var location by remember(eventId) { mutableStateOf(event?.cleanString("location").orEmpty()) }
    var status by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var allDay by remember(eventId) { mutableStateOf(event?.optBoolean("allDay") ?: false) }
    var picker by remember { mutableStateOf<EventPickerTarget?>(null) }
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy") }
    val timeFmt = remember { DateTimeFormatter.ofPattern("h:mm a") }

    fun save() {
        val titleText = title.trim()
        if (titleText.isBlank()) {
            status = "Enter an event title."
            return
        }
        val zone = ZoneId.systemDefault()
        val startsAt: Instant
        val endsAt: Instant
        if (allDay) {
            if (endDate.isBefore(startDate)) {
                status = "End date must not be before the start date."
                return
            }
            startsAt = startDate.atStartOfDay(zone).toInstant()
            endsAt = endDate.plusDays(1).atStartOfDay(zone).toInstant()
        } else {
            startsAt = LocalDateTime.of(startDate, startTime).atZone(zone).toInstant()
            endsAt = LocalDateTime.of(endDate, endTime).atZone(zone).toInstant()
            if (!endsAt.isAfter(startsAt)) {
                status = "End time must be after the start time."
                return
            }
        }
        if (event != null && eventId == null) {
            status = "Event is missing its ID."
            return
        }
        saving = true
        status = "Saving..."
        Thread {
            runCatching {
                if (eventId == null) api.createEvent(titleText, startsAt.toString(), endsAt.toString(), location.trim(), allDay)
                else api.updateEvent(eventId, titleText, startsAt.toString(), endsAt.toString(), location.trim(), allDay)
            }
                .onSuccess { runOnMain { done(it) } }
                .onFailure {
                    runOnMain {
                        saving = false
                        status = it.message
                    }
                }
        }.start()
    }

    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = close, enabled = !saving) { Icon(Icons.Default.Close, "Cancel") }
            Text(
                if (eventId == null) "New event" else "Edit event",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { save() }, enabled = !saving) {
                Text(if (saving) "Saving..." else "Save")
            }
        }
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                title, { title = it },
                Modifier.fillMaxWidth(),
                enabled = !saving,
                singleLine = true,
                label = { Text("Title") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Today,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("All-day", style = MaterialTheme.typography.titleSmall)
                }
                Switch(checked = allDay, onCheckedChange = { allDay = it }, enabled = !saving)
            }
            EventScheduleRow(
                label = "Starts",
                date = startDate.format(dateFmt),
                time = startTime.format(timeFmt),
                showTime = !allDay,
                enabled = !saving,
                onDate = { picker = EventPickerTarget.StartDate },
                onTime = { picker = EventPickerTarget.StartTime },
            )
            EventScheduleRow(
                label = "Ends",
                date = endDate.format(dateFmt),
                time = endTime.format(timeFmt),
                showTime = !allDay,
                enabled = !saving,
                onDate = { picker = EventPickerTarget.EndDate },
                onTime = { picker = EventPickerTarget.EndTime },
            )
            OutlinedTextField(
                location, { location = it },
                Modifier.fillMaxWidth(),
                enabled = !saving,
                singleLine = true,
                label = { Text("Location") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
            )
            status?.let {
                Text(it, color = if (it == "Saving...") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
        }
    }

    when (picker) {
        EventPickerTarget.StartDate -> DatePickerModal(startDate, { picker = null }) {
            startDate = it
            if (endDate.isBefore(startDate)) endDate = startDate
            picker = null
        }
        EventPickerTarget.StartTime -> TimePickerModal(startTime, { picker = null }) { startTime = it; picker = null }
        EventPickerTarget.EndDate -> DatePickerModal(endDate, { picker = null }) { endDate = it; picker = null }
        EventPickerTarget.EndTime -> TimePickerModal(endTime, { picker = null }) { endTime = it; picker = null }
        null -> {}
    }
}

private enum class EventPickerTarget { StartDate, StartTime, EndDate, EndTime }

@Composable
private fun EventScheduleRow(
    label: String,
    date: String,
    time: String,
    showTime: Boolean,
    enabled: Boolean,
    onDate: () -> Unit,
    onTime: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Default.AccessTime,
                null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(label, style = MaterialTheme.typography.titleSmall)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDate, enabled = enabled, modifier = Modifier.weight(2f)) { Text(date) }
            if (showTime) {
                OutlinedButton(onClick = onTime, enabled = enabled, modifier = Modifier.weight(1f)) { Text(time) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(initial: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis == null) onDismiss()
                else onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerModal(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
    )
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
    var notificationBusy by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        runCatching { withContext(Dispatchers.IO) { api.me().optJSONObject("user") } }
            .onSuccess { profile = it }
            .onFailure { status = it.message }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val user = profile
                val name = user?.cleanString("name") ?: user?.cleanString("email") ?: "jmail user"
                Box(
                    Modifier.size(56.dp).clip(CircleShape).background(avatarColor(name)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(avatarInitial(name), color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (user == null) {
                        Text("Loading profile...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        user.cleanString("email")?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        SettingsCard(Icons.Default.Dns, "Server") {
            Text(api.configuredServerUrl ?: "Not configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingsCard(Icons.Default.Notifications, "Push notifications") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Receive new mail notifications", modifier = Modifier.weight(1f))
                Switch(
                    checked = notificationsEnabled,
                    enabled = !notificationBusy,
                    onCheckedChange = { enabled ->
                        notificationsEnabled = enabled
                        api.notificationsEnabled = enabled
                        if (enabled) {
                            registerPush()
                            status = "Notification registration requested."
                        } else {
                            notificationBusy = true
                            status = "Disabling notifications..."
                            Thread {
                                runCatching { api.unregisterDevice() }
                                    .onSuccess {
                                        runOnMain {
                                            notificationBusy = false
                                            status = "Notifications disabled for this device."
                                        }
                                    }
                                    .onFailure {
                                        runOnMain {
                                            notificationBusy = false
                                            notificationsEnabled = true
                                            api.notificationsEnabled = true
                                            status = it.message
                                        }
                                    }
                            }.start()
                        }
                    },
                )
            }
            Text(
                "Re-register this device if notifications stop arriving or after server changes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(enabled = !notificationBusy, onClick = {
                api.notificationsEnabled = true
                notificationsEnabled = true
                registerPush()
                status = "Notification registration requested."
            }) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Text("Re-register device", modifier = Modifier.padding(start = 8.dp))
            }
        }
        SettingsCard(Icons.Default.DarkMode, "Appearance") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Dark theme")
                    Text(
                        "Dark blue surfaces and light blue accents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
        }
        status?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        OutlinedButton(onClick = logout, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
            Text("Sign out", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun SettingsCard(icon: ImageVector, title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

private fun MutableList<JSONObject>.replace(array: JSONArray) {
    clear()
    addAll(array.objects())
}

private fun JSONArray.objects(): List<JSONObject> =
    List(length()) { index -> optJSONObject(index) }.filterNotNull()

private fun JSONObject.messagePageItems(): JSONArray = optJSONArray("messages") ?: JSONArray()

private fun JSONObject.messageUid(): Int? = optInt("uid").takeIf { it > 0 }

private fun JSONObject.senderDisplay(): String {
    val from = optJSONArray("from")?.optJSONObject(0)
    val name = from?.optString("name").orEmpty().trim()
    if (name.isNotBlank() && !name.equals("null", ignoreCase = true)) return name
    val address = from?.optString("address").orEmpty().trim()
    return address.ifBlank { "(Unknown sender)" }
}

private fun avatarInitial(label: String): String =
    label.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "?"

private val AvatarColors = listOf(
    Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFF00897B),
    Color(0xFFF4511E), Color(0xFF3949AB), Color(0xFF6D4C41),
    Color(0xFF00ACC1), Color(0xFF7CB342), Color(0xFFD81B60),
)

private fun avatarColor(label: String): Color =
    AvatarColors[label.lowercase().hashCode().absoluteValue % AvatarColors.size]

@Composable
private fun SenderAvatar(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else avatarColor(label))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(
                avatarInitial(label),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private fun formatMessageDate(raw: String): String {
    if (raw.isBlank()) return ""
    val instant = runCatching { Instant.parse(raw) }.getOrNull()
        ?: runCatching { java.time.OffsetDateTime.parse(raw).toInstant() }.getOrNull()
        ?: runCatching { java.time.ZonedDateTime.parse(raw).toInstant() }.getOrNull()
        ?: return raw
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val today = LocalDate.now()
    return when {
        dateTime.toLocalDate() == today -> dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        dateTime.year == today.year -> dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
        else -> dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yy"))
    }
}

private fun folderDisplayName(path: String): String {
    val leaf = path.substringAfterLast('/').substringAfterLast('.')
    return when (leaf.uppercase()) {
        "INBOX" -> "Inbox"
        else -> leaf.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

private fun JSONObject.cleanString(key: String): String? {
    if (isNull(key)) return null
    return optString(key).takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
}

private fun JSONObject.accountLabel(): String = cleanString("displayName") ?: cleanString("email") ?: "Mail account"

private fun JSONObject.contactTitle(): String = cleanString("displayName") ?: cleanString("email") ?: "Untitled contact"

private fun hostWithPort(host: String, port: Int): String = if (port > 0) "$host:$port" else host

private fun invalidRecipients(value: String): List<String> =
    value
        .split(',', ';')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterNot { recipient ->
            val email = recipient.substringAfterLast('<').substringBefore('>').trim()
            email.contains("@") && email.substringAfter("@").contains(".")
        }

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
    intent.clipData = ClipData.newUri(context.contentResolver, safeName, uri)
    context.startActivity(Intent.createChooser(intent, "Open attachment").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
}

@Composable
private fun MessageHtmlBody(html: String, showRemoteImages: Boolean, onOpenUrl: (String) -> Unit) {
    val document = remember(html) { wrapEmailHtml(html) }
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setBackgroundColor(android.graphics.Color.WHITE)
                with(settings) {
                    javaScriptEnabled = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = false
                    setSupportZoom(false)
                    blockNetworkImage = true
                    blockNetworkLoads = true
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        request.url?.let { onOpenUrl(it.toString()) }
                        return true
                    }
                }
            }
        },
        update = { webView ->
            val signature = "$showRemoteImages:${html.hashCode()}"
            if (webView.tag != signature) {
                webView.tag = signature
                webView.settings.blockNetworkImage = !showRemoteImages
                webView.settings.blockNetworkLoads = !showRemoteImages
                webView.loadDataWithBaseURL(null, document, "text/html", "UTF-8", null)
            }
        },
    )
}

private fun wrapEmailHtml(html: String): String =
    """
    <!DOCTYPE html>
    <html>
    <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
      html, body { margin: 0; padding: 0; background: #ffffff; color: #1a1a1a; }
      body {
        font-family: -apple-system, Roboto, "Helvetica Neue", sans-serif;
        font-size: 15px; line-height: 1.5; padding: 6px 4px;
        word-wrap: break-word; overflow-wrap: break-word;
      }
      img { max-width: 100% !important; height: auto !important; }
      table { max-width: 100% !important; }
      a { color: #0b57d0; }
      pre { white-space: pre-wrap; word-wrap: break-word; }
    </style>
    </head>
    <body>$html</body>
    </html>
    """.trimIndent()

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
        CalendarMode.Agenda -> "Upcoming"
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

private fun eventsForDay(events: List<JSONObject>, day: LocalDate): List<JSONObject> =
    events.filter { eventDate(it) == day }

private fun eventDate(event: JSONObject): LocalDate? =
    runCatching {
        Instant.parse(event.optString("startsAt"))
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }.getOrNull()

private fun eventLocalDateTime(value: String, fallback: Instant): LocalDateTime =
    runCatching {
        Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }.getOrDefault(fallback.atZone(ZoneId.systemDefault()).toLocalDateTime())

private fun formatEventTime(event: JSONObject): String {
    val start = event.optString("startsAt")
    val end = event.optString("endsAt")
    return runCatching {
        val zone = ZoneId.systemDefault()
        val startZdt = Instant.parse(start).atZone(zone)
        if (event.optBoolean("allDay")) {
            val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d")
            val lastDay = Instant.parse(end).atZone(zone).toLocalDate().minusDays(1)
            if (lastDay <= startZdt.toLocalDate()) {
                "All day · ${startZdt.format(dateFmt)}"
            } else {
                "All day · ${startZdt.format(dateFmt)} - ${lastDay.format(dateFmt)}"
            }
        } else {
            val formatter = DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a")
            val startText = startZdt.format(formatter)
            val endText = Instant.parse(end).atZone(zone).format(DateTimeFormatter.ofPattern("h:mm a"))
            "$startText - $endText"
        }
    }.getOrDefault("Invalid event time")
}

private fun replyDraft(message: JSONObject, folder: String, uid: Int): ComposeDraft {
    val from = message.optJSONArray("from")?.optJSONObject(0)?.optString("address").orEmpty()
    val subject = prefixedSubject(message.optString("subject"), "Re:")
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
    val subject = prefixedSubject(message.optString("subject"), "Fwd:")
    val body = message.optString("text").ifBlank { stripHtml(message.optString("html")) }
    return ComposeDraft(
        subject = subject,
        body = "\n\n---------- Forwarded message ----------\n" +
            "From: ${addressLine("", message.optJSONArray("from")).removePrefix(": ").trim()}\n" +
            "Date: ${message.optString("date")}\n" +
            "Subject: ${message.optString("subject")}\n\n$body",
    )
}

private fun prefixedSubject(subject: String, prefix: String): String {
    val value = subject.trim().ifBlank { "(No subject)" }
    return if (value.startsWith(prefix, true)) value else "$prefix $value"
}
