package licorice.analysis;

import htsjdk.variant.variantcontext.VariantContext;
import licorice.analysis.outfmts.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;

public abstract class OutputFormat {

    private static Logger logger = LoggerFactory.getLogger(OutputFormat.class);

    public static String[] listFormats() {
        return new String[]{"One Column","Two Columns","Simple","Without Bar","Extended"};
    }

    enum OutputFormats {
        DEFAULT(0),ONE_COLUMN(0),TWO_COLUMNS(1),NOSTD(2),SIMPLE(3),EXTENDED(4);

        private int id;

        OutputFormats(int id) {
            this.id = id;
        }
    }


    public static OutputFormat getFormat(int selectedIndex,boolean transpose) {
        switch(selectedIndex) {
            case 0:
                return new OneColumnFormat(transpose);
            case 1:
                return new TwoColumnsFormat(transpose);
            case 2:
                return new NoStdFormat(transpose);
            case 3:
                return new SimplifiedFormat(transpose);
            case 4:
                return new ExtendedFormat(transpose);
            default:
                return new OneColumnFormat(transpose);
        }
    }


    static OutputFormat getFormat(String fmtName, boolean transpose) {
        switch(fmtName.toUpperCase()) {
            case "TWO_COLUMNS":
            case "TWOCOLUMNS":
            case "2":
                return new TwoColumnsFormat(transpose);
            case "EXTENDED":
            case "X":
                return new ExtendedFormat(transpose);
            case "SIMPLE":
            case "S":
                return new SimplifiedFormat(transpose);
            case "NOSTD":
            case "NON_STANDARD":
            case "N":
                return new NoStdFormat(transpose);
            case "DEFAULT":
            case "ONE_COLUMN":
            case "ONECOLUMN":
            case "1":
            default:
                return new OneColumnFormat(transpose);
        }
    }

    static OutputFormat getFormat(OutputFormats fmtName, boolean transpose) {
        switch (fmtName) {
            case TWO_COLUMNS:
                return new TwoColumnsFormat(transpose);
            case EXTENDED:
                return new ExtendedFormat(transpose);
            case SIMPLE:
                return new SimplifiedFormat(transpose);
            case NOSTD:
                return new NoStdFormat(transpose);
            case DEFAULT:
            case ONE_COLUMN:
            default:
                return new OneColumnFormat(transpose);
        }
    }

    protected boolean transpose;


    public OutputFormat(boolean transpose) {
        this.transpose = transpose;
        logger.info(String.format("Using output format '%s'",this.getClass().getSimpleName()));
    }

    abstract public void print(Predicate<VariantContext> filter, VariantsSource variants, Path matrixFile) throws IOException;
}

