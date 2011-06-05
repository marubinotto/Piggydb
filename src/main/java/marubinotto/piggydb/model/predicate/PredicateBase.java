package marubinotto.piggydb.model.predicate;

import marubinotto.piggydb.model.Classifiable;
import marubinotto.util.Assert;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.UnhandledException;

public abstract class PredicateBase implements Predicate {

	public boolean evaluate(Object object) {
		Assert.Arg.notNull(object, "object");
		
		if (!(object instanceof Classifiable)) {
			return false;
		}
		try {
			return evaluate((Classifiable)object);
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
	}

	protected abstract boolean evaluate(Classifiable classifiable) throws Exception;
}
