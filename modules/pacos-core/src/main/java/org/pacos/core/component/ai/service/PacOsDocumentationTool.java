package org.pacos.core.component.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pacos.base.ai.AiToolbox;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PacOsDocumentationTool implements AiToolbox {

    private final RestClient restClient;
    private static final String BASE_URL = "https://raw.githubusercontent.com/pacos-dev/documentation/main/docs/user/plugins/";

    public PacOsDocumentationTool(RestClient restClient) {
        this.restClient = restClient;
    }

    @Tool(description = "Fetch PacOS technical documentation. Always check this if you not sure what to do."
            + " Never repeat this with the same topic")
    public DocumentationResponse getPacOsDocumentation(
            @ToolParam(description = "The topic or document ID, e.g., 'plugins, 'settings'") String topic) {
        System.out.println("Checking page: " + topic);
        // Docusaurus mapping: internal links often use lowercase and hyphens
        String sanitizedTopic = topic.toLowerCase().replace(" ", "-").replace("_", "-");
        // GitHub-style or Docusaurus raw access often requires .md suffix for raw content
        // but here we assume a standard web fetch of the content.
        String targetUrl = BASE_URL + sanitizedTopic + ".md";

        try {
            String rawMarkdown = restClient.get()
                    .uri(targetUrl + ".md")
                    .retrieve()
                    .body(String.class);

            DocumentationResponse documentationResponse = processMarkdown(rawMarkdown, targetUrl);
            if (documentationResponse.content().contains("Page Not Found")) {
                return getPacOsDocumentation("");
            }
            return documentationResponse;
        } catch (Exception e) {
            throw new RuntimeException("DOCS_NOT_FOUND: Documentation for '" + topic + "' not found at " + targetUrl +
                    ". Try listing 'introduction' to find the right path.");
        }
    }

    private DocumentationResponse processMarkdown(String raw, String url) {
        // 1. Usuwamy FrontMatter (pomiędzy ---)
        String content = raw.replaceAll("(?s)^---.*?---", "").trim();

        // 2. Regex do wyciągania linków typu [Tekst](./link) lub [Tekst](link.md)
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[.*?\\]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String link = matcher.group(1);
            if (!link.startsWith("http")) { // Tylko linki wewnętrzne
                links.add(link.replace(".md", "").replace("./", ""));
            }
        }

        return new DocumentationResponse(
                content,
                links.stream().distinct().limit(10).toList(),
                url
        );
    }
}