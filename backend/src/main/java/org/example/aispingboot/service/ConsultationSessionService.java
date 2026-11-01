package org.example.aispingboot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.example.aispingboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationSessionService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        ConsultationSession session = ConsultationSession.builder()
                .userId(userId)
                .sessionTitle(createDTO.getSessionTitle())
                .startedAt(LocalDateTime.now())
                .build();

        if (StrUtil.isBlank(createDTO.getSessionTitle())) {
            session.setSessionTitle("宁渡AI助手 - " + DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm"));
        }

        consultationSessionMapper.insert(session);
        return session;
    }

    public ConsultationSession getById(Long sessionId) {
        return sessionId == null ? null : consultationSessionMapper.selectById(sessionId);
    }
}
