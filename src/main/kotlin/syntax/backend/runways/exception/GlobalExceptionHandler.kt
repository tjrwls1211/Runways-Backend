package syntax.backend.runways.exception

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleException(e: Exception): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("오류 발생: ${e.message}")
    }

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFoundException(e: UserNotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다: ${e.message}")
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(ex: EntityNotFoundException, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity("요청한 엔티티를 찾을 수 없습니다: ${ex.message}", HttpStatus.NOT_FOUND)
    }
}