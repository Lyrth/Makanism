package lyrth.makanism.api.object;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.state.StateView;
import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import discord4j.store.api.util.LongLongTuple2;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static reactor.function.TupleUtils.function;

public class MCEModifier {              // lol this is so hack-y

    // Does not change member data.
    public static Function<MessageCreateEvent, Mono<MessageCreateEvent>> mutate(Consumer<ImmutableMessageData.Builder> mutator){
        return event -> {
            Mono<MessageData> messageDataMono = event.getClient().getGatewayResources()
                .getStateView()
                .getMessageStore()
                .find(event.getMessage().getId().asLong()); // FIXME: probable failure, ask d4j

            return messageDataMono.map(MessageData.builder()::from)
                .doOnNext(mutator)
                .map(builder -> new MessageCreateEvent(
                    event.getClient(),
                    event.getShardInfo(),
                    new Message(event.getClient(), builder.build()),
                    event.getGuildId().map(Snowflake::asLong).orElse(null),
                    event.getMember().orElse(null)
                ));
        };
    }

    // Should change member if not same and not null
    public static Function<MessageCreateEvent, Mono<MessageCreateEvent>> mutate(Consumer<ImmutableMessageData.Builder> mutator, @Nullable Member member){
        return event -> {
            Mono<MessageData> messageDataMono = event.getClient().getGatewayResources()
                .getStateView()
                .getMessageStore()
                .find(event.getMessage().getId().asLong()); // FIXME: probable failure, ask d4j

            boolean dontModify = (event.getMember().isEmpty() && member == null) ||
                event.getMember().flatMap(m ->
                    Optional.of(member).map(Member::getId).map(m.getId()::equals)).orElse(false);

            // Wont be subscribed to if member is null
            Mono<Optional<MemberData>> newMemberData = Mono.just(event.getClient().getGatewayResources().getStateView())
                .filter($ -> !dontModify && member != null)
                .map(StateView::getMemberStore)
                .flatMap(store -> store.find(LongLongTuple2.of(member.getGuildId().asLong(), member.getId().asLong())))
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());

            Mono<UserData> newUserData = Mono.just(event.getClient().getGatewayResources().getStateView())
                .filter($ -> !dontModify && member != null)
                .map(StateView::getUserStore)
                .flatMap(store -> store.find(member.getId().asLong()))
                .defaultIfEmpty(event.getMessage().getUserData());

            return messageDataMono.map(MessageData.builder()::from)
                .doOnNext(mutator)
                .flatMap(builder -> Mono.zip(Mono.just(builder), newMemberData, newUserData))
                .map(function((builder, memberData, userData) -> builder
                    .member(memberData.map(MCEModifier::memberDataToPartial).map(Possible::of).orElse(Possible.absent()))
                    .author(userData)
                    .build()
                ))
                .map(messageData -> new MessageCreateEvent(
                    event.getClient(),
                    event.getShardInfo(),
                    new Message(event.getClient(), messageData),
                    event.getGuildId().map(Snowflake::asLong).orElse(null),
                    member
                ));
        };
    }

    private static PartialMemberData memberDataToPartial(MemberData data){
        return PartialMemberData.builder()
            .nick(data.nick())
            .addAllRoles(data.roles())
            .joinedAt(data.joinedAt())
            .premiumSince(data.premiumSince())
            .hoistedRole(data.hoistedRole())
            .deaf(data.deaf())
            .mute(data.mute())
            .build();
    }
}
