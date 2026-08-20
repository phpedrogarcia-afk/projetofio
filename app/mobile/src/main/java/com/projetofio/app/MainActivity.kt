package com.projetofio.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.net.Uri
import android.provider.OpenableColumns
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.projetofio.app.application.ExportCoordinator
import com.projetofio.app.application.ExportFormat
import com.projetofio.app.application.ExportOutcome
import com.projetofio.app.application.AndroidDocumentWriter
import com.projetofio.app.application.AndroidImportDocumentReader
import com.projetofio.app.domain.ImportSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import com.projetofio.app.domain.AppLockMode
import com.projetofio.app.domain.NotificationPermissionObserved
import com.projetofio.app.security.DeviceAuthenticator
import com.projetofio.app.security.AppLockPolicy
import com.projetofio.app.ui.FioApp
import com.projetofio.app.ui.FioViewModel
import com.projetofio.app.ui.LockedScreen
import com.projetofio.app.ui.PrivacyCover
import com.projetofio.app.ui.SafeOpenFailure
import com.projetofio.app.ui.theme.FioTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val graph get() = (application as FioApplication).graph
    private val viewModel by viewModels<FioViewModel> {
        FioViewModel.Factory(
            graph.service,
            graph.timeReturns,
            graph.localImport,
            BuildConfig.TIME_RETURNS_ENGINEERING_ENABLED,
            BuildConfig.LOCAL_IMPORT_ENGINEERING_ENABLED,
            graph.search,
        )
    }
    private lateinit var authenticator: DeviceAuthenticator
    private val appLockPolicy = AppLockPolicy()
    private var privacyCover by mutableStateOf(true)
    private var locked by mutableStateOf(false)
    private var authenticationInFlight = false
    private var accessGateResolved = false
    private var accessGateJob: Job? = null
    private var backgroundStartedAt: Long? = null
    private var safeOpenFailure by mutableStateOf(false)
    private var exportMessage by mutableStateOf<String?>(null)
    private val exportCoordinator = ExportCoordinator()
    private val documentWriter by lazy { AndroidDocumentWriter(contentResolver) }
    private val importReader by lazy { AndroidImportDocumentReader(contentResolver) }
    private var pendingReturnIntentId: String? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.observeNotificationPermission(
            if (granted) NotificationPermissionObserved.GRANTED else NotificationPermissionObserved.DENIED,
        )
    }

    // Activity-scoped registration must survive the privacy cover replacing FioApp onPause.
    private val markdownExportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(ExportFormat.MARKDOWN.mimeType),
    ) { uri -> writeExport(uri, ExportFormat.MARKDOWN) }
    private val textExportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(ExportFormat.PLAIN_TEXT.mimeType),
    ) { uri -> writeExport(uri, ExportFormat.PLAIN_TEXT) }
    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) readImport(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.SCREEN_CAPTURE_ALLOWED) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        authenticator = DeviceAuthenticator(this)
        captureReturnIntent(intent)
        setContent {
            FioTheme {
                when {
                    privacyCover -> PrivacyCover()
                    safeOpenFailure -> SafeOpenFailure()
                    locked -> LockedScreen(
                        authenticationAvailable = authenticator.isAvailable(),
                        onUnlock = ::requestUnlock,
                        onDisableUnavailableLock = ::disableUnavailableLock,
                    )
                    else -> FioApp(
                        viewModel = viewModel,
                        authorizeFresh = ::authorizeFresh,
                        authenticationAvailable = authenticator.isAvailable(),
                        exportMessage = exportMessage,
                        onExportMarkdown = {
                            authorizeFresh { launchExport(ExportFormat.MARKDOWN) }
                        },
                        onExportText = {
                            authorizeFresh { launchExport(ExportFormat.PLAIN_TEXT) }
                        },
                        onEnableReturns = { viewModel.enableReturns(::requestNotificationPermissionAfterConsent) },
                        onOpenPendingReturn = viewModel::openReturn,
                        onSelectImport = {
                            authorizeFresh { importLauncher.launch(arrayOf("text/plain", "text/markdown")) }
                        },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        resolveForegroundAccess()
    }

    private fun resolveForegroundAccess() {
        accessGateResolved = false
        privacyCover = true
        accessGateJob?.cancel()
        accessGateJob = lifecycleScope.launch {
            try {
                val mode = graph.service.loadSettings().appLockMode
                safeOpenFailure = false
                locked = shouldLock(mode)
                accessGateResolved = true
                privacyCover = false
                if (locked) requestUnlock()
                if (!locked) openCapturedReturn()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                locked = false
                safeOpenFailure = true
                accessGateResolved = true
                privacyCover = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // A transient system surface (for example, the Android notification
        // permission dialog) may call onPause/onResume without onStop/onStart.
        // In that path onStart cannot remove the privacy cover, so clear it
        // only after the current foreground access gate has already resolved.
        if (!accessGateResolved && accessGateJob?.isActive != true) {
            // ActivityScenario and some vendor lifecycle paths can resume after
            // a stopped access-gate job without delivering the normal onStart
            // sequence. Re-resolve instead of leaving the cover indefinitely.
            resolveForegroundAccess()
        } else if (accessGateResolved && !locked && !safeOpenFailure && !authenticationInFlight) {
            privacyCover = false
            openCapturedReturn()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureReturnIntent(intent)
        if (!locked && !privacyCover) openCapturedReturn()
    }

    override fun onPause() {
        privacyCover = true
        super.onPause()
    }

    override fun onStop() {
        accessGateResolved = false
        accessGateJob?.cancel()
        accessGateJob = null
        backgroundStartedAt = SystemClock.elapsedRealtime()
        super.onStop()
    }

    private fun shouldLock(mode: AppLockMode): Boolean {
        return appLockPolicy.shouldLock(
            mode = mode,
            backgroundStartedAtMillis = backgroundStartedAt,
            nowMillis = SystemClock.elapsedRealtime(),
        )
    }

    private fun requestUnlock() {
        if (authenticationInFlight) return
        authenticationInFlight = true
        authenticator.authenticate(
            title = "Abrir o Fio",
            onSuccess = {
                authenticationInFlight = false
                backgroundStartedAt = null
                locked = false
                privacyCover = false
                openCapturedReturn()
            },
            onFinishedWithoutSuccess = {
                authenticationInFlight = false
                locked = true
                privacyCover = false
            },
        )
    }

    private fun authorizeFresh(onSuccess: () -> Unit) {
        lifecycleScope.launch {
            if (graph.service.loadSettings().appLockMode == AppLockMode.OFF) {
                onSuccess()
            } else {
                authenticator.authenticate(
                    title = "Confirmar ação protegida",
                    onSuccess = onSuccess,
                )
            }
        }
    }

    private fun disableUnavailableLock() {
        lifecycleScope.launch {
            runCatching { graph.service.setAppLockMode(AppLockMode.OFF) }
                .onSuccess {
                    backgroundStartedAt = null
                    locked = false
                    privacyCover = false
                }
                .onFailure {
                    safeOpenFailure = true
                    locked = false
                    privacyCover = false
                }
        }
    }

    private fun launchExport(format: ExportFormat) {
        exportMessage = null
        when (format) {
            ExportFormat.MARKDOWN -> markdownExportLauncher.launch("fio-export.md")
            ExportFormat.PLAIN_TEXT -> textExportLauncher.launch("fio-export.txt")
        }
    }

    private fun writeExport(uri: Uri?, format: ExportFormat) {
        lifecycleScope.launch {
            exportMessage = when (
                exportCoordinator.export(
                    destination = uri,
                    format = format,
                    buildDocument = viewModel::buildExport,
                    writeDocument = documentWriter::write,
                )
            ) {
                ExportOutcome.SUCCESS -> "Exportação concluída no local escolhido."
                ExportOutcome.CANCELLED -> null
                ExportOutcome.FAILED -> "Não foi possível concluir a exportação. Nenhuma cópia adicional foi mantida pelo Fio."
            }
        }
    }

    private fun requestNotificationPermissionAfterConsent() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.observeNotificationPermission(NotificationPermissionObserved.GRANTED)
        }
    }

    private fun readImport(uri: Uri) {
        lifecycleScope.launch {
            runCatching {
                val name = displayName(uri)
                val lowerName = name?.lowercase()
                require(lowerName == null || lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".markdown")) {
                    "Unsupported import extension"
                }
                val source = if (
                    lowerName?.endsWith(".md") == true ||
                    lowerName?.endsWith(".markdown") == true ||
                    contentResolver.getType(uri) == "text/markdown"
                ) {
                    ImportSource.MARKDOWN
                } else {
                    ImportSource.TEXT
                }
                val bytes = withContext(Dispatchers.IO) { importReader.read(uri) }
                Triple(bytes, source, name)
            }.onSuccess { (bytes, source, name) ->
                viewModel.previewImport(bytes, source, name)
            }.onFailure {
                viewModel.importReadFailed()
            }
        }
    }

    private fun displayName(uri: Uri): String? = contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }

    private fun captureReturnIntent(intent: Intent?) {
        if (intent?.action == ACTION_OPEN_RETURN) {
            pendingReturnIntentId = intent.getStringExtra(EXTRA_RETURN_ID)
        }
    }

    private fun openCapturedReturn() {
        val id = pendingReturnIntentId ?: return
        pendingReturnIntentId = null
        viewModel.openReturn(id)
    }

    companion object {
        const val ACTION_OPEN_RETURN = "com.projetofio.app.action.OPEN_RETURN"
        const val EXTRA_RETURN_ID = "return_id"
    }
}
