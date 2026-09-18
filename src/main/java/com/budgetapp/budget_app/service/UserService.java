package com.budgetapp.budget_app.service;

import com.budgetapp.budget_app.model.User;
import com.budgetapp.budget_app.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Inscription
    public User registerUser(String email, String password) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email déjà utilisé !");
        }

        // Créer le nouvel utilisateur
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // Hashage

        return userRepository.save(user);
    }

    // Trouver un utilisateur par email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}