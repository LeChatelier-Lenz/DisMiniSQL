package utils;

import java.util.regex.*;

public class SQLParser {

    /**
     * SQL 类型枚举
     */
    public enum SQLType {
        SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, UNKNOWN
    }

    /**
     * 封装解析结果
     */
    public static class SQLInfo {
        public SQLType type;
        public String tableName;

        public SQLInfo(SQLType type, String tableName) {
            this.type = type;
            this.tableName = tableName;
        }

        @Override
        public String toString() {
            return "类型: " + type + ", 表名: " + tableName;
        }
    }

    /**
     * 主解析方法：识别 SQL 类型并提取表名
     */
    public static SQLInfo parse(String sql) {
        sql = sql.trim().toLowerCase().replaceAll("\\s+", " ");

        if (sql.startsWith("select")) {
            return new SQLInfo(SQLType.SELECT, extractWithRegex(sql, "from\\s+([a-zA-Z_][a-zA-Z0-9_]*)"));
        } else if (sql.startsWith("insert into")) {
            return new SQLInfo(SQLType.INSERT, extractWithRegex(sql, "insert into\\s+([a-zA-Z_][a-zA-Z0-9_]*)"));
        } else if (sql.startsWith("update")) {
            return new SQLInfo(SQLType.UPDATE, extractWithRegex(sql, "update\\s+([a-zA-Z_][a-zA-Z0-9_]*)"));
        } else if (sql.startsWith("delete from")) {
            return new SQLInfo(SQLType.DELETE, extractWithRegex(sql, "delete from\\s+([a-zA-Z_][a-zA-Z0-9_]*)"));
        } else if (sql.startsWith("create table")) {
            return new SQLInfo(SQLType.CREATE, extractWithRegex(sql, "create table\\s+([a-zA-Z_][a-zA-Z0-9_]*)"));
        } else if (sql.startsWith("drop table")) {
            return new SQLInfo(SQLType.DROP, extractWithRegex(sql, "drop table\\s+([a-zA-Z_][a-zA-Z0-9_]*)"));
        }

        return new SQLInfo(SQLType.UNKNOWN, null);
    }

    /**
     * 利用正则表达式提取表名
     */
    private static String extractWithRegex(String sql, String patternStr) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
