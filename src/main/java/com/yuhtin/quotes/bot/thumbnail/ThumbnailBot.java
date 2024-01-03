package com.yuhtin.quotes.bot.thumbnail;

import com.henryfabio.sqlprovider.connector.type.impl.SQLiteDatabaseType;
import com.henryfabio.sqlprovider.executor.SQLExecutor;
import com.yuhtin.quotes.bot.thumbnail.bot.DiscordBot;
import com.yuhtin.quotes.bot.thumbnail.command.CommandRegistry;
import com.yuhtin.quotes.bot.thumbnail.config.Config;
import com.yuhtin.quotes.bot.thumbnail.manager.UserManager;
import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.Date;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@Getter
public class ThumbnailBot implements DiscordBot {

    private static final ThumbnailBot INSTANCE = new ThumbnailBot();
    private final Logger logger = Logger.getLogger("ThumbnailBot");

    private Config config;
    private Jedis jedis;
    private JDA jda;

    private UserManager userManager;

    public static ThumbnailBot getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        formatLogger(logger);
        getLogger().info("Enabling bot...");

        loadConfig();
        setupSQL();
        setupRedisClient();

        File projectRootPath = new File(System.getProperty("user.dir"));

        /*ThumbnailRepository.instance().insert(new Thumbnail(
                UUID.randomUUID().toString(),
                "The Droppers",
                new File(projectRootPath, "/resources/1.png")
        ));

        ThumbnailRepository.instance().insert(new Thumbnail(
                UUID.randomUUID().toString(),
                "Run!",
                new File(projectRootPath, "/resources/2.png")
        ));*/

        getLogger().info("Bot enabled!");
    }

    @Override
    public void onReady() {
        getLogger().info("Bot ready!");
        getLogger().info("Logged in as @" + jda.getSelfUser().getName());

        CommandRegistry.of(jda).register();

        this.userManager = new UserManager(jda, config);
    }

    @Override
    public void serve(JDA jda) {
        this.jda = jda;
    }

    private void loadConfig() {
        config = Config.loadConfig("config.yml");
        if (config == null) {
            System.exit(0);
            return;
        }

        logger.info("Config loaded!");
    }

    private void setupRedisClient() {
        //jedis = new Jedis(new HostAndPort(config.getRedisAddress(), 6379));
        //jedis.auth(config.getRedisPassword());

        logger.info("Redis client connected!");
    }

    private void setupSQL() {
        File file = new File("./thumbnails.db");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IllegalStateException("Couldn't create database file!");
                }
            } catch (Exception exception) {
                throw new IllegalStateException("Couldn't create database file!", exception);
            }
        }

        SQLExecutor executor = new SQLExecutor(new SQLiteDatabaseType(file).connect());

        ThumbnailRepository.instance().init(executor);
    }

    private void formatLogger(Logger logger) {
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tT] [%2$s] %3$s %n";

            @Override
            public synchronized String format(LogRecord record) {
                return String.format(format,
                        new Date(record.getMillis()),
                        record.getLevel().getLocalizedName(),
                        record.getMessage()
                );
            }
        });

        logger.addHandler(handler);
    }


}
