package lyrth.makanism.common.util.reactor;

import reactor.core.publisher.Mono;

@Deprecated
public class Jump {
    private static final JumpException JUMP1 = new JumpTo1Exception();
    private static final JumpException JUMP2 = new JumpTo2Exception();
    private static final JumpException JUMP3 = new JumpTo3Exception();
    private static final JumpException JUMP4 = new JumpTo4Exception();
    private static final JumpException JUMP5 = new JumpTo5Exception();

    public static <T> Mono<T> to1(){
        return Mono.error(JUMP1);
    }

    public static <T> Mono<T> to2(){
        return Mono.error(JUMP2);
    }

    public static <T> Mono<T> to3(){
        return Mono.error(JUMP3);
    }

    public static <T> Mono<T> to4(){
        return Mono.error(JUMP4);
    }

    public static <T> Mono<T> to5(){
        return Mono.error(JUMP5);
    }

    public static <T> Mono<T> exit(){
        return Mono.error(new ExitException());
    }

    public static boolean from1(Throwable t){
        return t instanceof JumpTo1Exception;
    }

    public static boolean from2(Throwable t){
        return t instanceof JumpTo2Exception;
    }

    public static boolean from3(Throwable t){
        return t instanceof JumpTo3Exception;
    }

    public static boolean from4(Throwable t){
        return t instanceof JumpTo4Exception;
    }

    public static boolean from5(Throwable t){
        return t instanceof JumpTo5Exception;
    }

    public static class JumpException extends Exception {  // Todo: add light traceability for debug
        JumpException(){super("Unhandled jump",null,false,false);}
        JumpException(String msg){super(msg,null,false,false);}
        JumpException(String msg, Throwable t){super(msg,t,false,false);}
    }
    public static class JumpTo1Exception extends JumpException {}
    public static class JumpTo2Exception extends JumpException {}
    public static class JumpTo3Exception extends JumpException {}
    public static class JumpTo4Exception extends JumpException {}
    public static class JumpTo5Exception extends JumpException {}

    public static class ExitException extends Exception {}


}
