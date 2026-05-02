package name.expenses.service;

import name.expenses.dto.category.CategoryReqDto;
import name.expenses.dto.category.CategoryUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.model.Category;

public interface CategoryService extends CrudService<CategoryReqDto, CategoryUpdateDto, Category> {
    ResponseDto getCategoryByName(String name);
    ResponseDto addAssociation(String categoryRefNo, String subCategoryRefNo);
    ResponseDto removeAssociation(String categoryRefNo, String subCategoryRefNo);
    ResponseDto getSubcategories(String refNo, Long page, Long perPage, String sortBy, SortDirection dir);
}
