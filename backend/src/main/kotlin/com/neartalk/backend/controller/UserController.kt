package com.neartalk.backend.controller

import com.neartalk.backend.models.User
import com.neartalk.backend.repository.UserRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userRepository: UserRepository) {

    @PostMapping("/register")
    fun register(@RequestBody user: User): User {
        return userRepository.save(user)
    }
}
