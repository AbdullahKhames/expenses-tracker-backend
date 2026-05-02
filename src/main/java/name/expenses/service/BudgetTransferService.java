package name.expenses.service;

import name.expenses.dto.budget_transfer.BudgetTransferReqDto;
import name.expenses.dto.budget_transfer.BudgetTransferUpdateDto;
import name.expenses.model.BudgetTransfer;

public interface BudgetTransferService extends CrudService<BudgetTransferReqDto, BudgetTransferUpdateDto, BudgetTransfer> {
}
