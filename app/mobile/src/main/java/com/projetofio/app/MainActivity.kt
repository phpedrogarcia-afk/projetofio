package com.projetofio.app

import android.net.Uri
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
import com.projetofio.app.domain.AppLockMode
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
    private val viewModel by viewModels<FioViewModel> { FioViewModel.Factory(graph.service) }
    private lateinit var authenticator: DeviceAuthenticator
    private val appLockPolicy = AppLockPolicy()
    private var privacyCover by mutableStateOf(true)
    private var locked by mutableStateOf(false)
    private var authenticationInFlight = false
    private var backgroundStartedAt: Long? = null
    private var safeOpenFailure by mutableStateOf(false)
    private var exportMessage by mutableStateOf<String?>(null)
    private val exportCoordinator = ExportCoordinator()
    private val documentWriter by lazy { AndroidDocumentWriter(contentResolver) }

    // Activity-scoped registration must survive the privacy cover replacing FioApp onPause.
    private val markdownExportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(ExportFormat.MARKDOWN.mimeType),
    ) { uri -> writeExport(uri, ExportFormat.MARKDOWN) }
    private val textExportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(ExportFormat.PLAIN_TEXT.mimeType),
    ) { uri -> writeExport(uri, ExportFormat.PLAIN_TEXT) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        authenticator = DeviceAuthenticator(this)
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
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            runCatching { graph.service.loadSettings().appLockMode }
                .onSuccess { mode ->
                    safeOpenFailure = false
                    locked = shouldLock(mode)
                    privacyCover = false
                    if (locked) requestUnlock()
                }
                .onFailure {
                    locked = false
                    safeOpenFailure = true
                    privacyCover = false
                }
        }
    }

    override fun onPause() {
        privacyCover = true
        super.onPause()
    }

    override fun onStop() {
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
}
