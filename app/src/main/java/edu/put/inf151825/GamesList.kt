package edu.put.inf151825

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import java.util.concurrent.Executors

class GamesList : AppCompatActivity() {
    var addons: Boolean = false
    var desc = false
    var actO = MainDatabaseOrders._ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        addons = intent.getStringExtra("addons").toBoolean()

        val dbHandler = MainDatabaseHelper(this, null, null, 1)
        val theList = dbHandler.getValues(addons.toInt(), actO, desc)
        populate(theList)
    }

    private fun populate(l: List<MainDatabase>) {
        val table: TableLayout = findViewById(R.id.tabela)
        for (i in 0..l.lastIndex) {
            val row = TableRow(this)
            val lp = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
            row.layoutParams = lp
            val textViewId = TextView(this)
            textViewId.text = l[i].id.toString()
            textViewId.setPadding(20, 15, 20, 15)
            val textViewName = TextView(this)
            textViewName.text = stringCutter(l[i].title!!)
            textViewName.setPadding(20, 15, 20, 15)
            val textViewYear = TextView(this)
            textViewYear.text = l[i].year_pub.toString()
            textViewYear.setPadding(20, 15, 20, 15)
            val im = ImageView(this)
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            var image: Bitmap? = null
            executor.execute {
                val imageURL = l[i].pic
                try {
                    val `in` = java.net.URL(imageURL).openStream()
                    image = BitmapFactory.decodeStream(`in`)
                    handler.post {
                        im.setImageBitmap(image)
                    }
                } catch (e: Exception) {

                    im.setImageBitmap(image)
                    e.printStackTrace()
                }
            }
            row.addView(textViewId)
            row.addView(textViewName)
            row.addView(textViewYear)
            row.addView(im)
            row.setOnClickListener() {
                callStats(l[i].id)
            }
            if (!addons) {
                val textViewRank = TextView(this)
                if (l[i].rank_pos == 0) {
                    textViewRank.text = "N/A"
                } else {
                    textViewRank.text = l[i].rank_pos.toString()
                }
                textViewRank.setPadding(20, 15, 20, 15)
                textViewRank.gravity = Gravity.CENTER
                row.addView(textViewRank)
            } else {
                val rankCap: TextView = findViewById(R.id.ranga)
                rankCap.visibility = View.INVISIBLE
            }
            table.addView(row, i)
        }
    }

    fun callStats(id: Int) {
        val i = Intent(this, OneGameInfo::class.java)
        val b = Bundle()
        b.putInt("Id", id)
        i.putExtras(b)
        startActivity(i)
    }

    fun sort(v: View) {
        val table: TableLayout = findViewById(R.id.tabela)
        val childCount = table.childCount
        if (childCount > 0) {
            table.removeViews(0, childCount)
        }

        when (v.id) {
            R.id.id -> {
                checkSet(MainDatabaseOrders._ID)
            }
            R.id.tytul -> {
                checkSet(MainDatabaseOrders.TITLE)
            }
            R.id.rok -> {
                checkSet(MainDatabaseOrders.YEAR_PUB)
            }
            R.id.ranga -> {
                if (!addons) {
                    checkSet(MainDatabaseOrders.RANK_POS)
                }
            }
        }
        val dbHandler = MainDatabaseHelper(this, null, null, 1)
        val theList = dbHandler.getValues(addons.toInt(), actO, desc)
        populate(theList)
    }

    fun checkSet(new: MainDatabaseOrders) {
        if (actO == new) {
            desc = desc xor true
        } else {
            desc = false
            actO = new
        }
    }
}