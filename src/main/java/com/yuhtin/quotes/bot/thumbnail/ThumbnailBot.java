package com.yuhtin.quotes.bot.thumbnail;

import com.henryfabio.sqlprovider.connector.type.impl.SQLiteDatabaseType;
import com.henryfabio.sqlprovider.executor.SQLExecutor;
import com.yuhtin.quotes.bot.thumbnail.bot.DiscordBot;
import com.yuhtin.quotes.bot.thumbnail.command.CommandRegistry;
import com.yuhtin.quotes.bot.thumbnail.config.Config;
import com.yuhtin.quotes.bot.thumbnail.manager.RewardsManager;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import com.yuhtin.quotes.bot.thumbnail.repository.UserRepository;
import com.yuhtin.quotes.bot.thumbnail.task.RewardsTask;
import com.yuhtin.quotes.bot.thumbnail.task.ThumbnailInteractExpirationTask;
import com.yuhtin.quotes.bot.thumbnail.util.TaskHelper;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
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
    private JDA jda;

    public static ThumbnailBot getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        formatLogger(logger);
        getLogger().info("Enabling bot...");

        loadConfig();
        setupSQL();

        loadThumbnails();

        getLogger().info("Bot enabled!");
    }

    @Override
    public void onReady() {
        getLogger().info("Bot ready!");
        getLogger().info("Logged in as @" + jda.getSelfUser().getName());

        CommandRegistry.of(jda).register();

        RewardsManager.instance().init();

        RewardsTask rewardsTask = new RewardsTask();
        TaskHelper.runTaskTimerAsync(rewardsTask, 1, rewardsTask.getIntervalIn(TimeUnit.SECONDS), TimeUnit.SECONDS);

        ThumbnailInteractExpirationTask thumbnailTask = new ThumbnailInteractExpirationTask();
        TaskHelper.runTaskTimerAsync(thumbnailTask, 500, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void serve(JDA jda) {
        this.jda = jda;
    }

    public void loadConfig() {
        config = Config.loadConfig("config.yml");
        if (config == null) {
            System.exit(0);
            return;
        }

        logger.info("Config loaded!");
    }

    private void loadThumbnails() {
        File projectRootPath = new File(System.getProperty("user.dir"));
        File thumbnailsPath = new File(projectRootPath, "/thumbnails");

        if (!thumbnailsPath.exists()) {
            if (!thumbnailsPath.mkdirs()) {
                throw new IllegalStateException("Couldn't create thumbnails folder!");
            }
        }

        FilenameFilter filenameFilter = (dir, name) -> name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg");

        File[] files = thumbnailsPath.listFiles(filenameFilter);
        if (files == null) {
            logger.severe("Couldn't load thumbnails!");
            return;
        }

        for (File file : files) {
            ThumbnailRepository.instance().loadThumbnail(file);
        }

        logger.info("Thumbnails loaded!");
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
        UserRepository.instance().init(executor);
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
