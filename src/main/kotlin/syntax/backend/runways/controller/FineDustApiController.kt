package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.FineDustDataDTO
import syntax.backend.runways.service.FineDustService

@RestController
@RequestMapping("api/finedust")
class FineDustApiController(private val fineDustService: FineDustService) {

    @GetMapping
    fun getFineDust(@RequestParam nx:Int, ny:Int ): ResponseEntity<FineDustDataDTO> {
        val fineDustData = fineDustService.getFineDustData(nx, ny)
        return ResponseEntity.ok(fineDustData)
    }

}