package com.hottrend.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.hottrend.model.NewsItem;
import com.hottrend.model.dto.ContentResult;
import com.hottrend.service.IContentCrawlerService;
import com.hottrend.service.INewsService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * 内容爬取服务实现类
 */
@Slf4j
@Service
public class ContentCrawlerServiceImpl implements IContentCrawlerService {

    private final WebClient webClient;
    private final INewsService newsService;

    // 请求头，模拟浏览器
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public ContentCrawlerServiceImpl(INewsService newsService) {
        this.webClient = WebClient.builder().build();
        this.newsService = newsService;
    }

    @Override
    public ContentResult getContentAsMarkdown(String url) {
        try {
            String html = fetchHtml(url);
            if (html == null || html.isEmpty()) {
                return ContentResult.fail("无法获取网页内容");
            }

            // 检查是否为JSON格式（当URL为空时返回的是JSON）
            if (url == null || url.isEmpty()) {
                String markdown = convertJsonToMarkdown(html);
                return ContentResult.builder()
                        .success(true)
                        .title("热点新闻列表")
                        .content(markdown)
                        .url("")
                        .build();
            }

            Document doc = Jsoup.parse(html, url);

            // 移除脚本和样式
            doc.select("script, style, nav, header, footer, aside, .ad, .advertisement").remove();

            // 提取标题
            String title = extractTitle(doc);

            // 提取正文内容
            extractContent(doc);

            // 提取发布时间
            String publishTime = extractPublishTime(doc);

            // 提取作者
            String author = extractAuthor(doc);

            // 转换为Markdown
            String markdown = convertToMarkdown(doc);

            return ContentResult.builder()
                    .success(true)
                    .title(title)
                    .content(markdown)
                    .publishTime(publishTime)
                    .author(author)
                    .url(url)
                    .build();

        } catch (Exception e) {
            log.error("获取内容失败: {}", e.getMessage());
            return ContentResult.fail("获取内容失败: " + e.getMessage());
        }
    }

    @Override
    public String getSummary(String url) {
        try {
            String html = fetchHtml(url);
            if (html == null || html.isEmpty()) {
                return null;
            }

            Document doc = Jsoup.parse(html, url);
            doc.select("script, style, nav, header, footer, aside, .ad, .advertisement").remove();

            // 尝试获取meta描述
            String description = doc.select("meta[name=description]").attr("content");
            if (description != null && !description.isEmpty()) {
                return description;
            }

            // 获取og描述
            description = doc.select("meta[property=og:description]").attr("content");
            if (description != null && !description.isEmpty()) {
                return description;
            }

            // 尝试获取文章第一段文字
            Elements paragraphs = doc.select("article p, .content p, .article p, main p");
            for (Element p : paragraphs) {
                String text = p.text();
                if (text.length() > 50) {
                    return text.substring(0, Math.min(200, text.length()));
                }
            }

            return null;
        } catch (Exception e) {
            log.error("获取摘要失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 抓取HTML内容
     */
    private String fetchHtml(String url) {
        try {
            // 检查URL是否有效
            if (url == null || url.isEmpty()) {
                List<NewsItem> records =
                        newsService.listNews(null, null, 1, 20).getRecords();
                return JSONUtil.toJsonStr(records);
            }

            String response = webClient.get()
                    .uri(url)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return response;
        } catch (Exception e) {
            log.error("抓取HTML失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 提取标���
     */
    private String extractTitle(Document doc) {
        // 尝试多种方式获取标题
        String title = doc.select("meta[property=og:title]").attr("content");
        if (title == null || title.isEmpty()) {
            title = doc.select("h1").first() != null ? doc.select("h1").first().text() : null;
        }
        if (title == null || title.isEmpty()) {
            title = doc.title();
        }
        return title;
    }

    /**
     * 提取正文内容（HTML）
     */
    private String extractContent(Document doc) {
        // 尝试找到文章主体
        Element article = doc.select("article").first();
        if (article == null) {
            article = doc.select(".article-content, .article, .content, .post-content, main").first();
        }
        if (article == null) {
            article = doc.body();
        }

        return article != null ? article.html() : "";
    }

    /**
     * 提取发布时间
     */
    private String extractPublishTime(Document doc) {
        // 尝试多种方式获取发布时间
        String[] selectors = {
                "meta[property=article:published_time]",
                "meta[name=publishdate]",
                "meta[name=date]",
                ".publish-time",
                ".pub-time",
                ".article-time",
                "time[datetime]"
        };

        for (String selector : selectors) {
            Element el = doc.select(selector).first();
            if (el != null) {
                String time = el.attr("content");
                if (time == null || time.isEmpty()) {
                    time = el.attr("datetime");
                }
                if (time == null || time.isEmpty()) {
                    time = el.text();
                }
                if (time != null && !time.isEmpty()) {
                    return time;
                }
            }
        }
        return null;
    }

    /**
     * 提取作者
     */
    private String extractAuthor(Document doc) {
        // 尝试多种方式获取作者
        String[] selectors = {
                "meta[name=author]",
                "meta[property=article:author]",
                ".author",
                ".author-name",
                ".article-author"
        };

        for (String selector : selectors) {
            Element el = doc.select(selector).first();
            if (el != null) {
                String author = el.attr("content");
                if (author == null || author.isEmpty()) {
                    author = el.text();
                }
                if (author != null && !author.isEmpty()) {
                    return author;
                }
            }
        }
        return null;
    }

    /**
     * 将HTML转换为Markdown
     */
    private String convertToMarkdown(Document doc) {
        StringBuilder md = new StringBuilder();

        // 获取标题
        String title = extractTitle(doc);
        if (title != null && !title.isEmpty()) {
            md.append("# ").append(title).append("\n\n");
        }

        // 获取作者
        String author = extractAuthor(doc);
        if (author != null && !author.isEmpty()) {
            md.append("*作者：").append(author).append("*\n\n");
        }

        // 获取发布时间
        String publishTime = extractPublishTime(doc);
        if (publishTime != null && !publishTime.isEmpty()) {
            md.append("*发布时间：").append(publishTime).append("*\n\n");
        }

        md.append("---\n\n");

        // 找到主要内容区域
        Element content = doc.select("article").first();
        if (content == null) {
            content = doc.select(".article-content, .article, .content, .post-content, main, .main").first();
        }
        if (content == null) {
            content = doc.body();
        }

        // 遍历子元素并转换
        if (content != null) {
            convertElements(content.children(), md);
        }

        return md.toString();
    }

    /**
     * 转换元素列表为Markdown
     */
    private void convertElements(Elements elements, StringBuilder md) {
        for (Element el : elements) {
            String tagName = el.tagName().toLowerCase();

            // 跳过不需要的元素
            if (tagName.equals("script") || tagName.equals("style") || tagName.equals("nav") ||
                    tagName.equals("header") || tagName.equals("footer") || tagName.equals("aside")) {
                continue;
            }

            switch (tagName) {
                case "h1":
                    md.append("# ").append(el.text()).append("\n\n");
                    break;
                case "h2":
                    md.append("## ").append(el.text()).append("\n\n");
                    break;
                case "h3":
                    md.append("### ").append(el.text()).append("\n\n");
                    break;
                case "h4":
                    md.append("#### ").append(el.text()).append("\n\n");
                    break;
                case "h5":
                    md.append("##### ").append(el.text()).append("\n\n");
                    break;
                case "h6":
                    md.append("###### ").append(el.text()).append("\n\n");
                    break;
                case "p":
                    String pText = el.text().trim();
                    if (!pText.isEmpty()) {
                        md.append(pText).append("\n\n");
                    }
                    break;
                case "strong":
                case "b":
                    md.append("**").append(el.text()).append("**");
                    break;
                case "em":
                case "i":
                    md.append("*").append(el.text()).append("*");
                    break;
                case "a":
                    String href = el.attr("href");
                    String linkText = el.text();
                    if (href != null && !href.isEmpty() && !linkText.isEmpty()) {
                        md.append("[").append(linkText).append("](").append(href).append(")");
                    } else {
                        md.append(linkText);
                    }
                    break;
                case "img":
                    String src = el.attr("src");
                    String alt = el.attr("alt");
                    if (src != null && !src.isEmpty()) {
                        md.append("![").append(alt != null ? alt : "").append("](").append(src).append(")\n\n");
                    }
                    break;
                case "ul":
                    convertList(el, md, "- ");
                    break;
                case "ol":
                    convertList(el, md, "1. ");
                    break;
                case "blockquote":
                    String quote = el.text().trim();
                    if (!quote.isEmpty()) {
                        md.append("> ").append(quote).append("\n\n");
                    }
                    break;
                case "pre":
                case "code":
                    String code = el.text();
                    if (!code.isEmpty()) {
                        md.append("```\n").append(code).append("\n```\n\n");
                    }
                    break;
                case "br":
                    md.append("\n");
                    break;
                case "hr":
                    md.append("---\n\n");
                    break;
                case "div":
                case "section":
                case "span":
                    // 递归处理子元素
                    convertElements(el.children(), md);
                    break;
                default:
                    // 其他元素，尝试获取文本
                    String text = el.text().trim();
                    if (!text.isEmpty()) {
                        md.append(text).append("\n\n");
                    }
            }
        }
    }

    /**
     * 转换列表
     */
    private void convertList(Element list, StringBuilder md, String prefix) {
        Elements items = list.select("> li");
        for (Element item : items) {
            String text = item.text().trim();
            if (!text.isEmpty()) {
                md.append(prefix).append(text).append("\n");
            }
        }
        md.append("\n");
    }

    /**
     * 将JSON格式的新闻数据转换为Markdown
     */
    private String convertJsonToMarkdown(String jsonStr) {
        try {
            cn.hutool.json.JSONArray jsonArray = JSONUtil.parseArray(jsonStr);
            StringBuilder md = new StringBuilder();
            
            md.append("# 热点新闻列表\n\n");
            md.append("---\n\n");
            
            for (int i = 0; i < jsonArray.size(); i++) {
                cn.hutool.json.JSONObject newsObj = jsonArray.getJSONObject(i);
                
                String title = newsObj.getStr("title", "未知标题");
                String platformName = newsObj.getStr("platformName", "未知平台");
                Integer hotRank = newsObj.getInt("hotRank");
                String url = newsObj.getStr("url", "");
                String summary = newsObj.getStr("summary", "");
                
                // 添加序号和标题
                md.append("## ").append(i + 1).append(". ").append(title).append("\n\n");
                
                // 添加平台信息
                md.append("**平台**: ").append(platformName).append("  ");
                
                // 添加热度排名
                if (hotRank != null) {
                    md.append("| **热度排名**: #").append(hotRank).append("  ");
                }
                md.append("\n\n");
                
                // 添加摘要
                if (summary != null && !summary.isEmpty()) {
                    md.append("> ").append(summary).append("\n\n");
                }
                
                // 添加链接
                if (url != null && !url.isEmpty()) {
                    md.append("[查看原文](").append(url).append(")\n\n");
                }
                
                md.append("---\n\n");
            }
            
            return md.toString();
        } catch (Exception e) {
            log.error("JSON转Markdown失败: {}", e.getMessage());
            return "# 热点新闻列表\n\n转换失败: " + e.getMessage();
        }
    }

}