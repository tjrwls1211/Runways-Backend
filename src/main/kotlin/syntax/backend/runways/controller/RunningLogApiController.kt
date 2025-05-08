package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.RequestRunningLogDTO
import syntax.backend.runways.service.RunningLogApiService
import syntax.backend.runways.util.SecurityUtil
import java.util.*

@RestController
@RequestMapping("api/runninglog")
class RunningLogApiController(
    private val runningLogApiService: RunningLogApiService
) {
    @PostMapping("/save")
    fun saveRunningLog(@RequestBody request: RequestRunningLogDTO): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        runningLogApiService.saveRunningLog(request, userId)
        return ResponseEntity.ok("러닝 로그 저장 완료")
    }
}