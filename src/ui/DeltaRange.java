package ui;

public class DeltaRange {

	int lowVal, highVal;
	boolean includeLowVal, includeHighVal;
	boolean remove;
	
	public DeltaRange (final int lowVal, final int highVal, final boolean includeLowVal,
			final boolean includeHighVal, final boolean remove) {
		super();
		this.lowVal = lowVal;
		this.highVal = highVal;
		this.includeLowVal = includeLowVal;
		this.includeHighVal = includeHighVal;
		this.remove = remove;
	}

	
	public final int getLowVal() {
		return lowVal;
	}
	public final int getHighVal() {
		return highVal;
	}
	public final boolean isIncludeLowVal() {
		return includeLowVal;
	}
	public final boolean isIncludeHighVal() {
		return includeHighVal;
	}
	public final boolean isRemove() {
		return remove;
	}
	
	@Override
	public String toString () {
		return super.toString()+"[lowVal: "+lowVal+", highVal: "+highVal+", incLowVal: "+includeLowVal
			+", incHighVal: "+includeHighVal+", remove: "+remove+"]";
	}
}
