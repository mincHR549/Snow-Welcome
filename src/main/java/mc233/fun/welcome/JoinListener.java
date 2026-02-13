package mc233.fun.welcome;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Welcome plugin = Welcome.getInstance();

        if (plugin.isFirstJoin(player.getUniqueId())) {
            plugin.registerNewPlayer(player);
            plugin.broadcastWelcome(player);
        }
    }


}
