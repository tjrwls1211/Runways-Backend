package syntax.backend.runways.service

import syntax.backend.runways.dto.RecommendTagDTO
import syntax.backend.runways.entity.Tag

interface TagApiService {
    fun addTag(tag: String) : Tag
    fun getTag(): List<String>
    fun searchTag(tag: String): List<Tag>
    fun getPopularTags(): List<Tag>
    fun getPersonalizedTags(userId : String): List<RecommendTagDTO>
    fun resetTagLogsByUserId(userId: String)
}