package com.naman14.timberx.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

import com.naman14.timberx.vo.Album
import com.naman14.timberx.vo.Song

object AlbumRepository {

    /* Album song sort order track list */
    private const val SONG_TRACK_LIST = (MediaStore.Audio.Media.TRACK + ", "
            + MediaStore.Audio.Media.DEFAULT_SORT_ORDER)

    fun getAlbum(cursor: Cursor?): Album {
        var album = Album()
        if (cursor != null) {
            if (cursor.moveToFirst())
                album = Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5))
        }
        cursor?.close()
        return album
    }


    fun getAlbumsForCursor(cursor: Cursor?): ArrayList<Album> {
        val arrayList = arrayListOf<Album>()
        if (cursor != null && cursor.moveToFirst())
            do {
                arrayList.add(Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5)))
            } while (cursor.moveToNext())
        cursor?.close()
        return arrayList
    }

    fun getAllAlbums(context: Context): ArrayList<Album> {
        return getAlbumsForCursor(makeAlbumCursor(context, null, null))
    }

    fun getAlbum(context: Context, id: Long): Album {
        return getAlbum(makeAlbumCursor(context, "_id=?", arrayOf(id.toString())))
    }

    fun getAlbums(context: Context, paramString: String, limit: Int): List<Album> {
        val result = getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", arrayOf("$paramString%")))
        if (result.size < limit) {
            result.addAll(getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", arrayOf("%_$paramString%"))))
        }
        return if (result.size < limit) result else result.subList(0, limit)
    }


    private fun makeAlbumCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf("_id", "album", "artist", "artist_id", "numsongs", "minyear"), selection, paramArrayOfString, null)
    }

    fun getSongsForAlbum(context: Context, albumID: Long): ArrayList<Song> {

        val cursor = makeAlbumSongCursor(context, albumID)
        val arrayList = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst())
            do {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getInt(4)
                var trackNumber = cursor.getInt(5)
                /*This fixes bug where some track numbers displayed as 100 or 200*/
                while (trackNumber >= 1000) {
                    trackNumber -= 1000 //When error occurs the track numbers have an extra 1000 or 2000 added, so decrease till normal.
                }
                val artistId = cursor.getInt(6).toLong()

                arrayList.add(Song(id, albumID, artistId, title, artist, album, duration, trackNumber))
            } while (cursor.moveToNext())
        cursor?.close()
        return arrayList
    }

    private fun makeAlbumSongCursor(context: Context, albumID: Long): Cursor? {
        val contentResolver = context.contentResolver
        val albumSongSortOrder =  SONG_TRACK_LIST
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val string = "is_music=1 AND title != '' AND album_id=$albumID"
        return contentResolver.query(uri, arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id"), string, null, albumSongSortOrder)
    }
}