package utils;

import java.util.regex.*;

public class SQLParser {
    // 简单解析 SELECT ... FROM table_name
    public static String extractTableName(String sql) {
        sql = sql.toLowerCase().replaceAll("\\s+", " ");
        Pattern pattern = Pattern.compile("from\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
