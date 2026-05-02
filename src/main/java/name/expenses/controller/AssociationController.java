package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.association.AssociationReqDto;
import name.expenses.dto.budget.BudgetReqDto;
import name.expenses.dto.expense.ExpenseReqDto;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.model.enums.EntityType;
import name.expenses.service.association.AssociationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/association")
@RequiredArgsConstructor
@Slf4j
public class AssociationController {

    private final AssociationManager associationManager;

    // ---- Customer associations ----

    @PutMapping("/customers/{customerRef}/add-accounts")
    public ResponseEntity<ResponseDto> addAccountsToCustomer(@PathVariable String customerRef,
                                                             @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding accounts to customer: {}", customerRef);
        ResponseDto response = associationManager.addAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.ACCOUNT);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/remove-accounts")
    public ResponseEntity<ResponseDto> removeAccountsFromCustomer(@PathVariable String customerRef,
                                                                  @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing accounts from customer: {}", customerRef);
        ResponseDto response = associationManager.removeAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.ACCOUNT);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/add-categories")
    public ResponseEntity<ResponseDto> addCategoriesToCustomer(@PathVariable String customerRef,
                                                               @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding categories to customer: {}", customerRef);
        ResponseDto response = associationManager.addAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/remove-categories")
    public ResponseEntity<ResponseDto> removeCategoriesFromCustomer(@PathVariable String customerRef,
                                                                    @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing categories from customer: {}", customerRef);
        ResponseDto response = associationManager.removeAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/add-sub-categories")
    public ResponseEntity<ResponseDto> addSubCategoriesToCustomer(@PathVariable String customerRef,
                                                                  @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding sub-categories to customer: {}", customerRef);
        ResponseDto response = associationManager.addAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.SUB_CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/remove-sub-categories")
    public ResponseEntity<ResponseDto> removeSubCategoriesFromCustomer(@PathVariable String customerRef,
                                                                      @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing sub-categories from customer: {}", customerRef);
        ResponseDto response = associationManager.removeAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.SUB_CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/add-expenses")
    public ResponseEntity<ResponseDto> addExpensesToCustomer(@PathVariable String customerRef,
                                                             @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding expenses to customer: {}", customerRef);
        ResponseDto response = associationManager.addAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/add-expenses-dtos")
    public ResponseEntity<ResponseDto> addExpensesDtosToCustomer(@PathVariable String customerRef,
                                                                 @Valid @RequestBody Set<ExpenseReqDto> expenseReqDtos) {
        log.info("Adding expenses via DTOs to customer: {}", customerRef);
        ResponseDto response = associationManager.addDtoAssociation(customerRef, EntityType.CUSTOMER, expenseReqDtos, EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/remove-expenses")
    public ResponseEntity<ResponseDto> removeExpensesFromCustomer(@PathVariable String customerRef,
                                                                  @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing expenses from customer: {}", customerRef);
        ResponseDto response = associationManager.removeAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/add-budgets")
    public ResponseEntity<ResponseDto> addBudgetsToCustomer(@PathVariable String customerRef,
                                                            @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding budgets to customer: {}", customerRef);
        ResponseDto response = associationManager.addAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/add-budgets-dtos")
    public ResponseEntity<ResponseDto> addBudgetsDtosToCustomer(@PathVariable String customerRef,
                                                                @Valid @RequestBody Set<BudgetReqDto> budgetReqDtos) {
        log.info("Adding budgets via DTOs to customer: {}", customerRef);
        ResponseDto response = associationManager.addDtoAssociation(customerRef, EntityType.CUSTOMER, budgetReqDtos, EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerRef}/remove-budgets")
    public ResponseEntity<ResponseDto> removeBudgetsFromCustomer(@PathVariable String customerRef,
                                                                 @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing budgets from customer: {}", customerRef);
        ResponseDto response = associationManager.removeAssociation(customerRef, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }

    // ---- Account associations ----

    @PutMapping("/accounts/{accountRef}/add-budgets")
    public ResponseEntity<ResponseDto> addBudgetsToAccount(@PathVariable String accountRef,
                                                           @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding budgets to account: {}", accountRef);
        ResponseDto response = associationManager.addAssociation(accountRef, EntityType.ACCOUNT, reqDto.getAssociationRefNos(), EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/{accountRef}/remove-budgets")
    public ResponseEntity<ResponseDto> removeBudgetsFromAccount(@PathVariable String accountRef,
                                                                @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing budgets from account: {}", accountRef);
        ResponseDto response = associationManager.removeAssociation(accountRef, EntityType.ACCOUNT, reqDto.getAssociationRefNos(), EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }

    // ---- Category associations ----

    @PutMapping("/categories/{categoryRef}/add-sub-categories")
    public ResponseEntity<ResponseDto> addSubCategoriesToCategory(@PathVariable String categoryRef,
                                                                  @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding sub-categories to category: {}", categoryRef);
        ResponseDto response = associationManager.addAssociation(categoryRef, EntityType.CATEGORY, reqDto.getAssociationRefNos(), EntityType.SUB_CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/categories/{categoryRef}/remove-sub-categories")
    public ResponseEntity<ResponseDto> removeSubCategoriesFromCategory(@PathVariable String categoryRef,
                                                                      @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing sub-categories from category: {}", categoryRef);
        ResponseDto response = associationManager.removeAssociation(categoryRef, EntityType.CATEGORY, reqDto.getAssociationRefNos(), EntityType.SUB_CATEGORY);
        return ResponseEntity.ok(response);
    }

    // ---- SubCategory associations ----

    @PutMapping("/sub-categories/{subCategoryRef}/add-expenses")
    public ResponseEntity<ResponseDto> addExpensesToSubCategory(@PathVariable String subCategoryRef,
                                                                @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding expenses to sub-category: {}", subCategoryRef);
        ResponseDto response = associationManager.addAssociation(subCategoryRef, EntityType.SUB_CATEGORY, reqDto.getAssociationRefNos(), EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/sub-categories/{subCategoryRef}/remove-expenses")
    public ResponseEntity<ResponseDto> removeExpensesFromSubCategory(@PathVariable String subCategoryRef,
                                                                     @Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing expenses from sub-category: {}", subCategoryRef);
        ResponseDto response = associationManager.removeAssociation(subCategoryRef, EntityType.SUB_CATEGORY, reqDto.getAssociationRefNos(), EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }
}
