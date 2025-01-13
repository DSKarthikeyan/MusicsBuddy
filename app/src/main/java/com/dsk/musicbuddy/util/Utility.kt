package com.dsk.musicbuddy.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dsk.musicbuddy.R
import com.dsk.musicbuddy.data.model.SongDetails
import com.intuit.sdp.BuildConfig
import java.io.File
import java.io.IOException
import java.util.Objects
import kotlin.math.min

class Utility {

    companion object {

        fun shareFiles(context: Context, path: String?) {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            val screenshotUri = Uri.parse(path)
            sharingIntent.setType("*/*")
            sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
            context.startActivity(Intent.createChooser(sharingIntent, "Music Buddy Sharing"))
        }

        fun shareMultipleFiles(context: Context, path: ArrayList<String?>) {
            val sharingIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
            val uriArrayList = ArrayList<Uri>()
            for (pathList in path) {
                uriArrayList.add(Uri.parse(pathList))
            }
            sharingIntent.setType("*/*")
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uriArrayList)
            context.startActivity(Intent.createChooser(sharingIntent, "Music Buddy Sharing"))
        }


        fun setRingTone(context: Context, songDetail: SongDetails?): Boolean {
            if (songDetail?.songPath != null && songDetail.songPath!!.isNotEmpty()
            ) {
                val k: File? =
                    songDetail.songPath?.let { File(it) } // path is a file to /sdcard/media/ringtone

                val values = ContentValues()
                if (k != null) {
                    values.put(MediaStore.MediaColumns.DATA, k.absolutePath)
                }
                values.put(MediaStore.MediaColumns.TITLE, songDetail.songName)
                if (k != null) {
                    values.put(MediaStore.MediaColumns.SIZE, k.length())
                }
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
                values.put(MediaStore.Audio.Media.IS_ALARM, false)
                values.put(MediaStore.Audio.Media.IS_MUSIC, false)

                //  Insert it into the database
                val uri = k?.let { MediaStore.Audio.Media.getContentUriForPath(it.absolutePath) }
                val newUri = context.contentResolver.insert(uri!!, values)
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                        context,
                        RingtoneManager.TYPE_RINGTONE,
                        newUri
                    )
                    Toast.makeText(context, "Success! Ringtone assigned...", Toast.LENGTH_LONG)
                        .show()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
                //            cursor.close();
            }
            return true
        }

        fun getAlbumArt(context: Context, albumId: Long): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                val sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart")
                val albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId)
                //            String imagePath = getPath(context, albumArtUri);
                bitmap = MediaStore.Images.Media.getBitmap(
                    context.contentResolver, albumArtUri
                )
                bitmap = scaleBitmap(
                    bitmap,
                    StringConstants.maxImageSize,
                    true
                )
            } catch (exception: IOException) {
                exception.printStackTrace()
                bitmap = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_notification
                )
            }
            return bitmap
        }

        private fun scaleBitmap(myBitmap: Bitmap?, maxImageSize: Float, filter: Boolean): Bitmap? {
            var bitmap: Bitmap? = null
            if (myBitmap != null) {
                val ratio = min(
                    (maxImageSize as Float / myBitmap.width).toDouble(),
                    (maxImageSize as Float / myBitmap.height).toDouble()
                ).toFloat()
                val width = Math.round(ratio * myBitmap.width)
                val height = Math.round(ratio * myBitmap.height)

                bitmap = Bitmap.createScaledBitmap(
                    myBitmap, width,
                    height, filter
                )
            }
            return bitmap
        }

        fun deleteFile(context: Context, filePath: String?): Boolean {
            var isFileDeleted = false
            if (filePath != null && !filePath.isEmpty()) {
                val photoLcl = File(filePath)
                val imageUriLcl = FileProvider.getUriForFile(
                    Objects.requireNonNull<Context>(context),
                    BuildConfig.APPLICATION_ID + ".provider", photoLcl
                )
                val contentResolver = context.contentResolver
                val fileDeleted = contentResolver.delete(imageUriLcl, null, null)
                //            Log.d("DSK","fileDeleted "+fileDeleted);
                if (fileDeleted > 0) {
                    isFileDeleted = true
                }
            }
            return isFileDeleted
        }

    }
}
