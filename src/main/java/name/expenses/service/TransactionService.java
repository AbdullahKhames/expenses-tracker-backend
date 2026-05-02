package name.expenses.service;

import name.expenses.dto.transaction.TransactionReqDto;
import name.expenses.dto.transaction.TransactionUpdateDto;
import name.expenses.model.Transaction;

public interface TransactionService extends CrudService<TransactionReqDto, TransactionUpdateDto, Transaction> {
}
