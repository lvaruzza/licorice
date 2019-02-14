package licorice.analysis.outfmts;

import htsjdk.variant.variantcontext.Allele;
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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/*
 * Output Format used by Longping
 *
 */
public class NoStdFormat extends OutputFormat {
    private static Logger logger = LoggerFactory.getLogger(NoStdFormat.class);

    public NoStdFormat(boolean transpose)  {
        super(transpose);
    }

    @Override
    public void print(Predicate<VariantContext> filter, VariantsSource variants, Path matrixFile) throws IOException {
        DataFrame dt = new MemoryDataFrame();

        // Add top header
        List<String> header =  variants.samples();
        header.add(0,"pos");
        header.add(0,"chr");
        dt.setColNames(header);

        for(VariantContext var:variants) {
            if (filter.test(var)) {
                GenotypesContext gctx = var.getGenotypes();

                Stream<String> gts = StreamSupport.stream(gctx.iterateInSampleNameOrder().spliterator(), false)
                        .map(gt -> {
                            if (gt != null) {
                                List<Allele> as = gt.getAlleles();
                                String a = as.get(0).getBaseString();
                                String b = as.get(1).getBaseString();
                                if ((a.length() + b.length() > 2) || a == "." || b == ".") {
                                    return gt.getGenotypeString();
                                }
                                return a + b;
                            } else {
                                return "INVALID";
                            }
                        });

                String name = var.getID().equals(".")
                        ? "novel-" + var.getContig() + "-" + var.getStart()
                        : var.getID();


				/*System.out.println(String.format("Adding %s: NC:%d filter:%s",name,
                        var.getNoCallCount(),
                        StringUtils.join(var.getFilters(),"|")));*/

                // Filter lines with only No Calls
                List<String> gtLst =  gts.collect(Collectors.toList());
                long ncCount = gtLst.stream().filter((String s) -> s.equals("./.")).count();
                logger.debug(String.format("row '%s': NC count:%d %s",name,ncCount, ncCount>=gtLst.size() ? "(removed)":""));
                gtLst.add(0,Integer.toString(var.getStart()));
                gtLst.add(0,var.getContig());
                if (ncCount < gtLst.size())
                    dt.addRow(name, gtLst);

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
