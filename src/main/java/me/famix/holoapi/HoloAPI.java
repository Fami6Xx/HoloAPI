package me.famix.holoapi;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import me.famix.holoapi.handlers.followHandler;
import me.famix.holoapi.types.FollowingHolo;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class HoloAPI extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        followHandler.start();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        followHandler.stop();
    }

    @EventHandler
    public void onConnect(PlayerLoginEvent event){
        FollowingHolo holo = new FollowingHolo(event.getPlayer(), 5, false, false);
        holo.getHologram().appendTextLine("NIGGA");
        System.out.println("Yep");
    }

    @EventHandler
    public void onBob(EntitySpawnEvent event){
        FollowingHolo holo = new FollowingHolo(event.getEntity(), 5, false, false);
        holo.getHologram().appendTextLine(event.getEntity().getName());
    }

    private static HashMap<UUID, List<Hologram>> list = new HashMap<>();
    private static List<Hologram> staticHolograms = new ArrayList<>();
    private static List<Hologram> getList(UUID uuid) {
        return list.get(uuid);
    }
    private static void addToList(UUID uuid, Hologram holo){
        List<Hologram> l = getList(uuid);
        if(l == null){
            l = new ArrayList<>();
            l.add(holo);
            list.put(uuid, l);
        }else {
            l.add(holo);
            list.replace(uuid, l);
        }
    }
    private static void removeFromList(UUID uuid, Hologram holo){
        List<Hologram> l = getList(uuid);
        l.remove(holo);
        list.replace(uuid, l);
    }

    public static boolean canCreateHolo(UUID uuid){
        if(getList(uuid) != null)
            return getList(uuid).size() < 3;
        else
            return true;
    }
    public static Hologram createFollowingHolo(Player player, String text, Integer seconds, Integer distance){
        double height = 2.25;

        Location spawnLoc = player.getLocation();
        if(getList(player.getUniqueId()) == null) {
            spawnLoc.setY(spawnLoc.getY() + height + 0.25);
        }else{
            spawnLoc.setY(spawnLoc.getY() + height + 0.25 * getList(player.getUniqueId()).size());
        }

        Hologram holo = HologramsAPI.createHologram(getPlugin(HoloAPI.class), spawnLoc);
        holo.appendTextLine("");
        holo.getVisibilityManager().setVisibleByDefault(distance == 0);
        addToList(player.getUniqueId(), holo);
        int time = seconds * 20;
        UUID uuid = player.getUniqueId();
        if(time > 0){
            new BukkitRunnable(){
                int r = time;
                int fullTime = time;
                List<Player> visible = new ArrayList<>();
                @Override
                public void run() {
                    if(r != 0) {
                        if (!holo.isDeleted()) {
                            if (player.isOnline()) {
                                Location loc = player.getLocation();
                                loc.setY(player.getLocation().getY() + height + 0.25 * getList(uuid).indexOf(holo));
                                holo.teleport(loc);

                                if(!holo.getVisibilityManager().isVisibleByDefault()) {
                                    VisibilityManager manager = holo.getVisibilityManager();
                                    if(fullTime == r){
                                        manager.showTo(player);
                                        Collection<Player> entities = player.getLocation().getNearbyPlayers(distance);
                                        entities.forEach(entity -> {
                                            manager.showTo(entity);
                                            visible.add(entity);
                                            visible.add(entity);
                                        });
                                    }else{
                                        int newDistance = distance + 1;
                                        List<Player> toRemove = new ArrayList<>();
                                        visible.forEach(p -> {
                                            if(p.getWorld().getName().equals(player.getWorld().getName()))
                                                if(p.getLocation().distance(player.getLocation()) > newDistance){
                                                    manager.hideTo(p);
                                                    toRemove.add(p);
                                                }
                                        });

                                        for(Player p : toRemove)
                                            visible.remove(p);

                                        Collection<Player> entities = player.getLocation().getNearbyPlayers(distance);
                                        entities.forEach(entity -> {
                                            manager.showTo(entity);
                                            visible.add(entity);
                                            if(!visible.contains(entity))
                                                visible.add(entity);
                                        });
                                    }
                                }
                            }else{
                                removeFromList(uuid, holo);
                                if(!holo.isDeleted())
                                    holo.delete();
                                cancel();
                            }
                        }else{
                            removeFromList(uuid, holo);
                            cancel();
                        }
                    }else{
                        removeFromList(uuid, holo);
                        if(!holo.isDeleted())
                            holo.delete();
                        cancel();
                    }

                    r--;
                }
            }.runTaskTimer(getPlugin(HoloAPI.class), 0L, 1L);
            return null;
        }else{
            new BukkitRunnable() {
                List<Player> visible = new ArrayList<>();
                int fullTime = 1;
                @Override
                public void run() {
                    if(!holo.isDeleted()) {
                        if(player.isOnline()) {
                            Location loc = player.getLocation();
                            loc.setY(player.getLocation().getY() + height + 0.25 * getList(uuid).indexOf(holo));
                            holo.teleport(loc);

                            if(!holo.getVisibilityManager().isVisibleByDefault()) {
                                VisibilityManager manager = holo.getVisibilityManager();
                                if(fullTime == 1){
                                    manager.showTo(player);
                                    Collection<Player> entities = player.getLocation().getNearbyPlayers(distance);
                                    entities.forEach(entity -> {
                                        manager.showTo(entity);
                                        visible.add(entity);
                                    });
                                }else{
                                    int newDistance = distance + 1;
                                    List<Player> toRemove = new ArrayList<>();
                                    visible.forEach(p -> {
                                        if(p.getWorld().getName().equals(player.getWorld().getName()))
                                            if(p.getLocation().distance(player.getLocation()) > newDistance){
                                                manager.hideTo(p);
                                                toRemove.add(p);
                                            }
                                    });

                                    for(Player p : toRemove)
                                        visible.remove(p);

                                    Collection<Player> entities = player.getLocation().getNearbyPlayers(distance);
                                    entities.forEach(entity -> {
                                        manager.showTo(entity);
                                        visible.add(entity);
                                        if(!visible.contains(entity))
                                            visible.add(entity);
                                    });
                                }
                            }
                        }else{
                            holo.delete();
                            removeFromList(uuid, holo);
                            cancel();
                        }
                    }else{
                        removeFromList(uuid, holo);
                        cancel();
                    }

                    if(fullTime == 1){
                        fullTime = 0;
                    }
                }
            }.runTaskTimer(getPlugin(HoloAPI.class), 0L, 1L);
            return holo;
        }
    }

    public static List<Hologram> getStaticHolograms(){
        return staticHolograms;
    }

    public static void addStaticHologram(Hologram holo){
        staticHolograms.add(holo);
    }

    public static void removeStaticHologram(Hologram holo){
        staticHolograms.remove(holo);
    }

    public static void destroy(){
        list.forEach((uuid, holograms) -> {
            holograms.forEach(Hologram::delete);
        });
        list.clear();

        staticHolograms.forEach(Hologram::delete);
        staticHolograms.clear();
    }
}
