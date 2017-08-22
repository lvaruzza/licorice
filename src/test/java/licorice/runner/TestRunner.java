package licorice.runner;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Test
public class TestRunner {
    public void testRunBinary() {
        RunBinary runner = new RunBinary();
        int exitValue = runner.runBinary("bcftools","merge");
        assertEquals(exitValue, 1);
    }
}