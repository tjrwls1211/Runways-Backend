package syntax.backend.runways.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.dto.ResponseCommentDTO
import syntax.backend.runways.dto.UpdateCommentDTO
import syntax.backend.runways.service.CommentApiService
import java.util.UUID

@RestController
@RequestMapping("api/comment")
class CommentApiController(
    private val commentApiService: CommentApiService
) {
    // 댓글 조회
    @GetMapping("/list")
    fun getCommentList(
        @RequestHeader("Authorization") token: String,
        @RequestParam courseId: UUID,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<Page<ResponseCommentDTO>> {
        val jwtToken = token.substring(7)
        val pageable = PageRequest.of(page, size)
        val parentComments = commentApiService.getParentCommentList(courseId, pageable, jwtToken)
        return ResponseEntity.ok(parentComments)
    }

    // 답글 조회
    @GetMapping("/list/{parentId}")
    fun getChildCommentList(
        @RequestHeader("Authorization") token: String,
        @PathVariable parentId: UUID,
        @RequestParam courseId: UUID,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<Page<ResponseCommentDTO>> {
        val jwtToken = token.substring(7)
        val pageable = PageRequest.of(page, size)
        val childComments = commentApiService.getChildCommentList(parentId, courseId, pageable, jwtToken)
        return ResponseEntity.ok(childComments)
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
    fun updateComment(@RequestHeader("Authorization") token: String, @RequestBody updateCommentDTO: UpdateCommentDTO ): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = commentApiService.updateComment(updateCommentDTO, jwtToken)
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