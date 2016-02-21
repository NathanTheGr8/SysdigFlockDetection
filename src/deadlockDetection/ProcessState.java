package deadlockDetection;

import java.util.HashMap;

public class ProcessState {
	private HashMap<Integer, Handle> fdHandleMap = new HashMap<Integer, Handle>();
	
	public Handle getProcessState(int pid) {
		Handle handle = fdHandleMap.get(pid);
		if( handle == null ) {
			System.err.println("ProcessState::getProcessState - no pid found");
			System.exit(1);
		}
		return handle;
	}
}
