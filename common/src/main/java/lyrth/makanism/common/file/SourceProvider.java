package lyrth.makanism.common.file;

import com.google.gson.reflect.TypeToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SourceProvider {

    <T> Mono<T>       read(String name, Class<T> clazz);        // default createIfMissing true
    <T> Mono<T>       read(String name, TypeToken<T> type);
    <T> Mono<T>       read(String name, Class<T> clazz, boolean createIfMissing);
    <T> Mono<T>       read(String name, TypeToken<T> type, boolean createIfMissing);
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
                `-- {moduleName}/
                    |-- config.json
                    `-- <other module files>

    Database structure:
    Makanism_config (database) [root]
        TABLE guilds
            guildId BIGINT PRIMARY KEY
            guild JSONB
            module_settings JSONB
        TABLE bot
            bot JSONB

 */