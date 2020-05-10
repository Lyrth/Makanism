package lyrth.makanism.api.util;

import java.util.Arrays;
import java.util.List;

public class Args {

    private final String raw;
    private final String[] split;

    public Args(String raw){
        this.raw = raw;
        this.split = raw.split("\\s+", 8);
    }

    // count: # of items that would appear in the array.
    public Args(String raw, int count){
        this.raw = raw;
        this.split = raw.split("\\s+",count);
    }

    public String getRaw() {
        return raw;
    }

    // 0 is always the command name.
    public String get(int index) {
        if (index < 0) throw new IllegalArgumentException("Index cannot be less than 0.");
        return (index < split.length) ? split[index] : "";
    }

    // fromIndex is inclusive.
    /*  - fromIndex = 2
        0   1   2   3   4   5
        aaa bbb ccc ddd eee ff
        - Returns:
        ccc ddd eee ff
     */
    public String getRest(int fromIndex){
        if (fromIndex < 0) throw new IllegalArgumentException("Index cannot be less than 0.");
        String[] split = raw.split("\\s+", fromIndex + 1);
        return (fromIndex < split.length) ? split[fromIndex] : "";
    }

    public String[] asArray(){
        return split;
    }

    public List<String> asList(){
        return Arrays.asList(split);
    }

    public boolean matchesAt(String regex, int index){
        if (index < 0) throw new IllegalArgumentException("Index cannot be less than 0.");
        return (index < split.length) && split[index].matches(regex);
    }

    public boolean equalsAt(String str, int index){
        if (index < 0) throw new IllegalArgumentException("Index cannot be less than 0.");
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