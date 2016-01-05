package com.imaginea.task;

import java.io.File;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * @author Santosh
 *
 */

public class ExecutableClass {

	private static Logger LOG = Logger.getLogger(ExecutableClass.class);

	private static ResourceBundle bundle = ResourceBundle.getBundle("webcrawler");

	public static void main(String[] args) {
		try {
			String pageURL = bundle.getString("pageURL");
			String year = bundle.getString("year");
			File outFile = new File(bundle.getString("filePath") + "/" + year + "/");

			LOG.info("--Given Page URL: " + pageURL + " and year to fetch mails is: " + year+" --");
			CrawlLinks link = new CrawlLinks();
			LOG.info("--Getting links by calling the method getWebLinks()--");
			link.getWebLinks(pageURL, year, outFile);
		} catch (Exception e) {
			LOG.error("Excpetion in Main Class is:: " + e.getMessage());
		}
	}
}