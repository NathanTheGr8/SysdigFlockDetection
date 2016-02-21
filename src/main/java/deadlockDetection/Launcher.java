package deadlockDetection;

public class Launcher {
	public static void main(String[] args) {
		Controller main = new Controller();
		main.readFile(args[0]);
	}
}
