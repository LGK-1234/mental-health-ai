package org.example.aispingboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.entity.EmotionDiary;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.mapper.EmotionDiaryMapper;
import org.example.aispingboot.mapper.UserMapper;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 情绪日记：用户记录 + 管理端分页查看/删除。
 */
@RestController
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {

    @Autowired
    private EmotionDiaryMapper diaryMapper;

    @Autowired
    private UserMapper userMapper;

    /** 新增一条情绪日记 */
    @PostMapping
    public Result<EmotionDiary> create(@RequestBody EmotionDiary diary) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), null);
        }
        if (diary.getMoodScore() == null) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "心情分数不能为空", null);
        }
        diary.setId(null);
        diary.setUserId(userId);
        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(LocalDate.now());
        }
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());
        diaryMapper.insert(diary);
        return Result.ok(diary);
    }

    /** 我的日记 */
    @GetMapping("/my")
    public Result<Page<EmotionDiary>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), null);
        }
        Page<EmotionDiary> page = new Page<>(pageNum, pageSize);
        diaryMapper.selectPage(page, new LambdaQueryWrapper<EmotionDiary>()
                .eq(EmotionDiary::getUserId, userId)
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId));
        return Result.ok(page);
    }

    /** 管理端分页查询全部日记（带用户名） */
    @GetMapping("/admin/page")
    public Result<Map<String, Object>> adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<EmotionDiary> page = new Page<>(pageNum, pageSize);
        diaryMapper.selectPage(page, new LambdaQueryWrapper<EmotionDiary>()
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId));

        page.getRecords().forEach(d -> {
            User u = userMapper.selectById(d.getUserId());
            d.setNickname(u == null ? null : u.getDisplayName());
        });

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        return Result.ok(data);
    }

    @DeleteMapping("/admin/{id}")
    public Result<String> delete(@PathVariable Long id) {
        diaryMapper.deleteById(id);
        return Result.ok("已删除");
    }
}
