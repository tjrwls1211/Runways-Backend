package syntax.backend.runways.controller

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.PagedResponse
import syntax.backend.runways.dto.RequestRunningLogDTO
import syntax.backend.runways.dto.RunningLogDTO
import syntax.backend.runways.entity.RunningLog
import syntax.backend.runways.service.RunningLogApiService
import syntax.backend.runways.util.SecurityUtil

@RestController
@RequestMapping("api/runninglog")
class RunningLogApiController(
    private val runningLogApiService: RunningLogApiService,
) {
    // 러닝 로그 저장
    @PostMapping("/save")
    fun saveRunningLog(@RequestBody request: RequestRunningLogDTO): ResponseEntity<String> {
        val user = SecurityUtil.getCurrentUser()
        runningLogApiService.saveRunningLog(request, user)
        return ResponseEntity.ok("러닝 로그 저장 완료")
    }

    // 러닝 로그 조회
    @GetMapping("/get")
    fun getRunningLog(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<RunningLogDTO>> {
        val pageable = PageRequest.of(page, size)
        val userId = SecurityUtil.getCurrentUserId()
        val runningLogs = runningLogApiService.getRunningLog(userId, pageable)

        val pagedResponse = PagedResponse(
            content = runningLogs.content,
            totalPages = runningLogs.totalPages,
            totalElements = runningLogs.totalElements,
            currentPage = runningLogs.number,
            pageSize = runningLogs.size,
        )

        return ResponseEntity.ok(pagedResponse)
    }
}