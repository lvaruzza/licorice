package licorice.utils;


import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.testng.annotations.Test;
import utils.VariantUtils;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

public class TestVariantUtils {

    @Test
    public void testMerge() {
        VCFFileReader reader = new VCFFileReader(new File("data/test_merge.vcf"),false);

        Queue q = new ArrayDeque<VariantContext>();

        for(VariantContext v:reader) {
            q.add(v);
        }

        VariantUtils.merge(q);

    }
}
