package cz.vitskalicky.lepsirozvrh.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import cz.vitskalicky.lepsirozvrh.MainApplication
import cz.vitskalicky.lepsirozvrh.R
import cz.vitskalicky.lepsirozvrh.SharedPrefs
import cz.vitskalicky.lepsirozvrh.bakaAPI.login.Login.LoginResult
import cz.vitskalicky.lepsirozvrh.theme.Theme
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation

class LoginActivity : BaseActivity() {
    lateinit var tilUsername: TextInputLayout
    lateinit var tilPassword: TextInputLayout
    lateinit var tilURL: TextInputLayout
    lateinit var bChooseSchool: Button
    lateinit var bLogin: Button
    lateinit var progressBar: ProgressBar
    lateinit var twMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        tilUsername = findViewById(R.id.textInputLayoutName)
        tilPassword = findViewById(R.id.textInputLayoutPassword)
        tilURL = findViewById(R.id.textInputLayoutURL)
        bChooseSchool = findViewById(R.id.buttonSchoolList)
        bLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)
        twMessage = findViewById(R.id.textViewMessage)
        tilUsername.isErrorEnabled = true
        tilPassword.isErrorEnabled = true
        tilURL.isErrorEnabled = true
        progressBar.visibility = View.GONE
        twMessage.text = ""
        twMessage.setTextColor(Theme.of(this).cError)

        if (tilUsername.editText!!.text.toString().isEmpty()) tilUsername.editText!!.setText(SharedPrefs.getString(this, SharedPrefs.USERNAME))
        if (tilURL.editText!!.text.toString().isEmpty()) tilURL.editText!!.setText(SharedPrefs.getString(this, SharedPrefs.URL))
        (applicationContext as MainApplication).login.logout()
        bChooseSchool.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this, SchoolsListActivity::class.java)
            startActivityForResult(intent, REQUEST_PICK_SCHOOL)
        })
        bLogin.setOnClickListener(View.OnClickListener { v: View? ->
            val url = tilURL.editText!!.text.toString()
            if (url.startsWith("http://")) {
                //todo ban http unless debug is enabled
                showUnsecureConnectionWanrning()
            } else {
                logIn()
            }
        })
    }

    fun logIn() {
        bLogin.isEnabled = false
        progressBar.visibility = View.VISIBLE
        twMessage.text = ""

        /*tilUsername.setError(null);
            tilPassword.setError(null);
            tilURL.setError(null);*/
        tilUsername.isErrorEnabled = false
        tilPassword.isErrorEnabled = false
        tilURL.isErrorEnabled = false
        if (tilURL.editText!!.text.toString().isBlank()) {
            tilURL.error = getText(R.string.enter_url)
            bLogin.isEnabled = true
            progressBar.visibility = View.GONE
            return
        }
        if (tilUsername.editText!!.text.toString().isBlank()) {
            tilUsername.error = getText(R.string.enter_username)
            bLogin.isEnabled = true
            progressBar.visibility = View.GONE
            return
        }
        if (tilPassword.editText!!.text.toString().isBlank()) {
            tilPassword.error = getText(R.string.enter_password)
            bLogin.isEnabled = true
            progressBar.visibility = View.GONE
            return
        }
        lifecycleScope.launch {
            val result = (applicationContext as MainApplication).login.firstLogin(tilURL.editText!!.text.toString(), tilUsername.editText!!.text.toString(), tilPassword.editText!!.text.toString())

            if (result === LoginResult.SUCCESS) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                return@launch
            }
            bLogin.isEnabled = true
            progressBar.visibility = View.GONE
            if (result === LoginResult.WRONG_LOGIN) {
                tilUsername.error = getText(R.string.invalid_login)
                tilPassword.error = getText(R.string.invalid_login)
            }
            if (result === LoginResult.UNREACHABLE) {
                twMessage.setText(R.string.unreachable)
                tilURL.error = " "
            }
            if (result === LoginResult.UNEXPECTED_RESPONSE) {
                tilURL.error = getText(R.string.unexpected_response)
            }
            /*if (code === Login.ROZVRH_DISABLED) {
                tilURL!!.error = " "
                twMessage!!.setText(R.string.schedule_disabled)
            }*/
        }
    }

    fun showUnsecureConnectionWanrning() {
        val adb = AlertDialog.Builder(this)
        adb.setTitle(R.string.unsecure_connectio_title)
                .setMessage(R.string.unsecure_connectio)
                .setPositiveButton(R.string.unsecure_connection_connect) { dialog, which -> logIn() }
                .setNegativeButton(R.string.unsecure_connection_cancel) { dialog, which -> }
                .setIcon(R.drawable.ic_no_encryption_black_24dp)
        adb.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_SCHOOL && resultCode == SchoolsListActivity.RESULT_OK && data != null) {
            val url = data.getStringExtra(SchoolsListActivity.EXTRA_URL)
            if (url != null) {
                tilURL!!.editText!!.setText(url)
            } else {
                Log.e(TAG, "No extra containing url (extra key: " + SchoolsListActivity.EXTRA_URL + ")")
            }
        }
    }

    companion object {
        val TAG = LoginActivity::class.java.simpleName

        /**
         * when you want this activity to log out as soon as it starts, pass this in intent extras.
         */
        const val LOGOUT = "logout"
        const val REQUEST_PICK_SCHOOL = 64585 //random number
    }
}