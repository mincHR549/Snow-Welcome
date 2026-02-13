package mc233.fun.welcome;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RewardUtil {

    public static void giveReward(Player sender, String newPlayer) {
        var config = Welcome.getInstance().getConfig();

        if (config.getBoolean("rewards.money.enable", false)) {
            int amount = randomAmount(config.getInt("rewards.money.min", 0),
                    config.getInt("rewards.money.max", 0));
            String cmd = config.getString("rewards.money.command", "")
                    .replace("%player%", sender.getName())
                    .replace("%amount%", String.valueOf(amount));
            if (!cmd.isBlank()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        if (config.getBoolean("rewards.playerpoints.enable", false)) {
            int amount = randomAmount(config.getInt("rewards.playerpoints.min", 0),
                    config.getInt("rewards.playerpoints.max", 0));
            String cmd = config.getString("rewards.playerpoints.command", "")
                    .replace("%player%", sender.getName())
                    .replace("%amount%", String.valueOf(amount));
            if (!cmd.isBlank()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        if (config.getBoolean("rewards.command.enable", false)) {
            List<String> cmds = config.getStringList("rewards.command.commands");
            for (String c : cmds) {
                if (c == null || c.isBlank()) continue;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        c.replace("%sender%", sender.getName())
                                .replace("%newplayer%", newPlayer));
            }
        }
    }

    private static int randomAmount(int min, int max) {
        if (max < min) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
