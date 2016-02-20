package deadlockDetection;

public class LockOwner {
	private int fd;
	private long tid;

	public LockOwner(int fd, long tid) {
		this.fd = fd;
		this.tid = tid;
	}

	public int getFd() {
		return this.fd;
	}

	public long getTid() {
		return this.tid;
	}
}
