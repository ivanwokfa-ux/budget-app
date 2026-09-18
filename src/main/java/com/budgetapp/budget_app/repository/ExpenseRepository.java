package com.budgetapp.budget_app.repository;

import com.budgetapp.budget_app.model.Expense;
import com.budgetapp.budget_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Toutes les dépenses d'un utilisateur
    List<Expense> findByUser(User user);

    // Dépenses d'un utilisateur par catégorie
    List<Expense> findByUserAndCategory(User user, String category);

    // Dépenses d'un utilisateur entre deux dates
    List<Expense> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
}