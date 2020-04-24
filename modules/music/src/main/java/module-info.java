import lyrth.makanism.api.GuildModule;
import makanism.module.music.Music;

module Music {

    requires lyrth.makanism.api;

    uses GuildModule;

    exports makanism.module.music;

    provides GuildModule
        with Music;

}