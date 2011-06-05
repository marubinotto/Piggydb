package marubinotto.piggydb.model.filters;

import java.util.List;

import marubinotto.piggydb.fixture.H2JdbcDaoFixtures;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.RepositoryTestBase;
import marubinotto.piggydb.model.repository.FilterRepositoryRI;

import org.junit.runners.Parameterized.Parameters;

public abstract class FilterRepositoryTestBase 
extends RepositoryTestBase<FilterRepository> {
	
	public FilterRepositoryTestBase(
			RepositoryFactory<FilterRepository> factory) {
		super(factory);
	}
	
	@Parameters
	public static List<Object[]> factories() {
		return toParameters(
			// RI
			new RepositoryFactory<FilterRepository>() {
				public FilterRepository create() throws Exception {
					return new FilterRepositoryRI();
				}
			},
			// H2 database
			new RepositoryFactory<FilterRepository>() {
				public FilterRepository create() throws Exception {
					return new H2JdbcDaoFixtures().createH2FilterRepository();
				}
			}
		);
	}
	
	public Filter newFilter(String name) {
		Filter filter = this.object.newInstance(getPlainUser());
		filter.setNameByUser(name, getPlainUser());
		return filter;
	}
}
