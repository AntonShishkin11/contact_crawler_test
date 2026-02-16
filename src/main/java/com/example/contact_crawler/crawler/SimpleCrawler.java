package com.example.contact_crawler.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Duration;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleCrawler {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\+?[0-9][0-9\\-() ]{7,}");

    public CrawlResult crawl(String url) throws IOException {
        Document doc = Jsoup
                .connect(url)
                .timeout(5000)                 // жёстко 5 секунд
                .ignoreHttpErrors(true)        // не падать на 404/500
                .ignoreContentType(true)       // если не text/html
                .userAgent("Mozilla/5.0")      // меньше блокируют
                .get();

        Set<String> links = new HashSet<>();
        Set<String> emails = new HashSet<>();
        Set<String> phones = new HashSet<>();

        for (Element link : doc.select("a[href]")) {
            links.add(link.absUrl("href"));
        }

        Matcher emailMatcher = EMAIL_PATTERN.matcher(doc.text());
        while (emailMatcher.find()) {
            emails.add(emailMatcher.group());
        }

        Matcher phoneMatcher = PHONE_PATTERN.matcher(doc.text());
        while (phoneMatcher.find()) {
            phones.add(phoneMatcher.group());
        }

        return new CrawlResult(links, emails, phones);
    }
}
