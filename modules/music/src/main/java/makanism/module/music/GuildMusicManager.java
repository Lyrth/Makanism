package makanism.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class GuildMusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    public final LavaplayerAudioProvider provider;

    private VoiceConnection connection;

    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
        provider = new LavaplayerAudioProvider(player);
    }

    public void setConnection(VoiceConnection connection) {
        this.connection = connection;
    }

    public boolean isConnected(){
        return (connection != null && connection.isConnected());
    }

    @Nullable
    public VoiceConnection getConnection() {
        return isConnected() ? connection : null;
    }

    public Mono<Boolean> disconnect(){
        return isConnected() ?
            connection.disconnect().thenReturn(true) :
            Mono.just(false);
    }

    // TODO send message to bus
}
