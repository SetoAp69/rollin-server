import com.rollinup.server.model.Role

data class JwtAuthClaim(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    private val _role: String = ""
) {
    val role
        get() = Role.fromValue(_role) ?: Role.STUDENT
}