/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
* limitations under the License.
 */
package com.example.exoplayer

import android.os.Build
import android.os.Bundle
import android.view.inspector.WindowInspector
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityPlayerBinding

/**
 * A fullscreen activity to play audio or video streams.
 */
class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null

    // 再生/一時停止状態（true:再生中/false:一時停止）
    private var playWhenReady = true
    // 現在のメディアアイテムインデックス
    private var currentItem = 0
    // 現在の再生位置
    private var playbackPosition = 0L

    // ビューをバインディングする
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // viewBinding.root でビューを取得
        setContentView(viewBinding.root)
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Build.VERSION.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) {
            releasePlayer()
        }
    }
    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) {
            releasePlayer()
        }
    }

    /**
     * ExoPlayer を初期化する
     */
    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
                exoPlayer.setMediaItem(mediaItem)

                // 2つ目のメディアを登録
                val secondMediaItem = MediaItem.fromUri(getString(R.string.media_url_mp3))
                exoPlayer.addMediaItem(secondMediaItem)

                // 再生/一時停止状態を設定
                exoPlayer.playWhenReady = playWhenReady
                // 現在のメディアアイテムのインデックスと再生位置を設定
                exoPlayer.seekTo(currentItem, playbackPosition)
                // プレイヤー準備。再生に必要なリソースを取得。設定しないと、読み込んだ際にリソースが表示されなかったり、
                // 再生状態でBack二移動してForeに持ってくると一時停止になってたりする
                exoPlayer.prepare()
            }
    }

    /**
     * ExoPlayer を解放する
     */
    private fun releasePlayer() {
        player?.let { exoPlayer ->
            // 再生位置を保存
            playbackPosition = exoPlayer.currentPosition
            // 現在再生しているプレイリストのインデックスを保存
            currentItem = exoPlayer.currentMediaItemIndex
            // 再生/一時停止状態を保存
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    /**
     * システムUI（ステータスバーとナビゲーションバー） を非表示にする
     * ユーザーが画面の端からスワイプすると、システムバーが一時的に再表示される
     */
    private fun hideSystemUi(){
        //システムウィンドウ（ステータスバーやナビゲーションバー）がアプリのコンテンツ領域に侵入しないように設定
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,viewBinding.videoView).let {controller ->
            // システムバーを非表示にします。
            controller.hide(WindowInsetsCompat.Type.systemBars())
            // システムバーの動作を、スワイプで一時的に表示する動作に設定します。
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}