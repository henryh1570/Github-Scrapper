package app.scrapper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class App {
	
	/**
	 * Load strings from a file into an array list.
	 * @param fileName
	 * @return String ArrayList
	 */
	public static ArrayList<String> loadList(String fileName) {
		try {
			File file = new File(fileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			ArrayList<String> list = new ArrayList<String>();
			String next = bufferedReader.readLine();
			while (next != null) {
				list.add(next);
				next = bufferedReader.readLine();
			}
			bufferedReader.close();
			fileReader.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Return list of all links from a given URL. Can repeat previous links.
	 * @param url
	 * @return List of links
	 */
	public static ArrayList<String> getLinks(String url) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			Document doc = Jsoup.connect(url).timeout(4000).get();

			// Select the a-href links from the URL Document
			Elements elts = doc.select("a");
			for (Element e : elts) {
				list.add(e.absUrl("href"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return list;
		}
	}
	
	/**
	 * Return list of only unique links via filtering.
	 * @param url
	 * @param blackList of links
	 * @return Filtered Links
	 */
	public static ArrayList<String> getUniqueLinks(String url, ArrayList<String> list, ArrayList<String> blackList, String userName) {
		try {
			Document doc = Jsoup.connect(url).timeout(10000).get();
			// Select the a-href links from the URL Document
			Elements elts = doc.select("a");
			for (Element e : elts) {
				String link = e.absUrl("href");
				// Filter out some other links
				if (link.contains("https://github.com/")) {
					// Check with ignore urls list
					if (!blackList.contains(link)) {
						// Check for repeats
						if (!list.contains(link)) {
							// Check with specific filter
							if (!isUnwanted(link)) {
								if (!link.toLowerCase().equals("https://github.com/" + userName)) {
									list.add(link);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return list;
		}
	}
	
	/**
	 * A method to grab all followers or followings of a user.
	 * @param userName is the seed
	 * @param list is the cumulative list of links
	 * @param blackList used to ignore certain sites
	 * @param b specify true for followers, false for following
	 * @return
	 */
	public static ArrayList<String> getGithubLinks(String userName, ArrayList<String> list,
			ArrayList<String> blackList, boolean b) {

		// Designate search type.
		String type = "followers";
		if (b == false) {
			type = "following";
		}
		
		// Grab first page.
		list = getUniqueLinks("https://github.com/" + userName + "?tab=" + type, list, blackList, userName);
		int num = 2;
		boolean isNotOver = true;
		
		// Obtain info from other pages.
		while (isNotOver) {			
			try {
				Thread.sleep(3000);
				String url = "https://github.com/" + userName + "?page=" + num + "&tab=" + type;
				num++;
				int a = list.size();
				list = getUniqueLinks(url, list, blackList, userName);
				
				// Finish if no new link
				if (a == list.size()) {
					isNotOver = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}
			
	/**
	 * Helper method to detect unwantedURL
	 * @param url
	 * @return wanted | unwanted
	 */
	public static boolean isUnwanted(String url) {		
		int slashCount = 0;
		int dotCount = 0;
		int colonCount = 0;
		
		for(int i=0; i<url.length(); i++ ) {			
		    if (url.charAt(i) == '?' ||
		    	url.charAt(i) == '#' ||
		    	url.charAt(i) == '=' ||
		    	url.charAt(i) == '+') {
		    	return true;
		    }
			
		    if (url.charAt(i) == '/' ) {
		        slashCount++;
		    }
		    
		    if (url.charAt(i) == '.' ) {
		        dotCount++;
		    } 

		    if (url.charAt(i) == ':' ) {
		        colonCount++;
		    } 
		}
		
		if (slashCount > 3) {
			return true;
		} else if (dotCount > 1) {
			return true;
		} else if (colonCount > 1) {
			return true;
		} else {
			return false;	
		}		
	}
	
	/**
	 * Helper method to detect unwanted tables of class names.
	 * @param list
	 * @param other
	 * @return other In | Out
	 */
	public static boolean isStringInList(ArrayList<String> list, String other) {
		for (String str : list) {
			if (other.contains(str)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a new directory.
	 * @param filePath
	 * @return made | not made
	 */
	public static boolean makeNewDir(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			if (file.mkdir()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Write an array list to a file.
	 * @param List
	 * @param fileName
	 * @return 
	 */
	public static boolean writeListToFile(ArrayList<String> data, String fileName) {
		try {
			File f = new File(fileName);
			if (f.exists()) {
				return false;
			} else {
				PrintWriter p = new PrintWriter(f);
				for (String str : data) {
					p.write(str + "\n");
				}
				p.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * An 2n duplicate removal method.
	 * Destroys the original ordering.
	 * @param Original list
	 * @return Clean list
	 */
	public static ArrayList<String> removeDuplicates(ArrayList<String> list) {
		ArrayList<String> sortedList = new ArrayList<String>();
		HashSet<String> setList = new HashSet<String>();
		for (String s : list) {
			setList.add(s);			
		}
		
		for (String s : setList) {
			sortedList.add(s);
		}
		return sortedList;
	}
	
	public static void main(String[] args) {

		ArrayList<String> blackList = loadList("blacklist.txt");
		ArrayList<String> list = new ArrayList<String>();
		list = getGithubLinks("liukevin5", list, blackList, true);
		
		for (String s : list) {
			System.out.println(s);
		}
	}
}
