package ac.ict.debs.qmapper.test;

public class Queries {
	public static String update1 = "UPDATE xyz_temp_td te     SET    te.hfsj = NULL     WHERE  te.hfsj > te.fxsj ";
	public static String delete1 = "DELETE xyz_temp_td te    WHERE  te.hfsj IS NOT NULL AND te.fxsj <= 1";
	public static String delete_exists1 = "DELETE\r\n"
			+ "    FROM   xyz_temp_td t\r\n"
			+ "    WHERE  EXISTS (SELECT 1    \r\n"
			+ "        FROM   xyz_tj_td\r\n"
			+ "        WHERE  tdsj > t.rqsj\r\n"
			+ "        AND zdjh =t.zdjh      \r\n" + "AND hfsj IS  NULL)\r\n"
			+ "        AND gjbm <>'0167'\r\n" + "";
	public static String delete_exists2 = "DELETE\r\n"
			+ "    FROM   xyz_temp_td t\r\n"
			+ "    WHERE  EXISTS (SELECT 1    \r\n"
			+ "        FROM   xyz_tj_td\r\n"
			+ "        WHERE  tdsj > t.rqsj\r\n"
			+ "        AND zdjh =t.zdjh     \r\n"
			+ "AND hfsj IS  NULL) AND EXISTS (SELECT 1    \r\n"
			+ "        FROM   xyz_tj_td\r\n"
			+ "        WHERE  tdsj > t.rqsj\r\n"
			+ "        AND zdjh =t.zdjh     \r\n" + "AND hfsj IS  NULL)";
	public static String delete_not_exists = "DELETE xyz_temp_td t\r\n"
			+ "WHERE  NOT EXISTS (SELECT 1\r\n"
			+ "            FROM   xyz_zc_zdzc\r\n"
			+ "            WHERE  hh IS NOT NULL\r\n"
			+ "                   AND zdjh = t.zdjh)\r\n" + "";
	public static String insert = " INSERT INTO xyz_temp_gk\r\n"
			+ " (qym, zdjh, hfsj, fssj, zdyt)\r\n"
			+ " SELECT DISTINCT qym, zdjh, hfsj, MAX(rqsj) AS fssj, zdyt\r\n"
			+ " FROM   xyz_temp_td\r\n" + " WHERE  gjbm = '0167'\r\n"
			+ " GROUP  BY qym, zdjh, hfsj, zdyt";
}
