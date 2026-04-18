package com.volleyball.volleyballcommunitybackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    public void sendMessageToUser(Long userId, String eventType, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventType)
                            .data(data));
                } catch (IOException e) {
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    public void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public void sendEventUpdate(Long userId, Object data) {
        sendMessageToUser(userId, "eventUpdate", data);
    }

    public void sendEventStatusChanged(Long userId, Object data) {
        sendMessageToUser(userId, "eventStatusChanged", data);
    }

    public void sendNewRegistration(Long organizerId, Object data) {
        sendMessageToUser(organizerId, "newRegistration", data);
    }

    public void sendRegistrationResult(Long userId, Object data) {
        sendMessageToUser(userId, "registrationResult", data);
    }
}
