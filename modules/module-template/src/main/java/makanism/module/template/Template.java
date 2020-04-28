package makanism.module.template;

import lyrth.makanism.api.GuildModule;
import reactor.core.publisher.Mono;

public class Template extends GuildModule {

    @Override
    protected Mono<Void> initModule() {
        return Mono.empty();
    }
}
