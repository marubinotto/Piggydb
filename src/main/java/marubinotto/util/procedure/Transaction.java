package marubinotto.util.procedure;

import marubinotto.util.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * marubinotto.util.procedure.Transaction
 */
public class Transaction {
	
	private static Log logger = LogFactory.getLog(Transaction.class);

    private PlatformTransactionManager transactionManager;

    public Transaction() {
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Object execute(Procedure procedure) throws Exception {
        Assert.Arg.notNull(procedure, "procedure");
        Assert.Property.requireNotNull(transactionManager, "transactionManager");

        logger.info("starting a transaction ....");
        
        Object returnObject = null;
        TransactionStatus ts = this.transactionManager.getTransaction(null);
        try {
            returnObject = procedure.execute(null);
        }
        catch (Exception e) {
            this.transactionManager.rollback(ts);
            logger.info("rollbacked");
            throw e;
        }
        this.transactionManager.commit(ts);
        logger.info("commited");
        return returnObject;
    }
}
