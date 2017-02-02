package licorice.dataframe;

import java.util.Collection;
import java.util.stream.Stream;

public interface DataFrame {
	public static interface Row {
		public String getName();
		public Stream<Object> stream();
	}
	
	public void setColNames(Collection<String> names);
	public Collection<String> getColNames();
	
	public void addRow(String colName,Collection<Object> line);
	public Stream<Row> rowStream();
}
