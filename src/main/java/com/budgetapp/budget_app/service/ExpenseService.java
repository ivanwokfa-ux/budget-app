package com.budgetapp.budget_app.service;

import com.budgetapp.budget_app.model.Expense;
import com.budgetapp.budget_app.model.User;
import com.budgetapp.budget_app.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.util.Map;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public ExpenseService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    // Catégorisation automatique
    private String autoCategorie(String description) {
        String desc = description.toLowerCase();
        if (desc.contains("carrefour") || desc.contains("leclerc") || desc.contains("lidl")) {
            return "Alimentation";
        } else if (desc.contains("uber") || desc.contains("sncf") || desc.contains("bus")) {
            return "Transport";
        } else if (desc.contains("netflix") || desc.contains("spotify") || desc.contains("cinema")) {
            return "Loisirs";
        } else if (desc.contains("pharmacie") || desc.contains("docteur") || desc.contains("medecin")) {
            return "Santé";
        } else {
            return "Autre";
        }
    }

    // Ajouter une dépense
    public Expense addExpense(String email, Double amount, String description, LocalDate date) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Expense expense = new Expense();
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setCategory(autoCategorie(description)); // Catégorisation automatique
        expense.setDate(date);
        expense.setUser(user);

        return expenseRepository.save(expense);
    }

    // Récupérer toutes les dépenses de l'utilisateur
    public List<Expense> getExpensesByUser(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return expenseRepository.findByUser(user);
    }

    // Total des dépenses de l'utilisateur
    public Double getTotalByUser(String email) {
        return getExpensesByUser(email).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    // Dépenses par catégorie
    public List<Expense> getExpensesByCategory(String email, String category) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return expenseRepository.findByUserAndCategory(user, category);
    }

    // Dépenses du mois en cours
    public List<Expense> getExpensesThisMonth(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        return expenseRepository.findByUserAndDateBetween(user, start, end);
    }

    // Total du mois en cours
    public Double getTotalThisMonth(String email) {
        return getExpensesThisMonth(email).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    // Résumé mensuel complet
    public Map<String, Object> getMonthlySummary(String email) {
        List<Expense> expenses = getExpensesThisMonth(email);
        Double total = getTotalThisMonth(email);

        // Grouper par catégorie
        Map<String, Double> byCategory = expenses.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Expense::getCategory,
                        java.util.stream.Collectors.summingDouble(Expense::getAmount)
                ));

        // Catégorie la plus dépensée
        String topCategory = byCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Aucune");

        return Map.of(
                "mois", LocalDate.now().getMonth().toString(),
                "total", total,
                "nombreDepenses", expenses.size(),
                "parCategorie", byCategory,
                "categorieTopDepense", topCategory
        );
    }

    // Prédiction de dépassement
    public Map<String, Object> getBudgetAlert(String email, Double budgetMensuel) {
        Double totalActuel = getTotalThisMonth(email);
        int jourEcoules = LocalDate.now().getDayOfMonth();
        int joursDansMois = LocalDate.now().lengthOfMonth();

        Double depenseMoyenneParJour = totalActuel / jourEcoules;
        Double projectionFin = depenseMoyenneParJour * joursDansMois;

        boolean vaDepasser = projectionFin > budgetMensuel;
        String message = vaDepasser
                ? " Attention ! Tu vas probablement dépasser ton budget de " + String.format("%.2f", projectionFin - budgetMensuel) + "€"
                : " Tu es à sec ! Il te reste environ " + String.format("%.2f", budgetMensuel - projectionFin) + "€";

        return Map.of(
                "budgetMensuel", budgetMensuel,
                "totalActuel", totalActuel,
                "projectionFinMois", projectionFin,
                "vaDepasser", vaDepasser,
                "message", message
        );
    }
}