package syntax.backend.runways.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/naver")
class NaverApiController {

    @GetMapping
    fun getNaverApi(): String {
        return "naver_map"
    }
}