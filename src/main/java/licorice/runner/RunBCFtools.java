package licorice.runner;

import com.google.common.collect.Lists;
import jdk.nashorn.internal.runtime.options.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.reference.GenomeRef;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.empty;

public class RunBCFtools extends RunBinary {
    private static final int CHUNK_SIZE = 96;

    private static Logger log = LoggerFactory.getLogger(RunBinary.class);

    public Optional<Path> normalizeVariant(GenomeRef reference, int minQual,Path out, Path input) {
        Path out1 = out.resolveSibling(out.getFileName() + ".split.vcf");

        Path out2 = out.resolveSibling(out.getFileName() + ".norm.vcf.gz");

        int ret1=runBinary("bcftools","view",
                "-a",
                "-U",
                "-o",out1.toString(),
                "-O","v",
                input.toString());


        if (ret1!=0) {
            log.error(String.format("Alleles split of '%s' failed",input.toString()));
            return Optional.empty();
        }

        int ret2=runBinary("bcftools","norm",
                "-f",reference.fastaFile().toString(),
                "-o",out2.toString(),
                "-O","z",
                out1.toString());

        if (ret2!=0) {
            log.error(String.format("Normalization of '%s' failed",out1.toString()));
            return Optional.empty();
        }


        int ret3=runBinary("bcftools","filter",
                String.format("-i 'QUAL>=%d'",minQual),
                "-o",out.toString(),
                "-O","z",
                out2.toString());

        if (ret3!=0) {
            log.error(String.format("Filter of '%s' failed",out2.toString()));
            return Optional.empty();
        }


        int ret4=runBinary("bcftools","index",
                "-f",
                out.toString());


        if (ret4!=0)  {
            log.error(String.format("Index of '%s' failed",out.toString()));
            return Optional.empty();
        } else {
            return Optional.of(out);
        }
    }

    public Stream<Path> normalizeVariants(GenomeRef reference,int minQual,Path tempDir, Stream<Path> variants) {
        return variants.map( p -> {
            Path out =tempDir.resolve(p.getFileName());
            return normalizeVariant(reference,minQual,out,p);
        }).filter(Optional::isPresent).map(Optional::get);
    }

    public int combineVariantsByChunks(GenomeRef reference,int minQual,Path combined, Stream<Path> variants) {
        List<Path> varLst = variants.collect(Collectors.toList());
        List<List<Path>> partitions=Lists.partition(varLst,CHUNK_SIZE);

        final AtomicInteger count = new AtomicInteger(0);
        Stream<Path> combinedParts = partitions.stream().map( (List<Path> vars) -> {
            int i = count.getAndIncrement();
            try {
                log.info(String.format("Merging step %d: Merging %d files",i,vars.size()));
                Path tempCombined = File.createTempFile(String.format("LM%03d",i),".vcf").toPath();

                combineVariants(reference,minQual,tempCombined,vars.stream());
                return tempCombined;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        return combineVariants(reference,minQual,combined,combinedParts);
    }

    /*public int filterVariants(GenomeRef reference,int minQual,Path combined, Stream<Path> variants) {

    }*/

    public int combineVariants(GenomeRef reference,int minQual,Path combined, Stream<Path> variants) {
        try {

            Path tempDir  = Files.createTempDirectory("licorice_norm");

            File tempCombined = File.createTempFile("licorice_merged",".vcf");

            Stream<Path> normalized = normalizeVariants(reference,minQual,tempDir,variants);

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
