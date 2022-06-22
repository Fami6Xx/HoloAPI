package me.famix.holoapi.handlers;

import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import me.famix.holoapi.HoloAPI;
import me.famix.holoapi.tools.rayCast.RayCast;
import me.famix.holoapi.tools.rayCast.RayCastResult;
import me.famix.holoapi.types.holograms.FollowingHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class followHandler {
    //ToDo:
    // - Optimize


    private static final HashMap<UUID, List<FollowingHologram>> map = new HashMap<>();
    public static List<FollowingHologram> getList(UUID uuid) {
        return map.get(uuid) == null ? new ArrayList<>() : map.get(uuid);
    }
    public static void addToList(UUID uuid, FollowingHologram holo){
        List<FollowingHologram> l = getList(uuid);
        l.add(holo);
        if(map.containsKey(uuid))
            map.replace(uuid, l);
        else
            map.put(uuid, l);
    }
    public static void removeFromList(UUID uuid, FollowingHologram holo){
        List<FollowingHologram> l = getList(uuid);
        l.remove(holo);
        if(l.size() != 0)
            map.replace(uuid, l);
        else
            map.remove(uuid);
    }
    public static void clearList(UUID uuid){
        List<FollowingHologram> list = map.get(uuid);

        FollowingHologram[] arr = list.toArray(new FollowingHologram[0]);

        for(FollowingHologram followingHolo : arr){
            followingHolo.destroy();
        }
    }

    private static BukkitTask task;

    public static void start(){
        task = new BukkitRunnable(){
            private final HashMap<UUID, List<Player>> hashMap = new HashMap<>();
            private List<Player> createList(UUID uuid){
                List<Player> l = new ArrayList<>();
                hashMap.put(uuid, l);
                return l;
            }

            public List<Player> getList(UUID uuid) {
                return hashMap.get(uuid) == null ? createList(uuid) : hashMap.get(uuid);
            }
            public void removeList(UUID uuid){
                hashMap.remove(uuid);
            }
            public void updateList(UUID uuid, List<Player> list){
                hashMap.replace(uuid, list);
            }


            public boolean checkConditions(Player player, FollowingHologram holo){
                boolean returnValue = true;

                if(player == holo.getFollowing()){
                    return true;
                }

                Vector playerVector = player.getEyeLocation().toVector();
                Vector entityVector = holo.getFollowing().getLocation().add(0, holo.getFollowing().getHeight(), 0).toVector();
                Vector vector = playerVector.clone().subtract(entityVector.clone());

                if(!holo.canSeeThroughBlocks()){
                    Location startLoc = holo.getFollowing().getLocation().add(0, holo.getFollowing().getHeight(), 0).clone();

                    RayCastResult result = new RayCast(vector, startLoc.getWorld(), startLoc, player.getEyeLocation(), holo.getDistance(), 0.1)
                            .enableIgnoreSeeThroughMaterials()
                            .showRayCast(player)
                            .shoot();

                    if(result.hasHit())
                        returnValue = false;
                }
                return returnValue;
            }

            @Override
            public void run() {
                ((HashMap<UUID, List<FollowingHologram>>) map.clone()).forEach(((uuid, followingHolos) -> {
                    Entity entity = Bukkit.getEntity(uuid);

                    // Check entity
                    if(entity == null){
                        clearList(uuid);
                        removeList(uuid);
                        return;
                    }
                    if(!entity.isValid()){
                        if(entity instanceof Player) {
                            if(!((Player) entity).isOnline()){
                                clearList(uuid);
                                removeList(uuid);
                                return;
                            }
                        }else{
                            clearList(uuid);
                            removeList(uuid);
                            return;
                        }
                    }

                    for(FollowingHologram holo : followingHolos) {
                        if(holo.getHologram().isDeleted()){
                            removeList(holo.getUUID());
                            removeFromList(uuid, holo);
                        }

                        // Move Hologram

                        holo.getHologram().teleport(
                                entity.getLocation().add(
                                        0,
                                        entity.getHeight() + 0.8 + 0.25 * followingHolos.indexOf(holo),
                                        0
                                )
                        );

                        if (!holo.getHologram().getVisibilityManager().isVisibleByDefault()) {
                            VisibilityManager manager = holo.getHologram().getVisibilityManager();

                            List<Player> prevVisible = getList(holo.getUUID());
                            Collection<Player> nowVisible =
                                    holo.getDistance() > 0 ?
                                            holo.getHologram().getLocation().getNearbyPlayers(holo.getDistance())
                                            :
                                            holo.getHologram().getLocation().getNearbyPlayers(Bukkit.getViewDistance() * 16);

                            // Hide to everyone who left visible distance or doesn't pass conditions
                            prevVisible.forEach(player -> {
                                if(!nowVisible.contains(player)) {
                                    manager.hideTo(player);
                                }
                                if(!checkConditions(player, holo)){
                                    manager.hideTo(player);
                                }
                            });

                            // Show hologram to players who don't see it but passed conditions
                            nowVisible.stream()
                                    .filter(player -> checkConditions(player, holo))
                                    .filter(player -> !manager.isVisibleTo(player))
                                    .forEach(manager::showTo);

                            // Filter everyone who doesn't see the hologram and collect them
                            List<Player> finalList =
                                    nowVisible.stream()
                                            .filter(manager::isVisibleTo)
                                            .collect(Collectors.toList());

                            updateList(holo.getUUID(), finalList);
                        }
                    }
                }));
            }
        }.runTaskTimerAsynchronously(HoloAPI.getPlugin(HoloAPI.class), 0L, 1L);
    }

    public static void stop(){
        task.cancel();
        ((HashMap<UUID, List<FollowingHologram>>) map.clone()).forEach((uuid, holograms) -> holograms.forEach(followingHologram -> followingHologram.getHologram().delete()));
        map.clear();
    }
}
