package ac.ict.debs.qmapper.rule;

import java.util.ArrayList;

import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.nodes.IExpressionVisitor;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TParseTreeNode;

public class JoinConditionVisitor implements IExpressionVisitor {
	class JoinCondition {

		public String lefttable, righttable, leftcolumn, rightcolumn;
		public jointype jt;
		public Boolean used;
		public TExpression lexpr, rexpr, expr;

		public String toString() {
			return "leftTable:" + lefttable + " rightTable:" + righttable
					+ " condition:" + leftcolumn + " : " + rightcolumn;
		}
	}

	enum jointype {
		inner, left, right
	};

	Boolean isFirstExpr = true;
	ArrayList<JoinCondition> jrs = new ArrayList<JoinCondition>();

	public ArrayList<JoinCondition> getJrs() {
		return jrs;
	}

	boolean is_compare_condition(EExpressionType t) {
		return ((t == EExpressionType.simple_comparison_t)
				|| (t == EExpressionType.group_comparison_t) || (t == EExpressionType.in_t));
	}

	private void analyzeJoinCondition(TExpression expr, TExpression parent_expr) {
		TExpression slexpr, srexpr, lc_expr = expr;

		if (is_compare_condition(lc_expr.getExpressionType())) {
			slexpr = lc_expr.getLeftOperand();
			srexpr = lc_expr.getRightOperand();

			if (slexpr.isOracleOuterJoin() || srexpr.isOracleOuterJoin()) {
				JoinCondition jr = new JoinCondition();
				jr.used = false;
				jr.lexpr = slexpr;
				jr.rexpr = srexpr;
				jr.expr = expr;
				if (slexpr.isOracleOuterJoin()) {
					// If the plus is on the left, the join type is right out
					// join.
					jr.jt = jointype.right;
					// remove (+)
					slexpr.getEndToken().setString("");
				}
				if (srexpr.isOracleOuterJoin()) {
					// If the plus is on the right, the join type is left out
					// join.
					jr.jt = jointype.left;
					srexpr.getEndToken().setString("");
				}
				if ((slexpr.getExpressionType() == EExpressionType.simple_constant_t)) {
					jr.lefttable = null;
					jr.righttable = srexpr.getObjectOperand().getObjectString();
				} else if (srexpr.getExpressionType() == EExpressionType.simple_constant_t) {
					jr.righttable = null;
					jr.lefttable = slexpr.getObjectOperand().getObjectString();
				} else {
					jr.lefttable = slexpr.getObjectOperand().getObjectString();
					jr.righttable = srexpr.getObjectOperand().getObjectString();
				}
				jrs.add(jr);
				// System.out.printf( "join condition: %s\n", expr.toString( )
				// );
			} else if ((slexpr.getExpressionType() == EExpressionType.simple_object_name_t)
					&& (!slexpr.toString().startsWith(":"))
					&& (!slexpr.toString().startsWith("?"))
					&& (srexpr.getExpressionType() == EExpressionType.simple_object_name_t)
					&& (!srexpr.toString().startsWith(":"))
					&& (!srexpr.toString().startsWith("?"))) {
				JoinCondition jr = new JoinCondition();
				jr.used = false;
				jr.lexpr = slexpr;
				jr.rexpr = srexpr;
				jr.expr = expr;
				jr.jt = jointype.inner;
				jr.lefttable = slexpr.getObjectOperand().getObjectString();
				jr.righttable = srexpr.getObjectOperand().getObjectString();
				jrs.add(jr);
				// System.out.printf(
				// "join condition: %s, %s:%d, %s:%d, %s\n",
				// expr.toString( ),
				// slexpr.toString( ),
				// slexpr.getExpressionType( ),
				// srexpr.toString( ),
				// srexpr.getExpressionType( ),
				// srexpr.getObjectOperand( ).getObjectType( ) );
			} else {
				// not a join condition
			}

		}

	}

	public boolean exprVisit(TParseTreeNode pNode, boolean isLeafNode) {
		TExpression expr = (TExpression) pNode;
		// System.out.printf("expr visited: %s\n",expr.toString());
		analyzeJoinCondition(expr, expr);
		return true;

	}
}
