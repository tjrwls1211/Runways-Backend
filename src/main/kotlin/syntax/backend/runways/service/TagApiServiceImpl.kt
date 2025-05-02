package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Tag
import syntax.backend.runways.repository.TagApiRepository

@Service
class TagApiServiceImpl(
    private val tagApiRepository: TagApiRepository
) : TagApiService {

    // 태그 추가 기능
    override fun addTag(tag: String){
        val newTag = Tag(name = tag)
        tagApiRepository.save(newTag)
    }

    // 태그 전체 조회
    override fun getTag(): List<String> {
        return tagApiRepository.findAll().map { it.name }
    }

    // 태그 검색 기능
    override fun searchTag(tag: String): List<Tag> {
        // 대소문자 구분 없이 검색
        return tagApiRepository.findByNameContainingIgnoreCase(tag)
    }

    // 인기 태그 조회
    override fun getPopularTags(): List<Tag> {
        return tagApiRepository.findTop10ByOrderByUsageCountDesc()
    }
}