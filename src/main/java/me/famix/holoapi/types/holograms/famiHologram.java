package me.famix.holoapi.types.holograms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

public abstract class famiHologram {
    private final Hologram hologram;

    public famiHologram(Hologram holo){
        hologram = holo;
    }

    public Hologram getHologram(){
        return hologram;
    }

    public abstract void destroy();
}
