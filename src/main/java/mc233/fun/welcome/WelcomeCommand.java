package mc233.fun.welcome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class WelcomeCommand implements CommandExecutor {

    private final Random random = new Random();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length != 1) {
            player.sendMessage("§c用法: /welcome <玩家名>");
            return true;
        }

        Player newPlayer = Bukkit.getPlayerExact(args[0]);
        if (newPlayer == null) {
            player.sendMessage("§c玩家不在线或名字错误");
            return true;
        }

        if (player.getUniqueId().equals(newPlayer.getUniqueId())) {
            player.sendMessage("§c不能欢迎自己");
            return true;
        }

        Welcome plugin = Welcome.getInstance();
        var data = plugin.getDataConfig();
        String path = "new-players." + newPlayer.getUniqueId();

        if (!data.contains(path)) {
            player.sendMessage(plugin.getConfig().getString("not-new", "§c不是新玩家或过期了"));
            return true;
        }

        long joinTime = data.getLong(path + ".join-time", 0L);
        long limit = plugin.getConfig().getLong("welcome-limit-seconds", 90L) * 1000L;

        if (System.currentTimeMillis() - joinTime > limit) {
            player.sendMessage(plugin.getConfig().getString("welcome-limit-seconds-message", "§c时间过了！"));
            return true;
        }

        List<String> welcomed = data.getStringList(path + ".welcomed-by");
        if (welcomed.contains(player.getUniqueId().toString())) {
            player.sendMessage(plugin.getConfig().getString("welcomed", "§c你已经欢迎过该玩家了！"));
            return true;
        }

        RewardUtil.giveReward(player, newPlayer.getName());

        welcomed.add(player.getUniqueId().toString());
        data.set(path + ".welcomed-by", welcomed);
        plugin.saveData();

        List<String> messages = plugin.getConfig().getStringList("sender-messages");
        if (!messages.isEmpty()) {
            String raw = messages.get(random.nextInt(messages.size()))
                    .replace("<player>", newPlayer.getName());
           Component component;
            try {
                component = MiniMessage.miniMessage().deserialize(raw);
            } catch (Exception e) {
                component = Component.text(raw);
            }
            String legacy = LegacyComponentSerializer.legacySection().serialize(component);

            if (!legacy.isEmpty() && legacy.charAt(0) == '/') {
                legacy = "\u200B" + legacy;
            }

            player.chat(legacy);
        }



        return true;
    }
}
