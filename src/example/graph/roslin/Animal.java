package example.graph.roslin;

public class Animal  {
	
	private final transient String name;
	final private boolean gender;
	private SNPData data;
	
	public Animal (final String str, final boolean isMale) { 
		name = str;
		gender = isMale;
	}
	
	public String getName () { return name; }
	
	public boolean isMale () { return gender; }
	
	
	public void setData (final SNPData data) {
		this.data = data;
	}

	public SNPData getData () {
		return data;
	}

	@Override
	public String toString () { return (gender ? "Male: " : "Female: ")+name; }
}
