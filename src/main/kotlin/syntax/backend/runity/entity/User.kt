package syntax.backend.runity.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: String,
    @Column(nullable = false, length = 100)
    var name: String,
    @Column(nullable = false, length = 100)
    var email: String
)