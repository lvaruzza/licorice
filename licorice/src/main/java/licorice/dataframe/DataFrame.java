package licorice.dataframe;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public interface DataFrame {
	public static interface Row {
		public String getName();
		public Stream<Object> stream();
		public Object get(int j);
	}

	public static interface Column {
		public String getName();
		public Stream<Object> stream();
		public Object get(int i);
	}

	public int numberOfColumns();
	public int numberOfRows();
	
	public void setColNames(Collection<String> names);
	public Collection<String> getColNames();
	public String getColName(int index);
	
	public void addRow(String colName,Collection<Object> line);
	public Stream<Row> rowStream();
	public Iterator<Row> rowIterator();
	public Stream<Column> columnStream();
}
