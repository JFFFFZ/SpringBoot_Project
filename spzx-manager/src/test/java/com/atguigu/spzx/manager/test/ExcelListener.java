package com.atguigu.spzx.manager.test;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExcelListener<T> extends AnalysisEventListener<T> {

    private List<T> data = new ArrayList<>();
    // 读取excel中的内容
    // 从第二行开始读取（认为第一行为表头信息）  把每行读取的内容封装到t对象里面去
    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        data.add(t);
    }

    public List<T> getData() {
        return data;
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
