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
	public boolean isZero() {
		return refCount == 0;
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

	public int decrementRefCount() throws Exception{
		
		refCount--;
		if (refCount < 0) {
			throw new negitiveRefCount("negitive refCount is not valid");
		}

		return refCount;
	}
}
