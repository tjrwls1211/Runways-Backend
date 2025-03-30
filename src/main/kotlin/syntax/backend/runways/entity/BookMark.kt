package syntax.backend.runways.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.*

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
data class BookMark(
    val bookMarkId: MutableList<String> = mutableListOf()
) {
    fun addBookMark(userId: String) {
        if (!bookMarkId.contains(userId)) {
            bookMarkId.add(userId)
        }
    }
}