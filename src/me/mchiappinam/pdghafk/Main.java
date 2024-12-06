package me.mchiappinam.pdghafk;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
  implements Listener
{
  public static final Logger log = Logger.getLogger("Minecraft");
  private Map<String, String> lastCoords = new HashMap<String, String>();
  private Map<String, Long> lastDateMove = new HashMap<String, Long>();

  public void onEnable()
  {
    log.info("AFK Kicker v.1.0 has been enabled!");
    getServer().getPluginManager().registerEvents(this, this);

    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    {
      public void run()
      {
        Player[] players = Main.this.getServer().getOnlinePlayers();

        for (Player p : players)
        {
          if (!p.hasPermission("afk_kicker.ignore"))
          {
            String playername = p.getName();

            long dateLast = ((Long)Main.this.lastDateMove.get(playername)).longValue();
            long r = new Date().getTime() - dateLast;

            if (r > 10000)
            {
              p.kickPlayer("[AFK Kicker] You was AFK kicked.");
              Main.log.info(playername + " was AFK kicked.");
            }
          }
        }
      }
    }
    , 400L, 400L);
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event)
  {
    this.lastDateMove.put(event.getPlayer().getName(), Long.valueOf(new Date().getTime()));
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
  {
    Player player = event.getPlayer();

    if ((player.getVehicle() == null) || (!player.getVehicle().getType().toString().equals("MINECART")))
    {
      Boolean continueCheck = Boolean.valueOf(true);

      int x = (int)player.getLocation().getX();
      int y = (int)player.getLocation().getY();
      int z = (int)player.getLocation().getZ();

      String coordsString = x + "|" + y + "|" + z;

      if (this.lastCoords.containsKey(player.getName()))
      {
        String last_coords = (String)this.lastCoords.get(player.getName());

        if (last_coords.equals(coordsString)) {
          continueCheck = Boolean.valueOf(false);
        }
        else {
          String[] exa = last_coords.split("\\|");

          int x_old = Integer.parseInt(exa[0]);
          int y_old = Integer.parseInt(exa[1]);
          int z_old = Integer.parseInt(exa[2]);

          if ((Math.abs(x - x_old) <= 3) && (Math.abs(y - y_old) <= 3) && (Math.abs(z - z_old) <= 3)) {
            continueCheck = Boolean.valueOf(false);
          }
        }
      }
      if (continueCheck.booleanValue())
      {
        this.lastCoords.put(player.getName(), coordsString);
        this.lastDateMove.put(player.getName(), Long.valueOf(new Date().getTime()));
      }
    }
  }

  @EventHandler
  public void onPlayerLogout(PlayerQuitEvent event)
  {
    String playername = event.getPlayer().getName();

    if (this.lastCoords.containsKey(playername)) {
      this.lastCoords.remove(playername);
    }
    this.lastDateMove.remove(playername);
  }

  public void onDisable()
  {
    log.info("AFK Kicker has been disabled!");
  }
}