package licorice.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import licorice.dataframe.DataFrame;
import licorice.dataframe.MemoryDataFrame;
import licorice.dataframe.TabulatedPrinter;

public class Matricifier {
	
	public void matricify(Path combinedVariants, Path matrixFile) throws IOException {
		File varFile = combinedVariants.toFile();
		VCFFileReader reader = new VCFFileReader(varFile);
		List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(samples.stream().flatMap(x -> Stream.of(x,x)).collect(Collectors.toList()));
		for(VariantContext var:reader) {
			GenotypesContext gctx = var.getGenotypes();
			
			Stream<String> gts = StreamSupport.stream(gctx.iterateInSampleNameOrder().spliterator(),false)
									.flatMap( gt -> {
										return gt.getAlleles().stream().map( a -> a.isNoCall() ? var.getReference().getBaseString() : a.getDisplayString());
									});

			dt.addRow(var.getContig() + ":" + Integer.toString(var.getStart()), gts.collect(Collectors.toList()));
		}
		reader.close();
		
		TabulatedPrinter output = new TabulatedPrinter(new FileOutputStream(matrixFile.toFile()));
		output.print(dt);
		output.close();
	}
}
