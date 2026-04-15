package com.hottrend.crawler.platform;

import com.hottrend.model.NewsItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知乎热榜抓取器
 */
@Component
public class ZhihuCrawler extends NewsNowCrawler {

    @Override
    public String getPlatformId() {
        return "zhihu";
    }

    @Override
    public String getPlatformName() {
        return "知乎";
    }

    @Override
    public List<NewsItem> fetchTrending() throws Exception {
        return fetchFromNewsNow("zhihu");
    }
}