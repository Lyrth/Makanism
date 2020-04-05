import lyrth.makanism.api.IModule;
import makanism.module.test.Test;

module TestModule {
    requires lyrth.makanism.api;

    uses IModule;

    exports makanism.module.test;

    provides IModule
        with Test;
}