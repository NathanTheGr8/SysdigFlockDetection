package deadlockDetection;

import java.io.FileReader;

public class Controller {
	private static LockHandler lockHandler = new LockHandler();

	public static void readFile(String file) {
		JSONParser parser = new JSONParser();
		try {

			JSONArray itemList = (JSONArray) parser.parse(new FileReader(file));

			for (int i = 0, length = itemList.size(); i < length; i++) {
				handleItem(itemList, i);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void handleItem(JSONArray itemList, int index) {
		JSONObject item = (JSONObject) itemList.get(index);
		String evtType = (String) item.get("evt.type");

		if (evtType.equals("open")) {

		} else if (evtType.equals("close")) {

		} else if (evtType.equals("clone")) {

		} else if (evtType.equals("fork")) {

		} else if (evtType.equals("dup")) {

		} else if (evtType.equals("flock")) {
			JSONObject itemRes = (JSONObject) itemList.get(index + 1);
			lockHandler.flockHandler(item, itemRes);
		} else if (evtType.equals("procexit")) {

		}
	}
}
