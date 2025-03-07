package syntax.backend.runways

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import io.github.cdimascio.dotenv.dotenv

@SpringBootApplication
class RunwaysApplication

fun main(args: Array<String>) {
    val dotenv = dotenv()
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
    runApplication<RunwaysApplication>(*args)
}
