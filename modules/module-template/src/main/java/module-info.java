import lyrth.makanism.api.GuildModule;
import makanism.module.template.Template;

module Template {

    requires lyrth.makanism.api;

    uses GuildModule;

    exports makanism.module.template;

    provides GuildModule
        with Template;

    // Other dependencies
    // requires org.jsoup;

}