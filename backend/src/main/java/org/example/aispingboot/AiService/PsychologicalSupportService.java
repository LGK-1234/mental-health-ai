package org.example.aispingboot.AiService;

import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.service.ConsultationMessageService;
import org.example.aispingboot.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PsychologicalSupportService {

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        ConsultationSession session = consultationSessionService.createSession(userId, createDTO);
        if (session == null) {
            throw new BusinessException("创建会话失败");
        }

        // 首条消息在这里落库，后续 stream 时不再重复保存
        consultationMessageService.saveUserMessage(session.getId(), createDTO.getInitialMessage(), null);

        String sessionId = "session_" + session.getId();
        long now = System.currentTimeMillis();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),
                now,
                now + 86400000L,
                1,
                "ACTIVE"
        );
    }

    /**
     * 流式对话。
     * 数据库读写与权限校验都在返回 Flux 之前完成，避免在响应式回调中做阻塞操作。
     * 记忆由 MessageChatMemoryAdvisor 统一维护，这里不再手动写入 ChatMemory。
     */
    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage, Long userId) {
        Long dbSessionId;
        try {
            dbSessionId = extractSessionId(sessionId);
        } catch (NumberFormatException e) {
            return Flux.error(new BusinessException("会话 ID 格式错误"));
        }
        if (dbSessionId == null) {
            return Flux.error(new BusinessException("会话 ID 格式错误"));
        }

        ConsultationSession session = consultationSessionService.getById(dbSessionId);
        if (session == null) {
            return Flux.error(new BusinessException("会话不存在"));
        }
        // 越权校验：只能访问自己的会话
        if (userId == null || !userId.equals(session.getUserId())) {
            return Flux.error(new BusinessException("无权访问该会话"));
        }

        // 首条消息已在 startSession 保存，这里只保存后续新消息
        if (!isInitialMessageSaved(dbSessionId, userMessage)) {
            consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);
        }

        String conversationId = "conversation_" + sessionId;
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .system(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
                .user(userMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() ->
                        consultationMessageService.saveAiMessage(dbSessionId, fullResponse.toString(), "deepseek"))
                .onErrorMap(e -> new BusinessException(describeAiError(e)));
    }

    /** 把底层异常翻译成用户能看懂的话 */
    private String describeAiError(Throwable e) {
        String msg = e == null || e.getMessage() == null ? "" : e.getMessage();
        String lower = msg.toLowerCase();
        if (lower.contains("402") || lower.contains("insufficient") || lower.contains("balance")) {
            return "AI 服务账户余额不足，请到硅基流动控制台充值后重试";
        }
        if (lower.contains("401") || lower.contains("unauthorized") || lower.contains("invalid api key")) {
            return "AI 服务密钥无效，请检查环境变量 AI_API_KEY";
        }
        if (lower.contains("timeout") || lower.contains("timed out")) {
            return "AI 服务响应超时，请稍后重试";
        }
        return "AI 服务暂时不可用：" + msg;
    }

    /** 判断这条消息是否就是 startSession 已经存过的首条消息 */
    private boolean isInitialMessageSaved(Long sessionId, String userMessage) {
        if (userMessage == null || consultationMessageService.getMessageCountBySessionId(sessionId) != 1) {
            return false;
        }
        ConsultationMessageResponseDTO last = consultationMessageService.getLastMessageBySessionId(sessionId);
        return last != null
                && Integer.valueOf(1).equals(last.getSenderType())
                && userMessage.equals(last.getContent());
    }

    public Long extractSessionId(String sessionId) {
        if (sessionId != null && sessionId.startsWith("session_")) {
            return Long.parseLong(sessionId.substring("session_".length()));
        }
        return null;
    }
}
