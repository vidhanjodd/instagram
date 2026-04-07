package com.instagram.clone.config;

import org.springframework.stereotype.Component;

/**
 * Thymeleaf helper bean — converts plain text with URLs into HTML with clickable links.
 * Used in chat.html as:  th:utext="${@linkifier.linkify(msg.content)}"
 */
@Component("linkifier")
public class Linkifier {

    private static final java.util.regex.Pattern URL_PATTERN =
            java.util.regex.Pattern.compile("(https?://[^\\s<>\"]+)");

    /**
     * Escapes HTML, then wraps any http/https URLs in <a> tags.
     */
    public String linkify(String text) {
        if (text == null || text.isBlank()) return "";

        // 1. Escape HTML entities first to prevent XSS
        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");

        // 2. Replace URLs with clickable anchor tags
        return URL_PATTERN.matcher(escaped).replaceAll(
                "<a href=\"$1\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"chat-link\">$1</a>"
        );
    }
}