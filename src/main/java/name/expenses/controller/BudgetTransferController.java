package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.budget_transfer.BudgetTransferReqDto;
import name.expenses.dto.budget_transfer.BudgetTransferUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.service.BudgetTransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget-transfers")
@RequiredArgsConstructor
@Slf4j
public class BudgetTransferController {

    private final BudgetTransferService budgetTransferService;

    @PostMapping
    public ResponseEntity<ResponseDto> createBudgetTransfer(@Valid @RequestBody BudgetTransferReqDto budgetTransferReqDto) {
        log.info("Creating budget transfer with name: {}", budgetTransferReqDto.getName());
        ResponseDto response = budgetTransferService.create(budgetTransferReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{refNo}")
    public ResponseEntity<ResponseDto> getBudgetTransfer(@PathVariable String refNo) {
        log.info("Fetching budget transfer with refNo: {}", refNo);
        ResponseDto response = budgetTransferService.get(refNo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{refNo}")
    public ResponseEntity<ResponseDto> updateBudgetTransfer(@PathVariable String refNo,
                                                            @Valid @RequestBody BudgetTransferUpdateDto budgetTransferUpdateDto) {
        log.info("Updating budget transfer with refNo: {}", refNo);
        ResponseDto response = budgetTransferService.update(refNo, budgetTransferUpdateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{refNo}")
    public ResponseEntity<ResponseDto> deleteBudgetTransfer(@PathVariable String refNo) {
        log.info("Deleting budget transfer with refNo: {}", refNo);
        ResponseDto response = budgetTransferService.delete(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getAllBudgetTransfers(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all budget transfers - page: {}, perPage: {}", page, perPage);
        ResponseDto response = budgetTransferService.getAllEntities(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }
}
