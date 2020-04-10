package lyrth.makanism.bot.util.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lyrth.makanism.bot.util.file.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Arrays;

import static lyrth.makanism.common.util.FuncUtil.it;

public class FileSourceProvider implements SourceProvider {    // TODO : Caches
    private static final Logger log = LoggerFactory.getLogger(FileSourceProvider.class);

    private final Gson gson;
    private final Scheduler scheduler;
    private final String root;

    public FileSourceProvider create(){
        return new FileSourceProvider("");
    }

    public FileSourceProvider create(String root){
        return new FileSourceProvider(root);
    }

    private FileSourceProvider(String root){
        gson = new GsonBuilder().setPrettyPrinting().create();
        scheduler = Schedulers.boundedElastic();  // TODO: find a fitting scheduler. newBoundedElastic??
        this.root = root.endsWith("/") ? root : root + "/";
    }

    // Gets T from a json file. Creates it when it doesn't exist and returns an empty/null-fields object;
    @Override
    public <T> Mono<T> read(String name, Class<T> clazz) {
        String fileName = root + (name.endsWith(".json") ? name : name + ".json");
        Mono<T> read = Mono.fromCallable(() -> {
                FileReader reader = new FileReader(fileName);
                T t = gson.fromJson(reader, clazz);
                reader.close();
                return t;
            })
            .subscribeOn(scheduler)
            .doOnError(err -> log.error("Error in reading file {}! ({})", fileName, err.getMessage()));
            //.onErrorResume($ -> Mono.empty());
        return createFile(fileName).then(read);
    }

    // Gets T from a json file
    @Override
    public <T> Mono<T> read(String name, TypeToken<T> type) {
        String fileName = root + (name.endsWith(".json") ? name : name + ".json");
        Mono<T> read = Mono.fromCallable(() -> {
            FileReader reader = new FileReader(fileName);
            T t = gson.fromJson(reader, type.getType());
            reader.close();
            return t;
        })
            .subscribeOn(scheduler)
            .doOnError(err -> log.error("Error in reading file {}! ({})", fileName, err.getMessage()));
        //.onErrorResume($ -> Mono.empty());
        return createFile(fileName).then(read);
    }


    // Writes T to a json file.
    @Override
    public <T> Mono<Void> write(String name, T t) {
        String fileName = root + (name.endsWith(".json") ? name : name + ".json");
        Mono<Void> write = Mono.fromCallable(() -> {
                FileWriter writer = new FileWriter(fileName);
                gson.toJson(t, writer);
                writer.close();
                return 1;
            })
            .subscribeOn(scheduler)
            .doOnError(err -> log.error("Error in writing to file {}! ({})", fileName, err.getMessage()))
            .then();
            //.onErrorResume($ -> Mono.empty());
        return moveBackup(fileName).then(write);
    }

    // Creates a file if and only if it doesn't exist.
    private Mono<Void> createFile(String fileName){
        return Mono.fromCallable(() -> {
            File file = new File(fileName);
            if (!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            return 0;
        }).subscribeOn(scheduler).then();
    }

    private Mono<Void> moveBackup(String fileName){
        return Mono.fromCallable(() -> {
            File current = new File(fileName);
            File backup  = new File(fileName + ".bak");
            if (backup.exists())
                if (!backup.delete())
                    log.warn("Cannot delete backup file {}.", fileName);
                else if (!current.renameTo(backup))
                    log.warn("Cannot rename file {}. Overwriting.", fileName);
            if (current.exists() && !current.delete())
                throw new IOException("Cannot modify file " + fileName);
            return 0;
        }).subscribeOn(scheduler).then();
    }

    @Override
    public Flux<String> listItems(String path) {
        return Mono.fromCallable(() -> new File(root + path).list((cur, name) -> new File(cur, name).isFile()))
            .subscribeOn(scheduler)
            .map(Arrays::asList)
            .flatMapIterable(it())
            .filter(name -> name.endsWith(".json"))
            .map(name -> name.replaceFirst(".json$", ""));
    }

    @Override
    public Flux<String> listDirs(String path) {
        return Mono.fromCallable(() -> new File(root + path).list((cur, name) -> new File(cur, name).isDirectory()))
            .subscribeOn(scheduler)
            .map(Arrays::asList)
            .flatMapIterable(it())
            .map(name -> name.replaceFirst("[\\/]+$", ""));
    }
}
