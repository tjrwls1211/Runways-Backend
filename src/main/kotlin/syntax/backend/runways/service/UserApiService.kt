package syntax.backend.runways.service

import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.FollowProfileDTO
import syntax.backend.runways.dto.RequestUserInfoDTO
import syntax.backend.runways.dto.ResponseMyInfoDTO
import syntax.backend.runways.dto.UserProfileWithCoursesDTO
import syntax.backend.runways.entity.User

interface UserApiService {
    fun getUserDataFromToken(token: String): User
    fun getUserInfoFromToken(token: String, pageable: Pageable): ResponseMyInfoDTO
    fun updateUserInfo(token: String, requestUserInfoDTO: RequestUserInfoDTO) : Int
    fun isNicknameDuplicate(nickname: String): Boolean
    fun deleteUser(token: String)
    fun registerDeviceId(token: String, deviceId:String)
    fun addFollow(senderId: String, receiverId: String)
    fun removeFollowing(senderId: String, receiverId: String)
    fun removeFollower(senderId: String, receiverId: String)
    fun getFollowerList(userId: String): List<FollowProfileDTO>
    fun getFollowingList(userId: String): List<FollowProfileDTO>
    fun getUserInfoFromId(senderId: String,receiverId: String, pageable: Pageable): UserProfileWithCoursesDTO
}