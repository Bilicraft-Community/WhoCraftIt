package com.bilicraft.whocraftit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public final class WhoCraftIt extends JavaPlugin implements Listener {
    private final NamespacedKey NAMESPACED_KEY = new NamespacedKey(this, "craftit");

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            return false;
        }
        Player player = (Player)sender;
        ItemStack stack = player.getInventory().getItemInMainHand();
        if(stack.getType().isAir()){
            sender.sendMessage(ChatColor.YELLOW+"主手必须持有有效物品");
            return true;
        }
        ItemMeta meta = stack.getItemMeta();
        if(meta == null){
            sender.sendMessage(ChatColor.GOLD+"主手必须持有有效物品");
            return true;
        }
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        if (!persistentDataContainer.has(NAMESPACED_KEY, DataContainerType.INSTANCE)) {
            sender.sendMessage(ChatColor.RED+"该物品暂无记录");
            return true;
        }
        DataContainer container =persistentDataContainer.get(NAMESPACED_KEY, DataContainerType.INSTANCE);
        if(container == null){
            sender.sendMessage(ChatColor.YELLOW+"该物品暂无记录");
            return true;
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                sender.sendMessage(ChatColor.AQUA+"制作人: "+ChatColor.YELLOW+Bukkit.getOfflinePlayer(container.getCrafter()).getName());
                sender.sendMessage(ChatColor.AQUA+"耐久消耗者TOP5: ");
                Map<UUID, Long> sorted= container.getUseDamage().entrySet()
                        .stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
                Iterator<Map.Entry<UUID, Long>> iterator = sorted.entrySet().iterator();
                for (int i = 0; i < 5; i++) {
                    if(!iterator.hasNext()){
                        return;
                    }
                    Map.Entry<UUID, Long> entry = iterator.next();
                    sender.sendMessage(ChatColor.YELLOW+" - "+ChatColor.BLUE+Bukkit.getOfflinePlayer(entry.getKey()).getName()+ChatColor.GRAY+": "+ChatColor.LIGHT_PURPLE+entry.getValue());
                }
            }
        }.runTaskAsynchronously(this);
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerCraft(PrepareItemCraftEvent event) {
        ItemStack itemStack = event.getInventory().getResult();
        if (itemStack == null || itemStack.getItemMeta() == null) {
            return;
        }
        changeItemDamageRecord(event.getView().getPlayer().getUniqueId(), itemStack, 0, true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemDamage(PlayerItemDamageEvent event) {
        changeItemDamageRecord(event.getPlayer().getUniqueId(), event.getItem(), event.getDamage(), true);
    }

//    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
//    public void onItemMending(PlayerItemMendEvent event){
//        changeItemDamageRecord(event.getPlayer().getUniqueId(),event.getItem(),event.getRepairAmount(),false);
//    }
//

    private void changeItemDamageRecord(UUID uuid, ItemStack stack, long point, boolean damage) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        if(stack.getType().getMaxDurability() == 0){
            return;
        }
        DataContainer dataContainer;
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        if (!persistentDataContainer.has(NAMESPACED_KEY, DataContainerType.INSTANCE)) {
            dataContainer = new DataContainer(uuid, new HashMap<>());
        } else {
            dataContainer = persistentDataContainer.get(NAMESPACED_KEY, DataContainerType.INSTANCE);
        }
        if (dataContainer == null) {
            return;
        }
        Map<UUID, Long> useDamage = dataContainer.getUseDamage();
        long record = useDamage.getOrDefault(uuid, 0L);
        if (damage) {
            record += point;
        } else {
            record -= point;
        }
        // 防止数据过多 NBT 爆炸
        if(useDamage.size() >= 32){
            Map<UUID, Long> newMap = useDamage.entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
            useDamage.clear();
            newMap.entrySet().stream().limit(16).forEach(set->useDamage.put(set.getKey(),set.getValue()));
        }
        useDamage.put(uuid, record);
        persistentDataContainer.set(NAMESPACED_KEY, DataContainerType.INSTANCE, dataContainer);
        stack.setItemMeta(meta);
    }
}
