package lyrth.makanism.api.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.gateway.ShardInfo;
import lyrth.makanism.common.file.config.BotConfig;
import lyrth.makanism.common.file.config.GuildConfig;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

public class CommandCtx {

    private final BotConfig config;
    private final String invokedName;
    private final MessageCreateEvent event;
    private Args args;

    public static CommandCtx from(MessageCreateEvent event, BotConfig config, String invokedName){
        return new CommandCtx(event, config, invokedName);
    }

    private CommandCtx(MessageCreateEvent event, BotConfig config, String invokedName){
        this.event = event;
        this.config = config;
        this.invokedName = invokedName;
    }

    public GatewayDiscordClient getClient() {
        return event.getClient();
    }

    public BotConfig getBotConfig() {
        return config;
    }

    public GuildConfig getGuildConfig(){
        return getGuildId().isPresent() ? config.getGuildConfig(getGuildId().get()) : null;
    }

    public String getPrefix(){
        GuildConfig config = getGuildConfig();
        if (config != null) return config.getPrefix();
        else return getBotConfig().getDefaultPrefix();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public String getContent() {
        return event.getMessage().getContent();
    }

    public Mono<MessageChannel> getChannel(){
        return event.getMessage().getChannel();
    }

    public Snowflake getChannelId(){
        return event.getMessage().getChannelId();
    }

    public Mono<Guild> getGuild() {
        return event.getGuild();
    }

    public Optional<Snowflake> getGuildId() {
        return event.getGuildId();
    }

    public Optional<Member> getMember() {
        return event.getMember();
    }

    public Optional<User> getUser(){
        return event.getMessage().getAuthor();
    }

    public Optional<Snowflake> getAuthorId(){       // Empty if user is invalid
        return getMember().map(User::getId).or(
            () -> getUser().map(User::getId)
        );
    }

    public String getAuthorIdText(){
        return getAuthorId().map(Snowflake::asString).orElse("Invalid user.");
    }

    public Instant getTimestamp(){
        return event.getMessage().getTimestamp();
    }

    public ShardInfo getShardInfo(){
        return event.getShardInfo();
    }

    public Args getArgs() {
        return (args == null) ?
            args = new Args(getContent().substring(getContent().toLowerCase().indexOf(invokedName))) :
            args;
    }

    public String getArg(int index) {
        return getArgs().get(index);
    }

    public <T> Mono<T> sendReply(String message){
        return sendReply(message, true);
    }

    public <T> Mono<T> sendReply(Consumer<EmbedCreateSpec> embed){      // TODO: check length
        return getChannel()
            .flatMap(channel -> channel.createMessage(spec -> spec.setEmbed(embed)))
            .then(Mono.empty());
    }

    public <T> Mono<T> sendReply(String message, boolean filtered){     // TODO: check length
        return getChannel()
            .flatMap(channel -> channel.createMessage(
                filtered ?
                    message.replace("@everyone", "@\u200Beveryone")
                        .replace("@here", "@\u200Bhere") :
                    message
            ))
            .then(Mono.empty());
    }
}
