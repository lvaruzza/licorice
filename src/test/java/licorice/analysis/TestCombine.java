package licorice.analysis;

import licorice.runner.RunBCFtools;
import org.testng.annotations.Test;
import utils.reference.SimpleGenomeRef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.testng.Assert.assertTrue;

public class TestCombine {

    private Path out1 = Paths.get("combined1.vcf.gz");
    private Path out2 = Paths.get("combined2.vcf.gz");

    //@Test
    public void testCombine() throws IOException {
        RunBCFtools bcftools = new RunBCFtools();
        Stream<Path> vars1 = Files.list(Paths.get("data/vars")).filter( p -> p.toString().endsWith(".vcf.gz"));
        bcftools.combineVariants(new SimpleGenomeRef(Paths.get("data/genome/GCF_000004515.4_Glycine_max_v2.0_genomic.fna")),30,out1,vars1);

        assertTrue(Files.exists(out1));
    }


    //@Test
    public void testCombineChunks() throws IOException {
        RunBCFtools bcftools = new RunBCFtools();

        Stream<Path> vars2 = Files.list(Paths.get("data/vars")).filter( p -> p.toString().endsWith(".vcf.gz"));
        bcftools.combineVariantsByChunks(new SimpleGenomeRef(Paths.get("data/genome/GCF_000004515.4_Glycine_max_v2.0_genomic.fna")),30,out2,vars2);

        assertTrue(Files.exists(out2));
    }


    @Test
    public void testCombineBoth() throws IOException {
        RunBCFtools bcftools = new RunBCFtools();

        Stream<Path> vars1 = Files.list(Paths.get("data/vars")).filter( p -> p.toString().endsWith(".vcf.gz"));
        bcftools.combineVariants(new SimpleGenomeRef(Paths.get("data/genome/GCF_000004515.4_Glycine_max_v2.0_genomic.fna")),30,out1,vars1);

        Stream<Path> vars2 = Files.list(Paths.get("data/vars")).filter( p -> p.toString().endsWith(".vcf.gz"));
        bcftools.combineVariantsByChunks(new SimpleGenomeRef(Paths.get("data/genome/GCF_000004515.4_Glycine_max_v2.0_genomic.fna")),30,out2,vars2);

        assertTrue(Files.exists(out1));
        assertTrue(Files.exists(out2));
    }

}
