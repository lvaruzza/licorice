package licorice.analysis.outfmts;

import htsjdk.variant.variantcontext.Allele;
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
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TwoColumnsFormat extends OutputFormat {
    private static Logger logger = LoggerFactory.getLogger(TwoColumnsFormat.class);

    public TwoColumnsFormat(boolean transpose)  {
        super(transpose);
    }

    @Override
    public void print(Predicate<VariantContext> filter, VariantsSource variants, Path matrixFile) throws IOException {
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
}
