package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.service.TagApiService

@RestController
@RequestMapping("/api/tag")
class TagApiController (
    private val tagApiService: TagApiService
){

    // 태그 생성 (일단 보류)
    @PostMapping("/create")
    fun createTag(@RequestBody tagName : String) : ResponseEntity<String> {
        tagApiService.addTag(tagName)
        return ResponseEntity.ok("태그 생성 완료")
    }

    // 태그 리스트 불러오기
    @GetMapping("/list")
    fun getTagList(): ResponseEntity<List<String>> {
        val tagList = tagApiService.getTag()
        return ResponseEntity.ok(tagList)
    }

    // 태그 검색
    @GetMapping("/search")
    fun searchTag(@RequestParam tag: String): ResponseEntity<List<String>> {
        val tagList = tagApiService.searchTag(tag)
        return ResponseEntity.ok(tagList.map { it.name })
    }

}