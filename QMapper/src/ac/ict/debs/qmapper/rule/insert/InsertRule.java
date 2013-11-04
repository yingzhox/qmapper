package ac.ict.debs.qmapper.rule.insert;

import java.util.ArrayList;

import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import ac.ict.debs.qmapper.rule.IRule;
import ac.ict.debs.qmapper.util.ColumnResolver;
import ac.ict.debs.qmapper.util.ColumnResolver.Column;

public class InsertRule extends IRule {

	public InsertRule(TCustomSqlStatement origin) {
		super(origin);
	}

	@Override
	public TSelectSqlStatement apply(TCustomSqlStatement origin,
			TSelectSqlStatement dest) {
		TInsertSqlStatement insert = (TInsertSqlStatement) origin;
		// 可以直接复制select部分
		dest = (TSelectSqlStatement) this.parser(insert.getSubQuery()
				.toString());
		// 对select部分做添加列
		ArrayList<Column> table = ColumnResolver.getTable(insert
				.getTargetTable().getFullName());

		if (insert.getColumnList() != null && insert.getColumnList().size() > 0) {
			// 一个一个补充
			String selectList = null;
			for (int i = 0; i < table.size(); i++) {
				Column current = table.get(i);
				String currentItem = null;
				boolean flag = false;
				// 查看是否存在于ColumnList中
				for (int j = 0; j < insert.getColumnList().size(); j++) {
					TObjectName objectName = insert.getColumnList()
							.getObjectName(j);
					// 看看列明是否一样
//					System.out.println(objectName.getColumnNameOnly()
//							+ "-compare-" + current.columnName);
					if (objectName.getColumnNameOnly().toLowerCase().trim()
							.equals(current.columnName.toLowerCase().trim())) {
						// 如果一样，则补充已有选择
						currentItem = insert.getSubQuery()
								.getResultColumnList().getResultColumn(j)
								.toString();
						flag = true;
						break;
					}
				}
				if (!flag) {
					// 用默认填充
					currentItem = current.toString();
				}
				if (selectList == null)
					selectList = currentItem;
				else
					selectList += "," + currentItem;
			}
			dest.getResultColumnList().setString(selectList);
		}

		return dest;
	}
}
