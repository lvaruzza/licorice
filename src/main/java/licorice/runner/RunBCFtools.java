package licorice.runner;

import utils.reference.GenomeRef;

import java.nio.file.Path;
import java.util.stream.Stream;

public class RunBCFtools extends RunBinary {

    public int combineVariants(GenomeRef reference,Path combined, Stream<Path> variants) {
        int ret=runBinary("bcftools",Stream.concat(Stream.of("merge",
                "-o",combined.toString(),
                "-O","z",
                "--force-samples",
                "--threads","4",
                "-m","all"),
                variants.map(p -> p.toString())).toArray(String[]::new));


        int ret2 = runBinary("bcftools",
                "index","-f","-t",combined.toString());

        return ret;
    }
}
