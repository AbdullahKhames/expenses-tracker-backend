package name.expenses.utils;

import name.expenses.globals.Page;
import name.expenses.globals.responses.ResponseDto;
import java.util.List;

public final class ResponseDtoBuilder {

    private ResponseDtoBuilder() {}

    public static ResponseDto getCreateResponse(String resourceName, String refNo, Object data) {
        return createResponse(
                String.format("%s resource was created successfully with reference number : %s", resourceName, refNo),
                true, 801, data);
    }

    public static ResponseDto getErrorResponse(int code, Object data) {
        return createResponse(
                "There was an Error Processing your request\nplease Try again later!",
                false, code, data);
    }

    public static ResponseDto getUpdateResponse(String resourceName, String refNo, Object data) {
        return createResponse(
                String.format("%s resource with reference number : %s was updated successfully ", resourceName, refNo),
                true, 802, data);
    }

    public static ResponseDto getDeleteResponse(String resourceName, String refNo) {
        return createResponse(
                String.format("%s resource with reference number : %s was deleted successfully ", resourceName, refNo),
                true, 805, null);
    }

    public static ResponseDto getFetchResponse(String resourceName, String refNo, Object data) {
        return createResponse(
                String.format("%s resource with reference number : %s was fetched successfully ", resourceName, refNo),
                true, 800, data);
    }

    public static <T> ResponseDto getFetchAllResponse(String resourceName, Page<T> entityPage) {
        return createResponse(
                String.format("Page %s of %s resources was fetched successfully ", entityPage.getPageNumber(), resourceName),
                true, 800, entityPage);
    }

    public static ResponseDto getFetchAllResponse(String resourceName, List<?> entities) {
        return createResponse(
                String.format("list of %s resources was fetched successfully ", resourceName),
                true, 800, entities);
    }

    public static ResponseDto createResponse(String message, boolean status, int code, Object data) {
        return ResponseDto.builder()
                .message(message)
                .status(status)
                .code(code)
                .data(data)
                .build();
    }
}
