package com.dsk.musicbuddy.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dsk.musicbuddy.R
import com.dsk.musicbuddy.ui.MainActivity
import com.dsk.musicbuddy.util.MusicService.Companion.NOTIFICATION_ID


object CreateNotification {

    private const val CHANNEL_ID = "music_channel"
    private lateinit var playIntent: Intent
    private lateinit var previousIntent: Intent
    private lateinit var nextIntent: Intent
    private lateinit var notificationIntent: Intent
    private lateinit var closeServiceIntent: Intent
    private lateinit var pendingIntent: PendingIntent
    private lateinit var ppreviousIntent: PendingIntent
    private lateinit var pplayIntent: PendingIntent
    private lateinit var pnextIntent: PendingIntent
    private lateinit var pcloseIntent: PendingIntent
    private var expandedView: RemoteViews? = null
    private var collapsedView: RemoteViews? = null

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Music Channel"
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
    }

    fun createNotification(context: Context, songDetails: MediaMetadataCompat, isSongPlaying: Boolean): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
            val notificationManagerCompat = NotificationManagerCompat.from(context)

            assignNotificationIntent(context, songDetails, isSongPlaying)

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            val notification = notificationBuilder
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification))
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .setCustomBigContentView(expandedView)
                .setCustomContentView(collapsedView)
                .build()

            notificationManagerCompat.notify(NOTIFICATION_ID, notification)
            return notification
        } else {
            return createLegacyNotification(context, songDetails, isSongPlaying)
        }
    }

    private fun createLegacyNotification(context: Context, songDetails: MediaMetadataCompat, isSongPlaying: Boolean): Notification {
        val notification = notificationIntent(context, songDetails, isSongPlaying)
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }

    private fun notificationIntent(context: Context, songDetails: MediaMetadataCompat, isSongPlaying: Boolean): Notification {
        assignNotificationIntent(context, songDetails, isSongPlaying)

        return NotificationCompat.Builder(context)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification))
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setCustomBigContentView(expandedView)
            .setCustomContentView(collapsedView)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun assignNotificationIntent(
        context: Context,
        songDetails: MediaMetadataCompat,
        isSongPlaying: Boolean
    ) {
        var songName: String? = "Music Buddy"
        var songAlbumName: String? = ""
        val imagePlayPause: Bitmap

        songDetails?.let {
            songName = it.description.title?.toString() ?: songName
            songAlbumName = it.description.subtitle?.toString() ?: songAlbumName
        }


        // onClick Notification Bar Intent
        notificationIntent = Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(notificationIntent)
        pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)

        //        Notification Bar Layout
        expandedView = RemoteViews(context.packageName, R.layout.notification_bar_expanded)
        collapsedView = RemoteViews(context.packageName, R.layout.notification_bar_shrink)

//        val bitmap = Utility.getAlbumArt(context, songDetails!!.description.iconUri)

        expandedView!!.setTextViewText(R.id.textNotificationSongName, songName)
        expandedView!!.setTextViewText(R.id.textNotificationAlbumName, songAlbumName)
        expandedView!!.setImageViewUri(R.id.notificationLogo, songDetails.description.iconUri)

        collapsedView!!.setTextViewText(R.id.textNotificationSongNameSh, songName)
        collapsedView!!.setTextViewText(R.id.textNotificationAlbumNameSh, songAlbumName)
        collapsedView!!.setImageViewUri(R.id.notificationLogoSh, songDetails!!.description.iconUri)

//        if (imagePlayPause != null) {
//            expandedView.setImageViewBitmap(R.id.relativeLayoutNotification, imagePlayPause);
//            collapsedView.setImageViewBitmap(R.id.relativeLayoutNotificationSh, imagePlayPause);
//        }
        previousIntent = Intent(context, MusicService::class.java)
        previousIntent.setAction(StringConstants.ACTION_PREVIOUS)
        ppreviousIntent = PendingIntent.getService(
            context, 0,
            previousIntent, PendingIntent.FLAG_IMMUTABLE
        )

        expandedView!!.setOnClickPendingIntent(R.id.notifyPreviousImage, ppreviousIntent)
        collapsedView!!.setOnClickPendingIntent(R.id.notifyPreviousImageSh, ppreviousIntent)

        playIntent = Intent(context, MusicService::class.java)
        if (songDetails != null) {
            if (isSongPlaying) {
                playIntent.setAction(StringConstants.ACTION_PAUSE)
                imagePlayPause =
                    BitmapFactory.decodeResource(context.resources, R.drawable.ic_pause)
                collapsedView!!.setImageViewBitmap(R.id.notifyPlayImageSh, imagePlayPause)
                expandedView!!.setImageViewBitmap(R.id.notifyPlayImage, imagePlayPause)
            } else {
                playIntent.setAction(StringConstants.ACTION_PLAY)
                imagePlayPause = BitmapFactory.decodeResource(context.resources, R.drawable.ic_play)
                collapsedView!!.setImageViewBitmap(R.id.notifyPlayImageSh, imagePlayPause)
                expandedView!!.setImageViewBitmap(R.id.notifyPlayImage, imagePlayPause)
            }
            playIntent.putExtra("SongId", songDetails.description.mediaId)
        }
        pplayIntent = PendingIntent.getService(
            context, 0,
            playIntent, PendingIntent.FLAG_IMMUTABLE
        )

        expandedView!!.setOnClickPendingIntent(R.id.notifyPlayImage, pplayIntent)
        collapsedView!!.setOnClickPendingIntent(R.id.notifyPlayImageSh, pplayIntent)

        nextIntent = Intent(context, MusicService::class.java)
        nextIntent.setAction(StringConstants.ACTION_NEXT)
        pnextIntent = PendingIntent.getService(
            context, 0,
            nextIntent, PendingIntent.FLAG_IMMUTABLE
        )

        expandedView!!.setOnClickPendingIntent(R.id.notifyNextImage, pnextIntent)
        collapsedView!!.setOnClickPendingIntent(R.id.notifyNextImageSh, pnextIntent)

        closeServiceIntent = Intent(context, MusicService::class.java)
        closeServiceIntent.setAction(StringConstants.ACTION_STOP_FOREGROUND_SERVICE)
        pcloseIntent = PendingIntent.getService(
            context, 0,
            closeServiceIntent, PendingIntent.FLAG_IMMUTABLE
        )

        expandedView!!.setOnClickPendingIntent(R.id.notificationCloseSh, pcloseIntent)
        collapsedView!!.setOnClickPendingIntent(R.id.notificationCloseSh, pcloseIntent)
    }

}
