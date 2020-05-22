package makanism.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.core.GatewayDiscordClient;
import discord4j.common.util.Snowflake;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

public class GuildMusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    public final LavaplayerAudioProvider provider;

    private final Snowflake guildId;
    private final GatewayDiscordClient client;

    public GuildMusicManager(AudioPlayerManager manager, Snowflake guildId, GatewayDiscordClient client) {
        this.player = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.player);
        this.provider = new LavaplayerAudioProvider(this.player);
        this.player.addListener(this.scheduler);
        this.guildId = guildId;
        this.client = client;
    }

    public Mono<VoiceConnection> getConnection() {
        return client.getVoiceConnectionRegistry().getVoiceConnection(guildId);
    }

    // returns true on successful disconnect
    public Mono<Boolean> disconnect(){
        return getConnection()
            .flatMap(conn -> conn.disconnect().thenReturn(true))
            .defaultIfEmpty(false);
    }

    // TODO send message to bus
}
