package lyrth.makanism.api;

import java.util.Arrays;
import java.util.List;

public class Args {
    private String raw;
    private String[] split;

    public Args(String raw){
        this.raw = raw;
        this.split = raw.split(" ");
    }

    public String getRaw() {
        return raw;
    }

    public String get(int index) {
        if (index < 0) throw new IllegalArgumentException("Position cannot be less than 0.");
        return (index < split.length) ? split[index] : "";
    }

    public String[] asArray(){
        return split;
    }

    public List<String> asList(){
        return Arrays.asList(split);
    }

    public boolean matchesAt(String regex, int index){
        return (index < split.length) && split[index].matches(regex);
    }

    public boolean equalsAt(String str, int index){
        return (index < split.length) && split[index].equals(str);
    }

    public boolean isEmpty(){
        return split.length == 1 && split[0].isEmpty();
    }

    public boolean isNotEmpty(){
        return !isEmpty();
    }

    public int getCount(){
        return split.length;
    }
}
