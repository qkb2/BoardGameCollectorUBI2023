package edu.put.inf151825

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import java.net.URL
import java.util.concurrent.Executors


class OneGameInfo : AppCompatActivity() {
    var Id: Int = 0
    lateinit var coverPicture: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_game_info)

        val extras = intent.extras
        Id = extras!!.getInt("Id")
        val mdb = MainDatabaseHelper(this, null, null, 1)
        val rec = mdb.findRecord(Id)
        setCaptions(rec!!)
        val phdb = PhotoDatabaseHelper(this, null, null, 1)
        val photos: List<PhotoDatabase> = phdb.getValues(Id)
        for (i in photos.indices) {
            if (photos[i].photo != null) {
                val myUri: Uri = Uri.parse(photos[i].photo)
                showPhoto(myUri)
            }
        }

        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        val mGetContent =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { result ->
                if (result != null) {
                    contentResolver.takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addPhoto(result)
                }
            }
        addPhotoButton.setOnClickListener { mGetContent.launch(arrayOf("image/*")) }
    }

    fun showPhoto(photoUri: Uri) {
        val table: LinearLayout = findViewById<View>(R.id.zdjecia) as LinearLayout
        val image = ImageView(this)
        image.setImageURI(null)
        image.setImageURI(photoUri)
        image.setPadding(0, 10, 0, 10)
        table.addView(image)

        val db = PhotoDatabaseHelper(this, null, null, 1)
        val toggle: ToggleButton = findViewById(R.id.togglePhotoDelete)
        image.setOnClickListener {
            if (toggle.isChecked) {
                db.deletePhoto(Id, photoUri.toString())
                table.removeView(image)
            } else {
                bigPhoto(image.drawable.toBitmap())
            }
        }
    }

    fun addPhoto(photoUri: Uri) {
        val table: LinearLayout = findViewById(R.id.zdjecia)
        val image = ImageView(this)
        image.setImageURI(photoUri)
        image.setPadding(0, 10, 0, 10)

        val db = PhotoDatabaseHelper(this, null, null, 1)
        val toggle: ToggleButton = findViewById(R.id.togglePhotoDelete)
        image.setOnClickListener {
            if (toggle.isChecked) {
                db.deletePhoto(Id, photoUri.toString())
                table.removeView(image)
            } else {
                bigPhoto(image.drawable.toBitmap())
            }
        }
        if (!toggle.isChecked) {
            val product = PhotoDatabase(Id, photoUri.toString())
            table.addView(image)
            db.addRecord(product)
        }
    }

    private fun bigPhoto(picture: Bitmap) {
        val builder = Dialog(this)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        builder.setOnDismissListener {}
        val imageView = ImageView(this)
        imageView.setImageBitmap(picture)
        builder.addContentView(
            imageView, RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        builder.show()
    }

    fun bigPhotoCover(v: View) {
        bigPhoto(coverPicture)
    }

    private fun setCaptions(rec: MainDatabase) {
        val statYear: TextView = findViewById(R.id.rok)
        statYear.text = "Rok: " + rec.year_pub.toString()
        val statID: TextView = findViewById(R.id.id)
        statID.text = "Id: " + rec.id.toString()
        val statName: TextView = findViewById(R.id.tytul)
        statName.text = stringCutter(rec.title!!)

        val statRanga: TextView = findViewById(R.id.ranga)
        statRanga.text = "Pozycja: " + rec.rank_pos.toString()


        val im: ImageView = findViewById(R.id.photo)
        val executor = Executors.newSingleThreadExecutor()
        val executor1 = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val handler1 = Handler(Looper.getMainLooper())
        var image: Bitmap? = null
        var image1: Bitmap? = null
        executor.execute {
            val imageURL = rec.pic
            try {
                val `in` = URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
                handler.post {
                    im.setImageBitmap(image)
                }
            } catch (e: Exception) {

                im.setImageBitmap(image)

                e.printStackTrace()
            }
        }
        executor1.execute {
            val imageURL = rec.photo
            try {
                val `in` = URL(imageURL).openStream()
                image1 = BitmapFactory.decodeStream(`in`)
                handler1.post {
                    coverPicture = image1!!
                }
            } catch (e: Exception) {
                coverPicture = image1!!
                e.printStackTrace()
            }
        }
    }
}