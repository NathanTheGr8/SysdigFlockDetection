package deadlockDetection;

import java.io.FileReader;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Controller {
	private static LockHandler lockHandler = new LockHandler();
	
	public static void readFile(String file) {
		//LockHandler lockHandler = new LockHandler();
		JSONParser parser = new JSONParser();
		try {
			
			JSONArray itemList = (JSONArray) parser.parse(new FileReader(file) );
			
			for(int i=0, length=itemList.size(); i<length; i++) {
				JSONObject item = (JSONObject) itemList.get(i);
				String procName = (String) item.get("proc.name");
				if( !procName.equals("vminfo") ) {
					i += handleItem(itemList, i);
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static int handleItem(JSONArray itemList, int index) {
		HashMap<Integer, JSONObject> syscallsByThread = new HashMap<Integer, JSONObject>();
		JSONObject item = (JSONObject) itemList.get(index);
		String evtType = (String) item.get("evt.type");
		String evtDir = (String) item.get("evt.dir");
		Integer evtPid = (Integer)item.get("proc.pid");
		JSONObject inCall = null;
		
		if(evtDir == ">") {
			syscallsByThread.put(evtPid, item);
			if(evtType == "flock") {
				//lockHandler.flockHandlerIn(item); // TODO: Need to detect hangs when flock called but does not return
			}
			return 1; //?
		} else {
			inCall = syscallsByThread.get(evtPid);
			syscallsByThread.remove(evtPid);
		}

		if( evtType.equals("open") ) {
			lockHandler.openHandler(item);
		} else if( evtType.equals("close") ) {
			lockHandler.closeHandler(item);
			return 1;
		} else if( evtType.equals("clone") ) {
			lockHandler.cloneHandler(item);
		} else if( evtType.equals("dup") ) {
			lockHandler.dupHandler(inCall, item);
			return 1;
		} else if( evtType.equals("flock") ) {
			lockHandler.flockHandler(inCall, item);
			return 1;
		} else if( evtType.equals("procexit") ) {
			lockHandler.procexitHandler(item);
		}
		
		return 0;
	}
}
