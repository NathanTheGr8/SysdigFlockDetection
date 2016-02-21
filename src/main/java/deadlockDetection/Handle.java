package deadlockDetection;

public class Handle {
	private String fileName;
	private int id;
	private int refCount = 1;
	
	public Handle(String name, int id) {
		this.fileName = name;
		this.id = id;
	}
	
	public String getName() {
		return fileName;
	}
	
	public void increRefCount() {
		refCount++;
	}
	
	public void decreRefCount() {
		refCount--;
	}
	
	public boolean isZero() {
		return refCount == 0;
	}
}
