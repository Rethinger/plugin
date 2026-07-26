package com.mmmm.story.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class MessageManagerTest {

    @Test
    public void testMessagesEnYmlSyntax(@TempDir Path tempDir) throws IOException {
        // Test that messages_en.yml can be loaded without syntax errors
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("messages_en.yml");
        assertNotNull(resourceStream, "messages_en.yml resource should exist");
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(
            new InputStreamReader(resourceStream, StandardCharsets.UTF_8)
        );
        
        // Test that we can access some known keys
        assertNotNull(config.getString("npc.messenger_name"), "Should be able to access npc.messenger_name");
        assertNotNull(config.getString("boss1.name"), "Should be able to access boss1.name");
        
        // Test the specific problematic end artifact entries
        assertNotNull(config.getString("chest.items.end_artifact_1.name"), "Should be able to access end_artifact_1.name");
        assertNotNull(config.getStringList("chest.items.end_artifact_1.lore"), "Should be able to access end_artifact_1.lore");
        assertNotNull(config.getString("chest.items.end_artifact_2.name"), "Should be able to access end_artifact_2.name");
        assertNotNull(config.getStringList("chest.items.end_artifact_2.lore"), "Should be able to access end_artifact_2.lore");
        assertNotNull(config.getString("chest.items.end_artifact_3.name"), "Should be able to access end_artifact_3.name");
        assertNotNull(config.getStringList("chest.items.end_artifact_3.lore"), "Should be able to access end_artifact_3.lore");
        assertNotNull(config.getString("chest.items.end_artifact_4.name"), "Should be able to access end_artifact_4.name");
        assertNotNull(config.getStringList("chest.items.end_artifact_4.lore"), "Should be able to access end_artifact_4.lore");
        assertNotNull(config.getString("chest.items.end_artifact_5.name"), "Should be able to access end_artifact_5.name");
        assertNotNull(config.getStringList("chest.items.end_artifact_5.lore"), "Should be able to access end_artifact_5.lore");
        
        // Verify that the lore lists have the expected content
        assertTrue(config.getStringList("chest.items.end_artifact_1.lore").size() > 0, "end_artifact_1 lore should not be empty");
        assertTrue(config.getStringList("chest.items.end_artifact_2.lore").size() > 0, "end_artifact_2 lore should not be empty");
        assertTrue(config.getStringList("chest.items.end_artifact_3.lore").size() > 0, "end_artifact_3 lore should not be empty");
        assertTrue(config.getStringList("chest.items.end_artifact_4.lore").size() > 0, "end_artifact_4 lore should not be empty");
        assertTrue(config.getStringList("chest.items.end_artifact_5.lore").size() > 0, "end_artifact_5 lore should not be empty");
    }

    /**
     * The two locale files must expose exactly the same key set.
     *
     * <p>They had drifted apart in both directions: keys present only in Russian left
     * English players on the Russian fallback, and {@code entities.end_guardian}
     * existed only in English, so Russian players saw the raw key as the boss name.
     */
    @Test
    public void testLocaleFilesHaveIdenticalKeys() throws IOException {
        Set<String> ru = leafKeys(load("messages.yml"));
        Set<String> en = leafKeys(load("messages_en.yml"));

        Set<String> missingFromEn = new TreeSet<>(ru);
        missingFromEn.removeAll(en);
        Set<String> missingFromRu = new TreeSet<>(en);
        missingFromRu.removeAll(ru);

        assertTrue(missingFromEn.isEmpty(), "Keys missing from messages_en.yml: " + missingFromEn);
        assertTrue(missingFromRu.isEmpty(), "Keys missing from messages.yml: " + missingFromRu);
    }

    /**
     * A key defined twice silently discards the first definition. This is how the
     * English {@code chest.items} block lost six story items and {@code act5} lost
     * four messages.
     */
    @Test
    public void testNoDuplicateKeysInLocaleFiles() throws IOException {
        for (String file : new String[]{"messages.yml", "messages_en.yml"}) {
            List<String> lines = readResourceLines(file);
            Map<String, Integer> seen = new HashMap<>();
            List<String> path = new ArrayList<>();
            List<Integer> indents = new ArrayList<>();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String stripped = line.stripLeading();
                if (stripped.isBlank() || stripped.startsWith("#") || stripped.startsWith("-")) {
                    continue;
                }
                int colon = stripped.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                String name = stripped.substring(0, colon).strip();
                if (name.isEmpty() || name.contains("\"") || name.contains(" ")) {
                    continue;
                }

                // Pop every entry at the same or deeper indentation - those siblings and
                // children are closed by this line.
                int indent = line.length() - stripped.length();
                while (!indents.isEmpty() && indents.get(indents.size() - 1) >= indent) {
                    indents.remove(indents.size() - 1);
                    path.remove(path.size() - 1);
                }
                path.add(name);
                indents.add(indent);

                String full = String.join(".", path);
                Integer first = seen.put(full, i + 1);
                assertNull(first, "Duplicate key '" + full + "' in " + file
                        + " at lines " + first + " and " + (i + 1));
            }
        }
    }

    private YamlConfiguration load(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, resource + " should exist");
            return YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    private List<String> readResourceLines(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, resource + " should exist");
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().toList();
        }
    }

    /** Every path that holds a value rather than a nested section. */
    private Set<String> leafKeys(YamlConfiguration config) {
        Set<String> leaves = new TreeSet<>();
        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                leaves.add(key);
            }
        }
        return leaves;
    }
}