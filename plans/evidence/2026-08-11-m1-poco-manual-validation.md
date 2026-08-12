# M1 POCO manual validation evidence — 2026-08-11

Status: Partial physical evidence; remaining observations retained as ADR-037
pre-pilot gates

This evidence belongs to
`plans/2026-08-10-m1-android-trusted-local-core.md`. ADR-037 separately
authorizes synthetic-data M2–M4 engineering; this evidence does not authorize a
pilot, release, signing, publication, or a broader compatibility claim.

## Privacy boundary

- Physical target: project owner's POCO M3 Pro 5G, Android 13/API 33.
- No serial, IMEI, account, journal text, screenshot, or UI hierarchy was
  retained.
- Checks below did not inspect or change the primary Fio database.
- A separate `validation` build uses application ID
  `com.projetofio.app.validation`, its own sandbox, and its own Keystore
  namespace for future synthetic manual checks.

## Recorded device state

| Item | Evidence |
|---|---|
| Display | 1080×2400, 440 dpi |
| Free `/data` storage | Approximately 30 GB; this is not low-storage evidence |
| Initial/restored font scale | `1.0` |
| Initial/restored rotation | auto `0`, user rotation `0` |
| Initial/restored animation scales | window/transition/animator `1.0` |
| Accessibility service state | disabled during preflight; TalkBack remains a manual check |
| Installed primary build | `0.1.0-dev`, version code 1, minSdk 26, targetSdk 36 |

## Physical checks completed

| Check | Result | Limitation |
|---|---|---|
| Background process killed, cold reopen | Pass | Does not by itself prove a newly typed Draft round trip |
| `FLAG_SECURE` after process recreation | Pass | OEM Recents visual inspection still manual |
| Start at font scale `1.30` | Pass | No-crash evidence only; clipping/readability still manual |
| Start with all animation scales `0` | Pass | No-crash evidence only; interaction review still manual |
| Start in landscape rotation | Pass | No-crash evidence only; layout/IME review still manual |
| Restore all changed system settings | Pass | Verified back at the recorded initial values |
| Install and cold-launch isolated validation build | Pass | MIUI owner approval completed |
| Synthetic Draft autosave after force-stop | Pass | Exact synthetic ASCII fixture only |
| Save confirmation and Archive after relaunch | Pass | Exact synthetic fixture visible; primary Fio untouched |
| Real SAF Markdown cancellation | Pass | Owner observed silent return with no false success or error |
| Real SAF Markdown write/read | Pass | `Arquivos` provider; owner opened the document and confirmed the exact synthetic phrase |
| Real SAF plain-text launch, pre-fix | Fail | Fio closed before the picker appeared; no document was created and the encrypted database was not changed |
| Corrected SAF plain-text picker launch | Pass on API 26/API 36 and POCO API 33 | Real system picker opened without terminating Fio; process remained alive and no new crash was recorded |
| First POCO TXT save after Fragment fix | Fail | DocumentsUI created `/sdcard/Download/fio-export.txt` with 0 bytes; the synthetic Entry remained visible and decryptable in Archive |
| Activity-scoped export-result correction | Pass on API 26/API 36 and POCO API 33 | Export launcher/result handling moved above the privacy-cover composition; 11/11 tests pass on both emulators |
| Corrected POCO TXT write/read | Pass | `/sdcard/Download/fio-export.txt` was 333 bytes, contained the exact synthetic Entry, showed Fio success, retained the same PID, and added no crash after the 21:32 install |
| Corrected POCO TXT cleanup | Pass | The exact 333-byte synthetic TXT was re-verified and removed; the encrypted validation Entry remains in Fio |

## Isolated manual-test application

The `validation` variant compiled offline and was installed after the owner
approved the MIUI prompt. It cold-launched with `FLAG_SECURE`, recovered the
synthetic Draft `Teste rascunho sintetico M1` after force-stop, saved it, and
showed it in Archive after another relaunch. The primary Fio installation
remains present and untouched.

An attempted coordinate-driven SAF navigation was stopped when MIUI left
WhatsApp foregrounded instead of keeping Fio active. No text was typed or sent
and no SAF result is claimed from that attempt. A later owner-driven Markdown
picker cancellation returned silently and passed. All remaining external UI
work requires direct owner interaction; automation must not tap while another
app can be foregrounded.

## Plain-text launch failure diagnosis

The report sent after tapping **Texto** corresponds to a reproducible process
crash before Android's document picker opened:
`IllegalArgumentException: Can only use lower 16 bits for requestCode`.
The stack reaches `ManagedActivityResultLauncher.launch` from the plain-text
export action. Dependency inspection found that AndroidX Biometric 1.1.0 was
selecting the obsolete transitive AndroidX Fragment 1.2.5 alongside the modern
Activity Result API. The corrective checkpoint explicitly pins AndroidX
Fragment 1.8.9 and adds an instrumented real-picker launch regression test.
The historical pre-fix failure is retained. The corrected build passes the
complete 11-test suite on API 26 and API 36; the POCO picker launch passes and
the corrected physical write/read remains pending.

After the Fragment correction was installed, the POCO proved that **Texto**
opened `com.google.android.documentsui` while the same Fio process remained
alive and no new crash appeared. Saving the first real TXT then exposed a
separate lifecycle defect: DocumentsUI created `fio-export.txt`, but it remained
0 bytes. `onPause` had replaced `FioApp` with `PrivacyCover`, disposing the
Compose-scoped Activity Result launcher before the URI returned. The synthetic
Entry remained visible and decryptable in Archive, so there was no database or
Keystore loss. Export launchers and result writing now live at the Activity
boundary; the privacy cover and `FLAG_SECURE` remain unchanged. The corrected
code passes 11/11 tests on API 26 and API 36. Its POCO re-test wrote a 333-byte
TXT containing the exact synthetic phrase, retained the same process, displayed
the Fio success message, and introduced no crash record. The latest exit record
is only the expected package-install force-stop at 21:32; crash records remain
the historical pre-fix entries ending at 15:47.

The first corrected-APK reinstall attempt did not start because ADB reported no
connected device after the emulator session. Restarting the ADB connection did
not rediscover the POCO. No APK or device data changed in that attempt; physical
confirmation resumes only after the owner reconnects and unlocks the phone.

## Human-observed checks still required

1. Exercise a real provider failure/unavailable destination; remove the
   remaining synthetic Markdown document afterward.
2. Observe fingerprint/device-credential success, lockout,
   immediate/one-minute/five-minute grace, and fresh authorization for export
   and permanent deletion. Cancellation is now closed below.
3. Inspect TalkBack order/announcements, enlarged-font clipping, contrast,
   keyboard, reduced animation, and the neutral Recents cover. Screenshot
   blocking is now closed below.
4. Remove the validation app and any synthetic export after recording results.

## Automated physical follow-up — 2026-08-11

The ADB-authorized POCO was unlocked and the isolated
`com.projetofio.app.validation` package was updated to the already-tested
validation APK. No emulator runner selected the phone, no PIN was entered, no
fingerprint was simulated, and no personal screen image was copied.

- **Capture control:** the MIUI launcher produced a 2,204,931-byte PNG through
  `screencap`. Immediately afterward, the same command with Fio validation in
  the foreground produced a 0-byte file. Both device files were deleted. This
  proves physical screenshot blocking for the current POCO/MIUI build without
  retaining a launcher image.
- **Credential cancellation:** the isolated app lock was set to Immediate. On
  cold reopen, the prompt belonged to `com.android.systemui`. Pressing Back
  cancelled it; the Fio window then exposed only `Fio`, `Protegido pelo
  bloqueio do seu aparelho.`, and `Abrir`, with zero unexpected visible text.
- **Cleanup:** only `com.projetofio.app.validation` data was cleared after the
  check. The package reopened at the empty Write screen with app lock off. The
  primary `com.projetofio.app` package and its data were not modified.

This closes physical screenshot blocking and device-authentication cancellation
on the POCO. It does not close Recents-card appearance, authentication success,
fingerprint behavior, lockout, grace modes, or fresh protected-action auth.

## Compatibility limitation

The owner has no second physical Android device. A second manufacturer is
therefore **not tested** and remains an explicit external acceptance gap. API
26/API 36 emulator evidence and the POCO API 33 evidence do not substitute for
another OEM. This is not a waiver or a claim of universal Android support.
