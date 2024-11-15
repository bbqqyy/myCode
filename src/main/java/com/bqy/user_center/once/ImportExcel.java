package com.bqy.user_center.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.bqy.user_center.model.domain.UserCenter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ImportExcel {
    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
//        String fileName = "D:\\myCode\\src\\main\\resources\\testExcel.xlsx";
        String fileName = "D:\\myCode\\src\\main\\resources\\prodExcel.xlsx";
//        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
//        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
//        EasyExcel.read(fileName, XingQiuTableUserInfo.class,new DemoDataListener() ).sheet().doRead();
        List<XingQiuTableUserInfo> userInfoList = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        System.out.println("总数 = "+ userInfoList.size());
        Map<String,List<XingQiuTableUserInfo>> listMap = userInfoList.stream()
                .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
        .collect(Collectors.groupingBy(XingQiuTableUserInfo::getUsername));
        System.out.println("不重复昵称："+listMap.keySet().size());
    }
}
