package lyrth.makanism.api.reply;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.AllowedMentions;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Reply<T extends Reply<T>> {

    //region// modified MessageCreateSpec code

    protected String content = "";
    protected Snowflake nonce;    /// Modified ///
    protected boolean tts;
    protected Consumer<? super EmbedCreateSpec> embedSpec;    /// Modified ///
    protected final List<Tuple2<String, InputStream>> files = new ArrayList<>(1); /// Modified ///
    protected AllowedMentions allowedMentions;    /// Modified ///

    public T setContent(String content) {
        this.content = content == null ? "" : content;
        return (T) this;
    }

    public T setNonce(Snowflake nonce) {
        this.nonce = nonce;     /// Modified ///
        return (T) this;
    }

    public T setTts(boolean tts) {
        this.tts = tts;
        return (T) this;
    }

    public T setEmbed(Consumer<? super EmbedCreateSpec> spec) {
        this.embedSpec = spec;  /// Modified ///
        return (T) this;
    }

    public T addFile(String fileName, InputStream file) {
        this.files.add(Tuples.of(fileName, file));  /// Modified ///
        return (T) this;
    }

    public T addFileSpoiler(String fileName, InputStream file) {
        return this.addFile("SPOILER_" + fileName, file);
    }

    public T setAllowedMentions(AllowedMentions allowedMentions) {
        this.allowedMentions = allowedMentions;     /// Modified ///
        return (T) this;
    }

    //endregion// modified MessageCreateSpec code


    /// Constructors

    protected final Snowflake invoker;        // The user who initiated the command.

    protected Reply(Snowflake invokerUser){
        this.invoker = invokerUser;
    }

    protected Reply(CommandCtx invokerCtx){
        this.invoker = invokerCtx.getAuthorId().orElse(Snowflake.of(0L));
    }

    public static Reply create(Snowflake invokerUser){
        return new Reply(invokerUser);
    }

    public static Reply create(CommandCtx invokerCtx){
        return new Reply(invokerCtx);
    }

    public static Reply fromEmbed(Snowflake invokerUser, Consumer<? super EmbedCreateSpec> spec){
        return new Reply(invokerUser).setEmbed(spec);
    }


    /// Modifier methods

    // Makes the mentions within the message not ping any role or user, except for the invoker's.
    // setAllowedMentions after this method will override this.
    public T blockMassMentions(){
        this.content = this.content
            .replace("@everyone", "@\u200Beveryone")
            .replace("@here", "@\u200Bhere");
        this.allowedMentions = AllowedMentions.builder()
            .parseType(
                AllowedMentions.Type.ROLE,
                AllowedMentions.Type.USER
            )
            .allowUser(invoker)
            .build();
        return (T) this;
    }


    /// Terminal functions

    public Mono<Message> send(CommandCtx ctx){
        return send(ctx.getChannel());
    }

    public Mono<Message> send(Mono<MessageChannel> channelMono){
        return channelMono.flatMap(this::send);
    }

    // Most likely the only method that should be overridden.
    public Mono<Message> send(MessageChannel channel){
        return channel.createMessage(this::apply);
    }

    protected void apply(MessageCreateSpec spec){
        if (this.content != null && !this.content.isBlank())
            spec.setContent(content);

        if (this.nonce != null)
            spec.setNonce(this.nonce);

        spec.setTts(this.tts);

        if (this.embedSpec != null)
            spec.setEmbed(this.embedSpec);

        this.files.forEach(TupleUtils.consumer(spec::addFile));

        if (this.allowedMentions != null)
            spec.setAllowedMentions(this.allowedMentions);
    }
}
