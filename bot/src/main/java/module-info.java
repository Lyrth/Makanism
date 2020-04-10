module lyrth.makanism.bot {
    requires lyrth.makanism.api;

    uses lyrth.makanism.api.IModule;
    uses lyrth.makanism.bot.util.file.SourceProvider;

    // the replacement to META-INF/services
    provides lyrth.makanism.bot.util.file.SourceProvider
        with lyrth.makanism.bot.util.file.impl.FileSourceProvider;
}