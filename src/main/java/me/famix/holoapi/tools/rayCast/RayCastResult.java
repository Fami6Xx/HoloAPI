package me.famix.holoapi.tools.rayCast;

import org.bukkit.block.Block;

public class RayCastResult {
    private boolean hit;
    private boolean foundEndLocation;
    private Block hitBlock;


    public RayCastResult(boolean hit, Block hitBlock, boolean foundEndLocation){
        this.hit = hit;
        this.hitBlock = hitBlock;
        this.foundEndLocation = foundEndLocation;
    }

    public boolean hasHit() {
        return hit;
    }

    public boolean hasFoundEndLocation() {
        return foundEndLocation;
    }

    public Block getHitBlock() {
        return hitBlock;
    }
}
