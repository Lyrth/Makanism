package lyrth.makanism.api.reply;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.api.react.listeners.MenuReactListener;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimplePaginator extends MenuMessage {

    private static final String PAGE_INFO = "Page **%d** of %d.";

    private final List<Consumer<EmbedCreateSpec>> pages = new ArrayList<>();
    @Nullable
    private Consumer<EmbedCreateSpec> endPage = null;
    private final AtomicInteger pageNumber = new AtomicInteger(0);

    private SimplePaginator(Snowflake invokerUser){
        super(invokerUser);
        this.listener = new MenuReactListener(invokerUser)
            .addAction("previous_track_button", this::firstPage)
            .addAction("arrow_backward", this::previousPage)
            .addAction("arrow_forward", this::nextPage)
            .addAction("next_track_button", this::lastPage)
            .addAction("x", this::end);
    }

    public static SimplePaginator create(Snowflake invokerUser){
        return new SimplePaginator(invokerUser);
    }

    public static SimplePaginator create(CommandCtx invokerCtx){
        return new SimplePaginator(invokerCtx.getAuthorId().orElse(Snowflake.of(0L)));
    }

    public SimplePaginator addPage(Consumer<EmbedCreateSpec> page){
        if (page != null){
            if (pages.isEmpty())
                this.setContent("").setEmbed(page);     // set first page to send
            this.pages.add(page);
        }
        return this;
    }

    public SimplePaginator setEndPage(Consumer<EmbedCreateSpec> page){
        this.endPage = page;
        return this;
    }

    private Mono<?> previousPage(ReactionAddEvent e){
        return Mono.just(pageNumber)
            .map(AtomicInteger::decrementAndGet)
            .map(i -> (i < 0) ? pageNumber.updateAndGet($ -> pages.size() - 1) : i)
            .map(pages::get)
            .flatMap(embed -> e.getMessage().flatMap(edit(embed)));
    }

    private Mono<?> nextPage(ReactionAddEvent e){
        return Mono.just(pageNumber)
            .map(AtomicInteger::incrementAndGet)
            .map(i -> (i >= pages.size()) ? pageNumber.updateAndGet($ -> 0) : i)
            .map(pages::get)
            .flatMap(embed -> e.getMessage().flatMap(edit(embed)));
    }

    private Mono<?> firstPage(ReactionAddEvent e){
        return Mono.just(pageNumber)
            .map(n -> n.updateAndGet($ -> 0))
            .map(pages::get)
            .flatMap(embed -> e.getMessage().flatMap(edit(embed)));
    }

    private Mono<?> lastPage(ReactionAddEvent e){
        return Mono.just(pageNumber)
            .map(n -> n.updateAndGet($ -> pages.size() - 1))
            .map(pages::get)
            .flatMap(embed -> e.getMessage().flatMap(edit(embed)));
    }

    private Mono<?> end(ReactionAddEvent e){
        return listener.cancel()
            .then(Mono.justOrEmpty(endPage))
            .switchIfEmpty(Mono.justOrEmpty(pages.get(pageNumber.get())))
            .flatMap(embed -> e.getMessage().flatMap(msg ->
                msg.edit(editSpec -> editSpec.setEmbed(embed).setContent(""))       // without content now
            ));
    }

    private Function<Message, Mono<Message>> edit(Consumer<EmbedCreateSpec> embed){
        return msg -> msg.edit(editSpec ->
            editSpec.setEmbed(embed).setContent(pageInfo())
        );
    }

    private String pageInfo(){
        return String.format(PAGE_INFO, pageNumber.get() + 1, pages.size());
    }




}

// would be immutable :D
// though, why
/*
class Page {
    private final String content;
    private final Consumer<EmbedCreateSpec> embed;

    public Page(){
        this("", spec -> {});
    }

    public Page(Consumer<EmbedCreateSpec> embed){
        this("", embed);
    }

    public Page(String content, Consumer<EmbedCreateSpec> embed){
        this.content = content;
        this.embed = embed;
    }

    public Page setMsgContent(String content){
        return new Page(content, this.embed);
    }

    public Page setTitle(String title){
        return new Page(this.content, this.embed.andThen(spec -> spec.setTitle(title)));
    }
}

 */
