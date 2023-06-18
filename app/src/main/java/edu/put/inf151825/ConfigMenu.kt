package edu.put.inf151825

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime

class ConfigMenu : AppCompatActivity() {
    lateinit var progressDialog: AlertDialog
    lateinit var sharedPreferences: SharedPreferences
    private val KEY_USERNAME = "KEY_USERNAME"
    private val KEY_USERDATE = "KEY_USERDATE"
    var PREFS_KEY = "prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_menu)
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    }

    fun login(v: View) {
        val user: EditText = findViewById(R.id.userName)
        if (user.text.toString() != "") {
            val userName = user.text.toString()
            val progressView: View = layoutInflater.inflate(R.layout.progress_popup, null)
            val dialogBuilder = AlertDialog.Builder(this@ConfigMenu)
            dialogBuilder.setView(progressView)
            progressDialog = dialogBuilder.create()
            progressDialog.show()

            val i = Intent(this, SynchroMenu::class.java)
            i.putExtra("isLoggingIn", true)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()

            editor.putString(KEY_USERNAME, userName)
            editor.putString(KEY_USERDATE, LocalDateTime.now().toString())

            editor.apply()
            startActivity(i)
        }
    }
}