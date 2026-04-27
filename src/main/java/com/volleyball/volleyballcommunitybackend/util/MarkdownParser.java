package com.volleyball.volleyballcommunitybackend.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Markdown 转纯文本工具
 */
@Component
public class MarkdownParser {

    /**
     * 将 Markdown 转换为纯文本
     */
    public String toPlainText(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return markdown;
        }

        String text = markdown;

        // 1. 移除代码块（保持内容）
        text = removeCodeBlocks(text);

        // 2. 移除行内代码标记
        text = text.replaceAll("`([^`]+)`", "$1");

        // 3. 移除标题标记
        text = Pattern.compile("^#{1,6}\\s+", Pattern.MULTILINE).matcher(text).replaceAll("");

        // 4. 移除加粗和斜体标记
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        text = text.replaceAll("(?<!\\*)\\*([^*]+)\\*(?!\\*)", "$1");
        text = text.replaceAll("__([^_]+)__", "$1");
        text = text.replaceAll("(?<!_)_(?!_) ([^_]+)_(?!_)", "$1");

        // 5. 移除链接，保留文本
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");

        // 6. 移除图片
        text = text.replaceAll("!\\[([^\\]]*)\\]\\([^)]+\\)", "");

        // 7. 移除引用标记
        text = Pattern.compile("^>+\\s*", Pattern.MULTILINE).matcher(text).replaceAll("");

        // 8. 移除列表标记
        text = Pattern.compile("^[-*+]\\s+", Pattern.MULTILINE).matcher(text).replaceAll("");
        text = Pattern.compile("^\\d+\\.\\s+", Pattern.MULTILINE).matcher(text).replaceAll("");

        // 9. 移除分割线
        text = Pattern.compile("^[-*_]{3,}$", Pattern.MULTILINE).matcher(text).replaceAll("");

        // 10. 移除 HTML 标签
        text = text.replaceAll("<[^>]+>", "");

        // 11. 清理多余空行
        text = text.replaceAll("\n{3,}", "\n\n");

        return text.trim();
    }

    /**
     * 移除代码块
     */
    private String removeCodeBlocks(String text) {
        // 移除 ```code``` 格式
        Pattern codeBlockPattern = Pattern.compile("```[\\s\\S]*?```");
        text = codeBlockPattern.matcher(text).replaceAll(mr -> {
            String content = mr.group();
            // 提取代码块内容（去掉```标记）
            content = content.replaceAll("^```\\w*\\n?", "");
            content = content.replaceAll("\\n?```$", "");
            return "\n" + content.trim() + "\n";
        });
        return text;
    }
}