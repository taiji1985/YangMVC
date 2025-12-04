package org.docshare.yangmvc.mvc;

import java.util.List;

import org.docshare.yangmvc.log.Log;
import org.docshare.yangmvc.orm.LasyList;
import org.docshare.yangmvc.orm.Model;

import com.alibaba.fastjson.JSON;

public class TestRawSQL {

    public static void user_growth_monthly() {
        try {
            Log.i("获取用户月度增长数据");

            // 使用原生SQL查询最近12个月的用户增长数据
            String sql = "SELECT DATE_FORMAT(addtm, '%Y-%m') as month, COUNT(*) as count " +
                        "FROM usr " +
                        "WHERE addtm >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                        "GROUP BY DATE_FORMAT(addtm, '%Y-%m') " +
                        "ORDER BY month ASC";
            Log.i("SQL="+sql);

            LasyList dbResults = LasyList.fromRawSql(sql, new Object[]{});
            List<Model> dbResultsList = dbResults.toArrayList();
            Log.i("result = \n"+JSON.toJSONString(dbResultsList,true));


        } catch (Exception e) {
            Log.e(e);
        }
    }

    public static void main(String[] args) {
        Config.loadProperties("/webA.properties");
        Log.i(Config.str());
        user_growth_monthly();


    }
}
