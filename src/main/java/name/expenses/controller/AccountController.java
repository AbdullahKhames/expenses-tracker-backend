package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.account.AccountReqDto;
import name.expenses.dto.account.AccountUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ResponseDto> createAccount(@Valid @RequestBody AccountReqDto accountReqDto) {
        log.info("Creating account with name: {}", accountReqDto.getName());
        ResponseDto response = accountService.create(accountReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refNo/{refNo}")
    public ResponseEntity<ResponseDto> getAccount(@PathVariable String refNo) {
        log.info("Fetching account with refNo: {}", refNo);
        ResponseDto response = accountService.get(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseDto> getAccountByName(@PathVariable String name) {
        log.info("Fetching account by name: {}", name);
        ResponseDto response = accountService.getAccountByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{refNo}/budgets")
    public ResponseEntity<ResponseDto> getAccountBudgets(@PathVariable String refNo) {
        log.info("Fetching budgets for account: {}", refNo);
        ResponseDto response = accountService.getAccountBudgets(refNo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{refNo}")
    public ResponseEntity<ResponseDto> updateAccount(@PathVariable String refNo,
                                                     @Valid @RequestBody AccountUpdateDto accountUpdateDto) {
        log.info("Updating account with refNo: {}", refNo);
        ResponseDto response = accountService.update(refNo, accountUpdateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{refNo}")
    public ResponseEntity<ResponseDto> deleteAccount(@PathVariable String refNo) {
        log.info("Deleting account with refNo: {}", refNo);
        ResponseDto response = accountService.delete(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getAllAccounts(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all accounts - page: {}, perPage: {}, sortBy: {}, sortDirection: {}", page, perPage, sortBy, sortDirection);
        ResponseDto response = accountService.getAllEntities(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/addAssociation/{accountRefNo}/{budgetRefNo}")
    public ResponseEntity<ResponseDto> addAssociation(@PathVariable String accountRefNo,
                                                      @PathVariable String budgetRefNo) {
        log.info("Adding budget {} to account {}", budgetRefNo, accountRefNo);
        ResponseDto response = accountService.addAssociation(accountRefNo, budgetRefNo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/removeAssociation/{accountRefNo}/{budgetRefNo}")
    public ResponseEntity<ResponseDto> removeAssociation(@PathVariable String accountRefNo,
                                                         @PathVariable String budgetRefNo) {
        log.info("Removing budget {} from account {}", budgetRefNo, accountRefNo);
        ResponseDto response = accountService.removeAssociation(accountRefNo, budgetRefNo);
        return ResponseEntity.ok(response);
    }
}
