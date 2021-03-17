package cz.vitskalicky.lepsirozvrh.settings

import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import cz.vitskalicky.lepsirozvrh.*
import cz.vitskalicky.lepsirozvrh.activity.LicencesActivity
import cz.vitskalicky.lepsirozvrh.donations.Donations
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.notification.PermanentNotification.showInfoDialog
import cz.vitskalicky.lepsirozvrh.whatsnew.WhatsNewFragment
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : MyCyaneaPreferenceFragmentCompat() {
    private var logoutListener = Utils.Listener {}
    private var shownThemeSettingsListener = Utils.Listener {}
    private var donations: Donations? = null
    private var supportingEnabled = false
    private var isSponsor = false
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    fun init(donations: Donations?) {
        this.donations = donations
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<Preference>(getString(R.string.PREFS_LOGOUT))!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            logoutListener.method()
            true
        }
        findPreference<Preference>(getString(R.string.PREFS_APP_THEME_SCREEN))!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            shownThemeSettingsListener.method()
            true
        }
        findPreference<Preference>(getString(R.string.PREFS_DONATE))!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            donations!!.showDialog()
            true
        }
        findPreference<Preference>(getString(R.string.PREFS_RESTORE_PURCHASES))!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            donations!!.restorePurchases()
            Snackbar.make(requireView(), R.string.purchases_restored, BaseTransientBottomBar.LENGTH_SHORT).show()
            true
        }
        val sendCrashReportsPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.PREFS_SEND_CRASH_REPORTS))
        //Crash reports are allowed on official release builds only. (see build.gradle)
        if (BuildConfig.ALLOW_SENTRY) {
            sendCrashReportsPreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                if (newValue is Boolean && activity != null) {
                    if (newValue) {
                        (requireActivity().application as MainApplication).enableSentry()
                    } else {
                        (requireActivity().application as MainApplication).diableSentry()
                    }
                }
                true
            }
            sendCrashReportsPreference.isVisible = true
        } else {
            sendCrashReportsPreference!!.isVisible = false
        }
        findPreference<Preference>(getString(R.string.PREFS_OSS_LICENCES))!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            val i = Intent(context, LicencesActivity::class.java)
            startActivity(i)
            true
        }
        findPreference<Preference>(getString(R.string.PREFS_SEND_FEEDBACK))!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            val ad = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.include_schedule)
                    .setMessage(R.string.include_schedule_desc)
                    .setNegativeButton(R.string.no) { dialog: DialogInterface?, which: Int -> sendFeedback(false) }
                    .setPositiveButton(R.string.yes) { dialog: DialogInterface?, which: Int -> sendFeedback(true) }
                    .setOnCancelListener { dialog: DialogInterface? -> sendFeedback(false) }
                    .create()
            ad.show()
            true
        }
        val userInfo = findPreference<Preference>(getString(R.string.PREFS_USER))
        userInfo!!.title = SharedPrefs.getString(context, SharedPrefs.NAME)
        val type = SharedPrefs.getString(context, SharedPrefs.TYPE)
        userInfo.summary = SharedPrefs.getString(context, SharedPrefs.TYPE_TEXT)
        val switchToNextWeek = findPreference<ListPreference>(getString(R.string.PREFS_SWITCH_TO_NEXT_WEEK))
        switchToNextWeek!!.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        val notificationPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.PREFS_NOTIFICATION))
        notificationPreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
            if (newValue as Boolean) {
                showInfoDialog(context, false)
                (requireContext().applicationContext as MainApplication).enableNotification()
            } else {
                (requireContext().applicationContext as MainApplication).disableNotification()
            }
            true
        }
        val appVersionPreference = findPreference<Preference>(getString(R.string.PREFS_APP_VERSION))
        val versionText = BuildConfig.FLAVOR + "-" + BuildConfig.BUILD_TYPE + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.GitHash + ")"
        appVersionPreference!!.summary = versionText
        appVersionPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(versionText, versionText)
            clipboard.setPrimaryClip(clip)
            Snackbar.make(requireView(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show()
            true
        }
        findPreference<Preference>(getString(R.string.PREFS_CHANGELOG))!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            val whatsNewFragment = WhatsNewFragment()
            whatsNewFragment.show(requireActivity().supportFragmentManager, "dialog")
            true
        }
    }

    fun setLogoutListener(listener: Utils.Listener) {
        logoutListener = listener
    }

    fun setShownThemeSettingsListener(listener: Utils.Listener) {
        shownThemeSettingsListener = listener
    }

    override fun onResume() {
        super.onResume()
        setSponsor(isSponsor)
        setSupportingEnabled(supportingEnabled)
    }

    fun setSupportingEnabled(supportingEnabled: Boolean) {
        this.supportingEnabled = supportingEnabled
        if (isResumed) {
            findPreference<Preference>(getString(R.string.PREFS_DONATE))!!.isVisible = supportingEnabled
            findPreference<Preference>(getString(R.string.PREFS_RESTORE_PURCHASES))!!.isVisible = supportingEnabled
        }
    }

    fun setSponsor(sponsor: Boolean) {
        isSponsor = sponsor
        if (isResumed) {
            val donatePref = findPreference<Preference>(getString(R.string.PREFS_DONATE))
            if (sponsor) {
                donatePref!!.setTitle(R.string.supporting_this_app)
                donatePref.setSummary(R.string.supporting_this_app_desc)
            } else {
                donatePref!!.setTitle(R.string.support_this_app)
                donatePref.setSummary(R.string.support_this_app_desc)
            }
        }
    }


    fun sendFeedback(includeRozvrh: Boolean) {
        val context = context
        val view = view
        if (context == null || view == null){
            return
        }
        var body: String? = null
        try {
            body = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            body = """

-----------------------------
${context.getString(R.string.email_message)}
 Device OS: Android 
 Device OS version: ${Build.VERSION.RELEASE}
 App Version: $body
 Commit hash: ${BuildConfig.GitHash}
 Build type: ${BuildConfig.BUILD_TYPE}
 Device Brand: ${Build.BRAND}
 Device Model: ${Build.MODEL}
 Device Manufacturer: ${Build.MANUFACTURER}"""
            body += if (Sentry.getContext() != null && Sentry.getContext().user != null) {
                """
 Sentry client id: ${Sentry.getStoredClient().context.user.id}"""
            } else {
                "\n Sentry client id not available"
            }
            body += """
 Sentry enabled: ${SharedPrefs.getBooleanPreference(context, R.string.PREFS_SEND_CRASH_REPORTS)}"""
            val finBody: String = body
            if (includeRozvrh) {
                val mainApplication = context.applicationContext as MainApplication
                lifecycleScope.launch(Dispatchers.IO) {
                    val current = mainApplication.repository.getRozvrh(Utils.getDisplayWeekMonday(context), true)
                    val currentText = MainApplication.objectMapper.writeValueAsString(current);
                    val perm = mainApplication.repository.getRozvrh(Rozvrh.PERM, true)
                    val permText = MainApplication.objectMapper.writeValueAsString(perm);
                    withContext(Dispatchers.Main){
                        var newBody = finBody
                        newBody += "\nCurrent schedule:\n\n$currentText\n"
                        newBody += "\nPermanent schedule:\n\n$permText\n"
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "message/rfc822"
                        val address = context.getString(R.string.CONTACT_MAIL)
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "")
                        intent.putExtra(Intent.EXTRA_TEXT, newBody)
                        try {
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)))
                        } catch (ex: ActivityNotFoundException) {
                            val snackbar = Snackbar.make(view, context.getText(R.string.no_email_client), Snackbar.LENGTH_LONG)
                            snackbar.setAction(R.string.copy_address) { v ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(address, address)
                                clipboard.setPrimaryClip(clip)
                                Snackbar.make(view, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show()
                            }
                            snackbar.show()
                        }
                    }
                }

                /*Thread {
                    val fileCurrent = "rozvrh-" + Utils.dateToString(Utils.getDisplayWeekMonday(context)) + ".xml"
                    val filePerm = "rozvrh-perm.xml"
                    var current = ""
                    var permanent = ""
                    try {
                        context.openFileInput(fileCurrent).use { inputStream ->
                            //converts inputStream to string
                            val s = Scanner(inputStream).useDelimiter("\\A")
                            current = if (s.hasNext()) s.next() else ""
                        }
                    } catch (e: FileNotFoundException) {
                        current = "File not found: " + e.message
                    } catch (e: IOException) {
                        current = "IOException: " + e.message
                    }
                    try {
                        context.openFileInput(filePerm).use { inputStream ->
                            //converts inputStream to string
                            val s = Scanner(inputStream).useDelimiter("\\A")
                            permanent = if (s.hasNext()) s.next() else ""
                        }
                    } catch (e: FileNotFoundException) {
                        permanent = "File not found: " + e.message
                    } catch (e: IOException) {
                        permanent = "IOException: " + e.message
                    }
                    val finCurrent = current
                    val finPermanent = permanent
                    Handler(Looper.getMainLooper()).post {
                        var newBody = finBody
                        newBody += "\nCurrent schedule:\n\n$finCurrent\n"
                        newBody += "\nPermanent schedule:\n\n$finPermanent\n"
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "message/rfc822"
                        val address = context.getString(R.string.CONTACT_MAIL)
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "")
                        intent.putExtra(Intent.EXTRA_TEXT, newBody)
                        try {
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)))
                        } catch (ex: ActivityNotFoundException) {
                            val snackbar = Snackbar.make(view, context.getText(R.string.no_email_client), Snackbar.LENGTH_LONG)
                            snackbar.setAction(R.string.copy_address) { v ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(address, address)
                                clipboard.setPrimaryClip(clip)
                                Snackbar.make(view, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show()
                            }
                            snackbar.show()
                        }
                    }
                }.run()*/
            } else {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "message/rfc822"
                val address = context.getString(R.string.CONTACT_MAIL)
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                intent.putExtra(Intent.EXTRA_TEXT, body)
                try {
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)))
                } catch (ex: ActivityNotFoundException) {
                    val snackbar = Snackbar.make(view, context.getText(R.string.no_email_client), Snackbar.LENGTH_LONG)
                    snackbar.setAction(R.string.copy_address) { v ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(address, address)
                        clipboard.setPrimaryClip(clip)
                        Snackbar.make(view, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show()
                    }
                    snackbar.show()
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "!", Toast.LENGTH_SHORT).show()
        }
    }

}