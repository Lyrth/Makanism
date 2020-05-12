import lyrth.makanism.api.GuildModule;
import makanism.module.reactordocs.ReactorDocs;

module ReactorDocs {

    requires lyrth.makanism.api;

    uses GuildModule;

    exports makanism.module.reactordocs;

    provides GuildModule
        with ReactorDocs;

    // Other dependencies
    requires org.jsoup;

}