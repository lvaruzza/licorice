package licorice.dataframe;

import java.io.OutputStream;

public abstract class  DataFramePrinter {
	protected OutputStream out;
	
	public DataFramePrinter(OutputStream outputStream) {
		this.out = outputStream;
	}
	
	public abstract void print(DataFrame data);
	public abstract void printTransposed(DataFrame data);
}
