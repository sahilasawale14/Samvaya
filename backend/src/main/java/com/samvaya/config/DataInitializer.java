package com.samvaya.config;

import com.samvaya.model.*;
import com.samvaya.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Initializes essential roles and default administrative/security accounts.
 * Leaves flats, residents, parking slots, and bills clean for custom entry.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;
    private final NoticeRepository noticeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Verifying core roles and administrative accounts...");

        // 1. Core Roles
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));

        Role residentRole = roleRepository.findByName("RESIDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("RESIDENT").build()));

        Role securityRole = roleRepository.findByName("SECURITY_GUARD")
                .orElseGet(() -> roleRepository.save(Role.builder().name("SECURITY_GUARD").build()));

        // 2. Default Administrator Account (admin / admin123)
        User adminUser = userRepository.findByUsername("admin")
                .map(existingAdmin -> {
                    existingAdmin.setPassword("admin123");
                    existingAdmin.setRole(adminRole);
                    existingAdmin.setIsActive(true);
                    return userRepository.save(existingAdmin);
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("admin")
                        .password("admin123")
                        .email("admin@samvaya.org")
                        .fullName("Society Administrator")
                        .phone("+91 98000 00001")
                        .role(adminRole)
                        .isActive(true)
                        .build()));

        // 3. Default Security Guard Account (guard1 / guard123)
        userRepository.findByUsername("guard1")
                .map(existingGuard -> {
                    existingGuard.setPassword("guard123");
                    existingGuard.setRole(securityRole);
                    existingGuard.setIsActive(true);
                    return userRepository.save(existingGuard);
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("guard1")
                        .password("guard123")
                        .email("guard1@samvaya.org")
                        .fullName("Vikram Singh")
                        .phone("+91 98000 00004")
                        .role(securityRole)
                        .isActive(true)
                        .build()));

        // 4. Default Amenities (for resident booking functionality)
        if (amenityRepository.count() == 0) {
            amenityRepository.save(Amenity.builder()
                    .name("Clubhouse & Banquet Hall")
                    .description("Air-conditioned community hall for private events and celebrations")
                    .capacity(120)
                    .openTime(LocalTime.of(8, 0))
                    .closeTime(LocalTime.of(23, 0))
                    .hourlyRate(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            amenityRepository.save(Amenity.builder()
                    .name("Swimming Pool & Sun Deck")
                    .description("Filtered half-olympic pool with certified lifeguard on duty")
                    .capacity(30)
                    .openTime(LocalTime.of(6, 0))
                    .closeTime(LocalTime.of(21, 0))
                    .hourlyRate(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            amenityRepository.save(Amenity.builder()
                    .name("Badminton Court")
                    .description("Indoor wooden floor badminton court with LED tournament lighting")
                    .capacity(4)
                    .openTime(LocalTime.of(6, 0))
                    .closeTime(LocalTime.of(22, 0))
                    .hourlyRate(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            amenityRepository.save(Amenity.builder()
                    .name("Fitness Center & Gym")
                    .description("Modern gymnasium equipped with cardio machines, free weights and trainers")
                    .capacity(25)
                    .openTime(LocalTime.of(5, 30))
                    .closeTime(LocalTime.of(22, 30))
                    .hourlyRate(BigDecimal.ZERO)
                    .isActive(true)
                    .build());
        }

        // 5. Default Welcome Notice
        if (noticeRepository.count() == 0) {
            noticeRepository.save(Notice.builder()
                    .title("Welcome to Samvaya Society Management")
                    .content("Society operations are live. Residents may view invoices, raise maintenance tickets, and pre-approve visitors.")
                    .category("GENERAL")
                    .priority("MEDIUM")
                    .publishedDate(LocalDate.now())
                    .expiryDate(LocalDate.now().plusDays(30))
                    .isActive(true)
                    .createdByUser(adminUser)
                    .build());
        }

        log.info("Core initialization completed: Admin ('admin'/'admin123') and Guard ('guard1'/'guard123') ready.");
    }
}
