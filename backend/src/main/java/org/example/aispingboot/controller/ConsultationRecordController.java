package org.example.aispingboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.entity.ConsultationMessage;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.mapper.ConsultationMessageMapper;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 咨询记录：会话列表、消息明细、删除会话、情绪结果。
 */
@RestController
@RequestMapping("/api/psychological-chat")
public class ConsultationRecordController {

    @Autowired
    private ConsultationSessionMapper sessionMapper;

    @Autowired
    private ConsultationMessageMapper messageMapper;

    @GetMapping("/sessions")
    public Result<Map<String, Object>> sessions(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), null);
        }

        Page<ConsultationSession> page = new Page<>(pageNum, pageSize);
        sessionMapper.selectPage(page, new LambdaQueryWrapper<ConsultationSession>()
                .eq(ConsultationSession::getUserId, userId)
                .orderByDesc(ConsultationSession::getStartedAt));

        List<Map<String, Object>> records = new ArrayList<>();
        for (ConsultationSession s : page.getRecords()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", s.getId());
            row.put("sessionTitle", s.getSessionTitle());
            row.put("startedAt", s.getStartedAt());

            ConsultationMessage last = messageMapper.selectOne(new LambdaQueryWrapper<ConsultationMessage>()
                    .eq(ConsultationMessage::getSessionId, s.getId())
                    .orderByDesc(ConsultationMessage::getId)
                    .last("limit 1"));
            row.put("lastMessageContent", last == null ? null : last.getContent());
            row.put("messageCount", messageMapper.selectCount(new LambdaQueryWrapper<ConsultationMessage>()
                    .eq(ConsultationMessage::getSessionId, s.getId())));
            records.add(row);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        return Result.ok(data);
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<List<ConsultationMessage>> messages(@PathVariable Long id) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        ConsultationSession session = sessionMapper.selectById(id);
        if (session == null || userId == null || !userId.equals(session.getUserId())) {
            return Result.error(ResultCode.ACCESS_UNAUTHORIZED.getCode(), "无权访问该会话", null);
        }
        return Result.ok(messageMapper.selectList(new LambdaQueryWrapper<ConsultationMessage>()
                .eq(ConsultationMessage::getSessionId, id)
                .orderByAsc(ConsultationMessage::getId)));
    }

    @DeleteMapping("/sessions/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        ConsultationSession session = sessionMapper.selectById(id);
        if (session == null || userId == null || !userId.equals(session.getUserId())) {
            return Result.error(ResultCode.ACCESS_UNAUTHORIZED.getCode(), "无权删除该会话", null);
        }
        messageMapper.delete(new LambdaQueryWrapper<ConsultationMessage>()
                .eq(ConsultationMessage::getSessionId, id));
        sessionMapper.deleteById(id);
        return Result.ok("会话已删除");
    }

    @GetMapping("/session/{id}/emotion")
    public Result<Map<String, Object>> emotion(@PathVariable Long id) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        ConsultationSession session = sessionMapper.selectById(id);
        if (session == null || userId == null || !userId.equals(session.getUserId())) {
            return Result.error(ResultCode.ACCESS_UNAUTHORIZED.getCode(), "无权访问该会话", null);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("analysis", session.getLastEmotionAnalysis());
        data.put("updatedAt", session.getLastEmotionUpdatedAt());
        return Result.ok(data);
    }
}
