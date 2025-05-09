import utils.SQLParser;// 测试主函数

public class test_SQLParser{
    public static void main(String[] args) {
        String[] testSQLs = {
                "SELECT * FROM users WHERE age > 18;",
                "INSERT INTO orders (id, price) VALUES (1, 100);",
                "UPDATE product SET price = 200 WHERE id = 2;",
                "DELETE FROM history WHERE time < '2024-01-01';",
                "CREATE TABLE student (id INT, name VARCHAR(20));",
                "DROP TABLE temp_data;",
                "TRUNCATE TABLE invalid_table;"
        };

        for (String sql : testSQLs) {
            SQLParser.SQLInfo info = SQLParser.parse(sql);
            System.out.println("SQL: " + sql);
            System.out.println("解析结果 → " + info);
            System.out.println("----");
        }
    }
}
