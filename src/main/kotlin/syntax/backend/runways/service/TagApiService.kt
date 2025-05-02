package syntax.backend.runways.service

import syntax.backend.runways.entity.Tag

interface TagApiService {
    fun addTag(tag: String)
    fun getTag(): List<String>
    fun searchTag(tag: String): List<Tag>
    fun getPopularTags(): List<Tag>
}