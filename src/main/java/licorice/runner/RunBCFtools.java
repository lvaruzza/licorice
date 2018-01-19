package licorice.runner;

import jdk.nashorn.internal.runtime.options.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.reference.GenomeRef;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;

public class RunBCFtools extends RunBinary {
    private static Logger log = LoggerFactory.getLogger(RunBinary.class);

    public Optional<Path> normalizeVariant(GenomeRef reference, Path out, Path input) {
        Path out1 = out.resolveSibling(out.getFileName() + ".split.vcf");

        int ret1=runBinary("bcftools","view",
                "-a",
                "-o",out1.toString(),
                "-O","v",
                input.toString());


        if (ret1!=0) {
            log.error(String.format("Alleles split of '%s' failed",input.toString()));
            return Optional.empty();
        }

        int ret2=runBinary("bcftools","norm",
                "-f",reference.fastaFile().toString(),
                "-o",out.toString(),
                "-O","z",
                out1.toString());

        if (ret2!=0) {
            log.error(String.format("Normalization of '%s' failed",out1.toString()));
            return Optional.empty();
        }


        int ret3=runBinary("bcftools","index",
                "-f",
                out.toString());


        if (ret3!=0)  {
            log.error(String.format("Index of '%s' failed",out1.toString()));
            return Optional.empty();
        } else {
            return Optional.of(out);
        }
    }

    public Stream<Path> normalizeVariants(GenomeRef reference,Path tempDir, Stream<Path> variants) {
        return variants.map( p -> {
            Path out =tempDir.resolve(p.getFileName());
            return normalizeVariant(reference,out,p);
        }).filter(Optional::isPresent).map(Optional::get);
    }

    public int combineVariants(GenomeRef reference,Path combined, Stream<Path> variants) {
        try {

            Path tempDir  = Files.createTempDirectory("licorice_norm");

            File tempCombined = File.createTempFile("licorice_merged",".vcf");

            Stream<Path> normalized = normalizeVariants(reference,tempDir,variants);

            int ret=runBinary("bcftools",Stream.concat(Stream.of("merge",
                    "-o",tempCombined.getAbsolutePath(),
                    "-O","v",
                    "--force-samples",
                    "--threads","4",
                    "-m","both"),
                    normalized.map(p -> p.toString())).toArray(String[]::new));



            //int ret=runBinary("vcfcombine",tempCombined,variants.map(p -> p.toString()).toArray(String[]::new)) ;



            if (ret != 0) {
                return -1;
            }

            /*File tempFixed = File.createTempFile("licorice_vcflib_input",".vcf");
            int ret2 = runBinary("vcfcreatemulti", tempFixed, tempCombined.getAbsolutePath());

            if (ret2 != 0) {
                return -1;
            }*/

            int ret3 = runBinary("bcftools",
                    "convert","-O","z","-o",combined.toString(),tempCombined.getAbsolutePath());

            if (ret3 != 0) {
                return -1;
            }

            int ret4 = runBinary("bcftools",
              "index","-f","-t",combined.toString());

            if (ret4 != 0) {
                return -1;
            }

            tempCombined.delete();
            //tempFixed.delete();

        } catch(IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return -1;
        }

        return 0;
    }
}
