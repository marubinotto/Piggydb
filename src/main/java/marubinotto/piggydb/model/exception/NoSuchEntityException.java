package marubinotto.piggydb.model.exception;

public class NoSuchEntityException extends Exception {

	public Long id;
	
	public NoSuchEntityException(Long id) {
		super();
		this.id = id;
	}
}
