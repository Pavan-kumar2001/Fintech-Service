package com.fintech.fintech_service.security.service;

import com.fintech.fintech_service.entity.User;
import com.fintech.fintech_service.enums.Role;
import com.fintech.fintech_service.enums.Status;
import com.fintech.fintech_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomInitializer {

    @Bean
    public CommandLineRunner createDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder){
        return  args -> {
            System.out.println();
            if(userRepository.findByUserName("Admin").isEmpty()){
                String rawPassword = "Admin@123";
                User admin=new User();
                admin.setUserName("Admin");
                admin.setMobile("9353177331");
                admin.setPassword(passwordEncoder.encode(rawPassword));
                admin.setRole(Role.ADMIN);
                admin.setStatus(Status.ACTIVE);
                userRepository.save(admin);
                admin.setCreatedAt(LocalDateTime.now());
                System.out.println("Default Admin Created - mobile: "+admin.getMobile()+" Password: "+rawPassword);
            }

            if(userRepository.findByUserName("User").isEmpty()){
                String rawPassword = "User@123";
                User user=new User();
                user.setUserName("User");
                user.setMobile("6363622640");
                user.setPassword(passwordEncoder.encode("User@123"));
                user.setRole(Role.USER);
                user.setStatus(Status.ACTIVE);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
                System.out.println("Default User Created - mobile: "+user.getMobile()+" Password: "+rawPassword);
            }

            if(userRepository.findByUserName("Analyst").isEmpty()){
                String rawPassword = "Analyst@123";
                User user=new User();
                user.setUserName("Analyst");
                user.setMobile("6778986543");
                user.setPassword(passwordEncoder.encode("Analyst@123"));
                user.setRole(Role.ANALYST);
                user.setStatus(Status.ACTIVE);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
                System.out.println("Default Analyst Created - mobile: "+user.getMobile()+" Password: "+rawPassword);
            }

            if(userRepository.findByUserName("Viewer").isEmpty()){
                String rawPassword = "Viewer@123";
                User user=new User();
                user.setUserName("Viewer");
                user.setMobile("8788899765");
                user.setPassword(passwordEncoder.encode("Viewer@123"));
                user.setRole(Role.VIEWER);
                user.setStatus(Status.ACTIVE);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
                System.out.println("Default User Created mobile: "+user.getMobile()+" Password: "+rawPassword);
            }


        };
    }


}
