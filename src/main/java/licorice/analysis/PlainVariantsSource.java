package licorice.analysis;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PlainVariantsSource implements VariantsSource {

    private VCFFileReader reader;

    public PlainVariantsSource(Path input) {
        File varFile = input.toFile();
        reader = new VCFFileReader(varFile,false);
    }

    @Override
    public Collection<String> samples() {
        List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
        return samples;
    }


    @Override
    public Iterator<VariantContext> iterator() {
        return reader.iterator();
    }
}
