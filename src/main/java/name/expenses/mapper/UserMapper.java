package name.expenses.mapper;

import name.expenses.dto.user.UserReqDto;
import name.expenses.dto.user.UserRespDto;
import name.expenses.dto.user.UserUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.Role;
import name.expenses.model.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "loggedIn", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User reqDtoToEntity(UserReqDto dto);

    @Mapping(target = "roles", expression = "java(rolesToRoleNames(entity.getRoles()))")
    UserRespDto entityToRespDto(User entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "loggedIn", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "deviceId", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateEntityFromDto(UserUpdateDto dto, @MappingTarget User entity);

    List<UserRespDto> entitiesToRespDtos(List<User> entities);
    Set<UserRespDto> entitiesToRespDtos(Set<User> entities);

    default Set<String> rolesToRoleNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    default Page<UserRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<User> entityPage) {
        return Page.<UserRespDto>builder()
                .content(entitiesToRespDtos(entityPage.getContent()))
                .pageNumber((long) entityPage.getNumber() + 1)
                .pageSize((long) entityPage.getSize())
                .totalElements(entityPage.getTotalElements())
                .totalPages((long) entityPage.getTotalPages())
                .hasNext(entityPage.hasNext())
                .hasPrevious(entityPage.hasPrevious())
                .build();
    }
}
