package licorice.analysis;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import licorice.dataframe.DataFrame;
import licorice.dataframe.MemoryDataFrame;
import licorice.dataframe.TabulatedPrinter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Matricifier {
	public void matricify(Path combinedVariants, Path matrixFile) throws IOException {
		String path = matrixFile.toAbsolutePath().toString();
		String base = FilenameUtils.removeExtension(path);
		printFormat1(combinedVariants,Paths.get(base + ".SNP_1.txt"),true);
		printFormat2(combinedVariants,Paths.get(base + ".SNP_2.txt"),true);
		
		printFormat1(combinedVariants,Paths.get(base + ".ALL_1.txt"),false);
		printFormat2(combinedVariants,Paths.get(base + ".ALL_2.txt"),false);
	}

	public void printFormat1(Path combinedVariants, Path matrixFile,boolean snpOnly) throws IOException {
		File varFile = combinedVariants.toFile();
		VCFFileReader reader = new VCFFileReader(varFile,false);
		List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(samples.stream().collect(Collectors.toList()));
		for(VariantContext var:reader) {
			if (snpOnly && !var.emptyID()) {
				GenotypesContext gctx = var.getGenotypes();

				Stream<String> gts = StreamSupport.stream(gctx.iterateInSampleNameOrder().spliterator(), false)
						.map(gt -> gt.getGenotypeString());

				String name = var.getID().equals(".")
						? var.getContig() + ":" + Integer.toString(var.getStart())
						: var.getID();

				/*System.out.println(String.format("Adding %s: NC:%d filter:%s",name,
                        var.getNoCallCount(),
                        StringUtils.join(var.getFilters(),"|")));*/
				dt.addRow(name, gts.collect(Collectors.toList()));
			}
		}
		TabulatedPrinter output = new TabulatedPrinter(new FileOutputStream(matrixFile.toFile()));
		output.printTransposed(dt);
        //output.print(dt);
		output.close();
	}

	public void printFormat2 (Path combinedVariants, Path matrixFile,boolean snpOnly) throws IOException {
		File varFile = combinedVariants.toFile();
		VCFFileReader reader = new VCFFileReader(varFile);
		List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(samples.stream().collect(Collectors.toList()));
		
		for(VariantContext var:reader) {
			if (snpOnly && !var.getID().equals(".")) {
	
				GenotypesContext gctx = var.getGenotypes();
				List<String[]> genolst = new LinkedList<String[]>();
				
				for( Genotype gt: gctx.iterateInSampleNameOrder()) {
					int n=gt.getPloidy();
					String[] as=new String[n];
					int i=0;
					for(Allele a:gt.getAlleles()) {
						as[i++]=a.isNoCall() ? "." /*var.getReference().getBaseString()*/ : a.getDisplayString();
					}
					genolst.add(as);
					
				}
				
				String name=var.getID().equals(".") 
						? var.getContig() + ":" + Integer.toString(var.getStart())
						: var.getID();
	
				dt.addRow(name, genolst.stream().map(x -> x[0]).collect(Collectors.toList()));
				dt.addRow(name, genolst.stream().map(x -> x[1]).collect(Collectors.toList()));
			}
		}
		reader.close();
		
		TabulatedPrinter output = new TabulatedPrinter(new FileOutputStream(matrixFile.toFile()));
		output.printTransposed(dt);
		output.close();
	}
}
