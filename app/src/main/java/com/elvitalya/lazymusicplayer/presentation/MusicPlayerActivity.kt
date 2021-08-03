package com.elvitalya.lazymusicplayer.presentation

import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elvitalya.lazymusicplayer.R
import com.elvitalya.lazymusicplayer.data.Music
import com.elvitalya.lazymusicplayer.databinding.ActivityMusicPlayerBinding
import com.elvitalya.lazymusicplayer.domain.ItemClicked
import com.elvitalya.lazymusicplayer.domain.MusicAdapter
import com.elvitalya.lazymusicplayer.utils.APP_ACTIVITY
import com.elvitalya.lazymusicplayer.utils.timerFormat

class MusicPlayerActivity : AppCompatActivity(), ItemClicked {
    private lateinit var binding: ActivityMusicPlayerBinding
    private lateinit var musicList: MutableList<Music>
    private lateinit var adapter: MusicAdapter
    private lateinit var recyclerView: RecyclerView
    private var currPosition: Int = 0
    private var state = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        APP_ACTIVITY = this
        musicList = mutableListOf()
        if (Build.VERSION.SDK_INT >= 23)
            checkPermission()
        with(binding) {
            fabPlay.setOnClickListener {
                play(currPosition)
                Log.d("position111", "Current position: $currPosition")

            }
            fabNext.setOnClickListener {
                mediaPlayer?.stop()
                state = false
                if(currPosition < musicList.size - 1) currPosition++ else currPosition = 0
                play(currPosition)
                Log.d("position111", "Current position: $currPosition")
            }
            fabPrevious.setOnClickListener {
                mediaPlayer?.stop()
                state = false
                if(currPosition > 0) currPosition--
                play(currPosition)
                Log.d("position111", "Current position: $currPosition")
            }

            seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if(fromUser){
                        mediaPlayer?.start()
                        mediaPlayer?.seekTo(progress)

                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    mediaPlayer?.pause()

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    mediaPlayer?.seekTo(seekBar!!.progress*1000)
                }

            })


        }
    }

    private fun play(currPosition: Int) {

        if (!state) {
            binding.fabPlay.setImageResource(R.drawable.ic_stop)
            state = true
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(this@MusicPlayerActivity, Uri.parse(musicList[currPosition].songUri))
                prepare()
                start()
            }

            val mHandler = Handler()
            this.runOnUiThread(object : Runnable {
                override fun run() {
                    val playerPosition = mediaPlayer?.currentPosition!! / 1000
                    val totalDuration = mediaPlayer?.duration!! / 1000
                    with(binding) {
                        seekBar.max = totalDuration
                        seekBar.progress = playerPosition
                        pastTextView.text = timerFormat(playerPosition.toLong())
                        remainTextView.text = timerFormat((totalDuration - playerPosition).toLong())
                    }


                    mHandler.postDelayed(this, 1000)
                }
            })
        } else {
            state = false
            mediaPlayer?.stop()
            binding.fabPlay.setImageResource(R.drawable.ic_play_arrow)
        }

    }


    private fun getSongs() {
        val selection = MediaStore.Audio.Media.IS_MUSIC
        val projections = arrayOf(
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA
        )
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projections,
            selection,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            musicList.add(Music(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
        }
        cursor.close()
        adapter = MusicAdapter(musicList, this)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(APP_ACTIVITY)
        recyclerView.adapter = adapter
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getSongs()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(this, "Music player needs access to your files", Toast.LENGTH_SHORT)
                    .show()
            }
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSongs()
            } else {
                Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    override fun itemClicked(position: Int) {
        mediaPlayer?.stop()
        state = false
        currPosition = position
        play(position)
    }

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }

    override fun onDestroy() {
        super.onDestroy()
            mediaPlayer?.stop()
    }

}