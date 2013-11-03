package ac.ict.debs.qmapper.rule.update;

import gudusoft.gsqlparser.ESqlStatementType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import ac.ict.debs.qmapper.rule.IRule;

/**
 * 把Update语句转换到INSERT SELECT
 * 
 * @author Felix
 * 
 */
public class WhereToSelectRule extends IRule {

	public WhereToSelectRule(TCustomSqlStatement origin) {
		super(origin);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TSelectSqlStatement apply(TCustomSqlStatement origin, TSelectSqlStatement dest) {
		//
		if (ESqlStatementType.sstupdate == origin.sqlstatementtype) {
			
		} else {
			error("Not a update statement:" + origin.sqlstatementtype);
		}
		return dest;
	}

}
