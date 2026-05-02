package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.transaction.TransactionReqDto;
import name.expenses.dto.transaction.TransactionUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ResponseDto> createTransaction(@Valid @RequestBody TransactionReqDto transactionReqDto) {
        log.info("Creating transaction with name: {}", transactionReqDto.getName());
        ResponseDto response = transactionService.create(transactionReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{refNo}")
    public ResponseEntity<ResponseDto> getTransaction(@PathVariable String refNo) {
        log.info("Fetching transaction with refNo: {}", refNo);
        ResponseDto response = transactionService.get(refNo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{refNo}")
    public ResponseEntity<ResponseDto> updateTransaction(@PathVariable String refNo,
                                                         @Valid @RequestBody TransactionUpdateDto transactionUpdateDto) {
        log.info("Updating transaction with refNo: {}", refNo);
        ResponseDto response = transactionService.update(refNo, transactionUpdateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{refNo}")
    public ResponseEntity<ResponseDto> deleteTransaction(@PathVariable String refNo) {
        log.info("Deleting transaction with refNo: {}", refNo);
        ResponseDto response = transactionService.delete(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getAllTransactions(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all transactions - page: {}, perPage: {}", page, perPage);
        ResponseDto response = transactionService.getAllEntities(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }
}
