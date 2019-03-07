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

	private OutputFormat outfmt;

	public Matricifier(OutputFormat outfmt) {
		this.outfmt = outfmt;
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


		outfmt.print(filterSNP.and(filterFrequency),new PlainVariantsSource(combinedVariants),Paths.get(base + "." + outfmt.getExt() + ".SNP.txt"));
		outfmt.print(filterAll,new PlainVariantsSource(combinedVariants),Paths.get(base + "." + outfmt.getExt() + ".ALL.txt"));
	}


}
