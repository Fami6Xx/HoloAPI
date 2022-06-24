package me.famix.holoapi.types.holograms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.famix.holoapi.HoloAPI;
import me.famix.holoapi.handlers.followHandler;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class FollowingHologram {

    Hologram holo;
    UUID uuid;

    boolean seeThrough;
    double distance;
    Entity following;
    UUID followingUUID;


    public FollowingHologram(Entity toFollow, double visibleDistance, boolean isVisibleByDefault, boolean seeThroughBlocks){
        following = toFollow;
        seeThrough = seeThroughBlocks;
        if(!isVisibleByDefault)
            this.distance = visibleDistance;
        else
            this.distance = -1;

        uuid = UUID.randomUUID();


        holo = HologramsAPI.createHologram(
                HoloAPI.getPlugin(HoloAPI.class),
                toFollow.getLocation().add(0, toFollow.getHeight() + (0.25 * followHandler.getList(uuid).size()) + 0.5, 0)
        );

        holo.getVisibilityManager().setVisibleByDefault(isVisibleByDefault);

        followHandler.addToList(toFollow.getUniqueId(), this);
        followingUUID = toFollow.getUniqueId();
    }

    public Hologram getHologram(){
        return holo;
    }

    public Entity getFollowing(){
        return following;
    }

    public boolean canSeeThroughBlocks() {
        return seeThrough;
    }

    public double getDistance(){
        return distance;
    }

    public int getIntDistance(){
        return (int) Math.ceil(distance);
    }

    public UUID getUUID() {
        return uuid;
    }

    public void destroy(){
        followHandler.removeFromList(followingUUID, this);
        holo.delete();
    }

    @Override
    public String toString(){
        return "FollowingHologram [uuid: " + uuid + ", canSeeThrough:" + seeThrough + ", maxVisibleDistance:" + distance + ", " + holo.toString() + "]";
    }
}
