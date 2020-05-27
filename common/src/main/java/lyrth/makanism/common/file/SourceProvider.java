package lyrth.makanism.common.file;

import com.google.gson.reflect.TypeToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SourceProvider {

    <T> Mono<T>       read(String name, Class<T> clazz);
    <T> Mono<T>       read(String name, TypeToken<T> clazz);
    <T> Mono<?>      write(String name, T t);

    Flux<String> listItems(String path);
    Flux<String>  listDirs(String path);

}

/*
    Filesystem structure [Configs]:
    /
    `-- config/  [root]
        |-- bot/
        |   `-- bot.json
        `-- guilds/
            `-- {guildID}/
               |-- guild.json
               `-- module_settings.json

    Database structure:
    Makanism_config (database) [root]
        TABLE guilds
            guildId BIGINT PRIMARY KEY
            guild JSONB
            module_settings JSONB
        TABLE bot
            bot JSONB

 */