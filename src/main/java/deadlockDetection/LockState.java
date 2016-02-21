package deadlockDetection;

import java.util.HashSet;

public class LockState {
	private HashSet<Handle> lockset = new HashSet<Handle>();
	
	public boolean contains(Handle handle) {
		return lockset.contains(handle);
	}
	
	public void remove(Handle handle) {
		lockset.remove(handle);
	}
}
