package deadlockDetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
			
			if( !handleMap.containsKey(file) ) {
				handleMap.put(file, 0);
			}
			
			
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
			
		} else if( evtInfo.indexOf("LOCK_EX") != -1 ) {
			
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
				ArrayList<LockOwner> lockArray = new ArrayList<LockOwner>();
				LockOwner lockOwner = new LockOwner(fd, pid);
				lockArray.add(lockOwner);
				lockMap.put( file, lockArray );
				
				System.out.format( "proc.pid=%d fd=%d flock LOCK_EX %s SUCCESSFUL\n", pid, fd, file);
			} else {
				System.out.format( "proc.pid=%d fd=%d flock LOCK_EX %s FAILED - lock owner(s) are [ ", pid, fd, file);
				for(LockOwner lockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d pid=%d; ", lockOwner.getFd(), lockOwner.getPid() );
				}
				System.out.println("]");
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
	
	public static void dupHandler(JSONObject item, JSONObject itemRes) {
		String evtInfo = (String) item.get("evt.info");
		int fdPos = evtInfo.indexOf("fd=");
		int oParenPos = evtInfo.indexOf("("); // (<f>...)
		int cParenPos = evtInfo.indexOf(")");
		int fd = Integer.parseInt( evtInfo.substring(fdPos+3, oParenPos) );
		String file = evtInfo.substring(oParenPos+4, cParenPos);
		long pid = (Long) item.get("proc.pid");
		
		LockOwner lockOwner = new LockOwner(fd, pid);
		if( !lockMap.containsKey(file) ) {
			lockMap.put(file, new ArrayList<LockOwner>());
		}
		lockMap.get(file).add(lockOwner);
		
		System.out.format("proc.pid=%d fd=%d dup %s SUCCESSFUL - lock owner(s) are [ ", pid, fd, file);
		for( LockOwner iLockOwner : lockMap.get(file) ) {
			System.out.format("proc.pid=%d fd=%d; ", iLockOwner.getPid(), iLockOwner.getFd());
		}
		System.out.println("]");
		
		// assumption:
		// format
		// fd=3(<f>/home/ubuntu/hello)
		// res=10(<f>/home/ubuntu/hello)
		String resEvtInfo = (String) itemRes.get("evt.info");
		int resFdPos = resEvtInfo.indexOf("res=");
		int resOParenPos = resEvtInfo.indexOf("(");
		int resFd = Integer.parseInt( resEvtInfo.substring(resFdPos+4, resOParenPos) );
		long resPid = (Long) itemRes.get("proc.pid");
		
		lockOwner = new LockOwner(resFd, resPid);
		lockMap.get(file).add(lockOwner);
		
		System.out.format("proc.pid=%d fd=%d dup %s SUCCESSFUL - lock owner(s) are [ ", resPid, resFd, file);
		for( LockOwner iLockOwner : lockMap.get(file) ) {
			System.out.format("proc.pid=%d fd=%d; ", iLockOwner.getPid(), iLockOwner.getFd());
		}
		System.out.println("]");
		
	}
	
	public static void cloneHandler() {
		
	}*/
}
