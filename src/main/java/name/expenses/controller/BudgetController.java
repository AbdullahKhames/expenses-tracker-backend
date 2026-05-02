package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.budget.BudgetReqDto;
import name.expenses.dto.budget.BudgetUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<ResponseDto> createBudget(@Valid @RequestBody BudgetReqDto budgetReqDto) {
        log.info("Creating budget with name: {}", budgetReqDto.getName());
        ResponseDto response = budgetService.create(budgetReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refNo/{refNo}")
    public ResponseEntity<ResponseDto> getBudget(@PathVariable String refNo) {
        log.info("Fetching budget with refNo: {}", refNo);
        ResponseDto response = budgetService.get(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseDto> getBudgetByName(@PathVariable String name) {
        log.info("Fetching budget by name: {}", name);
        ResponseDto response = budgetService.getBudgetByName(name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{refNo}")
    public ResponseEntity<ResponseDto> updateBudget(@PathVariable String refNo,
                                                    @Valid @RequestBody BudgetUpdateDto budgetUpdateDto) {
        log.info("Updating budget with refNo: {}", refNo);
        ResponseDto response = budgetService.update(refNo, budgetUpdateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{refNo}")
    public ResponseEntity<ResponseDto> deleteBudget(@PathVariable String refNo) {
        log.info("Deleting budget with refNo: {}", refNo);
        ResponseDto response = budgetService.delete(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getAllBudgets(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all budgets - page: {}, perPage: {}, sortBy: {}, sortDirection: {}", page, perPage, sortBy, sortDirection);
        ResponseDto response = budgetService.getAllEntities(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/noAccount")
    public ResponseEntity<ResponseDto> getAllBudgetsWithoutAccount(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all budgets without account");
        ResponseDto response = budgetService.getAllEntitiesWithoutAccount(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }
}
