package ac.ict.debs.qmapper.rule;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import ac.ict.debs.qmapper.exception.TableNotFoundException;
import ac.ict.debs.qmapper.rule.delete.DeleteRule;
import ac.ict.debs.qmapper.rule.insert.InsertRule;
import ac.ict.debs.qmapper.rule.update.UpdateRule;
import ac.ict.debs.qmapper.util.ColumnResolver;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.stmt.TDeleteSqlStatement;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import gudusoft.gsqlparser.stmt.TUpdateSqlStatement;

public class RuleChain {
	private ArrayList<IRule> rules = new ArrayList<IRule>();
	private TGSqlParser parser;
	private TCustomSqlStatement root;
	private ColumnResolver resolver;

	public RuleChain(ColumnResolver resolver) {
		// here prepare the rule
		this.resolver = resolver;
	}

	final static Logger LOG = Logger.getLogger(RuleChain.class);
	EDbVendor vendor = EDbVendor.dbvoracle;

	public String transform(String query) throws TableNotFoundException {
		parser = new TGSqlParser(vendor);
		parser.sqltext = query;
		int ErrorNo = parser.parse();
		if (ErrorNo != 0) {
			String errorMessage = parser.getErrormessage();
			LOG.error("SQL format exception:" + errorMessage);
			System.exit(1);
		}
		root = parser.sqlstatements.get(0);
		this.prepareRule(root);
		return this.applyRule();
	}

	private void prepareRule(TCustomSqlStatement root) {
		System.out.println("before processing:" + root);
		if (root instanceof TUpdateSqlStatement) {
			UpdateRule rule = new UpdateRule(root, this.resolver);
			rules.add(rule);
		} else if (root instanceof TDeleteSqlStatement) {
			DeleteRule rule = new DeleteRule(root, this.resolver);
			rules.add(rule);
		} else if (root instanceof TInsertSqlStatement) {
			InsertRule rule = new InsertRule(root, this.resolver);
			rules.add(rule);
		}
	}

	private String applyRule() throws TableNotFoundException {
		for (IRule rule : rules) {
			TSelectSqlStatement output = new TSelectSqlStatement(vendor);
			output = rule.apply(root, output);
			String finalOut = "";
			if (rule instanceof InsertRule) {
				// 不OVER WRITE
				String table = "INSERT INTO TABLE "
						+ rule.getOrigin().getTargetTable().getFullName();
				finalOut = table + "\n" + output;
			} else {
				// OVER WRITE
				String table = "INSERT OVERWRITE TABLE "
						+ rule.getOrigin().getTargetTable().getFullName();
				finalOut = table + "\n" + output;
			}
			System.out.println("\nAfter processing:\n" + finalOut);
			return finalOut;
		}
		return null;
	}
}
