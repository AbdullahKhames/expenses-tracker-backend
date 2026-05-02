package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.account.AccountReqDto;
import name.expenses.dto.account.AccountRespDto;
import name.expenses.dto.account.AccountUpdateDto;
import name.expenses.dto.budget.BudgetRespDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.AccountMapper;
import name.expenses.mapper.BudgetMapper;
import name.expenses.model.Account;
import name.expenses.model.Budget;
import name.expenses.repository.AccountRepository;
import name.expenses.repository.BudgetRepository;
import name.expenses.service.AccountService;
import name.expenses.utils.FieldValidator;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    @Override
    @CacheEvict(value = "accounts", allEntries = true)
    public ResponseDto create(AccountReqDto reqDto) {
        Account account = accountMapper.reqDtoToEntity(reqDto);
        account = accountRepository.save(account);
        log.info("Created account with refNo: {}", account.getRefNo());
        return ResponseDtoBuilder.getCreateResponse("Account", account.getRefNo(),
                accountMapper.entityToRespDto(account));
    }

    @Override
    @Cacheable("accounts")
    public ResponseDto get(String refNo) {
        Account account = accountRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("Account", refNo,
                accountMapper.entityToRespDto(account));
    }

    @Override
    public Optional<Account> getEntity(String refNo) {
        return accountRepository.findByRefNoAndDeletedFalse(refNo);
    }

    @Override
    @CacheEvict(value = "accounts", allEntries = true)
    public ResponseDto update(String refNo, AccountUpdateDto updateDto) {
        Account account = accountRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found with refNo: " + refNo));
        accountMapper.updateEntityFromDto(updateDto, account);
        account = accountRepository.save(account);
        log.info("Updated account with refNo: {}", refNo);
        return ResponseDtoBuilder.getUpdateResponse("Account", refNo,
                accountMapper.entityToRespDto(account));
    }

    @Override
    @CacheEvict(value = "accounts", allEntries = true)
    public ResponseDto delete(String refNo) {
        Account account = accountRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found with refNo: " + refNo));
        account.setDeleted(true);
        accountRepository.save(account);
        log.info("Soft-deleted account with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("Account", refNo);
    }

    @Override
    public ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, Account.class)) {
            sortBy = "id";
        }

        Sort sort = sortDirection == SortDirection.DESC
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);

        PageRequest pageRequest = PageRequest.of(
                pageNumber.intValue() - 1,
                pageSize.intValue(),
                sort
        );

        Page<Account> entityPage = accountRepository.findAllByDeletedFalse(pageRequest);
        name.expenses.globals.Page<AccountRespDto> dtoPage = accountMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("Account", dtoPage);
    }

    @Override
    public Set<Account> getEntities(Set<String> refNos) {
        return accountRepository.findAllByRefNoIn(refNos);
    }

    @Override
    public ResponseDto getAccountByName(String name) {
        var accounts = accountRepository.findByNameContainingIgnoreCaseAndDeletedFalse(name);
        var dtos = accountMapper.entitiesToRespDtos(accounts);
        return ResponseDtoBuilder.getFetchAllResponse("Account", dtos);
    }

    @Override
    public ResponseDto getAccountBudgets(String refNo) {
        Account account = accountRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found with refNo: " + refNo));
        Set<BudgetRespDto> budgetDtos = account.getBudgets().stream()
                .filter(b -> !b.isDeleted())
                .map(budgetMapper::entityToRespDto)
                .collect(Collectors.toSet());
        return ResponseDtoBuilder.getFetchResponse("Account Budgets", refNo, budgetDtos);
    }

    @Override
    @CacheEvict(value = "accounts", allEntries = true)
    public ResponseDto addAssociation(String accountRefNo, String budgetRefNo) {
        Account account = accountRepository.findByRefNoAndDeletedFalse(accountRefNo)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found with refNo: " + accountRefNo));
        Budget budget = budgetRepository.findByRefNoAndDeletedFalse(budgetRefNo)
                .orElseThrow(() -> new ObjectNotFoundException("Budget not found with refNo: " + budgetRefNo));
        account.getBudgets().add(budget);
        accountRepository.save(account);
        log.info("Added budget {} to account {}", budgetRefNo, accountRefNo);
        return ResponseDtoBuilder.getUpdateResponse("Account", accountRefNo,
                accountMapper.entityToRespDto(account));
    }

    @Override
    @CacheEvict(value = "accounts", allEntries = true)
    public ResponseDto removeAssociation(String accountRefNo, String budgetRefNo) {
        Account account = accountRepository.findByRefNoAndDeletedFalse(accountRefNo)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found with refNo: " + accountRefNo));
        Budget budget = budgetRepository.findByRefNoAndDeletedFalse(budgetRefNo)
                .orElseThrow(() -> new ObjectNotFoundException("Budget not found with refNo: " + budgetRefNo));
        account.getBudgets().remove(budget);
        accountRepository.save(account);
        log.info("Removed budget {} from account {}", budgetRefNo, accountRefNo);
        return ResponseDtoBuilder.getUpdateResponse("Account", accountRefNo,
                accountMapper.entityToRespDto(account));
    }

    @Override
    public Account getDefaultAccount() {
        return accountRepository.findDefaultAccount()
                .orElseGet(() -> {
                    Account defaultAccount = new Account();
                    defaultAccount.setName("default account");
                    defaultAccount.setDetails("Default system account");
                    defaultAccount = accountRepository.save(defaultAccount);
                    log.info("Created default account with refNo: {}", defaultAccount.getRefNo());
                    return defaultAccount;
                });
    }
}
