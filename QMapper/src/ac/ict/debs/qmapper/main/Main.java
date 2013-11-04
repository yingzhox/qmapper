package ac.ict.debs.qmapper.main;

import java.io.FileNotFoundException;
import java.io.IOException;

import ac.ict.debs.qmapper.exception.TableNotFoundException;
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
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ColumnResolver initializeWithFile = ColumnResolver
				.initializeWithFile("G:/git/qmapper/QMapper/tables/tableDesc.properties");
		RuleChain chain = new RuleChain(initializeWithFile);
		chain.transform(Queries.insert);
	}
}
