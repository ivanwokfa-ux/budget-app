package com.budgetapp.budget_app.controller;

import com.budgetapp.budget_app.model.Expense;
import com.budgetapp.budget_app.model.User;
import com.budgetapp.budget_app.service.ExpenseService;
import com.budgetapp.budget_app.service.JwtService;
import com.budgetapp.budget_app.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ExpenseService expenseService;
    private final JwtService jwtService;

    // Code secret admin — change-le !
    private static final String ADMIN_SECRET = "lise emmanuelle 2005";

    public AdminController(UserService userService, ExpenseService expenseService, JwtService jwtService) {
        this.userService = userService;
        this.expenseService = expenseService;
        this.jwtService = jwtService;
    }

    // Créer un compte admin (protégé par code secret)
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(
            @RequestHeader("X-Admin-Secret") String secret,
            @RequestBody Map<String, String> request) {
        if (!ADMIN_SECRET.equals(secret)) {
            return ResponseEntity.status(403).body("Code secret incorrect !");
        }
        try {
            String email = request.get("email");
            String password = request.get("password");
            User user = userService.registerAdmin(email, password);
            return ResponseEntity.ok("Compte admin créé ! ID : " + user.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Voir tous les utilisateurs
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        return userService.findByEmail(email)
                .filter(user -> "ADMIN".equals(user.getRole()))
                .map(user -> ResponseEntity.ok(userService.getAllUsers()))
                .orElse(ResponseEntity.status(403).build());
    }

    // Voir toutes les dépenses
    @GetMapping("/expenses")
    public ResponseEntity<?> getAllExpenses(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        return userService.findByEmail(email)
                .filter(user -> "ADMIN".equals(user.getRole()))
                .map(user -> ResponseEntity.ok(expenseService.getAllExpenses()))
                .orElse(ResponseEntity.status(403).build());
    }
}