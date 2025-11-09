package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import com.mmmm.story.managers.MessageManager;
import com.mmmm.story.managers.PlayerPlacedBlocksManager;
import com.mmmm.story.bosses.CircleStrafeTracker;
import com.mmmm.story.bosses.BossAttackState;
import com.mmmm.story.bosses.SafeZone;
import com.mmmm.story.bosses.SpecialAttackConfiguration;
import com.mmmm.story.bosses.WitherSkullProjectile;
import com.mmmm.story.bosses.BossRisingAnimation;
import com.mmmm.story.bosses.BossSpecialAttackManager;
import com.mmmm.story.bosses.StationaryCastingManager;
import com.mmmm.story.bosses.StationaryAttackConfiguration;
import com.mmmm.story.managers.SafeZoneManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.util.EulerAngle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.enchantments.Enchantment;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Act2Listener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    private BossBar boss1BossBar;
    private Skeleton boss1Entity;
    private int boss1Phase = 1;
    private Set<UUID> boss1Warriors = new HashSet<>();
    private Map<UUID, Long> playersAboveBoss = new HashMap<>(); // Отслеживание игроков над боссом
    private Map<UUID, Long> playersNearBoss = new HashMap<>(); // Отслеживание игроков рядом с боссом для телепортации
    private Map<UUID, Integer> playerArrowsShot = new HashMap<>(); // Счетчик стрел для отражения каждой 3-й
    
    // BUG #2 FIX: Boss teleport tracking
    private Map<UUID, Long> playerProximityStart = new HashMap<>(); // When player entered proximity
    private Map<UUID, Long> teleportCooldown = new HashMap<>(); // Last teleport time per player
    
    // Circle-strafe detection for boss exploit prevention
    private Map<UUID, CircleStrafeTracker> circleStrafeTrackers = new HashMap<>();
    
    // Wither skull attack state tracking (phase 1 only)
    private BossAttackState witherSkullAttackState = null;
    private List<SafeZone> currentSafeZones = new ArrayList<>();
    
    // NEW: Special attack components
    private SpecialAttackConfiguration specialAttackConfig;
    private SafeZoneManager safeZoneManager;
    private BossRisingAnimation currentRisingAnimation;
    private List<WitherSkullProjectile> activeProjectiles = new ArrayList<>();

    // Enhanced hemisphere special attack manager
    private BossSpecialAttackManager bossSpecialAttackManager;
    private StationaryCastingManager stationaryCastingManager;

    // Task references for proper cleanup
    private BukkitTask bossBarTask;
    private BukkitTask circleStrafeTask;
    private BukkitTask witherSkullAttackTask;
    private BukkitTask bossAITask;
    private BukkitTask heightCheckTask;
    private BukkitTask antiWallTask;
    private BukkitTask teleportTask;
    private BukkitTask proximityTeleportTask;
    private BukkitTask risingAnimationTask;
    private BukkitTask wolfEliminationTask;
    private final List<LivingEntity> warriorSkeletons = new ArrayList<>();
    
    public Act2Listener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        this.safeZoneManager = new SafeZoneManager(plugin);
        this.specialAttackConfig = loadSpecialAttackConfiguration();
    }
    
    /**
     * Load special attack configuration from plugin config
     */
    private SpecialAttackConfiguration loadSpecialAttackConfiguration() {
        if (!plugin.getConfig().contains("acts.boss1.witherSkullAttack.specialAttack")) {
            return new SpecialAttackConfiguration(); // Use defaults
        }
        
        // Load from config
        boolean enabled = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.enabled", true);
        boolean risingAnimation = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.risingAnimation", true);
        double risingHeight = plugin.getConfig().getDouble("acts.boss1.witherSkullAttack.specialAttack.risingHeight", 12.0);
        int risingDuration = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.specialAttack.risingDuration", 40);
        int projectileCount = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.specialAttack.projectileCount", 36);
        boolean spherePattern = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.spherePattern", true);
        boolean preventSkeletonSpawn = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.preventSkeletonSpawn", true);
        boolean removeStunEffect = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.removeStunEffect", true);
        
        // NEW: Timing and cadence settings
        int hoverDurationTicks = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.specialAttack.hoverDurationTicks", 0);
        int minSpecialAttackSpacingSeconds = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.specialAttack.minSpecialAttackSpacingSeconds", 20);
        boolean requireWarriorSummonBetweenSpecials = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.requireWarriorSummonBetweenSpecials", true);
        
        // NEW: Ground skull gather animation settings
        int groundGatherDurationTicks = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.specialAttack.groundGatherDurationTicks", 30);
        int groundGatherPauseTicks = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.specialAttack.groundGatherPauseTicks", 20);
        
        boolean soulFireParticles = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.particleEffects.soulFire", true);
        boolean endRodParticles = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.particleEffects.endRod", true);
        boolean skullTrailParticles = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.specialAttack.particleEffects.skullTrail", true);
        
        return new SpecialAttackConfiguration(
            enabled, risingAnimation, risingHeight, risingDuration, projectileCount,
            spherePattern, preventSkeletonSpawn, removeStunEffect,
            hoverDurationTicks, minSpecialAttackSpacingSeconds, requireWarriorSummonBetweenSpecials,
            soulFireParticles, endRodParticles, skullTrailParticles,
            groundGatherDurationTicks, groundGatherPauseTicks
        );
    }
    
    @EventHandler
    public void onBoss1KeyDrop(org.bukkit.event.entity.ItemSpawnEvent event) {
        if (plugin.getDataManager().getCurrentAct() < 2) {
            return;
        }
        
        org.bukkit.entity.Item droppedItem = event.getEntity();
        ItemStack itemStack = droppedItem.getItemStack();
        
        // Check if it's Boss 1 Summon Key
        if (!plugin.getItemManager().isStoryItem(itemStack)) {
            return;
        }
        
        if (!ItemManager.BOSS1_SUMMON_KEY.equals(plugin.getItemManager().getStoryItemId(itemStack))) {
            return;
        }
        
        // Schedule check for Ancient Debris below
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!droppedItem.isValid() || droppedItem.isDead()) {
                    cancel();
                    return;
                }
               
                Location itemLoc = droppedItem.getLocation();
                Location blockBelow = itemLoc.clone().subtract(0, 1, 0);
               
                // Check if item is on Ancient Debris
                if (blockBelow.getBlock().getType() == Material.ANCIENT_DEBRIS) {
                    // Summon Boss 1!
                    summonBoss1(droppedItem);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 5L);
    }
    
    @EventHandler
    public void onBoss1MaterialsCombine(org.bukkit.event.entity.ItemSpawnEvent event) {
        if (!plugin.getDataManager().isBoss1Defeated()) {
            return;
        }
        
        org.bukkit.entity.Item droppedItem = event.getEntity();
        ItemStack itemStack = droppedItem.getItemStack();
        
        // Check if it's Boss 1 Material or Catalyst
        if (!plugin.getItemManager().isStoryItem(itemStack)) {
            return;
        }
        
        String itemId = plugin.getItemManager().getStoryItemId(itemStack);
        if (!ItemManager.BOSS1_MATERIAL.equals(itemId) && !ItemManager.BOSS1_CATALYST.equals(itemId)) {
            return;
        }
        
        // Schedule check for both items on Ancient Debris
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!droppedItem.isValid() || droppedItem.isDead()) {
                    cancel();
                    return;
                }
               
                Location itemLoc = droppedItem.getLocation();
                Location blockBelow = itemLoc.clone().subtract(0, 1, 0);
               
                // Check if item is on Ancient Debris
                if (blockBelow.getBlock().getType() != Material.ANCIENT_DEBRIS) {
                    return;
                }
               
                // Check for both materials nearby
                boolean hasMaterial = false;
                boolean hasCatalyst = false;
                org.bukkit.entity.Item materialItem = null;
                org.bukkit.entity.Item catalystItem = null;
               
                for (org.bukkit.entity.Item nearbyItem : droppedItem.getNearbyEntities(2, 2, 2).stream()
                        .filter(e -> e instanceof org.bukkit.entity.Item)
                        .map(e -> (org.bukkit.entity.Item) e)
                        .toList()) {
                    ItemStack stack = nearbyItem.getItemStack();
                    if (plugin.getItemManager().isStoryItem(stack)) {
                        String id = plugin.getItemManager().getStoryItemId(stack);
                        if (ItemManager.BOSS1_MATERIAL.equals(id)) {
                            hasMaterial = true;
                            materialItem = nearbyItem;
                        } else if (ItemManager.BOSS1_CATALYST.equals(id)) {
                            hasCatalyst = true;
                            catalystItem = nearbyItem;
                        }
                    }
                }
               
                // Include: dropped item itself
                if (ItemManager.BOSS1_MATERIAL.equals(itemId)) {
                    hasMaterial = true;
                    materialItem = droppedItem;
                } else if (ItemManager.BOSS1_CATALYST.equals(itemId)) {
                    hasCatalyst = true;
                    catalystItem = droppedItem;
                }
               
                // If both present, trigger Boss 2 summoning
                if (hasMaterial && hasCatalyst && materialItem != null && catalystItem != null) {
                    combineMaterialsAndSummonBoss2(materialItem, catalystItem, blockBelow);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 5L);
    }
    
    private void combineMaterialsAndSummonBoss2(org.bukkit.entity.Item material, org.bukkit.entity.Item catalyst, Location ancientDebris) {
        Location location = ancientDebris.clone().add(0.5, 1, 0.5);
        World world = location.getWorld();
        
        // Remove both items
        material.remove();
        catalyst.remove();
        
        // Epic combination effects
        world.spawnParticle(Particle.PORTAL, location, 300, 1, 2, 1, 1);
        world.spawnParticle(Particle.DRAGON_BREATH, location, 200, 1, 2, 1, 0.1);
        world.spawnParticle(Particle.END_ROD, location, 100, 0.5, 1, 0.5, 0.1);
        world.playSound(location, Sound.BLOCK_PORTAL_TRIGGER, 2.0f, 0.5f);
        world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 1.5f, 1.0f);
        
        // Play dialog FIRST for nearby players
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(location) < 100) {
                plugin.getDialogManager().playDialog(player, "boss2.summon");
            }
        }
        
        // Spawn Boss 2 AFTER dialog (32 seconds = after "БОЙ С ИЗВЕРГОМ НАЧИНАЕТСЯ")
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnBoss2(location);
            }
        }.runTaskLater(plugin, 640L); // 32 seconds * 20 ticks
    }
    
    private void summonBoss1(org.bukkit.entity.Item droppedItem) {
        final Location location = droppedItem.getLocation();
        final World world = location.getWorld();
        
        // Remove: item
        droppedItem.remove();
        
        // Initial effects when key is inserted (delay: 0)
        world.spawnParticle(Particle.FLAME, location, 200, 2, 2, 2, 0.1);
        world.spawnParticle(Particle.LAVA, location, 100, 1, 1, 1, 0.1);
        world.spawnParticle(Particle.SMOKE, location, 150, 1.5, 1.5, 1.5, 0.05);
        world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
        world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1.2f);
        
        // Synchronized particle effects with dialog timeline:
        
        // Delay 3s: "Земля разверзается" - Ground cracks
        new BukkitRunnable() {
            @Override
            public void run() {
                world.spawnParticle(Particle.BLOCK, location, 300, 2, 0.5, 2, 0, Material.NETHERRACK.createBlockData());
                world.spawnParticle(Particle.LAVA, location, 50, 1.5, 0.5, 1.5, 0.1);
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
            }
        }.runTaskLater(plugin, 60L); // 3 seconds
        
        // Delay 6s: "Из глубин доносится рык" - Dark energy rising
        new BukkitRunnable() {
            @Override
            public void run() {
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 100, 1, 1, 1, 0.15);
                world.spawnParticle(Particle.SMOKE, location, 200, 1.5, 1, 1.5, 0.1);
                world.spawnParticle(Particle.REVERSE_PORTAL, location, 50, 1, 1, 1, 0.3);
            }
        }.runTaskLater(plugin, 120L); // 6 seconds
        
        // Delay 9s: "Кто посмел потревожить мой сон" - Awakening power
        new BukkitRunnable() {
            @Override
            public void run() {
                world.spawnParticle(Particle.END_ROD, location, 80, 2, 2, 2, 0.2);
                world.spawnParticle(Particle.DRAGON_BREATH, location, 60, 1.5, 1, 1.5, 0.05);
                world.spawnParticle(Particle.SOUL, location, 40, 1, 1.5, 1, 0.1);
            }
        }.runTaskLater(plugin, 180L); // 9 seconds
        
        // Delay 13s: "Я был рыцарем" - Noble memories
        new BukkitRunnable() {
            @Override
            public void run() {
                world.spawnParticle(Particle.ENCHANT, location, 150, 2, 1.5, 2, 1);
                world.spawnParticle(Particle.END_ROD, location, 50, 1, 2, 1, 0.1);
                world.playSound(location, Sound.BLOCK_BELL_USE, 1.0f, 1.2f);
            }
        }.runTaskLater(plugin, 260L); // 13 seconds
        
        // Delay 17s: "Я охранял печать" - Ancient seal imagery
        new BukkitRunnable() {
            @Override
            public void run() {
                world.spawnParticle(Particle.ENCHANT, location, 200, 2.5, 1, 2.5, 1.5);
                world.spawnParticle(Particle.GLOW, location, 100, 2, 1.5, 2, 0);
                world.spawnParticle(Particle.END_ROD, location, 80, 1.5, 1.5, 1.5, 0.15);
                world.playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.5f, 1.0f);
            }
        }.runTaskLater(plugin, 340L); // 17 seconds
        
        // Delay 21s: "ВЫ УМРЁТЕ!" - Rage explosion
        new BukkitRunnable() {
            @Override
            public void run() {
                world.spawnParticle(Particle.EXPLOSION, location, 15, 2, 1, 2, 0);
                world.spawnParticle(Particle.FLASH, location, 5, 1, 1, 1, 0);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 300, 3, 2, 3, 0.3);
                world.spawnParticle(Particle.LAVA, location, 150, 2.5, 1, 2.5, 0.1);
                world.playSound(location, Sound.ENTITY_WITHER_SHOOT, 2.0f, 0.7f);
                world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1.0f);
            }
        }.runTaskLater(plugin, 420L); // 21 seconds
        
        // Delay 28s: "он... страж... ты освобождаешь..." - Warning signs
        new BukkitRunnable() {
            @Override
            public void run() {
                world.spawnParticle(Particle.REVERSE_PORTAL, location, 200, 2, 2, 2, 0.5);
                world.spawnParticle(Particle.DRAGON_BREATH, location, 100, 2, 1.5, 2, 0.1);
                world.spawnParticle(Particle.SOUL, location, 60, 1.5, 1.5, 1.5, 0.05);
                world.playSound(location, Sound.BLOCK_PORTAL_AMBIENT, 2.0f, 0.8f);
            }
        }.runTaskLater(plugin, 560L); // 28 seconds
        
        // Play dialog FIRST for nearby players
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(location) < 100) {
                plugin.getDialogManager().playDialog(player, "boss1.summon");
            }
        }
        
        // Spawn Boss 1 AFTER dialog (31 seconds = after "БОЙ НАЧИНАЕТСЯ")
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnBoss1(location);
            }
        }.runTaskLater(plugin, 620L); // 31 seconds * 20 ticks
    }
    
    private void spawnBoss1(Location location) {
        final Location spawnLoc = location.clone();
        final World world = spawnLoc.getWorld();
        
        // НЕ СОЗДАЁМ новый ancient_debris - просто используем тот который уже есть!
        // Предполагается что блок уже ancient_debris под dropped item
        
        // PHASE 1: Dramatic particle buildup (0-2 seconds)
        world.spawnParticle(Particle.LAVA, spawnLoc.clone().add(0.5, 0.5, 0.5), 100, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.FLAME, spawnLoc.clone().add(0.5, 0.5, 0.5), 200, 1, 1, 1, 0.1);
        world.spawnParticle(Particle.SOUL, spawnLoc.clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0.05);
        world.playSound(spawnLoc, Sound.BLOCK_PORTAL_TRIGGER, 2.0f, 0.5f);
        world.playSound(spawnLoc, Sound.ENTITY_WITHER_HURT, 1.5f, 0.8f);
        
        // PHASE 2: Череп на невидимом armor stand (2 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Create invisible armor stand for skull
                final ArmorStand skullStand = world.spawn(spawnLoc.clone().add(0.5, 1, 0.5), ArmorStand.class);
                skullStand.setVisible(false);
                skullStand.setGravity(false);
                skullStand.setInvulnerable(true);
                skullStand.setMarker(true); // No hitbox
                skullStand.setHelmet(new ItemStack(Material.SKELETON_SKULL)); // Changed from WITHER_SKELETON_SKULL
               
                // Skull flies up with particles
                world.spawnParticle(Particle.EXPLOSION, spawnLoc.clone().add(0.5, 1, 0.5), 3, 0.3, 0.3, 0.3, 0);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc.clone().add(0.5, 1, 0.5), 100, 0.5, 0.5, 0.5, 0.1);
                world.playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 1.2f);
                world.playSound(spawnLoc, Sound.BLOCK_BELL_USE, 1.0f, 0.8f);
               
                // Animate skull flying up
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks >= 20 || !skullStand.isValid()) { // 1 second
                            cancel();
                            return;
                        }
                        skullStand.teleport(skullStand.getLocation().add(0, 0.1, 0));
                        world.spawnParticle(Particle.SOUL, skullStand.getLocation(), 2, 0.1, 0.1, 0.1, 0.01);
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
               
                // PHASE 3: Sword flies out (after 1 second)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final ArmorStand swordStand = world.spawn(spawnLoc.clone().add(0.5, 1, 0.5), ArmorStand.class);
                        swordStand.setVisible(false);
                        swordStand.setGravity(false);
                        swordStand.setInvulnerable(true);
                        swordStand.setMarker(true);
                        swordStand.setItemInHand(new ItemStack(Material.NETHERITE_SWORD));
                        swordStand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0)); // Hold sword up
                       
                        world.spawnParticle(Particle.ENCHANTED_HIT, swordStand.getLocation(), 50, 0.3, 0.3, 0.3, 0.1);
                        world.playSound(spawnLoc, Sound.ITEM_TRIDENT_THROW, 1.5f, 0.8f);
                        world.playSound(spawnLoc, Sound.BLOCK_ANVIL_LAND, 0.8f, 1.5f);
                       
                        // Animate sword flying to the side
                        new BukkitRunnable() {
                            int ticks = 0;
                            @Override
                            public void run() {
                                if (ticks >= 15 || !swordStand.isValid()) {
                                    cancel();
                                    return;
                                }
                                swordStand.teleport(swordStand.getLocation().add(0.15, 0.05, 0));
                                world.spawnParticle(Particle.CRIT, swordStand.getLocation(), 1, 0.05, 0.05, 0.05, 0);
                                ticks++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);
                       
                        // PHASE 4: Armor pieces fly out (after 0.5 seconds)
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                // Helmet
                                final ArmorStand helmetStand = world.spawn(spawnLoc.clone().add(0.5, 1.5, 0.5), ArmorStand.class);
                                helmetStand.setVisible(false);
                                helmetStand.setGravity(false);
                                helmetStand.setInvulnerable(true);
                                helmetStand.setMarker(true);
                                helmetStand.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                               
                                // Chestplate
                                final ArmorStand chestStand = world.spawn(spawnLoc.clone().add(0.5, 1.3, 0.5), ArmorStand.class);
                                chestStand.setVisible(false);
                                chestStand.setGravity(false);
                                chestStand.setInvulnerable(true);
                                chestStand.setMarker(true);
                                chestStand.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                               
                                // Leggings
                                final ArmorStand legsStand = world.spawn(spawnLoc.clone().add(0.5, 1.1, 0.5), ArmorStand.class);
                                legsStand.setVisible(false);
                                legsStand.setGravity(false);
                                legsStand.setInvulnerable(true);
                                legsStand.setMarker(true);
                                legsStand.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                               
                                // Boots
                                final ArmorStand bootsStand = world.spawn(spawnLoc.clone().add(0.5, 0.9, 0.5), ArmorStand.class);
                                bootsStand.setVisible(false);
                                bootsStand.setGravity(false);
                                bootsStand.setInvulnerable(true);
                                bootsStand.setMarker(true);
                                bootsStand.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                               
                                world.spawnParticle(Particle.ITEM, spawnLoc.clone().add(0.5, 1.2, 0.5), 100, 0.5, 0.5, 0.5, 0.1, new ItemStack(Material.NETHERITE_CHESTPLATE));
                                world.playSound(spawnLoc, Sound.BLOCK_CHAIN_PLACE, 2.0f, 0.7f);
                                world.playSound(spawnLoc, Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
                               
                                // Animate armor pieces flying around in circle
                                new BukkitRunnable() {
                                    int ticks = 0;
                                    @Override
                                    public void run() {
                                        if (ticks >= 20) {
                                            // After circular animation, move armor to skeleton position
                                            new BukkitRunnable() {
                                                int convergeTicks = 0;
                                                @Override
                                                public void run() {
                                                    if (convergeTicks >= 10) {
                                                        cancel();
                                                        return;
                                                    }
                                                    // Move armor pieces towards center skeleton position
                                                    double progress = convergeTicks / 10.0;
                                                    Location targetHelmet = spawnLoc.clone().add(0.5, 1.7, 0.5);
                                                    Location targetChest = spawnLoc.clone().add(0.5, 1.3, 0.5);
                                                    Location targetLegs = spawnLoc.clone().add(0.5, 0.9, 0.5);
                                                    Location targetBoots = spawnLoc.clone().add(0.5, 0.3, 0.5);
                                                   
                                                    helmetStand.teleport(helmetStand.getLocation().clone().add(
                                                        (targetHelmet.getX() - helmetStand.getLocation().getX()) * 0.2,
                                                        (targetHelmet.getY() - helmetStand.getLocation().getY()) * 0.2,
                                                        (targetHelmet.getZ() - helmetStand.getLocation().getZ()) * 0.2
                                                    ));
                                                    chestStand.teleport(chestStand.getLocation().clone().add(
                                                        (targetChest.getX() - chestStand.getLocation().getX()) * 0.2,
                                                        (targetChest.getY() - chestStand.getLocation().getY()) * 0.2,
                                                        (targetChest.getZ() - chestStand.getLocation().getZ()) * 0.2
                                                    ));
                                                    legsStand.teleport(legsStand.getLocation().clone().add(
                                                        (targetLegs.getX() - legsStand.getLocation().getX()) * 0.2,
                                                        (targetLegs.getY() - legsStand.getLocation().getY()) * 0.2,
                                                        (targetLegs.getZ() - legsStand.getLocation().getZ()) * 0.2
                                                    ));
                                                    bootsStand.teleport(bootsStand.getLocation().clone().add(
                                                        (targetBoots.getX() - bootsStand.getLocation().getX()) * 0.2,
                                                        (targetBoots.getY() - bootsStand.getLocation().getY()) * 0.2,
                                                        (targetBoots.getZ() - bootsStand.getLocation().getZ()) * 0.2
                                                    ));
                                                   
                                                    world.spawnParticle(Particle.ENCHANT, helmetStand.getLocation(), 2, 0.1, 0.1, 0.1, 0);
                                                    world.spawnParticle(Particle.ENCHANT, chestStand.getLocation(), 2, 0.1, 0.1, 0.1, 0);
                                                    convergeTicks++;
                                                }
                                            }.runTaskTimer(plugin, 0L, 1L);
                                            cancel();
                                            return;
                                        }
                                        double angle = ticks * Math.PI / 10;
                                        helmetStand.teleport(spawnLoc.clone().add(0.5 + Math.cos(angle) * 0.8, 1.8, 0.5 + Math.sin(angle) * 0.8));
                                        chestStand.teleport(spawnLoc.clone().add(0.5 + Math.cos(angle + Math.PI/2) * 0.7, 1.3, 0.5 + Math.sin(angle + Math.PI/2) * 0.7));
                                        legsStand.teleport(spawnLoc.clone().add(0.5 + Math.cos(angle + Math.PI) * 0.6, 1.1, 0.5 + Math.sin(angle + Math.PI) * 0.6));
                                        bootsStand.teleport(spawnLoc.clone().add(0.5 + Math.cos(angle + 3*Math.PI/2) * 0.5, 0.9, 0.5 + Math.sin(angle + 3*Math.PI/2) * 0.5));
                                       
                                        world.spawnParticle(Particle.ENCHANT, helmetStand.getLocation(), 1, 0, 0, 0, 0);
                                        ticks++;
                                    }
                                }.runTaskTimer(plugin, 0L, 1L);
                               
                                // PHASE 5: Skeleton silhouette buildup with particles (after 1 second)
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        // Draw skeleton silhouette with particles
                                        for (int i = 0; i < 40; i++) {
                                            double y = 0.2 + (i * 0.05); // From feet to head
                                            world.spawnParticle(Particle.END_ROD, spawnLoc.clone().add(0.5, y, 0.5), 1, 0.2, 0, 0.2, 0);
                                        }
                                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc.clone().add(0.5, 1, 0.5), 50, 0.3, 0.5, 0.3, 0.05);
                                        world.playSound(spawnLoc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.5f, 1.5f);
                                       
                                        // PHASE 6: IMPACT FRAME! (after 0.5 seconds)
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                // MASSIVE IMPACT EFFECT
                                                world.spawnParticle(Particle.EXPLOSION, spawnLoc.clone().add(0.5, 1, 0.5), 10, 0, 0, 0, 0);
                                                world.spawnParticle(Particle.FLASH, spawnLoc.clone().add(0.5, 1, 0.5), 3, 0, 0, 0, 0);
                                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc.clone().add(0.5, 1, 0.5), 300, 1.5, 1.5, 1.5, 0.3);
                                                world.spawnParticle(Particle.END_ROD, spawnLoc.clone().add(0.5, 1, 0.5), 200, 1, 1, 1, 0.2);
                                                world.spawnParticle(Particle.REVERSE_PORTAL, spawnLoc.clone().add(0.5, 1, 0.5), 500, 1.5, 1.5, 1.5, 0.5);
                                                world.spawnParticle(Particle.DRAGON_BREATH, spawnLoc.clone().add(0.5, 1, 0.5), 150, 1, 1, 1, 0.1);
                                               
                                                // EPIC SOUND COMBO
                                                world.playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
                                                world.playSound(spawnLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
                                                world.playSound(spawnLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
                                                world.playSound(spawnLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                                                world.playSound(spawnLoc, Sound.BLOCK_BELL_RESONATE, 2.0f, 0.6f);
                                               
                                                // Remove all armor stands
                                                skullStand.remove();
                                                swordStand.remove();
                                                helmetStand.remove();
                                                chestStand.remove();
                                                legsStand.remove();
                                                bootsStand.remove();
                                               
                                                // PHASE7: Spawn actual boss (after impact)
                                                new BukkitRunnable() {
                                                    @Override
                                                    public void run() {
                                                        Skeleton boss = (Skeleton) world.spawnEntity(spawnLoc.clone().add(0, 1, 0), EntityType.SKELETON);
                                                        boss.setCustomName(plugin.getMessageManager().getMessage("npc.entities.skeleton_lord"));
                                                        boss.setCustomNameVisible(true);
                                                       
                                                        // More epic particles on spawn
                                                        world.spawnParticle(Particle.TOTEM_OF_UNDYING, boss.getLocation(), 100, 1, 1, 1, 0.1);
                                                        world.spawnParticle(Particle.END_ROD, boss.getLocation(), 50, 0.5, 1, 0.5, 0.1);
                                                       
                                                        // Setup boss attributes
                                                        setupBossAttributes(boss);
                                                       
                                                        // T028: Remove wolves' fear mechanic from Boss 1
                                                        // Wolf elimination removed - using Arrow Rain Phase 2 attack instead
                                                    }
                                                }.runTaskLater(plugin, 10L); // 0.5 seconds after impact
                                            }
                                        }.runTaskLater(plugin, 10L); // 0.5 seconds for silhouette
                                    }
                                }.runTaskLater(plugin, 20L); // 1 second for armor dance
                            }
                        }.runTaskLater(plugin, 10L); // 0.5 seconds after sword
                    }
                }.runTaskLater(plugin, 20L); // 1 second after skull
            }
        }.runTaskLater(plugin, 40L); // 2 seconds initial buildup
    }
    
    private void setupBossAttributes(Skeleton boss) {
        boss.setCustomNameVisible(true);
        
        // Set attributes
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
        boss.setHealth(300.0);
        boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
        
        // Equipment: full netherite with enchantments
        ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
        helmet.addEnchantment(Enchantment.VANISHING_CURSE, 1); // Проклятие утраты
        
        ItemStack chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.VANISHING_CURSE, 1);
        
        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        leggings.addEnchantment(Enchantment.VANISHING_CURSE, 1);
        
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);
        boots.addEnchantment(Enchantment.VANISHING_CURSE, 1);
        boots.addEnchantment(Enchantment.FROST_WALKER, 2); // Frost Walker II
        
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        sword.addEnchantment(Enchantment.LOOTING, 3); // Looting III
        
        boss.getEquipment().setHelmet(helmet);
        boss.getEquipment().setChestplate(chestplate);
        boss.getEquipment().setLeggings(leggings);
        boss.getEquipment().setBoots(boots);
        boss.getEquipment().setItemInMainHand(sword);
        
        // Fire immunity (potion effect)
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        boss.setFireTicks(0);
        boss.setVisualFire(false);
        
        // Store boss entity for boss bar
        boss1Entity = boss;
        boss1Phase = 1;
        boss1Warriors.clear();
        
        // Create boss bar
        createBoss1BossBar();
        
        // Show boss bar to all nearby players
        for (Player player : boss.getWorld().getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) < 100) {
                player.showBossBar(boss1BossBar);
            }
        }
        // Mark as boss
        boss.setRemoveWhenFarAway(false);
        
        // Start summon task
        startBoss1SummonTask(boss);
        
        // Start boss bar update task
        startBoss1BossBarTask();
        
        // Start circle-strafe detection task
        startCircleStrafeDetection();
        
        // Initialize special attack components
        if (witherSkullAttackState == null) {
            witherSkullAttackState = new BossAttackState(boss.getUniqueId(), 1);
        }
        
        // Start wither skull attack task (phase 1 only)
        startWitherSkullAttackTask();
        
        // Start boss AI task (distance keeping in phase 2)
        startBoss1AITask(boss);
        
        // Start player height check task (pull players down if above)
        startBoss1HeightCheckTask(boss);
        
        // Start anti-wall task (prevent boxing boss)
        startBoss1AntiWallTask(boss);
        
        // Start teleport task (teleport away from players crowding boss)
        startBoss1TeleportTask(boss);
        
        // Start proximity teleport task (Bug #2: teleport behind close players in Phase 2)
        startBoss1ProximityTeleportTask(boss);
    }
    
    private void createBoss1BossBar() {
        boss1BossBar = BossBar.bossBar(
                Component.text("⚔ Повелитель Скелетов ⚔").color(NamedTextColor.DARK_RED),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.NOTCHED_10
        );
    }
    
    private void startBoss1BossBarTask() {
        bossBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss1Entity == null || boss1Entity.isDead() || !boss1Entity.isValid()) {
                    // Remove boss bar from all players
                    if (boss1BossBar != null) {
                        for (Player player : plugin.getServer().getOnlinePlayers()) {
                            player.hideBossBar(boss1BossBar);
                        }
                        boss1BossBar = null;
                    }
                    boss1Entity = null;
                    cancel();
                    return;
                }
               
                // Update boss bar progress
                double health = boss1Entity.getHealth();
                double maxHealth = boss1Entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                float progress = (float) (health / maxHealth);
                boss1BossBar.progress(Math.max(0.0f, Math.min(1.0f, progress)));
               
                // Update boss bar for nearby players
                for (Player player : boss1Entity.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(boss1Entity.getLocation());
                    if (distance < 100) {
                        // Show boss bar if not already shown
                        boolean alreadyViewing = false;
                        for (var viewer : boss1BossBar.viewers()) {
                            if (viewer.equals(player)) {
                                alreadyViewing = true;
                                break;
                            }
                        }
                        if (!alreadyViewing) {
                            player.showBossBar(boss1BossBar);
                        }
                    } else {
                        // Hide boss bar if shown
                        boolean isViewing = false;
                        for (var viewer : boss1BossBar.viewers()) {
                            if (viewer.equals(player)) {
                                isViewing = true;
                                break;
                            }
                        }
                        if (isViewing) {
                            player.hideBossBar(boss1BossBar);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Update every 0.5 seconds
    }
    
    /**
     * Start circle-strafe detection task for boss combat.
     * Runs every 0.5 seconds to update player movement patterns and teleport exploiters.
     */
    private void startCircleStrafeDetection() {
        double minDistance = plugin.getConfig().getDouble("acts.boss1.circleStrafeDetection.minDistance", 2.0);
        double maxDistance = plugin.getConfig().getDouble("acts.boss1.circleStrafeDetection.maxDistance", 3.0);
        double angleThreshold = plugin.getConfig().getDouble("acts.boss1.circleStrafeDetection.angleThreshold", 0.1);
        int minAngleChanges = plugin.getConfig().getInt("acts.boss1.circleStrafeDetection.minAngleChanges", 10);
        int trackingDurationSeconds = plugin.getConfig().getInt("acts.boss1.circleStrafeDetection.trackingDurationSeconds", 3);
        long checkIntervalTicks = plugin.getConfig().getLong("acts.boss1.circleStrafeDetection.checkIntervalTicks", 5);
        
        circleStrafeTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss1Entity == null || boss1Entity.isDead() || !boss1Entity.isValid()) {
                    // Clear all trackers when boss dies
                    circleStrafeTrackers.clear();
                    cancel();
                    return;
                }
               
                Location bossLocation = boss1Entity.getLocation();
               
                // Update trackers for all nearby players
                for (Player player : boss1Entity.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(bossLocation);
                   
                    // Only track players in circle-strafe range
                    if (distance < 100) { // Within boss fight area
                        UUID playerId = player.getUniqueId();
                        Location playerLocation = player.getLocation();
                       
                        // Initialize tracker if needed
                        CircleStrafeTracker tracker = circleStrafeTrackers.computeIfAbsent(
                            playerId, 
                            k -> new CircleStrafeTracker(
                                player,
                                minDistance,
                                maxDistance,
                                angleThreshold,
                                minAngleChanges,
                                trackingDurationSeconds
                            )
                        );
                       
                        // Update tracker with current position
                        tracker.update(playerLocation, bossLocation);
                       
                        // Debug: Log tracking progress
                        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                            plugin.getLogger().info("[Circle-Strafe] " + player.getName() + 
                                " distance=" + String.format("%.2f", distance) + 
                                " blocks, tracking=" + (tracker.isConfirmed() ? "CONFIRMED" : "in progress"));
                        }
                       
                        // Check if circle-strafing detected
                        if (tracker.isConfirmed()) {
                            // Teleport player on top of boss (punishment for exploiting)
                            Location teleportLoc = bossLocation.clone().add(0, 2, 0);
                            player.teleport(teleportLoc);
                            String cowardiceMsg = plugin.getMessageManager().getMessage(player, "boss1.cowardice_punishment");
                            player.sendMessage(Component.text(cowardiceMsg).color(NamedTextColor.RED));
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.7f);
                            bossLocation.getWorld().spawnParticle(Particle.PORTAL, teleportLoc, 50, 0.5, 1, 0.5, 0.5);
                           
                            // Reset tracker after teleport
                            circleStrafeTrackers.remove(playerId);
                           
                            plugin.getLogger().info("[Circle-Strafe] EXPLOIT DETECTED: " + player.getName() + " teleported to boss location");
                        }
                    } else {
                        // Remove tracker if player leaves area
                        circleStrafeTrackers.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, checkIntervalTicks); // Check at configured interval
    }
    
    /**
     * Start wither skull attack task for phase 1 boss combat.
     * Boss fires circular volley every 30 seconds with safe zones.
     */
    private void startWitherSkullAttackTask() {
        int intervalSeconds = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.intervalSeconds", 30);
        int minSafeZones = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.safeZones.count.min", 6);
        int maxSafeZones = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.safeZones.count.max", 8);
        double safeZoneRadius = plugin.getConfig().getDouble("acts.boss1.witherSkullAttack.safeZones.radius", 15.0);
        double protectionRadius = plugin.getConfig().getDouble("acts.boss1.witherSkullAttack.safeZones.protectionRadius", 1.0);
        
        // Initialize attack state with boss UUID and phase 1
        witherSkullAttackState = new BossAttackState(UUID.randomUUID(), 1); // Will be set to actual boss UUID on spawn
        
        witherSkullAttackTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss1Entity == null || boss1Entity.isDead() || !boss1Entity.isValid()) {
                    currentSafeZones.clear();
                    cancel();
                    return;
                }
               
                // Only attack in phase 1
                if (boss1Phase != 1) {
                    return;
                }

                // Check if can attack (respects interval)
                if (witherSkullAttackState.canAttack(intervalSeconds)) {
                    // Check cadence requirements for special attacks
                    if (specialAttackConfig.isEnabled()) {
                        // Prevent re-triggering if a special attack is already in progress
                        if (witherSkullAttackState != null && witherSkullAttackState.isInSpecialAttack()) {
                            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                                plugin.getLogger().info("[Special Attack] Attack already in progress, skipping new attack initiation.");
                            }
                            return;
                        }
                        
                        // Check cadence requirements
                        if (!witherSkullAttackState.canStartSpecialAttack(
                            specialAttackConfig.getMinSpecialAttackSpacingSeconds(),
                            specialAttackConfig.requiresWarriorSummonBetweenSpecials())) {
                            return;
                        }
                    }
                    
                    performWitherSkullAttack(minSafeZones, maxSafeZones, safeZoneRadius, protectionRadius);
                }
            }
        }.runTaskTimer(plugin, 100L, 20L); // Start after 5s, check every second
    }
    
    /**
     * Perform wither skull volley attack with safe zones.
     */
    private void performWitherSkullAttack(int minSafeZones, int maxSafeZones, double radius, double protectionRadius) {
        if (boss1Entity == null) return;
        
        // Check if special attack is enabled and use new mechanics
        if (specialAttackConfig.isEnabled()) {
            // FIX: Prevent re-triggering if a special attack is already in progress
            if (witherSkullAttackState != null && witherSkullAttackState.isInSpecialAttack()) {
                if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                    plugin.getLogger().info("[Special Attack] Attack already in progress, skipping new attack initiation.");
                }
                return;
            }
            performNewSpecialAttack(minSafeZones, maxSafeZones, radius, protectionRadius);
            return;
        }
        
        // Legacy implementation for backward compatibility
        witherSkullAttackState.startAttack();
        Location bossLoc = boss1Entity.getLocation();
        World world = bossLoc.getWorld();
        
        // Check if boss should freeze during cast
        boolean freezeBoss = plugin.getConfig().getBoolean("acts.boss1.witherSkullAttack.freezeBossDuringCast", true);
        int freezeDurationSeconds = plugin.getConfig().getInt("acts.boss1.witherSkullAttack.freezeDurationSeconds", 5);
        
        // Freeze boss if enabled
        if (freezeBoss && boss1Entity instanceof Mob mob) {
            mob.setAI(false);
            mob.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        }
        
        // Generate safe zones
        int safeZoneCount = minSafeZones + (int)(Math.random() * (maxSafeZones - minSafeZones + 1));
        currentSafeZones.clear();
        
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            plugin.getLogger().info("[Safe Zones] Generating " + safeZoneCount + " safe zones within " + radius + " blocks of boss");
        }
        
        for (int i = 0; i < safeZoneCount; i++) {
            SafeZone zone = SafeZone.generateRandom(bossLoc, radius, protectionRadius, freezeDurationSeconds);
            currentSafeZones.add(zone);
           
            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                Location center = zone.getCenter();
                plugin.getLogger().info("[Safe Zones]   Zone " + (i+1) + " at (" +
                    String.format("%.1f", center.getX()) + ", " +
                    String.format("%.1f", center.getY()) + ", " +
                    String.format("%.1f", center.getZ()) + ") radius=" + protectionRadius);
            }
        }
        
        // Visual/audio warning
        world.playSound(bossLoc, Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.7f);
        world.spawnParticle(Particle.SMOKE, bossLoc, 100, 2, 2, 2, 0.1);
        
        // Send localized warning message to nearby players
        MessageManager messageManager = plugin.getMessageManager();
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distance(bossLoc) < 50) {
                String warningMessage = messageManager.getMessage(p, "boss1.wither_skull_attack.warning");
                p.sendMessage(Component.text(warningMessage).color(NamedTextColor.DARK_PURPLE));
            }
        }
        
        // Show safe zones for configured duration
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = freezeDurationSeconds * 20; // Convert seconds to ticks
           
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                   
                    // Unfreeze boss
                    if (freezeBoss && boss1Entity instanceof Mob mob) {
                        mob.setAI(true);
                    }
                   
                    fireWitherSkulls();
                    return;
                }
                for (SafeZone zone : currentSafeZones) {
                    zone.spawnParticles(20);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Perform stationary casting special attack with new mechanics.
     * Uses the StationaryCastingManager for ground-based casting with evoker fangs and danger zone visualization.
     */
    private void performNewSpecialAttack(int minSafeZones, int maxSafeZones, double radius, double protectionRadius) {
        if (boss1Entity == null) return;

        // Clean up any existing special attack managers
        if (bossSpecialAttackManager != null) {
            bossSpecialAttackManager.cleanup();
        }
        if (stationaryCastingManager != null) {
            stationaryCastingManager.cleanup();
        }

        // Create stationary casting manager
        stationaryCastingManager = new StationaryCastingManager(plugin, boss1Entity, safeZoneManager, boss1Phase);
        StationaryAttackConfiguration stationaryConfig = new StationaryAttackConfiguration();

        // Set completion callback to reset attack state
        stationaryCastingManager.setCompletionCallback(new com.mmmm.story.bosses.AttackCompletionCallback() {
            @Override
            public void onAttackCompleted(boolean successful) {
                if (successful) {
                    plugin.getLogger().info("[Stationary Casting] Attack completed successfully, resetting state");
                } else {
                    plugin.getLogger().info("[Stationary Casting] Attack failed or was interrupted, resetting state");
                }

                // Reset attack state to allow new attacks
                witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.NONE);
                witherSkullAttackState.endAttack();
            }
        });

        // Check if stationary casting attack requirements are met
        boolean spacingMet = witherSkullAttackState.canStartSpecialAttack(
                specialAttackConfig.getMinSpecialAttackSpacingSeconds(),
                specialAttackConfig.requiresWarriorSummonBetweenSpecials());

        if (!spacingMet) {
            plugin.getLogger().info("[Stationary Casting] Spacing requirements not met. " +
                "Warrior waves: " + witherSkullAttackState.getWarriorSummonWaveCount() +
                ", Time since last wave: " + String.format("%.1f", witherSkullAttackState.getTimeSinceLastWarriorWave() / 1000.0) + "s" +
                ", Min spacing: " + specialAttackConfig.getMinSpecialAttackSpacingSeconds() + "s");
            return;
        }

        // Check hemisphere-specific warrior summon requirements
        boolean hemisphereMet = witherSkullAttackState.canStartHemisphereSpecialAttack(2, 12);
        if (!hemisphereMet) {
            plugin.getLogger().info("[Stationary Casting] Hemisphere requirements not met. " +
                "Warrior waves: " + witherSkullAttackState.getWarriorSummonWaveCount() +
                " (need >=2), Time since last wave: " + String.format("%.1f", witherSkullAttackState.getTimeSinceLastWarriorWave() / 1000.0) + "s" +
                ", Required: 2 waves with 12 warriors total");
            return;
        }

        // Update attack state for stationary casting
        witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.STATIONARY_CASTING_PREPARATION);

        // Start stationary casting attack
        boolean attackStarted = stationaryCastingManager.startCastingAttack();

        if (attackStarted) {
            // Log start of stationary casting attack
            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                plugin.getLogger().info("[Stationary Casting] Started stationary casting attack in phase " + boss1Phase);
                plugin.getLogger().info("[Stationary Casting] Attack radius: " + stationaryConfig.getAttackRadius() +
                    " blocks, Safe zone radius: " + stationaryConfig.getSafeZoneRadius() + " blocks");
            }

            // Update attack state tracking
            witherSkullAttackState.startAttack();
            witherSkullAttackState.updateLastSpecialAttackTime();

            // Record that a warrior summon was skipped due to special attack
            witherSkullAttackState.recordSummonSkippedBySpecial();

            // Transition to safe zones appearing phase
            witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.STATIONARY_SAFE_ZONES_APPEARING);
        } else {
            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                plugin.getLogger().warning("[Stationary Casting] Failed to start stationary casting attack");
            }
            witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.NONE);
        }
    }

    /**
     * Start to boss rising animation phase.
     */
    private void startBossRisingAnimation() {
        if (boss1Entity == null) return;
        
        Location bossLoc = boss1Entity.getLocation();
        World world = bossLoc.getWorld();
        
        // Create rising animation with attack state for proper boundary tracking
        currentRisingAnimation = new BossRisingAnimation(
            plugin,
            specialAttackConfig,
            boss1Entity,
            witherSkullAttackState
        );
        
        // Start animation
        currentRisingAnimation.startAnimation();
        
        // Play warning sound and effects
        world.playSound(bossLoc, Sound.ENTITY_WITHER_AMBIENT, 2.0f, 0.7f);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, bossLoc, 50, 1, 1, 1, 0.1);
        
        // Send warning message to nearby players
        MessageManager messageManager = plugin.getMessageManager();
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distance(bossLoc) < 50) {
                String warningMessage = messageManager.getMessage(p, "boss1.special_attack.warning");
                if (warningMessage.equals("boss1.special_attack.warning")) {
                    warningMessage = "§c§lБосс начинает особую атаку!"; // Fallback message
                }
                p.sendMessage(Component.text(warningMessage).color(NamedTextColor.DARK_RED));
            }
        }
        
        // The BossRisingAnimation now handles its own completion and state transition.
        // We just need to start it and let it run.
        // The completeAnimation() method will transition the state to CASTING_SKULLS.
        // A separate task will monitor for the CASTING_SKULLS state to begin the next phase.
        
        // Start a task to monitor for the transition to the CASTING_SKULLS phase
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss1Entity == null || boss1Entity.isDead() || !boss1Entity.isValid()) {
                    cancel();
                    return;
                }
                
                if (witherSkullAttackState != null && witherSkullAttackState.isCastingSkulls()) {
                    cancel();
                    startSkullCastingPhase(6, 8, 15.0, 1.0);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L); // Check every tick
    }
    
    /**
     * Start to skull casting phase with safe zones.
     */
    private void startSkullCastingPhase(int minSafeZones, int maxSafeZones, double radius, double protectionRadius) {
        if (boss1Entity == null) return;
        
        // Update attack state
        witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.GROUND_SKULL_GATHER);
        
        // Start the ground skull gather animation
        startGroundSkullGatherAnimation(minSafeZones, maxSafeZones, radius, protectionRadius);
    }

    /**
     * Starts the ground skull gather animation.
     */
    private void startGroundSkullGatherAnimation(int minSafeZones, int maxSafeZones, double radius, double protectionRadius) {
        if (boss1Entity == null) return;

        Location bossLoc = boss1Entity.getLocation();
        World world = bossLoc.getWorld();

        // Generate safe zones
        int safeZoneCount = minSafeZones + (int)(Math.random() * (maxSafeZones - minSafeZones + 1));
        currentSafeZones.clear();
        safeZoneManager.setBeaconEffectEnabled(false);
        for (int i = 0; i < safeZoneCount; i++) {
            SafeZone zone = SafeZone.generateRandom(bossLoc, radius, protectionRadius, 5);
            currentSafeZones.add(zone);
        }

        // Visual/audio warning
        world.playSound(bossLoc, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.8f);
        world.spawnParticle(Particle.DRAGON_BREATH, bossLoc, 30, 1, 1, 1, 0.1);

        // Send warning message
        MessageManager messageManager = plugin.getMessageManager();
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distance(bossLoc) < 50) {
                String warningMessage = messageManager.getMessage(p, "boss1.wither_skull_attack.warning");
                p.sendMessage(Component.text(warningMessage).color(NamedTextColor.DARK_PURPLE));
            }
        }

        new BukkitRunnable() {
            int ticks = 0;
            final int gatherDuration = specialAttackConfig.getGroundGatherDurationTicks(); // Configurable gather duration
            final int pauseDuration = specialAttackConfig.getGroundGatherPauseTicks(); // Configurable pause duration
            final int totalDuration = gatherDuration + pauseDuration;
            final double animRadius = 2.0;

            @Override
            public void run() {
                if (ticks >= totalDuration || boss1Entity == null || boss1Entity.isDead()) {
                    cancel();
                    startSphereAttackPhase();
                    return;
                }

                // Show safe zones
                for (SafeZone zone : currentSafeZones) {
                    safeZoneManager.spawnParticlesWithoutBeacon(zone, 20);
                }

                Location bossHeadLoc = boss1Entity.getEyeLocation();

                if (ticks < gatherDuration) {
                    // --- Gather Phase ---
                    double progress = (double) ticks / gatherDuration;
                    for (int i = 0; i <= 10; i++) {
                        double angle = Math.PI * (i / 10.0); // Lower semi-circle
                        double x = Math.cos(angle) * animRadius;
                        double z = Math.sin(angle) * animRadius;
                        
                        Location groundLoc = bossLoc.clone().add(x, 0, z);
                        Location targetLoc = bossHeadLoc.clone().add(x, 0, z);

                        Location particleLoc = groundLoc.clone().add(0, (targetLoc.getY() - groundLoc.getY()) * progress, 0);
                        
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0, 0, 0, 0.01);
                        world.spawnParticle(Particle.SMOKE, particleLoc, 1, 0, 0, 0, 0);
                    }
                } else {
                    // --- Pause Phase ---
                    for (int i = 0; i <= 10; i++) {
                        double angle = Math.PI * (i / 10.0); // Lower semi-circle
                        double x = Math.cos(angle) * animRadius;
                        double z = Math.sin(angle) * animRadius;
                        Location particleLoc = bossHeadLoc.clone().add(x, 0, z);
                        
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0.02);
                        world.spawnParticle(Particle.ENCHANT, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                    
                    world.spawnParticle(Particle.PORTAL, bossHeadLoc, 5, 0.5, 0.5, 0.5, 0.5);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
     /**
      * Start to sphere attack phase with projectiles.
      */
    private void startSphereAttackPhase() {
        if (boss1Entity == null) return;
        
        Location bossLoc = boss1Entity.getLocation();
        World world = bossLoc.getWorld();
        
        // Update attack state
        witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.SPHERE_ATTACK);
        
        // Get nearby players for targeting
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distance(bossLoc) < 50 &&
                (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)) {
                nearbyPlayers.add(p);
            }
        }
        
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            plugin.getLogger().info("[Special Attack] Starting sphere attack phase with " +
                specialAttackConfig.getProjectileCount() + " projectiles targeting " +
                nearbyPlayers.size() + " players");
        }
        
        // Start gather-to-boss choreography
        startSkullGatherPhase(bossLoc, nearbyPlayers);
    }
    
    /**
     * Start skull gather-to-boss choreography phase
     */
    private void startSkullGatherPhase(Location bossLoc, List<Player> nearbyPlayers) {
        World world = bossLoc.getWorld();
        
        // Generate ground spawn points around boss
        List<Location> groundSpawnPoints = generateGroundSpawnPoints(bossLoc, specialAttackConfig.getProjectileCount());
        
        // Create gather-to-boss projectiles
        activeProjectiles.clear();
        
        for (int i = 0; i < groundSpawnPoints.size(); i++) {
            Location groundPoint = groundSpawnPoints.get(i);
            
            try {
                // Create projectile that will gather to boss
                WitherSkullProjectile projectile = new WitherSkullProjectile(
                    plugin, specialAttackConfig, groundPoint, bossLoc.clone().add(0, 1.5, 0), 8.0, boss1Entity
                );
                
                if (projectile.getId() != null) {
                    activeProjectiles.add(projectile);
                    projectile.spawn(world);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error creating gather projectile " + i + ": " + e.getMessage());
            }
        }
        
        // Play gather start effects
        world.playSound(bossLoc, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.8f);
        world.spawnParticle(Particle.DRAGON_BREATH, bossLoc, 30, 1, 1, 1, 0.1);
        
        // Start projectile tracking and wait for gather completion
        startGatherPhaseTracking(bossLoc, nearbyPlayers);
    }
    
    /**
     * Generate ground spawn points for skull choreography
     */
    private List<Location> generateGroundSpawnPoints(Location center, int count) {
        List<Location> points = new ArrayList<>();
        double radius = 8.0; // Spawn radius around boss
        
        for (int i = 0; i < count; i++) {
            double angle = (i / (double) count) * 2 * Math.PI;
            double r = radius * (0.5 + Math.random() * 0.5); // Vary radius
            
            double x = center.getX() + r * Math.cos(angle);
            double z = center.getZ() + r * Math.sin(angle);
            
            // Find ground level
            Location groundPoint = new Location(center.getWorld(), x, center.getY(), z);
            
            // Go down to find solid ground
            for (int y = (int)center.getY(); y >= center.getY() - 5; y--) {
                groundPoint.setY(y);
                if (groundPoint.getBlock().getType().isSolid()) {
                    groundPoint.setY(y + 1); // Spawn just above ground
                    break;
                }
            }
            
            points.add(groundPoint);
        }
        
        return points;
    }
    
    /**
     * Track gather phase and transition to hemisphere release
     */
    private void startGatherPhaseTracking(Location bossLoc, List<Player> nearbyPlayers) {
        new BukkitRunnable() {
            int ticks = 0;
            final int maxGatherTicks = 60; // 3 seconds max gather time
            
            @Override
            public void run() {
                ticks++;
                
                // Check if all projectiles have reached boss area
                boolean allGathered = true;
                for (WitherSkullProjectile projectile : activeProjectiles) {
                    if (projectile.isValid() && projectile.isRisingPhase()) {
                        allGathered = false;
                        break;
                    }
                }
                
                if (allGathered || ticks >= maxGatherTicks) {
                    cancel();
                    startHemisphereReleasePhase(bossLoc, nearbyPlayers);
                    return;
                }
                
                // Remove invalid projectiles
                activeProjectiles.removeIf(p -> !p.isValid());
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
    
    /**
     * Start hemisphere release phase
     */
    private void startHemisphereReleasePhase(Location bossLoc, List<Player> nearbyPlayers) {
        World world = bossLoc.getWorld();
        
        // Generate hemisphere targets (downward pattern)
        List<Location> hemisphereTargets = generateHemisphereTargets(bossLoc, nearbyPlayers);
        
        // Update existing projectiles to attack hemisphere targets
        int targetIndex = 0;
        for (WitherSkullProjectile projectile : activeProjectiles) {
            if (projectile.isValid() && targetIndex < hemisphereTargets.size()) {
                projectile.setFinalTarget(hemisphereTargets.get(targetIndex));
                targetIndex++;
            }
        }
        
        // Play hemisphere release effects
        world.playSound(bossLoc, Sound.ENTITY_WITHER_SHOOT, 2.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION, bossLoc.clone().add(0, 1.5, 0), 10, 0.5, 0.5, 0.5, 0);
        
        // Start projectile tracking for completion
        startProjectileTrackingTask();
        
        // Schedule transition to cooldown phase
        new BukkitRunnable() {
            @Override
            public void run() {
                witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.COOLDOWN);
                
                // Clear safe zones after attack
                currentSafeZones.clear();
                
                // End attack after cooldown
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.NONE);
                        witherSkullAttackState.endAttack();
                        witherSkullAttackState.updateLastSpecialAttackTime();
                        
                        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                            plugin.getLogger().info("[Special Attack] Special attack completed and timing updated");
                        }
                    }
                }.runTaskLater(plugin, 60L); // 3 second cooldown
            }
        }.runTaskLater(plugin, 100L); // Start cooldown after 5 seconds
    }
    
    /**
     * Generate hemisphere release targets (downward pattern)
     */
    private List<Location> generateHemisphereTargets(Location center, List<Player> nearbyPlayers) {
        List<Location> targets = new ArrayList<>();
        
        if (specialAttackConfig.useSpherePattern() && !nearbyPlayers.isEmpty()) {
            // Generate hemisphere pattern targeting players
            targets = WitherSkullProjectile.generateSpherePatternTargets(
                center, nearbyPlayers, specialAttackConfig.getProjectileCount()
            );
            
            // Modify targets to create downward hemisphere
            for (Location target : targets) {
                Vector direction = target.toVector().subtract(center.toVector()).normalize();
                // Ensure downward component for hemisphere
                if (direction.getY() > -0.3) {
                    direction.setY(-0.3 - Math.random() * 0.4); // Downward bias
                }
                Location newTarget = center.clone().add(direction.multiply(15));
                target.setX(newTarget.getX());
                target.setY(newTarget.getY());
                target.setZ(newTarget.getZ());
            }
        } else {
            // Fallback to downward hemisphere pattern
            for (int i = 0; i < specialAttackConfig.getProjectileCount(); i++) {
                // Generate hemisphere points
                double phi = Math.acos(1 - 2 * (double) i / specialAttackConfig.getProjectileCount());
                double theta = Math.sqrt(specialAttackConfig.getProjectileCount() * Math.PI) * phi;
                
                // Only use lower hemisphere (Y <= 0)
                double y = -Math.abs(Math.cos(phi)); // Force negative Y for downward
                double x = Math.cos(theta) * Math.sin(phi);
                double z = Math.sin(theta) * Math.sin(phi);
                
                Vector direction = new Vector(x, y, z).normalize();
                Location target = center.clone().add(direction.multiply(15));
                targets.add(target);
            }
        }
        
        return targets;
    }
    
    /**
     * Track active projectiles and handle their lifecycle.
     */
    private void startProjectileTrackingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Remove completed projectiles
                activeProjectiles.removeIf(projectile -> !projectile.isValid());
               
                // Update remaining projectiles
                for (WitherSkullProjectile projectile : activeProjectiles) {
                    projectile.update();
                }
               
                // Stop tracking when all projectiles are done
                if (activeProjectiles.isEmpty()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L); // Update every tick
    }
    
    /**
     * Fire wither skulls in circular pattern.
     */
    private void fireWitherSkulls() {
        if (boss1Entity == null) return;
        
        Location bossLoc = boss1Entity.getLocation();
        World world = bossLoc.getWorld();
        double damageMultiplier = plugin.getConfig().getDouble("acts.boss1.witherSkullAttack.damageMultiplier", 2.0);
        
        // Fire 16 skulls in circle
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * 2 * Math.PI;
            Vector direction = new Vector(Math.cos(angle), 0.1, Math.sin(angle));
           
            WitherSkull skull = world.spawn(bossLoc.clone().add(0, 1.5, 0), WitherSkull.class);
            skull.setShooter(boss1Entity);
            skull.setDirection(direction);
            skull.setYield(0f); // No terrain damage
            skull.setIsIncendiary(false); // No fire
            skull.setCharged(false); // Not blue skull
        }
        
        world.playSound(bossLoc, Sound.ENTITY_WITHER_SHOOT, 2.0f, 1.0f);
        witherSkullAttackState.endAttack();
    }
    
    /**
     * Handle wither skull explosions - prevent block damage but allow player damage
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWitherSkullExplode(EntityExplodeEvent event) {
        // Only handle wither skull explosions from our boss
        if (!(event.getEntity() instanceof WitherSkull)) {
            return;
        }
        
        WitherSkull skull = (WitherSkull) event.getEntity();
        
        // Check if this skull was shot by our boss
        if (skull.getShooter() != boss1Entity) {
            return;
        }
        
        // Prevent all block damage
        event.blockList().clear();
        event.setCancelled(true);
        
        // Check if this is from a gather-phase projectile (should be harmless)
        boolean isFromGatherPhase = false;
        for (WitherSkullProjectile projectile : activeProjectiles) {
            if (projectile.getSkullEntity() == skull && projectile.isRisingPhase()) {
                isFromGatherPhase = true;
                break;
            }
        }
        
        // Skip damage if this is from gather phase
        if (isFromGatherPhase) {
            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                plugin.getLogger().info("[Skull Explosion] Skipping damage from gather-phase projectile");
            }
            return;
        }
        
        // Calculate manual player damage with safe zone check
        Location explosionLoc = event.getLocation();
        World world = explosionLoc.getWorld();
        double damageMultiplier = plugin.getConfig().getDouble("acts.boss1.witherSkullAttack.damageMultiplier", 2.0);
        double explosionRadius = 3.0; // Standard wither skull explosion radius
        
        for (Player player : world.getPlayers()) {
            double distance = player.getLocation().distance(explosionLoc);
            
            if (distance <= explosionRadius) {
                // Check if player is in a safe zone
                boolean inSafeZone = false;
                for (SafeZone zone : currentSafeZones) {
                    if (zone.contains(player.getLocation())) {
                        inSafeZone = true;
                        break;
                    }
                }
                
                // If not in safe zone, apply damage
                if (!inSafeZone) {
                    // Calculate damage based on distance (inverse square law)
                    double damageRatio = 1.0 - (distance / explosionRadius);
                    double baseDamage = 8.0; // Base wither skull damage
                    double damage = baseDamage * damageRatio * damageMultiplier;
                    
                    // Apply damage
                    player.damage(damage, skull);
                    
                    // Apply wither effect
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.WITHER,
                        40, // 2 seconds
                        1 // Level 2
                    ));
                }
            }
        }
    }
    
    @EventHandler
    public void onBoss1Damage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Skeleton)) {
            return;
        }

        Skeleton skeleton = (Skeleton) event.getEntity();
        if (skeleton != boss1Entity) {
            return;
        }

        // Debug: Log that damage event was received
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            plugin.getLogger().info("[BOSS DAMAGE] Damage event received! Damage: " + String.format("%.1f", event.getFinalDamage()) +
                ", Current HP: " + String.format("%.1f", skeleton.getHealth()));
        }
        
        // If boss takes damage from a player, break shields of all blocking players nearby
        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
            org.bukkit.event.entity.EntityDamageByEntityEvent damageEvent = (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
           
            // Check if damager is a player (or arrow from player)
            boolean isPlayerAttack = false;
            if (damageEvent.getDamager() instanceof Player) {
                isPlayerAttack = true;
            } else if (damageEvent.getDamager() instanceof org.bukkit.entity.AbstractArrow) {
                org.bukkit.entity.AbstractArrow arrow = (org.bukkit.entity.AbstractArrow) damageEvent.getDamager();
                if (arrow.getShooter() instanceof Player) {
                    isPlayerAttack = true;
                }
            }
           
            // If player attacked boss, break shields of all blocking players nearby
            if (isPlayerAttack) {
                for (Player player : skeleton.getWorld().getPlayers()) {
                    if (player.getLocation().distance(skeleton.getLocation()) < 15 && player.isBlocking()) {
                        // Break shield in main hand or offhand
                        ItemStack mainHand = player.getInventory().getItemInMainHand();
                        ItemStack offHand = player.getInventory().getItemInOffHand();
                       
                        if (mainHand.getType() == Material.SHIELD) {
                            mainHand.setAmount(0);
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                            player.getWorld().spawnParticle(Particle.ITEM, player.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.1, mainHand);
                            String shieldBreakMsg = plugin.getMessageManager().getMessage(player, "boss1.shield_break");
                            player.sendMessage(Component.text(shieldBreakMsg).color(NamedTextColor.RED));
                        } else if (offHand.getType() == Material.SHIELD) {
                            offHand.setAmount(0);
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                            player.getWorld().spawnParticle(Particle.ITEM, player.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.1, offHand);
                            String shieldBreakMsg = plugin.getMessageManager().getMessage(player, "boss1.shield_break");
                            player.sendMessage(Component.text(shieldBreakMsg).color(NamedTextColor.RED));
                        }
                    }
                }
            }
        }
        
        // Check for phase 2 transition at 100 HP
        double currentHealth = skeleton.getHealth();
        double healthAfterDamage = currentHealth - event.getFinalDamage();

        // Debug logging for boss health
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            plugin.getLogger().info("[BOSS HEALTH] Current HP: " + String.format("%.1f", currentHealth) +
                ", Damage: " + String.format("%.1f", event.getFinalDamage()) +
                ", HP after damage: " + String.format("%.1f", healthAfterDamage) +
                ", Phase: " + boss1Phase);
        }

        if (boss1Phase == 1 && healthAfterDamage <= 100) {
            plugin.getLogger().info("[BOSS PHASE] Transitioning to Phase 2! HP: " + String.format("%.1f", healthAfterDamage));
            transitionToPhase2(skeleton);
        }
        
        // Update boss bar immediately on damage
        if (boss1BossBar != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (boss1Entity != null && !boss1Entity.isDead()) {
                        double health = boss1Entity.getHealth() - event.getFinalDamage();
                        double maxHealth = boss1Entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        float progress = (float) Math.max(0, health / maxHealth);
                        boss1BossBar.progress(progress);
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }
    
    @EventHandler
    public void onBoss1AttackPlayer(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        // Check if boss attacks player
        if (!(event.getDamager() instanceof Skeleton)) {
            return;
        }
        
        Skeleton skeleton = (Skeleton) event.getDamager();
        if (skeleton != boss1Entity) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Check if player is 2+ blocks above boss (NO sneaking protection)
        double heightDifference = player.getLocation().getY() - skeleton.getLocation().getY();
        if (heightDifference >= 2.0) {
            // Knockback player away from boss
            Vector direction = player.getLocation().toVector().subtract(skeleton.getLocation().toVector()).normalize();
            direction.setY(0.8); // Strong upward component
            direction.multiply(2.5); // Strong knockback
           
            player.setVelocity(direction);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.8f);
            player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
            String knockbackMsg = plugin.getMessageManager().getMessage(player, "boss1.knockback");
            player.sendMessage(Component.text(knockbackMsg).color(NamedTextColor.RED));
        }
    }
    
    @EventHandler
    public void onWarriorAttackWarrior(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        // Prevent boss warriors from attacking each other (including arrow hits!)
        if (!(event.getEntity() instanceof Skeleton)) {
            return;
        }
        
        Skeleton victim = (Skeleton) event.getEntity();
        Skeleton attacker = null;
        
        // Check if damager is skeleton directly or arrow from skeleton
        if (event.getDamager() instanceof Skeleton) {
            attacker = (Skeleton) event.getDamager();
        } else if (event.getDamager() instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow) event.getDamager();
            if (arrow.getShooter() instanceof Skeleton) {
                attacker = (Skeleton) arrow.getShooter();
            }
        }
        
        // If no skeleton attacker found, ignore
        if (attacker == null) {
            return;
        }
        
        // Check if both are boss warriors
        boolean victimIsWarrior = victim.getCustomName() != null && 
                                 (victim.getCustomName().contains("Воин Повелителя") || 
                                   victim.getCustomName().contains("Лучник Повелителя"));
        boolean attackerIsWarrior = attacker.getCustomName() != null && 
                                    (attacker.getCustomName().contains("Воин Повелителя") || 
                                     attacker.getCustomName().contains("Лучник Повелителя"));
        
        // Also check if one is the boss
        boolean victimIsBoss = victim == boss1Entity;
        boolean attackerIsBoss = attacker == boss1Entity;
        
        // Cancel damage if both are warriors, or if boss attacks warrior, or warrior attacks boss
        if ((victimIsWarrior && attackerIsWarrior) || 
            (victimIsBoss && attackerIsWarrior) || 
            (victimIsWarrior && attackerIsBoss)) {
            event.setCancelled(true);
           
            // Redirect attacker to nearest player
            Player nearestPlayer = null;
            double minDistance = Double.MAX_VALUE;
           
            for (Player player : attacker.getWorld().getPlayers()) {
                if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                    double distance = player.getLocation().distance(attacker.getLocation());
                    if (distance < 50 && distance < minDistance) {
                        minDistance = distance;
                        nearestPlayer = player;
                    }
                }
            }
           
            if (nearestPlayer != null) {
                attacker.setTarget(nearestPlayer);
            }
        }
    }
    
    @EventHandler
    public void onBoss1ArrowDeflect(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        // Check if boss is hit by arrow
        if (!(event.getEntity() instanceof Skeleton)) {
            return;
        }
        
        Skeleton skeleton = (Skeleton) event.getEntity();
        if (skeleton.getCustomName() == null || !skeleton.getCustomName().contains("Повелитель Скелетов")) {
            return;
        }
        
        // Check if damage is from arrow
        if (!(event.getDamager() instanceof AbstractArrow)) {
            return;
        }
        
        AbstractArrow arrow = (AbstractArrow) event.getDamager();
        
        // Check if arrow was shot by player
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }
        
        Player shooter = (Player) arrow.getShooter();
        UUID shooterId = shooter.getUniqueId();
        
        // Track arrow count for this player
        int arrowCount = playerArrowsShot.getOrDefault(shooterId, 0) + 1;
        playerArrowsShot.put(shooterId, arrowCount);
        
        // Every 3rd arrow is deflected
        if (arrowCount % 3 == 0) {
            event.setCancelled(true);
           
            // Deflect arrow back at shooter
            Vector direction = shooter.getLocation().toVector().subtract(skeleton.getLocation().toVector()).normalize();
            direction.multiply(2.0); // Fast deflection
           
            arrow.setVelocity(direction);
            arrow.setShooter(skeleton); // Boss becomes the shooter
            // setBounce is deprecated, but we need to keep it for compatibility
            // arrow.setBounce(false); // Deprecated method removed
           
            // Epic deflection effects
            World world = skeleton.getWorld();
            Location arrowLoc = arrow.getLocation();
           
            world.spawnParticle(Particle.ENCHANTED_HIT, arrowLoc, 30, 0.3, 0.3, 0.3, 0.1);
            world.spawnParticle(Particle.CRIT, arrowLoc, 20, 0.2, 0.2, 0.2, 0.1);
            world.spawnParticle(Particle.FLASH, arrowLoc, 1, 0, 0, 0);
           
            world.playSound(arrowLoc, Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.5f);
            world.playSound(arrowLoc, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.7f);
           
            plugin.getLogger().info("Boss deflected arrow #" + arrowCount + " from " + shooter.getName());
        }
    }
    
    @EventHandler
    public void onArcherHitPlayer(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        // Check if arrow hit player in phase 2
        if (!(event.getDamager() instanceof AbstractArrow)) {
            return;
        }
        
        AbstractArrow arrow = (AbstractArrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Skeleton)) {
            return;
        }
        
        Skeleton shooter = (Skeleton) arrow.getShooter();
        
        // Check if shooter is an archer warrior in phase 2
        if (boss1Phase != 2) {
            return;
        }
        
        if (shooter.getCustomName() == null || !shooter.getCustomName().contains("Лучник Повелителя")) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Apply nausea effect from config - Level 127 for 10 seconds (200 ticks)
        int nauseaLevel = plugin.getConfig().getInt("phase2.archerNausea.level", 127);
        int nauseaDurationSeconds = plugin.getConfig().getInt("phase2.archerNausea.durationSeconds", 10);
        int nauseaDurationTicks = nauseaDurationSeconds * 20;

        player.addPotionEffect(new PotionEffect(
            PotionEffectType.NAUSEA,
            nauseaDurationTicks,
            nauseaLevel - 1, // Amplifier is level - 1 (e.g., level 127 = amplifier 126)
            false,
            true // Show particles
        ));

        // Apply poison effect - Level 1 for 3 seconds (60 ticks)
        int poisonLevel = 1;
        int poisonDurationSeconds = 3;
        int poisonDurationTicks = poisonDurationSeconds * 20;

        player.addPotionEffect(new PotionEffect(
            PotionEffectType.POISON,
            poisonDurationTicks,
            poisonLevel - 1, // Amplifier is level - 1 (level 1 = amplifier 0)
            false,
            true // Show particles
        ));

            }

    /**
     * Handle special bow effects when player hits entities
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSpecialArrowHit(EntityDamageByEntityEvent event) {
        // Check if target is player
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }

        // Try to find the shooter
        Player shooter = null;

        // Check if damager is player (melee attack with bow)
        if (event.getDamager() instanceof Player player) {
            shooter = player;
        }
        // Check if damager is arrow
        else if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player player) {
            shooter = player;
        }
        // Check if damager is spectral arrow or other projectile
        else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            shooter = player;
        }

        if (shooter == null) {
            return;
        }

        // Check if shooter has special bow in main hand OR off hand
        boolean hasSpecialBow = isSpecialArcherBow(shooter.getInventory().getItemInMainHand()) ||
                              isSpecialArcherBow(shooter.getInventory().getItemInOffHand());

        if (!hasSpecialBow) {
            return;
        }

        // Apply effects to target
        applySpecialBowEffects(target);

        // Add visual effects
        target.getWorld().spawnParticle(Particle.ENCHANT, target.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 0.8f, 1.2f);

            }

    /**
     * Check if bow is the special archer bow
     */
    private boolean isSpecialArcherBow(ItemStack bow) {
        if (bow == null || !bow.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = bow.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        // Check display name
        if (!meta.getDisplayName().equals("§5Токсичный Лук Повелителя")) {
            return false;
        }

        // Check persistent data
        NamespacedKey key = new NamespacedKey(plugin, "special_archer_bow");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(key, PersistentDataType.BYTE);
    }

    /**
     * Apply special bow effects to target
     */
    private void applySpecialBowEffects(Player target) {
        // Apply nausea effect (Level 127 for 10 seconds)
        target.addPotionEffect(new PotionEffect(
            PotionEffectType.NAUSEA,
            200, // 10 seconds
            126, // Level 127 (amplifier 126)
            false,
            true
        ));

        // Apply poison effect (Level 1 for 3 seconds)
        target.addPotionEffect(new PotionEffect(
            PotionEffectType.POISON,
            60, // 3 seconds
            0, // Level 1 (amplifier 0)
            false,
            true
        ));

        // Send message to target
        target.sendActionBar(Component.text("§cВы поражены токсичным ядом Повелителя!").decoration(TextDecoration.ITALIC, false));
    }

    private void startBoss1SummonTask(Skeleton boss) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    cancel();
                    return;
                }
               
                // Check if special attack is active and skeleton spawning is prevented
                if (specialAttackConfig != null &&
                    specialAttackConfig.shouldPreventSkeletonSpawn() &&
                    witherSkullAttackState != null &&
                    witherSkullAttackState.getSpecialAttackPhase() != BossAttackState.SpecialAttackPhase.NONE) {
                    
                    // Record that summon was skipped due to special attack
                    witherSkullAttackState.recordSummonSkippedBySpecial();
                    
                    if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                        plugin.getLogger().info("[Summon Task] Skipping skeleton spawn due to active special attack (recorded for cadence)");
                    }
                    return;
                }
               
                // Summon 2-3 warrior skeletons
                for (int i = 0; i < 2 + (int)(Math.random() * 2); i++) {
                    // Find safe spawn location
                    Location summonLoc = findSafeSpawnLocation(boss.getLocation(), 4);
                   
                    if (summonLoc == null) {
                        continue; // Skip if no safe location found
                    }
                   
                    Skeleton warrior = (Skeleton) boss.getWorld().spawnEntity(summonLoc, EntityType.SKELETON);
                    warrior.setCustomName("§6Воин Повелителя");
                    warrior.setCustomNameVisible(true);
                   
                    // Phase 1: Sword warriors
                    if (boss1Phase == 1) {
                        warrior.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                        warrior.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        warrior.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));

                        // Remove all drops
                        warrior.getEquipment().setHelmetDropChance(0.0f);
                        warrior.getEquipment().setChestplateDropChance(0.0f);
                        warrior.getEquipment().setItemInMainHandDropChance(0.0f);
                    } else {
                        // Phase 2: Archer warriors
                        warrior.setCustomName("§6Лучник Повелителя");
                        warrior.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                        warrior.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

                        // Create special bow with lore and effects
                        ItemStack bow = createSpecialArcherBow();
                        warrior.getEquipment().setItemInMainHand(bow);

                        // Remove all drops except bow (for special bow chance)
                        warrior.getEquipment().setHelmetDropChance(0.0f);
                        warrior.getEquipment().setChestplateDropChance(0.0f);
                        warrior.getEquipment().setItemInMainHandDropChance(0.05f); // 5% chance for special bow
                    }
                   
                    // T029/T031: Remove wolves' fear mechanic from summoned skeleton warriors
                    // Wolf elimination removed - using Arrow Rain Phase 2 attack instead
                   
                    warrior.setTarget(boss.getTarget());
                    boss1Warriors.add(warrior.getUniqueId());
                    warriorSkeletons.add(warrior);
                }
               
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_SKELETON_AMBIENT, 1.0f, 0.5f);
                    boss.getWorld().spawnParticle(Particle.SOUL, boss.getLocation(), 20, 1, 1, 1, 0.05);

                    // Record complete warrior summon wave for hemisphere attack tracking
                    if (witherSkullAttackState != null) {
                        witherSkullAttackState.recordSuccessfulWarriorSummonWave();

                        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                            plugin.getLogger().info("[Summon Task] Recorded complete warrior summon wave (wave #" +
                                witherSkullAttackState.getWarriorSummonWaveCount() + ") for hemisphere attack");
                        }
                    }
            }
        }.runTaskTimer(plugin, 200L, 200L); // Every 10 seconds
    }
    
    private void transitionToPhase2(Skeleton boss) {
        boss1Phase = 2;
        
        // Remove chest and leggings only, keep helmet and boots
        boss.getEquipment().setChestplate(null);
        boss.getEquipment().setLeggings(null);
        
        // Keep helmet and boots but make them not drop
        boss.getEquipment().setHelmetDropChance(0.0f);
        boss.getEquipment().setBootsDropChance(0.0f);
        
        // Give enchanted bow with Power 4 and Punch
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.POWER, 4); // Changed from 3 to 4
        bow.addEnchantment(Enchantment.PUNCH, 1); // Punch = knockback для луков
        boss.getEquipment().setItemInMainHand(bow);
        boss.getEquipment().setItemInMainHandDropChance(0.0f); // Bow doesn't drop
        
        // Add Speed 2, Resistance 2, and Fire Resistance
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false)); // Resistance II
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        
        // Increase movement speed
        boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        
        // Update boss bar
        if (boss1BossBar != null) {
            boss1BossBar.name(Component.text("⚔ Повелитель Скелетов - ФАЗА 2 ⚔").color(NamedTextColor.DARK_RED));
            boss1BossBar.color(BossBar.Color.PURPLE);
        }
        
        // Convert all living warriors to archers
        for (UUID warriorId : boss1Warriors) {
            Entity entity = boss.getWorld().getEntity(warriorId);
            if (entity instanceof Skeleton warrior && warrior.isValid() && !warrior.isDead()) {
                warrior.setCustomName("§6Лучник Повелителя");
                warrior.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
               
                ItemStack warriorBow = new ItemStack(Material.BOW);
                warriorBow.addEnchantment(Enchantment.POWER, 2);
                warrior.getEquipment().setItemInMainHand(warriorBow);
            }
        }
        
        // Effects
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
        boss.getWorld().spawnParticle(Particle.PORTAL, boss.getLocation(), 200, 1, 2, 1, 1);
        boss.getWorld().spawnParticle(Particle.FLAME, boss.getLocation(), 100, 1, 1, 1, 0.1);
        
        // Notify nearby players
        for (Player player : boss.getWorld().getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) < 100) {
                String phase2Msg = plugin.getMessageManager().getMessage(player, "boss1.phase2");
                player.sendMessage(Component.text(phase2Msg).color(NamedTextColor.DARK_PURPLE));
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.5f);
            }
        }

        // Reset attack state to clean up Phase 1 attacks
        if (witherSkullAttackState != null) {
            witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.NONE);
            witherSkullAttackState.endAttack();
        }
    }

    
    private Location findSafeSpawnLocation(Location center, double radius) {
        World world = center.getWorld();
        
        // Try 10 times to find a safe location
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;
           
            double x = center.getX() + Math.cos(angle) * distance;
            double z = center.getZ() + Math.sin(angle) * distance;
           
            // Find highest solid block at this position
            Location testLoc = new Location(world, x, center.getY(), z);
           
            // Check blocks from boss Y level down to 10 blocks below
            for (int y = (int)center.getY(); y >= (int)center.getY() - 10; y--) {
                testLoc.setY(y);
                Block block = testLoc.getBlock();
                Block above = block.getRelative(0, 1, 0);
                Block above2 = block.getRelative(0, 2, 0);
               
                // Check if this is a valid spawn location:
                // 1. Block below is solid
                // 2. Two blocks above are air (enough space for skeleton)
                if (block.getType().isSolid() && 
                    above.getType().isAir() && 
                    above2.getType().isAir()) {
                    // Found safe location
                    return above.getLocation().add(0.5, 0, 0.5); // Center of block
                }
            }
        }
        
        // If no safe location found, return original center location
        return center.clone().add(0, 1, 0);
    }
    
    private void startBoss1AITask(Skeleton boss) {
        bossAITask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    cancel();
                    return;
                }
               
                // Only manage distance in phase 2
                if (boss1Phase != 2) {
                    return;
                }
               
                // Find nearest player
                Player nearestPlayer = null;
                double minDistance = Double.MAX_VALUE;
               
                for (Player player : boss.getWorld().getPlayers()) {
                    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                        double distance = player.getLocation().distance(boss.getLocation());
                        if (distance < 50 && distance < minDistance) {
                            minDistance = distance;
                            nearestPlayer = player;
                        }
                    }
                }
               
                if (nearestPlayer == null) {
                    return;
                }
               
                // Keep distance between 5-10 blocks
                if (minDistance < 5.0) {
                    // Too close, move away
                    Vector direction = boss.getLocation().toVector().subtract(nearestPlayer.getLocation().toVector()).normalize();
                    Location targetLoc = boss.getLocation().add(direction.multiply(2));
                    boss.getPathfinder().moveTo(targetLoc);
                } else if (minDistance > 10.0) {
                    // Too far, move closer
                    boss.getPathfinder().moveTo(nearestPlayer.getLocation(), 0.8);
                }
            }
        }.runTaskTimer(plugin, 20L, 10L); // Check every 0.5 seconds
    }
    
    // ==========================================
    // BUG #2 FIX: BOSS PROXIMITY TELEPORT MECHANIC
    // ==========================================
    
    /**
     * Start proximity teleport mechanic for Boss #1 Phase 2
     * Teleports boss behind player if they stay within 3 blocks for 3+ seconds
     */
    private void startBoss1ProximityTeleportTask(Skeleton boss) {
        // Get config values
        double proximityDistance = plugin.getConfig().getDouble("acts.boss1.teleport.proximityDistance", 3.0);
        long triggerDelay = plugin.getConfig().getLong("acts.boss1.teleport.triggerDelaySeconds", 3) * 1000; // Convert to ms
        double behindDistance = plugin.getConfig().getDouble("acts.boss1.teleport.behindDistance", 4.0);
        long cooldownTime = plugin.getConfig().getLong("acts.boss1.teleport.cooldownSeconds", 10) * 1000; // Convert to ms
        int checkInterval = plugin.getConfig().getInt("acts.boss1.teleport.checkIntervalTicks", 20);
        
        proximityTeleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    cancel();
                    // Cleanup tracking maps
                    playerProximityStart.clear();
                    teleportCooldown.clear();
                    return;
                }
               
                // Only in Phase 2
                if (boss1Phase != 2) {
                    if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                        plugin.getLogger().info("[DEBUG] Proximity check (Phase " + boss1Phase + "): skipping - only Phase 2 teleports");
                    }
                    return;
                }
               
                // Check all nearby players
                for (Player player : boss.getWorld().getPlayers()) {
                    if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                        continue;
                    }
                   
                    UUID playerId = player.getUniqueId();
                    double distance = player.getLocation().distance(boss.getLocation());
                    long currentTime = System.currentTimeMillis();
                   
                    // Check if player is within proximity distance
                    if (distance <= proximityDistance) {
                        // Start tracking if not already tracking
                        playerProximityStart.putIfAbsent(playerId, currentTime);
                       
                        long proximityDuration = currentTime - playerProximityStart.get(playerId);
                       
                        // Check if player has been close long enough
                        if (proximityDuration >= triggerDelay) {
                            // Check cooldown
                            Long lastTeleport = teleportCooldown.get(playerId);
                            if (lastTeleport == null || (currentTime - lastTeleport) >= cooldownTime) {
                                // Attempt teleport
                                if (teleportBossBehindPlayer(boss, player, behindDistance)) {
                                    if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                                        plugin.getLogger().info("[DEBUG] Proximity teleport triggered (Phase 2) for player: " + player.getName());
                                    }
                                    // Reset tracking
                                    playerProximityStart.remove(playerId);
                                    teleportCooldown.put(playerId, currentTime);
                                }
                            }
                        }
                    } else {
                        // Player moved away, reset tracking
                        playerProximityStart.remove(playerId);
                    }
                }
            }
        }.runTaskTimer(plugin, checkInterval, checkInterval);
    }
    
    /**
     * Calculate location behind player
     */
    private Location calculateBehindLocation(Player player, double distance) {
        // Get player's facing direction
        Vector direction = player.getLocation().getDirection();
        
        // Reverse direction (behind player)
        direction.multiply(-1);
        
        // Find location behind player
        Location behindLoc = player.getLocation().add(direction.multiply(distance));
        
        // Set to player's eye level
        behindLoc.setY(player.getEyeLocation().getY());
        
        return behindLoc;
    }
    
    /**
     * Check if location is safe for boss teleport
     */
    private boolean isSafeLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }
        
        // Check block at location is air
        if (!loc.getBlock().getType().isAir()) {
            return false;
        }
        
        // Check block above is air (2 blocks height for skeleton)
        if (!loc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }
        
        // Check block below is solid
        if (!loc.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
            return false;
        }
        
        // Check not in lava/water
        Material below = loc.clone().add(0, -1, 0).getBlock().getType();
        if (below == Material.LAVA || below == Material.WATER) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Teleport boss behind player with safety checks
     * @return true if teleport successful
     */
    private boolean teleportBossBehindPlayer(Skeleton boss, Player player, double distance) {
        // Try direct behind location
        Location behindLoc = calculateBehindLocation(player, distance);
        
        if (isSafeLocation(behindLoc)) {
            return executeBossTeleport(boss, player, behindLoc);
        }
        
        // Try angles (45, 90, 135, 180, 225, 270, 315 degrees)
        for (int angle = 45; angle < 360; angle += 45) {
            double radians = Math.toRadians(angle);
            Vector offset = new Vector(
                Math.cos(radians) * distance,
                0,
                Math.sin(radians) * distance
            );
           
            Location testLoc = player.getLocation().add(offset);
            testLoc.setY(player.getEyeLocation().getY());
           
            if (isSafeLocation(testLoc)) {
                return executeBossTeleport(boss, player, testLoc);
            }
        }
        
        // Always find a surface location for teleportation
        Location surfaceLoc = findSurfaceTeleportLocation(player.getLocation(), distance);
        if (surfaceLoc != null) {
            return executeBossTeleport(boss, player, surfaceLoc);
        }

        // Last resort: teleport to player eye level directly
        Location lastResort = player.getEyeLocation().clone();
        lastResort.add(player.getLocation().getDirection().multiply(-3)); // Behind player
        return executeBossTeleport(boss, player, lastResort);
    }

    /**
     * Find a suitable surface teleport location
     */
    private Location findSurfaceTeleportLocation(Location playerLoc, double distance) {
        World world = playerLoc.getWorld();

        // Try different locations around player
        for (int angle = 0; angle < 360; angle += 30) {
            double radians = Math.toRadians(angle);
            Vector offset = new Vector(
                Math.cos(radians) * distance,
                0,
                Math.sin(radians) * distance
            );

            Location testLoc = playerLoc.clone().add(offset);

            // Find highest solid block (surface)
            Location surface = findSurfaceLocation(world, testLoc.getX(), testLoc.getZ());
            if (surface != null) {
                return surface;
            }
        }

        return null;
    }

    /**
     * Find the highest solid block (surface) at given x,z coordinates
     */
    private Location findSurfaceLocation(World world, double x, double z) {
        // Start from a high point and go down - search from reasonable surface level
        int startY = Math.min(world.getMaxHeight() - 1, 200); // Search from height 200 for better surface detection
        int surfaceY = -1;
        int airColumnHeight = 0;

        for (int y = startY; y >= world.getMinHeight(); y--) {
            Location testLoc = new Location(world, x, y, z);
            Material blockType = testLoc.getBlock().getType();

            if (blockType.isAir()) {
                // Count air blocks to find surface level
                airColumnHeight++;
            } else if (blockType.isSolid()) {
                // Check if this is actually the surface (not underground)
                if (airColumnHeight >= 2 && surfaceY == -1) { // At least 2 air blocks above indicates surface
                    // Found ground level, make sure we have enough space above
                    Location above = testLoc.clone().add(0, 1, 0);
                    Location twoAbove = testLoc.clone().add(0, 2, 0);

                    if (above.getBlock().getType().isAir() && twoAbove.getBlock().getType().isAir()) {
                        surfaceY = above.getBlockY();
                        break; // Found suitable surface location
                    }
                }
                // Reset air counter for underground detection
                airColumnHeight = 0;
            }
        }

        if (surfaceY != -1) {
            // Ensure the found surface is not underground (avoid Y < 60 for overworld surface)
            if (world.getEnvironment() == World.Environment.NORMAL && surfaceY < 50) {
                plugin.getLogger().warning("[TELEPORT] Found surface at Y=" + surfaceY + " seems too low, searching higher...");
                return findHigherSurfaceLocation(world, x, z);
            }
            return new Location(world, x, surfaceY, z);
        }

        // Fallback: try to find any location with air above solid block
        for (int y = startY; y >= Math.max(world.getMinHeight(), 50); y--) { // Don't go below Y=50 for surface
            Location testLoc = new Location(world, x, y, z);
            if (testLoc.getBlock().getType().isSolid()) {
                Location above = testLoc.clone().add(0, 1, 0);
                if (above.getBlock().getType().isAir()) {
                    Location twoAbove = testLoc.clone().add(0, 2, 0);
                    if (twoAbove.getBlock().getType().isAir()) {
                        return above;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Helper method to find higher surface location
     */
    private Location findHigherSurfaceLocation(World world, double x, double z) {
        // Search in a reasonable surface range (Y=60 to Y=120 for overworld)
        for (int y = 120; y >= 60; y--) {
            Location testLoc = new Location(world, x, y, z);
            if (testLoc.getBlock().getType().isSolid()) {
                Location above = testLoc.clone().add(0, 1, 0);
                if (above.getBlock().getType().isAir()) {
                    Location twoAbove = testLoc.clone().add(0, 2, 0);
                    if (twoAbove.getBlock().getType().isAir()) {
                        plugin.getLogger().info("[TELEPORT] Found higher surface at Y=" + above.getBlockY());
                        return above;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Execute actual teleport with effects
     */
    private boolean executeBossTeleport(Skeleton boss, Player player, Location destination) {
        World world = boss.getWorld();
        
        // Teleport boss
        boss.teleport(destination);
        
        // Effects at old location
        Location oldLoc = boss.getLocation();
        world.spawnParticle(Particle.PORTAL, oldLoc, 50, 0.5, 0.5, 0.5, 0.3);
        world.playSound(oldLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        
        // Effects at new location
        world.spawnParticle(Particle.PORTAL, destination, 50, 0.5, 0.5, 0.5, 0.3);
        world.spawnParticle(Particle.SMOKE, destination, 20, 0.3, 0.3, 0.3, 0.05);
        world.playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        
        // Message to player
        player.sendMessage(Component.text(plugin.getMessageManager().getMessage(player, "boss.teleported")));
        
        // Debug logging
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            plugin.getLogger().info("Boss #1 teleported behind " + player.getName() + " to " + 
                destination.getBlockX() + "," + destination.getBlockY() + "," + destination.getBlockZ());
        }
        
        return true;
    }
    
    private void startBoss1AntiWallTask(Skeleton boss) {
        antiWallTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    cancel();
                    return;
                }
               
                Location bossLoc = boss.getLocation();
                World world = boss.getWorld();
                int radius = 3;
                int blockCount = 0;
                List<Block> blocksToDestroy = new ArrayList<>();
               
                // Подсчитываем блоки вокруг босса
                for (int x = -radius; x <= radius; x++) {
                    for (int y = 0; y <= 3; y++) { // От ног до головы
                        for (int z = -radius; z <= radius; z++) {
                            if (x == 0 && z == 0) continue; // Пропускаем центр (босса)
                           
                            Location checkLoc = bossLoc.clone().add(x, y, z);
                            Block block = checkLoc.getBlock();
                            Material type = block.getType();
                           
                            // ВАЖНО: Считаем ТОЛЬКО блоки, поставленные игроками!
                            if (type.isSolid() && 
                                type != Material.BEDROCK &&
                                type != Material.END_STONE &&
                                type != Material.OBSIDIAN &&
                                !type.toString().contains("NETHER") &&
                                PlayerPlacedBlocksManager.isPlayerPlaced(block)) { // <-- Проверка!
                               
                                blockCount++;
                                blocksToDestroy.add(block);
                            }
                        }
                    }
                }
               
                // Логируем для отладки
                if (blockCount > 0) {
                    plugin.getLogger().info("Boss1 anti-wall check: " + blockCount + " player-placed blocks found around boss");
                }
               
                // Если больше 8 блоков вокруг босса - игрок пытается застроить!
                if (blockCount > 8) {
                    plugin.getLogger().info("Boss1 RAGE ACTIVATED! Player-placed blocks around: " + blockCount);
                   
                    // ЯРОСТЬ БОССА!
                    world.playSound(bossLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.5f);
                    world.playSound(bossLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
                   
                    // Сообщение всем игрокам
                    for (Player player : world.getPlayers()) {
                        if (player.getLocation().distance(bossLoc) < 50) {
                            String wallRageMsg = plugin.getMessageManager().getMessage(player, "boss1.wall_rage");
                            player.sendMessage(Component.text(wallRageMsg).color(NamedTextColor.DARK_RED));
                        }
                    }
                   
                    // Взрываем блоки с эффектами (без разрушения местности)
                    for (Block block : blocksToDestroy) {
                        Location blockLoc = block.getLocation().add(0.5, 0.5, 0.5);
                       
                        // Мощные партиклы взрыва
                        world.spawnParticle(Particle.EXPLOSION, blockLoc, 3, 0.1, 0.1, 0.1, 0);
                        world.spawnParticle(Particle.FLAME, blockLoc, 20, 0.3, 0.3, 0.3, 0.1);
                        world.spawnParticle(Particle.SMOKE, blockLoc, 15, 0.2, 0.2, 0.2, 0.05);
                        world.spawnParticle(Particle.LAVA, blockLoc, 5, 0.2, 0.2, 0.2, 0);
                       
                        // Партиклы разрушения блока
                        world.spawnParticle(Particle.BLOCK, blockLoc, 30, 0.3, 0.3, 0.3, 0.5, block.getType().createBlockData());
                       
                        // Убираем блок из трекинга
                        PlayerPlacedBlocksManager.removeBlock(block);
                       
                        // Уничтожаем блок
                        block.setType(Material.AIR);
                    }
                   
                    // Дополнительный звук взрыва в центре
                    world.playSound(bossLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
                   
                    // ЭФФЕКТЫ ЯРОСТИ НА БОССА
                    boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1)); // 10 секунд Speed II
                    boss.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 0)); // 10 секунд Strength I
                    boss.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1)); // 5 секунд Resistance II
                   
                    // Партиклы ярости вокруг босса
                    world.spawnParticle(Particle.ANGRY_VILLAGER, bossLoc.clone().add(0, 2, 0), 20, 0.5, 0.5, 0.5, 0);
                    world.spawnParticle(Particle.LAVA, bossLoc.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);
                   
                    // НАКАЗАНИЕ ИГРОКОВ ПОБЛИЗОСТИ
                    for (Player player : world.getPlayers()) {
                        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                            continue;
                        }
                       
                        double distance = player.getLocation().distance(bossLoc);
                        if (distance < 8.0) {
                            // Слабость и замедление
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1)); // 10 секунд
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0)); // 5 секунд
                           
                            // Небольшой откат
                            Vector direction = player.getLocation().toVector().subtract(bossLoc.toVector()).normalize();
                            direction.setY(0.5);
                            direction.multiply(1.5);
                            player.setVelocity(direction);
                           
                            String wrathMsg = plugin.getMessageManager().getMessage(player, "boss1.wall_rage_feel");
                            player.sendMessage(Component.text(wrathMsg).color(NamedTextColor.RED));
                           
                            // Звук удара
                            world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 0.8f);
                        }
                    }
                   
                    // Кулдаун 10 секунд перед следующей проверкой
                    cancel();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!boss.isDead() && boss.isValid()) {
                                startBoss1AntiWallTask(boss);
                            }
                        }
                    }.runTaskLater(plugin, 200L); // 10 секунд
                }
            }
        }.runTaskTimer(plugin, 40L, 40L); // Проверка каждые 2 секунды (40 тиков)
    }
    
    private void startBoss1HeightCheckTask(Skeleton boss) {
        heightCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    playersAboveBoss.clear();
                    cancel();
                    return;
                }
               
                long currentTime = System.currentTimeMillis();
                Location bossHeadLoc = boss.getEyeLocation();
               
                for (Player player : boss.getWorld().getPlayers()) {
                    if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                        continue;
                    }
                   
                    // Calculate horizontal distance (without Y coordinate)
                    Location bossLoc2D = boss.getLocation();
                    Location playerLoc2D = player.getLocation();
                    double horizontalDistance = Math.sqrt(
                        Math.pow(playerLoc2D.getX() - bossLoc2D.getX(), 2) + 
                        Math.pow(playerLoc2D.getZ() - bossLoc2D.getZ(), 2)
                    );
                    double heightDifference = player.getLocation().getY() - bossHeadLoc.getY();
                   
                    // Игрок выше головы босса и в пределах 3 блоков по горизонтали (включая присевших!)
                    if (heightDifference > 0 && horizontalDistance <= 3.0) {
                        UUID uuid = player.getUniqueId();
                       
                        if (!playersAboveBoss.containsKey(uuid)) {
                            // Начинаем отсчет
                            playersAboveBoss.put(uuid, currentTime);
                            String cowardiceMsg = plugin.getMessageManager().getMessage(player, "boss1.cowardice");
                            player.sendMessage(Component.text(cowardiceMsg).color(NamedTextColor.RED));
                            plugin.getLogger().info("Player " + player.getName() + " above boss. Height diff: " + heightDifference + ", Horizontal: " + horizontalDistance);
                        } else {
                            long timeAbove = currentTime - playersAboveBoss.get(uuid);
                           
                            // Через 5 секунд (5000ms) притягиваем
                            if (timeAbove >= 5000) {
                                // МОЩНОЕ ПРИТЯГИВАНИЕ С ЭФФЕКТАМИ
                                pullPlayerToBoss(player, boss);
                               
                                // Сбрасываем таймер
                                playersAboveBoss.remove(uuid);
                            }
                        }
                    } else {
                        // Игрок не над боссом - сбрасываем таймер
                        playersAboveBoss.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }
    
    private void pullPlayerToBoss(Player player, Skeleton boss) {
        Location playerLoc = player.getLocation();
        Location bossLoc = boss.getLocation();
        
        // Поднимаем игрока на 1.5 блока вверх перед притягиванием
        Vector pullDirection = bossLoc.toVector().subtract(playerLoc.toVector()).normalize();
        pullDirection.setY(0.3); // Слегка вверх, чтобы оторвать от блока
        pullDirection.multiply(3.0); // Очень сильное притяжение
        
        player.setVelocity(pullDirection);
        String pullMsg = plugin.getMessageManager().getMessage(player, "boss1.pull");
        player.sendMessage(Component.text(pullMsg).color(NamedTextColor.DARK_RED));
        plugin.getLogger().info("Pulling player " + player.getName() + " to boss with block breaking");
        
        World world = player.getWorld();
        
        // ЭПИЧНЫЕ ЭФФЕКТЫ
        // 1. Звуки
        world.playSound(playerLoc, Sound.ENTITY_WITHER_SHOOT, 2.0f, 0.5f);
        world.playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
        world.playSound(bossLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f);
        
        // 2. Партиклы вокруг игрока (портал + дым)
        world.spawnParticle(Particle.PORTAL, playerLoc, 100, 0.5, 1, 0.5, 2);
        world.spawnParticle(Particle.SMOKE, playerLoc, 50, 0.3, 0.5, 0.3, 0.1);
        world.spawnParticle(Particle.WITCH, playerLoc, 30, 0.5, 0.5, 0.5, 0.5);
        
        // 3. Линия партиклов от босса к игроку
        Vector direction = playerLoc.toVector().subtract(bossLoc.toVector());
        double distance = direction.length();
        direction.normalize();
        
        for (double d = 0; d < distance; d += 0.5) {
            Location particleLoc = bossLoc.clone().add(direction.clone().multiply(d));
            world.spawnParticle(Particle.FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0);
        }
        
        // ЛОМАНИЕ БЛОКОВ НА ПУТИ
        new BukkitRunnable() {
            int ticks = 0;
           
            @Override
            public void run() {
                if (ticks > 40 || !player.isOnline() || player.isDead()) { // 2 секунды максимум
                    cancel();
                    return;
                }
               
                // Проверяем блоки вокруг игрока
                Location loc = player.getLocation();
                int radius = 1;
               
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -1; y <= 2; y++) { // От ног до головы + выше
                        for (int z = -radius; z <= radius; z++) {
                            Location blockLoc = loc.clone().add(x, y, z);
                            Block block = blockLoc.getBlock();
                            Material type = block.getType();
                           
                            // Ломаем только блоки, поставленные ИГРОКАМИ
                            if (type.isSolid() && 
                                type != Material.BEDROCK && 
                                type != Material.END_PORTAL_FRAME &&
                                type != Material.BARRIER &&
                                type != Material.COMMAND_BLOCK &&
                                type != Material.OBSIDIAN &&
                                type != Material.CRYING_OBSIDIAN &&
                                type != Material.ANCIENT_DEBRIS &&
                                PlayerPlacedBlocksManager.isPlayerPlaced(block)) { // <-- Проверка!
                               
                                // Эффект разрушения
                                world.spawnParticle(Particle.BLOCK, blockLoc.add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1, type.createBlockData());
                                world.playSound(blockLoc, Sound.BLOCK_STONE_BREAK, 0.5f, 0.8f);
                               
                                // Убираем из трекинга
                                PlayerPlacedBlocksManager.removeBlock(block);
                               
                                // Ломаем блок
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }
               
                // Дополнительные партиклы во время полета
                world.spawnParticle(Particle.SMOKE, loc, 5, 0.2, 0.2, 0.2, 0.05);
               
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Каждый тик
    }
    
    private void startBoss1TeleportTask(Skeleton boss) {
        teleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    playersNearBoss.clear();
                    cancel();
                    return;
                }
                
                // Only teleport in phase 1
                if (boss1Phase != 1) {
                    return;
                }
               
                long currentTime = System.currentTimeMillis();
                List<Player> nearbyPlayers = new ArrayList<>();
               
                // Find all players within 2 blocks
                for (Player player : boss.getWorld().getPlayers()) {
                    if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                        continue;
                    }
                   
                    double distance = player.getLocation().distance(boss.getLocation());
                    if (distance <= 2.0) {
                        nearbyPlayers.add(player);
                       
                        UUID uuid = player.getUniqueId();
                        if (!playersNearBoss.containsKey(uuid)) {
                            playersNearBoss.put(uuid, currentTime);
                        }
                    } else {
                        playersNearBoss.remove(player.getUniqueId());
                    }
                }
               
                // Check if any player has been near for 5+ seconds
                boolean shouldTeleport = false;
                for (Player player : nearbyPlayers) {
                    Long startTime = playersNearBoss.get(player.getUniqueId());
                    if (startTime != null && (currentTime - startTime >= 5000)) {
                        shouldTeleport = true;
                        break;
                    }
                }
               
                if (shouldTeleport && nearbyPlayers.size() > 0) {
                    Location teleportLoc = findBossTeleportLocation(boss, nearbyPlayers);
                   
                    if (teleportLoc != null) {
                        // Epic teleport effects at old location
                        Location oldLoc = boss.getLocation();
                        World world = boss.getWorld();
                       
                        world.spawnParticle(Particle.PORTAL, oldLoc, 100, 0.5, 1, 0.5, 1);
                        world.spawnParticle(Particle.SMOKE, oldLoc, 50, 0.5, 1, 0.5, 0.1);
                        world.playSound(oldLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.7f);
                       
                        // Teleport boss
                        boss.teleport(teleportLoc);
                       
                        // Epic effects at new location
                        world.spawnParticle(Particle.REVERSE_PORTAL, teleportLoc, 100, 0.5, 1, 0.5, 1);
                        world.spawnParticle(Particle.END_ROD, teleportLoc, 50, 0.5, 1, 0.5, 0.1);
                        world.playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.2f);
                        world.playSound(teleportLoc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.5f);
                       
                        // Clear tracking
                        playersNearBoss.clear();
                       
                        plugin.getLogger().info("Boss teleported away from " + nearbyPlayers.size() + " crowding players");
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }
    
    private Location findBossTeleportLocation(Skeleton boss, List<Player> nearbyPlayers) {
        World world = boss.getWorld();
        Location bossLoc = boss.getLocation();
        
        // Try 20 times to find a good teleport spot
        for (int attempt = 0; attempt < 20; attempt++) {
            Location targetLoc = null;
           
            // 50% chance: teleport behind random player
            // 50% chance: teleport to elevated position (tree, hill)
            if (Math.random() < 0.5 && !nearbyPlayers.isEmpty()) {
                // Behind player
                Player randomPlayer = nearbyPlayers.get((int) (Math.random() * nearbyPlayers.size()));
                Vector direction = randomPlayer.getLocation().getDirection().multiply(-3); // 3 blocks behind
                targetLoc = randomPlayer.getLocation().add(direction);
            } else {
                // Elevated random position around boss
                double angle = Math.random() * Math.PI * 2;
                double distance = 8 + Math.random() * 7; // 8-15 blocks away
               
                double x = bossLoc.getX() + Math.cos(angle) * distance;
                double z = bossLoc.getZ() + Math.sin(angle) * distance;
               
                // Find highest block (for tree/hill)
                targetLoc = world.getHighestBlockAt((int) x, (int) z).getLocation().add(0, 1, 0);
            }
           
            // Validate location
            if (targetLoc == null) continue;
           
            // Check if location is safe (not underground, not in lava, has line of sight)
            Block block = targetLoc.getBlock();
            Block above = targetLoc.clone().add(0, 1, 0).getBlock();
            Block below = targetLoc.clone().subtract(0, 1, 0).getBlock();
           
            // Must have solid ground and air above
            if (!below.getType().isSolid() || !block.getType().isAir() || !above.getType().isAir()) {
                continue;
            }
           
            // Don't teleport into lava/fire
            if (below.getType() == Material.LAVA || block.getType() == Material.LAVA) {
                continue;
            }
           
            // Don't teleport too deep underground (Y level check)
            if (targetLoc.getY() < bossLoc.getY() - 10) {
                continue;
            }
           
            // Check line of sight to at least one player
            boolean hasLineOfSight = false;
            for (Player player : nearbyPlayers) {
                if (player.hasLineOfSight(targetLoc.clone().add(0, 1, 0))) {
                    hasLineOfSight = true;
                    break;
                }
            }
           
            if (!hasLineOfSight) {
                continue;
            }
           
            // Location is good!
            return targetLoc;
        }
        
        // Fallback: teleport 10 blocks away in random direction at same Y level
        double angle = Math.random() * Math.PI * 2;
        Location fallback = bossLoc.clone().add(Math.cos(angle) * 10, 0, Math.sin(angle) * 10);
        return fallback;
    }
    
    @EventHandler
    public void onWitherSkullDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        // Check if damage is from wither skull
        if (!(event.getDamager() instanceof WitherSkull)) {
            return;
        }
        
        WitherSkull skull = (WitherSkull) event.getDamager();
        
        // Only handle skulls from boss
        if (!(skull.getShooter() instanceof Skeleton) || skull.getShooter() != boss1Entity) {
            return;
        }
        
        // Prevent damage to skeleton warriors
        if (event.getEntity() instanceof Skeleton) {
            Skeleton target = (Skeleton) event.getEntity();
            if (boss1Warriors.contains(target.getUniqueId()) || target == boss1Entity) {
                event.setCancelled(true);
                return;
            }
        }
        
        // Check if player is in safe zone
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Location playerLoc = player.getLocation();
           
            for (SafeZone zone : currentSafeZones) {
                if (zone.contains(playerLoc)) {
                    event.setCancelled(true);
                    String safeZoneMsg = plugin.getMessageManager().getMessage(player, "boss1.safe_zone_protection");
                    player.sendMessage(Component.text(safeZoneMsg).color(NamedTextColor.GREEN));
                    playerLoc.getWorld().playSound(playerLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
                    return;
                }
            }
           
            // Apply damage multiplier if not in safe zone
            double damageMultiplier = plugin.getConfig().getDouble("acts.boss1.witherSkullAttack.damageMultiplier", 2.0);
            event.setDamage(event.getDamage() * damageMultiplier);
        }
    }

    /**
     * Handle warrior and archer deaths - remove all drops
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWarriorDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton skeleton)) {
            return;
        }

        String customName = skeleton.getCustomName();
        if (customName == null) {
            return;
        }

        // Check if it's a warrior or archer
        if (customName.contains("Воин Повелителя") || customName.contains("Лучник Повелителя")) {
            // Clear ALL drops completely
            event.getDrops().clear();
            event.setDroppedExp(0);

            // Only allow special bow to drop (5% chance for archers)
            if (customName.contains("Лучник Повелителя") && Math.random() < 0.05) {
                ItemStack specialBow = createSpecialArcherBow();
                event.getDrops().add(specialBow);

                }

            // Clean up tracking lists
            boss1Warriors.remove(skeleton.getUniqueId());
            warriorSkeletons.remove(skeleton);
        }
    }

    @EventHandler
    public void onBoss1Death(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton)) {
            return;
        }
        
        Skeleton skeleton = (Skeleton) event.getEntity();
        if (skeleton.getCustomName() == null || !skeleton.getCustomName().contains("Повелитель Скелетов")) {
            return;
        }
        
        // Boss 1 defeated!
        boolean wasFirstKill = !plugin.getDataManager().isBoss1Defeated();
        plugin.getDataManager().setBoss1Defeated(true);
        
        // Clean up all tasks when boss dies
        cleanupAllBossTasks();

        // Clean up all tracked blocks when boss is defeated to optimize memory
        PlayerPlacedBlocksManager.clearAll();
        
        // Drop Boss 1 Material (Фрагмент Гнева) - ONLY on first kill
        event.getDrops().clear();
        if (wasFirstKill) {
            ItemStack material = plugin.getItemManager().createStoryItem(ItemManager.BOSS1_MATERIAL);
            event.getDrops().add(material);
        }
        event.getDrops().add(new ItemStack(Material.NETHERITE_SCRAP, 3));
        event.getDrops().add(new ItemStack(Material.DIAMOND, 5));
        
        // Remove boss bar
        if (boss1BossBar != null) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.hideBossBar(boss1BossBar);
            }
            boss1BossBar = null;
        }
        boss1Entity = null;
        boss1Phase = 1;
        boss1Warriors.clear();
        playersAboveBoss.clear(); // Очистка таймеров игроков
        
        // Broadcast
        plugin.getDialogManager().playDialogForAll("boss1.defeated");
        
        // Notify about materials needed
        Location deathLoc = skeleton.getLocation();
        for (Player player : deathLoc.getWorld().getPlayers()) {
            if (player.getLocation().distance(deathLoc) < 100) {
                if (wasFirstKill) {
                    String catalystMsg = plugin.getMessageManager().getMessage(player, "boss1.defeated_first");
                    player.sendMessage(Component.text(catalystMsg).color(NamedTextColor.YELLOW));
                } else {
                    String defeatedMsg = plugin.getMessageManager().getMessage(player, "boss1.defeated_again");
                    player.sendMessage(Component.text(defeatedMsg).color(NamedTextColor.GOLD));
                }
            }
        }
    }
    
    /**
     * Clean up all boss-related tasks when boss dies
     */
    private void cleanupAllBossTasks() {
        // Cancel all running tasks
        if (bossBarTask != null) {
            bossBarTask.cancel();
            bossBarTask = null;
        }
        if (circleStrafeTask != null) {
            circleStrafeTask.cancel();
            circleStrafeTask = null;
        }
        if (witherSkullAttackTask != null) {
            witherSkullAttackTask.cancel();
            witherSkullAttackTask = null;
        }
        if (bossAITask != null) {
            bossAITask.cancel();
            bossAITask = null;
        }
        if (heightCheckTask != null) {
            heightCheckTask.cancel();
            heightCheckTask = null;
        }
        if (antiWallTask != null) {
            antiWallTask.cancel();
            antiWallTask = null;
        }
        if (teleportTask != null) {
            teleportTask.cancel();
            teleportTask = null;
        }
        if (proximityTeleportTask != null) {
            proximityTeleportTask.cancel();
            proximityTeleportTask = null;
        }
        if (risingAnimationTask != null) {
            risingAnimationTask.cancel();
            risingAnimationTask = null;
        }
        
        // Clean up animation and attack state
        if (currentRisingAnimation != null) {
            currentRisingAnimation.completeAnimation();
            currentRisingAnimation = null;
        }

        // Clean up special attack managers
        if (bossSpecialAttackManager != null) {
            bossSpecialAttackManager.cleanup();
            bossSpecialAttackManager = null;
        }
        if (stationaryCastingManager != null) {
            stationaryCastingManager.cleanup();
            stationaryCastingManager = null;
        }

        if (witherSkullAttackState != null) {
            witherSkullAttackState.setSpecialAttackPhase(BossAttackState.SpecialAttackPhase.NONE);
            witherSkullAttackState.endAttack();
        }

        // Clean up wolf elimination system
        if (wolfEliminationTask != null) {
            wolfEliminationTask.cancel();
            wolfEliminationTask = null;
        }

        // Clear tracking data
        circleStrafeTrackers.clear();
        currentSafeZones.clear();
        activeProjectiles.clear();
        playersAboveBoss.clear();
        playersNearBoss.clear();
        playerProximityStart.clear();
        teleportCooldown.clear();
        warriorSkeletons.clear();
    }
    
    @EventHandler
    public void onBoss2SummonInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        if (!plugin.getDataManager().isBoss1Defeated()) {
            return;
        }
        
        // Check if it's Boss 2 summon structure (placeholder: Crying Obsidian)
        if (event.getClickedBlock().getType() == Material.CRYING_OBSIDIAN) {
            Player player = event.getPlayer();
            ItemStack handItem = player.getInventory().getItemInMainHand();
           
            if (plugin.getItemManager().isStoryItem(handItem) &&
                ItemManager.BOSS2_STRUCTURE_KEY.equals(plugin.getItemManager().getStoryItemId(handItem))) {
               
                handItem.setAmount(handItem.getAmount() - 1);
                spawnBoss2(event.getClickedBlock().getLocation());
               
                event.setCancelled(true);
            }
        }
    }
    
    private void spawnBoss2(Location location) {
        Location spawnLoc = location.clone().add(0, 5, 0);
        
        Wither boss = (Wither) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.WITHER);
        boss.setCustomName(plugin.getMessageManager().getMessage("entities.nether_fiend"));
        boss.setCustomNameVisible(true);
        
        // Set attributes
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
        boss.setHealth(500.0);
        boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.7);
        
        boss.setRemoveWhenFarAway(false);
        
        // Effects
        boss.getWorld().playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 3.0f, 0.5f);
        boss.getWorld().spawnParticle(Particle.EXPLOSION, spawnLoc, 5, 2, 2, 2, 0);
    }
    
    @EventHandler
    public void onBoss2Death(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Wither)) {
            return;
        }
        
        Wither wither = (Wither) event.getEntity();
        if (wither.getCustomName() == null || !wither.getCustomName().contains("Изверг Адских Глубин")) {
            return;
        }
        
        // Boss 2 defeated!
        plugin.getDataManager().setBoss2Defeated(true);
        plugin.getDataManager().setEndEnabled(true);
        plugin.getActManager().progressToAct(3);
        
        // Drop Overworld Portal Key
        ItemStack key = plugin.getItemManager().createStoryItem(ItemManager.OVERWORLD_PORTAL_KEY);
        event.getDrops().clear();
        event.getDrops().add(key);
        event.getDrops().add(new ItemStack(Material.NETHER_STAR, 2));
        
        // Broadcast victory message
        String victoryMsg = plugin.getMessageManager().getMessage("ru", "boss2.defeated_message");
        if (victoryMsg == null || victoryMsg.equals("boss2.defeated_message")) {
            victoryMsg = "§6§l⚔ Изверг Адских Глубин повержен!";
        }
        plugin.getServer().broadcast(Component.text(victoryMsg).color(NamedTextColor.GOLD));
        
        // Play dialog
        plugin.getDialogManager().playDialogForAll("boss2.defeated");
        
        // Create portal in Overworld
        World overworld = plugin.getServer().getWorlds().get(0);
        Location spawn = overworld.getSpawnLocation();
        int distance = plugin.getConfigManager().getConfig().getInt("structures.overworldPortalDistanceFromSpawn", 500);
        
        Location portalLoc = plugin.getStructureManager().findSafeLocation(overworld, spawn, distance);
        plugin.getStructureManager().placeStructure("overworld_portal", portalLoc);
        plugin.getDataManager().saveLocation("structures.overworld_portal", portalLoc);
        
        // Announce portal creation
        String portalMsg = plugin.getMessageManager().getMessage("ru", "boss2.portal_created");
        if (portalMsg == null || portalMsg.equals("boss2.portal_created")) {
            portalMsg = "§5§l⚡ Портал в Край создан в Верхнем Мире!";
        }
        plugin.getServer().broadcast(Component.text(portalMsg).color(NamedTextColor.LIGHT_PURPLE));
        
        plugin.getLogger().info(plugin.getMessageManager().getMessage("log.overworld_portal_placed").replace("%location%", portalLoc.getBlockX() + ", " + portalLoc.getBlockY() + ", " + portalLoc.getBlockZ()));
    }
    
    /**
     * Create special archer bow with lore and effects
     */
    private ItemStack createSpecialArcherBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.POWER, 2);
        bow.addEnchantment(Enchantment.PUNCH, 1);
        bow.addEnchantment(Enchantment.UNBREAKING, 1);

        // Add custom lore and name
        ItemMeta meta = bow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5Токсичный Лук Повелителя");

            java.util.List<Component> lore = new java.util.ArrayList<>();
            lore.add(Component.text("§7Лук, отравленный ядом Повелителя Скелетов").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("§7Накладывает на цели сильную тошноту и смертельный яд").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(""));
            lore.add(Component.text("§c+ Эффект: Тошнота (10 сек)").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("§c+ Эффект: Смертельный яд (3 сек)").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(""));
            lore.add(Component.text("§6Редкий трофей с лучников Повелителя").decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            // Add custom data to identify special bow
            NamespacedKey key = new NamespacedKey(plugin, "special_archer_bow");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.BYTE, (byte) 1);

            bow.setItemMeta(meta);
        }

        return bow;
    }

    /**
     * T028/T029: Enable wolf elimination system for skeletons
     * This makes skeletons kill any wolves within 5 blocks with beautiful visual effects
     * @param skeleton The skeleton to modify
     */
    // Wolf elimination system removed - using Arrow Rain Phase 2 attack instead

  }
