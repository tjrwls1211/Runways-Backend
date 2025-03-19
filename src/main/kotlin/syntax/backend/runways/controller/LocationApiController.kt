package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runways.service.LocationApiService
import syntax.backend.runways.dto.LocationDataDTO

@RestController
@RequestMapping("/api/location")
class LocationApiController(
    private val locationApiService: LocationApiService
) {
    @GetMapping
    fun getLocationData(@RequestParam nx: Double, @RequestParam ny: Double): ResponseEntity<LocationDataDTO> {
        val nearLocation = locationApiService.getNearestLocation(nx, ny) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            LocationDataDTO(
                nearLocation.sido,
                nearLocation.sigungu,
                nearLocation.daegioyem
            )
        )
    }
}
