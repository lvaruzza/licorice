package licorice.analysis;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import utils.VariantUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class CombineByIDSource implements VariantsSource {

    public class CombiningIterator implements Iterator<VariantContext> {
        private Iterator<VariantContext> src;
        private Queue<VariantContext> pool;
        private String current = null;
        public CombiningIterator(Iterator<VariantContext> variants) {
            src = variants;
            pool = new ArrayDeque<VariantContext>();
            if (src.hasNext()) {
                VariantContext v = src.next();
                pool.add(v);
                current = v.getID();
            }
        }


        @Override
        public boolean hasNext() {
            return !pool.isEmpty() || src.hasNext();
        }

        @Override
        public VariantContext next() {
            while (src.hasNext()) {
                VariantContext v = src.next();
                if (v.getID().equals(current)) {
                    pool.add(v);
                } else {
                    current = v.getID();
                    VariantContext r= VariantUtils.merge(pool);
                    pool = new ArrayDeque<>();
                    pool.add(v);
                }
            }
            return null;
        }
    }

    private VCFFileReader reader;

    public CombineByIDSource(Path input) {
        File varFile = input.toFile();
        reader = new VCFFileReader(varFile,false);
    }

    @Override
    public List<String> samples() {
        List<String> samples = reader.getFileHeader().getSampleNamesInOrder();
        return samples;
    }


    @Override
    public Iterator<VariantContext> iterator() {
        return reader.iterator();
    }
}
