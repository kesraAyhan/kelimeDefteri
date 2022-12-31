package com.nalid.kelimedefterim

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nalid.kelimedefterim.databinding.RecyclerRowBinding

class WordAdapter (val wordList: ArrayList<Word>) : RecyclerView.Adapter<WordAdapter.WordHolder>() {
    class WordHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return WordHolder(binding)
    }

    override fun onBindViewHolder(holder: WordHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = wordList.get(position).name
       holder.itemView.setOnClickListener {
           val intent = Intent(holder.itemView.context,WordActivity::class.java)
           intent.putExtra("info", "old")
           intent.putExtra("id", wordList.get(position).id)
           holder.itemView.context.startActivity(intent)
       }
    }

    override fun getItemCount(): Int {
        return wordList.size
    }
}