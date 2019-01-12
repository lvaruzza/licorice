package utils;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class VariantUtils {
    
    static public VariantContext merge(Queue<VariantContext> vars) {
        VariantContext f=vars.poll();
        VariantContext r = new VariantContextBuilder(f).make();
        GenotypesContext gf=r.getGenotypes();
        Set<String> samples=gf.getSampleNames();

        System.out.println(String.format("%s",r));
        System.out.println(String.format("%s",gf));

        for(VariantContext v:vars) {
            for(String sample:samples) {
                var gt=v.getGenotype(sample);

            }
        }


        System.out.println(r);
        return r;
    }
}
