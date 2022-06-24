package me.famix.holoapi.types.lines;

import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.famix.holoapi.HoloAPI;
import org.bukkit.scheduler.BukkitRunnable;


public abstract class UpdatingLine {
    private final TextLine line;

    private final long period;

    public UpdatingLine(TextLine textLine){
        this.line = textLine;
        this.period = 20;
        start();
    }

    public UpdatingLine(TextLine textLine, long period){
        this.line = textLine;
        this.period = period;
        start();
    }

    public abstract String update();

    private void start(){
        new BukkitRunnable(){
            @Override
            public void run(){
                if(!line.getParent().isDeleted()) {
                    String update = update();
                    if (!line.getText().equals(update)) {
                        line.setText(update());
                    }
                }else {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(HoloAPI.getPlugin(HoloAPI.class), 1, period);
    }
}
