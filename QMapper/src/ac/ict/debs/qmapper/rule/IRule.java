package ac.ict.debs.qmapper.rule;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import ac.ict.debs.qmapper.util.ColumnResolver;
import ac.ict.debs.qmapper.util.ColumnResolver.Column;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.ETokenType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.TSourceToken;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TResultColumnList;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TWhereClause;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

public abstract class IRule {
	public static class ExistsJoinCondition {
		public TTable joinTable;
		public TExpression condition;
	}

	public TCustomSqlStatement getOrigin() {
		return this.origin;
	}

	protected IRule nextRule;
	protected TCustomSqlStatement origin;
	final protected static Logger LOG = Logger.getLogger(IRule.class);

	public IRule(TCustomSqlStatement origin) {
		this.origin = origin;
	}

	public abstract TSelectSqlStatement apply(TCustomSqlStatement origin,
			TSelectSqlStatement dest);

	protected void error(String message) {
		LOG.error("Error:" + message + " Rule Class:"
				+ this.getClass().getName());
	}

	protected TCustomSqlStatement parser(String sql) {
		TGSqlParser parser = new TGSqlParser(EDbVendor.dbvoracle);
		parser.sqltext = sql;
		int ErrorNo = parser.parse();
		if (ErrorNo != 0) {
			String errorMessage = parser.getErrormessage();
			LOG.error("SQL format exception:" + errorMessage);
			System.exit(1);
		}
		return parser.sqlstatements.get(0);
	}

	protected TWhereClause oppositeCondition(TWhereClause where) {
		if (where == null || where.getCondition() == null)
			return null;
		recursiveOpposite(where.getCondition());
		return where;

	}

	protected boolean isIncludeEXISTS(TWhereClause where) {
		if (where == null)
			return false;
		TExpression condition = where.getCondition();
		LinkedList<TExpression> queue = new LinkedList<TExpression>();
		queue.add(condition);
		while (!queue.isEmpty()) {
			TExpression pop = queue.pop();
			if (pop.getExpressionType() == EExpressionType.logical_and_t) {
				queue.add(pop.getLeftOperand());
				queue.add(pop.getRightOperand());
			} else if (pop.getExpressionType() == EExpressionType.logical_or_t) {
				queue.add(pop.getLeftOperand());
				queue.add(pop.getRightOperand());
			} else if (pop.getExpressionType() == EExpressionType.exists_t) {
				return true;
			} else if (pop.getExpressionType() == EExpressionType.logical_not_t) {
				// System.out.println(pop.getRightOperand().getExpressionType());
				if (pop.getRightOperand() != null
						&& pop.getRightOperand().getExpressionType() == EExpressionType.exists_t)
					return true;
			}
		}
		return false;

	}

	private void recursiveOpposite(TExpression cond) {

		// System.out.println(cond.getOperatorToken());
		if (cond.getExpressionType() == EExpressionType.logical_and_t) {
			recursiveOpposite(cond.getLeftOperand());
			recursiveOpposite(cond.getRightOperand());
			cond.setString("(" + cond.getLeftOperand() + " OR "
					+ cond.getRightOperand() + ")");
			// System.out.println(cond.getExpressionType() + " to OR");
		} else if (cond.getExpressionType() == EExpressionType.logical_or_t) {
			recursiveOpposite(cond.getLeftOperand());
			recursiveOpposite(cond.getRightOperand());
			cond.setString("(" + cond.getLeftOperand() + " AND "
					+ cond.getRightOperand() + ")");

		} else if (cond.getExpressionType() == EExpressionType.null_t) {
			if (cond.getNotToken() != null
					&& cond.getNotToken().toString().toUpperCase()
							.equals("NOT")) {
				cond.getNotToken().setString("");
			} else {
				System.out.println("leftObj:" + cond.getLeftOperand());
				System.out.println("rightObj:" + cond.getOperatorToken());
				// 这里t.x IS NULL
				// t.x是leftOperand
				// NULL 是getOperatorToken
				cond.setString(cond.getLeftOperand() + " IS NOT "
						+ cond.getOperatorToken());
			}
		} else if (cond.getExpressionType() == EExpressionType.simple_comparison_t) {
			// System.out.println("left:" + cond.getLeftOperand());
			// System.out.println("right:" + cond.getRightOperand());
			// System.out.println("op:" + cond.getComparisonOperator());
			String oppositeCompare = oppositeCompare(cond
					.getComparisonOperator());
			cond.setString(cond.getLeftOperand() + " " + oppositeCompare + " "
					+ cond.getRightOperand());
		} else {
			System.out.println("condition:" + cond + " type:"
					+ cond.getExpressionType());
		}
	}

	private String oppositeCompare(TSourceToken tok) {
		// System.out.println(tok.tokentype);
		// =
		if (tok.tokentype == ETokenType.ttequals) {
			return "!=";
			// <>
		} else if (tok.tokentype == ETokenType.ttmulticharoperator) {
			if (tok.toString().equals("<="))
				return ">";
			if (tok.toString().equals(">="))
				return "<";
			return "=";
		} else if (tok.tokentype == ETokenType.ttlessthan) {
			return ">=";
		} else if (tok.tokentype == ETokenType.ttgreaterthan) {
			return "<=";
		}
		return "NOT_IDENTIFIED";
	}

	protected TSelectSqlStatement genSelectList(TTable targetTable) {
		ArrayList<Column> table = ColumnResolver
				.getTable(targetTable.getName());
		// dest.setNodeType(TSelectSqlStatement.)
		// System.out.println(targetTable.getAliasClause());
		String alias = targetTable.getAliasClause() == null ? null
				: targetTable.getAliasClause().toString();

		String select = "SELECT ";
		for (Column col : table) {
			// dest.
			// TResultColumn newCol = new TResultColumn();
			// newCol.setString(col.columnName);
			if (alias == null) {
				select += col.columnName + ",";
			} else {
				select += alias + "." + col.columnName + ",";
			}

		}
		select = select.substring(0, select.length() - 1);
		select += " FROM " + targetTable.getFullNameWithAliasString();
		TSelectSqlStatement realSelect = (TSelectSqlStatement) this
				.parser(select);
		// System.out.println(realSelect);
		return realSelect;
	}

	// public abstract IRule next();
	protected boolean isAliasMatch(TExpression one, TTable target) {
		if (one.getObjectOperand().getObjectString() != null
				&& target.getAliasClause() != null
				&& target
						.getAliasClause()
						.toString()
						.toLowerCase()
						.equals(one.getObjectOperand().getObjectString()
								.toLowerCase()))
			return true;
		return false;
	}

	protected void removeAllResultColumn(TResultColumnList result, int size) {
		int i = 0;
		while (i++ < size) {
			result.removeResultColumn(0);
		}
		// result.removeElementAt(arg0)
	}

	protected boolean isColumnNameMatchTable(TExpression one, TTable target) {
		ArrayList<Column> table = ColumnResolver.getTable(target.getFullName());
		for (Column col : table) {
			if (one.getObjectOperand().getColumnNameOnly().toLowerCase()
					.equals(col.columnName.toLowerCase()))
				return true;
		}
		return false;
	}
}
