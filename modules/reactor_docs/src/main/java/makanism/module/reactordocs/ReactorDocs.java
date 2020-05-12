package makanism.module.reactordocs;

import lyrth.makanism.api.GuildModule;
//import org.apache.batik.transcoder.TranscoderException;
//import org.apache.batik.transcoder.TranscoderInput;
//import org.apache.batik.transcoder.TranscoderOutput;
//import org.apache.batik.transcoder.image.JPEGTranscoder;
//import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.*;

public class ReactorDocs extends GuildModule {
    private static final Logger log = LoggerFactory.getLogger(ReactorDocs.class);
    /*
    public static void main(String[] args) throws IOException, TranscoderException {
        log.info("Hi.");
        //Jsoup.parse("<span></span>");

        // Create a JPEG transcoder
        JPEGTranscoder t = new JPEGTranscoder();

        // Set the transcoding hints.
        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, .8f);

        // Create the transcoder input.
        String svgURI = new File("repeatWhenForFlux.svg").toURI().toString();
        TranscoderInput input = new TranscoderInput(svgURI);

        // Create the transcoder output.
        OutputStream ostream = new FileOutputStream("out.jpg");
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
        System.exit(0);

    }
*/
    @Override
    protected Mono<Void> initModule() {
        return Mono.empty();
    }
}
