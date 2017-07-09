import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals

object JsonParseSpec : Spek({

    val json = """
{
    "hostname":"test.com",
    "imageUrl":"http://test.com/image1.png"
}
"""
    val json2 ="""
{
        "hostname":"flowermeaning.com",
        "imageUrl":"http://www.flowermeaning.com/flower-pics/Orchid-Meaning.jpg"
}"""
    it("parses a test json") {

        val moderationRequest = JsonMapper.parseModerationRequest(json)

        assertEquals("test.com", moderationRequest.hostname)
        assertEquals("http://test.com/image1.png", moderationRequest.imageUrl)
    }

    it("parse real json"){
        val moderationRequest = JsonMapper.parseModerationRequest(json2)


        assertEquals("flowermeaning.com", moderationRequest.hostname)
        assertEquals("http://www.flowermeaning.com/flower-pics/Orchid-Meaning.jpg", moderationRequest.imageUrl)
    }

})



