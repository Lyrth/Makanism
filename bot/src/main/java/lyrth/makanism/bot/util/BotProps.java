package lyrth.makanism.bot.util;

import discord4j.common.GitProperties;
import lyrth.makanism.common.util.file.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class BotProps implements Props {
    private static final Logger log = LoggerFactory.getLogger(BotProps.class);

    private Properties props;

    public String get(String name){
        return get(name,null);
    }

    public String get(String name, String defaultValue){
        if (props == null){
            props =  new Properties();
            try {
                InputStream stream = BotProps.class.getClassLoader().getResourceAsStream(".properties");
                if (stream == null) throw new IOException(".properties file missing.");
                props.load(stream);
            } catch (IOException e){
                log.error("Error loading .properties file.", e);
                return null;
            }
        }
        return props.getProperty(name, defaultValue);
    }

    public static class D4JProps {

        private static Properties props;

        public static String get(String name){
            return get(name,null);
        }

        public static String get(String name, String defaultValue){
            if (props == null)
                props = GitProperties.getProperties();
            return props.getProperty(name, defaultValue);
        }
    }
}
