package ac.ict.debs.qmapper.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Felix
 */
public class ColumnResolver {
	public static class Column {
		public String defaultValue;
		public String columnName;
		public String type;

		public String toString() {
			if (defaultValue != null) {
				if (type.toLowerCase().equals("string")) {
					return "'" + defaultValue + "' AS " + columnName;
				} else {
					return defaultValue + " AS " + columnName;
				}
			} else {
				return "null AS " + columnName;
			}
		}
	}

	final static Logger LOG = Logger.getLogger(ColumnResolver.class);

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void initialize(String file) throws FileNotFoundException,
			IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		Set<Entry<Object, Object>> entrySet = prop.entrySet();
		for (Entry<Object, Object> ent : entrySet) {
			addDesc(ent.getKey().toString(), ent.getValue().toString());
		}
		LOG.info("Initialized table num:" + map.size());
	}

	public static ArrayList<Column> getTable(String name) {
		if (map.containsKey(name)) {
			return map.get(name);
		}
		LOG.error("Not find table:" + name + " in desc!");
		return null;
	}

	private static HashMap<String, ArrayList<Column>> map = new HashMap<String, ArrayList<Column>>();

	public static void addDesc(String tableName, String columns) {
		String[] split2 = columns.split(",");
		ArrayList<Column> arr = new ArrayList<Column>();
		map.put(tableName, arr);
		for (String column : split2) {
			String[] split = column.split("\\|");
			Column col = new Column();
			col.columnName = split[0];
			if (split.length == 2) {
				col.type = split[1];
			} else if (split.length == 3) {
				col.defaultValue = split[2];
			}
			arr.add(col);
		}
	}
}
