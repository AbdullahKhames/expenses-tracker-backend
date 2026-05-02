package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.category.CategoryReqDto;
import name.expenses.dto.category.CategoryUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ResponseDto> createCategory(@Valid @RequestBody CategoryReqDto categoryReqDto) {
        log.info("Creating category with name: {}", categoryReqDto.getName());
        ResponseDto response = categoryService.create(categoryReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refNo/{refNo}")
    public ResponseEntity<ResponseDto> getCategory(@PathVariable String refNo) {
        log.info("Fetching category with refNo: {}", refNo);
        ResponseDto response = categoryService.get(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseDto> getCategoryByName(@PathVariable String name) {
        log.info("Fetching category by name: {}", name);
        ResponseDto response = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{refNo}")
    public ResponseEntity<ResponseDto> updateCategory(@PathVariable String refNo,
                                                      @Valid @RequestBody CategoryUpdateDto categoryUpdateDto) {
        log.info("Updating category with refNo: {}", refNo);
        ResponseDto response = categoryService.update(refNo, categoryUpdateDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{refNo}/subCategories")
    public ResponseEntity<ResponseDto> getSubcategories(
            @PathVariable String refNo,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching subcategories for category: {}", refNo);
        ResponseDto response = categoryService.getSubcategories(refNo, page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/addAssociation/{categoryRefNo}/{subCategoryRefNo}")
    public ResponseEntity<ResponseDto> addAssociation(@PathVariable String categoryRefNo,
                                                      @PathVariable String subCategoryRefNo) {
        log.info("Adding sub-category {} to category {}", subCategoryRefNo, categoryRefNo);
        ResponseDto response = categoryService.addAssociation(categoryRefNo, subCategoryRefNo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/removeAssociation/{categoryRefNo}/{subCategoryRefNo}")
    public ResponseEntity<ResponseDto> removeAssociation(@PathVariable String categoryRefNo,
                                                         @PathVariable String subCategoryRefNo) {
        log.info("Removing sub-category {} from category {}", subCategoryRefNo, categoryRefNo);
        ResponseDto response = categoryService.removeAssociation(categoryRefNo, subCategoryRefNo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{refNo}")
    public ResponseEntity<ResponseDto> deleteCategory(@PathVariable String refNo) {
        log.info("Deleting category with refNo: {}", refNo);
        ResponseDto response = categoryService.delete(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getAllCategories(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all categories - page: {}, perPage: {}", page, perPage);
        ResponseDto response = categoryService.getAllEntities(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }
}
