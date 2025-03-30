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
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <script th:inline=\"javascript\">\n" +
                "        /*<![CDATA[*/\n" +
                "        var naverClientId = /*[[${naverClientId}]]*/ 'defaultClientId';\n" +
                "        /*]]>*/\n" +
                "    </script>\n" +
                "    <script type=\"text/javascript\" th:src=\"|https://openapi.map.naver.com/openapi/v3/maps.js?ncpClientId=${naverClientId}|\"></script>\n" +
                "    <style>\n" +
                "        body, html { margin: 0; padding: 0; width: 100%; height: 100%; }\n" +
                "        #map { width: 100%; height: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"map\"></div>\n" +
                "<script>\n" +
                "    function sendLog(message) {\n" +
                "        console.log(message);\n" +
                "    }\n" +
                "\n" +
                "    try {\n" +
                "        sendLog(\"네이버 지도 API 로딩 시작\");\n" +
                "\n" +
                "        if (!window.naver || !window.naver.maps) {\n" +
                "            sendLog(\"네이버 지도 API 로드 실패: naver.maps 객체가 존재하지 않습니다.\");\n" +
                "        } else {\n" +
                "            sendLog(\"네이버 지도 API 로드 성공!\");\n" +
                "\n" +
                "            var map = new naver.maps.Map('map', {\n" +
                "                center: new naver.maps.LatLng(37.5665, 126.9780),\n" +
                "                zoom: 15\n" +
                "            });\n" +
                "\n" +
                "            sendLog(\"네이버 지도 로딩 완료!\");\n" +
                "        }\n" +
                "    } catch (error) {\n" +
                "        sendLog(\"네이버 지도 API 오류: \" + error.message);\n" +
                "    }\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>"
        return naverMap
    }
}