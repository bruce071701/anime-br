package com.animebr.app.data.player

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BloggerExtractorTest2 {

    private val extractor = BloggerVideoExtractor()

    @Test
    fun `test with origin parameter URL`() = runBlocking {
        val url = "https://www.blogger.com/video.g?token=AD6v5dwjJoUulZmogQNCjfPcQcwobKnwo5s4SQxyy2B4SyG9Py4QFoa_XgiVLTsZ7Mllxi9YhOR8xqS8TO3eaItVn5NRlNgScIM71k9bdgCM7nEdgSNkd7owfC7yQEU4oXDUFH9sYxU&origin=bulbalegend.blogspot.com"

        val result = extractor.extractVideoUrl(url)

        println("Extracted URL: $result")

        assertNotNull("Should extract a video URL", result)
        assertTrue(
            "URL should contain googlevideo.com/videoplayback",
            result!!.contains("googlevideo.com/videoplayback")
        )
    }
}
