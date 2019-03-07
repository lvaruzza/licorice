package licorice.dataframe;

import java.io.OutputStream;
import java.util.function.Function;

public abstract class  DataFramePrinter<T> {
	protected OutputStream out;
	protected Function<String,String> escape;

	public DataFramePrinter(OutputStream outputStream) {
		this(outputStream,x->x);
	}
	public DataFramePrinter(OutputStream outputStream, Function<String,String> escape) {
		this.escape = escape;
		this.out = outputStream;
	}

	public abstract void print(DataFrame<T> data);
	public abstract void printTransposed(DataFrame<T> data);
}
