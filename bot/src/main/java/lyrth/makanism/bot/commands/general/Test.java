package lyrth.makanism.bot.commands.general;

import discord4j.common.util.Snowflake;
import discord4j.rest.util.Color;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.api.react.listeners.MenuReactListener;
import lyrth.makanism.api.react.listeners.ReactListener;
import lyrth.makanism.api.reply.MenuMessage;
import lyrth.makanism.api.reply.SimplePaginator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@CommandInfo(
    accessLevel = AccessLevel.GENERAL,
    desc = "Tests something."
)
public class Test extends GuildCommand {

    private final Map<String, Function<CommandCtx, Mono<?>>> tests = new HashMap<>();
    {
        tests.put("menumessage", this::menuMessageTest);
        tests.put("pagination", this::paginationTest);
        tests.put("flags", this::flagsTest);
    }

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        if (ctx.getArgs().isEmpty()){
            return Flux.fromIterable(tests.keySet())
                .map("\n"::concat)
                .collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .map(s -> "Tests: (run as args to this command.) ```" + s + "```")
                .flatMap(ctx::sendReply);
        }

        return Mono.justOrEmpty(tests.get(ctx.getArg(1).toLowerCase()))
            .flatMap(f -> f.apply(ctx));
    }

    public Mono<?> menuMessageTest(CommandCtx ctx){
        ReactListener menuListener = new MenuReactListener(ctx.getAuthorId().orElse(Snowflake.of(0L)))
            .addAction("one", e -> ctx.sendReply("It's a one!"))
            .addAction("two", e -> ctx.sendReply("It's a two!"))
            .addAction("six", e -> ctx.sendReply("It's a six!"))
            .addAction("four", e -> ctx.sendReply("It's a four!"))
            .cancelOn("x")
            .addAction("b", e -> ctx.sendReply("It's a b!"));
        return MenuMessage.create(ctx, "1264xb", menuListener).send(ctx);
    }

    public Mono<?> paginationTest(CommandCtx ctx){
        SimplePaginator paginator = SimplePaginator.create(ctx)
            .addPage(spec -> spec
                .setTitle("First page tho")
                .setDescription("lol")
                .setFooter("red", null)
                .setColor(Color.RED)
            ).addPage(spec -> spec
                .setTitle("Second page tho")
                .setDescription("lol XD")
                .setFooter("green", null)
                .setColor(Color.GREEN)
            ).addPage(spec -> spec
                .setTitle("THIRD page tho")
                .setDescription("aaaa")
                .setFooter("blue", null)
                .setColor(Color.BLUE)
            ).addPage(spec -> spec
                .setTitle("4")
                .setDescription("a a a [a](https://www.google.com/)")
                .setFooter("black", null)
                .setColor(Color.BLACK)
            ).addPage(spec -> spec
                .setTitle("`5`")
                .setDescription("no u")
                .setFooter("yay color", null)
                .setColor(Color.of(128, 86, 255))
            );
        return paginator.send(ctx);
    }

    public Mono<?> flagsTest(CommandCtx ctx){
        return Flux.fromIterable(ctx.getArgs().getFlags().entrySet())
            .map(entry -> entry.getKey() + ": [" + entry.getValue() + "]")
            .map("\n"::concat)
            .collect(StringBuilder::new, StringBuilder::append)
            .map(StringBuilder::toString)
            .map(s -> "Flags: ```" + s + "```")
            .flatMap(ctx::sendReply);
    }
}
