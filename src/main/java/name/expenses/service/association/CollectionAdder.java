package name.expenses.service.association;

import name.expenses.dto.association.AssociationResponse;
import name.expenses.model.enums.EntityType;

import java.util.Set;

public interface CollectionAdder {
    AssociationResponse addAssociation(Object entity, EntityType entityType, Set<String> refNos);
    AssociationResponse addDtoAssociation(Object entity, EntityType entityType, Set<?> dtos);
}
