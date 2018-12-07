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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private static Logger logger = LoggerFactory.getLogger(Matricifier.class);

    private boolean transpose;

	public Matricifier(boolean transpose) {
		this.transpose = transpose;
	}


	public void matricify(final double maxNC,final Path combinedVariants, final Path matrixFile) throws IOException {
		String path = matrixFile.toAbsolutePath().toString();
		String base = FilenameUtils.removeExtension(path);

        logger.info(String.format("Filtering all variants with No Call Rate > %f",maxNC));

		final Predicate<VariantContext> filterFrequency = ((VariantContext var) -> {
			double f=var.getNoCallCount()*1.0/var.getNSamples();
			if (f <= maxNC) {
				return true;
			} else {
				//System.out.println(var.getID() + " NC=" + var.getNoCallCount() + " f=" + f);
				return false;
			}
		});
		final Predicate<VariantContext> filterSNP = ((VariantContext var) ->  !var.emptyID() );
		final Predicate<VariantContext> filterAll = ((VariantContext var) ->  true );


        printOneColumn(filterSNP.and(filterFrequency),new PlainVariantsSource(combinedVariants),Paths.get(base + ".SNP_1.txt"));
		printTowColumns(filterSNP.and(filterFrequency),new PlainVariantsSource(combinedVariants),Paths.get(base + ".SNP_2.txt"));
		printSimplified(filterSNP.and(filterFrequency),new PlainVariantsSource(combinedVariants),Paths.get(base + ".SNP_simple.txt"));
		printExtended(filterSNP.and(filterFrequency),new PlainVariantsSource(combinedVariants),Paths.get(base + ".SNP_ext.txt"));

		printOneColumn(filterAll,new PlainVariantsSource(combinedVariants),Paths.get(base + ".ALL_1.txt"));
		printTowColumns(filterAll,new PlainVariantsSource(combinedVariants),Paths.get(base + ".ALL_2.txt"));
	}

	public void printOneColumn(Predicate<VariantContext> filter,VariantsSource variants, Path matrixFile) throws IOException {
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(variants.samples());

		for(VariantContext var:variants) {
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

	public void printTowColumns(Predicate<VariantContext> filter,VariantsSource variants, Path matrixFile) throws IOException {
		DataFrame dt = new MemoryDataFrame();
        dt.setColNames(variants.samples());

        for(VariantContext var:variants) {
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

		TabulatedPrinter output = new TabulatedPrinter(new FileOutputStream(matrixFile.toFile()));
		if (transpose) {
			output.printTransposed(dt);
		} else {
			output.print(dt);
		}
		output.close();
	}


	public void printSimplified(Predicate<VariantContext> filter,VariantsSource variants, Path matrixFile) throws IOException {
		DataFrame dt = new MemoryDataFrame();
		dt.setColNames(variants.samples());
		for(VariantContext var:variants) {
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

	public void printExtended(Predicate<VariantContext> filter, VariantsSource variants, Path matrixFile) throws IOException {
        DataFrame dt = new MemoryDataFrame();
        dt.setColNames(variants.samples());
        for(VariantContext var:variants) {
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
