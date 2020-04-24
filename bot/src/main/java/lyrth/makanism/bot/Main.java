package lyrth.makanism.bot;

import discord4j.common.GitProperties;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.store.api.service.StoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisException;
import lyrth.makanism.bot.handlers.GatewayHandler;
import lyrth.makanism.bot.util.BotProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
//        log.debug("Installing BlockHound...");
//        BlockHound.install();
//        log.debug("BlockHound ready.");

        // load bot meta
        log.debug("Reading .properties...");
        BotProps tempProps = new BotProps();
        String tokenVar = tempProps.get("bot.token.var");

        // get the token from env var
        log.debug("Reading env var...");
        String token = System.getenv(tokenVar);

        if (token == null || token.isEmpty()){
            log.warn("Bot token is {}. Please update the {} environment variable with the bot's token. Quitting.",
                token == null ? "null" : "empty",
                tokenVar);
            return;
        }

        new Main(token, tempProps);
    }

    private Main(String token, BotProps props){
        log.info("{} version {} starting.",
            props.get("bot.name"),
            props.get("bot.version")
        );
        log.info("On Java version {}", System.getProperty("java.runtime.version"));
        log.info("Running on Discord4J version {} (Git {}:{} built on {}).",
            props.get("d4j.version"),
            BotProps.D4JProps.get(GitProperties.APPLICATION_VERSION),
            BotProps.D4JProps.get("git.commit.id.abbrev"),
            BotProps.D4JProps.get("git.commit.time")
        );

        DiscordClientBuilder.create(token).build()
            .gateway()
            .setStoreService(getStoreService())
            .setInitialStatus(shardInfo -> Presence.doNotDisturb(Activity.listening("initialization sounds.")))
            .setEventDispatcher(EventDispatcher.replayingWithSize(16))
            .withGateway(GatewayHandler::create)
            /*
            .withGateway(client -> Mono.when(
                client.on(MessageCreateEvent.class).filter(event ->  event.getGuildId().isPresent() && event.getGuildId().get().equals(Snowflake.of(412444259887611914L))).flatMap(e -> Mono.just(e)
                        .doOnNext(event -> log.debug("message:" + event.getMessage().getContent()))
                    .filter(event -> event.getMessage().getContent().equals(";join"))
                    .map(event -> event.getMember().get())
                        .doOnNext(member -> log.info("Member {}", member.getId()))
                    .flatMap(Member::getVoiceState)
                        .doOnNext(state -> log.info("SessID {}", state.getSessionId()))
                    .flatMap(VoiceState::getChannel)
                        .doOnNext(channel -> log.info("Channel {}", channel.getId()))
                    .flatMap(vc -> vc.join(spec -> {}))
                        .doOnNext(conn -> log.info("VoiceState {}", conn.getState().name()))
                    .switchIfEmpty(Mono.fromRunnable(() -> log.debug("Next was empty!")))
                    .then()),
                client.onDisconnect()
                )
            )*/
            .block();

        log.info("ded!");
    }

    private StoreService getStoreService(){
        try {
            //return new RedisStoreService();
            return new RedisStoreService();
        } catch (RedisException e) {    // todo would we even use this
            //log.warn("Redis store service error! Using JdkStoreService instead. ({})", e.getMessage());
            //return new JdkStoreService();
            log.error("Redis store service error! Quitting.", e);
            System.exit(1);
            return null;
        }
    }
}
