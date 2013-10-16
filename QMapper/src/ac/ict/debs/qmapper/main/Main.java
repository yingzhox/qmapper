package ac.ict.debs.qmapper.main;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.ESqlStatementType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sql = "select * from test";
		TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvoracle);
		sqlparser.sqltext = sql;
		int ErrorNo = sqlparser.parse();
		if (ErrorNo != 0) {
			String errorMessage = sqlparser.getErrormessage();
			System.out.println(errorMessage);
		}
		System.out.println("sdf");
		TCustomSqlStatement tCustomSqlStatement = sqlparser.sqlstatements
				.get(0);
		if (tCustomSqlStatement.sqlstatementtype == ESqlStatementType.sstselect) {
			TSelectSqlStatement select = (TSelectSqlStatement) tCustomSqlStatement;
			select.t
		}

	}
}
