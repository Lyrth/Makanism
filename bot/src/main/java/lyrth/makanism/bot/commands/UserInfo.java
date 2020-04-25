package lyrth.makanism.bot.commands;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.CommandCtx;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandInfo(
    accessLevel = AccessLevel.GENERAL,
    aliases = {"profile", "me"}
)
public class UserInfo extends GuildCommand {
    @Override
    public Mono<Void> execute(CommandCtx ctx) {

        Optional<String> reply =
        ctx.getMember().map(m -> (User) m).or(ctx::getUser).map(user ->
            new StringBuilder("**User information for ")
            .append(user.getUsername()).append("**:").append("\n")
            .append("Username: ").append(user.getUsername()).append("#").append(user.getDiscriminator()).append("\n")
            .append("ID: ").append(user.getId().asString()).append("\n")
            .append((user instanceof Member) ? "Nickname: ".concat(((Member) user).getNickname().orElse("")).concat("\n") : "")
            .append((user instanceof Member) ? "Joined: ".concat(((Member) user).getJoinTime().toString()).concat("\n") : "")
            .toString());

        return Mono.justOrEmpty(reply)
            .flatMap(ctx::sendReply);
    }
}
