package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Act3Listener implements Listener {
    
    private final MmmmStoryPlugin plugin;
    private boolean phantomSpawningActive = false;
    private boolean dragonAlliesSpawned = false; // Bug #3: Track if enderman allies spawned
    
    // Bug #4 Fix: Portal protection after dragon death
    private boolean portalProtected = false;
    private Location endPortalCenter = null;
    private static final double PORTAL_PROTECTION_RADIUS = 10.0; // Protect 10 blocks around portal
    
    public Act3Listener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
        
        // Start task to aggro endermen near players without pumpkin
        startEndermanAggroTask();
    }
    
    private void startEndermanAggroTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check all players in The End
                for (World world : plugin.getServer().getWorlds()) {
                    if (world.getEnvironment() == World.Environment.THE_END) {
                        for (Player player : world.getPlayers()) {
                            // Skip if dragon defeated
                            if (plugin.getDataManager().isDragonDefeated()) {
                                continue;
                            }
                            
                            // Check if player has pumpkin on head
                            ItemStack helmet = player.getInventory().getHelmet();
                            if (helmet != null && helmet.getType() == Material.CARVED_PUMPKIN) {
                                continue;
                            }
                            
                            // Aggro all nearby endermen
                            int radius = plugin.getConfigManager().getConfig().getInt("acts.end.endermanAggroRadius", 48);
                            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                                if (entity instanceof Enderman enderman && enderman.getTarget() == null) {
                                    enderman.setTarget(player);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Every second
    }
    
    @EventHandler
    public void onGolemTargetEnderman(EntityTargetLivingEntityEvent event) {
        // Prevent Crystal Guardian golems from targeting endermen
        if (event.getEntity() instanceof IronGolem golem) {
            if (golem.getCustomName() != null && golem.getCustomName().contains("Страж Кристалла")) {
                if (event.getTarget() instanceof Enderman) {
                    event.setCancelled(true);
                    // Keep golem aggressive to players only
                    if (golem.getTarget() == null || !(golem.getTarget() instanceof Player)) {
                        // Find nearest player to target
                        Player nearest = null;
                        double nearestDist = 50.0;
                        for (Player player : golem.getWorld().getPlayers()) {
                            double dist = player.getLocation().distance(golem.getLocation());
                            if (dist < nearestDist) {
                                nearest = player;
                                nearestDist = dist;
                            }
                        }
                        if (nearest != null) {
                            golem.setTarget(nearest);
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onBoss2KeyDrop(org.bukkit.event.entity.ItemSpawnEvent event) {
        if (plugin.getDataManager().getCurrentAct() < 3) {
            return;
        }
        
        org.bukkit.entity.Item droppedItem = event.getEntity();
        ItemStack itemStack = droppedItem.getItemStack();
        
        // Check if it's Boss 2 Key
        if (!plugin.getItemManager().isStoryItem(itemStack)) {
            return;
        }
        
        if (!ItemManager.BOSS2_STRUCTURE_KEY.equals(plugin.getItemManager().getStoryItemId(itemStack))) {
            return;
        }
        
        // Schedule check for Dragon Egg below
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!droppedItem.isValid() || droppedItem.isDead()) {
                    cancel();
                    return;
                }
                
                Location itemLoc = droppedItem.getLocation();
                Location blockBelow = itemLoc.clone().subtract(0, 1, 0);
                
                // Check if item is on Dragon Egg
                if (blockBelow.getBlock().getType() == Material.DRAGON_EGG) {
                    // Summon Boss 2!
                    summonBoss2(droppedItem);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 5L);
    }
    
    private void summonBoss2(org.bukkit.entity.Item droppedItem) {
        Location location = droppedItem.getLocation();
        World world = location.getWorld();
        
        // Remove the item
        droppedItem.remove();
        
        // Effects
        world.spawnParticle(Particle.PORTAL, location, 300, 2, 2, 2, 0.2);
        world.spawnParticle(Particle.DRAGON_BREATH, location, 200, 1.5, 1.5, 1.5, 0.1);
        world.spawnParticle(Particle.END_ROD, location, 150, 2, 2, 2, 0.15);
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.7f);
        world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.5f, 1.0f);
        world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 2.0f, 0.8f);
        
        // Spawn Boss 2 (Enderman boss)
        Location spawnLoc = location.clone().add(0, 3, 0);
        Enderman boss = (Enderman) world.spawnEntity(spawnLoc, EntityType.ENDERMAN);
        boss.setCustomName("§5§lСтраж Края");
        boss.setCustomNameVisible(true);
        
        // Set attributes
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
        boss.setHealth(300.0);
        boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
        boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(15.0);
        
        // Mark as boss
        boss.setPersistent(true);
        
        // Light pillar effect
        for (int y = 0; y < 30; y++) {
            Location particleLoc = location.clone().add(0, y, 0);
            world.spawnParticle(Particle.END_ROD, particleLoc, 5, 0.3, 0.3, 0.3, 0.02);
            world.spawnParticle(Particle.PORTAL, particleLoc, 3, 0.2, 0.2, 0.2, 0.1);
        }
        
        // Notify nearby players
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(location) < 150) {
                player.sendMessage(Component.text("§5§l⚔ Страж Края материализуется!").color(NamedTextColor.DARK_PURPLE));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1.0f, 0.6f);
            }
        }
    }
    
    @EventHandler
    public void onEndermanTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Enderman)) {
            return;
        }
        
        if (!(event.getTarget() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getTarget();
        
        // Check if in The End
        if (player.getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }
        
        // Check if dragon is defeated
        if (plugin.getDataManager().isDragonDefeated()) {
            event.setCancelled(true);
            return;
        }
        
        // Check if player has pumpkin on head
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.getType() == Material.CARVED_PUMPKIN) {
            return;
        }
        
        // Aggro all endermen in radius
        int radius = plugin.getConfigManager().getConfig().getInt("acts.end.endermanAggroRadius", 48);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Enderman enderman) {
                enderman.setTarget(player);
            }
        }
    }
    
    @EventHandler
    public void onCrystalDestroy(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) {
            return;
        }
        
        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        
        if (crystal.getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }
        
        Player damager = null;
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                damager = (Player) projectile.getShooter();
            }
        }
        
        if (damager == null) {
            return;
        }
        
        // Spawn Iron Golem "Crystal Guardian" on ground near the tower
        Location crystalLoc = crystal.getLocation();
        Location spawnLoc = null;
        
        // Search for ground at main End island level (around Y=64)
        // The main End island is typically at Y=60-70
        World world = crystalLoc.getWorld();
        
        // First, try to find the main island level by going straight down
        int mainIslandY = 64; // Default End island height
        for (int y = (int) crystalLoc.getY(); y >= 0; y--) {
            Block checkBlock = world.getBlockAt((int) crystalLoc.getX(), y, (int) crystalLoc.getZ());
            if (checkBlock.getType() == Material.END_STONE) {
                mainIslandY = y;
                break;
            }
        }
        
        // Now search in horizontal radius at the found island level
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                // Search around the main island level (±10 blocks)
                for (int y = mainIslandY + 10; y >= mainIslandY - 10; y--) {
                    Location checkLoc = new Location(world, 
                        crystalLoc.getX() + x, 
                        y, 
                        crystalLoc.getZ() + z);
                    Block groundBlock = checkLoc.getBlock();
                    
                    // Found solid ground (end_stone)
                    if (groundBlock.getType() == Material.END_STONE) {
                        // Check if there's 3 blocks of air space above for golem
                        Location spawnCheckLoc = checkLoc.clone().add(0, 1, 0);
                        if (spawnCheckLoc.getBlock().getType().isAir() && 
                            spawnCheckLoc.clone().add(0, 1, 0).getBlock().getType().isAir() &&
                            spawnCheckLoc.clone().add(0, 2, 0).getBlock().getType().isAir()) {
                            spawnLoc = spawnCheckLoc;
                            break;
                        }
                    }
                }
                if (spawnLoc != null) break;
            }
            if (spawnLoc != null) break;
        }
        
        // If no ground found, spawn on main island level as fallback
        if (spawnLoc == null) {
            spawnLoc = new Location(world, crystalLoc.getX(), mainIslandY + 1, crystalLoc.getZ());
        }
        
        IronGolem guardian = (IronGolem) crystal.getWorld().spawnEntity(spawnLoc, EntityType.IRON_GOLEM);
        guardian.setCustomName("§5§lСтраж Кристалла");
        guardian.setCustomNameVisible(true);
        guardian.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(25.0);
        guardian.setHealth(25.0);
        guardian.setTarget(damager);
        
        crystal.getWorld().playSound(crystalLoc, Sound.ENTITY_IRON_GOLEM_HURT, 2.0f, 0.8f);
        crystal.getWorld().spawnParticle(Particle.PORTAL, crystalLoc, 50, 1, 1, 1, 0.5);
        
        // Check if all crystals are destroyed
        checkAllCrystalsDestroyed(crystal.getWorld());
    }
    
    private void checkAllCrystalsDestroyed(World world) {
        // Count remaining crystals
        long crystalCount = world.getEntitiesByClass(EnderCrystal.class).size();
        
        if (crystalCount <= 1 && !phantomSpawningActive) { // <= 1 because this one is about to be destroyed
            startPhantomBurst(world);
        }
    }
    
    private void startPhantomBurst(World world) {
        phantomSpawningActive = true;
        
        plugin.getServer().broadcast(Component.text("§5§lПустота открывается... Фантомы надвигаются!"));
        
        int duration = plugin.getConfigManager().getConfig().getInt("acts.end.phantomBurst.durationSeconds", 15);
        int interval = plugin.getConfigManager().getConfig().getInt("acts.end.phantomBurst.intervalSeconds", 3);
        int minCount = plugin.getConfigManager().getConfig().getInt("acts.end.phantomBurst.countMin", 1);
        int maxCount = plugin.getConfigManager().getConfig().getInt("acts.end.phantomBurst.countMax", 5);
        
        new BukkitRunnable() {
            int elapsed = 0;
            
            @Override
            public void run() {
                if (elapsed >= duration) {
                    phantomSpawningActive = false;
                    cancel();
                    return;
                }
                
                // Spawn phantoms around each player in The End
                for (Player player : world.getPlayers()) {
                    int count = minCount + (int)(Math.random() * (maxCount - minCount + 1));
                    
                    for (int i = 0; i < count; i++) {
                        Location spawnLoc = player.getLocation().clone().add(
                            (Math.random() - 0.5) * 20,
                            10 + Math.random() * 10,
                            (Math.random() - 0.5) * 20
                        );
                        
                        Phantom phantom = (Phantom) world.spawnEntity(spawnLoc, EntityType.PHANTOM);
                        phantom.setTarget(player);
                    }
                }
                
                world.playSound(world.getSpawnLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1.0f, 0.8f);
                elapsed += interval;
            }
        }.runTaskTimer(plugin, 0L, interval * 20L);
    }
    
    // ==========================================
    // BUG #3 FIX: ENDERMAN ALLY SPAWNING AT 50% DRAGON HEALTH
    // ==========================================
    
    @EventHandler
    public void onDragonDamage(EntityDamageEvent event) {
        // Only handle Ender Dragon damage
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }
        
        // Skip if allies already spawned
        if (dragonAlliesSpawned) {
            return;
        }
        
        EnderDragon dragon = (EnderDragon) event.getEntity();
        
        // Calculate health after this damage
        double healthAfter = dragon.getHealth() - event.getFinalDamage();
        double maxHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healthPercent = (healthAfter / maxHealth) * 100;
        
        // Spawn allies at 50% health
        if (healthPercent <= 50.0) {
            dragonAlliesSpawned = true;
            spawnDragonAllies(dragon);
        }
    }
    
    /**
     * Spawn Endermen allies for the Ender Dragon at 50% HP
     */
    private void spawnDragonAllies(EnderDragon dragon) {
        World world = dragon.getWorld();
        Location dragonLoc = dragon.getLocation();
        
        // Get configuration
        int spawnCount = plugin.getConfig().getInt("acts.end.dragonFight.endermanAllies.spawnCount", 5);
        
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            plugin.getLogger().info("Spawning " + spawnCount + " Enderman allies for dragon at 50% HP");
        }
        
        // Spawn configured number of Endermen around the dragon
        for (int i = 0; i < spawnCount; i++) {
            // Calculate spawn location in a circle around the dragon
            double angle = (360.0 / spawnCount) * i;
            double radians = Math.toRadians(angle);
            double radius = 8.0 + (Math.random() * 4.0); // 8-12 blocks away
            
            double x = dragonLoc.getX() + (Math.cos(radians) * radius);
            double z = dragonLoc.getZ() + (Math.sin(radians) * radius);
            double y = dragonLoc.getY();
            
            Location spawnLoc = new Location(world, x, y, z);
            
            // Find safe ground level
            while (spawnLoc.getY() > 50 && !spawnLoc.getBlock().getType().isSolid()) {
                spawnLoc.subtract(0, 1, 0);
            }
            spawnLoc.add(0, 1, 0); // Place on top of solid block
            
            // Spawn Enderman
            Enderman ally = (Enderman) world.spawnEntity(spawnLoc, EntityType.ENDERMAN);
            
            // Add scoreboard tag to identify as dragon ally (Bug #3 tracking)
            ally.addScoreboardTag("story_dragon_ally");
            
            // Apply potion effects from config
            var effectsList = plugin.getConfig().getList("acts.end.dragonFight.endermanAllies.effects");
            if (effectsList != null) {
                for (Object effectObj : effectsList) {
                    if (effectObj instanceof java.util.Map effectMap) {
                        String typeName = (String) effectMap.get("type");
                        int level = (int) effectMap.get("level");
                        int duration = (int) effectMap.get("duration");
                        
                        try {
                            PotionEffectType type = PotionEffectType.getByName(typeName);
                            if (type != null) {
                                // -1 duration means infinite
                                int actualDuration = duration == -1 ? Integer.MAX_VALUE : duration * 20;
                                ally.addPotionEffect(new PotionEffect(type, actualDuration, level, false, false));
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to apply potion effect " + typeName + " to Enderman ally");
                        }
                    }
                }
            }
            
            // Make ally aggressive
            ally.setTarget(getNearestPlayer(dragonLoc));
            
            // Spawn effects
            world.spawnParticle(Particle.PORTAL, spawnLoc, 50, 0.5, 1, 0.5, 0.3);
            world.playSound(spawnLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.7f);
        }
        
        // Broadcast message to players
        for (Player player : world.getPlayers()) {
            player.sendMessage(Component.text("Эндер Дракон призвал своих союзников!", NamedTextColor.DARK_PURPLE));
        }
        
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            plugin.getLogger().info("Spawned " + spawnCount + " tagged Enderman allies for dragon");
        }
    }
    
    /**
     * Find nearest player to a location
     */
    private Player getNearestPlayer(Location location) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for (Player player : location.getWorld().getPlayers()) {
            double dist = player.getLocation().distance(location);
            if (dist < nearestDist) {
                nearest = player;
                nearestDist = dist;
            }
        }
        
        return nearest;
    }
    
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }
        
        EnderDragon dragon = (EnderDragon) event.getEntity();
        
        // Mark dragon as defeated
        plugin.getDataManager().setDragonDefeated(true);
        plugin.getActManager().progressToAct(4);
        
        // Broadcast
        plugin.getDialogManager().playDialogForAll("dragon.defeated");
        
        // Break the portal but keep the egg
        Location dragonLoc = dragon.getLocation();
        World world = dragon.getWorld();
        
        // ==========================================
        // BUG #3 FIX: CLEANUP ENDERMAN ALLIES AFTER DRAGON DEATH
        // ==========================================
        
        boolean removeOnDeath = plugin.getConfig().getBoolean("acts.end.dragonFight.endermanAllies.removeOnDragonDeath", true);
        int cleanedCount = 0;
        
        // Debug logging: Start cleanup process
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            plugin.getLogger().info("[DEBUG] Dragon death cleanup - removeOnDeath config: " + removeOnDeath);
            long totalEndermen = world.getEntities().stream()
                .filter(e -> e instanceof Enderman)
                .count();
            long taggedEndermen = world.getEntities().stream()
                .filter(e -> e instanceof Enderman)
                .map(e -> (Enderman) e)
                .filter(e -> e.getScoreboardTags().contains("story_dragon_ally"))
                .count();
            plugin.getLogger().info("[DEBUG] Found " + totalEndermen + " total Endermen, " + taggedEndermen + " with 'story_dragon_ally' tag");
        }
        
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Enderman) {
                Enderman enderman = (Enderman) entity;
                
                // Check if this is a tagged dragon ally
                if (enderman.getScoreboardTags().contains("story_dragon_ally")) {
                    if (removeOnDeath) {
                        // Remove the ally entirely
                        enderman.remove();
                        cleanedCount++;
                    } else {
                        // Revert to vanilla behavior
                        enderman.removeScoreboardTag("story_dragon_ally");
                        enderman.setTarget(null);
                        enderman.clearActivePotionEffects();
                        enderman.setAware(true);
                        enderman.setAI(true);
                        enderman.setCanPickupItems(false);  // Reset pickup behavior
                        cleanedCount++;
                    }
                } else {
                    // Reset vanilla endermen (not allies)
                    enderman.setTarget(null);
                    enderman.setAware(true);
                    enderman.setAI(true);
                }
            }
        }
        
        if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
            String action = removeOnDeath ? "removed" : "reverted";
            plugin.getLogger().info("Dragon defeated! " + cleanedCount + " Enderman allies " + action + ", vanilla Endermen reset.");
        } else {
            plugin.getLogger().info("Dragon defeated! Endermen returned to vanilla state.");
        }
        
        // Reset ally spawn flag for potential future dragon fights
        dragonAlliesSpawned = false;
        
        // ==========================================
        // BUG #4 FIX: PROTECT END PORTAL FROM CORRUPTION
        // ==========================================
        
        // Find and protect the End portal
        Location portalLoc = new Location(world, 0, 65, 0);
        endPortalCenter = findEndPortal(world);
        if (endPortalCenter != null) {
            portalProtected = true;
            plugin.getLogger().info("[Portal Protection] End portal protected at " + 
                endPortalCenter.getBlockX() + ", " + endPortalCenter.getBlockY() + ", " + endPortalCenter.getBlockZ());
        } else {
            plugin.getLogger().warning("[Portal Protection] Could not find End portal to protect!");
            portalProtected = true; // Still enable protection to be safe
            endPortalCenter = portalLoc; // Use default location
        }
        
        // Find the exit portal location (usually at 0, ~, 0)
        
        // Break portal and replace bedrock with end_stone continuously
        new BukkitRunnable() {
            int ticksElapsed = 0;
            final int maxTicks = 72000; // Run for 1 hour (until story ends)
            
            @Override
            public void run() {
                // Stop when Act 5 is completed (Act >= 5 means ritual completed)
                if (ticksElapsed >= maxTicks || plugin.getDataManager().getCurrentAct() >= 5) {
                    cancel();
                    return;
                }
                
                // Replace portal blocks and bedrock, but keep dragon egg
                for (int x = -10; x <= 10; x++) {
                    for (int y = 50; y <= 90; y++) {
                        for (int z = -10; z <= 10; z++) {
                            Location checkLoc = new Location(world, x, y, z);
                            Material type = checkLoc.getBlock().getType();
                            
                            // Replace portal with air (portal "breaks")
                            if (type == Material.END_PORTAL || type == Material.END_PORTAL_FRAME) {
                                checkLoc.getBlock().setType(Material.AIR);
                            }
                            // Replace bedrock with end_stone
                            else if (type == Material.BEDROCK) {
                                checkLoc.getBlock().setType(Material.END_STONE);
                            }
                            // Keep dragon egg - don't touch it!
                        }
                    }
                }
                
                // BUG #6 FIX: Dragon_loot structure removed - artifacts obtained via other mechanics
                // Previously spawned dragon_loot structure here, but this is no longer needed
                // Artifacts are obtained through End City chest searches instead
                if (ticksElapsed == 0) {
                    plugin.getLogger().info("Dragon defeated! Portal broken, egg preserved. Artifacts available in End Cities.");
                }
                
                ticksElapsed += 20; // Increment by 1 second
            }
        }.runTaskTimer(plugin, 40L, 20L); // Start after 2 seconds, then repeat every 1 second
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Block teleportation through END portal (exit portal) after dragon defeat until ritual starts
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (event.getFrom().getWorld().getEnvironment() == World.Environment.THE_END) {
                int currentAct = plugin.getDataManager().getCurrentAct();
                
                // Block exit if dragon defeated but ritual hasn't started (Acts 3-4)
                if (plugin.getDataManager().isDragonDefeated() && currentAct < 5) {
                    // Cancel portal teleport - players must stay in End until ritual
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Component.text("§c§lВыход из Края заблокирован! Соберите все артефакты для начала ритуала.")
                            .color(NamedTextColor.RED));
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 0.5f);
                    return;
                }
            }
        }
        
        // Regenerate End Crystals when player enters The End (if dragon not defeated)
        if (event.getTo() != null && event.getTo().getWorld() != null) {
            if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
                if (!plugin.getDataManager().isDragonDefeated()) {
                    // Play end.entry dialog
                    plugin.getDialogManager().playDialog(event.getPlayer(), "end.entry");
                    
                    // Schedule crystal regeneration after teleport
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            regenerateEndCrystals(event.getTo().getWorld());
                        }
                    }.runTaskLater(plugin, 20L); // 1 second delay
                }
            }
        }
    }
    
    private void regenerateEndCrystals(World world) {
        // Check if crystals already exist
        if (!world.getEntitiesByClass(EnderCrystal.class).isEmpty()) {
            return; // Crystals already present
        }
        
        plugin.getLogger().info("Regenerating End Crystals on obsidian pillars...");
        
        // Standard End crystal positions (10 pillars)
        int[][] pillarPositions = {
            {0, 0},      // Center
            {42, 0},     // North
            {-42, 0},    // South
            {0, 42},     // East
            {0, -42},    // West
            {30, 30},    // NE
            {-30, 30},   // SE
            {30, -30},   // NW
            {-30, -30},  // SW
            {21, 21}     // Additional pillars
        };
        
        for (int[] pos : pillarPositions) {
            // Find the highest obsidian pillar at this position
            Location pillarBase = new Location(world, pos[0], 60, pos[1]);
            Location highestObsidian = null;
            
            for (int y = 60; y <= 90; y++) {
                Location checkLoc = new Location(world, pos[0], y, pos[1]);
                if (checkLoc.getBlock().getType() == Material.OBSIDIAN) {
                    highestObsidian = checkLoc;
                }
            }
            
            if (highestObsidian != null) {
                // Spawn crystal on top of pillar
                Location crystalLoc = highestObsidian.clone().add(0.5, 1, 0.5);
                EnderCrystal crystal = (EnderCrystal) world.spawnEntity(crystalLoc, EntityType.END_CRYSTAL);
                crystal.setShowingBottom(true);
                plugin.getLogger().info("Spawned crystal at: " + crystalLoc.getBlockX() + ", " + crystalLoc.getBlockY() + ", " + crystalLoc.getBlockZ());
            }
        }
        
        plugin.getLogger().info("End crystal regeneration complete!");
    }
    
    /**
     * Find the End portal location by scanning for END_PORTAL blocks
     */
    private Location findEndPortal(World world) {
        // Scan area around 0,0 at typical portal heights
        for (int y = 40; y <= 80; y++) {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    Location loc = new Location(world, x, y, z);
                    if (loc.getBlock().getType() == Material.END_PORTAL) {
                        // Found a portal block, return its location
                        plugin.getLogger().info("[Portal Protection] Found END_PORTAL block at " + x + ", " + y + ", " + z);
                        return loc;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Check if a location is within the protected portal radius
     */
    private boolean isNearProtectedPortal(Location location) {
        if (!portalProtected || endPortalCenter == null) {
            return false;
        }
        
        // Check if in same world
        if (!location.getWorld().equals(endPortalCenter.getWorld())) {
            return false;
        }
        
        // Check distance (use 2D distance since portal is horizontal)
        double distance = location.distance(endPortalCenter);
        return distance <= PORTAL_PROTECTION_RADIUS;
    }
    
    /**
     * Prevent block physics from corrupting portal (e.g., end_stone replacing portal blocks)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!portalProtected) {
            return;
        }
        
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();
        
        // Protect portal blocks from physics changes
        if (block.getType() == Material.END_PORTAL || block.getType() == Material.END_PORTAL_FRAME) {
            if (isNearProtectedPortal(blockLoc)) {
                // Cancel physics to prevent portal corruption
                event.setCancelled(true);
                
                if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                    plugin.getLogger().info("[Portal Protection] Prevented physics change at portal block: " + 
                        blockLoc.getBlockX() + ", " + blockLoc.getBlockY() + ", " + blockLoc.getBlockZ());
                }
            }
        }
        
        // Prevent end_stone from appearing near portal
        if (block.getType() == Material.END_STONE && isNearProtectedPortal(blockLoc)) {
            event.setCancelled(true);
            
            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                plugin.getLogger().info("[Portal Protection] Prevented end_stone physics at: " + 
                    blockLoc.getBlockX() + ", " + blockLoc.getBlockY() + ", " + blockLoc.getBlockZ());
            }
        }
    }
    
    /**
     * Prevent players from placing end_stone near protected portal
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!portalProtected) {
            return;
        }
        
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();
        
        // Prevent end_stone placement near portal
        if (block.getType() == Material.END_STONE && isNearProtectedPortal(blockLoc)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("§c§l⚠ Cannot place blocks near the protected End Portal!").color(NamedTextColor.RED));
            
            if (plugin.getConfig().getBoolean("logging.debugMode", false)) {
                plugin.getLogger().info("[Portal Protection] Prevented " + event.getPlayer().getName() + 
                    " from placing end_stone near portal");
            }
        }
    }
}
