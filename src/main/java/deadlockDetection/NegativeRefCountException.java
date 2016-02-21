package deadlockDetection;

public class NegativeRefCountException extends Exception {


	public NegativeRefCountException (){

	}
  public NegativeRefCountException(String message){
     super(message);
  }

}
