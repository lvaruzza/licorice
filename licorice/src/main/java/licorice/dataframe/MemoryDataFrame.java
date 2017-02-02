package licorice.dataframe;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

public class MemoryDataFrame implements DataFrame {

	private static class MemoryRow implements DataFrame.Row {
		private String name;
		private Vector<Object> row;
		
		public MemoryRow(String name,Collection<Object> row) {
			this.name = name;
			this.row = new Vector<Object>(row);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Stream<Object> stream() {
			return row.stream();
		}
	}
	
	private List<Row> data;
	private Vector<String> colNames;
	
 	public MemoryDataFrame() {
		data=new LinkedList<Row>();
	}
	
	@Override
	public void setColNames(Collection<String> names) {
		colNames=new Vector<String>(names);
	}

	@Override
	public void addRow(String name,Collection<Object> line) {
		data.add(new MemoryRow(name,line));
		
	}

	@Override
	public Stream<Row> rowStream() {
		return data.stream();
	}

	@Override
	public Collection<String> getColNames() {
		return colNames;
	}

}
