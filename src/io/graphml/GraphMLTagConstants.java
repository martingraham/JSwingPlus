package io.graphml;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class GraphMLTagConstants {

    private static final String[] ELEMENTS = {"MULTICLASSIFICATION", "AUTHORS", "AUTHOR", "NAMES", "NAME", "TREES", "TREE", "TREE_NAME", "TREE_NODE", "RANKS", "RANK", "RANK_NAME", "RANK_VALUE", "CONCEPT", "CONCEPTS", "TREE_LIST", "SPECIMEN_COUNT"};
    static enum MyTags {MULTICLASSIFICATION, AUTHORS, AUTHOR, NAMES, NAME, TREES, TREE, TREE_NAME, TREE_NODE, RANKS, RANK, RANK_NAME, RANK_VALUE, CONCEPT, CONCEPTS, TREE_LIST, SPECIMEN_COUNT};

    static EnumSet<MyTags> startTags = EnumSet.copyOf (
		Arrays.asList (new MyTags[] {MyTags.NAMES, MyTags.TREES, MyTags.TREE, MyTags.TREE_NODE, MyTags.CONCEPTS}));
  	static EnumSet<MyTags> endTags = EnumSet.copyOf (
		Arrays.asList (new MyTags[] {MyTags.NAME, MyTags.TREE_NAME, MyTags.TREE_NODE, MyTags.RANK, MyTags.RANK_NAME, MyTags.RANK_VALUE, MyTags.SPECIMEN_COUNT, MyTags.CONCEPT}));
	//private static String[] attributes = {"NAME_ID", "AUTHOR_IDREF", "RANK_IDREF", "RANK_ID", "AUTHOR_ID", "TREE_IDREF", "NAME_IDREF", "STATUS", "FROM_NAME_IDREF", "TO_NAME_IDREF", "FROM_TREE_IDREF", "TO_TREE_IDREF"};

    static Map<String, MyTags> tagMatcher;
    static Map<MyTags, String> stringMatcher;

    static {

    	final Map<String, MyTags> tempTagMatcher = new HashMap<String, MyTags> ();
    	final Map<MyTags, String> tempStringMatcher = new HashMap<MyTags, String> ();

        for (int n = MyTags.values().length; --n >= 0;) {
            tempTagMatcher.put (ELEMENTS [n], (MyTags.values()) [n]);
            tempStringMatcher.put ((MyTags.values()) [n], ELEMENTS [n]);
        }

        tagMatcher = Collections.unmodifiableMap (tempTagMatcher);
        stringMatcher = Collections.unmodifiableMap (tempStringMatcher);
        //endTags.add (MyTags.RANK_VALUE);
        //endTags.add (MyTags.SPECIMEN_COUNT);
       // endTags.add (MyTags.CONCEPT);
    }
}