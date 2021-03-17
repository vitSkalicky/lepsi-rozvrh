package cz.vitskalicky.lepsirozvrh.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cz.vitskalicky.lepsirozvrh.AppSingleton
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.R
import cz.vitskalicky.lepsirozvrh.Utils
import cz.vitskalicky.lepsirozvrh.donations.Donations
import cz.vitskalicky.lepsirozvrh.model.relations.RozvrhRelated
import cz.vitskalicky.lepsirozvrh.widget.WidgetThemeFragment.CallbackListener
import kotlinx.coroutines.launch

/**
 * A base class for Widget configuration activities taking care of saving the data, OK, button and spinner.
 */
abstract class WidgetConfigActivity : AppCompatActivity(), CallbackListener {
    protected var widgetThemeFragment: WidgetThemeFragment? = null
    protected var okButton: Button? = null
    protected var donations: Donations? = null
    var widgetID = 0
    var isWidgetIDSet = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createContentView()
        donations = Donations(this, this) {
            //on sponsor change
            widgetThemeFragment?.updateSponsor()
        }
        if (widgetThemeFragment == null) {
            widgetThemeFragment = supportFragmentManager.findFragmentById(R.id.widgetThemeFragment) as WidgetThemeFragment?
            if (widgetThemeFragment == null) {
                throw RuntimeException("Layout must contain a WidgetThemeFragment with id \"widgetThemeFragment\" or the protected field \"widgetThemeFragment\" must be set in \"createContentView()\"")
            }
        }
        widgetThemeFragment!!.init(donations)
        if (okButton == null) {
            okButton = findViewById(R.id.buttonOK)
            if (okButton == null) {
                throw RuntimeException("Layout must contain a Button with id \"okButton\" or the protected field \"okButton\" must be set in \"createContentView()\"")
            }
        }
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            widgetID = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
            isWidgetIDSet = true
            widgetThemeFragment!!.setWidgetID(widgetID)
        }
        okButton!!.setOnClickListener { v: View? ->
            widgetThemeFragment!!.saveConfig()
            if (isWidgetIDSet) {
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
                setResult(RESULT_OK, resultValue)

                lifecycleScope.launch{
                    val rozvrh: RozvrhRelated? = (application as? MainApplication)?.repository?.getRozvrh(Utils.getCurrentMonday(),true)
                    if (rozvrh != null) {
                        val tmp = rozvrh.getWidgetDisplayBlocks(5)
                        WidgetProvider.update(this@WidgetConfigActivity,widgetID,tmp?.first, tmp?.second )
                        finish()
                    }
                }
            } else {
                finish()
            }
        }
        widgetThemeFragment!!.setCallbackListener(this)
    }

    protected abstract fun createContentView()
    override fun onBackPressed() {
        if (isWidgetIDSet) {
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
            setResult(RESULT_CANCELED, resultValue)
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        donations!!.release()
    }

    companion object {
        private val TAG = WidgetConfigActivity::class.java.simpleName
    }
}