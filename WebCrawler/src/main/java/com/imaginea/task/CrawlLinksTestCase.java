package com.imaginea.task;

import java.io.File;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Santosh
 *
 */

public class CrawlLinksTestCase {

	private static CrawlLinks link;
	private static ResourceBundle bundle = ResourceBundle.getBundle("webcrawler");
	private static String pageURL;
	private static String year;
	private static File outFile;
	private static int level;

	@Before
	public void createCrawlLinksInstance() {
		link = new CrawlLinks();
		pageURL = bundle.getString("pageURL");
		year = bundle.getString("year");
		outFile = new File(bundle.getString("filePath") + "/" + year + "/");
		level = 5;
	}

	@Test
	public void testGetWebLinks() {
		link.getWebLinks(pageURL, year, outFile);
	}

	@Test
	public void testGetPageLinks() {
		CrawlLinks.getPageLinks(pageURL, level, year, outFile);
	}

}
