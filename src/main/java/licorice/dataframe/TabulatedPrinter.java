package licorice.dataframe;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.stream.Collectors;

public class TabulatedPrinter extends DataFramePrinter implements Closeable {
	private CharSequence sep = "\t";
	
	public TabulatedPrinter(OutputStream out) {
		super(out);
	}
	
	public TabulatedPrinter(OutputStream out,CharSequence sep) {
		super(out);
		this.sep=sep;
	}

	@Override
	public void print(DataFrame data) {
		PrintStream ps = new PrintStream(out);
		ps.print(sep);
		ps.println(data.getColNames().stream().collect(Collectors.joining(sep)));
		data.rowStream().forEach(row -> {
			ps.print(row.getName());
			ps.print(sep);
			ps.println(row.stream().map( x -> x.toString()).collect(Collectors.joining(sep)));
		});
	}

	@Override
	public void printTransposed(DataFrame data) {
		PrintStream ps = new PrintStream(out);
		ps.print(sep);
		ps.println(data.rowStream().map(x -> x.getName()).collect(Collectors.joining(sep)));
		
		data.columnStream().forEach(col -> {
			ps.print(col.getName());
			ps.print(sep);
			ps.println(col.stream().map( x -> x.toString()).collect(Collectors.joining(sep)));
		});		
	}

	public void close() throws IOException {
		out.close();
	}
}
