package example.multiview;

import java.util.List;

public class KeyedData {

	protected Object key;
	protected List<?> data;
	protected int labelColumn;
	
	
	public KeyedData (Object key, List<?> data, int labelColumn) {
		setKey (key);
		setData (data);
		this.labelColumn = labelColumn;
	}
	
	public final Object getKey() {
		return key;
	}
	
	public final void setKey(Object key) {
		this.key = key;
	}
	
	public final List<?> getData() {
		return data;
	}
	
	public final void setData (List<?> data) {
		this.data = data;
	}
	
	public String toString () {
		return data.get(labelColumn).toString();
	}
}
