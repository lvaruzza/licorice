package licorice.analysis;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import utils.ZipUtil;

public class TestZip {

	private static Path zipfile = Paths.get("data/vars.zip");

	@Test
	public void testPath() {
		assertTrue(ZipUtil.isZip(zipfile));
	}

	@Test
	void testExtract() throws Exception {
		Path outputDir =  Files.createTempDirectory(Paths.get("."),"zipfile"); 
		
		ZipUtil.unzip(zipfile, outputDir);
		
		for(File f:FileUtils.listFiles(outputDir.toFile(), new String[]{"vcf"}, false)) {
			System.out.println(f.toString());
		}
		FileUtils.forceDeleteOnExit(outputDir.toFile());
	}
}
