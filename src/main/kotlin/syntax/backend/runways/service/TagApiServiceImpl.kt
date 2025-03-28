package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Tag
import syntax.backend.runways.repository.TagApiRepository

@Service
class TagApiServiceImpl(
    private val tagApiRepository: TagApiRepository
) : TagApiService {

    override fun addTag(tag: String){
        val newTag = Tag(name = tag)
        tagApiRepository.save(newTag)
    }

    override fun getTag(): List<String> {
        return tagApiRepository.findAll().map { it.name }
    }

}