package licorice.analysis;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PlainVariantsSource implements VariantsSource {

    private VCFFileReader reader;

    public PlainVariantsSource(Path input) {
        File varFile = input.toFile();
        reader = new VCFFileReader(varFile,false);
    }

    @Override
    public List<String> samples() {
        List<String> samples  = new ArrayList<String>();
        samples.addAll(reader.getFileHeader().getSampleNamesInOrder());
        return samples;
    }


    @Override
    public Iterator<VariantContext> iterator() {
        return reader.iterator();
    }
}
