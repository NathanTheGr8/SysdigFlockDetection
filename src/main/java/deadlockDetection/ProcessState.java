package deadlockDetection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ProcessState {
	private HashMap<Integer, Handle> fdHandleMap = new HashMap<Integer, Handle>();
	
	public Collection<Handle> getValues() {
		return fdHandleMap.values();
	}
	
	public Handle get(int fd) {
		Handle handle = fdHandleMap.get(fd);
		if( handle == null ) {
			System.err.println("ProcessState::getProcessState - no pid found");
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
