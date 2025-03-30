package com.zed.user_service.service.impl;


import com.zed.user_service.dto.CreateUserDTO;
import com.zed.user_service.dto.UpdateUserDTO;
import com.zed.user_service.dto.UserDTO;
import com.zed.user_service.entity.AppUserEntity;
import com.zed.user_service.exception.NotFoundException;
import com.zed.user_service.mapper.UserMapper;
import com.zed.user_service.repository.UserRepository;
import com.zed.user_service.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        AppUserEntity appUserEntity = userMapper.toUserEntity(createUserDTO);
        AppUserEntity savedEntity = userRepository.save(appUserEntity);
        UserDTO result = userMapper.toUserDTO(savedEntity);
        log.info("UserService: createUser method result: {}", result);
        return result;
    }

    @Override
    @Transactional
    public UserDTO getUserById(Long id) {
        AppUserEntity userEntity = findUserById(id);
        UserDTO result = userMapper.toUserDTO(userEntity);
        log.info("UserService: getUserById method result: {}", result);
        return result;
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserDTO updateUserDTO) {
        AppUserEntity appUserEntity = findUserById(id);
        userMapper.updateEntityFromDTO(updateUserDTO,appUserEntity);
        AppUserEntity updatedEntity = userRepository.save(appUserEntity);
        UserDTO result = userMapper.toUserDTO(updatedEntity);
        log.info("UserService: updateUser method result: {}",result);
        return result;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        AppUserEntity appUserEntity = findUserById(id);
        userRepository.delete(appUserEntity);
        log.info("UserService: User with id={} deleted successfully", id);
    }

    @Override
    @Transactional
    public List<UserDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<AppUserEntity> userPage = userRepository.findAll(pageable);
        List<UserDTO> userDTOList = userMapper.toUserDTOList(userPage.getContent());
        log.info("Retrieved {} users for page {} with size {}", userDTOList.size(), page, size);
        return userDTOList;
    }

    private AppUserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new NotFoundException("User not found with id: " + id));
    }
}
