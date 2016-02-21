package deadlockDetection;

import java.io.FileReader;

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
				//if( !procName.equals("vminfo") ) {
					i += handleItem(itemList, i);
				//}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static int handleItem(JSONArray itemList, int index) {
		JSONObject item = (JSONObject) itemList.get(index);
		String evtType = (String) item.get("evt.type");
		if( evtType.equals("open") ) {
			lockHandler.openHandler(item);
		} else if( evtType.equals("close") ) {
			lockHandler.closeHandler(item);
		} else if( evtType.equals("clone") ) {
			lockHandler.cloneHandler(item);
		} else if( evtType.equals("dup") ) {
			JSONObject itemRes = (JSONObject) itemList.get(index+1);
			lockHandler.dupHandler(item, itemRes);
			return 1;
		} else if( evtType.equals("flock") ) {
			JSONObject itemRes = (JSONObject) itemList.get(index+1);
			lockHandler.flockHandler(item, itemRes);
			return 1;
		} else if( evtType.equals("procexit") ) {
			lockHandler.procexitHandler(item);
		}
		
		return 0;
	}
}
