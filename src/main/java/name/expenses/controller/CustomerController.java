package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.association.AssociationReqDto;
import name.expenses.dto.budget.BudgetReqDto;
import name.expenses.dto.customer.CustomerReqDto;
import name.expenses.dto.customer.CustomerUpdateDto;
import name.expenses.dto.expense.ExpenseReqDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.model.enums.EntityType;
import name.expenses.service.CustomerService;
import name.expenses.service.association.AssociationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final AssociationManager associationManager;

    @PostMapping
    public ResponseEntity<ResponseDto> createCustomer(@Valid @RequestBody CustomerReqDto customerReqDto) {
        log.info("Creating customer");
        ResponseDto response = customerService.create(customerReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{refNo}")
    public ResponseEntity<ResponseDto> getCustomer(@PathVariable String refNo) {
        log.info("Fetching customer with refNo: {}", refNo);
        ResponseDto response = customerService.get(refNo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{refNo}")
    public ResponseEntity<ResponseDto> updateCustomer(@PathVariable String refNo,
                                                      @Valid @RequestBody CustomerUpdateDto customerUpdateDto) {
        log.info("Updating customer with refNo: {}", refNo);
        ResponseDto response = customerService.update(refNo, customerUpdateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{refNo}")
    public ResponseEntity<ResponseDto> deleteCustomer(@PathVariable String refNo) {
        log.info("Deleting customer with refNo: {}", refNo);
        ResponseDto response = customerService.delete(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getAllCustomers(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all customers - page: {}, perPage: {}", page, perPage);
        ResponseDto response = customerService.getAllEntities(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    // ---- Customer-scoped entity listing endpoints ----

    @GetMapping("/accounts")
    public ResponseEntity<ResponseDto> getAllCustomerAccounts(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all customer accounts");
        ResponseDto response = customerService.getAllCustomerAccounts(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountRef}/budgets")
    public ResponseEntity<ResponseDto> getAllCustomerAccountBudgets(
            @PathVariable String accountRef,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching budgets for customer account: {}", accountRef);
        ResponseDto response = customerService.getAllCustomerAccountBudgets(accountRef, page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }


    @GetMapping("/sub-categories/{subCategoryRef}/expenses")
    public ResponseEntity<ResponseDto> getAllCustomerSubCategoryExpenses(
            @PathVariable String subCategoryRef,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching expenses for customer sub-category: {}", subCategoryRef);
        ResponseDto response = customerService.getAllCustomerSubCategoryExpenses(subCategoryRef, page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/budgets")
    public ResponseEntity<ResponseDto> getAllCustomerBudgets(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all customer budgets");
        ResponseDto response = customerService.getAllCustomerBudgets(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expenses")
    public ResponseEntity<ResponseDto> getAllCustomerExpenses(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all customer expenses");
        ResponseDto response = customerService.getAllCustomerExpenses(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<ResponseDto> getAllCustomerTransactions(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all customer transactions");
        ResponseDto response = customerService.getAllCustomerTransactions(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/budget-transfers")
    public ResponseEntity<ResponseDto> getAllCustomerBudgetTransfers(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all customer budget transfers");
        ResponseDto response = customerService.getAllCustomerBudgetTransfers(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    // ---- Get customer association endpoints ----

    @GetMapping("/get-accounts")
    public ResponseEntity<ResponseDto> getCustomerAccounts() {
        log.info("Getting customer accounts association");
        ResponseDto response = customerService.getCustomerAssociation(EntityType.ACCOUNT);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-categories")
    public ResponseEntity<ResponseDto> getCustomerCategories() {
        log.info("Getting customer categories association");
        ResponseDto response = customerService.getCustomerAssociation(EntityType.CATEGORY);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-sub-categories")
    public ResponseEntity<ResponseDto> getCustomerSubCategories() {
        log.info("Getting customer sub-categories association");
        ResponseDto response = customerService.getCustomerAssociation(EntityType.SUB_CATEGORY);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-budgets")
    public ResponseEntity<ResponseDto> getCustomerBudgets() {
        log.info("Getting customer budgets association");
        ResponseDto response = customerService.getCustomerAssociation(EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-expenses")
    public ResponseEntity<ResponseDto> getCustomerExpenses() {
        log.info("Getting customer expenses association");
        ResponseDto response = customerService.getCustomerAssociation(EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-transactions")
    public ResponseEntity<ResponseDto> getCustomerTransactions() {
        log.info("Getting customer transactions association");
        ResponseDto response = customerService.getCustomerAssociation(EntityType.TRANSACTION);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/budget-transfers")
    public ResponseEntity<ResponseDto> getBudgetTransfers() {
        log.info("Getting customer transactions association");
        ResponseDto response = customerService.getCustomerAssociation(EntityType.BUDGET_TRANSFER);
        return ResponseEntity.ok(response);
    }

    // ---- Association management endpoints ----

    @PutMapping("/add-accounts")
    public ResponseEntity<ResponseDto> addAccounts(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding accounts to customer");
        ResponseDto response = associationManager.addAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.ACCOUNT);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/remove-accounts")
    public ResponseEntity<ResponseDto> removeAccounts(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing accounts from customer");
        ResponseDto response = associationManager.removeAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.ACCOUNT);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-categories")
    public ResponseEntity<ResponseDto> addCategories(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding categories to customer");
        ResponseDto response = associationManager.addAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/remove-categories")
    public ResponseEntity<ResponseDto> removeCategories(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing categories from customer");
        ResponseDto response = associationManager.removeAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-sub-categories")
    public ResponseEntity<ResponseDto> addSubCategories(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding sub-categories to customer");
        ResponseDto response = associationManager.addAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.SUB_CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/remove-sub-categories")
    public ResponseEntity<ResponseDto> removeSubCategories(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing sub-categories from customer");
        ResponseDto response = associationManager.removeAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.SUB_CATEGORY);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-expenses")
    public ResponseEntity<ResponseDto> addExpenses(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding expenses to customer");
        ResponseDto response = associationManager.addAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/remove-expenses")
    public ResponseEntity<ResponseDto> removeExpenses(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing expenses from customer");
        ResponseDto response = associationManager.removeAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-budgets")
    public ResponseEntity<ResponseDto> addBudgets(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Adding budgets to customer");
        ResponseDto response = associationManager.addAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/remove-budgets")
    public ResponseEntity<ResponseDto> removeBudgets(@Valid @RequestBody AssociationReqDto reqDto) {
        log.info("Removing budgets from customer");
        ResponseDto response = associationManager.removeAssociation(null, EntityType.CUSTOMER, reqDto.getAssociationRefNos(), EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }

    // ---- DTO-based association endpoints ----

    @PutMapping("/add-expenses-dtos")
    public ResponseEntity<ResponseDto> addExpensesDtos(@Valid @RequestBody Set<ExpenseReqDto> expenseReqDtos) {
        log.info("Adding expenses via DTOs to customer");
        ResponseDto response = associationManager.addDtoAssociation(null, EntityType.CUSTOMER, expenseReqDtos, EntityType.EXPENSE);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/add-budgets-dtos")
    public ResponseEntity<ResponseDto> addBudgetsDtos(@Valid @RequestBody Set<BudgetReqDto> budgetReqDtos) {
        log.info("Adding budgets via DTOs to customer");
        ResponseDto response = associationManager.addDtoAssociation(null, EntityType.CUSTOMER, budgetReqDtos, EntityType.BUDGET);
        return ResponseEntity.ok(response);
    }
}
