package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.PermissionSet;
import discord4j.rest.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class MessageHandler extends Handler {
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    public static Mono<Void> create(GatewayDiscordClient client){
        return new MessageHandler(client).handlers();
    }

    private MessageHandler(GatewayDiscordClient client){
        Mono<Void> onMessage = client.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getContent().equals("mrawr"))
            .filter(event -> !event.getMessage().getAuthor().map(u -> u.getId().equals(Snowflake.of(453401329335795714L))).orElse(false))
            .flatMap(event -> event.getMessage().getChannel())
            .flatMap(channel -> channel.createMessage("mrrawrrf!"))
            .then();

        Mono<Void> onMessage2 = client.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getContent().equals(";checkperms"))
            .filter(event -> event.getMessage().getAuthor().map(u -> u.getId().equals(Snowflake.of(368727799189733376L))).orElse(false))
            .flatMap(event ->
                event.getMessage().getAuthorAsMember()
                    .flatMap(Member::getBasePermissions)
                    .flatMapIterable(PermissionSet::asEnumSet)
                    .map(perm -> perm.name() + " " + perm.getValue() + "\n")
                    .collect(StringBuilder::new, StringBuilder::append)
                    .map(StringBuilder::toString)
                    .zipWith(event.getMessage().getChannel(), (msg, ch) -> ch.createMessage(msg))
                    .flatMap(m -> m)
            )
            .then();

        handlers = Mono.when(onMessage, onMessage2);
    }
}
