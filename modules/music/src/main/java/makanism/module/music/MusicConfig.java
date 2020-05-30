package makanism.module.music;

import lyrth.makanism.common.file.config.ModuleConfig;

public class MusicConfig implements ModuleConfig {
    private String a;
    private int b;

    public void setA(String a) {
        this.a = a;
    }

    public void setB(int b) {
        this.b = b;
    }

    public String getA() {
        return a;
    }

    public int getB() {
        return b;
    }
}
