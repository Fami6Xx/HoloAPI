package me.famix.holoapi.types.holograms;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.famix.holoapi.HoloAPI;
import me.famix.holoapi.misc.AExecuteQueue;
import org.bukkit.Location;

import java.util.UUID;

public class StaticHologram extends famiHologram{
    UUID uuid = UUID.randomUUID();

    HoloAPI api = (HoloAPI) HoloAPI.getProvidingPlugin(HoloAPI.class);

    private double distance;
    private boolean seeThroughBlocks;

    public StaticHologram(Location loc, boolean isVisibleByDefault, double visibleDistance, boolean seeThroughBlocks){
        super(
                HologramsAPI.createHologram(HoloAPI.getPlugin(HoloAPI.class), loc)
        );

        getHologram().getVisibilityManager().setVisibleByDefault(isVisibleByDefault);
        distance = visibleDistance;
        this.seeThroughBlocks = seeThroughBlocks;

        StaticHologram staticHologram = this;

        api.getStaticHandler().queue.add(
                new AExecuteQueue() {
                    @Override
                    public void execute() {
                        api.getStaticHandler().addToList(uuid, staticHologram);
                    }
                }
        );
    }

    public UUID getUUID() {
        return uuid;
    }

    public double getDistance(){
        return distance;
    }

    public void setDistance(double num){
        this.distance = num;
    }

    public boolean canSeeThroughBlocks(){
        return seeThroughBlocks;
    }

    public void setSeeThroughBlocks(boolean seeThroughBlocks){
        this.seeThroughBlocks = seeThroughBlocks;
    }

    @Override
    public void destroy() {
        StaticHologram staticHologram = this;

        api.getStaticHandler().queue.add(
                new AExecuteQueue() {
                    @Override
                    public void execute() {
                        api.getStaticHandler().removeFromList(uuid, staticHologram);
                    }
                }
        );
    }
}
