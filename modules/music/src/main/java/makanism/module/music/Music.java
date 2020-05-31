package makanism.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.voice.VoiceConnection;
import lyrth.makanism.api.GuildModule;
import lyrth.makanism.api.annotation.GuildModuleInfo;
import lyrth.makanism.common.file.config.GuildConfig;
import makanism.module.music.commands.Join;
import makanism.module.music.commands.Leave;
import makanism.module.music.commands.Play;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@GuildModuleInfo(
    commands = {
        Play.class,
        Join.class,
        Leave.class
    }
)
public class Music extends GuildModule<MusicConfig> {
    // private static final Logger log = LoggerFactory.getLogger(Music.class);

    // Most code taken from:
    // https://github.com/sedmelluq/lavaplayer
    // https://github.com/sedmelluq/lavaplayer/blob/master/demo-d4j/src/main/java/com/sedmelluq/discord/lavaplayer/demo/d4j/Main.java

    private final ConcurrentHashMap<Snowflake, GuildMusicManager> guildManagers = new ConcurrentHashMap<>();

    private final AudioPlayerManager playerManager;

    public Music(){
        playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);           // Online sources
        AudioSourceManagers.registerLocalSource(playerManager);             // File sources
    }

    @Override
    protected Mono<?> initModule() {
        return Mono.empty();
    }

    // true: success, false: already joined, empty: not in visible vc, error: perms?
    public Mono<Boolean> join(Snowflake guildId, Member member){
        return Mono.justOrEmpty(guildManagers.get(guildId))
            .flatMap(manager -> join(manager, member));
    }

    // ^                        // todo check if bot's current channel has track and members
    private Mono<Boolean> join(GuildMusicManager manager, Member member){
        return member.getVoiceState()
            .flatMap(VoiceState::getChannel)
            .flatMap(ch -> Mono.just(ch)
                .filterWhen(vc ->
                    client.getVoiceConnectionRegistry()
                        .getVoiceConnection(vc.getGuildId())                // get current guild VoiceConnection
                        .flatMap(VoiceConnection::getChannelId)             // get what channel it is on
                        .map(channelId -> !channelId.equals(vc.getId()))    // make sure it's not the same channel
                        .defaultIfEmpty(true)                               // connection is empty, so not in channel
                )
                .flatMap(vc -> vc.join(spec -> spec.setProvider(manager.provider)))
                .hasElement()           // true if successful. false if bot already in channel. TODO: account for errors
            );
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
        return Mono.justOrEmpty(guildManagers.get(guildId))
            .flatMap(GuildMusicManager::disconnect);
    }

    // Stops everything
    public Mono<Boolean> stop(Snowflake guildId){       // TODO yeah
        return Mono.empty();
    }

    @Override
    protected Mono<?> onRegister(GuildConfig config) {
        // if not yet setup, new GuildMusicManager
        return Mono.fromRunnable(() ->
            guildManagers.computeIfAbsent(config.getId(), id -> new GuildMusicManager(playerManager, id, client)));
    }

    @Override
    protected Mono<?> onRemove(GuildConfig config) {
        return leave(config.getId());
    }
}

