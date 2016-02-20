package deadlockDetection;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;

public class LockHandler {
	public static HashMap<String, ArrayList<LockOwner>> lockMap = new HashMap<String, ArrayList<LockOwner>>();
	
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
			long tid = (Long) item.get("thread.tid");
			if( successful ) {
				if( !lockMap.containsKey(file) ) {
					lockMap.put( file, new ArrayList<LockOwner>() );
				}
				LockOwner lockOwner = new LockOwner(fd, tid);
				lockMap.get(file).add(lockOwner);
				
				System.out.format( "thread.tid=%d fd=%d requests for LOCK_SH on %s SUCCESSFUL - lock owner(s) is/are [ ", tid, fd, file);
				for(LockOwner ilockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d tid=%d; ", ilockOwner.getFd(), ilockOwner.getTid() );
				}
				System.out.println("]");
			} else {
				System.out.format( "thread.tid=%d fd=%d requests for LOCK_SH on %s FAILED - lock owner(s) is/are [ ", tid, fd, file);
				for(LockOwner lockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d tid=%d; ", lockOwner.getFd(), lockOwner.getTid() );
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
			long tid = (Long) item.get("thread.tid");
			if( successful ) {
				ArrayList<LockOwner> lockArray = new ArrayList<LockOwner>();
				LockOwner lockOwner = new LockOwner(fd, tid);
				lockArray.add(lockOwner);
				lockMap.put( file, lockArray );
				
				System.out.format( "thread.tid=%d fd=%d requests for LOCK_EX on %s SUCCESSFUL\n", tid, fd, file);
			} else {
				System.out.format( "thread.tid=%d fd=%d requests for LOCK_EX on %s FAILED - lock owner(s) is/are [ ", tid, fd, file);
				for(LockOwner lockOwner : lockMap.get(file) ) {
					System.out.format( "fd=%d tid=%d; ", lockOwner.getFd(), lockOwner.getTid() );
				}
				System.out.println("]");
			}
			
		} else if( evtInfo.indexOf("LOCK_UN") != -1 ) {
			
		}
	}
}
