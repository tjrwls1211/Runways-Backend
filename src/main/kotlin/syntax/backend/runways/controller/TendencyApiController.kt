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

@RestController
@RequestMapping("api/tendency")
class TendencyApiController (
    private val tendencyApiService: TendencyApiService
){
    // 성향 저장
    @PostMapping("/save")
    fun saveTendency(@RequestHeader("Authorization") token: String, @RequestBody tendencyDTO: TendencyDTO): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        tendencyApiService.saveTendency(jwtToken, tendencyDTO)
        return ResponseEntity.ok("성향 저장 성공")
    }

    // 성향 데이터 확인
    @GetMapping("/get")
    fun getTendency(@RequestHeader("Authorization") token: String): ResponseEntity<TendencyDTO?> {
        val jwtToken = token.substring(7)
        val tendency = tendencyApiService.getTendency(jwtToken)
        return if (tendency != null) {
            ResponseEntity.ok(tendency)
        } else {
            ResponseEntity.noContent().build()
        }
    }

}