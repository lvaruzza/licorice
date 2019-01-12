package licorice.dataframe;

import java.io.OutputStream;

public abstract class  DataFramePrinter<T> {
	protected OutputStream out;
	
	public DataFramePrinter(OutputStream outputStream) {
		this.out = outputStream;
	}
	
	public abstract void print(DataFrame<T> data);
	public abstract void printTransposed(DataFrame<T> data);
}
