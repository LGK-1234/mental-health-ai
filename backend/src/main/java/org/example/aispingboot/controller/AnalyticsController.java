package org.example.aispingboot.controller;

import org.example.aispingboot.common.Result;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.entity.EmotionDiary;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.example.aispingboot.mapper.EmotionDiaryMapper;
import org.example.aispingboot.mapper.KnowledgeArticleMapper;
import org.example.aispingboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端数据看板。
 * 返回结构与前端 dashboard.vue 对齐：
 * systemOverview / emotionTrend / consultationStats.dailyTrend / userActivity
 */
@RestController
@RequestMapping("/api/data-analytics")
public class AnalyticsController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM-dd");

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsultationSessionMapper sessionMapper;
    @Autowired
    private EmotionDiaryMapper diaryMapper;
    @Autowired
    private KnowledgeArticleMapper articleMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        List<User> users = userMapper.selectList(null);
        List<EmotionDiary> diaries = diaryMapper.selectList(null);
        List<ConsultationSession> sessions = sessionMapper.selectList(null);

        LocalDate today = LocalDate.now();

        // ---------- 概览 ----------
        Map<String, Object> systemOverview = new HashMap<>();
        systemOverview.put("totalUsers", users.size());
        systemOverview.put("activeUsers", users.stream()
                .filter(u -> Integer.valueOf(1).equals(u.getStatus())).count());
        systemOverview.put("totalDiaries", diaries.size());
        systemOverview.put("todayNewDiaries", diaries.stream()
                .filter(d -> today.equals(d.getDiaryDate())).count());
        systemOverview.put("totalSessions", sessions.size());
        systemOverview.put("todayNewSessions", sessions.stream()
                .filter(s -> s.getStartedAt() != null && today.equals(s.getStartedAt().toLocalDate())).count());
        systemOverview.put("totalArticles", articleMapper.selectCount(null));
        systemOverview.put("avgMoodScore", diaries.isEmpty() ? 0 :
                Math.round(diaries.stream().filter(d -> d.getMoodScore() != null)
                        .mapToInt(EmotionDiary::getMoodScore).average().orElse(0) * 10) / 10.0);

        // ---------- 近 7 天 ----------
        List<LocalDate> days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            days.add(today.minusDays(i));
        }

        List<Map<String, Object>> emotionTrend = new ArrayList<>();
        List<Map<String, Object>> consultationTrend = new ArrayList<>();
        List<Map<String, Object>> userActivity = new ArrayList<>();

        for (LocalDate day : days) {
            String label = day.format(DAY);

            // 情绪：当天日记的平均分与条数
            List<EmotionDiary> dayDiaries = diaries.stream()
                    .filter(d -> day.equals(d.getDiaryDate())).collect(Collectors.toList());
            double avg = dayDiaries.stream().filter(d -> d.getMoodScore() != null)
                    .mapToInt(EmotionDiary::getMoodScore).average().orElse(0);
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("date", label);
            e.put("avgMoodScore", Math.round(avg * 10) / 10.0);
            e.put("recordCount", dayDiaries.size());
            emotionTrend.add(e);

            // 咨询：当天会话数与参与用户数
            List<ConsultationSession> daySessions = sessions.stream()
                    .filter(s -> s.getStartedAt() != null && day.equals(s.getStartedAt().toLocalDate()))
                    .collect(Collectors.toList());
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("date", label);
            c.put("sessionCount", daySessions.size());
            c.put("userCount", daySessions.stream().map(ConsultationSession::getUserId).distinct().count());
            consultationTrend.add(c);

            // 用户活跃：新增用户、记日记的人、咨询的人
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("date", label);
            a.put("newUsers", users.stream()
                    .filter(u -> u.getCreatedAt() != null && day.equals(u.getCreatedAt().toLocalDate())).count());
            a.put("activeUsers", users.stream()
                    .filter(u -> u.getCreatedAt() != null && day.equals(u.getCreatedAt().toLocalDate())).count());
            a.put("diaryUsers", dayDiaries.stream().map(EmotionDiary::getUserId).distinct().count());
            a.put("consultationUsers", daySessions.stream().map(ConsultationSession::getUserId).distinct().count());
            userActivity.add(a);
        }

        Map<String, Object> consultationStats = new HashMap<>();
        consultationStats.put("dailyTrend", consultationTrend);

        Map<String, Object> data = new HashMap<>();
        data.put("systemOverview", systemOverview);
        data.put("emotionTrend", emotionTrend);
        data.put("consultationStats", consultationStats);
        data.put("userActivity", userActivity);
        return Result.ok(data);
    }
}
