package mc233.fun.welcome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public final class Welcome extends JavaPlugin {

    private static Welcome instance;
    private final ReentrantLock ioLock = new ReentrantLock();

    private File dataFile;
    private FileConfiguration dataConfig;

    public static Welcome getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        initDataFile();

        int pluginId = 29514;
        Metrics metrics = new Metrics(this, pluginId);

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        var cmd = getCommand("welcome");
        if (cmd != null) cmd.setExecutor(new WelcomeCommand());

        getLogger().info("Welcome enabled! Author: SnowyMC");
    }

    @Override
    public void onDisable() {
        saveData();
        getLogger().info("Welcome disabled! Author: SnowyMC");
    }

    private void initDataFile() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create plugin data folder");
        }
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile()) {
                    getLogger().warning("Could not create data.yml");
                }
            } catch (IOException e) {
                getLogger().severe("Failed to create data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public void saveData() {
        ioLock.lock();
        try {
            if (dataConfig != null && dataFile != null) dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Failed to save data.yml: " + e.getMessage());
        } finally {
            ioLock.unlock();
        }
    }

    private String playerPath(UUID uuid) {
        return "new-players." + uuid.toString();
    }

    public boolean isFirstJoin(UUID uuid) {
        return !dataConfig.contains(playerPath(uuid));
    }

    public void registerNewPlayer(Player player) {
        String path = playerPath(player.getUniqueId());
        dataConfig.set(path + ".name", player.getName());
        dataConfig.set(path + ".join-time", System.currentTimeMillis());
        dataConfig.set(path + ".welcomed-by", List.of());
        saveData();
    }

    public boolean hasWelcomed(UUID newPlayer, UUID sender) {
        List<String> list = dataConfig.getStringList(playerPath(newPlayer) + ".welcomed-by");
        return list.contains(sender.toString());
    }

    public void markWelcomed(UUID newPlayer, UUID sender) {
        String path = playerPath(newPlayer) + ".welcomed-by";
        List<String> list = dataConfig.getStringList(path);
        if (!list.contains(sender.toString())) {
            list.add(sender.toString());
            dataConfig.set(path, list);
            saveData();
        }
    }

    public long getJoinTime(UUID newPlayer) {
        return dataConfig.getLong(playerPath(newPlayer) + ".join-time", 0L);
    }

    public void broadcastWelcome(Player player) {
        String msg = getConfig().getString("welcome-message", "");
        if (msg.isBlank()) return;
        Component component = MiniMessage.miniMessage()
                .deserialize(msg.replace("<player>", player.getName()));
        Bukkit.broadcast(component);
    }
}
