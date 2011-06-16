package marubinotto.piggydb.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import marubinotto.piggydb.impl.db.DatabaseSchema;
import marubinotto.piggydb.impl.db.H2JdbcUrl;
import marubinotto.util.Assert;
import marubinotto.util.RdbUtils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class ExternalFactory {

	private BeanFactory factory;

	public ExternalFactory(BeanFactory factory) {
		Assert.Arg.notNull(factory, "factory");
		this.factory = factory;
	}

	public DataSource getDataSource() {
		return (DataSource) this.factory.getBean("dataSource");
	}

	public Connection getJdbcConnection() {
		return RdbUtils.getSpringTransactionalConnection(getDataSource());
	}

	public DatabaseSchema getDatabaseSchema() {
		return (DatabaseSchema) this.factory.getBean("databaseSchema");
	}

	public H2JdbcUrl getH2JdbcUrl() {
		return (H2JdbcUrl) getBeanIfExists("h2JdbcUrl");
	}

	protected Object getBeanIfExists(String beanName) {
		try {
			return this.factory.getBean(beanName);
		}
		catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}
}
