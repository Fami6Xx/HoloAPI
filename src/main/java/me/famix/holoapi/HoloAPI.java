package me.famix.holoapi;

import me.famix.holoapi.handlers.followHandler;
import me.famix.holoapi.types.holograms.FollowingHologram;
import me.famix.holoapi.types.lines.UpdatingLine;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class HoloAPI extends JavaPlugin implements Listener {

    // ToDo: Static hologram

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
    // Methods below are there only for testing purposes
    @EventHandler
    public void onConnect(PlayerLoginEvent event){
        FollowingHologram holo = new FollowingHologram(event.getPlayer(), 5, false, false);
        new UpdatingLine(holo.getHologram().appendTextLine("")){
            @Override
            public String update(){
                return event.getPlayer().getHealth() + "";
            }
        };
        new UpdatingLine(holo.getHologram().appendTextLine("")){
            @Override
            public String update(){
                return event.getPlayer().getHealth() + "";
            }
        };
        new UpdatingLine(holo.getHologram().appendTextLine("")){
            @Override
            public String update(){
                return event.getPlayer().getHealth() + "";
            }
        };
        new UpdatingLine(holo.getHologram().appendTextLine("")){
            @Override
            public String update(){
                return event.getPlayer().getHealth() + "";
            }
        };
    }
    @EventHandler
    public void onBob(EntitySpawnEvent event){
        FollowingHologram holo = new FollowingHologram(event.getEntity(), 5, false, false);
        new UpdatingLine(holo.getHologram().appendTextLine(""), 5){
            @Override
            public String update(){
                try{
                    return ((LivingEntity) event.getEntity()).getHealth() + "";
                }catch(Exception exc){return "Not Living Entity";}
            }
        };
    }
}
