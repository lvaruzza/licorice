package licorice.analysis.outfmts;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import licorice.analysis.OutputFormat;
import licorice.analysis.VariantsSource;
import licorice.dataframe.DataFrame;
import licorice.dataframe.MemoryDataFrame;
import licorice.dataframe.TabulatedPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ExtendedFormat extends OutputFormat {
    private static Logger logger = LoggerFactory.getLogger(ExtendedFormat.class);

    public ExtendedFormat(boolean transpose)  {
        super(OutputFormats.EXTENDED,transpose);
    }

    @Override
    public void print(Predicate<VariantContext> filter, VariantsSource variants, Path matrixFile) throws IOException {
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
        TabulatedPrinter output = new TabulatedPrinter(new FileOutputStream(matrixFile.toFile()), x -> String.format("\"%s\"",x));
        if(transpose) {
            output.printTransposed(dt);
        } else {
            output.print(dt);
        }
        output.close();
    }
}
