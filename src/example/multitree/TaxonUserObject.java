package example.multitree;

public class TaxonUserObject {

	String name;
	String rank;
	
	public TaxonUserObject (final String name, final String rank) {
		setName (name);
		setRank (rank);
	}
	
	public final String getName() {
		return name;
	}
	
	public final String getRank() {
		return rank;
	}
	
	public final void setName (final String name) {
		this.name = name;
	}
	
	public final void setRank (final String rank) {
		this.rank = rank;
	}
	
	@Override
	public String toString () {
		return name + " (" + rank + ")";
	}
}

