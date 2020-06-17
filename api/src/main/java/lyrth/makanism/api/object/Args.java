package lyrth.makanism.api.object;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Index zero is the invoked command name
public class Args {

    private static final Pattern FLAG_PATTERN = Pattern.compile("\\s/(\\w)(=(\\S*))?(?!\\S)");  // todo: /+X -> X: "+", /-X=fish -> X: "-fish"; space support; escape flag; 'Succeeding is Literal' flag

    private Map<Character, String> flags;

    private final String original;
    private final String raw;
    private final String[] split;

    public Args(String input){
        this.original = input;
        this.raw = removeFlags(input);
        this.split = this.raw.split("\\s+", 8);
    }

    // Raw is the original message contents.
    public String getOriginal() {
        return original;
    }

    // Raw is the original message contents **without the flags**.
    public String getRaw() {
        return raw;
    }

    // 0 is always the command name.
    public String get(int index) {
        return checkIndex(index) ? split[index] : "";
    }

    // fromIndex is inclusive.
    /*  - fromIndex = 2
        0   1   2   3   4   5
        ;aa bbb ccc ddd eee ff
        - Returns:
        ccc ddd eee ff
     */
    public String getRest(int fromIndex){
        if (fromIndex < 0) throw new IndexOutOfBoundsException("Index cannot be less than 0.");
        String[] split = raw.split("\\s+", fromIndex + 1);
        return (fromIndex < split.length) ? split[fromIndex] : "";
    }

    public boolean hasFlag(char name){
        if (!isValidFlag(name)) throw new IllegalArgumentException("Character is not a valid switch name.");
        return getFlags().containsKey(name);
    }

    public String getFlagValue(char name){
        if (!isValidFlag(name)) throw new IllegalArgumentException("Character is not a valid switch name.");
        return getFlags().get(name);
    }

    public Map<Character, String> getFlags() {
        if (this.flags == null) this.flags = parseFlags(original);  // lazy parse
        return this.flags;
    }

    // concat the flags into a string that can be put back into a command message
    public String concatFlags(){
        StringBuilder result = new StringBuilder(" ");
        for (Map.Entry<Character, String> entry : getFlags().entrySet()) {
            result.append('/').append(entry.getKey());
            if (!entry.getValue().isBlank())
                result.append('=').append(entry.getValue());
            result.append(' ');
        }
        return result.toString();
    }

    public String[] asArray(){
        return split;
    }

    public List<String> asList(){
        return Arrays.asList(split);
    }

    public boolean matchesAt(int index, String regex){
        return checkIndex(index) && split[index].toLowerCase().matches(regex);
    }

    public boolean equalsAt(int index, String str){
        return checkIndex(index) && split[index].equalsIgnoreCase(str);
    }

    // Returns true if command has no actual args, e.g.:
    // ;ping
    public boolean isEmpty(){
        return split.length == 1;
    }

    public boolean isNotEmpty(){
        return !isEmpty();
    }

    public int count(){
        return split.length - 1;    // exclude the command name
    }


    // Utilities

    // returns true if index is within split array length (max 8)
    // false if it isn't
    // throws an exception instead if the index is less than zero.
    private boolean checkIndex(int index) throws IndexOutOfBoundsException {
        if (index < 0) throw new IndexOutOfBoundsException("Index cannot be less than 0.");
        return index < split.length;
    }

    private static String removeFlags(String in){
        return FLAG_PATTERN.matcher(in).replaceAll("");
    }

    // parse flags
    // /F   -> "F":""
    // /n=  -> "n":""
    // /0=x -> "0":"x"
    private static Map<Character, String> parseFlags(String in){
        HashMap<Character, String> map = new HashMap<>();
        Matcher mat = FLAG_PATTERN.matcher(in);
        while (mat.find())
            map.put(mat.group(1).charAt(0), mat.group(3) == null ? "" : mat.group(3));
        return map;
    }

    private static boolean isValidFlag(char c) {
        return (c >= 'A' && c <= 'Z') ||
            (c >= 'a' && c <= 'z') ||
            (c >= '0' && c <= '9') ||
            (c == '_');     // yeah _ would be a switch
    }
}


// Old code that's 10x faster; use if the performance is needed.
/*
    private static Map<Character, String> parseFlags(String in){
        Map<Character, String> map = new HashMap<>();
        int pos = -1;
        while ((pos = in.indexOf('/', pos + 1)) != -1){
            if (pos + 1 == in.length()) continue;           // end is slash
            boolean atEdge = (pos + 2 == in.length());      // `/.$`: automatic no hasEqual
            boolean hasEqual = !atEdge && (in.charAt(pos + 2) == '=');
            if (!(atEdge || (Character.isWhitespace(in.charAt(pos + 2)) || hasEqual)) ||
                !(pos == 0 || Character.isWhitespace(in.charAt(pos - 1))) ||
                !isValidFlag(in.charAt(pos + 1))
            ) continue;

            if (hasEqual){
                if (pos + 3 >= in.length()){
                    map.put(in.charAt(pos + 1), "");
                    continue;
                }
                for (int i = pos + 3; i < in.length(); i++) {
                    if (Character.isWhitespace(in.charAt(i))) {
                        map.put(in.charAt(pos + 1), in.substring(pos + 3, i));
                        pos = i;    // begin on match end to save resources
                        break;
                    }
                    if (i == in.length() - 1){      // we've reached the end
                        System.out.println("end");
                        map.put(in.charAt(pos + 1), in.substring(pos + 3));
                        pos = in.length();      // we're done
                    }
                }
            } else {
                map.put(in.charAt(pos + 1), "");
                pos += 2;
            }
        }
        return map;
    }
 */