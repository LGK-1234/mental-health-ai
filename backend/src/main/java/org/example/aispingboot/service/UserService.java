package org.example.aispingboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.aispingboot.DTO.command.UserLoginCommandDTO;
import org.example.aispingboot.DTO.command.UserRegisterCommandDTO;
import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.enumClass.UserType;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.mapper.UserMapper;
import org.example.aispingboot.service.convert.UserConvert;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserLoginResponseDTO login(UserLoginCommandDTO commandDTO) {
        // 用户名或邮箱均可登录
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.eq(User::getUsername, commandDTO.getUsername())
                .or()
                .eq(User::getEmail, commandDTO.getUsername()));

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            // 统一提示，避免被用来枚举账号
            throw new BusinessException("用户名/邮箱或密码错误");
        }

        String inputPassword = commandDTO.getPassword().trim();
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BusinessException("用户名/邮箱或密码错误");
        }

        if (!user.isActive()) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);
        return UserConvert.entityToLoginResponse(token, userInfo);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO commandDTO) {
        if (!commandDTO.getPassword().equals(commandDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }

        LambdaQueryWrapper<User> userNameQuery = new LambdaQueryWrapper<>();
        userNameQuery.eq(User::getUsername, commandDTO.getUsername());
        if (userMapper.selectCount(userNameQuery) > 0) {
            throw new BusinessException("用户名已存在");
        }

        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, commandDTO.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException("邮箱已存在");
        }

        if (!UserType.isValidCode(commandDTO.getUserType())) {
            throw new BusinessException("无效的用户类型");
        }

        String encodedPassword = passwordEncoder.encode(commandDTO.getPassword().trim());
        User user = UserConvert.registerCommandToEntity(commandDTO, encodedPassword);
        userMapper.insert(user);
        return UserConvert.entityToDetailResponse(user);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserConvert.entityToDetailResponse(user);
    }
}
