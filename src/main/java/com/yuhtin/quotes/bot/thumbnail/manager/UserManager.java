package com.yuhtin.quotes.bot.thumbnail.manager;

import com.yuhtin.quotes.bot.thumbnail.config.Config;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;

import java.util.logging.Logger;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@AllArgsConstructor
public class UserManager {

    private static final Logger LOGGER = Logger.getLogger("WaitlistBot");

    private final JDA jda;
    private final Config config;


}
