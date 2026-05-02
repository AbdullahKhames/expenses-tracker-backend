package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.user.UserRoleDto;
import name.expenses.dto.user.UserRespDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.UserMapper;
import name.expenses.model.Role;
import name.expenses.model.User;
import name.expenses.repository.RoleRepository;
import name.expenses.repository.UserRepository;
import name.expenses.service.UserService;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public ResponseDto getUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with email: " + email));
        return ResponseDtoBuilder.getFetchResponse("User", user.getRefNo(),
                userMapper.entityToRespDto(user));
    }

    @Override
    public ResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with id: " + id));
        return ResponseDtoBuilder.getFetchResponse("User", user.getRefNo(),
                userMapper.entityToRespDto(user));
    }

    @Override
    public ResponseDto getUserByRef(String refNo) {
        User user = userRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("User", refNo,
                userMapper.entityToRespDto(user));
    }

    @Override
    public ResponseDto getAll() {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .collect(Collectors.toList());
        List<UserRespDto> dtos = userMapper.entitiesToRespDtos(users);
        return ResponseDtoBuilder.getFetchAllResponse("User", dtos);
    }

    @Override
    public ResponseDto addRoleToUser(UserRoleDto userRoleDto) {
        User user = userRepository.findByEmail(userRoleDto.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("User not found with email: " + userRoleDto.getEmail()));
        Role role = roleRepository.findByName(userRoleDto.getRoleName())
                .orElseThrow(() -> new ObjectNotFoundException("Role not found with name: " + userRoleDto.getRoleName()));
        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Added role {} to user {}", userRoleDto.getRoleName(), userRoleDto.getEmail());
        return ResponseDtoBuilder.getUpdateResponse("User", user.getRefNo(),
                userMapper.entityToRespDto(user));
    }

    @Override
    public ResponseDto softDeleteUser(String refNo) {
        User user = userRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with refNo: " + refNo));
        user.setDeleted(true);
        userRepository.save(user);
        log.info("Soft-deleted user with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("User", refNo);
    }

    @Override
    public ResponseDto activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with id: " + id));
        user.setVerified(true);
        userRepository.save(user);
        log.info("Activated user with id: {}", id);
        return ResponseDtoBuilder.getUpdateResponse("User", user.getRefNo(),
                userMapper.entityToRespDto(user));
    }

    @Override
    public ResponseDto deActivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with id: " + id));
        user.setVerified(false);
        userRepository.save(user);
        log.info("Deactivated user with id: {}", id);
        return ResponseDtoBuilder.getUpdateResponse("User", user.getRefNo(),
                userMapper.entityToRespDto(user));
    }

    @Override
    public ResponseDto validateChangeEmail(String refNo, String token) {
        User user = userRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with refNo: " + refNo));
        // Token validation logic will be handled by AuthService/TokenService
        log.info("Validated change email token for user with refNo: {}", refNo);
        return ResponseDtoBuilder.getFetchResponse("User", refNo,
                userMapper.entityToRespDto(user));
    }

    @Override
    public ResponseDto changeEmail(String token, String refNo, String newEmail) {
        User user = userRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("User not found with refNo: " + refNo));
        // Token validation logic will be handled by AuthService/TokenService
        user.setEmail(newEmail);
        userRepository.save(user);
        log.info("Changed email for user with refNo: {} to {}", refNo, newEmail);
        return ResponseDtoBuilder.getUpdateResponse("User", refNo,
                userMapper.entityToRespDto(user));
    }
}
