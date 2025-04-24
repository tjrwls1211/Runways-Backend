package syntax.backend.runways.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/naver")
class NaverApiController(
    @Value("\${naver.client.id}") private val naverClientId: String
) {
    @GetMapping("/map")
    fun showMap(@RequestParam nx: Double, @RequestParam ny: Double): String {

        // 좌표 유효성 검사
        val x: Double
        val y: Double

        if (nx !in 124.0..132.0 || ny !in 33.0..43.0) {
            x = 126.9780
            y = 37.5665
        } else {
            x = nx
            y = ny
        }

        val naverMap = """
            <html lang="ko">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <script>
                        var script = document.createElement('script');
                        script.src = "https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=$naverClientId";
                        script.onload = function() {
                            new naver.maps.Map('map', {
                                center: new naver.maps.LatLng($y, $x), 
                                zoom: 15
                            });
                        };
                        document.head.appendChild(script);
                    </script>
                    <style>
                        body, html { margin: 0; padding: 0; width: 100%; height: 100%; }
                        #map { width: 100%; height:100%; }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                </body>
            </html>
        """.trimIndent()
        return naverMap
    }
}