package com.hottrend.crawler.platform;

import com.hottrend.model.NewsItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 百度热搜抓取器
 */
@Component
public class BaiduCrawler extends NewsNowCrawler {

    @Override
    public String getPlatformId() {
        return "baidu";
    }

    @Override
    public String getPlatformName() {
        return "百度";
    }

    @Override
    public List<NewsItem> fetchTrending() throws Exception {
        return fetchFromNewsNow("baidu");
    }
}