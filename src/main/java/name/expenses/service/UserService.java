package name.expenses.service;

import name.expenses.dto.user.UserRoleDto;
import name.expenses.globals.responses.ResponseDto;

public interface UserService {

    ResponseDto getUser(String email);

    ResponseDto getUserById(Long id);

    ResponseDto getUserByRef(String refNo);

    ResponseDto getAll();

    ResponseDto addRoleToUser(UserRoleDto userRoleDto);

    ResponseDto softDeleteUser(String refNo);

    ResponseDto activateUser(Long id);

    ResponseDto deActivateUser(Long id);

    ResponseDto validateChangeEmail(String refNo, String token);

    ResponseDto changeEmail(String token, String refNo, String newEmail);
}
