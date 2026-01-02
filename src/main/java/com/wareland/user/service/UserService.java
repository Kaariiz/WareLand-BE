package com.wareland.user.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * Service layer untuk logika bisnis manajemen user:
 * registrasi, login, pengelolaan profil, dan penghapusan akun.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Mengambil seluruh data user.
     */
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToProfile)
                .collect(Collectors.toList());
    }

    /**
     * Registrasi user baru berdasarkan role.
     */
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

    /**
     * Proses login menggunakan username atau email.
     */
    public UserProfileResponse login(LoginRequest request) {
        String identifier =
                request.getUsername() != null
                        ? request.getUsername().trim()
                        : null;

        if (identifier == null || identifier.isEmpty()) {
            throw new BadRequestException("Username/Email harus diisi");
        }

        User user;
        if (identifier.contains("@")) {
            if (!isValidEmail(identifier)) {
                throw new BadRequestException("Format email tidak valid");
            }
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() ->
                            new InvalidCredentialException("Email atau password salah"));
        } else {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() ->
                            new InvalidCredentialException("Username atau password salah"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialException("Kredensial login salah");
        }

        return mapToProfile(user);
    }

    /**
     * Validasi format email sederhana untuk kebutuhan login.
     */
    private boolean isValidEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) return false;

        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        if (local.isEmpty() || domain.isEmpty()) return false;
        if (!domain.contains(".")) return false;
        if (domain.startsWith(".") || domain.endsWith(".")) return false;

        return true;
    }

    /**
     * Mengambil profil user berdasarkan ID.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long id) {
        return mapToProfile(getUserById(id));
    }

    /**
     * Mengambil profil user berdasarkan username.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User dengan username " + username + " tidak ditemukan"));
        return mapToProfile(user);
    }

    /**
     * Update profil user (parsial).
     */
    public UserProfileResponse updateProfile(
            Long id,
            UpdateProfileRequest request
    ) {
        User user = getUserById(id);

        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email sudah digunakan");
        }

        user.updateBasicProfile(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getImageUrl()
        );

        // Update password jika diminta
        if (request.getOldPassword() != null || request.getNewPassword() != null) {

            if (request.getOldPassword() == null ||
                request.getNewPassword() == null) {
                throw new BadRequestException(
                        "Password lama dan baru harus diisi");
            }

            if (!passwordEncoder.matches(
                    request.getOldPassword(),
                    user.getPassword())) {
                throw new InvalidCredentialException(
                        "Password lama tidak sesuai");
            }

            user.setPassword(
                    passwordEncoder.encode(request.getNewPassword()));
        }

        User updated = userRepository.save(user);
        return mapToProfile(updated);
    }

    /**
     * Menghapus akun user berdasarkan ID.
     */
    public void deleteAccount(Long id) {
        userRepository.delete(getUserById(id));
    }

    /**
     * Mengambil user berdasarkan ID.
     */
    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User dengan ID " + id + " tidak ditemukan"));
    }

    /**
     * Mengambil daftar user berdasarkan role.
     */
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersByRole(String role) {
        Class<? extends User> userType;
        if ("SELLER".equalsIgnoreCase(role)) {
            userType = Seller.class;
        } else if ("BUYER".equalsIgnoreCase(role)) {
            userType = Buyer.class;
        } else {
            throw new BadRequestException("Role tidak dikenal: " + role);
        }

        return userRepository.findByType(userType)
                .stream()
                .map(this::mapToProfile)
                .collect(Collectors.toList());
    }

    /**
     * Mapper internal dari entity User ke UserProfileResponse.
     */
    private UserProfileResponse mapToProfile(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUserRole(),
                user.getImageUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
