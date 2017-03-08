package licorice.analysis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMatricify {

	@Test
	public void test() {
		Matricifier mat = new Matricifier();
		Path output=Paths.get("output.txt");
		try {
			mat.matricify(Paths.get("data/combined.vcf"), output);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		Assert.assertTrue(Files.exists(Paths.get("output.SNP_1.txt")));
	}
}
