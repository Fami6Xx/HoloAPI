package me.famix.holoapi.types.holograms;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.famix.holoapi.HoloAPI;
import me.famix.holoapi.misc.AExecuteQueue;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class FollowingHologram extends famiHologram{

    FollowingHologram followingHologram = this;
    UUID uuid = UUID.randomUUID();

    HoloAPI api = (HoloAPI) HoloAPI.getProvidingPlugin(HoloAPI.class);

    boolean seeThrough;
    double distance;
    Entity following;
    UUID followingUUID;


    public FollowingHologram(Entity toFollow, double visibleDistance, boolean isVisibleByDefault, boolean seeThroughBlocks){
        super(
                HologramsAPI.createHologram(
                        HoloAPI.getPlugin(HoloAPI.class),
                        toFollow.getLocation().add(
                                0,
                                ((HoloAPI) HoloAPI.getProvidingPlugin(HoloAPI.class)).getFollowHandler().calculateHeight(toFollow.getUniqueId()),
                                0
                        )
                )
        );
        following = toFollow;
        seeThrough = seeThroughBlocks;
        if(!isVisibleByDefault)
            this.distance = visibleDistance;
        else
            this.distance = -1;

        getHologram().getVisibilityManager().setVisibleByDefault(isVisibleByDefault);

        api.getFollowHandler().queue.add(new AExecuteQueue() {
            @Override
            public void execute() {
                api.getFollowHandler().addToList(toFollow.getUniqueId(), followingHologram);
            }
        });
        followingUUID = toFollow.getUniqueId();
    }

    public Entity getFollowing(){
        return following;
    }

    public void setVisibleThroughBlocks(boolean visible){
        this.seeThrough = visible;
    }

    public boolean canSeeThroughBlocks() {
        return seeThrough;
    }

    public void setVisibleByDefault(boolean visible){
        getHologram().getVisibilityManager().setVisibleByDefault(visible);
        getHologram().getVisibilityManager().resetVisibilityAll();
    }

    public void setVisibleDistance(double distance){ this.distance = distance; }

    public double getDistance(){
        return distance;
    }

    public int getIntDistance(){
        return (int) Math.ceil(distance);
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void destroy(){
        api.getFollowHandler().queue.add(new AExecuteQueue() {
            @Override
            public void execute() {
                api.getFollowHandler().removeFromList(followingUUID, followingHologram);
            }
        });
        getHologram().delete();
    }

    @Override
    public String toString(){
        return "FollowingHologram [uuid: " + uuid + ", canSeeThrough:" + seeThrough + ", maxVisibleDistance:" + distance + ", " + getHologram().toString() + "]";
    }
}
