package com.mmmm.story.managers;

/**
 * Enumeration of different menu types in the story plugin menu system.
 * Used for navigation and state management.
 */
public enum MenuType {
    /**
     * Main menu - the root menu with primary options
     */
    MAIN,
    
    /**
     * Settings submenu - player configuration options
     */
    SETTINGS,
    
    /**
     * Information submenu - plugin information and version details
     */
    INFO,
    
    /**
     * Server start menu - menu for server initialization
     */
    SERVER_START,
    
    /**
     * Story menu - shows player progress and quest information
     */
    STORY
}