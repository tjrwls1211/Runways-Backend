package syntax.backend.runways.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/naver")
class NaverApiController(
    @Value("\${naver.client.id}") private val naverClientId: String
) {
    @GetMapping("/map")
    fun showMap2(): String {
        val naverMap = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
                "\n" +
                "  <!-- \uD83D\uDCA1 네이버 지도 SDK 로드 -->\n" +
                "  <script th:inline=\"javascript\">\n" +
                "    /*<![CDATA[*/\n" +
                "    var naverClientId = /*[[${naverClientId}]]*/ 'defaultClientId';\n" +
                "    /*]]>*/\n" +
                "  </script>\n" +
                "  <script type=\"text/javascript\" th:src=\"|https://openapi.map.naver.com/openapi/v3/maps.js?ncpClientId=${naverClientId}|\"></script>\n" +
                "\n" +
                "  <!-- \uD83D\uDCA1 기본 스타일 -->\n" +
                "  <style>\n" +
                "    html, body {\n" +
                "      margin: 0;\n" +
                "      padding: 0;\n" +
                "      width: 100%;\n" +
                "      height: 100%;\n" +
                "    }\n" +
                "    #map {\n" +
                "      width: 100%;\n" +
                "      height: 100%;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <!-- \uD83D\uDDFA\uFE0F 지도 영역 -->\n" +
                "  <div id=\"map\"></div>\n" +
                "\n" +
                "  <!-- ❌ 지도 생성 제거, WebView에서 따로 처리할 것 -->\n" +
                "  <script>\n" +
                "    console.log(\"\uD83D\uDEF0 네이버 지도 SDK 로드 완료 - 지도는 WebView에서 동적으로 생성됩니다.\");\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>"
        return naverMap
    }
}