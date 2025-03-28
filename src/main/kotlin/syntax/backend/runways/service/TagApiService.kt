package syntax.backend.runways.service

interface TagApiService {
    fun addTag(tag: String)
    fun getTag(): List<String>
}