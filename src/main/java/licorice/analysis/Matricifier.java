package licorice.analysis;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import htsjdk.variant.vcf.VCFFileReader;
import licorice.dataframe.DataFrame;
import licorice.dataframe.MemoryDataFrame;
import licorice.dataframe.TabulatedPrinter;
import org.apache.commons.io.FilenameUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Matricifier {
	private boolean transpose;
	private double maxFreq = 0.95;

	public Matricifier(boolean transpose) {
		this.transpose = transpose;
	}

	public void matricify(Path combinedVariants, Path matrixFile) throws IOException {
		String path = matrixFile.toAbsolutePath().toString();
		String base = FilenameUtils.removeExtension(path);

		final Predicate<VariantContext> filterFrequency = ((VariantContext var) -> {
			double f=var.getNoCallCount()*1.0/var.getNSamples();
			if (f <= maxFreq) {
				return true;
			} else {
				//System.out.println(var.getID() + " NC=" + var.getNoCallCount() + " f=" + f);
				return false;
			}
		});
		final Predicate<VariantContext> filterSNP = ((VariantContext var) ->  !var.emptyID() );
		final Predicate<VariantContext> filterAll = ((VariantContext var) ->  true );

		printOneColumn(filterSNP,combinedVariants,Paths.get(base + ".SNP_1.txt"));
		printTowColumns(filterSNP,combinedVariants,Paths.get(base + ".SNP_2.txt"));
		printSimplified(filterSNP,combinedVariants,Paths.get(base + ".SNP_simple.txt"));
		printExtended(filterSNP.and(filterFrequency),combinedVariants,Paths.get(base + ".SNP_ext.txt"));

		printOneColumn(filterAll,combinedVariants,Paths.get(base + ".ALL_1.txt"));
		printTowColumns(filterAll,combinedVariants,Paths.get(base + ".ALL_2.txt"));
	}

	public void printOneColumn(Predicate<VariantContext> filter,Path combinedVariants, Path matrixFile) throws IOException {
		File varFile = combinedVariants.toFile();
		VCFFileReader reader = new VCFFileReader(varFile,false);
		List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(samples.stream().collect(Collectors.toList()));
		for(VariantContext var:reader) {
			if (filter.test(var)) {
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
		if(transpose) {
			output.printTransposed(dt);
		} else {
			output.print(dt);
		}
		output.close();
	}

	public void printTowColumns(Predicate<VariantContext> filter,Path combinedVariants, Path matrixFile) throws IOException {
		File varFile = combinedVariants.toFile();
		VCFFileReader reader = new VCFFileReader(varFile);
		List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(samples.stream().collect(Collectors.toList()));
		
		for(VariantContext var:reader) {
			if (filter.test(var)) {
	
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
				dt.addRow(name, genolst.stream().map(x -> (x.length >1)  ? x[1] : "." ).collect(Collectors.toList()));
			}
		}
		reader.close();
		
		TabulatedPrinter output = new TabulatedPrinter(new FileOutputStream(matrixFile.toFile()));
		if (transpose) {
			output.printTransposed(dt);
		} else {
			output.print(dt);
		}
		output.close();
	}


	public void printSimplified(Predicate<VariantContext> filter,Path combinedVariants, Path matrixFile) throws IOException {
		File varFile = combinedVariants.toFile();
		VCFFileReader reader = new VCFFileReader(varFile,false);
		List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(samples.stream().collect(Collectors.toList()));
		for(VariantContext var:reader) {
			if (filter.test(var)) {
				GenotypesContext gctx = var.getGenotypes();

				Stream<String> gts = StreamSupport.stream(gctx.iterateInSampleNameOrder().spliterator(), false)
						.map((Genotype gt) -> gt.isNoCall() ?  "NOCALL" : (gt.isHom() ? "HOM" : "HET"));

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
		if(transpose) {
			output.printTransposed(dt);
		} else {
			output.print(dt);
		}
		output.close();
	}

	public void printExtended(Predicate<VariantContext> filter, Path combinedVariants, Path matrixFile) throws IOException {
		File varFile = combinedVariants.toFile();
		VCFFileReader reader = new VCFFileReader(varFile,false);
		List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(samples.stream().collect(Collectors.toList()));
		for(VariantContext var:reader) {
			if (filter.test(var)) {
				GenotypesContext gctx = var.getGenotypes();

				Stream<String> gts = StreamSupport.stream(gctx.iterateInSampleNameOrder().spliterator(), false)
						.map((Genotype gt) -> gt.isNoCall() ?  "" : (gt.isHom() ? "HOM" : "HET") + ":" +
								(gt.isHomRef() ? "REF:" : "CALL:") +
								gt.getGenotypeString()
								+ ":Q" + gt.getGQ());

				String name = var.getID().equals(".")
						? var.getContig() + ":" + Integer.toString(var.getStart())
						: var.getID();

				/*System.out.println(String.format("Adding %s: NC:%d filter:%s",name,
                        var.getNoCallCount(),
                        StringUtils.join(var.getFilters(),"|")));*/
				dt.addRow(name + ":" + var.getContig() +":" + var.getStart()  , gts.collect(Collectors.toList()));
			}
		}
		TabulatedPrinter output = new TabulatedPrinter(new FileOutputStream(matrixFile.toFile()));
		if(transpose) {
			output.printTransposed(dt);
		} else {
			output.print(dt);
		}
		output.close();
	}


}
