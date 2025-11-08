package com.mmmm.story.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

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
}