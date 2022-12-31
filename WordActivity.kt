package com.nalid.kelimedefterim

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Size
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.nalid.kelimedefterim.databinding.ActivityWordBinding
import java.io.ByteArrayOutputStream

class WordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        database = this.openOrCreateDatabase("Words", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")){
            binding.wordText.setText("")
            binding.meaningText.setText("")
            binding.exampleText.setText("")
            binding.button.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.selectimage)
        } else {
            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)

            val cursor = database.rawQuery("SELECT * FROM words WHERE id = ?", arrayOf(selectedId.toString()))
            val wordIx = cursor.getColumnIndex("word")
            val meaningIx = cursor.getColumnIndex("meaning")
            val exampleIx = cursor.getColumnIndex("example")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {
                binding.wordText.setText(cursor.getString(wordIx))
                binding.meaningText.setText(cursor.getString(meaningIx))
                binding.exampleText.setText(cursor.getString(exampleIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()
        }

    }

    fun save(view: View) {
        val word = binding.wordText.text.toString()
        val meaning = binding.meaningText.text.toString()
        val example = binding.exampleText.text.toString()
        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                //val database =this.openOrCreateDatabase("Words", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS words (id INTEGER PRIMARY KEY, word VARCHAR, meaning VARCHAR, example VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO words (word, meaning, example, image) VALUES (?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,word)
                statement.bindString(2,meaning)
                statement.bindString(3,example)
                statement.bindBlob(4,byteArray)
                statement.execute()
            }catch (e: Exception){
                e.printStackTrace()
            }
             val intent = Intent(this@WordActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun makeSmallerBitmap(image : Bitmap, maximumSize: Int) : Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()

        }else{
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImage(view: View) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Snackbar.make(
                    view,
                    "Galeriye gitmek için izin gerekli.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("İzin ver", View.OnClickListener {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

        }
    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageData = intentFromResult.data
                        // binding.imageView.setImageURI(imageData)
                        if (imageData != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(
                                        this@WordActivity.contentResolver, imageData
                                    )
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                } else {
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(
                                        contentResolver,
                                        imageData
                                    )
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }


                        }

                    }
                }
            }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            result ->
            if (result){
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this@WordActivity,"İzin Gerekli", Toast.LENGTH_LONG).show()
            }

        }
    }
}
