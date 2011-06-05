package marubinotto.util;

public interface MessageSource {

	public String getMessage(String code);
	
	public String getMessage(String code, Object arg);
	
	public String getMessage(String code, Object[] args);
}
