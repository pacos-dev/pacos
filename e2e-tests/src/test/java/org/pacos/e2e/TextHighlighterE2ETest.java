package org.pacos.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@EnabledIfSystemProperty(named = "pacos.e2e.browser", matches = "true")
class TextHighlighterE2ETest {

    @Test
    void whenQueryContainsRegexCharactersThenHighlightLiteralMatchesAndActiveResult() throws IOException {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            try {
                Page page = browser.newPage();
                page.setContent("<div id='target'></div>");
                page.addScriptTag(new Page.AddScriptTagOptions().setContent(loadSystemScript()));

                page.evaluate("() => {"
                        + " const target = document.querySelector('#target');"
                        + " target.textContent = 'Before [.*] after [.*] <img src=x>';"
                        + " window.pacos.textHighlighter.highlight(target, '[.*]', 1);"
                        + "}");

                assertEquals(2, page.locator("#target mark.search-highlight").count());
                assertEquals(1, page.locator("#target mark.search-highlight-active").count());
                assertEquals("[.*]", page.locator("#target mark.search-highlight-active").textContent());
                assertEquals(0, page.locator("#target img").count());
                assertEquals("Before [.*] after [.*] <img src=x>", page.locator("#target").textContent());
            } finally {
                browser.close();
            }
        }
    }

    @Test
    void whenTargetDomChangesThenHighlightNewLightDomAndShadowDomText() throws IOException {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            try {
                Page page = browser.newPage();
                page.setContent("<div id='target'>initial token</div>");
                page.addScriptTag(new Page.AddScriptTagOptions().setContent(loadSystemScript()));

                page.evaluate("() => {"
                        + " const target = document.querySelector('#target');"
                        + " window.pacos.textHighlighter.highlight(target, 'token');"
                        + " const light = document.createElement('span');"
                        + " light.textContent = ' later token';"
                        + " target.append(light);"
                        + " const host = document.createElement('div');"
                        + " host.id = 'shadow-host';"
                        + " const shadow = host.attachShadow({mode: 'open'});"
                        + " const shadowText = document.createElement('span');"
                        + " shadowText.textContent = ' shadow token';"
                        + " shadow.append(shadowText);"
                        + " target.append(host);"
                        + "}");

                page.locator("#target mark.search-highlight").nth(2).waitFor();
                assertEquals(3, page.locator("#target mark.search-highlight").count());

                page.evaluate("() => {"
                        + " const shadow = document.querySelector('#shadow-host').shadowRoot;"
                        + " shadow.querySelector('span').append(document.createTextNode(' token'));"
                        + "}");
                page.locator("#target mark.search-highlight").nth(3).waitFor();
                assertEquals(4, page.locator("#target mark.search-highlight").count());
            } finally {
                browser.close();
            }
        }
    }

    @Test
    void whenClearIsCalledRepeatedlyThenOriginalTextIsRestoredWithoutWrappers() throws IOException {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            try {
                Page page = browser.newPage();
                page.setContent("<div id='target'></div>");
                page.addScriptTag(new Page.AddScriptTagOptions().setContent(loadSystemScript()));

                page.evaluate("() => {"
                        + " const target = document.querySelector('#target');"
                        + " target.textContent = 'literal <tag> & token';"
                        + " window.pacos.textHighlighter.highlight(target, 'token');"
                        + " window.pacos.textHighlighter.clear(target);"
                        + " window.pacos.textHighlighter.clear(target);"
                        + "}");

                assertEquals("literal <tag> & token", page.locator("#target").textContent());
                assertEquals(0, page.locator("#target mark.search-highlight").count());
                assertEquals(0, page.locator("#target span").count());
            } finally {
                browser.close();
            }
        }
    }

    @Test
    void whenRapidLongQueriesAreClearedThenNoStaleHighlightIsApplied() throws IOException {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            try {
                Page page = browser.newPage();
                page.setContent("<div id='target'>token remains visible</div>");
                page.addScriptTag(new Page.AddScriptTagOptions().setContent(loadSystemScript()));

                page.evaluate("() => {"
                        + " const target = document.querySelector('#target');"
                        + " for (let index = 0; index < 300; index++) {"
                        + "   window.pacos.textHighlighter.highlight(target, 'x'.repeat(index + 1));"
                        + " }"
                        + " window.pacos.textHighlighter.clear(target);"
                        + "}");

                page.waitForTimeout(250);
                assertEquals("token remains visible", page.locator("#target").textContent());
                assertEquals(0, page.locator("#target mark.search-highlight").count());
            } finally {
                browser.close();
            }
        }
    }

    private static String loadSystemScript() throws IOException {
        try (InputStream input = TextHighlighterE2ETest.class.getResourceAsStream(
                "/META-INF/resources/js/system.js")) {
            if (input == null) {
                throw new IllegalStateException("PacOS system.js was not found on the test classpath");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
