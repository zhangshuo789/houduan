package com.volleyball.volleyballcommunitybackend.util;

import com.volleyball.volleyballcommunitybackend.entity.SensitiveWord;
import com.volleyball.volleyballcommunitybackend.repository.SensitiveWordRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SensitiveWordFilter {
    private final SensitiveWordRepository sensitiveWordRepository;

    private volatile List<SensitiveWord> cachedWords;
    private long lastRefreshTime = 0;
    private static final long CACHE_EXPIRE_MS = 5 * 60 * 1000; // 5分钟

    public SensitiveWordFilter(SensitiveWordRepository sensitiveWordRepository) {
        this.sensitiveWordRepository = sensitiveWordRepository;
    }

    // 过滤敏感词，返回过滤后的文本
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<SensitiveWord> words = getWords();
        for (SensitiveWord word : words) {
            if (text.contains(word.getWord())) {
                text = text.replace(word.getWord(), word.getReplacement());
            }
        }
        return text;
    }

    // 获取敏感词列表（带缓存）
    private List<SensitiveWord> getWords() {
        // 如果缓存过期或为空，重新加载
        if (cachedWords == null || System.currentTimeMillis() - lastRefreshTime > CACHE_EXPIRE_MS) {
            cachedWords = sensitiveWordRepository.findAll();
            lastRefreshTime = System.currentTimeMillis();
        }
        return cachedWords;
    }

    // 清除缓存
    public void clearCache() {
        cachedWords = null;
        lastRefreshTime = 0;
    }
}
