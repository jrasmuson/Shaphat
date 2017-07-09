import com.amazonaws.util.json.Jackson
import com.fasterxml.jackson.module.kotlin.KotlinModule
import models.ModerationRequest
import java.io.IOException

class JsonMapper {

    companion object {
        private val objectMapper = Jackson.getObjectMapper().registerModule(KotlinModule())

        @Throws(IOException::class)
        fun parseModerationRequest(json: String): ModerationRequest {
            return objectMapper.readValue(json, ModerationRequest::class.java)
        }
    }
}