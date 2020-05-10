package makanism.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Snowflake;
import lyrth.makanism.api.GuildModule;
import lyrth.makanism.api.annotation.GuildModuleInfo;
import lyrth.makanism.common.util.file.config.GuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@GuildModuleInfo(
    commands = {
        PlayCmd.class,
        JoinCmd.class,
        LeaveCmd.class
    }
)
public class Music extends GuildModule {
    private static final Logger log = LoggerFactory.getLogger(Music.class);

    // Most code taken from:
    // https://github.com/sedmelluq/lavaplayer
    // https://github.com/sedmelluq/lavaplayer/blob/master/demo-d4j/src/main/java/com/sedmelluq/discord/lavaplayer/demo/d4j/Main.java

    private final ConcurrentHashMap<Snowflake, GuildMusicManager> guildManagers = new ConcurrentHashMap<>();

    private final AudioPlayerManager playerManager;

    public Music(){
        super();
        playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);           // Online sources
        AudioSourceManagers.registerLocalSource(playerManager);             // File sources
    }

    @Override
    protected Mono<Void> initModule() {
        return Mono.empty();
    }

    // true: success, false: already joined, empty: not in visible vc, error: perms?
    public Mono<Boolean> join(Snowflake guildId, Member member){
        GuildMusicManager manager = guildManagers.get(guildId);
        if (manager == null) return Mono.empty();
        return join(manager, member);
    }

    // ^
    private Mono<Boolean> join(GuildMusicManager manager, Member member){
        return member.getVoiceState()
            .doOnNext(vs -> log.info("SessId {}", vs.getSessionId()))
            .flatMap(VoiceState::getChannel)
            .transform(ch -> ch
                .filterWhen(vc ->
                    client.getMemberById(member.getGuildId(), config.getBotId())    // get bot as member of guild
                        .flatMap(Member::getVoiceState)                             // get bot's voice state
                        .map(vs -> vs.getChannelId().map(vc.getId()::equals).orElse(false))    // check if bot's channel is also requesting member's channel
                        .map(b -> !b)                                               // if it's the same channel, don't redo the join.
                        .defaultIfEmpty(true)                                       // bot VoiceState is empty (not in any channel), so let it join.
                )
                .doOnNext(vc -> log.info("ChannelId {}", vc.getId().asString()))
                .flatMap(vc -> manager.disconnect().then(vc.join(spec -> spec.setProvider(manager.provider))))
                //.doOnNext(conn -> log.info("VoiceState {}", conn.getState().name()))
                .map($ -> true)                                                     // true if successful
                .defaultIfEmpty(false)                                              // false if bot already in channel
            );

        /*
        return member.getVoiceState()                                       // todo check if bot's current channel has track and members
            .flatMap(memberVS -> memberVS.getChannel()                      // get the channel member is in
                .filterWhen(vc -> vc.getVoiceStates()
                    .doOnNext(vs -> log.debug("{} == {} ?",vs.getUserId(), config.getBotId()))
                    .any(vs -> vs.getUserId().equals(config.getBotId()))    // true if bot is in already
                    .map(b -> !b)                                           // so don't continue if true
                )
                .flatMap(vc -> manager.disconnect()
                    .then(vc.join(spec -> spec.setProvider(manager.provider)))
                    .doOnNext(manager::setConnection)
                    .thenReturn(true))
                .defaultIfEmpty(false)
            );
        */
    }

    // true: played successfully, false: input invalid/not ready, empty: member not in voice channel
    public Mono<Boolean> play(Snowflake guildId, String input, Member member){
        GuildMusicManager musicManager = guildManagers.get(guildId);
        if (input.isEmpty() || musicManager == null) return Mono.just(false);
        return join(musicManager,member)
            .doOnNext(b ->
                playerManager.loadItemOrdered(musicManager, input, new BotAudioLoadResultHandler(musicManager)
            ));
    }

    // true: leave success, false: bot not in vc, empty: ??    TODO: Vote, stop playing
    public Mono<Boolean> leave(Snowflake guildId){
        GuildMusicManager musicManager = guildManagers.get(guildId);
        return Mono.justOrEmpty(musicManager)
            .flatMap(GuildMusicManager::disconnect);
    }

    public Mono<Boolean> stop(Snowflake guildId){       // TODO yeah
        return Mono.empty();
    }

    @Override
    protected Mono<Void> onRegister(GuildConfig config) {
        // if not yet setup, new GuildMusicManager
        return Mono.fromRunnable(() ->
            guildManagers.computeIfAbsent(config.getId(), id -> new GuildMusicManager(playerManager, id, client)));
    }

    @Override
    protected Mono<Void> onRemove(GuildConfig config) {
        return leave(config.getId()).then();
    }
}

