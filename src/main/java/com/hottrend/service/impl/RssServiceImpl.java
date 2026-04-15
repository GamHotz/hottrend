package com.hottrend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hottrend.mapper.RssItemMapper;
import com.hottrend.mapper.RssSourceMapper;
import com.hottrend.model.RssItem;
import com.hottrend.model.RssSource;
import com.hottrend.service.IRssService;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RSS 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RssServiceImpl implements IRssService {

    private final RssSourceMapper rssSourceMapper;
    private final RssItemMapper rssItemMapper;

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    // 预编译常用正则，提升性能
    private static final Pattern PATTERN_DOCTYPE = Pattern.compile("(?si)<!DOCTYPE[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_XML_DECLARATION = Pattern.compile("<\\?xml[^>]*\\?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_CDATA_START = Pattern.compile("<!\\[CDATA\\[", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_CDATA_END = Pattern.compile("\\]\\]>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_SELF_CLOSING_LINK = Pattern.compile("<link([^>]*)/>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_UNCLOSED_LINK = Pattern.compile("<link([^>]*)>(?!.*</link>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PATTERN_ILLEGAL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
    private static final Pattern PATTERN_BOM = Pattern.compile("\uFEFF");

    // ===== RSS 源操作 =====

    @Override
    public List<RssSource> listAllSources() {
        return rssSourceMapper.selectList(null);
    }

    @Override
    public List<RssSource> listEnabledSources() {
        QueryWrapper<RssSource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true);
        return rssSourceMapper.selectList(queryWrapper);
    }

    @Override
    public RssSource getSourceById(String id) {
        return rssSourceMapper.selectById(id);
    }

    @Override
    public RssSource getSourceByUrl(String url) {
        QueryWrapper<RssSource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("feed_url", url);
        return rssSourceMapper.selectOne(queryWrapper);
    }

    @Override
    public RssSource addSource(String name, String url, String groupName) {
        // 检查是否已存在
        RssSource existing = getSourceByUrl(url);
        if (existing != null) {
            throw new IllegalArgumentException("RSS 源已存在: " + url);
        }

        RssSource source = RssSource.builder()
                .name(name)
                .feedUrl(url)
                .groupName(groupName)
                .isActive(true)
                .build();

        rssSourceMapper.insert(source);
        return source;
    }

    @Override
    public boolean updateSource(RssSource source) {
        return rssSourceMapper.updateById(source) > 0;
    }

    @Override
    public boolean deleteSource(String id) {
        return rssSourceMapper.deleteById(id) > 0;
    }

    // ===== RSS 文章操作 =====

    @Override
    public List<RssItem> listRssItems(String feedId, int page, int pageSize) {
        Page<RssItem> pageParam = new Page<>(page, pageSize);
        QueryWrapper<RssItem> queryWrapper = new QueryWrapper<>();

        if (feedId != null) {
            queryWrapper.eq("feed_id", feedId);
        }

        queryWrapper.orderByDesc("first_crawl_time");

        IPage<RssItem> result = rssItemMapper.selectPage(pageParam, queryWrapper);
        return result.getRecords();
    }

    @Override
    public List<RssItem> getUnpushedItems() {
        return rssItemMapper.findByIsPushedFalseOrderByPublishTimeDesc();
    }

    @Override
    public RssItem getRssItemByUrl(String url) {
        return rssItemMapper.findByUrl(url);
    }

    @Override
    public List<RssItem> getRssItemsBySourceId(String sourceId) {
        return rssItemMapper.findBySourceId(sourceId);
    }

    // ===== 抓取操作 =====

    @Override
    public List<RssItem> fetchAllRss() {
        List<RssSource> sources = listEnabledSources();
        List<RssItem> allItems = new ArrayList<>();

        for (RssSource source : sources) {
            try {
                List<RssItem> items = fetchRssSource(source);
                allItems.addAll(items);

                // 更新最后抓取时间
                source.setLastFetchTime(LocalDateTime.now());
                rssSourceMapper.updateById(source);
            } catch (Exception e) {
                log.error("抓取 RSS 源 {} 失败: {}", source.getName(), e.getMessage());
            }
        }

        log.info("RSS 抓取完成，共获取 {} 篇文章", allItems.size());
        return allItems;
    }

    @Override
    public List<RssItem> fetchRssSource(RssSource source) throws Exception {
        List<RssItem> items = new ArrayList<>();

        if (source == null || source.getFeedUrl() == null || source.getFeedUrl().isBlank()) {
            log.warn("RSS 源配置无效: {}", source != null ? source.getId() : "null");
            return items;
        }

        try {
            // 🔥 核心：使用三重保护机制解析 RSS
            SyndFeed feed = parseFeedSafely(source.getFeedUrl());

            if (feed == null || feed.getEntries() == null) {
                log.warn("RSS 源 {} 没有获取到有效内容", source.getName());
                return items;
            }

            LocalDateTime cutoffTime = calculateCutoffTime(source.getMaxAgeDays());

            for (SyndEntry entry : feed.getEntries()) {
                try {
                    if (!isValidEntry(entry)) continue;
                    String articleUrl = entry.getLink();
                    if (isDuplicate(articleUrl)) continue;
                    if (isExpired(entry, cutoffTime)) continue;

                    RssItem item = buildRssItem(entry, source);
                    rssItemMapper.insert(item);
                    items.add(item);
                } catch (Exception e) {
                    log.warn("解析 RSS 条目失败: {}", e.getMessage());
                }
            }

            log.info("抓取 RSS 源 {} 完成，获取 {} 篇新文章", source.getName(), items.size());
        } catch (Exception e) {
            log.error("抓取 RSS 源 {} 失败: {}", source.getName(), e.getMessage(), e);
            throw e;
        }

        return items;
    }

    /**
     * 🔥 三重保护机制解析 RSS - 最大限度容忍各种畸形 XML
     */
    private SyndFeed parseFeedSafely(String feedUrl) {
        // 尝试 1: 直接解析（干净 XML）
        try {
            return fetchAndParseFeed(feedUrl, 1);
        } catch (Exception e) {
            log.debug("方式1失败，尝试方式2: {}", e.getMessage());
        }

        // 尝试 2: 强力清洗后解析
        try {
            return fetchAndParseFeed(feedUrl, 2);
        } catch (Exception e) {
            log.debug("方式2失败，尝试方式3: {}", e.getMessage());
        }

        // 尝试 3: 激进清洗 + 多种编码尝试
        try {
            return fetchAndParseFeed(feedUrl, 3);
        } catch (Exception e) {
            log.debug("方式3失败，尝试方式4: {}", e.getMessage());
        }

        // 尝试 4: 从 HTML 页面提取 RSS 链接
        try {
            String rssUrl = extractRssLinkFromHtml(feedUrl);
            if (rssUrl != null) {
                log.info("从 HTML 页面提取到 RSS 链接: {}", rssUrl);
                return fetchAndParseFeed(rssUrl, 2);
            }
        } catch (Exception e) {
            log.debug("方式4失败: {}", e.getMessage());
        }

        log.error("四种方式全部失败: {}", feedUrl);
        return null;
    }

    /**
     * 🔥 从 HTML 页面中提取 RSS/Atom 链接
     */
    private String extractRssLinkFromHtml(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                String html = response.body().string();

                // 检查是否真的是 HTML
                if (!html.toLowerCase().contains("<html") && !html.toLowerCase().contains("<!doctype html")) {
                    return null;
                }

                // 提取 RSS/Atom 链接
                // 支持 <link rel="alternate" type="application/rss+xml" href="...">
                // 支持 <link rel="alternate" type="application/atom+xml" href="...">
                Pattern rssLinkPattern = Pattern.compile(
                        "<link[^>]+(?:rel=\"alternate\"|type=\"(?:application/rss|application/atom)\\+xml\")[^>]+href=\"([^\"]+)\"",
                        Pattern.CASE_INSENSITIVE
                );

                Matcher matcher = rssLinkPattern.matcher(html);
                if (matcher.find()) {
                    String rssUrl = matcher.group(1);
                    // 相对路径转换为绝对路径
                    if (rssUrl.startsWith("/")) {
                        java.net.URL baseUrl = new java.net.URL(url);
                        rssUrl = new java.net.URL(baseUrl, rssUrl).toString();
                    } else if (!rssUrl.startsWith("http")) {
                        java.net.URL baseUrl = new java.net.URL(url);
                        rssUrl = new java.net.URL(baseUrl, rssUrl).toString();
                    }
                    return rssUrl;
                }

                // 备用：直接搜索 RSS/Atom URL
                Pattern urlPattern = Pattern.compile("(https?://[^\"'\\s]+\\.rss|https?://[^\"'\\s]+\\.xml|https?://[^\"'\\s]+/feed/?[\"'])", Pattern.CASE_INSENSITIVE);
                matcher = urlPattern.matcher(html);
                if (matcher.find()) {
                    return matcher.group(1).replace("\"", "").replace("'", "");
                }
            }
        } catch (Exception e) {
            log.warn("提取 RSS 链接失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 根据不同模式解析 RSS
     */
    private SyndFeed fetchAndParseFeed(String feedUrl, int mode) throws Exception {
        Request request = new Request.Builder()
                .url(feedUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/xml,text/xml,application/rss+xml,application/atom+xml,text/html")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .build();

        try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP 请求失败，状态码：" + response.code());
            }

            String xml = response.body().string();

            // 根据模式进行不同程度的清洗
            xml = sanitizeXml(xml, mode);

            try (InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                 XmlReader reader = new XmlReader(stream)) {

                SyndFeedInput input = new SyndFeedInput();
                input.setAllowDoctypes(false);
                input.setXmlHealerOn(mode >= 2); // 模式2和3启用XML修复

                return input.build(reader);
            }
        }
    }

    /**
     * 🔥 分层 XML 清洗策略
     */
    private String sanitizeXml(String xml, int mode) {
        if (xml == null || xml.isBlank()) return "";

        // 模式1: 基础清洗
        if (mode >= 1) {
            xml = PATTERN_DOCTYPE.matcher(xml).replaceAll("");
            xml = PATTERN_BOM.matcher(xml).replaceAll("");
            xml = PATTERN_ILLEGAL_CHARS.matcher(xml).replaceAll("");
        }

        // 模式2: 强力清洗
        if (mode >= 2) {
            // 处理自闭合的 link 标签
            xml = PATTERN_SELF_CLOSING_LINK.matcher(xml).replaceAll("<link$1></link>");

            // 处理未闭合的 link 标签（单标签直接忽略）
            xml = fixUnclosedLinkTags(xml);

            // 移除 CDATA 部分但保留内容
            xml = stripCdataSections(xml);

            // 移除 HTML 注释
            xml = xml.replaceAll("<!--[\\s\\S]*?-->", "");

            // 移除 PHP 或其他动态内容标签
            xml = xml.replaceAll("<\\?php[\\s\\S]*?\\?>", "");

            // 修复 & 符号（必须是合法实体或 &amp;）
            xml = fixAmpersandEntities(xml);
        }

        // 模式3: 激进清洗（处理极端情况）
        if (mode >= 3) {
            // 移除所有可能的脚本和样式
            xml = xml.replaceAll("<script[\\s\\S]*?</script>", "");
            xml = xml.replaceAll("<style[\\s\\S]*?</style>", "");

            // 移除非标准命名空间
            xml = xml.replaceAll("xmlns:[a-zA-Z]+=\"[^\"]*\"", "");

            // 移除多余的空白
            xml = xml.replaceAll("\\s+", " ");
        }

        return xml.trim();
    }

    /**
     * 修复未闭合的 link 标签
     */
    private String fixUnclosedLinkTags(String xml) {
        // 匹配孤立的 <link...> 后面没有 </link> 的情况
        StringBuilder result = new StringBuilder();
        int linkStartCount = 0;
        int linkEndCount = 0;

        // 简单计数法：计算 link 开闭标签数量
        Pattern linkOpenPattern = Pattern.compile("<link\\b[^>]*>", Pattern.CASE_INSENSITIVE);
        Pattern linkClosePattern = Pattern.compile("</link>", Pattern.CASE_INSENSITIVE);

        Matcher openMatcher = linkOpenPattern.matcher(xml);
        Matcher closeMatcher = linkClosePattern.matcher(xml);

        while (openMatcher.find()) linkStartCount++;
        while (closeMatcher.find()) linkEndCount++;

        // 如果数量不匹配，说明有未闭合的标签，尝试用更激进的方式处理
        if (linkStartCount != linkEndCount) {
            // 把所有 link 标签转换为安全的格式
            xml = linkOpenPattern.matcher(xml).replaceAll("<link></link>");
        }

        return xml;
    }

    /**
     * 移除 CDATA 部分但保留内容
     */
    private String stripCdataSections(String xml) {
        // 移除 <![CDATA[ 和 ]]>
        String result = PATTERN_CDATA_START.matcher(xml).replaceAll("");
        result = PATTERN_CDATA_END.matcher(result).replaceAll("");
        return result;
    }

    /**
     * 修复 & 符号
     */
    private String fixAmpersandEntities(String xml) {
        // 修复单独的 & 后面没有分号的情况
        // 匹配 & 后面的非合法实体字符
        return xml.replaceAll("&(?!(amp|lt|gt|quot|apos|nbsp|#[0-9]+|#x[0-9a-fA-F]+);)", "&amp;");
    }

    private LocalDateTime calculateCutoffTime(Integer maxAgeDays) {
        if (maxAgeDays == null || maxAgeDays <= 0) return null;
        return LocalDateTime.now().minusDays(maxAgeDays);
    }

    private boolean isValidEntry(SyndEntry entry) {
        return entry.getTitle() != null && !entry.getTitle().isBlank()
                && entry.getLink() != null && !entry.getLink().isBlank();
    }

    private boolean isDuplicate(String url) {
        return url == null || url.isBlank() || rssItemMapper.findByUrl(url) != null;
    }

    private boolean isExpired(SyndEntry entry, LocalDateTime cutoffTime) {
        if (cutoffTime == null || entry.getPublishedDate() == null) return false;
        LocalDateTime publishTime = entry.getPublishedDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return publishTime.isBefore(cutoffTime);
    }

    private RssItem buildRssItem(SyndEntry entry, RssSource source) {
        String summary = extractSummary(entry);
        String content = extractContent(entry);
        LocalDateTime publishedAt = extractPublishedAt(entry);

        return RssItem.builder()
                .title(entry.getTitle() != null ? entry.getTitle().trim() : "")
                .url(entry.getLink() != null ? entry.getLink().trim() : "")
                .summary(summary)
                .content(content)
                .author(entry.getAuthor())
                .publishedAt(publishedAt)
                .feedId(source.getId())
                .isPushed(false)
                .firstCrawlTime(LocalDateTime.now())
                .lastCrawlTime(LocalDateTime.now())
                .crawlCount(1)
                .build();
    }

    private String extractSummary(SyndEntry entry) {
        if (entry.getDescription() != null) {
            return entry.getDescription().getValue();
        }
        return null;
    }

    private String extractContent(SyndEntry entry) {
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            return entry.getContents().get(0).getValue();
        }
        return null;
    }

    private LocalDateTime extractPublishedAt(SyndEntry entry) {
        if (entry.getPublishedDate() != null) {
            return entry.getPublishedDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        if (entry.getUpdatedDate() != null) {
            return entry.getUpdatedDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return LocalDateTime.now();
    }
}