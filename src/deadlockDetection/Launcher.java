package deadlockDetection;

public class Launcher {
	public static void main(String[] args) {
		Controller main = new Controller();
		main.readFile("/Users/liwingyee/Documents/eclipse/workspace/deadlockDetection/src/main/resources/logRaw_SH_DeadLock_ShEx.json");
	}
}
