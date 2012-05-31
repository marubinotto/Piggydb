package marubinotto.util.message;

public interface MessageSource {

	public String getMessage(String code);
	
	public String getMessage(String code, Object arg);
	
	public String getMessage(String code, Object[] args);
	
	public String getMessage(MessageCode messageCode);
}
