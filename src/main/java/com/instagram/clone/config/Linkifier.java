package com.instagram.clone.config;

import org.springframework.stereotype.Component;


@Component("linkifier")
public class Linkifier {

    private static final java.util.regex.Pattern URL_PATTERN =
            java.util.regex.Pattern.compile("(https?://[^\\s<>\"]+)");

    public String linkify(String text) {
        if (text == null || text.isBlank()) return "";

        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");

        return URL_PATTERN.matcher(escaped).replaceAll(
                "<a href=\"$1\" target=\"_blank\" rel=\"noopener noreferrer\" class=\"chat-link\">$1</a>"
        );
    }
}