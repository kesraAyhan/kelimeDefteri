package com.nalid.kelimedefterim

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.nalid.kelimedefterim.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var wordList : ArrayList<Word>
    private lateinit var wordAdapter: WordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        wordList = ArrayList<Word>()
        wordAdapter = WordAdapter(wordList)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = wordAdapter

        try {
            val  database = this.openOrCreateDatabase("Words", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM words", null)
            val wordIx = cursor.getColumnIndex("word")
            val idIx = cursor.getColumnIndex("id")
            while(cursor.moveToNext()){
                val name = cursor.getString(wordIx)
                val id = cursor.getInt(idIx)
                val word = Word(name, id)
                wordList.add(word)
            }

            wordAdapter.notifyDataSetChanged()

            cursor.close()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.word_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_word_item){
            val intent = Intent(this@MainActivity, WordActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}