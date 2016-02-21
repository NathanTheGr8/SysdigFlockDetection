package deadlockDetection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Iterator;

import org.json.simple.JSONObject;

public class LockHandler {
	private static HashMap<Long, ProcessState> procInfoMap = new HashMap<Long, ProcessState>();
	private static HashMap<String, LockState> fileLockMap = new HashMap<String, LockState>();
	
	private static HashMap<String, Integer> handleCount = new HashMap<String, Integer>();
	
	public static void openHandler(JSONObject item) {
		String evtInfo = (String) item.get("evt.info");
		int fdPos = evtInfo.indexOf("fd=");
		
		if( fdPos != -1 ) {
			int oParenPos = evtInfo.indexOf("("); // (<f>...)
			int cParenPos = evtInfo.indexOf(")");
			int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
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
			
			System.out.format("open SUCCESSFUL pid=%d fd=%d fh=%d file=%s\n", pid, fd, handleCount.get(file), file);
		}
	}
	
	public static void closeHandler(JSONObject item) {
		String evtInfo = (String) item.get("evt.info");
		int fdPos = evtInfo.indexOf("fd=");
		int oParenPos = evtInfo.indexOf("("); // (<f>...)
		
		if( fdPos != -1 && oParenPos != -1 ) {
			int cParenPos = evtInfo.indexOf(")");
			int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
			String file = evtInfo.substring(oParenPos+4, cParenPos);
			long pid = (Long) item.get("proc.pid");
			
			ProcessState procState = procInfoMap.get(pid);
			if( procState != null ) {
				procState.remove(fd);
				
				System.out.format("close SUCCESSFUL pid=%d fd=%d\n", pid, fd);
			}
			
		}
	}
	
	public static void flockHandler(JSONObject item, JSONObject itemRes) {
		String evtInfo = (String) item.get("evt.info");
		
		if( evtInfo.indexOf("LOCK_SH") != -1 ) {
			/*
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
			if( successful ) {
				if( !fileLockMap.containsKey(file) ) {
					fileLockMap.put( file, new HashSet<LockOwner>() );
				}
				LockOwner lockOwner = new LockOwner(fd, pid);
				lockMap.get(file).add(lockOwner);
				
				System.out.format( "proc.pid=%d fd=%d flock LOCK_SH %s SUCCESSFUL - lock owner(s) are [ ", pid, fd, file);
				for(LockOwner ilockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d pid=%d; ", ilockOwner.getFd(), ilockOwner.getPid() );
				}
				System.out.println("]");
				
				if( !procMap.containsKey(pid) ) {
					procMap.put(pid, new ArrayList<String>());
				}
			} else {
				System.out.format( "proc.pid=%d fd=%d flock LOCK_SH %s FAILED - lock owner(s) are [ ", pid, fd, file);
				for(LockOwner lockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d pid=%d; ", lockOwner.getFd(), lockOwner.getPid() );
				}
				System.out.println("]");
			}
			*/
		} else if( evtInfo.indexOf("LOCK_EX") != -1 ) {
			
			/*boolean successful = true;
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
			if( successful ) {
				
				
				System.out.format( "proc.pid=%d fd=%d flock LOCK_EX %s SUCCESSFUL\n", pid, fd, file);
			} else {
				System.out.format( "proc.pid=%d fd=%d flock LOCK_EX %s FAILED - lock owner(s) are [ ", pid, fd, file);
				for(LockOwner lockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d pid=%d; ", lockOwner.getFd(), lockOwner.getPid() );
				}
				System.out.println("]");
			}*/
			
		} else if( evtInfo.indexOf("LOCK_UN") != -1 ) {
			/*
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
			if( successful ) {
				for( LockOwner lockOwner : lockMap.get(file) ) {
					if( fd == lockOwner.getFd() ) {
						lockMap.get(file).remove(lockOwner);
						break;
					}
				}
				System.out.format("thread.pid=%d fd=%d flock LOCK_UN %s SUCCESSFUL - lock owner(s) are [ ", pid, fd, file);
				for( LockOwner lockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d pid=%d; ", lockOwner.getFd(), lockOwner.getPid() );
				}
				System.out.println("]");
				if( lockMap.get(file).size() == 0 ) lockMap.remove(file);
			} else {
				System.out.format("proc.pid=%d fd=%d flock LOCK_UN %s FAILED\n", pid, fd, file);
			}
			*/
		}
	}
	
	/*public static void closeHandler(JSONObject item, JSONObject itemRes) {
		String evtInfo = (String) item.get("evt.info");
		
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
		System.out.println(item);
		int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
		String file = evtInfo.substring(oParenPos+4, cParenPos);
		long pid = (Long) item.get("proc.pid");
		//System.out.println(file);
		if( file.length() > 0 ) {
			if( successful ) {
				for( LockOwner lockOwner : lockMap.get(file) ) {
					if( fd == lockOwner.getFd() ) {
						lockMap.get(file).remove(lockOwner);
						break;
					}
				}
				System.out.format("proc.pid=%d fd=%d close %s SUCCESSFUL - lock owner(s) are [ ", pid, fd, file);
				for( LockOwner lockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d pid=%d; ", lockOwner.getFd(), lockOwner.getPid() );
				}
				System.out.println("]");
				if( lockMap.get(file).size() == 0 ) lockMap.remove(file);
			} else {
				System.out.format("proc.pid=%d fd=%d close %s FAILED\n", pid, fd, file);
			}
		}
	}
	*/
	
	public static void dupHandler(JSONObject item, JSONObject itemRes) {
		String evtInfo = (String) item.get("evt.info");
		int fdPos = evtInfo.indexOf("fd=");
		int oParenPos = evtInfo.indexOf("("); // (<f>...)
		int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
		long pid = (Long) item.get("proc.pid");
		
		String resEvtInfo = (String) itemRes.get("evt.info");
		int resFdPos = resEvtInfo.indexOf("res=");
		int resOParenPos = resEvtInfo.indexOf("("); // (<f>...)
		int resFd = Integer.parseInt( resEvtInfo.substring(resFdPos+4, resOParenPos) );
		
		ProcessState procState = procInfoMap.get(pid);
		Handle handle = procState.get(fd);
		procState.put(resFd, handle);
		
		System.out.format("dup SUCCESSFUL pid=%d fd=%d resFd=%d file=%s\n", pid, fd, resFd, handle.getName());
	}
	
	public static void cloneHandler(JSONObject item) {
		long pid = (Long) item.get("proc.pid");
		long ppid = (Long) item.get("proc.ppid");
		
		if( !procInfoMap.containsKey(ppid) ) {
			procInfoMap.put(ppid, new ProcessState());
		}
		
		ProcessState pprocState = procInfoMap.get(ppid);
		
		Collection<Handle> values = pprocState.getValues();
		for( Handle handle : values ) {
			handle.increRefCount();
		}
		
		procInfoMap.put(pid, pprocState);
		
		System.out.format("clone SUCCESSFUL pid=%d ppid=%d\n", pid, ppid);
	}
	
	public static void procexitHandler(JSONObject item) {
		long pid = (Long) item.get("proc.pid");
		ProcessState procState = procInfoMap.get(pid);
		
		Collection<Handle> values = procState.getValues();
		for( Handle handle : values ) {
			handle.decreRefCount();
			
			if( handle.isZero()
				&& fileLockMap.containsKey(handle.getName()) ) {
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
