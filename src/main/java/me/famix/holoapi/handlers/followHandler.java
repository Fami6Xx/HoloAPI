package me.famix.holoapi.handlers;

import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import me.famix.holoapi.HoloAPI;
import me.famix.holoapi.types.FollowingHolo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
    // - Static Holo
    // - Fix ConcurrentModExc when turning off system


    private static final HashMap<UUID, List<FollowingHolo>> map = new HashMap<>();
    public static List<FollowingHolo> getList(UUID uuid) {
        return map.get(uuid) == null ? new ArrayList<>() : map.get(uuid);
    }
    public static void addToList(UUID uuid, FollowingHolo holo){
        List<FollowingHolo> l = getList(uuid);
        l.add(holo);
        if(map.containsKey(uuid))
            map.replace(uuid, l);
        else
            map.put(uuid, l);
    }
    public static void removeFromList(UUID uuid, FollowingHolo holo){
        List<FollowingHolo> l = getList(uuid);
        l.remove(holo);
        if(l.size() != 0)
            map.replace(uuid, l);
        else
            map.remove(uuid);
    }
    public static void clearList(UUID uuid){
        List<FollowingHolo> list = map.get(uuid);

        FollowingHolo[] arr = list.toArray(new FollowingHolo[0]);

        for(FollowingHolo followingHolo : arr){
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

            public boolean rayCast(Vector vector, Location startLoc, Location endLoc, double increment, double maxDistence){
                boolean returnValue = true;

                vector.normalize();
                vector.multiply(increment);

                double count = 0;
                //double visCount = 0; - For testing

                while(count <= maxDistence){
                    count += increment;

                    Block block = startLoc.getBlock();
                    Location blockLoc = block.getLocation();

                    startLoc.add(vector);

/* Visualize raycast
                    visCount++;
                    if(visCount % 10 == 0)
                    new ParticleBuilder(Particle.REDSTONE)
                            .color(Color.RED)
                            .count(0)
                            .location(startLoc)
                            .receivers(player)
                            .spawn();
                            - Put this on start, if u want to get it precisely

                    if(visCount % 10 == 0) {
                        float forAdd = 0.25f;
                        for (double x = blockLoc.getBlockX(); x <= blockLoc.getBlockX() + 1; x += forAdd) {
                            for (double y = blockLoc.getBlockY(); y <= blockLoc.getBlockY() + 1; y += forAdd) {
                                for (double z = blockLoc.getBlockZ(); z <= blockLoc.getBlockZ() + 1; z += forAdd) {
                                    boolean edge = false;
                                    if ((x == blockLoc.getBlockX() || x == blockLoc.getBlockX() + 1) &&
                                            (y == blockLoc.getBlockY() || y == blockLoc.getBlockY() + 1)) edge = true;
                                    if ((z == blockLoc.getBlockZ() || z == blockLoc.getBlockZ() + 1) &&
                                            (y == blockLoc.getBlockY() || y == blockLoc.getBlockY() + 1)) edge = true;
                                    if ((x == blockLoc.getBlockX() || x == blockLoc.getBlockX() + 1) &&
                                            (z == blockLoc.getBlockZ() || z == blockLoc.getBlockZ() + 1)) edge = true;
                                    if (edge) {
                                        Location newLoc = new Location(blockLoc.getWorld(), x, y, z);
                                        new ParticleBuilder(Particle.REDSTONE)
                                                .color(Color.BLACK)
                                                .count(0)
                                                .receivers(player)
                                                .location(newLoc)
                                                .spawn();
                                    }
                                }
                            }
                        }
                    }
*/
                    if(
                            blockLoc.getBlockX() == endLoc.getBlockX() &&
                            blockLoc.getBlockY() == endLoc.getBlockY() &&
                            blockLoc.getBlockZ() == endLoc.getBlockZ()
                    ){
                        return returnValue;
                    }
                    if(block.isLiquid()) continue;
                    if(block.isEmpty()) continue;
                    if(block.getType().isTransparent()) continue;
                    if(!block.getType().isOccluding()) continue;
                    returnValue = false;
                }
                return returnValue;
            }

            public boolean checkConditions(Player player, FollowingHolo holo){
                boolean returnValue = true;

                if(player == holo.getFollowing()){
                    return true;
                }

                Vector playerVector = player.getEyeLocation().toVector();
                Vector entityVector = holo.getFollowing().getLocation().add(0, holo.getFollowing().getHeight(), 0).toVector();
                Vector vector = playerVector.clone().subtract(entityVector.clone());

                if(!holo.canSeeThroughBlocks()){
                    Location startLoc = holo.getFollowing().getLocation().add(0, holo.getFollowing().getHeight(), 0).clone();

                    boolean rayResult = rayCast(vector, startLoc, player.getEyeLocation(), 0.1, holo.getDistance());

                    if(!rayResult){
                        returnValue = false;
                    }
                }
                return returnValue;
            }

            @Override
            public void run() {
                ((HashMap<UUID, List<FollowingHolo>>) map.clone()).forEach(((uuid, followingHolos) -> {
                    Entity entity = Bukkit.getEntity(uuid);

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

                    for(FollowingHolo holo : followingHolos) {
                        if(holo.getHologram().isDeleted()){
                            removeList(holo.getUUID());
                            removeFromList(uuid, holo);
                        }

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
        ((HashMap<UUID, List<FollowingHolo>>) map.clone()).forEach((uuid, holograms) -> holograms.forEach(FollowingHolo::destroy));
    }
}
