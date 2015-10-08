import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;


public class MergePageRank {

	static HashMap<String, ArrayList<String>> inmap = new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> outmap = new HashMap<String, ArrayList<String>>();
	static HashMap<String, Double> oldprmap = new HashMap<String, Double>();
	static HashMap<String, Double> newprmap = new HashMap<String, Double>();
	static HashMap<String, Integer> completelinks = new HashMap<String, Integer>();
	static HashMap<String, Double> sinkmap = new HashMap<String, Double>();
	static HashMap<String, String> urlmap = new HashMap<String, String>();
	static double d = 0.85;


	public static void main(String[] args) throws Exception {
		
		String oinlink = "";
		String filePathop = "C:\\Users\\AKI\\workspace\\PageRank\\urls.txt";
		try {
			oinlink = String.join("\n", Files.readAllLines(Paths.get(filePathop) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] parray = oinlink.split("\n");
		
		for(String str : parray) {
			String[] part = str.split("\t");
			urlmap.put(part[0], part[1]);
		}
		
		String winlink = "";
		String filePath = "C:\\Users\\AKI\\workspace\\PageRank\\inlinks.txt";
		try {
			winlink = String.join("\n", Files.readAllLines(Paths.get(filePath) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Inlink Loaded");

		String[] warray = winlink.split("\n");

		// Creating In Links
		for(String str : warray) {
			String[] eachline = str.split(" ");

			ArrayList<String> alist = new ArrayList<String>();
			if(eachline.length > 1 ) {
				for(int i = 1 ; i < eachline.length; i++) {
					alist.add(eachline[i]);

				}
				inmap.put(eachline[0], alist);
			}
			else {
				inmap.put(eachline[0], alist);
			}
		}

		System.out.println("InLink Map Loaded");

		// Loading All Pages
		for(Entry<String, ArrayList<String>> e : inmap.entrySet()) {
			//System.out.println(e);
			completelinks.put(e.getKey(), 0);
		}

		System.out.println("Complete links Map Loaded");


		// Creating Outlinks
		for(String s : warray) {
			String[] eachline = s.split(" ");
			if(eachline.length > 1 ) {
				String id = eachline[0];
				for(int i = 1 ; i < eachline.length; i++) {
					String str = eachline[i];
					if(outmap.containsKey(str)) {
						ArrayList<String> alist = new ArrayList<String>();
						alist = outmap.get(str);
						alist.add(id);
						outmap.put(str, alist);
					}
					else {
						ArrayList<String> alist = new ArrayList<String>();
						alist.add(id);
						outmap.put(str, alist);
					}
				}
			}
		}

		//System.out.println(outmap.size());

		System.out.println("Outlink Map Loaded");

		float n = inmap.size();

		// Load PRMap with original values
		Iterator<String> itr = completelinks.keySet().iterator();
		while(itr.hasNext()) {
			String id = itr.next();
			oldprmap.put(id,  (1.0/n));
			newprmap.put(id,  (1.0/n));
		}


		// Loading Sink Nodes into Sink Map
		Iterator<String> otr = completelinks.keySet().iterator();
		while(otr.hasNext()) {
			String id = otr.next();
			if(outmap.containsKey(id)){
			}
			else {
				sinkmap.put(id, 0.0);
			}
		}
		System.out.println("Sink Map Loaded");
		//System.out.println(sinkmap);


		//Page Rank

		ArrayList<Double> countlist = new ArrayList<Double>();

		for(int i = 0 ; i < 100 ; i++) {
			
			// Page Rank fr Sink Nodes
			Double sinkPR = 0.0 ;
			Iterator<String> sitr = sinkmap.keySet().iterator();
			while(sitr.hasNext()) {
				String id = sitr.next();
				Double val = oldprmap.get(id);
				sinkPR = sinkPR + val ;

			}


			// Perplexity for the Page Rank
			double perplexity = 0.0 ;

			Iterator<String> pitr = oldprmap.keySet().iterator();
			double h = 0.0;
			while(pitr.hasNext()) {
				String id = pitr.next();
				Double pr = oldprmap.get(id);
				h = h + pr*(Math.log(pr)/Math.log(2));
			}

			perplexity = Math.pow(2, -h);
			countlist.add(perplexity);
			System.out.println(perplexity);
			
			
			//Converges, Break the Loop
			if(convergence(countlist))
				break;

			
			// Computing Page Rank
			Iterator<String> tempitr = completelinks.keySet().iterator();
			while(tempitr.hasNext()) {
				String page = tempitr.next();
				ArrayList<String> templist = inmap.get(page);

				Double newPR = (1-d)/n;
				newPR += d*(sinkPR/n);
				for(String s : templist) {
					if(outmap.containsKey(s) && oldprmap.containsKey(s))
					newPR = newPR + d*(oldprmap.get(s)/outmap.get(s).size());
				}
				newprmap.put(page, newPR);

			}

			oldprmap.putAll(newprmap);
		}

		
		HashMap newmap = new HashMap<>();
		
		// Sort Map by Values
		newmap = sortByValues(oldprmap);
		
		//Write Top 500 Page Ranks
		Iterator newmapitr = newmap.keySet().iterator();
		int id0 = 0;
		File log = new File("C:\\Users\\AKI\\workspace\\PageRank\\mergerank.txt");

			FileWriter fileWriter = new FileWriter(log, true);
			int no = 0;
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			while(newmapitr.hasNext())
			{
				no++;
				String id = newmapitr.next().toString();
				float val = Float.parseFloat(newmap.get(id).toString());
				bufferedWriter.write(no + " - " + urlmap.get(id) + "\n"); //+ " - " + val + "\n");
			}
		
			bufferedWriter.close();
	}


	private static boolean convergence(ArrayList<Double> countlist) {
		boolean bo = true;
		if(countlist.size() >= 4) {
			int n = countlist.size() - 1;
			double a = Math.floor(countlist.get(n));
			double b = Math.floor(countlist.get(n-1));
			double c = Math.floor(countlist.get(n-2));
			double d = Math.floor(countlist.get(n-3));

			if(a == b && c == d && a == c) {
				bo = true;
				System.out.println(n+1);
			}
			else {
				bo = false;
			}
		}
		else
		{
			bo = false;
		}
		return bo;
	}

	private static HashMap sortByValues(HashMap map) { 
		List list = new LinkedList(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		} 

		HashMap newresult = new LinkedHashMap<>();
		Iterator atr = sortedHashMap.keySet().iterator();
		int id =0 ;

		if(sortedHashMap.size() < 500)
			newresult.putAll(sortedHashMap);
		else
		{
			while(atr.hasNext() && id < 500)
			{
				id++;
				String next = atr.next().toString();
				newresult.put( next, sortedHashMap.get(next));
			}
		}

		return newresult;
	}

}



