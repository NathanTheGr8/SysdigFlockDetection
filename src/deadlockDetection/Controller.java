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
				handleItem(itemList, i);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void handleItem(JSONArray itemList, int index) {
		JSONObject item = (JSONObject) itemList.get(index);
		String evtType = (String) item.get("evt.type");
		
		if( evtType.equals("close") ) {
			JSONObject itemRes = (JSONObject) itemList.get(index+1);
			//lockHandler.closeHandler(item, itemRes);
		} else if( evtType.equals("clone") ) {
			
		} else if( evtType.equals("fork") ) {
			
		} else if( evtType.equals("dup") ) {
			JSONObject itemRes = (JSONObject) itemList.get(index+1);
			lockHandler.dupHandler(item, itemRes);
		} else if( evtType.equals("flock") ) {
			JSONObject itemRes = (JSONObject) itemList.get(index+1);
			lockHandler.flockHandler(item, itemRes);
		} else if( evtType.equals("procexit") ) {
			
		}
	}
}
