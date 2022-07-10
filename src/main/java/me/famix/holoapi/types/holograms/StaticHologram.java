package me.famix.holoapi.types.holograms;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.famix.holoapi.HoloAPI;
import me.famix.holoapi.misc.AExecuteQueue;
import org.bukkit.Location;

public class StaticHologram extends famiHologram{
    HoloAPI api = (HoloAPI) HoloAPI.getProvidingPlugin(HoloAPI.class);

    public StaticHologram(Location loc, boolean isVisibleByDefault, double visibleDistance, boolean seeThroughBlocks){
        super(
                HologramsAPI.createHologram(HoloAPI.getPlugin(HoloAPI.class), loc)
        );

        getHologram().getVisibilityManager().setVisibleByDefault(isVisibleByDefault);
        updateVisibility(visibleDistance, seeThroughBlocks);

        StaticHologram staticHologram = this;

        api.getVisibilityHandler().queue.add(
                new AExecuteQueue() {
                    @Override
                    public void execute() {
                        api.getVisibilityHandler().addToList(getUUID(), staticHologram);
                    }
                }
        );
    }

    @Override
    public Location getBaseLocation(){
        return getHologram().getLocation().clone();
    }

    @Override
    public void destroy() {
        StaticHologram staticHologram = this;

        api.getVisibilityHandler().queue.add(
                new AExecuteQueue() {
                    @Override
                    public void execute() {
                        api.getVisibilityHandler().removeFromList(getUUID(), staticHologram);
                    }
                }
        );
    }
}
