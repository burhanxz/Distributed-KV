package lsm.base;

import java.util.Comparator;

//TODO
public class Options {
	// TODO
	public static final Comparator<InternalKey> INTERNAL_KEY_COMPARATOR = new InternalKeyComparator();
	public static final String INTERNAL_KEY_COMPARATOR_NAME = "default_comparator";
	public static final String FILTER = "Bloom filter";
}
