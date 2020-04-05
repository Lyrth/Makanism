package makanism.module.test;

import lyrth.makanism.api.IModule;
import reactor.core.publisher.Mono;

public class Test implements IModule {

    @Override
    public Mono<String> getName() {
        return Mono.just("reeEEEEEEEE");
    }
}
