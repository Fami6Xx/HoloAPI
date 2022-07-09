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

public class followHandler extends famiHandler{
    //ToDo:
    // - Optimize

    public double calculateHeight(UUID uuid){
        double[] height = {Bukkit.getEntity(uuid).getHeight() + 0.5};

        getMap().get(uuid).forEach(holo -> {
            height[0] += holo.getHologram().size() * 0.25;
        });

        return height[0];
    }

    @Override
    public BukkitTask startTask(){
        return new BukkitRunnable(){
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
                            .shoot();

                    if(result.hasHit())
                        returnValue = false;
                }
                return returnValue;
            }

            @Override
            public void run() {
                // Checking queue and if there is something then executing it safely in this thread
                if(queue.size() > 0){
                    try {
                        while(queue.size() > 0) {
                            queue.take().execute();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Cloning HashMap because we are modifying it inside the forEach loop
                ((HashMap<UUID, ArrayList<FollowingHologram>>) getMap().clone()).forEach(((uuid, followingHolos) -> {
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

                    double height = entity.getHeight() + 0.5;

                    FollowingHologram[] arr = followingHolos.toArray(new FollowingHologram[0]);

                    for(FollowingHologram holo : arr) {
                        if(holo.getHologram().isDeleted()){
                            removeList(holo.getUUID());
                            removeFromList(uuid, holo);
                            continue;
                        }

                        // Move Hologram

                        Location toTeleport = entity.getLocation();
                        height += holo.getHologram().size() * 0.25;
                        toTeleport.setY(toTeleport.getY() + height);

                        if(
                                toTeleport.getX() != holo.getHologram().getX() ||
                                toTeleport.getY() != holo.getHologram().getY() ||
                                toTeleport.getZ() != holo.getHologram().getZ()
                        ) {
                            holo.getHologram().teleport(toTeleport);
                        }

                        if (!holo.getHologram().getVisibilityManager().isVisibleByDefault()) {
                            VisibilityManager manager = holo.getHologram().getVisibilityManager();

                            List<Player> prevVisible = getList(holo.getUUID());
                            Collection<Player> nowVisible =
                                    !manager.isVisibleByDefault() ?
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
}
