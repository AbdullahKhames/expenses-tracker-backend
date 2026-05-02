package name.expenses.service;

import name.expenses.dto.sub_category.SubCategoryReqDto;
import name.expenses.dto.sub_category.SubCategoryUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.model.SubCategory;

public interface SubCategoryService extends CrudService<SubCategoryReqDto, SubCategoryUpdateDto, SubCategory> {
    ResponseDto getSubCategoryByName(String name);
    ResponseDto getAllEntitiesWithoutCategory(Long page, Long perPage, String sortBy, SortDirection dir);
    ResponseDto getSubCategoryExpenses(String refNo);
}
