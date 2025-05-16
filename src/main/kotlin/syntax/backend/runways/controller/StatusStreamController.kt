package syntax.backend.runways.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import syntax.backend.runways.dto.LlmRequestDTO
import syntax.backend.runways.dto.StatusMessageDTO
import syntax.backend.runways.service.CourseApiService

@Controller
class StatusStreamController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val courseApiService: CourseApiService,
) {

    @MessageMapping("/status/course/generate")
    fun generateCourse(
        llmRequestDTO: LlmRequestDTO,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        // 세션 ID 가져오기
        val session = "/topic/status/${llmRequestDTO.statusSessionId}"

        // 세션에서 사용자 ID 가져오기
        val userId = headerAccessor.sessionAttributes?.get("userId") as? String

        if (userId == null) {
            println("세션에 사용자 ID 없음")
            messagingTemplate.convertAndSend(session, StatusMessageDTO("ERROR", "인증되지 않은 사용자입니다.", null))
            return
        }

        println("사용자 인증 완료: $userId")
        messagingTemplate.convertAndSend(session, StatusMessageDTO("RECEIVED", "요청 준비 중...", null))

        try {
            println("AI 서버에 요청 전송: ${llmRequestDTO.statusSessionId}")
            messagingTemplate.convertAndSend(session, StatusMessageDTO("PROMPTING", "AI 서버에 요청을 보내는 중...", null))

            println("AI 서버에 요청 전송 완료: ${llmRequestDTO.statusSessionId}")
            messagingTemplate.convertAndSend(session, StatusMessageDTO("GENERATING", "AI 분석 및 처리 중...", null))

            val response = courseApiService.createCourseByLLM(llmRequestDTO, userId)

            println("AI 서버 응답 수신 완료: ${llmRequestDTO.statusSessionId}")
            messagingTemplate.convertAndSend(session, StatusMessageDTO("COMPLETED", "코스 생성 완료!", response))
        } catch (e: Exception) {
            e.printStackTrace()
            messagingTemplate.convertAndSend(session, StatusMessageDTO("ERROR", "생성 중에 문제가 발생하였습니다.", null))
        }
    }
}
