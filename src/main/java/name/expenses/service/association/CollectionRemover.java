package name.expenses.service.association;

import name.expenses.dto.association.AssociationResponse;
import name.expenses.model.enums.EntityType;

import java.util.Set;

public interface CollectionRemover {
    AssociationResponse removeAssociation(Object entity, EntityType entityType, Set<String> refNos);
}
