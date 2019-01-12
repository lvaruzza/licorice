package licorice.dataframe;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public interface DataFrame<T> {
	public static interface Row<T> {
		public String getName();
		public Stream<T> stream();
		public T get(int j);
	}

	public static interface Column<T> {
		public String getName();
		public Stream<T> stream();
		public T get(int i);
	}

	public int numberOfColumns();
	public int numberOfRows();
	
	public void setColNames(Collection<String> names);
	public Collection<String> getColNames();
	public String getColName(int index);
	
	public void addRow(String colName,Collection<T> line);
	public Stream<Row<T>> rowStream();
	public Iterator<Row<T>> rowIterator();
	public Stream<Column<T>> columnStream();
}
