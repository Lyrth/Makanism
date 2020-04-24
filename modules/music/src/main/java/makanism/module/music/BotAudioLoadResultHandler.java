package makanism.module.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class BotAudioLoadResultHandler implements AudioLoadResultHandler {

    private final GuildMusicManager musicManager;

    protected BotAudioLoadResultHandler(GuildMusicManager musicManager /*, FluxProcessor<String,String> messageBus*/) {
        this.musicManager = musicManager;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        System.out.println("onLoaded");
        musicManager.scheduler.queue(track);
        System.out.println("onLoadedDone");

        // send "Added tu queue" track title
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {    // TODO not only first track
        System.out.println("onPlaylistLoaded");
        AudioTrack firstTrack = playlist.getSelectedTrack();

        if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
        }

        musicManager.scheduler.queue(firstTrack);
        System.out.println("onPlaylistLoadedDone");

        // "Added ... (first track ...title)"
    }

    @Override
    public void noMatches() {
        System.out.println("onNoMatch");
        // send "not found"
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        System.out.println("onFail: " + exception.toString());
        // send FAIL
    }
}
