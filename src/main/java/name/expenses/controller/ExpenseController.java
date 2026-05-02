package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.expense.ExpenseReqDto;
import name.expenses.dto.expense.ExpenseUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ResponseDto> createExpense(@Valid @RequestBody ExpenseReqDto expenseReqDto) {
        log.info("Creating expense with name: {}", expenseReqDto.getName());
        ResponseDto response = expenseService.create(expenseReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refNo/{refNo}")
    public ResponseEntity<ResponseDto> getExpense(@PathVariable String refNo) {
        log.info("Fetching expense with refNo: {}", refNo);
        ResponseDto response = expenseService.get(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseDto> getExpenseByName(@PathVariable String name) {
        log.info("Fetching expense by name: {}", name);
        ResponseDto response = expenseService.getExpenseByName(name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{refNo}")
    public ResponseEntity<ResponseDto> updateExpense(@PathVariable String refNo,
                                                     @Valid @RequestBody ExpenseUpdateDto expenseUpdateDto) {
        log.info("Updating expense with refNo: {}", refNo);
        ResponseDto response = expenseService.update(refNo, expenseUpdateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{refNo}")
    public ResponseEntity<ResponseDto> deleteExpense(@PathVariable String refNo) {
        log.info("Deleting expense with refNo: {}", refNo);
        ResponseDto response = expenseService.delete(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getAllExpenses(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all expenses - page: {}, perPage: {}", page, perPage);
        ResponseDto response = expenseService.getAllEntities(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/noSubCategory")
    public ResponseEntity<ResponseDto> getAllWithoutSubCategory(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all expenses without sub-category");
        ResponseDto response = expenseService.getAllEntitiesWithoutSubCategory(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }
}
