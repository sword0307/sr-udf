package com.starunion.main;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlExprParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import java.util.HashMap;
import java.util.Map;

public class RewriteUDF {
    public static final String EventTableName = "events";
    public static final String FullUserTableName = "full_users";


    public static class CustomVisitor extends SQLASTVisitorAdapter {
        private final Map<String, String> fieldToTableMap;
        public final String fillTableName;

        public CustomVisitor(String fillTableName, Map<String, String> fieldToTableMap) {
            this.fieldToTableMap = fieldToTableMap;
            this.fillTableName = fillTableName;
        }

        @Override
        public boolean visit(SQLIdentifierExpr x) {
            String columnName = x.getName();
            String tableName = fieldToTableMap.get(x.getName());
            if (null != tableName) {
                x.setName(tableName + "." + columnName);
            } else {
                x.setName(fillTableName + "." + columnName);
            }
            return true;
        }

        @Override
        public boolean visit(SQLPropertyExpr x) {
            String tableName = fieldToTableMap.get(x.getName());
            if (null != tableName) {
                x.setOwner(tableName);
                return false;
            }
            return false; // Continue to visit child nodes if necessary
        }

        @Override
        public boolean visit(SQLCastExpr x) {
            // 递归处理 CAST 函数内的表达式
            x.getExpr().accept(this);
            return true;
        }

        @Override
        public boolean visit(SQLMethodInvokeExpr x) {
            // 递归处理函数调用内的所有参数
            for (SQLExpr arg : x.getArguments()) {
                arg.accept(this);
            }
            return false;
        }
    }

    public String evaluate(String param, Integer category, String extra) {
        // 使用 MySQL 表达式解析器解析复杂 SQL 表达式
        MySqlExprParser exprParser = new MySqlExprParser(param);
        SQLExpr sqlExpr = exprParser.expr();
        String tableName;
        if (category == 1) {
            tableName = EventTableName;
        } else {
            tableName = FullUserTableName;
        }

        Map<String, String> fieldMap = new HashMap<>();
        if (null != extra && !extra.isEmpty()) {
            String[] arrays = extra.split(",");
            for (String arr : arrays) {
                String[] splits = arr.split("\\|");
                if (splits.length == 2) {
                    fieldMap.put(splits[0], splits[1]);
                    fieldMap.put("`"+splits[0]+"`", splits[1]);
                }else {
                    return param;
                }
            }
        }
        CustomVisitor visitor = new CustomVisitor(tableName, fieldMap);
        // 打印解析后的表达式树
        sqlExpr.accept(visitor);
        return sqlExpr.toString();
    }
//
//    public static void main(String[] args) {
//        System.out.println(evaluate("1,4,6,7", 9));
//    }
}
