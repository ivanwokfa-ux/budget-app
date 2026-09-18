package com.budgetapp.budget_app.service;

import com.budgetapp.budget_app.model.User;
import com.budgetapp.budget_app.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email déjà utilisé !");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    // Créer un compte admin
    public User registerAdmin(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email déjà utilisé !");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ADMIN");
        return userRepository.save(user);
    }

    // Trouver un utilisateur par email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Récupérer tous les utilisateurs (admin seulement)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}