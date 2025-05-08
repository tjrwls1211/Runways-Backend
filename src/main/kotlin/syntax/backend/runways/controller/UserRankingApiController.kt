package syntax.backend.runways.controller

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runways.dto.PagedResponse
import syntax.backend.runways.dto.UserRankingDTO
import syntax.backend.runways.service.UserRankingService

@RestController
@RequestMapping("/api/ranking")
class UserRankingApiController(
    private val userRankingService: UserRankingService
) {

    @GetMapping("/season")
    fun getSeasonRanking(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<UserRankingDTO>> {
        val pageable = PageRequest.of(page, size)
        val rankingPage = userRankingService.getSeasonRanking(pageable)

        val pagedResponse = PagedResponse(
            content = rankingPage.content,
            totalPages = rankingPage.totalPages,
            totalElements = rankingPage.totalElements,
            currentPage = rankingPage.number,
            pageSize = rankingPage.size
        )

        return ResponseEntity.ok(pagedResponse)
    }

}