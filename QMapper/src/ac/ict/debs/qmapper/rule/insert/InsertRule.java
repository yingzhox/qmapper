package ac.ict.debs.qmapper.rule.insert;

import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import ac.ict.debs.qmapper.rule.IRule;

public class InsertRule extends IRule {

	public InsertRule(TCustomSqlStatement origin) {
		super(origin);
	}

	@Override
	public TSelectSqlStatement apply(TCustomSqlStatement origin,
			TSelectSqlStatement dest) {
		TInsertSqlStatement insert = (TInsertSqlStatement) origin;
		//可以直接复制select部分
		dest = (TSelectSqlStatement) this.parser(insert.getSubQuery().toString());
		//对select部分做添加列
		return dest;
	}

}
