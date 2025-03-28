package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.entity.Comment
import syntax.backend.runways.service.CommentApiService
import java.util.UUID

@RestController
@RequestMapping("api/comment")
class CommentApiController(
    private val commentApiService: CommentApiService,
) {

    @GetMapping("/{id}")
    fun getCommentList(
        @RequestHeader("Authorization")
        @RequestParam courseId: UUID,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<List<Comment>> {
        val comment = commentApiService.getCommentList(courseId, page, size)
        return ResponseEntity.ok(comment)
    }

    @PostMapping("/insert")
    fun insertComment(@RequestHeader("Authorization") token: String, @RequestParam courseId: UUID, content: String ): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = commentApiService.insertComment(courseId, content, jwtToken)
        return ResponseEntity.ok(result)
    }

    @PatchMapping("/update")
    fun updateComment(@RequestHeader("Authorization") token: String, @RequestParam commentId: UUID, content: String ): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = commentApiService.updateComment(commentId, content, jwtToken)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/delete")
    fun deleteComment(@RequestHeader("Authorization") token: String, @RequestParam commentId: UUID): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = commentApiService.deleteComment(commentId, jwtToken)
        return ResponseEntity.ok(result)
    }
}