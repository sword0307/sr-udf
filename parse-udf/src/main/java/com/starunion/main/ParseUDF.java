package com.starunion.main;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlExprParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import java.util.Map;

public class ParseUDF {


    public static class CustomVisitor extends SQLASTVisitorAdapter {

        @Override
        public boolean visit(SQLIdentifierExpr x) {
            String columnName = x.getName();
            x.setName(columnName);
            return true;
        }

        @Override
        public boolean visit(SQLPropertyExpr x) {
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

    public  String evaluate(String param) {
        // 使用 MySQL 表达式解析器解析复杂 SQL 表达式
        MySqlExprParser exprParser = new MySqlExprParser(param);
        CustomVisitor visitor = new CustomVisitor();
        SQLExpr sqlExpr = exprParser.expr();
        sqlExpr.accept(visitor);
        return sqlExpr.toString();
    }

    public static void main(String[] args) {
        ParseUDF parseUDF = new ParseUDF();
        System.out.println(parseUDF.evaluate("lpad(concat(upper(行为), '_', a.b.c.d, '_', full_users.register_country, '_', full_users.`channel@channel_name`), 6, '*')))"));
    }
}
