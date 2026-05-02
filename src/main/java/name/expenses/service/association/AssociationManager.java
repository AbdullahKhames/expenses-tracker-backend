package name.expenses.service.association;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.association.AssociationResponse;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.*;
import name.expenses.model.*;
import name.expenses.model.enums.EntityType;
import name.expenses.repository.CustomerRepository;
import name.expenses.service.*;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AssociationManager {

    private final AccountService accountService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final ExpenseService expenseService;
    private final CustomerService customerService;
    private final TransactionService transactionService;
    private final BudgetTransferService budgetTransferService;
    private final CustomerRepository customerRepository;

    // Mappers for DTO-based association creation
    private final AccountMapper accountMapper;
    private final BudgetMapper budgetMapper;
    private final CategoryMapper categoryMapper;
    private final SubCategoryMapper subCategoryMapper;
    private final ExpenseMapper expenseMapper;

    /**
     * Add associations by reference numbers.
     * Routes to the correct service based on entity type and association type.
     */
    public ResponseDto addAssociation(String entityRefNo, EntityType entityType,
                                      Set<String> associationRefNos, EntityType associationType) {
        // Resolve customer if entityRefNo is null and entityType is CUSTOMER
        entityRefNo = resolveEntityRefNo(entityRefNo, entityType);

        Object entity = getParentEntity(entityRefNo, entityType);
        Set<?> collection = getAssociationCollection(entity, associationType);

        AssociationResponse response = AssociationResponse.builder()
                .success(new HashMap<>())
                .error(new HashMap<>())
                .build();

        for (String refNo : associationRefNos) {
            Optional<?> associationOpt = getAssociationEntity(refNo, associationType);

            if (associationOpt.isEmpty()) {
                response.getError().put(refNo, "no entity corresponds to this ref no");
                continue;
            }

            Object associationEntity = associationOpt.get();

            if (collectionContains(collection, associationEntity)) {
                response.getError().put(refNo, "entity already contains this association");
            } else {
                addToCollection(entity, associationType, associationEntity);
                response.getSuccess().put(refNo, "was added successfully");
            }
        }

        saveParentEntity(entity, entityType);
        log.info("Association add completed for {} {}: {} successes, {} errors",
                entityType, entityRefNo, response.getSuccess().size(), response.getError().size());

        return ResponseDtoBuilder.getUpdateResponse("Association", entityRefNo, response);
    }

    /**
     * Remove associations by reference numbers.
     */
    public ResponseDto removeAssociation(String entityRefNo, EntityType entityType,
                                         Set<String> associationRefNos, EntityType associationType) {
        entityRefNo = resolveEntityRefNo(entityRefNo, entityType);

        Object entity = getParentEntity(entityRefNo, entityType);

        AssociationResponse response = AssociationResponse.builder()
                .success(new HashMap<>())
                .error(new HashMap<>())
                .build();

        for (String refNo : associationRefNos) {
            Optional<?> associationOpt = getAssociationEntity(refNo, associationType);

            if (associationOpt.isEmpty()) {
                response.getError().put(refNo, "no entity corresponds to this ref no");
                continue;
            }

            Object associationEntity = associationOpt.get();
            Set<?> collection = getAssociationCollection(entity, associationType);

            if (!collectionContains(collection, associationEntity)) {
                response.getError().put(refNo, "entity does not contain this association");
            } else {
                removeFromCollection(entity, associationType, associationEntity);
                response.getSuccess().put(refNo, "was removed successfully");
            }
        }

        saveParentEntity(entity, entityType);
        log.info("Association remove completed for {} {}: {} successes, {} errors",
                entityType, entityRefNo, response.getSuccess().size(), response.getError().size());

        return ResponseDtoBuilder.getUpdateResponse("Association", entityRefNo, response);
    }

    /**
     * Add associations from DTOs (creates new entities and adds them to the collection).
     */
    @SuppressWarnings("unchecked")
    public ResponseDto addDtoAssociation(String entityRefNo, EntityType entityType,
                                         Set<?> dtos, EntityType associationType) {
        entityRefNo = resolveEntityRefNo(entityRefNo, entityType);

        Object entity = getParentEntity(entityRefNo, entityType);

        AssociationResponse response = AssociationResponse.builder()
                .success(new HashMap<>())
                .error(new HashMap<>())
                .build();

        for (Object dto : dtos) {
            try {
                Object newEntity = createEntityFromDto(dto, associationType);
                addToCollection(entity, associationType, newEntity);
                String newRefNo = getRefNo(newEntity);
                response.getSuccess().put(newRefNo, "was added successfully");
            } catch (Exception e) {
                response.getError().put(dto.toString(), "failed to create entity: " + e.getMessage());
            }
        }

        saveParentEntity(entity, entityType);
        log.info("DTO association add completed for {} {}: {} successes, {} errors",
                entityType, entityRefNo, response.getSuccess().size(), response.getError().size());

        return ResponseDtoBuilder.getUpdateResponse("Association", entityRefNo, response);
    }

    // ---- Private helper methods ----

    private String resolveEntityRefNo(String entityRefNo, EntityType entityType) {
        if (entityRefNo == null && entityType == EntityType.CUSTOMER) {
            Customer customer = getCurrentCustomer();
            return customer.getRefNo();
        }
        return entityRefNo;
    }

    private Customer getCurrentCustomer() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return customerRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found for current user"));
    }

    private Object getParentEntity(String refNo, EntityType entityType) {
        Optional<?> entityOpt = switch (entityType) {
            case ACCOUNT -> accountService.getEntity(refNo);
            case BUDGET -> budgetService.getEntity(refNo);
            case CATEGORY -> categoryService.getEntity(refNo);
            case SUB_CATEGORY -> subCategoryService.getEntity(refNo);
            case EXPENSE -> expenseService.getEntity(refNo);
            case CUSTOMER -> customerService.getEntity(refNo);
            case TRANSACTION -> transactionService.getEntity(refNo);
            case BUDGET_TRANSFER -> budgetTransferService.getEntity(refNo);
        };

        return entityOpt.orElseThrow(() ->
                new ObjectNotFoundException(entityType.name() + " not found with refNo: " + refNo));
    }

    private Optional<?> getAssociationEntity(String refNo, EntityType associationType) {
        return switch (associationType) {
            case ACCOUNT -> accountService.getEntity(refNo);
            case BUDGET -> budgetService.getEntity(refNo);
            case CATEGORY -> categoryService.getEntity(refNo);
            case SUB_CATEGORY -> subCategoryService.getEntity(refNo);
            case EXPENSE -> expenseService.getEntity(refNo);
            case CUSTOMER -> customerService.getEntity(refNo);
            case TRANSACTION -> transactionService.getEntity(refNo);
            case BUDGET_TRANSFER -> budgetTransferService.getEntity(refNo);
        };
    }

    @SuppressWarnings("unchecked")
    private Set<?> getAssociationCollection(Object entity, EntityType associationType) {
        if (entity instanceof Customer customer) {
            return switch (associationType) {
                case ACCOUNT -> customer.getAccounts();
                case BUDGET -> customer.getBudgets();
                case EXPENSE -> customer.getExpenses();
                case TRANSACTION -> customer.getTransactions();
                case BUDGET_TRANSFER -> customer.getBudgetTransfers();
                default -> Collections.emptySet();
            };
        } else if (entity instanceof Account account) {
            return switch (associationType) {
                case BUDGET -> account.getBudgets();
                default -> Collections.emptySet();
            };
        } else if (entity instanceof Category category) {
            return switch (associationType) {
                case SUB_CATEGORY -> category.getSubCategories();
                default -> Collections.emptySet();
            };
        } else if (entity instanceof SubCategory subCategory) {
            return switch (associationType) {
                case EXPENSE -> subCategory.getExpenses();
                default -> Collections.emptySet();
            };
        }
        return Collections.emptySet();
    }

    private boolean collectionContains(Set<?> collection, Object entity) {
        return collection.contains(entity);
    }

    @SuppressWarnings("unchecked")
    private void addToCollection(Object parentEntity, EntityType associationType, Object associationEntity) {
        if (parentEntity instanceof Customer customer) {
            switch (associationType) {
                case ACCOUNT -> ((Set<Account>) (Set<?>) customer.getAccounts()).add((Account) associationEntity);
                case BUDGET -> {
                    Budget budget = (Budget) associationEntity;
                    budget.setCustomer(customer);
                    ((Set<Budget>) (Set<?>) customer.getBudgets()).add(budget);
                }

                case EXPENSE -> {
                    Expense expense = (Expense) associationEntity;
                    expense.setCustomer(customer);
                    ((Set<Expense>) (Set<?>) customer.getExpenses()).add(expense);
                }
                case TRANSACTION -> {
                    Transaction transaction = (Transaction) associationEntity;
                    transaction.setCustomer(customer);
                    ((Set<Transaction>) (Set<?>) customer.getTransactions()).add(transaction);
                }
                case BUDGET_TRANSFER -> {
                    BudgetTransfer transfer = (BudgetTransfer) associationEntity;
                    transfer.setCustomer(customer);
                    ((Set<BudgetTransfer>) (Set<?>) customer.getBudgetTransfers()).add(transfer);
                }
                default -> log.warn("Unsupported association type {} for Customer", associationType);
            }
        } else if (parentEntity instanceof Account account) {
            if (associationType == EntityType.BUDGET) {
                ((Set<Budget>) (Set<?>) account.getBudgets()).add((Budget) associationEntity);
            }
        } else if (parentEntity instanceof Category category) {
            if (associationType == EntityType.SUB_CATEGORY) {
                ((Set<SubCategory>) (Set<?>) category.getSubCategories()).add((SubCategory) associationEntity);
            }
        } else if (parentEntity instanceof SubCategory subCategory) {
            if (associationType == EntityType.EXPENSE) {
                ((Set<Expense>) (Set<?>) subCategory.getExpenses()).add((Expense) associationEntity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void removeFromCollection(Object parentEntity, EntityType associationType, Object associationEntity) {
        if (parentEntity instanceof Customer customer) {
            switch (associationType) {
                case ACCOUNT -> customer.getAccounts().remove(associationEntity);
                case BUDGET -> customer.getBudgets().remove(associationEntity);
                case EXPENSE -> customer.getExpenses().remove(associationEntity);
                case TRANSACTION -> customer.getTransactions().remove(associationEntity);
                case BUDGET_TRANSFER -> customer.getBudgetTransfers().remove(associationEntity);
                default -> log.warn("Unsupported association type {} for Customer", associationType);
            }
        } else if (parentEntity instanceof Account account) {
            if (associationType == EntityType.BUDGET) {
                account.getBudgets().remove(associationEntity);
            }
        } else if (parentEntity instanceof Category category) {
            if (associationType == EntityType.SUB_CATEGORY) {
                category.getSubCategories().remove(associationEntity);
            }
        } else if (parentEntity instanceof SubCategory subCategory) {
            if (associationType == EntityType.EXPENSE) {
                subCategory.getExpenses().remove(associationEntity);
            }
        }
    }

    private void saveParentEntity(Object entity, EntityType entityType) {
        switch (entityType) {
            case CUSTOMER -> customerRepository.save((Customer) entity);
            case ACCOUNT -> {
                // Account is saved via its repository through the service layer
                // The entity is already managed by JPA, so changes will be flushed
            }
            case CATEGORY, SUB_CATEGORY, EXPENSE, BUDGET, TRANSACTION, BUDGET_TRANSFER -> {
                // Managed entities are flushed automatically within the transaction
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object createEntityFromDto(Object dto, EntityType associationType) {
        return switch (associationType) {
            case ACCOUNT -> accountMapper.reqDtoToEntity(
                    (name.expenses.dto.account.AccountReqDto) dto);
            case BUDGET -> budgetMapper.reqDtoToEntity(
                    (name.expenses.dto.budget.BudgetReqDto) dto);
            case CATEGORY -> categoryMapper.reqDtoToEntity(
                    (name.expenses.dto.category.CategoryReqDto) dto);
            case SUB_CATEGORY -> subCategoryMapper.reqDtoToEntity(
                    (name.expenses.dto.sub_category.SubCategoryReqDto) dto);
            case EXPENSE -> expenseMapper.reqDtoToEntity(
                    (name.expenses.dto.expense.ExpenseReqDto) dto);
            default -> throw new IllegalArgumentException(
                    "DTO-based association creation not supported for type: " + associationType);
        };
    }

    private String getRefNo(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            return baseEntity.getRefNo();
        }
        return "unknown";
    }
}
