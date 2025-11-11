package com.mmmm.story.data;

/**
 * Represents personal settings for a player
 */
public class PlayerSettings {
    
    private boolean showDialogs;
    private DialogSpeed dialogSpeed;
    
    public enum DialogSpeed {
        SLOW(1.5),      // 150% slower
        NORMAL(1.0),    // Default speed
        FAST(0.75);     // 25% faster
        
        private final double multiplier;
        
        DialogSpeed(double multiplier) {
            this.multiplier = multiplier;
        }
        
        public double getMultiplier() {
            return multiplier;
        }
        
        public static DialogSpeed fromString(String str) {
            try {
                return valueOf(str.toUpperCase());
            } catch (IllegalArgumentException e) {
                return NORMAL;
            }
        }
    }
    
    // Default constructor with default settings
    public PlayerSettings() {
        this.showDialogs = true;
        this.dialogSpeed = DialogSpeed.NORMAL;
    }
    
    public PlayerSettings(boolean showDialogs, DialogSpeed dialogSpeed) {
        this.showDialogs = showDialogs;
        this.dialogSpeed = dialogSpeed;
    }
    
    public boolean isShowDialogs() {
        return showDialogs;
    }
    
    public void setShowDialogs(boolean showDialogs) {
        this.showDialogs = showDialogs;
    }
    
    
    public DialogSpeed getDialogSpeed() {
        return dialogSpeed;
    }
    
    public void setDialogSpeed(DialogSpeed dialogSpeed) {
        this.dialogSpeed = dialogSpeed;
    }
    
    public double getSpeedMultiplier() {
        return dialogSpeed.getMultiplier();
    }
    
    // Toggle methods for easy switching
    public void toggleDialogs() {
        this.showDialogs = !this.showDialogs;
    }
    
    
    public void cycleSpeed() {
        this.dialogSpeed = switch (this.dialogSpeed) {
            case SLOW -> DialogSpeed.NORMAL;
            case NORMAL -> DialogSpeed.FAST;
            case FAST -> DialogSpeed.SLOW;
        };
    }
}
