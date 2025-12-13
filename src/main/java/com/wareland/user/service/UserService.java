package com.wareland.user.service;

import com.wareland.common.exception.BadRequestException;
import com.wareland.common.exception.InvalidCredentialException;
import com.wareland.common.exception.ResourceNotFoundException;
import com.wareland.user.dto.LoginRequest;
import com.wareland.user.dto.UpdateProfileRequest;
import com.wareland.user.dto.UserProfileResponse;
import com.wareland.user.dto.UserRegisterRequest;
import com.wareland.user.model.Buyer;
import com.wareland.user.model.Seller;
import com.wareland.user.model.User;
import com.wareland.user.model.UserRole;
import com.wareland.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;


import java.util.List;

/**
 * Service layer untuk logika bisnis manajemen user:
 * registrasi, login, update profil, dan delete akun.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToProfile)   // gunakan mapper yang sudah ada
                .collect(Collectors.toList());

    }

    public UserProfileResponse register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username sudah digunakan");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email sudah digunakan");
        }

        User user;
        if (request.getRole() == UserRole.BUYER) {
            Buyer buyer = new Buyer();
            buyer.register();
            user = buyer;
        } else if (request.getRole() == UserRole.SELLER) {
            Seller seller = new Seller();
            seller.register();
            user = seller;
        } else {
            throw new BadRequestException("Role tidak dikenal");
        }

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        User saved = userRepository.save(user);
        return mapToProfile(saved);
    }

    public UserProfileResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialException("Username atau password salah"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Username atau password salah");
        }

        return mapToProfile(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long id) {
        User user = getUserById(id);
        return mapToProfile(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan username " + username + " tidak ditemukan"));
        return mapToProfile(user);
    }

    public UserProfileResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = getUserById(id);

        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {

            throw new BadRequestException("Email sudah digunakan");
        }

        // Update data basic (parsial)
        user.updateBasicProfile(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        // Jika user ingin update password
        if (request.getOldPassword() != null || request.getNewPassword() != null) {

            if (request.getOldPassword() == null || request.getNewPassword() == null) {
                throw new BadRequestException("Password lama dan baru harus diisi");
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new InvalidCredentialException("Password lama tidak sesuai");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        User updated = userRepository.save(user);
        return mapToProfile(updated);
    }


    public void deleteAccount(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User dengan ID " + id + " tidak ditemukan"));
    }

    public List<UserProfileResponse> getUsersByRole(String role) {
        UserRole userRole = UserRole.valueOf(role.toUpperCase());

        return userRepository.findByUserRole(userRole)
                .stream()
                .map(this::mapToProfile)
                .collect(Collectors.toList());
    }


    private UserProfileResponse mapToProfile(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUserRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
