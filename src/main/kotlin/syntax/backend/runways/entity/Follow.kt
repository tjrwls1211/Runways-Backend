package syntax.backend.runways.entity

// USER 테이블 칼럼
data class Follow(
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList()
)
