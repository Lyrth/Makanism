package lyrth.makanism.bot.commands.owner;

import discord4j.rest.util.Color;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.api.reply.SimplePaginator;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class Test extends GuildCommand {

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        /*
        ReactListener menuListener = new MenuReactListener(ctx.getAuthorId().orElse(Snowflake.of(0L)))
            .addAction("one", e -> ctx.sendReply("It's a one!"))
            .addAction("two", e -> ctx.sendReply("It's a two!"))
            .addAction("six", e -> ctx.sendReply("It's a six!"))
            .addAction("four", e -> ctx.sendReply("It's a four!"))
            .cancelOn("x")
            .addAction("b", e -> ctx.sendReply("It's a b!"));
        return MenuMessage.create("1264xb", menuListener).send(ctx);
        */

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
}
