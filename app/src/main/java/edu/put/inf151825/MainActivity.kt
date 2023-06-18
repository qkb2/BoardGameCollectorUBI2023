package edu.put.inf151825

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


class MainActivity : AppCompatActivity() {
    var userName = ""
    var numberOfGames = 0
    var numberOfAddons = 0
    var date = "2023-01-01"
    private val KEY_USERNAME = "KEY_USERNAME"
    private val KEY_USERDATE = "KEY_USERDATE"
    var PREFS_KEY = "prefs"
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

        userName = sharedPreferences.getString(KEY_USERNAME, userName).toString()
        date = sharedPreferences.getString(KEY_USERDATE, date).toString()

        val db = MainDatabaseHelper(this, null, null, 1)
        numberOfGames = db.countGames()
        numberOfAddons = db.countAddons()

        val user1: TextView = findViewById(R.id.user)
        if (userName == "") {
            val i = Intent(this, ConfigMenu::class.java)
            startActivity(i)
        }
        loadUser()

    }

    private fun loadUser() {
        val user: TextView = findViewById(R.id.user)
        val message1 = "Cześć, $userName!"
        user.text = message1

        val games: TextView = findViewById(R.id.games)
        val message2 = "Liczba gier: $numberOfGames"
        games.text = message2

        val dlc: TextView = findViewById(R.id.addons)
        val message3 = "Liczba dodatków: $numberOfAddons"
        dlc.text = message3

        val data1: TextView = findViewById(R.id.date)
        val message4 = "Data synchronizacji: $date"
        data1.text = message4
    }

    fun gamesActivity(v: View) {
        val i = Intent(this, GamesList::class.java)
        i.putExtra("addons", "false")
        startActivity(i)
    }

    fun addonsActivity(v: View) {
        val i = Intent(this, GamesList::class.java)
        i.putExtra("addons", "true")
        startActivity(i)
    }

    fun synchroActivity(v: View) {
        val i = Intent(this, SynchroMenu::class.java)
        i.putExtra("isLoggingIn", false)
        startActivity(i)
    }

    fun logout(v: View) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setMessage("Na pewno chesz usunąć dane?").setCancelable(false)
            .setPositiveButton("Tak") { _, _ ->
                val path = Paths.get("$filesDir/date.txt")
                try {
                    val result = Files.deleteIfExists(path)
                    if (result) {
                        println("Usunięto dane")
                    } else {
                        println("Usuwanie zakończyło się niepowodzeniem.")
                    }
                } catch (e: IOException) {
                    println("Usuwanie zakończyło się niepowodzeniem.")
                    e.printStackTrace()
                }
                val dbHandler = MainDatabaseHelper(this, null, null, 1)
                val phdbHandler = PhotoDatabaseHelper(this, null, null, 1)
                dbHandler.clear()
                phdbHandler.clear()
                finish()
                userName = ""
                numberOfAddons = 0
                numberOfGames = 0
                date = ""
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                val i = Intent(this, ConfigMenu::class.java)
                startActivity(i)
            }.setNegativeButton("Nie") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
}