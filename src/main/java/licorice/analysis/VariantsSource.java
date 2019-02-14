package licorice.analysis;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.List;

public interface VariantsSource extends Iterable<VariantContext>{
    List<String> samples();
}
