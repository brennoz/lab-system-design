package com.lab.webcrawler.domain.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

// Algorithm: HTML link extraction — parses <a href> elements and resolves relative URLs
// Why Jsoup: purpose-built HTML parser; handles malformed real-world HTML without throwing
public class LinkExtractor {

    public List<String> extract(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);
        return doc.select("a[href]")
                .stream()
                // absUrl resolves relative href against baseUrl automatically
                .map(element -> element.absUrl("href"))
                .filter(url -> url.startsWith("http://") || url.startsWith("https://"))
                .distinct()
                .toList();
    }
}
