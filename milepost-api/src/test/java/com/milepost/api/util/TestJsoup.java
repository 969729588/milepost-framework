package com.milepost.api.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestJsoup {

	@Test
	public void testClean() {
		String unsafe = "<p><a href='http://example.com/' onclick='stealCookies()'>Link</a></p>";
		String safe = Jsoup.clean(unsafe, Whitelist.basic());
		System.out.println(safe);
		// now: <p><a href="http://example.com/" rel="nofollow">Link</a></p>
	}

	@Test
	public void testListLinks() throws IOException {
		String url = "http://10.25.0.59:8080/xc-economy-console/index";
		print("Fetching %s...", url);

		Document doc = Jsoup.connect(url).get();
		Elements links = doc.select("a[href]");
		Elements media = doc.select("[src]");
		Elements imports = doc.select("link[href]");

		print("\nMedia: (%d)", media.size());
		for (Element src : media) {
			if (src.tagName().equals("img"))
				print(" * %s: <%s> %sx%s (%s)", src.tagName(), src.attr("abs:src"), src.attr("width"),
						src.attr("height"), trim(src.attr("alt"), 20));
			else
				print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
		}

		print("\nImports: (%d)", imports.size());
		for (Element link : imports) {
			print(" * %s <%s> (%s)", link.tagName(), link.attr("abs:href"), link.attr("rel"));
		}

		print("\nLinks: (%d)", links.size());
		for (Element link : links) {
			print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
		}
	}

	private void print(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	private String trim(String s, int width) {
		if (s.length() > width)
			return s.substring(0, width - 1) + ".";
		else
			return s;
	}

	@Test
	public void test4_1() throws IOException {
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("username", "admin");
		paramsMap.put("pwd", "123456");
		Document doc = Jsoup.connect("http://localhost:8080/AutoODN/login").data(paramsMap).post();
		System.out.println(doc.html());
	}

	@Test
	public void test4() throws IOException {
		// 可以使用静态 Jsoup.parse(File in, String charsetName, String baseUri)
		// 方法从一个本地文件中加载文档
		Document doc = Jsoup.connect("http://10.25.0.59:8080/xc-economy-console/index").get();
		String title = doc.title();
		System.out.println(doc.html());
		System.out.println(title);
	}

	@Test
	public void test3() {
		String html = "<div><p>Lorem ipsum.</p><script>alert(1);</script>";
		Document doc = Jsoup.parseBodyFragment(html);
		Element body = doc.body();
		System.out.println(body.html());

		Whitelist whitelist = new Whitelist();
		whitelist.addTags("script");
		whitelist.addTags("p");
		System.out.println(Jsoup.clean(body.html(), whitelist));
	}

	@Test
	public void test2() {
		String html = "<html><head><title>First parse</title></head>" + "<body><p>Parsed HTML into a doc.</p>"
				+ "<img src='bd_logo1.png'></img></body></html>";
		Document doc = Jsoup.parse(html, "https://www.baidu.com/img/");
		System.out.println(doc.baseUri());
	}

	@Test
	public void test1() {
		String html = "<html><head><title>First parse</title></head>"
				+ "<body><p>Parsed HTML into a doc.</p></body></html>";
		Document doc = Jsoup.parse(html);
		System.out.println(doc.html());
		System.out.println(doc.normalise().html());
		System.out.println(doc.location());
		System.out.println(doc.head().nodeName());
		System.out.println(doc.outerHtml());
	}
}
