package lyrth.makanism.api;

import reactor.core.publisher.Mono;

public interface IModule {  // TODO WIP (also not really Mono)
    Mono<String> getName();
}
