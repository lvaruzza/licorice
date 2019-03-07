package licorice.dataframe;

import java.util.stream.Stream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class TestMemoryDataFrame {
	private DataFrame df;
	private DataFramePrinter printer;
	
	@BeforeClass
	public void setup() {
		df = new MemoryDataFrame();
		df.setColNames(Lists.newArrayList("a", "b","c"));
		df.addRow("1", Lists.newArrayList(1,2,3));
		df.addRow("2", Lists.newArrayList(4,5,6));
		df.addRow("3", Lists.newArrayList(7,8,9));
		
		printer = new TabulatedPrinter(System.out,x->String.format("\"%s\"",x));
	}
	
	@Test
	public void testPrint() {
		printer.print(df);
	}
	
	
	@Test
	public void testColStream() {
		Stream<DataFrame.Column> stream = df.columnStream();
		stream.forEach(col -> System.out.println(col.toString()));
	}
}
