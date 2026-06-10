package com.animebr.app.data.player

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Integration test for BloggerVideoExtractor.
 * Makes real network requests to blogger.com.
 *
 * Run: ./gradlew testDebugUnitTest --tests "*.BloggerVideoExtractorTest"
 */
class BloggerVideoExtractorTest {

    private val extractor = BloggerVideoExtractor()

    private val testVideoGUrl =
        "https://www.blogger.com/video.g?token=AD6v5dyB-fdSYgPnTfqbLm70ELRS-K0Qk-YKGt-1FSGAnJWyVN5lQHEhh1sdyb44ESfpbjC_1uJSZVJ9zmh7ll_HqabBkiYq-SHYHUYjkCB01-7_5klrwjNI7Xpm_c-XCdoq0LeokHc"

    @Test
    fun `extractVideoUrl returns googlevideo URL`() = runBlocking {
        val result = extractor.extractVideoUrl(testVideoGUrl)

        println("Extracted URL: $result")

        assertNotNull("Should extract a video URL", result)
        assertTrue(
            "URL should contain googlevideo.com/videoplayback",
            result!!.contains("googlevideo.com/videoplayback")
        )
        assertTrue(
            "URL should have cpn parameter",
            result.contains("cpn=")
        )
    }

    @Test
    fun `extractVideoUrl with invalid token returns null`() = runBlocking {
        val result = extractor.extractVideoUrl(
            "https://www.blogger.com/video.g?token=INVALID_TOKEN_12345"
        )
        println("Result for invalid token: $result")
        assertNull("Invalid token should return null", result)
    }
}
