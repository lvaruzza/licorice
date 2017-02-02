package licorice.dataframe;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MemoryDataFrame implements DataFrame {
	
	private static class MemoryColumn implements DataFrame.Column {
		private Vector<Object> data;
		private int column;
		private String name;
		
		private MemoryColumn(MemoryDataFrame df,String name,int column) {
			this.name = name;
			this.column=column;
			data = new Vector<Object>(df.numberOfRows());
			data.setSize(df.numberOfRows());
			Iterator<Row> rows = df.rowIterator();
			System.out.println(String.format("Copying %d rows to column", df.numberOfRows()));
			
			for(int j=0;j<df.numberOfRows();j++) {
				Row row = rows.next();
				data.set(j, row.get(column));
			}
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Stream<Object> stream() {
			return data.stream();
		}
		
		@Override
		public String toString() {
			return "[C" + column +":" + name + " " + data.stream().map(x->x.toString()).collect(Collectors.joining(" "))   + "]";
		}
		
		public Object get(int i) {
			return data.get(i);
		}
	}
	private static class ColumnSpliterator implements  Spliterator<Column> {
		private MemoryDataFrame d;
		private int index=0;
		
		private ColumnSpliterator(MemoryDataFrame dt) {
			//super(dt.numberOfColumns(), );
			d=dt;
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super Column> action) {
			System.out.println("**** Index = " + index + " cols = " + d.numberOfColumns());
			action.accept(new MemoryColumn(d,d.getColName(index),index));
			return (++index) < d.numberOfColumns();
		}

		@Override
		public Spliterator<Column> trySplit() {
			return this;
		}

		@Override
		public long estimateSize() {
			return d.numberOfColumns();
		}

		@Override
		public int characteristics() {
			return Spliterator.ORDERED + Spliterator.SIZED;
		}
				
	}
	
	private static class MemoryRow implements DataFrame.Row {
		private String name;
		private Vector<Object> data;
		
		private MemoryRow(String name,Collection<Object> row) {
			this.name = name;
			this.data = new Vector<Object>(row);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Stream<Object> stream() {
			return data.stream();
		}

		@Override
		public Object get(int j) {
			return data.get(j);
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

	@Override
	public Stream<Column> columnStream() {
		return StreamSupport.stream(new ColumnSpliterator(this), false);
	}

	@Override
	public int numberOfColumns() {
		return colNames.size();
	}

	@Override
	public String getColName(int index) {
		return this.colNames.get(index);
	}

	@Override
	public int numberOfRows() {
		return data.size();
	}

	@Override
	public Iterator<Row> rowIterator() {
		return data.iterator();
	}

}
