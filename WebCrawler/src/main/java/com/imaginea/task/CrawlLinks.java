package com.imaginea.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Santosh
 *
 */

@SuppressWarnings("unused")
public class CrawlLinks {

	private static Logger LOG = Logger.getLogger(CrawlLinks.class);

	private static Set<String> arrLink = new HashSet<String>();
	private static Hashtable<String, String> docs = new Hashtable<String, String>();
	private static String baseURL;
	private static int pdfCnt = 0;
	private static int htmlCnt = 0;
	private static int repCnt = 0;

	private static Hashtable<String, Integer> linkLevels = new Hashtable<String, Integer>();
	private static Hashtable<String, String> parentChild = new Hashtable<String, String>();

	public void getWebLinks(String pageURL, String year, File outFile) {
		LOG.info("--getWebLinks() called--");
		try {
			int level = 1;
			String temp = "";
			temp = pageURL.substring(pageURL.indexOf("//"));
			int start = temp.indexOf("//") + 2;
			int end = temp.indexOf("/");
			if (end > 0) {
				baseURL = temp.substring(start, end);
			} else {
				temp = temp.substring(temp.indexOf("//") + 2);
				end = temp.indexOf("/");
				if (end > 0) {
					baseURL = temp.substring(0, end);
				} else {
					baseURL = temp;
				}
			}
			LOG.debug("--Connecting to the Page URL: " + pageURL + " via JSOUP --");
			Document doc = Jsoup.connect(pageURL).get();
			LOG.debug("--Fetched HTML code is: " + doc.html() + " --");
			Elements links = doc.select("a[href]");
			level++;
			for (Element link : links) {
				String lnk = link.attr("abs:href");
				LOG.debug("--Selected href link is: " + lnk + " --");
				if (lnk.indexOf(baseURL) >= 0) {
					String lnkChk = "";
					if (lnk.indexOf("#") > 0) {
						lnkChk = lnk.substring(0, lnk.indexOf("#"));
					} else {
						lnkChk = lnk;
					}
					String path = lnk.substring((baseURL.length() + (lnk.indexOf(baseURL))));
					String extension = "";
					String fileName = "";
					LOG.debug("--Path to be crawled is: " + path + " --");
					if (path != null) {
						if (path.lastIndexOf('.') > 0) {
							extension = path.substring(path.lastIndexOf('.') + 1);
							if (extension.indexOf("#") > 0) {
								extension = extension.substring(0, extension.indexOf("#"));
							}
						}
						if (path.lastIndexOf('/') >= 0 && path.indexOf('.') > 0) {
							fileName = path;
							path = path.substring(0, path.lastIndexOf('/'));
							path = baseURL + path;
						}
						if (extension.equalsIgnoreCase("htm") || extension.equalsIgnoreCase("html")
								|| extension.equalsIgnoreCase("aspx") || extension.equalsIgnoreCase("jsp")) {
							// html2xml.writeToXML(lnk, path, fileName);
							htmlCnt++;
						} else {
							pdfCnt++;
							if (extension.equalsIgnoreCase("pdf") || extension.equalsIgnoreCase("doc")
									|| extension.equalsIgnoreCase("docx") || extension.equalsIgnoreCase("xls")
									|| extension.equalsIgnoreCase("ppt") || extension.equalsIgnoreCase("xlsx")
									|| extension.equalsIgnoreCase("pptx")) {
								docs.put(lnkChk, pageURL);
								arrLink.add(lnkChk);
								parentChild.put(lnkChk, pageURL);
								linkLevels.put(lnkChk, new Integer(level));
								continue;
							}
						}

						arrLink.add(lnkChk);
						if (linkLevels.get(lnkChk) != null) {
							int cLevel = Integer.parseInt(linkLevels.get(new String(lnkChk)).toString());
							if (level < cLevel) {
								linkLevels.put(lnkChk, new Integer(level));
								parentChild.put(lnkChk, pageURL);
								LOG.info("--Calling getPageLinks()--");
								getPageLinks(lnkChk, level, year, outFile);
							}
						} else {
							linkLevels.put(lnkChk, new Integer(level));
							parentChild.put(lnkChk, pageURL);
							LOG.info("--Calling getPageLinks()--");
							getPageLinks(lnkChk, level, year, outFile);
						}
					}
				}
			}
			level--;
			linkLevels.put(pageURL, new Integer(level));
			arrLink.clear();
			docs.clear();
		} catch (Exception e) {
			LOG.error("Excpetion Occurred is: " + e.getMessage());
		}
	}

	public static void getPageLinks(String pageURL, int level, String year, File outFile) {
		LOG.info("--getPageLinks() Called--");
		try {
			level++;
			LOG.debug("--Connecting to the Page Link: " + pageURL + " via JSOUP with Year " + year
					+ " and the Path in which the Mails are downloading is: " + outFile.getPath() + " --");
			Document doc = Jsoup.connect(pageURL).get();
			Elements links = doc.select("a[href]");
			outerLoop: for (Element link : links) {
				LOG.info("--In the Outer Loop of getPageLinks()--");
				String lnk = link.attr("abs:href");
				if (lnk.indexOf(baseURL) >= 0) {
					String lnkChk = "";
					if (lnk.indexOf("#") > 0) {
						lnkChk = lnk.substring(0, lnk.indexOf("#"));
					} else {
						lnkChk = lnk;
					}
					if (arrLink.contains(new String(lnkChk))) {
						if (linkLevels.get(lnkChk) != null) {
							int curLevel = Integer.parseInt(linkLevels.get(new String(lnkChk)).toString());
							if (level < curLevel) {
								linkLevels.put(lnkChk, new Integer(level));
								String extension = "";
								if (lnkChk.lastIndexOf('.') > 0) {
									extension = lnkChk.substring(lnkChk.lastIndexOf('.') + 1);
									if (extension.indexOf("#") > 0) {
										extension = extension.substring(0, extension.indexOf("#"));
									}
								}
								if (extension.equalsIgnoreCase("htm") || extension.equalsIgnoreCase("html")
										|| extension.equalsIgnoreCase("aspx") || extension.equalsIgnoreCase("jsp")) {
									parentChild.put(lnkChk, pageURL);
									LOG.info("Recurrence of getPageLinks() occurred");
									getPageLinks(lnkChk, level, year, outFile);
								}
							}
						} else {
							parentChild.put(lnkChk, pageURL);
							linkLevels.put(lnkChk, new Integer(level));
						}

						continue;
					} else {
						String path = lnk.substring((baseURL.length() + (lnk.indexOf(baseURL))));
						String extension = "";
						String fileName = "";
						if (path != null) {
							if (path.indexOf('.') > 0) {
								extension = path.substring(path.lastIndexOf('.') + 1);
								if (extension.indexOf("#") > 0) {
									extension = extension.substring(0, extension.indexOf("#"));
								}
							}
							if (path.lastIndexOf('/') >= 0 && path.indexOf('.') > 0) {
								fileName = path;
								path = path.substring(0, path.lastIndexOf('/'));
								path = baseURL + path;

								String[] slashSplit = path.split(".mbox");

								String yr = slashSplit[1].substring(slashSplit[1].length() - 6,
										slashSplit[1].length() - 2);
								int urlYear = Integer.parseInt(yr.substring(0, 4));

								if (path.contains("raw/") && path.contains(year)) {
									LOG.debug("Raw Path to be crawled is: " + path);
									Document rawDoc = Jsoup.connect("http://" + path).get();
									LOG.info("=====================================");
									String msgID = path.split("%")[1];
									LOG.debug("Message ID of the Mail is: " + msgID);
									File temp = new File(outFile.getPath() + "/" + slashSplit[1]
											.substring(slashSplit[1].length() - 2, slashSplit[1].length()) + "/");
									if (!temp.exists())
										temp.mkdirs();
									File finalOutFile = new File(outFile.getPath() + "/" + slashSplit[1].substring(
											slashSplit[1].length() - 2, slashSplit[1].length()) + "/" + msgID);

									FileOutputStream fos = new FileOutputStream(finalOutFile);
									fos.write(rawDoc.html().getBytes());
									LOG.debug("Mail content has been written in the path: " + finalOutFile.getPath());
									fos.close();
									LOG.info("FOS is closed after writing the file");
								}
								if (urlYear < Integer.parseInt(year)) {
									LOG.info(
											"Breaking the Outer Loop of getPageLinks() as the Year in the above URL is less than the Given Year");
									break outerLoop;
								}

							}
							if (extension.equalsIgnoreCase("htm") || extension.equalsIgnoreCase("html")
									|| extension.equalsIgnoreCase("aspx")) {
								// html2xml.writeToXML(lnk, path, fileName);
								htmlCnt++;
							} else {
								pdfCnt++;
								if (extension.equalsIgnoreCase("pdf") || extension.equalsIgnoreCase("doc")
										|| extension.equalsIgnoreCase("docx") || extension.equalsIgnoreCase("xls")
										|| extension.equalsIgnoreCase("ppt") || extension.equalsIgnoreCase("xlsx")
										|| extension.equalsIgnoreCase("pptx")) {
									docs.put(lnkChk, pageURL);
									arrLink.add(lnkChk);
									parentChild.put(lnkChk, pageURL);
									linkLevels.put(lnkChk, new Integer(level));
									continue;
								}
							}

							arrLink.add(lnkChk);
							if (linkLevels.get(lnkChk) != null) {
								int cLevel = Integer.parseInt(linkLevels.get(new String(lnkChk)).toString());
								if (level < cLevel) {
									linkLevels.put(lnkChk, new Integer(level));
									parentChild.put(lnkChk, pageURL);
									LOG.info("Recurrence of getPageLinks() occurred");
									getPageLinks(lnkChk, level, year, outFile);
								}
							} else {
								linkLevels.put(lnkChk, new Integer(level));
								parentChild.put(lnkChk, pageURL);
								LOG.info("Recurrence of getPageLinks() occurred");
								getPageLinks(lnkChk, level, year, outFile);
							}
						}
					}
				}
			}
		} catch (SocketTimeoutException ste) {
			LOG.error("Exception Occurred is: "+ste.getMessage());
			LOG.info("Recurrence of getPageLinks() occurred");
			getPageLinks(pageURL, level, year, outFile);
		} catch (Exception e) {
			LOG.error("Exception Occurred is: "+e.getMessage());
		}
	}
}