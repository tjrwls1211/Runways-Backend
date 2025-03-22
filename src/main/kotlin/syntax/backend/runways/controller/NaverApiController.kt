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
        val naverMap = "<html lang=\"ko\">\n" +
                "            <head>\n" +
                "                <meta charset=\"utf-8\">\n" +
                "                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "                <script>\n" +
                "                    var script = document.createElement('script');\n" +
                "                    script.src = \"https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=nkrxv181ko\";\n" +
                "                    script.onload = function() {\n" +
                "                        new naver.maps.Map('map', {\n" +
                "                            center: new naver.maps.LatLng(37.5665, 126.9780), // 서울 좌표\n" +
                "                            zoom: 15\n" +
                "                        });\n" +
                "                    };\n" +
                "                    document.head.appendChild(script);\n" +
                "                </script>\n" +
                "                <style>\n" +
                "                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; }\n" +
                "                    #map { width: 100%; height:100%; }\n" +
                "                </style>\n" +
                "            </head>\n" +
                "            <body>\n" +
                "                <div id=\"map\" />\n" +
                "            </body>\n" +
                "        </html>"
        return naverMap
    }
}