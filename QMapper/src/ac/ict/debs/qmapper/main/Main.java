package ac.ict.debs.qmapper.main;

import java.io.FileNotFoundException;
import java.io.IOException;

import ac.ict.debs.qmapper.rule.RuleChain;
import ac.ict.debs.qmapper.test.Queries;
import ac.ict.debs.qmapper.util.ColumnResolver;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.ESqlStatementType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

public class Main {

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		initialize("G:/git/qmapper/QMapper/tables/tableDesc.properties");
		RuleChain chain = new RuleChain();
		chain.transform(Queries.insert);
	}

	public static void initialize(String conf) throws FileNotFoundException,
			IOException {
		ColumnResolver.initialize(conf);
	}
}
