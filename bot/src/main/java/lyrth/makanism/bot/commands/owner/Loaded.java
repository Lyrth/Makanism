package lyrth.makanism.bot.commands.owner;

import discord4j.core.object.entity.channel.MessageChannel;
import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.GuildModule;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;
import java.util.stream.Stream;

@CommandInfo(
    accessLevel = AccessLevel.OWNER,
    desc = "Prints out visible modules.",
    aliases = {"loadedmodules"}
)
public class Loaded extends BotCommand {
    private static final Logger log = LoggerFactory.getLogger(Loaded.class);

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        return ctx.getChannel().flatMap(MessageChannel::type).then(Mono.fromCallable(() -> {
            //URLClassLoader loader = new URLClassLoader(new URL[0]);

            File[] jars = new File("hotmodules/").listFiles(s -> s.getName().endsWith(".jar"));

            for (File f : jars) {
                URLClassLoader loader = new URLClassLoader(new URL[]{ f.toURI().toURL() });
                Stream<GuildModule> modules = ServiceLoader.load(GuildModule.class, loader).stream().map(m->m.get()).filter(m->m.getClass().getClassLoader().equals(loader));
                modules.forEach(m -> log.info("Seen class: {}", m.getName()));



                loader.close();
                //loader = null;
                //finder = null;
                //classes.removeIf(a -> true);
                //classes = null;
            }
            //jars = null;

            return "Doniea";
        }))
            .flatMap(ctx::sendReply);
    }
}
