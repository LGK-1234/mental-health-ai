package org.example.aispingboot.controller;

import cn.hutool.json.JSONUtil;
import jakarta.validation.Valid;
import org.example.aispingboot.AiService.PsychologicalSupportService;
import org.example.aispingboot.AiService.StructOutPut;
import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.DTO.command.ConsultationStreamDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChat {

    @Autowired
    private PsychologicalSupportService psychologicalSupportService;

    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(
            @Valid @RequestBody ConsultationSessionCreateDTO createDTO) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), null);
        }
        return Result.ok(psychologicalSupportService.startSession(userId, createDTO));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO streamDTO) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            return Flux.just(errorEvent(ResultCode.UNAUTHORIZED.getCode(), "用户未登录"));
        }

        return psychologicalSupportService
                .streamPsychologicalChat(streamDTO.getSessionId(), streamDTO.getUserMessage(), userId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .event("message")
                        .data(JSONUtil.toJsonStr(Result.ok(Map.of("content", chunk, "type", "normal"))))
                        .build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()))
                // 出错时也通过 SSE 返回，前端能显示具体原因（如余额不足）
                .onErrorResume(e -> Flux.just(errorEvent(ResultCode.SYSTEM_ERROR.getCode(), e.getMessage())));
    }

    private ServerSentEvent<String> errorEvent(String code, String msg) {
        return ServerSentEvent.<String>builder()
                .event("error")
                .data(JSONUtil.toJsonStr(Result.error(code, msg, null)))
                .build();
    }
}
