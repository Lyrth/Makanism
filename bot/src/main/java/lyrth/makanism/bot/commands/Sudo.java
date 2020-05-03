package lyrth.makanism.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Snowflake;
import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.util.CommandCtx;
import lyrth.makanism.api.annotation.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class Sudo extends BotCommand {
    private static final Logger log = LoggerFactory.getLogger(Sudo.class);

    @Override
    public Mono<Void> execute(CommandCtx ctx) {
        return Mono.just(ctx.getArgs().get(1).replaceAll("(<@!?)?(\\d{12,})>?","$2"))
            .flatMap(strId -> Mono.fromCallable(() -> Long.parseLong(strId)))
            .flatMap(id ->
                ctx.getGuild().flatMap(guild -> guild.getMemberById(Snowflake.of(id))).cast(User.class)
                .switchIfEmpty(ctx.getClient().getUserById(Snowflake.of(id)))
            )
            .map(user -> new Message(ctx.getClient(), getMessageData(user, ctx)))
            .flatMap(message -> message.getAuthorAsMember().map(Optional::of).defaultIfEmpty(Optional.empty())
                .doOnNext(member ->
                    ctx.getClient().getEventDispatcher()
                        .publish(new MessageCreateEvent(
                            ctx.getClient(),
                            ctx.getShardInfo(),
                            message,
                            ctx.getGuildId().map(Snowflake::asLong).orElse(null), member.orElse(null)))).then())
            .onErrorResume(NumberFormatException.class, t -> ctx.sendReply("Invalid args."))
            .doOnError(t -> log.error("Error at sudo!", t))
            .onErrorResume(t -> ctx.sendReply("Error! " + t.getMessage()));
    }

    private static MessageData getMessageData(User user, CommandCtx ctx){
        return ImmutableMessageData.builder()
            .id(ctx.getMessage().getId().asString())
            .channelId(ctx.getChannelId().asString())
            .guildId(Possible.of(ctx.getGuildId().orElse(Snowflake.of(0L)).asString()))
            .author(ImmutableUserData.builder()
                .id(user.getId().asString())
                .username(user.getUsername())
                .discriminator(user.getDiscriminator())
                .build()
            )
            .member((user instanceof Member) ?
                Possible.of(ImmutablePartialMemberData.builder()
                    .nick(Possible.of(((Member) user).getNickname()))
                    .joinedAt(((Member) user).getJoinTime().toString())
                    .deaf(false)
                    .mute(false)
                    .build()) :
                Possible.absent()
            )
            .content(ctx.getArgs().getRest(2))
            .timestamp(ctx.getTimestamp().toString())
            .tts(false)
            .mentionEveryone(false)
            .pinned(false)
            .type(0)
            .build();
    }
}
