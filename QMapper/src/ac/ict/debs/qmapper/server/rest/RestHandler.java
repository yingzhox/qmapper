package ac.ict.debs.qmapper.server.rest;

import java.io.FileNotFoundException;
import java.io.IOException;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.pp.output.OutputConfig;
import gudusoft.gsqlparser.pp.output.OutputConfigFactory;
import gudusoft.gsqlparser.pp.para.GFmtOpt;
import gudusoft.gsqlparser.pp.para.GFmtOptFactory;
import gudusoft.gsqlparser.pp.para.GOutputFmt;
import gudusoft.gsqlparser.pp.stmtformatter.FormatterFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ac.ict.debs.qmapper.exception.TableNotFoundException;
import ac.ict.debs.qmapper.rule.RuleChain;
import ac.ict.debs.qmapper.server.Result;
import ac.ict.debs.qmapper.test.Queries;
import ac.ict.debs.qmapper.util.ColumnResolver;

@Path("/rest")
public class RestHandler {
	@GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Result getIt() {
		return new Result();
	}

	@POST
	@Path("/translate")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Result translate(@FormParam("sql") String sql,
			@FormParam("meta") String meta) {
		Result rs = new Result();
		ColumnResolver initializeWithFile;
		System.out.println("meta:" + meta);
		try {
			initializeWithFile = ColumnResolver.initializeWithString(meta);
			RuleChain chain = new RuleChain(initializeWithFile);
			String transform = chain.transform(sql);
			// format
			Result format = this.format("html", transform);
			if (format.isResult()) {
				rs.setResult(true);
				rs.setContent(format.getContent());
			} else {
				format.setContent("Result:" + transform + " Error:"
						+ format.getContent());
				return format;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			rs.setResult(false);
			rs.setContent(e.getMessage());
		} catch (TableNotFoundException e) {
			e.printStackTrace();
			rs.setResult(false);
			rs.setContent(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rs.setResult(false);
			rs.setContent(e.getMessage());
		}
		return rs;
	}

	@POST
	@Path("/format/{type}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Result format(@PathParam("type") String type,
			@FormParam("sql") String sql) {
		Result rs = new Result();
		TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvoracle);
		boolean flag = false;
		if (sql.contains("OVERWRITE")) {
			flag = true;
			sql = sql.replace("OVERWRITE", "INTO");
		}
		sqlparser.sqltext = sql;
		GFmtOpt option = GFmtOptFactory.newInstance();
		// umcomment next line generate formatted sql in html
		// option.outputFmt = GOutputFmt.ofhtml;
		int parse = sqlparser.parse();
		if (parse != 0) {
			rs.setResult(false);
			rs.setContent(sqlparser.getErrormessage());
			return rs;
		}
		if (type.toLowerCase().equals("html")) {
			// System.out.println("HTML type!");
			option.outputFmt = GOutputFmt.ofhtml;
		} else if (type.toLowerCase().equals("text")) {
			// System.out.println("Text type!");
			option.outputFmt = GOutputFmt.ofSql;
			// option.outputFmt = GOutputFmt.ofSql;
		} else {
			rs.setResult(false);
			rs.setContent("can not recognize format type:" + type);
			return rs;
		}
		OutputConfig outputConfig = OutputConfigFactory.getOutputConfig(option,
				sqlparser.getDbVendor());
		FormatterFactory.setOutputConfig(outputConfig);
		String result = FormatterFactory.pp(sqlparser, option);
		if(flag){
			result=result.replace("INTO", "OVERWRITE");
		}
		rs.setResult(true);
		rs.setContent(result);
		return rs;
	}
}
