package syntax.backend.runity.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.spec.SecretKeySpec


@Component
class JwtUtil {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration}")
    private lateinit var expiration: String

    // 토큰 생성
    fun generateToken(id : String): String {
        return createToken(id)
    }

    // 토큰 생성 내부 메서드
    private fun createToken(id : String): String {
        // 시크릿 키 값 디코딩해서 바이트 배열 변환
        val keyBytes = Base64.getDecoder().decode(secret)
        // SecretKeySpec 객체 생성
        val key = SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.jcaName)

        return Jwts.builder()
            .setSubject(id)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration.toLong()))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    // 토큰 유효성 검증
    fun validateToken(token: String): Boolean {
        return try {
            // 시크릿 키 값 디코딩해서 바이트 배열 변환
            val keyBytes = Base64.getDecoder().decode(secret)
            // SecretKeySpec 객체 생성
            val key = SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.jcaName)

            // 토큰 파싱
            val claims: Claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            val expirationDate: Date = claims.expiration
            !expirationDate.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    // 토큰에서 ID 추출
    fun extractUsername(token: String): String {
        // 시크릿 키 값 디코딩해서 바이트 배열 변환
        val keyBytes = Base64.getDecoder().decode(secret)
        // SecretKeySpec 객체 생성
        val key = SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.jcaName)

        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

}