package me.frep.thotpatrol.checks.movement.speed;

import me.frep.thotpatrol.checks.Check;
import me.frep.thotpatrol.checks.movement.ascension.AscensionA;
import me.frep.thotpatrol.events.SharedEvents;
import me.frep.thotpatrol.utils.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpeedH extends Check {

    private Map<UUID, Long> airTicks = new HashMap<>();
    private Map<UUID, Integer> verbose = new HashMap<>();

    public SpeedH(me.frep.thotpatrol.ThotPatrol ThotPatrol) {
        super("SpeedH", "Speed (Type H) [#]", ThotPatrol);
        setEnabled(true);
        setBannable(false);
        setMaxViolations(8);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        airTicks.remove(e.getPlayer().getUniqueId());
        verbose.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!UtilPlayer.isOnGround(p)) {
            airTicks.put(p.getUniqueId(), System.currentTimeMillis());
        }
        if (e.getTo().getX() == e.getFrom().getX() && e.getFrom().getZ() == e.getTo().getZ()
            || e.getFrom().getY() != e.getTo().getY()
            || !(p.getLocation().getY() % 1 == 0)
            || !(e.getTo().getY() % 1 == 0)
            || !(e.getFrom().getY() % 1 == 0)
            || !UtilPlayer.isOnGround(p)
            || p.getVehicle() != null
            || p.getAllowFlight()
            || e.isCancelled()
            || SharedEvents.worldChange.contains(p.getUniqueId())
            || SpeedC.jumpingOnIce.contains(p.getUniqueId())
            || !UtilTime.elapsed(airTicks.getOrDefault(p.getUniqueId(), 0L), 500)
            || !UtilTime.elapsed(SharedEvents.getLastJoin().getOrDefault(p.getUniqueId(), 0L), 1500)
            || !UtilTime.elapsed(AscensionA.toggleFlight.getOrDefault(p.getUniqueId(), 0L), 5000L)
            || !UtilTime.elapsed(getThotPatrol().LastVelocity.getOrDefault(p.getUniqueId(), 0L), 2000)
            || p.hasPermission("thotpatrol.bypass")
            || SpeedC.highKb.contains(p.getUniqueId())
            || !UtilPlayer.isOnGround(p.getLocation())
            || !p.isOnGround()) {
            return;
        }
        int count = verbose.getOrDefault(p.getUniqueId(), 0);
        double speed = UtilMath.getHorizontalDistance(e.getFrom(), e.getTo());
        double maxSpeed = .29;
        double tps = getThotPatrol().getLag().getTPS();
        int ping = getThotPatrol().getLag().getPing(p);
        //TODO calc these values
        for (Block b : UtilBlock.getNearbyBlocks(p.getLocation(), 2)) {
            if (b.getType().toString().contains("ICE")) {
                return;
            }
        }
        if (p.getLocation().clone().add(0, .5, 0).getBlock().getType().toString().contains("DOOR")
            || p.getLocation().clone().add(0, 1, 0).getBlock().getType().toString().contains("DOOR")) {
            maxSpeed += .08;
        }
        //todo :(
        if (p.getWalkSpeed() > .21) {
            maxSpeed += p.getWalkSpeed() * 1.5;
        }
        for (PotionEffect effect : p.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.SPEED)) {
                maxSpeed += (effect.getAmplifier() + 1) * .06;
            }
        }
        if (speed > maxSpeed + .2) {
            count += 3;
            dumplog(p, "[Count Increase (High)] Speed: " + speed + " > " + maxSpeed + " | Ping: " + ping + " | TPS: " + tps);
        } else {
            if (speed > maxSpeed) {
                count++;
                dumplog(p, "[Count Increase] Speed: " + speed + " > " + maxSpeed + " | Ping: " + ping + " | TPS: " + tps);
            } else {
                if (count > 0) {
                    count -= 1.5;
                }
            }
        }
        if (count > 12) {
            getThotPatrol().logCheat(this, p, speed + " > " + maxSpeed + " | Ping: " + ping + " | TPS: " + tps);
            getThotPatrol().logToFile(p, this, "Ground", "Speed: " + speed + " > " +  maxSpeed + " | TPS: " + tps + " | Ping: " + ping);
            dumplog(p, "[Flag] Speed: " + speed + " > " + maxSpeed + " | Ping: " + ping + " | TPS: " + tps);
            count = 0;
        }
        verbose.put(p.getUniqueId(), count);
    }
}