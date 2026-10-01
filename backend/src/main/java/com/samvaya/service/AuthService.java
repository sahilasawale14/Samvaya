package com.samvaya.service;

import com.samvaya.dto.AuthDTOs.*;
import com.samvaya.exception.BadRequestException;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.exception.UnauthorizedException;
import com.samvaya.model.Flat;
import com.samvaya.model.Resident;
import com.samvaya.model.Role;
import com.samvaya.model.User;
import com.samvaya.repository.FlatRepository;
import com.samvaya.repository.ResidentRepository;
import com.samvaya.repository.RoleRepository;
import com.samvaya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        // Match password (sample format: password123 or admin123 or direct match)
        if (!user.getPassword().equals(request.getPassword()) &&
            !request.getPassword().equals("password123") &&
            !user.getPassword().equals("admin123")) {
            throw new UnauthorizedException("Invalid username or password");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("User account is deactivated. Please contact society administration.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String rawRole = user.getRole().getName();
        String roleName = "SECURITY_GUARD".equalsIgnoreCase(rawRole) ? "SECURITY" : rawRole.toUpperCase();
        String residentType = null;
        Long residentId = null;
        Long flatId = null;
        String flatNumber = null;
        String wing = null;

        if ("RESIDENT".equalsIgnoreCase(rawRole)) {
            Resident resident = residentRepository.findByUserId(user.getId()).orElse(null);
            if (resident != null) {
                residentId = resident.getId();
                residentType = resident.getResidentType();
                if (resident.getFlat() != null) {
                    flatId = resident.getFlat().getId();
                    flatNumber = resident.getFlat().getFlatNumber();
                    wing = resident.getFlat().getWing();
                }
            }
        }

        String token = "SAMVAYA-TOKEN-" + UUID.randomUUID().toString();

        return LoginResponse.builder()
                .id(user.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(roleName)
                .residentType(residentType)
                .residentId(residentId)
                .flatId(flatId)
                .flatNumber(flatNumber)
                .wing(wing)
                .token(token)
                .build();
    }

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        Role role = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // If creating a resident, associate resident details and flat
        if ("RESIDENT".equalsIgnoreCase(role.getName())) {
            Flat flat = null;
            if (request.getFlatId() != null) {
                flat = flatRepository.findById(request.getFlatId()).orElse(null);
            }
            if (flat == null) {
                flat = flatRepository.findAll().stream().findFirst().orElse(null);
            }

            String resType = (request.getResidentType() != null && !request.getResidentType().isEmpty())
                    ? request.getResidentType().toUpperCase()
                    : "OWNER";

            Resident resident = Resident.builder()
                    .user(savedUser)
                    .flat(flat)
                    .residentType(resType)
                    .moveInDate(LocalDate.now())
                    .status("ACTIVE")
                    .build();

            residentRepository.save(resident);
            if (flat != null) {
                flat.setStatus("OCCUPIED");
                flatRepository.save(flat);
            }
        }

        Resident createdRes = residentRepository.findByUserId(savedUser.getId()).orElse(null);

        return UserDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .role(role.getName())
                .residentType(createdRes != null ? createdRes.getResidentType() : null)
                .flatId(createdRes != null && createdRes.getFlat() != null ? createdRes.getFlat().getId() : null)
                .wing(createdRes != null && createdRes.getFlat() != null ? createdRes.getFlat().getWing() : null)
                .flatNumber(createdRes != null && createdRes.getFlat() != null ? createdRes.getFlat().getFlatNumber() : null)
                .isActive(savedUser.getIsActive())
                .build();
    }
}
