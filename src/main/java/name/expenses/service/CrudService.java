package name.expenses.service;

import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;

import java.util.Optional;
import java.util.Set;

public interface CrudService<REQ, UPD, ENTITY> {
    ResponseDto create(REQ reqDto);
    ResponseDto get(String refNo);
    Optional<ENTITY> getEntity(String refNo);
    ResponseDto update(String refNo, UPD updateDto);
    ResponseDto delete(String refNo);
    ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection);
    Set<ENTITY> getEntities(Set<String> refNos);
}
