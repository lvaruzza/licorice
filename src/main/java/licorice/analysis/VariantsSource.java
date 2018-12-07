package licorice.analysis;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.Collection;
import java.util.Iterator;

public interface VariantsSource extends Iterable<VariantContext>{
    Collection<String> samples();
}
