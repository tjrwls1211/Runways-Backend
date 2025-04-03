package syntax.backend.runways.controller

import syntax.backend.runways.dto.CommentListResponseDTO
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.dto.ResponseCommentDTO
import syntax.backend.runways.service.CommentApiService
import java.util.UUID

@RestController
@RequestMapping("api/comment")
class CommentApiController(
    private val commentApiService: CommentApiService
) {
    @GetMapping("/list")
    fun getCommentList(
        @RequestParam courseId: UUID,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<CommentListResponseDTO> {
        val pageable = PageRequest.of(page, size)
        val parentComments = commentApiService.getParentCommentList(courseId, pageable)
        val childComments = commentApiService.getChildCommentList(courseId, pageable)
        val response = CommentListResponseDTO(parentComments, childComments)
        return ResponseEntity.ok(response)
    }

    // 댓글 입력
    @PostMapping("/insert")
    fun insertComment(@RequestHeader("Authorization") token: String, @RequestBody requestInsertCommentDTO: RequestInsertCommentDTO): ResponseEntity<ResponseCommentDTO> {
        val jwtToken = token.substring(7)
        val result = commentApiService.insertComment(requestInsertCommentDTO, jwtToken)
        return ResponseEntity.ok(result)
    }

    // 댓글 업데이트
    @PatchMapping("/update")
    fun updateComment(@RequestHeader("Authorization") token: String, @RequestParam commentId: UUID, content: String ): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = commentApiService.updateComment(commentId, content, jwtToken)
        return ResponseEntity.ok(result)
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    fun deleteComment(@RequestHeader("Authorization") token: String, @PathVariable commentId: UUID): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = commentApiService.deleteComment(commentId, jwtToken)
        return ResponseEntity.ok(result)
    }
}
