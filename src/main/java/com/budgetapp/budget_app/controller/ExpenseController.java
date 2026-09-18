package com.budgetapp.budget_app.controller;

import com.budgetapp.budget_app.model.Expense;
import com.budgetapp.budget_app.service.ExpenseService;
import com.budgetapp.budget_app.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final JwtService jwtService;

    public ExpenseController(ExpenseService expenseService, JwtService jwtService) {
        this.expenseService = expenseService;
        this.jwtService = jwtService;
    }

    // Extraire l'email depuis le token JWT
    private String getEmailFromToken(String authHeader) {
        String token = authHeader.substring(7); // Enlève "Bearer "
        return jwtService.extractEmail(token);
    }

    // Ajouter une dépense
    @PostMapping
    public ResponseEntity<?> addExpense(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String email = getEmailFromToken(authHeader);
            Double amount = Double.parseDouble(request.get("amount"));
            String description = request.get("description");
            LocalDate date = LocalDate.parse(request.get("date"));

            Expense expense = expenseService.addExpense(email, amount, description, date);
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Voir toutes mes dépenses
    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestHeader("Authorization") String authHeader) {
        String email = getEmailFromToken(authHeader);
        return ResponseEntity.ok(expenseService.getExpensesByUser(email));
    }

    // Voir mon total
    @GetMapping("/total")
    public ResponseEntity<?> getTotal(
            @RequestHeader("Authorization") String authHeader) {
        String email = getEmailFromToken(authHeader);
        Double total = expenseService.getTotalByUser(email);
        return ResponseEntity.ok(Map.of("total", total));
    }

    // Voir mes dépenses par catégorie
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Expense>> getByCategory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String category) {
        String email = getEmailFromToken(authHeader);
        return ResponseEntity.ok(expenseService.getExpensesByCategory(email, category));
    }

    // Résumé mensuel
    @GetMapping("/summary")
    public ResponseEntity<?> getMonthlySummary(
            @RequestHeader("Authorization") String authHeader) {
        String email = getEmailFromToken(authHeader);
        return ResponseEntity.ok(expenseService.getMonthlySummary(email));
    }

    // Alerte de dépassement de budget
    @GetMapping("/alert")
    public ResponseEntity<?> getBudgetAlert(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Double budget) {
        String email = getEmailFromToken(authHeader);
        return ResponseEntity.ok(expenseService.getBudgetAlert(email, budget));
    }
}