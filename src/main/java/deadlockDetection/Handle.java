package deadlockDetection;

public class Handle {
	private String fileName;
	private int id = 0;
	private int refCount = 0;
	
	public Handle(String name, int id) {
		this.fileName = name;
		this.id = id;
		refCount++;
	}

	public String getFileName (){
		return fileName;
	}

	public int getID(){
		return id;
	}

	public int getRefCount (){
		return refCount;
	}

	public void incrementRefCount(){
		refCount++;
	}

	public void decrementRefCount(){
		refCount--;
	}
}
