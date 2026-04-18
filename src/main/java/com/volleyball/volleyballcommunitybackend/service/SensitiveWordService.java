package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.SensitiveWordRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.SensitiveWordResponse;
import com.volleyball.volleyballcommunitybackend.entity.SensitiveWord;
import com.volleyball.volleyballcommunitybackend.repository.SensitiveWordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensitiveWordService {
    private final SensitiveWordRepository sensitiveWordRepository;

    public SensitiveWordService(SensitiveWordRepository sensitiveWordRepository) {
        this.sensitiveWordRepository = sensitiveWordRepository;
    }

    // 敏感词列表
    public List<SensitiveWordResponse> getAll() {
        return sensitiveWordRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 添加敏感词
    public SensitiveWordResponse add(SensitiveWordRequest request) {
        if (sensitiveWordRepository.existsByWord(request.getWord())) {
            throw new RuntimeException("敏感词已存在");
        }
        SensitiveWord entity = new SensitiveWord();
        entity.setWord(request.getWord());
        entity.setReplacement(request.getReplacement() != null ? request.getReplacement() : "***");
        entity.setLevel(request.getLevel() != null ? request.getLevel() : "WARN");
        SensitiveWord saved = sensitiveWordRepository.save(entity);
        return toResponse(saved);
    }

    // 更新敏感词
    public SensitiveWordResponse update(Long id, SensitiveWordRequest request) {
        SensitiveWord entity = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("敏感词不存在"));
        entity.setWord(request.getWord());
        if (request.getReplacement() != null) {
            entity.setReplacement(request.getReplacement());
        }
        if (request.getLevel() != null) {
            entity.setLevel(request.getLevel());
        }
        SensitiveWord saved = sensitiveWordRepository.save(entity);
        return toResponse(saved);
    }

    // 删除敏感词
    public void delete(Long id) {
        if (!sensitiveWordRepository.existsById(id)) {
            throw new RuntimeException("敏感词不存在");
        }
        sensitiveWordRepository.deleteById(id);
    }

    private SensitiveWordResponse toResponse(SensitiveWord entity) {
        return new SensitiveWordResponse(
                entity.getId(),
                entity.getWord(),
                entity.getReplacement(),
                entity.getLevel(),
                entity.getCreatedAt()
        );
    }
}
