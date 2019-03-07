package licorice.dataframe;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TabulatedPrinter<T> extends DataFramePrinter<T> implements Closeable {
	private CharSequence sep = "\t";
	
	public TabulatedPrinter(OutputStream out, Function<String,String> escape) {
		super(out,escape);
	}
	
	public TabulatedPrinter(OutputStream out,Function<String,String> escape,CharSequence sep) {
		super(out,escape);
		this.sep=sep;
	}

	@Override
	public void print(DataFrame<T> data) {
		PrintStream ps = new PrintStream(out);
		ps.print(sep);
		ps.println(data.getColNames().stream().map( x -> escape.apply(x)).collect(Collectors.joining(sep)));
		data.rowStream().forEach(row -> {
			ps.print(escape.apply(row.getName()));
			ps.print(sep);
			ps.println(row.stream().map( x -> x.toString()).collect(Collectors.joining(sep)));
		});
	}

	@Override
	public void printTransposed(DataFrame<T> data) {
		PrintStream ps = new PrintStream(out);
		ps.print(sep);
		ps.println(data.rowStream().map(x -> escape.apply(x.getName())).collect(Collectors.joining(sep)));
		
		data.columnStream().forEach(col -> {
			ps.print(escape.apply(col.getName()));
			ps.print(sep);
			ps.println(col.stream().map( x -> x.toString()).collect(Collectors.joining(sep)));
		});		
	}

	public void close() throws IOException {
		out.close();
	}
}
