package example.matrix;

public class OntologyObject extends Object {

	int listIndex;
	int depth;
	String name;
	double weight;

	public OntologyObject (final String name, final double weight, 
			final int listIndex, final int depth) {
		super ();
		this.name = name;
		this.weight = weight;
		this.listIndex = listIndex;
		this.depth = depth;
    }

    @Override
	public String toString() { return name+", "+Double.toString(weight)+", "+Integer.toString(listIndex)+", depth: "+Integer.toString(depth); }
}