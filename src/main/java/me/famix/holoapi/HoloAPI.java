package me.famix.holoapi;

import me.famix.holoapi.handlers.FollowHandler;
import me.famix.holoapi.handlers.VisibilityHandler;
import me.famix.holoapi.types.holograms.FollowingHologram;
import me.famix.holoapi.types.lines.UpdatingLine;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class HoloAPI extends JavaPlugin implements Listener {
    Random random = new Random();
    FollowHandler followHandler;
    VisibilityHandler VisibilityHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.followHandler = new FollowHandler();
        this.VisibilityHandler = new VisibilityHandler();

        this.followHandler.start();
        this.VisibilityHandler.start();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.followHandler.stop();
        this.VisibilityHandler.stop();
    }

    public FollowHandler getFollowHandler(){
        return this.followHandler;
    }
    public VisibilityHandler getVisibilityHandler(){return this.VisibilityHandler;}
    // Methods below are there only for testing purposes
    @EventHandler
    public void onConnect(PlayerLoginEvent event){
        FollowingHologram holo = new FollowingHologram(event.getPlayer(), 5, false, false);
        for(int i = 0; i < random.nextInt(10); i++){
            if(random.nextBoolean()) {
                new UpdatingLine(holo.getHologram().appendTextLine("")) {
                    @Override
                    public String update() {
                        return event.getPlayer().getHealth() + "";
                    }
                };
            }else{
                FollowingHologram boomRandom = new FollowingHologram(event.getPlayer(), 5, false, false);
                boomRandom.getHologram().appendTextLine("Randomly created line");
            }
        }
    }
    @EventHandler
    public void onBob(EntitySpawnEvent event){
        FollowingHologram holo = new FollowingHologram(event.getEntity(), 5, false, false);
        for (int i = 0; i < random.nextInt(4); i++){
            if(random.nextBoolean()) {
                new UpdatingLine(holo.getHologram().appendTextLine(""), 5) {
                    @Override
                    public String update() {
                        try {
                            return ((LivingEntity) event.getEntity()).getHealth() + "";
                        } catch (Exception exc) {
                            return "Not Living Entity";
                        }
                    }
                };
            }else {
                FollowingHologram boomRandom = new FollowingHologram(event.getEntity(), 5, false, false);
                boomRandom.getHologram().appendTextLine("Randomly created line");

            }
        }
    }
}
