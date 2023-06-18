package edu.put.inf151825

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileWriter
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.xml.parsers.DocumentBuilderFactory

class SynchroMenu : AppCompatActivity() {
    var userName = ""
    var numberOfGames = 0
    var numberOfAddons = 0
    var date = "2018-12-30T19:34:50.63"
    private val KEY_DATA = "KEY_DATA"
    lateinit var progressDialog: AlertDialog
    lateinit var sharedPreferences: SharedPreferences
    private val KEY_USERNAME = "KEY_USERNAME"
    private val KEY_USERDATE = "KEY_USERDATE"
    var PREFS_KEY = "prefs"
    var isLoggingIn: Boolean = false


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_DATA, date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_synchro_menu)

        isLoggingIn = intent.getBooleanExtra("isLoggingIn", false)
        sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        userName = sharedPreferences.getString(KEY_USERNAME, userName).toString()
        date = sharedPreferences.getString(KEY_USERDATE, date).toString()

        val data1: TextView = findViewById(R.id.synchroDate)
        data1.text = date

        if (isLoggingIn) {
            if (isOnline()) {
                synchronize(userName)
            }
            else {
                run { Toast.makeText(this, "Brak Internetu. Nie można zalogować.", Toast.LENGTH_LONG).show() }
                val i = Intent(this, ConfigMenu::class.java)
                startActivity(i)
            }
        }
    }

    fun synchronizeClick(v: View) {
        val synchroDate = LocalDateTime.parse(date)
        val today = LocalDateTime.now()

        val howManyHours = ChronoUnit.HOURS.between(synchroDate, today)

        if (!isOnline()) {
            run { Toast.makeText(this, "Brak Internetu. Nie można synchronizować.", Toast.LENGTH_LONG).show() }
            return
        }

        if (howManyHours < 24) {
            val message = AlertDialog.Builder(this@SynchroMenu)
            message.setMessage("Dane są aktualne.\nNa pewno chcesz je zaktualizować?")
                .setCancelable(false).setPositiveButton("Tak") { dialog, _ ->
                    dialog.dismiss()
                    synchronize(userName)
                }.setNegativeButton("Nie") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = message.create()
            alert.show()
        } else {
            synchronize(userName)
        }
    }

    private fun synchronize(user: String) {
        val progressView: View = layoutInflater.inflate(R.layout.progress_popup, null)
        val dialogBuilder = AlertDialog.Builder(this@SynchroMenu)
        dialogBuilder.setView(progressView)
        progressDialog = dialogBuilder.create()
        progressDialog.show()

        val dbHandler = MainDatabaseHelper(this, null, null, 1)
        dbHandler.clear()

        writeToPref()

        val xmlDirectory = File("$filesDir/XML")
        if (!xmlDirectory.exists()) xmlDirectory.mkdir()
        val dirFile1 = "$xmlDirectory/collection.xml"
        val dirFile2 = "$xmlDirectory/addons.xml"


        val q1 =
            "https://boardgamegeek.com/xmlapi2/collection?username=$user&stats=1&subtype=boardgame&excludesubtype=boardgameexpansion"
        val q2 =
            "https://boardgamegeek.com/xmlapi2/collection?username=$user&stats=1&subtype=boardgameexpansion"

//        val q = "https://boardgamegeek.com/xmlapi2/collection?username=$user&stats=1"

        downloadFile(q1, dirFile1)
        downloadFile(q2, dirFile2)

        progressDialog.dismiss()
    }

    fun toMenu(v: View) {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

    private fun end(filename: String) {
//        val dbHandler = MainDatabaseHelper(this, null, null, 1)
//        dbHandler.clear()
        val isSuccessful = saveDataMain(filename)
        if (!isSuccessful) {
            run { Toast.makeText(this, "Niepoprawny login", Toast.LENGTH_LONG).show() }
            val i = Intent(this, ConfigMenu::class.java)
            startActivity(i)
        }
        run { Toast.makeText(this, "Koniec", Toast.LENGTH_LONG).show() }
    }

    private fun writeToPref() {
        val db = MainDatabaseHelper(this, null, null, 1)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_USERNAME, userName)
        editor.putString(KEY_USERDATE, LocalDateTime.now().toString())
        editor.apply()
    }

    private fun saveDataMain(filename: String): Boolean {
        val dbHandler = MainDatabaseHelper(this, null, null, 1)

        val file = File(filename)
        val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        xmlDoc.documentElement.normalize()
        val items: NodeList = xmlDoc.getElementsByTagName("item")


        for (i in 0 until items.length) {
            val itemNode: Node = items.item(i)
            var subtype = itemNode.attributes.getNamedItem("subtype").nodeValue
            if (itemNode.nodeType == Node.ELEMENT_NODE) {
                val elem = itemNode as Element
                val children = elem.childNodes
                var id: String? = null
                var title: String? = null
//                var org_title: String? = null
                var year_pub: String? = null
                var rank_pos: String? = null
                var pic: String? = null
                var photo: String? = null
                var error: String? = null
                var tmp: String? = null
                val tags = itemNode.attributes
                for (j in 0 until tags.length) {
                    val node = tags.item(j)
                    when (node.nodeName) {
                        "objectid" -> {
                            id = node.nodeValue
                        }
                    }
                }
                for (j in 0 until children.length) {
                    val node = children.item(j)
                    if (node is Element) {
                        when (node.nodeName) {
                            "name" -> {
                                title = node.textContent
                            }
                            "yearpublished" -> {
                                year_pub = node.textContent
                            }
                            "thumbnail" -> {
                                pic = node.textContent
                            }
                            "image" -> {
                                photo = node.textContent
                            }
                            "error" -> {
                                error = node.textContent
                            }
                            "stats" -> {
                                val n = node.childNodes
                                for (j1 in 0 until n.length) {
                                    val node = n.item(j1)
                                    if (node is Element) {
                                        when (node.nodeName) {
                                            "rating" -> {
                                                val n = node.childNodes
                                                for (j2 in 0 until n.length) {
                                                    val node = n.item(j2)
                                                    if (node is Element) {
                                                        when (node.nodeName) {
                                                            "ranks" -> {
                                                                val n = node.childNodes
                                                                for (j3 in 0 until n.length) {
                                                                    val node = n.item(j3)
                                                                    if (node is Element) {
                                                                        val tags = node.attributes
                                                                        for (j4 in 0 until tags.length) {
                                                                            val node = tags.item(j4)
                                                                            when (node.nodeName) {
                                                                                "id" -> {
                                                                                    tmp =
                                                                                        node.nodeValue
                                                                                }
                                                                                "value" -> {
                                                                                    rank_pos =
                                                                                        node.nodeValue
                                                                                }
                                                                            }
                                                                            if (tmp == "1" && rank_pos != null) {
                                                                                break
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                val _error: String? = error
                var _expansion: Int = 0
                if (subtype == "boardgameexpansion") {
                    _expansion = 1
                }
                if (rank_pos == "Not Ranked" || rank_pos == null) {
                    rank_pos = "0"
                }
                val _id: Int = Integer.parseInt(id)
                val _title: String? = title
                val _org_title: String? = title
                var _year_pub: Int = 0
                _year_pub = if (year_pub == null) {
                    1900
                } else {
                    Integer.parseInt(year_pub)
                }
                val _rank_pos: Int = Integer.parseInt(rank_pos)
                val _pic: String? = pic
                val _photo: String? = photo

                val product = MainDatabase(
                    _id, _title, _org_title, _year_pub, _rank_pos, _pic, _photo, _error, _expansion
                )
                try {
                    dbHandler.addRecord(product)
                } catch (e: java.lang.Exception) {
                    Log.d("DB Exceptions", "Copy @ $_id")
                }
            }
        }
        return true
    }

    private fun downloadFile(urlString: String, fileName: String) {
//        val xmlDirectory = File("$filesDir/XML")
//        if (!xmlDirectory.exists()) xmlDirectory.mkdir()
//        val fileName = "$xmlDirectory/$name.xml"
        Log.d("Test", "In downloadFile w/ url: $urlString")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlString)
                val reader = url.openStream().bufferedReader()
                val downloadFile = File(fileName). also { it.createNewFile() }
                Log.d("Test", "Created $downloadFile. ${downloadFile.exists()}")
                val writer = FileWriter(downloadFile).buffered()
                var line: String
                while (reader.readLine().also { line = it?.toString() ?: "" } != null)
                    writer.write(line)
                reader.close()
                writer.close()

                withContext(Dispatchers.Main) {
                    Log.d("Test", "In Dispatchers.Main before end()")
                    end(fileName)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    when (e) {
                        is MalformedURLException -> print("malformed URL")
                        else -> print("error")
                    }
                    val incompleteFile = File(fileName)
                    if (incompleteFile.exists()) incompleteFile.delete()
                }
            }
        }
    }

    fun isOnline(): Boolean {
        var b = true
        CoroutineScope(Dispatchers.IO).launch {
            b = try {
                val addr = withContext(Dispatchers.IO) {
                    InetAddress.getByName("google.com")
                }
                !addr.equals("")
            } catch (e: Exception) {
                false
            }
        }
        return b
    }
}
