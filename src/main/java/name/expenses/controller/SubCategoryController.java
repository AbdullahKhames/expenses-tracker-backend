package name.expenses.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.sub_category.SubCategoryReqDto;
import name.expenses.dto.sub_category.SubCategoryUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.service.SubCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sub-categories")
@RequiredArgsConstructor
@Slf4j
public class SubCategoryController {

    private final SubCategoryService subCategoryService;

    @PostMapping
    public ResponseEntity<ResponseDto> createSubCategory(@Valid @RequestBody SubCategoryReqDto subCategoryReqDto) {
        log.info("Creating sub-category with name: {}", subCategoryReqDto.getName());
        ResponseDto response = subCategoryService.create(subCategoryReqDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refNo/{refNo}")
    public ResponseEntity<ResponseDto> getSubCategory(@PathVariable String refNo) {
        log.info("Fetching sub-category with refNo: {}", refNo);
        ResponseDto response = subCategoryService.get(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseDto> getSubCategoryByName(@PathVariable String name) {
        log.info("Fetching sub-category by name: {}", name);
        ResponseDto response = subCategoryService.getSubCategoryByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{refNo}/expenses")
    public ResponseEntity<ResponseDto> getSubCategoryExpenses(@PathVariable String refNo) {
        log.info("Fetching expenses for sub-category: {}", refNo);
        ResponseDto response = subCategoryService.getSubCategoryExpenses(refNo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{refNo}")
    public ResponseEntity<ResponseDto> updateSubCategory(@PathVariable String refNo,
                                                         @Valid @RequestBody SubCategoryUpdateDto subCategoryUpdateDto) {
        log.info("Updating sub-category with refNo: {}", refNo);
        ResponseDto response = subCategoryService.update(refNo, subCategoryUpdateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{refNo}")
    public ResponseEntity<ResponseDto> deleteSubCategory(@PathVariable String refNo) {
        log.info("Deleting sub-category with refNo: {}", refNo);
        ResponseDto response = subCategoryService.delete(refNo);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getAllSubCategories(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all sub-categories - page: {}, perPage: {}", page, perPage);
        ResponseDto response = subCategoryService.getAllEntities(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/noCategory")
    public ResponseEntity<ResponseDto> getAllWithoutCategory(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(name = "per_page", defaultValue = "10") Long perPage,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Fetching all sub-categories without category");
        ResponseDto response = subCategoryService.getAllEntitiesWithoutCategory(page, perPage, sortBy, SortDirection.fromString(sortDirection));
        return ResponseEntity.ok(response);
    }
}
