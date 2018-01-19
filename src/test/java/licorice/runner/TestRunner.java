package licorice.runner;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class TestRunner {
    public void testRunBinary() {
        RunBinary runner = new RunBinary();
        int exitValue = runner.runBinary("bcftools", "merge");
        assertEquals(exitValue, 1);
    }


    /*public void testRunBinaryRedirect() {
        File out = new File("help.txt");
        RunBinary runner = new RunBinary();
        int exitValue = runner.runBinary("echo",out, "hello");
        assertEquals(exitValue, 0);
        assertTrue(out.exists());

    }*/


    public void testRunVCFLib() {
        File out = new File("fix.vcf");
        RunBinary runner = new RunBinary();
        int exitValue = runner.runBinary("vcfcreatemulti", out,"tests/merged.vcf");
        //assertEquals(exitValue, 1);
        assertTrue(out.exists());

    }

}