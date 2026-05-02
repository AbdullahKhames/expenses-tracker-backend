package name.expenses.service;

import name.expenses.dto.account.AccountReqDto;
import name.expenses.dto.account.AccountUpdateDto;
import name.expenses.model.Account;
import name.expenses.globals.responses.ResponseDto;

public interface AccountService extends CrudService<AccountReqDto, AccountUpdateDto, Account> {
    ResponseDto addAssociation(String accountRefNo, String budgetRefNo);
    ResponseDto removeAssociation(String accountRefNo, String budgetRefNo);
    ResponseDto getAccountBudgets(String refNo);
    ResponseDto getAccountByName(String name);
    Account getDefaultAccount();
}
