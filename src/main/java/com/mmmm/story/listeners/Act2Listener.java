package com.mmmm.story.listeners;

import com.mmmm.story.MmmmStoryPlugin;
import com.mmmm.story.managers.ItemManager;
import com.mmmm.story.managers.PlayerPlacedBlocksManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.enchantments.Enchantment;

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
    
    public Act2Listener(MmmmStoryPlugin plugin) {
        this.plugin = plugin;
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
                
                // Include the dropped item itself
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
        Location location = droppedItem.getLocation();
        World world = location.getWorld();
        
        // Remove the item
        droppedItem.remove();
        
        // Effects
        world.spawnParticle(Particle.FLAME, location, 200, 2, 2, 2, 0.1);
        world.spawnParticle(Particle.LAVA, location, 100, 1, 1, 1, 0.1);
        world.spawnParticle(Particle.SMOKE, location, 150, 1.5, 1.5, 1.5, 0.05);
        world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
        world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 1.2f);
        
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
        Location spawnLoc = location.clone().add(0, 2, 0);
        
        Skeleton boss = (Skeleton) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.SKELETON);
        boss.setCustomName("§4§lПовелитель Скелетов");
        boss.setCustomNameVisible(true);
        
        // Set attributes
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0);
        boss.setHealth(200.0);
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
        
        // Start boss AI task (distance keeping in phase 2)
        startBoss1AITask(boss);
        
        // Start player height check task (pull players down if above)
        startBoss1HeightCheckTask(boss);
        
        // Start anti-wall task (prevent boxing the boss)
        startBoss1AntiWallTask(boss);
        
        // Effects
        boss.getWorld().playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.8f);
        boss.getWorld().spawnParticle(Particle.SMOKE, spawnLoc, 100, 2, 2, 2, 0.1);
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
        new BukkitRunnable() {
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
    
    @EventHandler
    public void onBoss1Damage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Skeleton)) {
            return;
        }
        
        Skeleton skeleton = (Skeleton) event.getEntity();
        if (skeleton != boss1Entity) {
            return;
        }
        
        // Check for phase 2 transition at 100 HP
        double healthAfterDamage = skeleton.getHealth() - event.getFinalDamage();
        if (boss1Phase == 1 && healthAfterDamage <= 100) {
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
            player.sendMessage(Component.text("§c§l⚡ Повелитель отбрасывает вас!").color(NamedTextColor.RED));
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
        } else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
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
    
    private void startBoss1SummonTask(Skeleton boss) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    cancel();
                    return;
                }
                
                // Summon 2-3 warrior skeletons
                for (int i = 0; i < 2 + (int)(Math.random() * 2); i++) {
                    Location summonLoc = boss.getLocation().clone().add(
                        (Math.random() - 0.5) * 4,
                        0,
                        (Math.random() - 0.5) * 4
                    );
                    
                    Skeleton warrior = (Skeleton) boss.getWorld().spawnEntity(summonLoc, EntityType.SKELETON);
                    warrior.setCustomName("§6Воин Повелителя");
                    warrior.setCustomNameVisible(true);
                    
                    // Phase 1: Sword warriors
                    if (boss1Phase == 1) {
                        warrior.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                        warrior.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        warrior.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                    } else {
                        // Phase 2: Archer warriors
                        warrior.setCustomName("§6Лучник Повелителя");
                        warrior.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                        warrior.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                        
                        ItemStack bow = new ItemStack(Material.BOW);
                        bow.addEnchantment(Enchantment.POWER, 2);
                        warrior.getEquipment().setItemInMainHand(bow);
                    }
                    
                    warrior.setTarget(boss.getTarget());
                    boss1Warriors.add(warrior.getUniqueId());
                }
                
                boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_SKELETON_AMBIENT, 1.0f, 0.5f);
                boss.getWorld().spawnParticle(Particle.SOUL, boss.getLocation(), 20, 1, 1, 1, 0.05);
            }
        }.runTaskTimer(plugin, 400L, 400L); // Every 20 seconds (was 30)
    }
    
    private void transitionToPhase2(Skeleton boss) {
        boss1Phase = 2;
        
        // Remove all armor
        boss.getEquipment().setHelmet(null);
        boss.getEquipment().setChestplate(null);
        boss.getEquipment().setLeggings(null);
        boss.getEquipment().setBoots(null);
        
        // Give enchanted bow with Punch (knockback for bows)
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.POWER, 3);
        bow.addEnchantment(Enchantment.PUNCH, 1); // Punch = knockback для луков
        boss.getEquipment().setItemInMainHand(bow);
        
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
                player.sendMessage(Component.text("§5§l⚡ Повелитель входит во ВТОРУЮ ФАЗУ!").color(NamedTextColor.DARK_PURPLE));
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.5f);
            }
        }
    }
    
    private void startBoss1AITask(Skeleton boss) {
        new BukkitRunnable() {
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
    
    private void startBoss1AntiWallTask(Skeleton boss) {
        new BukkitRunnable() {
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
                            player.sendMessage(Component.text("§4§l⚡⚡⚡ ПОВЕЛИТЕЛЬ В ЯРОСТИ! ОН РАЗРУШАЕТ СТЕНЫ! ⚡⚡⚡").color(NamedTextColor.DARK_RED));
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
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0)); // 5 секунд
                            
                            // Небольшой откат
                            Vector direction = player.getLocation().toVector().subtract(bossLoc.toVector()).normalize();
                            direction.setY(0.5);
                            direction.multiply(1.5);
                            player.setVelocity(direction);
                            
                            player.sendMessage(Component.text("§c§lВы ощущаете гнев Повелителя!").color(NamedTextColor.RED));
                            
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
        new BukkitRunnable() {
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
                            player.sendMessage(Component.text("§c§l⚠ Повелитель чувствует вашу трусость...").color(NamedTextColor.RED));
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
        player.sendMessage(Component.text("§4§l⚡ ПОВЕЛИТЕЛЬ ПРИТЯГИВАЕТ ВАС!").color(NamedTextColor.DARK_RED));
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
                                type != Material.OBSIDIAN && // Обсидиан не ломается
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
                    player.sendMessage(Component.text("§e§l✦ Найдите Катализатор Пустоты в Бастионе!").color(NamedTextColor.YELLOW));
                } else {
                    player.sendMessage(Component.text("§6§l⚔ Повелитель побежден снова!").color(NamedTextColor.GOLD));
                }
            }
        }
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
        boss.setCustomName("§4§lИзверг Адских Глубин");
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
        
        // Broadcast
        plugin.getDialogManager().playDialogForAll("boss2.defeated");
        
        // Create portal in Overworld
        World overworld = plugin.getServer().getWorlds().get(0);
        Location spawn = overworld.getSpawnLocation();
        int distance = plugin.getConfigManager().getConfig().getInt("structures.overworldPortalDistanceFromSpawn", 500);
        
        Location portalLoc = plugin.getStructureManager().findSafeLocation(overworld, spawn, distance);
        plugin.getStructureManager().placeStructure("overworld_portal", portalLoc);
        plugin.getDataManager().saveLocation("structures.overworld_portal", portalLoc);
        
        plugin.getLogger().info("Overworld portal placed at: " + portalLoc);
    }
}
