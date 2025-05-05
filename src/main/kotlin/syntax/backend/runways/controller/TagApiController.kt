package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.RecommendTagDTO
import syntax.backend.runways.dto.TagDTO
import syntax.backend.runways.entity.Tag
import syntax.backend.runways.service.TagApiService

@RestController
@RequestMapping("/api/tag")
class TagApiController (
    private val tagApiService: TagApiService
){

    // 태그 생성
    @PostMapping("/create")
    fun createTag(@RequestBody tagDTO: TagDTO) : ResponseEntity<Tag> {
        val newTag = tagApiService.addTag(tagDTO.tagName)
        return ResponseEntity.ok(newTag)
    }

    // 태그 리스트 불러오기
    @GetMapping("/list")
    fun getTagList(): ResponseEntity<List<String>> {
        val tagList = tagApiService.getTag()
        return ResponseEntity.ok(tagList)
    }

    // 태그 검색
    @GetMapping("/search")
    fun searchTag(@RequestParam tagName : String): ResponseEntity<List<Tag>> {
        val tagList = tagApiService.searchTag(tagName)
        return ResponseEntity.ok(tagList)
    }

    // 인기 태그 조회
    @GetMapping("/popular")
    fun getPopularTags(): ResponseEntity<List<Tag>> {
        val popularTags = tagApiService.getPopularTags()
        return ResponseEntity.ok(popularTags)
    }

    // 사용자 맞춤형 태그 조회
    @GetMapping("/personalized")
    fun getPersonalizedTags(@RequestHeader("Authorization") token: String): ResponseEntity<List<RecommendTagDTO>> {
        val jwtToken = token.substring(7)
        val personalizedTags = tagApiService.getPersonalizedTags(jwtToken)
        return ResponseEntity.ok(personalizedTags)
    }

}