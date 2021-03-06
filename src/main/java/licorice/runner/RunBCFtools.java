package licorice.runner;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.reference.GenomeRef;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunBCFtools extends RunBinary {
    private static final int CHUNK_SIZE = 96;

    private static Logger log = LoggerFactory.getLogger(RunBinary.class);
    private String mergeOption="id";

    public int normalizeVariantFinal(GenomeRef reference,Path out, Path input) {
        log.info(String.format("Post Normalization. Output '%s'",out.toString()));

        int ret2=runBinary("bcftools","norm",
                "-m+any",
                "-d","any",
                "-f",reference.fastaFile().toString(),
                "-o",out.toString(),
                "-O","z",
                input.toString());

        int ret3=runBinary("bcftools","index",
                "-t",
                "-f",
                out.toString());

        if (ret3!=0) {
            log.error(String.format("Index of '%s' failed", out.toString()));
            throw new RuntimeException("Index Failed");
        }

        return ret2;
    }

    public Optional<Path> normalizeVariant(GenomeRef reference, int minQual,Path out, Path input) {
        Path out1 = out.resolveSibling(out.getFileName() + ".split.vcf.gz");

        //Path out2 = out.resolveSibling(out.getFileName() + ".norm.vcf.gz");

        int ret1=runBinary("bcftools","view",
//                "-a",
//                Remove NOCALLs
//                "-U",
                String.format("-i \"QUAL>%d\"",minQual),
                "-o",out1.toString(),
                "-O","z",
                input.toString());


        if (ret1!=0) {
            log.error(String.format("Alleles split of '%s' failed", input.toString()));
            throw new RuntimeException(String.format("Failed to run command", out1));
        }

        if (!Files.exists(out1)) {
            throw new RuntimeException(String.format("Failed to genarate the file %s",out1));
        }

        int ret2=runBinary("bcftools","norm",
                "-m-any",
                "-f",reference.fastaFile().toString(),
                "-o",out.toString(),
                "-O","z",
                input.toString());
                //out1.toString());

        if (ret2!=0) {
            log.error(String.format("Normalization of '%s' failed",out1.toString()));
            throw new RuntimeException(String.format("Failed to run command",out1));
        }

        if (!Files.exists(out1)) {
            throw new RuntimeException(String.format("Failed to genarate the file %s",out1));
        }

       /* int ret3=runBinary("bcftools","filter",
                String.format("-i 'QUAL>%d'",minQual),
                "-o",out.toString(),
                "-O","z",
                out2.toString());

        if (ret3!=0) {
            log.error(String.format("Filter of '%s' failed",out2.toString()));
            return Optional.empty();
        }
        */

        int ret4=runBinary("bcftools","index",
                "-t",
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

    public int combineVariantsByChunks(GenomeRef reference,int minQual,Path combined, Stream<Path> variants) throws IOException {
        List<Path> varLst = variants.collect(Collectors.toList());
        List<List<Path>> partitions = Lists.partition(varLst, Math.min(CHUNK_SIZE, varLst.size() / 2));

        final AtomicInteger count = new AtomicInteger(1);
        Stream<Path> combinedParts = partitions.stream().map((List<Path> vars) -> {
            int i = count.getAndIncrement();
            try {
                log.info("=============================================================");
                log.info(String.format("Merging step %d: Merging %d files", i, vars.size()));
                log.info("=============================================================");
                Path tempCombined = File.createTempFile(String.format("LM%03d.", i), ".vcf").toPath();
                Stream<Path> normalized = filterVariants(reference, minQual, vars.stream());
                mergeVariants(reference, tempCombined, normalized);
                return tempCombined;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        log.info("Merging %d chunks", partitions.size());


        Path out1 = combined.resolveSibling(combined.getFileName() + ".merge.vcf.gz");

        int ret = mergeVariants(reference, out1, combinedParts);
        if (ret != 0) {
            throw new RuntimeException("Merge Variants failed");
        }

        int ret2=normalizeVariantFinal(reference, combined, out1);

        return ret2;
    }


    public int combineVariants(GenomeRef reference, int minQual, Path combined, Stream<Path> variants) throws IOException {
        Stream<Path> normalized = filterVariants(reference, minQual, variants);
        mergeVariants(reference, combined, normalized);
        return 0;
    }

    public Stream<Path> filterVariants(GenomeRef reference,int minQual,Stream<Path> variants) throws IOException {
        Path tempDir  = Files.createTempDirectory("licorice_norm");
        Stream<Path> normalized = normalizeVariants(reference,minQual,tempDir,variants);
        return normalized;
    }

    public int mergeVariants(GenomeRef reference, Path combined, Stream<Path> variants) throws IOException {
        try {

            File tempCombined = File.createTempFile("licorice_merged",".vcf");

            String[] varLst = variants.map(p -> p.toString()).toArray(String[]::new);


            switch(varLst.length) {
                case 0: throw new RuntimeException("Invalid Merge: Zero size list of vcf files");
                case 1: {
                    log.info(String.format("Uncompressing '%s' to '%s'",varLst[0],tempCombined.getAbsolutePath()));

                    int ret1=runBinary("bcftools","view",
                            "-a",
                            "-U",
                            "-o",tempCombined.getAbsolutePath(),
                            "-O","v",
                            varLst[0]);

                    break;
                }
                default: {
                    int ret=runBinary("bcftools",Stream.concat(Stream.of("merge",
                            "-o",tempCombined.getAbsolutePath(),
                            "-O","v",
                            "--force-samples",
                            "--threads","4",
                            "-m",mergeOption),
                            Arrays.stream(varLst)).toArray(String[]::new));

                    if (ret != 0) {
                        return -1;
                    }

                }
            }



            //int ret=runBinary("vcfcombine",tempCombined,variants.map(p -> p.toString()).toArray(String[]::new)) ;




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
