package ac.ict.debs.qmapper.rule.update;

import java.util.ArrayList;
import java.util.LinkedList;

import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TResultColumn;
import gudusoft.gsqlparser.nodes.TResultColumnList;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TTableList;
import gudusoft.gsqlparser.nodes.TWhereClause;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import gudusoft.gsqlparser.stmt.TUpdateSqlStatement;
import ac.ict.debs.qmapper.exception.TableNotFoundException;
import ac.ict.debs.qmapper.rule.IRule;
import ac.ict.debs.qmapper.util.ColumnResolver;
import ac.ict.debs.qmapper.util.ColumnResolver.Column;

public class UpdateRule extends IRule {

	public UpdateRule(TCustomSqlStatement origin, ColumnResolver resolver) {
		super(origin, resolver);
	}

	// dest 是生成的
	// origin 是一个TUpdateSqlStatement
	@Override
	public TSelectSqlStatement apply(TCustomSqlStatement origin,
			TSelectSqlStatement dest) throws TableNotFoundException {
		TUpdateSqlStatement update = (TUpdateSqlStatement) origin;

		// 先把update的目标表给dest
		dest.setTargetTable(update.getTargetTable());
		// 找到要更新的表，然后查找所有的列
		TTable targetTable = update.getTargetTable();

		// 生成列表
		dest = genSelectList(targetTable);
		// dest.set

		// 生成条件
		// dest.addWhereClause(origin.getWhereClause().toString());
		// 根据UPDATE的条件生成SELECT，这里只考虑不包含EXISTS的情况
		if (!isIncludeEXISTS(origin.getWhereClause())) {
			extractSimpleWhereToSelect(update, dest);
		}
		System.out.println(origin.getResultColumnList());
		System.out.println(dest.toString());
		return dest;
	}

	/**
	 * 该方法将Update中的WHERE条件转换到SELECT的列当中
	 * 
	 * @param origin
	 * @param select
	 */
	private void extractSimpleWhereToSelect(TUpdateSqlStatement origin,
			TSelectSqlStatement select) {
		TResultColumnList resultColumnList = select.getResultColumnList();
		long columnNo = resultColumnList.getColumnNo();
		TResultColumnList updateSet = origin.getResultColumnList();
		for (int i = 0; i < columnNo
				&& resultColumnList.getResultColumn(i) != null; i++) {
			TResultColumn resultColumn = resultColumnList.getResultColumn(i);
			// 检查该列是否在Update的SET语句中

			for (int j = 0; j < updateSet.getColumnNo()
					&& updateSet.getResultColumn(j) != null; j++) {
				TResultColumn set = updateSet.getResultColumn(j);
				// 找到了，需要替换成Update的值
				// System.out.println(set.getExpr().getLeftOperand()
				// .getExpressionType());
				// System.out.println(resultColumn.getExpr().getObjectOperand()
				// .getColumnNameOnly());
				if (set.getExpr()
						.getLeftOperand()
						.getObjectOperand()
						.getColumnNameOnly()
						.equals(resultColumn.getExpr().getObjectOperand()
								.getColumnNameOnly())) {
					// System.out.println("-:" + resultColumn);
					// System.out.println("--:" + set);
					setCaseWhenExpr(resultColumn, set, origin.getWhereClause());
				}
			}
		}
	}

	private void setCaseWhenExpr(TResultColumn dest, TResultColumn update,
			TWhereClause where) {
		String origin = dest.toString();
		if (where != null) {
			String whereStr = "CASE WHEN ";
			whereStr += where.getCondition();
			whereStr += " THEN " + update.getExpr().getRightOperand();
			whereStr += " ELSE " + origin + " END AS "
					+ dest.getExpr().getObjectOperand().getColumnNameOnly();
			dest.setString(whereStr);
		} else {
			dest.setString(update.getExpr().getRightOperand() + " AS "
					+ dest.getExpr().getObjectOperand().getColumnNameOnly());
		}
	}

}
