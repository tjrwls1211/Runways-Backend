package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runways.dto.TendencyDTO
import syntax.backend.runways.service.TendencyApiService
import syntax.backend.runways.util.SecurityUtil

@RestController
@RequestMapping("api/tendency")
class TendencyApiController (
    private val tendencyApiService: TendencyApiService
){
    // 성향 저장
    @PostMapping("/save")
    fun saveTendency(@RequestBody tendencyDTO: TendencyDTO): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        tendencyApiService.saveTendency(userId, tendencyDTO)
        return ResponseEntity.ok("성향 저장 성공")
    }

    // 성향 데이터 확인
    @GetMapping("/get")
    fun getTendency(): ResponseEntity<TendencyDTO?> {
        val userId = SecurityUtil.getCurrentUserId()
        val tendency = tendencyApiService.getTendency(userId)
        return if (tendency != null) {
            ResponseEntity.ok(tendency)
        } else {
            ResponseEntity.noContent().build()
        }
    }

}