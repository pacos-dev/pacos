package org.pacos.core.component.ai.service;

import java.util.List;

public record DocumentationResponse(
        String content,
        List<String> relatedTopics,
        String sourceUrl
) {}