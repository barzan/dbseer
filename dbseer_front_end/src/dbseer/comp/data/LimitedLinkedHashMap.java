package dbseer.comp.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by dyoon on 15. 1. 4..
 */
public class LimitedLinkedHashMap<K, V> extends LinkedHashMap<K, V>
{
	private final int maxSize;

	public LimitedLinkedHashMap(int size)
	{
		this.maxSize = size;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> entry)
	{
		return size() > maxSize;
	}
}
