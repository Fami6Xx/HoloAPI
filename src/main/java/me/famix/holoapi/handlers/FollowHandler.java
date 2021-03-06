package me.famix.holoapi.handlers;

import me.famix.holoapi.HoloAPI;
import me.famix.holoapi.types.holograms.famiHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FollowHandler extends famiHandler{
    //ToDo:
    // - Optimize

    public double calculateHeight(UUID uuid){
        double[] height = {Bukkit.getEntity(uuid).getHeight() + 0.5};

        getMap().get(uuid).forEach(holo -> height[0] += holo.getHologram().size() * 0.25);

        return height[0];
    }

    @Override
    public BukkitTask startTask(){
        return new BukkitRunnable(){
            @Override
            public void run() {
                // Checking queue and if there is something then executing it safely in this thread
                handleQueue();

                // Cloning HashMap because we are modifying it inside the forEach loop
                ((HashMap<UUID, ArrayList<famiHologram>>) getMap().clone()).forEach(((uuid, famiHolograms) -> {
                    Entity entity = Bukkit.getEntity(uuid);

                    // Check entity
                    if(entity == null){
                        clearList(uuid);
                        return;
                    }
                    if(!entity.isValid()){
                        if(entity instanceof Player) {
                            if(!((Player) entity).isOnline()){
                                clearList(uuid);
                                return;
                            }
                        }else{
                            clearList(uuid);
                            return;
                        }
                    }

                    double height = entity.getHeight() + 0.5;

                    famiHologram[] arr = famiHolograms.toArray(new famiHologram[0]);

                    for(famiHologram holo : arr) {
                        if(holo.getHologram().isDeleted()){
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
                    }
                }));
            }
        }.runTaskTimerAsynchronously(HoloAPI.getPlugin(HoloAPI.class), 0L, 1L);
    }
}
