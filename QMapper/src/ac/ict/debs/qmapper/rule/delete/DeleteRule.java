package ac.ict.debs.qmapper.rule.delete;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TJoin;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TResultColumnList;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TWhereClause;
import gudusoft.gsqlparser.stmt.TDeleteSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import ac.ict.debs.qmapper.rule.IRule;
import ac.ict.debs.qmapper.rule.JoinConditionVisitor;

public class DeleteRule extends IRule {

	public DeleteRule(TCustomSqlStatement origin) {
		super(origin);
	}

	@Override
	public TSelectSqlStatement apply(TCustomSqlStatement origin,
			TSelectSqlStatement dest) {
		TDeleteSqlStatement del = (TDeleteSqlStatement) origin;
		dest = this.genSelectList(del.getTargetTable());
		if (origin.getWhereClause() != null) {
			if (!this.isIncludeEXISTS(origin.getWhereClause())) {
				// copy to generate condition
				TCustomSqlStatement copy = this.parser(origin.toString());

				TWhereClause oppositeCondition = this.oppositeCondition(copy
						.getWhereClause());
				// System.out.println("oppo:" + oppositeCondition);
				if (oppositeCondition != null)
					dest.addWhereClause(oppositeCondition.toString());
			} else {
				// 如果包含EXISTS
				// 这里认为EXISTS里面的条件都是AND连接
				// 先把EXISTS或者NOT EXISTS的JOIN给拿出来放上
				TCustomSqlStatement copy = this.parser(origin.toString());
				extractExistsSubQueryToJOIN(copy.getWhereClause(), dest);
				// 然后对WHERE做一些替换并取反
				TWhereClause oppositeCondition = this.oppositeCondition(copy
						.getWhereClause());
				dest.addWhereClause(oppositeCondition.getCondition().toString());
			}
		}
		// System.out.println(dest);
		return dest;
	}

	private void extractExistsSubQueryToJOIN(TWhereClause where,
			TSelectSqlStatement dest) {
		TExpression condition = where.getCondition();
		LinkedList<TExpression> queue = new LinkedList<TExpression>();
		queue.add(condition);
		ArrayList<JoinTableAndCondition> batchAddJoin = new ArrayList<JoinTableAndCondition>();
		while (!queue.isEmpty()) {
			TExpression pop = queue.pop();
			if (pop.getExpressionType() == EExpressionType.logical_and_t) {
				queue.add(pop.getLeftOperand());
				queue.add(pop.getRightOperand());
			} else if (pop.getExpressionType() == EExpressionType.logical_or_t) {
				queue.add(pop.getLeftOperand());
				queue.add(pop.getRightOperand());
			} else if (pop.getExpressionType() == EExpressionType.exists_t) {
				// here extract exists sub query
				// System.out.println("sub:" + pop.getSubQuery());

				// JoinConditionVisitor v = new JoinConditionVisitor();
				// pop.getSubQuery().getWhereClause().getCondition()
				// .preOrderTraverse(v);
				// System.out.println(v.getJrs());
				// 先提取需要JOIN的表以及JOIN的条件，并 把需要JOIN的条件从WHERE里面去掉
				JoinTableAndCondition modifyExistsToJoin = modifyExistsToJoin(
						pop, dest.joins.getJoin(0));
				batchAddJoin.add(modifyExistsToJoin);

				// 现在处理EXISTS处的条件
				// if (pop.getNotToken() == null
				// || pop.getNotToken().toString() == null
				// || pop.getNotToken().toString().equals("")) {
				// EXISTS，应该判定IS NOT NULL，考虑到这里是DELETE转的，因此改写为NOT NULL
				pop.setString(modifyExistsToJoin.selectItem.get(0) + " IS NULL");
				// } else {
				// // NOT EXISTS，改成IS NULL
				// pop.setString(modifyExistsToJoin.selectItem.get(0)
				// + " IS NULL");
				// }
			} else if (pop.getExpressionType() == EExpressionType.logical_not_t
					&& pop.getRightOperand() != null
					&& pop.getRightOperand().getExpressionType() == EExpressionType.exists_t) {
				// 处理NOT EXISTS的情况
				// here extract exists sub query
				// System.out.println("sub:" + pop.getSubQuery());

				// JoinConditionVisitor v = new JoinConditionVisitor();
				// pop.getSubQuery().getWhereClause().getCondition()
				// .preOrderTraverse(v);
				// System.out.println(v.getJrs());
				// 先提取需要JOIN的表以及JOIN的条件，并 把需要JOIN的条件从WHERE里面去掉
				JoinTableAndCondition modifyExistsToJoin = modifyExistsToJoin(
						pop.getRightOperand(), dest.joins.getJoin(0));
				batchAddJoin.add(modifyExistsToJoin);

				// 现在处理EXISTS处的条件
				// if (pop.getNotToken() == null
				// || pop.getNotToken().toString() == null
				// || pop.getNotToken().toString().equals("")) {
				// // EXISTS，应该判定IS NOT NULL，考虑到这里是DELETE转的，因此改写为NOT NULL
				// pop.setString(modifyExistsToJoin.selectItem.get(0)
				// + " IS NOT NULL");
				// } else {
				// NOT EXISTS，即删掉未匹配的，保留匹配的 改成IS NOT NULL
				pop.setString(modifyExistsToJoin.selectItem.get(0)
						+ " IS NOT NULL");
				// }
			} else {
				System.out.println("Type:" + pop.getExpressionType()
						+ " Content:" + pop);
			}
		}
		// 最后批量处理，否则会影响后面的表查找
		for (JoinTableAndCondition modifyExistsToJoin : batchAddJoin) {
			TJoin join = dest.joins.getJoin(0);
			join.setString(join + " LEFT OUTER JOIN "
					+ modifyExistsToJoin.tableNameOrSubQuery + " "
					+ modifyExistsToJoin.alias + " ON "
					+ modifyExistsToJoin.condition);
		}
	}

	private static class JoinTableAndCondition {
		String tableNameOrSubQuery;
		boolean isSubQuery = false;
		String alias = "";
		String condition = "";
		ArrayList<String> selectItem = new ArrayList<String>();

		public String toString() {
			return "JOIN:" + this.alias + " ON:" + condition;
		}
	}

	private static int counter = 1;
	final protected static Logger LOG = Logger.getLogger(DeleteRule.class);

	private JoinTableAndCondition modifyExistsToJoin(TExpression exists,
			TJoin master) {
		JoinTableAndCondition res = new JoinTableAndCondition();
		res.alias = "outer_" + counter++;
		res.isSubQuery = true;

		TSelectSqlStatement subQuery = exists.getSubQuery();

		// System.out.println("sub before:" + exists.getSubQuery());
		// System.out.println("master:" + master.getTable());
		// int count = subQuery.getResultColumnList().size();
		// this.removeAllResultColumn(subQuery.getResultColumnList(), count);
		// subQuery.setResultColumnList(new TResultColumnList());
		// subQuery.getResultColumnList().setString("1");
		ArrayList<TExpression> neededToBeRemove = new ArrayList<TExpression>();
		boolean first = true;
		if (subQuery.getWhereClause() != null
				&& subQuery.getWhereClause().getCondition() != null) {
			// 分析condition
			LinkedList<TExpression> queue = new LinkedList<TExpression>();
			queue.add(subQuery.getWhereClause().getCondition());
			while (!queue.isEmpty()) {
				TExpression pop = queue.pop();
				if (pop.getExpressionType() == EExpressionType.logical_and_t) {
					queue.add(pop.getLeftOperand());
					queue.add(pop.getRightOperand());
				} else if (pop.getExpressionType() == EExpressionType.logical_or_t) {
					queue.add(pop.getLeftOperand());
					queue.add(pop.getRightOperand());
				} else if (pop.getExpressionType() == EExpressionType.simple_comparison_t) {
					if (pop.getLeftOperand().getExpressionType() == EExpressionType.simple_object_name_t
							&& pop.getRightOperand().getExpressionType() == EExpressionType.simple_object_name_t) {
						// 这里要区分主表条件和子条件
						TObjectName joinTable = null;
						if (this.isAliasMatch(pop.getLeftOperand(),
								master.getTable())) {
							// 左边是主表
							// 改变alias
							joinTable = pop.getRightOperand()
									.getObjectOperand();
						} else if (this.isAliasMatch(pop.getRightOperand(),
								master.getTable())) {
							// 右边是主表
							joinTable = pop.getLeftOperand().getObjectOperand();
						} else if (this.isColumnNameMatchTable(
								pop.getLeftOperand(), master.getTable())) {
							// 依靠列名判断,左边是主表
							joinTable = pop.getRightOperand()
									.getObjectOperand();
						} else if (this.isColumnNameMatchTable(
								pop.getRightOperand(), master.getTable())) {
							// 依靠列名判断,右边是主表
							joinTable = pop.getLeftOperand().getObjectOperand();
						} else {
							LOG.info("Condition is two side objectName but not satisfied to be extracted!");
							continue;
						}
						// 先把改列加入select
						// System.out.println("table:" + joinTable);
						// if (subQuery.getResultColumnList().toString()
						// .equals("")) {
						// subQuery.getResultColumnList().setString(
						// joinTable.toString());
						// } else {
						// subQuery.getResultColumnList().setString(
						// subQuery.getResultColumnList().toString()
						// + "," + joinTable);
						// }
						if (first) {
							first = false;
							subQuery.getResultColumnList().setString(
									joinTable.toString());
						} else {
							subQuery.getResultColumnList().addResultColumn(
									joinTable.toString());
						}
						// 改变alias
						joinTable.setString(res.alias + "."
								+ joinTable.getColumnNameOnly());
						res.selectItem.add(joinTable.toString());
						if (res.condition.equals("")) {
							res.condition += pop;
						} else {
							res.condition += " AND " + pop;
						}
						// 这些需要被remove掉
						neededToBeRemove.add(pop);
						// System.out.println("simple:"
						// + pop
						// + " left:"
						// + pop.getLeftOperand()
						// + " right:"
						// + pop.getRightOperand()
						// + " left alias:"
						// + pop.getLeftOperand().getObjectOperand()
						// .getObjectString());
					}
				}
			}
		}
		while (iterativeRemove(neededToBeRemove))
			;
		// System.out.println("sub after:" + subQuery);
		res.tableNameOrSubQuery = subQuery.toString();
		return res;
	}

	private boolean iterativeRemove(ArrayList<TExpression> neededToBeRemove) {
		if (neededToBeRemove.size() == 0)
			return false;
		HashSet<TExpression> removeAll = new HashSet<TExpression>();
		boolean flag = false;
		int fixSize = neededToBeRemove.size();
		for (int i = 0; i < fixSize; i++) {
			TExpression needed = neededToBeRemove.get(i);

			// 如果parent是logic，则需要判定父是否有必要移除
			if (needed.getParentExpr().getExpressionType() == EExpressionType.logical_and_t
					|| needed.getParentExpr().getExpressionType() == EExpressionType.logical_or_t) {
				TExpression otherChild = null;
				if (needed == needed.getParentExpr().getLeftOperand())
					otherChild = needed.getParentExpr().getRightOperand();
				else
					otherChild = needed.getParentExpr().getLeftOperand();
				// 如果另外一个孩子也要移除，那么父就要被移除
				if (neededToBeRemove.indexOf(otherChild) >= 0) {
					removeAll.add(needed);
					removeAll.add(otherChild);
					neededToBeRemove.add(needed.getParentExpr());
					flag = true;
				} else if (needed.getParentExpr() != null && otherChild != null
						&& otherChild.toString() != null) {
					// 否则的话，用另外一个孩子替代父亲
					needed.getParentExpr().setString(otherChild.toString());
				}
			}
			if (needed != null && needed.toString() != null)
				needed.setString(" ");
		}
		neededToBeRemove.removeAll(removeAll);
		return flag;
	}
}
