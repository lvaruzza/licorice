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

public class MemoryDataFrame<T> implements DataFrame<T> {
	
	private static class MemoryColumn<T> implements DataFrame.Column<T> {
		private Vector<T> data;
		private int column;
		private String name;
		
		private MemoryColumn(MemoryDataFrame<T> df,String name,int column) {
			this.name = name;
			this.column=column;
			data = new Vector<T>(df.numberOfRows());
			data.setSize(df.numberOfRows());
			Iterator<Row<T>> rows = df.rowIterator();
			//System.out.println(String.format("Copying %d rows to column", df.numberOfRows()));
			
			for(int j=0;j<df.numberOfRows();j++) {
				Row<T> row = rows.next();
				data.set(j, row.get(column));
			}
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Stream<T> stream() {
			return data.stream();
		}
		
		@Override
		public String toString() {
			return "[C" + column +":" + name + " " + data.stream().map(x->x.toString()).collect(Collectors.joining(" "))   + "]";
		}
		
		public T get(int i) {
			return data.get(i);
		}
	}
	private static class ColumnSpliterator<T> implements  Spliterator<Column<T>> {
		private MemoryDataFrame<T> d;
		private int index=0;
		
		private ColumnSpliterator(MemoryDataFrame<T> dt) {
			//super(dt.numberOfColumns(), );
			d=dt;
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super Column<T>> action) {
			action.accept(new MemoryColumn<T>(d,d.getColName(index),index));
			return (++index) < d.numberOfColumns();
		}

		@Override
		public Spliterator<Column<T>> trySplit() {
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
	
	private static class MemoryRow<T> implements DataFrame.Row<T>{
		private String name;
		private Vector<T> data;
		
		private MemoryRow(String name,Collection<T> row) {
			this.name = name;
			this.data = new Vector<T>(row);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Stream<T> stream() {
			return data.stream();
		}

		@Override
		public T get(int j) {
			return data.get(j);
		}
	}
	
	private List<Row<T>> data;
	private Vector<String> colNames;
	
 	public MemoryDataFrame() {
		data=new LinkedList<Row<T>>();
	}
	
	@Override
	public void setColNames(Collection<String> names) {
		colNames=new Vector<String>(names);
	}

	@Override
	public void addRow(String name,Collection<T> line) {
		data.add(new MemoryRow(name,line));
		
	}

	@Override
	public Stream<Row<T>> rowStream() {
		return data.stream();
	}

	@Override
	public Collection<String> getColNames() {
		return colNames;
	}

	@Override
	public Stream<Column<T>> columnStream() {
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
	public Iterator<Row<T>> rowIterator() {
		return data.iterator();
	}

}
