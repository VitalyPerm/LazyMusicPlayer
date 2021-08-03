package com.elvitalya.lazymusicplayer.domain

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elvitalya.lazymusicplayer.R
import com.elvitalya.lazymusicplayer.data.Music
import com.elvitalya.lazymusicplayer.databinding.MusicItemBinding

class MusicAdapter(private var musicList: MutableList<Music>, private var itemClicked: ItemClicked): RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

  inner  class MusicViewHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {
       private val binding = MusicItemBinding.bind(v)
        private lateinit var music: Music
        init {
            v.setOnClickListener(this)
        }
        fun bindMusic(music: Music){
            this.music = music
            with(binding){
                artistTextView.text = music.artistName
               songTextView.text = music.songName
            }
        }

        override fun onClick(v: View?) {
            itemClicked.itemClicked(adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_item, parent,false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = musicList[position]
        holder.bindMusic(item)
    }

    override fun getItemCount(): Int = musicList.size


}


interface ItemClicked {
    fun itemClicked(position: Int)
}