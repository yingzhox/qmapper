package ac.ict.debs.qmapper.test;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;

public class ParseTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sql = "select a,b,c,d from test where a=1 OR b=2 AND EXISTS( select x from bx where x=test.a)";
		TGSqlParser parser = new TGSqlParser(EDbVendor.dbvoracle);
		parser.sqltext = sql;
		int ErrorNo = parser.parse();
		if (ErrorNo != 0) {
			String errorMessage = parser.getErrormessage();
		}
		TCustomSqlStatement exp = parser.sqlstatements.get(0);
		;
		EExpressionType type;
		System.out.println(exp.getWhereClause().getCondition()
				.getExpressionType());
	}

}
