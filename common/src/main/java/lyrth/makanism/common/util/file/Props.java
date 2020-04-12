package lyrth.makanism.common.util.file;

public interface Props {
    String get(String name);
    String get(String name, String defaultValue);
}
