package deadlockDetection;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.HashSet;
import org.json.simple.JSONObject;

public class LockHandler {
	private static HashMap<Long, ProcessState> procInfoMap = new HashMap<Long, ProcessState>();
	private static HashMap<String, LockState> fileLockMap = new HashMap<String, LockState>();
	
	private static HashMap<String, Integer> handleCount = new HashMap<String, Integer>();
	
	public static void openHandler(JSONObject item) {
		String evtInfo = (String) item.get("evt.info");
		int fdPos = evtInfo.indexOf("fd=");
		if( fdPos == -1 ) return; // handle the case of evt.info = ""
		
		int oParenPos = evtInfo.indexOf("("); // (<f>...)
		int cParenPos = evtInfo.indexOf(")");
		int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
		if( fd < 0 ) return; // handle the case of evt.info = "fd=-2(ENOENT)..."
		String file = evtInfo.substring(oParenPos+4, cParenPos);
		long pid = (Long) item.get("proc.pid");
		
		if( !procInfoMap.containsKey(pid) ) {
			procInfoMap.put(pid, new ProcessState());
		}
		
		if( !handleCount.containsKey(file) ) {
			handleCount.put(file, 1);
		} else {
			int count = handleCount.get(file) + 1;
			handleCount.put(file, count);
		}
		
		Handle handle = new Handle(file, handleCount.get(file));
		ProcessState procState = procInfoMap.get(pid);
		procState.put(fd, handle);
		
		//System.out.format("open SUCCESSFUL pid=%d fd=%d fh=%d file=%s\n", pid, fd, handleCount.get(file), file);
		System.out.println("open SUCCESSFUL");
		System.out.format(" pid=%d\n", pid);
		for( Entry<Integer, Handle> entry : procState.getEntrySet() ) {
			int ifd = entry.getKey();
			Handle iHandle = entry.getValue();
			System.out.format(" | fd=%d file=%s fh=%d", ifd, iHandle.getName(), iHandle.getID());
			LockState lockState = fileLockMap.get(iHandle.getName());
			if( lockState != null && lockState.contains(iHandle) ) {
				System.out.print(" *");
			}
			System.out.println("");
		}
	}
	
	public static void closeHandler(JSONObject item) {
		String evtInfo = (String) item.get("evt.info");
		int fdPos = evtInfo.indexOf("fd=");
		int oParenPos = evtInfo.indexOf("(<f>"); // (<f>...)
		
		// handle the cases of
		// evt.info="res=..."
		// evt.info="fd=4 "
		if( fdPos == -1 || oParenPos == -1 ) return;
		
		int cParenPos = evtInfo.indexOf(")");
		int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
		String file = evtInfo.substring(oParenPos+4, cParenPos);
		long pid = (Long) item.get("proc.pid");
		
		ProcessState procState = procInfoMap.get(pid);
		if( procState == null ) return; // handle the case of previously opened processes being closed
		
		// Remove fd associated with pid
		Handle handle = procState.get(fd);
		procState.remove(fd);
		
		try {
			handle.decrementRefCount();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("close SUCCESSFUL");
		System.out.format(" pid=%d\n", pid);
		for( Entry<Integer, Handle> entry : procState.getEntrySet() ) {
			int ifd = entry.getKey();
			Handle iHandle = entry.getValue();
			System.out.format(" | fd=%d file=%s fh=%d", ifd, iHandle.getName(), iHandle.getID());
			LockState lockState = fileLockMap.get(iHandle.getName());
			if( lockState != null && lockState.contains(iHandle) ) {
				System.out.print(" *");
			}
			System.out.println("");
		}
		
		// if the handle is holding a lock, decrement the ref count, and remove the handle if the ref count = 0
		LockState lockState = fileLockMap.get(file);
		if( lockState == null ) return;
		if( !lockState.contains(handle) ) return;
		if( handle.isZero() ) {
			lockState.remove(handle);
		}
	}
	
	public static void flockHandler(JSONObject item, JSONObject itemRes) {
		String evtInfo = (String) item.get("evt.info");
		
		if( evtInfo.indexOf("LOCK_SH") != -1 ) {
			
			boolean successful = true;
			String resEvtInfo = (String) itemRes.get("evt.info");
			int resPos = resEvtInfo.indexOf("res=");
			// assumption: if the next line is not a res, request is unsuccessful
			if( resPos == -1 ) successful = false;
			if( successful ) {
				int res = Integer.parseInt( resEvtInfo.substring(resPos+4, resEvtInfo.length()-1 ) );
				if( res != 0 ) successful = false;
			}
			
			int fdPos = evtInfo.indexOf("fd=");
			int oParenPos = evtInfo.indexOf("("); // (<f>...)
			int cParenPos = evtInfo.indexOf(")");
			int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
			String file = evtInfo.substring(oParenPos+4, cParenPos);
			long pid = (Long) item.get("proc.pid");
			ProcessState procState = procInfoMap.get(pid);
			Handle handle = procState.get(fd);
			
			if( successful ) {
				
				if( !fileLockMap.containsKey(file) ) {
					fileLockMap.put(file, new LockState());
				}
				LockState lockState = fileLockMap.get(file);
				lockState.put(handle);
				
				System.out.println("flock LOCK_SH SUCCESSFUL");
				System.out.format(" pid=%d\n", pid);
				
				for( Entry<Integer, Handle> entry : procState.getEntrySet() ) {
					int ifd = entry.getKey();
					Handle iHandle = entry.getValue();
					System.out.format(" | fd=%d file=%s fh=%d", ifd, iHandle.getName(), iHandle.getID());
					LockState lstate = fileLockMap.get(iHandle.getName());
					if( lstate != null && lstate.contains(iHandle) ) {
						System.out.print(" *");
					}
					System.out.println("");
				}
				
			} else {
				
				System.out.println("flock LOCK_SH FAILED");
				System.out.format( " pid=%d fd=%d file=%s fh=%d\n", pid, fd, file, handle.getID() );
				System.out.println(" lock owner(s) are:");
				LockState lockState = fileLockMap.get(file);
				for( Handle h : lockState.getLocks() ) {
					System.out.format(" | file=%s fh=%d *\n", h.getName(), h.getID() );
				}
			}
			
		} else if( evtInfo.indexOf("LOCK_EX") != -1 ) {
			//System.out.println(item);
			boolean successful = true;
			String resEvtInfo = (String) itemRes.get("evt.info");
			int resPos = resEvtInfo.indexOf("res=");
			// assumption: if the next line is not a res, request is unsuccessful
			if( resPos == -1 ) successful = false;
			if( successful ) {
				if( !resEvtInfo.substring(resPos+4, resPos+5 ).equals("0") ) successful = false;
			}
			
			int fdPos = evtInfo.indexOf("fd=");
			int oParenPos = evtInfo.indexOf("("); // (<f>...)
			int cParenPos = evtInfo.indexOf(")");
			int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
			String file = evtInfo.substring(oParenPos+4, cParenPos);
			long pid = (Long) item.get("proc.pid");
			ProcessState procState = procInfoMap.get(pid);
			Handle handle = procState.get(fd);
			
			if( successful ) {
				
				LockState lockState = new LockState();
				lockState.put(handle);
				fileLockMap.put(file, lockState);
				
				System.out.println("flock LOCK_EX SUCCESSFUL");
				System.out.format(" pid=%d\n", pid);
				for( Entry<Integer, Handle> entry : procState.getEntrySet() ) {
					int ifd = entry.getKey();
					Handle iHandle = entry.getValue();
					System.out.format(" | fd=%d file=%s fh=%d", ifd, iHandle.getName(), iHandle.getID());
					if( iHandle == handle ) {
						System.out.print(" *");
					}
					System.out.println("");
				}
				
			} else {
				System.out.println( "flock LOCK_EX FAILED"); 
				System.out.format( " pid=%d fd=%d file=%s fh=%d\n", pid, fd, file, handle.getID() );
				System.out.println( " lock owner(s) are:" );
				LockState lockState = fileLockMap.get(file);
				for( Handle h : lockState.getLocks() ) {
					System.out.format(" | file=%s fh=%d *\n", h.getName(), h.getID() );
				}
			}
			
		} else if( evtInfo.indexOf("LOCK_UN") != -1 ) {
			
			boolean successful = true;
			String resEvtInfo = (String) itemRes.get("evt.info");
			int resPos = resEvtInfo.indexOf("res=");
			// assumption: if the next line is not a res, request is unsuccessful
			if( resPos == -1 ) successful = false;
			if( successful ) {
				int res = Integer.parseInt( resEvtInfo.substring(resPos+4, resEvtInfo.length()-1 ) );
				if( res != 0 ) successful = false;
			}
			
			int fdPos = evtInfo.indexOf("fd=");
			int oParenPos = evtInfo.indexOf("("); // (<f>...)
			int cParenPos = evtInfo.indexOf(")");
			int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
			String file = evtInfo.substring(oParenPos+4, cParenPos);
			long pid = (Long) item.get("proc.pid");
			ProcessState procState = procInfoMap.get(pid);
			Handle handle = procState.get(fd);
			
			if( successful ) {
				LockState lockState = fileLockMap.get(file);
				lockState.remove(handle);
				if( lockState.isEmpty() ) {
					fileLockMap.remove(file);
				}
				
				System.out.println("flock LOCK_UN SUCCESSFUL");
				System.out.format(" pid=%d\n", pid);
				for( Entry<Integer, Handle> entry : procState.getEntrySet() ) {
					int ifd = entry.getKey();
					Handle iHandle = entry.getValue();
					System.out.format(" | fd=%d file=%s fh=%d", ifd, iHandle.getName(), iHandle.getID());
					LockState lstate = fileLockMap.get(iHandle.getName());
					if( lstate != null && lstate.contains(iHandle) ) {
						System.out.print(" *");
					}
					System.out.println("");
				}
				
			} else {
				System.out.println("flock LOCK_UN FAILED");
				System.out.format("pid=%d fd=%d file=%s fh=%d\n", pid, fd, file, handle.getID());
			}
			
		}
	}
	
	public static void dupHandler(JSONObject item, JSONObject itemRes) {
		String evtInfo = (String) item.get("evt.info");
		int fdPos = evtInfo.indexOf("fd=");
		int oParenPos = evtInfo.indexOf("("); // (<f>...)
		int cParenPos = evtInfo.indexOf(")");
		int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
		String file = evtInfo.substring(oParenPos+4, cParenPos);
		long pid = (Long) item.get("proc.pid");
		
		String resEvtInfo = (String) itemRes.get("evt.info");
		int resFdPos = resEvtInfo.indexOf("res=");
		int resOParenPos = resEvtInfo.indexOf("("); // (<f>...)
		int resFd = Integer.parseInt( resEvtInfo.substring(resFdPos+4, resOParenPos) );
		
		ProcessState procState = procInfoMap.get(pid);
		Handle handle = procState.get(fd);
		handle.increRefCount();
		procState.put(resFd, handle);
		
		System.out.println("dup SUCCESSFUL");
		System.out.format(" pid=%d\n", pid);
		for( Entry<Integer, Handle> entry : procState.getEntrySet() ) {
			int ifd = entry.getKey();
			Handle iHandle = entry.getValue();
			System.out.format(" | fd=%d file=%s fh=%d", ifd, iHandle.getName(), iHandle.getID());
			LockState lockState = fileLockMap.get(iHandle.getName());
			if( lockState != null && lockState.contains(iHandle) ) {
				System.out.print(" *");
			}
			System.out.println("");
		}
	}
	
	public static void cloneHandler(JSONObject item) {
		long pid = (Long) item.get("proc.pid");
		long ppid = (Long) item.get("proc.ppid");
		String evtInfo = (String) item.get("evt.info");
		
		// assumption: we're only interested in evt.info = "res=..."
		if( evtInfo.indexOf("res=") == -1 ) return;
		
		// cannot support the case where ppid exists before sysdig
		if( !procInfoMap.containsKey(ppid) ) return;
		
		ProcessState pprocState = procInfoMap.get(ppid);
		
		for(Entry<Integer, Handle> entry : pprocState.getEntrySet()) {
			Handle handle = entry.getValue();
			handle.increRefCount();
		}
		
		procInfoMap.put(pid, pprocState);
		
		System.out.println("clone SUCCESSFUL");
		System.out.format(" pid=%d ppid=%d\n", pid, ppid);
		for( Entry<Integer, Handle> entry : pprocState.getEntrySet() ) {
			int ifd = entry.getKey();
			Handle iHandle = entry.getValue();
			System.out.format(" | fd=%d file=%s fh=%d", ifd, iHandle.getName(), iHandle.getID());
			LockState lockState = fileLockMap.get(iHandle.getName());
			if( lockState != null && lockState.contains(iHandle) ) {
				System.out.print(" *");
			}
			System.out.println("");
		}
	}
	
	public static void procexitHandler(JSONObject item) {
		long pid = (Long) item.get("proc.pid");
		ProcessState procState = procInfoMap.get(pid);
		
		if( procState == null ) return; // handle the case where a previous process exits
		
		for( Entry<Integer, Handle> entry : procState.getEntrySet() ) {
			Handle handle = entry.getValue();
			
			try {
				handle.decrementRefCount();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			if( handle.isZero() && fileLockMap.containsKey(handle.getName()) ) {
				LockState lockState = fileLockMap.get(handle.getName());
				if( lockState.contains(handle) ) {
					lockState.remove(handle);
				}
			}
		}
		
		procInfoMap.remove(pid);
		
		System.out.format("procexit SUCCESSFUL pid=%d\n", pid);
	}
}
