package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RecommendTagDTO
import syntax.backend.runways.entity.Tag
import syntax.backend.runways.repository.TagLogRepository
import syntax.backend.runways.repository.TagRepository
import syntax.backend.runways.service.UserApiService

@Service
class TagApiServiceImpl(
    private val tagRepository: TagRepository,
    private val tagLogRepository: TagLogRepository,
    private val userApiService : UserApiService
) : TagApiService {

    // 태그 추가 기능
    override fun addTag(tag: String) : Tag {
        val newTag = Tag(name = tag)
        tagRepository.save(newTag)
        return newTag
    }

    // 태그 전체 조회
    override fun getTag(): List<String> {
        return tagRepository.findAll().map { it.name }
    }

    // 태그 검색 기능
    override fun searchTag(tag: String): List<Tag> {
        // 대소문자 구분 없이 검색
        return tagRepository.findByNameContainingIgnoreCase(tag)
    }

    // 인기 태그 조회
    override fun getPopularTags(): List<Tag> {
        return tagRepository.findTop10ByOrderByUsageCountDesc()
    }

    override fun getPersonalizedTags(userId : String): List<RecommendTagDTO> {
        // JWT 토큰에서 사용자 ID 추출
        val user = userApiService.getUserDataFromId(userId)

        // 사용자 ID를 사용하여 태그 로그에서 가중치가 부여된 태그 조회
        return tagLogRepository.findWeightedTagsByUser(user.id)
            .map {
                RecommendTagDTO(
                    id = it.getId(),
                    name = it.getName(),
                    score = it.getScore(),
                    lastUsed = it.getLastUsed()
                )
            }
    }


}