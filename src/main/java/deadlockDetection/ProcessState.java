package deadlockDetection;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class ProcessState {
	private HashMap<Integer, Handle> fdHandleMap = new HashMap<Integer, Handle>();
	
	public Set<Entry<Integer, Handle>> getEntrySet() {
		return fdHandleMap.entrySet();
	}
	
	public Handle get(int fd) {
		Handle handle = fdHandleMap.get(fd);
		if( handle == null ) {
			System.err.println("ProcessState::get - no pid found");
			System.exit(1);
		}
		return handle;
	}
	
	public void put(int fd, Handle handle) {
		fdHandleMap.put(fd, handle);
	}
	
	public void remove(int fd) {
		fdHandleMap.remove(fd);
	}
}
