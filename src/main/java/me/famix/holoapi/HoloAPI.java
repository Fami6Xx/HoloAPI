package me.famix.holoapi;

import me.famix.holoapi.handlers.followHandler;
import me.famix.holoapi.types.FollowingHologram;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
        FollowingHologram holo = new FollowingHologram(event.getPlayer(), 5, false, false);
        holo.getHologram().appendTextLine("Good man");
    }

    @EventHandler
    public void onBob(EntitySpawnEvent event){
        FollowingHologram holo = new FollowingHologram(event.getEntity(), 5, false, false);
        holo.getHologram().appendTextLine(event.getEntity().getName());
    }
}
