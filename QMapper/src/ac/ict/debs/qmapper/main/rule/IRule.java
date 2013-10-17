package ac.ict.debs.qmapper.main.rule;

import gudusoft.gsqlparser.TCustomSqlStatement;

public interface IRule {
	public void apply(TCustomSqlStatement query);
}
