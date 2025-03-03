package syntax.backend.runity.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import syntax.backend.runity.entity.User

@Controller
@RequestMapping("/login")
class UserApiController {

    @GetMapping
    fun loginPage(): String {
        return "login"
    }

    @GetMapping("/auth")
    @ResponseBody
    fun main(): ResponseEntity<User> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is OAuth2AuthenticationToken) {
            val oAuth2User = authentication.principal as OAuth2User
            val attributes = oAuth2User.attributes

            val user = User(
                id = attributes["sub"] as String,
                name = attributes["name"] as String,
                email = attributes["email"] as String
            )

            return ResponseEntity.ok(user)
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}