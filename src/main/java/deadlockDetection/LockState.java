package deadlockDetection;

import java.util.Collection;
import java.util.HashSet;

public class LockState {
	private HashSet<Handle> lockset = new HashSet<Handle>();
	
	public boolean contains(Handle handle) {
		return lockset.contains(handle);
	}
	
	public void remove(Handle handle) {
		lockset.remove(handle);
	}
	
	public void put(Handle handle) {
		lockset.add(handle);
	}
	
	public HashSet<Handle> getLocks() {
		return lockset;
	}
	
	public boolean isEmpty() {
		return lockset.isEmpty();
	}
	
}
