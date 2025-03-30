package syntax.backend.runways.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Bookmark(
    val bookMarkId: MutableList<String> = mutableListOf()
) {
    fun addBookMark(userId: String) {
        if (!bookMarkId.contains(userId)) {
            bookMarkId.add(userId)
        }
    }
    fun isBookmarked(userId: String): Boolean {
        return bookMarkId.contains(userId)
    }
}