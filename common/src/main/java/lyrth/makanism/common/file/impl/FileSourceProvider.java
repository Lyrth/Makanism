package lyrth.makanism.common.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lyrth.makanism.common.file.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileSourceProvider implements SourceProvider {    // TODO : Caches
    private static final Logger log = LoggerFactory.getLogger(FileSourceProvider.class);

    private final Gson gson;
    private final Scheduler scheduler;
    private final String root;

    public FileSourceProvider(){
        this("");
    }

    public FileSourceProvider(String root){
        gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
            .create();
        scheduler = Schedulers.boundedElastic();  // TODO: find a fitting scheduler. newBoundedElastic??
        this.root = root.endsWith("/") ? root : root + "/";
    }

    // Gets T from a json file.
    // Creates it when it doesn't exist and returns an empty/null-fields object;
    // If createIfMissing is false, will return an empty Mono instead when file doesn't exist.
    @Override
    public <T> Mono<T> read(String name, Class<T> clazz, boolean createIfMissing) {
        String fileName = root + (name.endsWith(".json") ? name : name + ".json");
        Mono<T> read = Mono.fromCallable(() -> {
                BufferedReader reader = Files.newBufferedReader(Paths.get(fileName));
                T t = gson.fromJson(reader, clazz);
                if (t == null) t = gson.fromJson("{}", clazz);
                reader.close();
                return t;
            })
            .subscribeOn(scheduler)
            .onErrorResume(t -> t instanceof IOException && !createIfMissing, t -> Mono.empty())
            .doOnError(err -> log.error("Error in reading file {}! ({})", fileName, err.getMessage()));
        return createIfMissing ? createFile(fileName).then(read) : read;
    }

    @Override
    public <T> Mono<T> read(String name, Class<T> clazz) {
        return read(name, clazz, true);
    }

    // Gets T from a json file
    // Creates it when it doesn't exist and returns an empty/null-fields object;
    // If createIfMissing is false, will return an empty Mono instead when file doesn't exist.
    @Override
    public <T> Mono<T> read(String name, TypeToken<T> type, boolean createIfMissing) {
        String fileName = root + (name.endsWith(".json") ? name : name + ".json");
        Mono<T> read = Mono.fromCallable(() -> {
            BufferedReader reader = Files.newBufferedReader(Paths.get(fileName));
            T t = gson.fromJson(reader, type.getType());
            if (t == null) t = gson.fromJson("{}", type.getType());
            reader.close();
            return t;
        })
            .subscribeOn(scheduler)
            .onErrorResume(t -> t instanceof IOException && !createIfMissing, t -> Mono.empty())
            .doOnError(err -> log.error("Error in reading file {}! ({})", fileName, err.getMessage()));
        return createIfMissing ? createFile(fileName).then(read) : read;
    }

    @Override
    public <T> Mono<T> read(String name, TypeToken<T> type) {
        return read(name, type, true);
    }


    // Writes T to a json file.
    @Override
    public <T> Mono<?> write(String name, T t) {
        String fileName = root + (name.endsWith(".json") ? name : name + ".json");
        Mono<?> write = Mono.fromCallable(() -> {
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                gson.toJson(t, writer);
                writer.close();
                return 1;
            })
            .subscribeOn(scheduler)
            .doOnError(err -> log.error("Error in writing to file {}! ({})", fileName, err.getMessage()));
            //.onErrorResume($ -> Mono.empty());
        return moveBackup(fileName).then(createFile(fileName)).then(write);
    }

    // Creates a file if and only if it doesn't exist.
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Mono<?> createFile(String fileName){
        return Mono.fromCallable(() -> {
            File file = new File(fileName);
            if (!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            return 0;
        }).subscribeOn(scheduler);
    }

    private Mono<?> moveBackup(String fileName){
        return Mono.fromCallable(() -> {
            File current = new File(fileName);
            File backup  = new File(fileName + ".bak");
            if (!current.exists()) return 1;
            if (backup.exists() && !backup.delete())
                log.warn("Cannot delete backup file {}.", fileName);
            if (!current.renameTo(backup))
                log.warn("Cannot rename file {}. Overwriting.", fileName);
            if (current.exists() && !current.delete())
                throw new IOException("Cannot modify file " + fileName);
            return 0;
        }).subscribeOn(scheduler);
    }

    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")     // fromCallable is fine with null return.
    @Override
    public Flux<String> listItems(String path) {
        return Mono.fromCallable(() -> new File(root + path).list((cur, name) -> new File(cur, name).isFile()))
            .subscribeOn(scheduler)
            .map(Arrays::asList)
            .flatMapIterable(s -> s)
            .filter(name -> name.endsWith(".json"))
            .map(name -> name.replaceFirst(".json$", ""));
    }

    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    @Override
    public Flux<String> listDirs(String path) {
        return Mono.fromCallable(() -> new File(root + path).list((cur, name) -> new File(cur, name).isDirectory()))
            .subscribeOn(scheduler)
            .map(Arrays::asList)
            .flatMapIterable(s -> s)
            .map(name -> name.replaceFirst("[\\\\/]+$", ""));
    }
}
