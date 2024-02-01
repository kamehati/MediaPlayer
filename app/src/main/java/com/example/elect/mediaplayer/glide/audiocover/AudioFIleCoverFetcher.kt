package com.example.elect.mediaplayer.glide.audiocover

import android.media.MediaMetadataRetriever
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class AudioFIleCoverFetcher(
    val model: AudioFileCover
) : DataFetcher<InputStream> {

    private var stream: InputStream? = null

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {

        return DataSource.LOCAL
    }

    override fun loadData(
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>
    ) {
        val retriever: MediaMetadataRetriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(model.filePath)
            val picture : ByteArray? = retriever.embeddedPicture

            if(picture != null){
                stream = ByteArrayInputStream(picture)
            }
            callback.onDataReady(stream)
        } catch (e : FileNotFoundException) {
            callback.onLoadFailed(e)
        } finally {
            retriever.release()
        }
    }

    override fun cleanup() {
        if(stream != null){
            try {
                stream!!.close()
            } catch (ignore: IOException){

            }
        }
    }

    override fun cancel() {

    }
}