package org.docshare.yangmvc.mvc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.orm.LasyList;
import org.docshare.yangmvc.orm.Model;

import com.alibaba.fastjson.JSON;

public class TestRawSQL {
    private static final Logger logger = LoggerFactory.getLogger(TestRawSQL.class);

    public static void user_growth_monthly() {
        try {
            logger.info("获取用户月度增长数据");

            // 使用原生SQL查询最近12个月的用户增长数据
            String sql = "SELECT DATE_FORMAT(addtm, '%Y-%m') as month, COUNT(*) as count " +
                        "FROM usr " +
                        "WHERE addtm >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                        "GROUP BY DATE_FORMAT(addtm, '%Y-%m') " +
                        "ORDER BY month ASC";
            logger.info("SQL="+sql);

            LasyList dbResults = LasyList.fromRawSql(sql, new Object[]{});
            List<Model> dbResultsList = dbResults.toArrayList();
            logger.info("result = \n"+JSON.toJSONString(dbResultsList,true));

        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static void main(String[] args) {
        Config.loadProperties("/webA.properties");
        logger.info(Config.str());
        user_growth_monthly();


    }
}
