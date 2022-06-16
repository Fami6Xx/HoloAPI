package me.famix.holoapi.tools.rayCast;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class RayCast {
    private final Vector vector;
    private final double distance;
    private final double increment;
    private final World world;

    private final ArrayList<Material> ignoredMaterials = new ArrayList<>();
    private boolean ignoreSeeThroughBlocks = false;

    private Location endLoc;
    private boolean foundEndLocation = false;

    private boolean visualizeRay = false;
    private boolean visualizeBlocks = false;
    private Player player;

    public RayCast(Vector vector, World world, double maxDistance){
        this.vector = vector;
        this.distance = maxDistance;
        this.increment = 0.05;
        this.world = world;
    }

    public RayCast(Vector vector, World world, double maxDistance, double increment){
        this.vector = vector;
        this.distance = maxDistance;
        this.increment = increment;
        this.world = world;
    }

    public RayCast(Vector vector, World world, Location endLocation, double maxDistance){
        this.vector = vector;
        this.distance = maxDistance;
        this.increment = 0.05;
        this.endLoc = endLocation;
        this.world = world;
    }

    public RayCast(Vector vector, World world, Location endLocation, double maxDistance, double increment){
        this.vector = vector;
        this.distance = maxDistance;
        this.increment = increment;
        this.endLoc = endLocation;
        this.world = world;
    }

    public RayCast showRayCast(Player player){
        this.visualizeRay = true;
        this.player = player;
        return this;
    }

    public RayCast showLoopedBlocks(Player player){
        this.visualizeBlocks = true;
        this.player = player;
        return this;
    }

    public RayCast enableIgnoreSeeThroughMaterials(){
        this.ignoreSeeThroughBlocks = true;
        return this;
    }

    public RayCast addIgnoredMaterials(Material... materials){
        this.ignoredMaterials.addAll(Arrays.asList(materials));
        return this;
    }

    public RayCastResult shoot(){
        Location startLoc = vector.toLocation(world);

        // Normalize vector
        vector.normalize();

        // Add increment to vector
        vector.multiply(increment);

        double count = 0;

        boolean hit = false;
        Block blockHit = null;

        while(count <= distance){
            Block block = startLoc.getBlock();
            Location blockLoc = block.getLocation();

            if(visualizeRay){
                if(count % 0.2 == 0) {
                    new ParticleBuilder(Particle.REDSTONE)
                            .color(Color.RED)
                            .count(0)
                            .location(startLoc)
                            .receivers(player)
                            .spawn();
                }
            }

            if(visualizeBlocks){
                if(count % 0.5 == 0) {
                    float forAdd = 0.25f;
                    // Calculate borders of block
                    for (double x = blockLoc.getBlockX(); x <= blockLoc.getBlockX() + 1; x += forAdd) {
                        for (double y = blockLoc.getBlockY(); y <= blockLoc.getBlockY() + 1; y += forAdd) {
                            for (double z = blockLoc.getBlockZ(); z <= blockLoc.getBlockZ() + 1; z += forAdd) {
                                boolean edge = false;
                                if ((x == blockLoc.getBlockX() || x == blockLoc.getBlockX() + 1) &&
                                        (y == blockLoc.getBlockY() || y == blockLoc.getBlockY() + 1)) edge = true;
                                if ((z == blockLoc.getBlockZ() || z == blockLoc.getBlockZ() + 1) &&
                                        (y == blockLoc.getBlockY() || y == blockLoc.getBlockY() + 1)) edge = true;
                                if ((x == blockLoc.getBlockX() || x == blockLoc.getBlockX() + 1) &&
                                        (z == blockLoc.getBlockZ() || z == blockLoc.getBlockZ() + 1)) edge = true;
                                if (edge) {
                                    Location newLoc = new Location(blockLoc.getWorld(), x, y, z);
                                    new ParticleBuilder(Particle.REDSTONE)
                                            .color(Color.BLACK)
                                            .count(0)
                                            .receivers(player)
                                            .location(newLoc)
                                            .spawn();
                                }
                            }
                        }
                    }
                }
            }

            count += increment;

            startLoc.add(vector);

            if(endLoc != null) {

                // ToDo: Find better endLoc solution

                // Distance can't be used as it uses Pythagoras's theorem which is costly

                if (
                        blockLoc.getBlockX() == endLoc.getBlockX() &&
                                blockLoc.getBlockY() == endLoc.getBlockY() &&
                                blockLoc.getBlockZ() == endLoc.getBlockZ()
                ) {
                    foundEndLocation = true;
                    break;
                }
            }

            // Check if block is solid and check conditions
            if(block.isEmpty()) continue;
            if(ignoredMaterials.contains(block.getType())) continue;
            if(ignoreSeeThroughBlocks){
                if(block.isLiquid()) continue;
                if(!block.getType().isOccluding()) continue;
                if(block.getType().isTransparent()) continue;
            }

            hit = true;
            blockHit = block;
            break;
        }

        return new RayCastResult(hit, blockHit, foundEndLocation);
    }
}