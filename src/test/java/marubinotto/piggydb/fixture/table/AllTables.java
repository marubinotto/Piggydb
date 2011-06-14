package marubinotto.piggydb.fixture.table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import marubinotto.util.fixture.DatabaseTableFixture;

public class AllTables {

	public GlobalSettingTable global_setting;
	public TagTable tag;
	public TaggingTable tagging;
	public FragmentTable fragment;
	public FilterTable filter;
	public FragmentRelationTable fragment_relation;
	
	public AllTables(DataSource dataSource) throws Exception {
		this.global_setting = new GlobalSettingTable(dataSource);
		this.tag = new TagTable(dataSource);
		this.tagging = new TaggingTable(dataSource);
		this.fragment = new FragmentTable(dataSource);
		this.filter = new FilterTable(dataSource);
		this.fragment_relation = new FragmentRelationTable(dataSource);
	}

	public void cleanAll() throws Exception {
		for (DatabaseTableFixture table : getTables()) {
			table.deleteAll();
		}
	}
	
	public void shouldBeEmpty() throws Exception {
		for (DatabaseTableFixture table : getTables()) {
			table.shouldBeEmpty();
		}
	}
	
	public List<DatabaseTableFixture> getTables() throws Exception {
		Field[] fields = getClass().getDeclaredFields();
		List<DatabaseTableFixture> tables = new ArrayList<DatabaseTableFixture>();
		for (Field field : fields) {
			if (Modifier.isPublic(field.getModifiers())) {
				tables.add((DatabaseTableFixture)field.get(this));
			}
		}
		return tables;
	}
}
