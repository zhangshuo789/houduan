package com.volleyball.volleyballcommunitybackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volleyball.volleyballcommunitybackend.config.AiProperties;
import com.volleyball.volleyballcommunitybackend.dto.request.AiMessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.AiConversationResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.AiMessageResponse;
import com.volleyball.volleyballcommunitybackend.entity.AiConversation;
import com.volleyball.volleyballcommunitybackend.entity.AiMessage;
import com.volleyball.volleyballcommunitybackend.repository.AiConversationRepository;
import com.volleyball.volleyballcommunitybackend.repository.AiMessageRepository;
import com.volleyball.volleyballcommunitybackend.util.MarkdownParser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AiService {

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final MarkdownParser markdownParser;

    public AiService(AiConversationRepository conversationRepository,
                     AiMessageRepository messageRepository,
                     AiProperties aiProperties,
                     MarkdownParser markdownParser) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.aiProperties = aiProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.markdownParser = markdownParser;
    }

    @Transactional
    public AiConversationResponse createConversation(Long userId) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle("新对话");
        AiConversation saved = conversationRepository.save(conversation);
        return toConversationResponse(saved);
    }

    public List<AiConversationResponse> getConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId, null)
                .stream()
                .map(this::toConversationResponse)
                .toList();
    }

    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        AiConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("会话不存在或无权删除"));
        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.delete(conversation);
    }

    public List<AiMessageResponse> getMessages(Long conversationId, Long userId) {
        if (!conversationRepository.existsByIdAndUserId(conversationId, userId)) {
            throw new RuntimeException("会话不存在或无权访问");
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    public AiMessageResponse sendMessage(Long conversationId, Long userId, AiMessageRequest request) {
        AiConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("会话不存在或无权访问"));

        // 保存用户消息
        AiMessage userMessage = new AiMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("user");
        userMessage.setContent(request.getContent());
        messageRepository.save(userMessage);

        // 构建历史上下文
        List<Map<String, String>> messages = buildMessagesHistory(conversationId);

        // 调用 DeepSeek API
        String thinking = null;
        String assistantContent = callDeepSeekApi(messages, request.getThinking(), request.getStream());

        // 非流式返回完整内容，解析 thinking
        if (request.getThinking() != null && request.getThinking()) {
            try {
                JsonNode root = objectMapper.readTree(assistantContent);
                thinking = root.has("thinking") ? root.get("thinking").asText() : null;
                assistantContent = root.has("content") ? root.get("content").asText() : assistantContent;
            } catch (Exception e) {
                // 如果解析失败，content 就是完整内容
            }
        }

        // 保存助手消息
        AiMessage assistantMessage = new AiMessage();
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(markdownParser.toPlainText(assistantContent));
        assistantMessage.setThinking(thinking);
        AiMessage saved = messageRepository.save(assistantMessage);

        // 更新会话标题（如果第一条是用户消息，用它的前20字符作为标题）
        if (conversation.getTitle().equals("新对话")) {
            String title = request.getContent().length() > 20
                    ? request.getContent().substring(0, 20) + "..."
                    : request.getContent();
            conversation.setTitle(title);
            conversationRepository.save(conversation);
        } else {
            conversationRepository.save(conversation);
        }

        return toMessageResponse(saved);
    }

    public SseEmitter sendMessageStream(Long conversationId, Long userId, AiMessageRequest request) {
        AiConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("会话不存在或无权访问"));

        // 保存用户消息
        AiMessage userMessage = new AiMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("user");
        userMessage.setContent(request.getContent());
        messageRepository.save(userMessage);

        SseEmitter emitter = new SseEmitter(300000L);

        // 构建历史上下文
        List<Map<String, String>> messages = buildMessagesHistory(conversationId);

        // 异步调用流式 API
        executor.submit(() -> {
            try {
                String assistantContent = callDeepSeekStreamApi(messages, request.getThinking(), emitter);

                // 流式结束后，保存助手消息到数据库
                AiMessage assistantMessage = new AiMessage();
                assistantMessage.setConversationId(conversationId);
                assistantMessage.setRole("assistant");
                assistantMessage.setContent(markdownParser.toPlainText(assistantContent));
                messageRepository.save(assistantMessage);

                // 更新会话标题
                AiConversation conv = conversationRepository.findById(conversationId).orElse(null);
                if (conv != null && conv.getTitle().equals("新对话")) {
                    conv.setTitle(request.getContent().length() > 20
                            ? request.getContent().substring(0, 20) + "..."
                            : request.getContent());
                    conversationRepository.save(conv);
                }

                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private List<Map<String, String>> buildMessagesHistory(Long conversationId) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are a helpful assistant."));

        List<AiMessage> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        for (AiMessage msg : history) {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("role", msg.getRole());
            messageMap.put("content", msg.getContent());
            messages.add(messageMap);
        }
        return messages;
    }

    private String callDeepSeekApi(List<Map<String, String>> messages, Boolean thinking, Boolean stream) {
        String url = aiProperties.getBaseUrl() + "/chat/completions";

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", messages);
        body.put("stream", stream != null ? stream : false);

        if (thinking != null && thinking) {
            Map<String, Object> thinkingConfig = new HashMap<>();
            thinkingConfig.put("type", "enabled");
            body.put("thinking", thinkingConfig);
            body.put("reasoning_effort", "high");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiProperties.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (thinking != null && thinking) {
                    // 返回完整 JSON（含 thinking 和 content）
                    return message.toString();
                } else {
                    return message.get("content").asText();
                }
            }
            throw new RuntimeException("API 返回格式异常");
        } catch (Exception e) {
            throw new RuntimeException("解析 AI 响应失败: " + e.getMessage());
        }
    }

    private String callDeepSeekStreamApi(List<Map<String, String>> messages, Boolean thinking, SseEmitter emitter) {
        String urlStr = aiProperties.getBaseUrl() + "/chat/completions";
        StringBuilder fullContent = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + aiProperties.getApiKey());
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "text/event-stream");

            Map<String, Object> body = new HashMap<>();
            body.put("model", aiProperties.getModel());
            body.put("messages", messages);
            body.put("stream", true);

            if (thinking != null && thinking) {
                Map<String, Object> thinkingConfig = new HashMap<>();
                thinkingConfig.put("type", "enabled");
                body.put("thinking", thinkingConfig);
                body.put("reasoning_effort", "high");
            }

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(body);

            try (java.io.OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                emitter.send(SseEmitter.event().name("error").data("API请求失败: " + responseCode));
                emitter.complete();
                return "";
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if ("[DONE]".equals(line)) {
                        break;
                    }
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        try {
                            JsonNode node = mapper.readTree(data);
                            JsonNode delta = node.path("choices").path(0).path("delta");
                            String chunkContent = delta.path("content").asText("");
                            if (!chunkContent.isEmpty()) {
                                fullContent.append(chunkContent);
                                emitter.send(SseEmitter.event().name("message").data(chunkContent));
                            }
                        } catch (Exception e) {
                            // 忽略解析错误
                        }
                    }
                }
            }

            return fullContent.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AiConversationResponse toConversationResponse(AiConversation conversation) {
        return new AiConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private AiMessageResponse toMessageResponse(AiMessage message) {
        return new AiMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getThinking(),
                message.getCreatedAt()
        );
    }
}