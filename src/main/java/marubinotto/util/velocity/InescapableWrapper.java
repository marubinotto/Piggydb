package marubinotto.util.velocity;

public class InescapableWrapper implements Inescapable {

	 private Object object;
	 
	 public InescapableWrapper(Object object) {
         this.object = object;
     }
 
     public String toString() {
         return this.object == null ? "" : this.object.toString();
     }
}
