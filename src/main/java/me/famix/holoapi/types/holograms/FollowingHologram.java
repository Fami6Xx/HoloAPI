package me.famix.holoapi.types.holograms;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.famix.holoapi.HoloAPI;
import me.famix.holoapi.misc.AExecuteQueue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class FollowingHologram extends famiHologram{

    FollowingHologram followingHologram = this;

    HoloAPI api = (HoloAPI) HoloAPI.getProvidingPlugin(HoloAPI.class);

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
        updateVisibility(visibleDistance, seeThroughBlocks);
        if(!isVisibleByDefault)
            updateVisibility(visibleDistance, seeThroughBlocks);
        else
            updateVisibility(-1, seeThroughBlocks);

        getHologram().getVisibilityManager().setVisibleByDefault(isVisibleByDefault);

        api.getFollowHandler().queue.add(new AExecuteQueue() {
            @Override
            public void execute() {
                api.getFollowHandler().addToList(toFollow.getUniqueId(), followingHologram);
            }
        });
        if(!isVisibleByDefault) {
            api.getVisibilityHandler().queue.add(
                    new AExecuteQueue() {
                        @Override
                        public void execute() {
                            api.getVisibilityHandler().addToList(getUUID(), followingHologram);
                        }
                    }
            );
        }

        followingUUID = toFollow.getUniqueId();
    }

    public Entity getFollowing(){
        return following;
    }

    @Override
    public Location getBaseLocation() {
        return following.getLocation().clone().add(0, following.getHeight(), 0);
    }

    @Override
    public void destroy(){
        api.getFollowHandler().queue.add(new AExecuteQueue() {
            @Override
            public void execute() {
                api.getFollowHandler().removeFromList(followingUUID, followingHologram);
            }
        });
        api.getVisibilityHandler().queue.add(new AExecuteQueue() {
            @Override
            public void execute() {
                api.getVisibilityHandler().removeFromList(followingUUID, followingHologram);
            }
        });
        getHologram().delete();
    }

    @Override
    public String toString(){
        return "FollowingHologram [uuid: " + getUUID() + ", canSeeThrough:" + canSeeThroughBlocks() + ", maxVisibleDistance:" + getDistance() + ", " + getHologram().toString() + "]";
    }
}
