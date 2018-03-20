package com.masd.event.analysis;

import com.masd.event.models.consul.ModelDir;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Slf4j
@Component
public class TensorflowAnalyzer {

    private final String labelFileName = "imagenet_comp_graph_label_strings.txt";
    private final String inceptionGraphDb = "tensorflow_inception_graph.pb";

    @Autowired
    private ModelDir modelDirectory;

    public TensorflowResult analyze(String imageFile) throws IOException {
        String modelDir = modelDirectory.getPath();
        log.info("Database location: {}", modelDir);

        byte[] graphDef = Files.readAllBytes(Paths.get(modelDir, inceptionGraphDb));
        List<String> labels = Files.readAllLines(Paths.get(modelDir, labelFileName), Charset.forName("UTF-8"));

        byte[] imageBytes = read(imageFile);
        log.info("Downloaded image");

        if(imageBytes.length == 0){
            log.warn("No bytes downloaded");
        }

        try (Tensor<Float> image = constructAndExecuteGraphToNormalizeImage(imageBytes)) {
            float[] labelProbabilities = executeInceptionGraph(graphDef, image);
            int bestLabelIdx = maxIndex(labelProbabilities);

            log.info("Result label: {}",  labels.get(bestLabelIdx));
            log.info("Result score: {}", labelProbabilities[bestLabelIdx] * 100f);

            return new TensorflowResult(labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f);
        }
    }

    private byte[] read(String imageFile) throws IOException {
        InputStream is = null;
        try {
            is = new URL(imageFile).openStream();
            return IOUtils.toByteArray(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private Tensor<Float> constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);

            final int H = 224;
            final int W = 224;
            final float mean = 117f;
            final float scale = 1f;

            final Output<String> input = b.constant("input", imageBytes);
            final Output<Float> output =
                    b.div(
                            b.sub(
                                    b.resizeBilinear(
                                            b.expandDims(
                                                    b.cast(b.decodeJpeg(input, 3), Float.class),
                                                    b.constant("make_batch", 0)),
                                            b.constant("size", new int[]{H, W})),
                                    b.constant("mean", mean)),
                            b.constant("scale", scale));
            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
            }
        }
    }

    private float[] executeInceptionGraph(byte[] graphDef, Tensor<Float> image) {
        try (Graph g = new Graph()) {
            g.importGraphDef(graphDef);
            try (Session s = new Session(g);
                 Tensor<Float> result =
                         s.runner().feed("input", image).fetch("output").run().get(0).expect(Float.class)) {
                final long[] rshape = result.shape();
                if (result.numDimensions() != 2 || rshape[0] != 1) {
                    throw new RuntimeException(
                            String.format(
                                    "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                    Arrays.toString(rshape)));
                }
                int nlabels = (int) rshape[1];
                return result.copyTo(new float[1][nlabels])[0];
            }
        }
    }

    private int maxIndex(float[] probabilities) {
        int best = 0;
        for (int i = 1; i < probabilities.length; ++i) {
            if (probabilities[i] > probabilities[best]) {
                best = i;
            }
        }
        return best;
    }

}
