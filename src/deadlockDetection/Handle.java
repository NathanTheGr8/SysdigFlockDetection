package deadlockDetection;

public class Handle {
	private String fileName;
	private int id = 0;
	private int refCount = 0;
	
	public Handle(String name) {
		this.fileName = name;
		refCount++;
	}
	
	public void incrementId() {
		
	}
}
