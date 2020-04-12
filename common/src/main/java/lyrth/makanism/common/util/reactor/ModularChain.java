package lyrth.makanism.common.util.reactor;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/* I can't do this immutably >~< */
public class ModularChain {
    private static final Logger log = LoggerFactory.getLogger(ModularChain.class);

    private final ConcurrentHashMap<String, Publisher<?>> sources;
    private MonoProcessor<Integer> switcher;
    private boolean started = false;

    public static ModularChain from(Map<String, Publisher<?>> sources){
        ModularChain chain = new ModularChain();
        chain.sources.putAll(sources);
        return chain;
    }

    public ModularChain(){
        switcher = MonoProcessor.create();
        switcher.doOnNext(e -> log.info(e.toString())).subscribe();
        sources = new ConcurrentHashMap<>();
    }

    public ModularChain(String firstName, Publisher<?> firstPub){
        switcher = MonoProcessor.create();
        sources = new ConcurrentHashMap<>();
        sources.put(firstName, firstPub);
    }

    public ModularChain add(String name, Publisher<?> pub){
        sources.put(name, pub);
        log.trace("added " + name);
        resubscribe();
        return this;
    }

    public ModularChain remove(String name){
        sources.remove(name);
        log.trace("removed " + name);
        resubscribe();
        return this;
    }

    public void resubscribe(){
        if (started) switcher.onComplete();
    }

    private void reset(){
        switcher = MonoProcessor.create();
    }

    public Mono<Void> start(){
        started = true;
        return Mono.defer(() ->
            Mono.when(sources.values())
                .and(Mono.never())               // Make sure it never completes on its own, only through the switcher
                .takeUntilOther(switcher)
                .doOnTerminate(this::reset)
        ).repeat().then();
    }
}
