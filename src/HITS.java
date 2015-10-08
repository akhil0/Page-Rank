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


public class HITS {

	static HashMap<String, ArrayList<String>> inmap = new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> outmap = new HashMap<String, ArrayList<String>>();
	static HashMap<String, Double> oldhubmap = new HashMap<String, Double>();
	static HashMap<String, Double> newhubmap = new HashMap<String, Double>();
	static HashMap<String, Double> oldautmap = new HashMap<String, Double>();
	static HashMap<String, Double> newautmap = new HashMap<String, Double>();
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
		String filePath = "C:\\Users\\AKI\\workspace\\PageRank\\baseinlinks.txt";
		try {
			winlink = String.join("\n", Files.readAllLines(Paths.get(filePath) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

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
		}

		System.out.println("InLink Map Loaded");
		System.out.println(inmap.size());

		// Loading All Pages
		for(Entry<String, ArrayList<String>> e : inmap.entrySet()) {
			//System.out.println(e);
			completelinks.put(e.getKey(), 0);
		}

		System.out.println("Complete links Map Loaded");
		System.out.println(completelinks.size());


		// Creating Outlinks
		String pinlink = "";
		String pfilePath = "C:\\Users\\AKI\\workspace\\PageRank\\baseoutlinks.txt";
		try {
			pinlink = String.join("\n", Files.readAllLines(Paths.get(pfilePath) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] pwarray = pinlink.split("\n");

		// Creating In Links
		for(String str : pwarray) {
			String[] eachline = str.split(" ");

			ArrayList<String> alist = new ArrayList<String>();
			if(eachline.length > 1 ) {
				for(int i = 1 ; i < eachline.length; i++) {
					alist.add(eachline[i]);

				}
				outmap.put(eachline[0], alist);
			}
		}

		//System.out.println(outmap.size());

		System.out.println("Outlink Map Loaded");
		System.out.println(outmap.size());

		float n = inmap.size();

		// Load PRMap with original values
		Iterator<String> itr = completelinks.keySet().iterator();
		while(itr.hasNext()) {
			String id = itr.next();
			oldhubmap.put(id,  1.0);
			newhubmap.put(id,  1.0);
			oldautmap.put(id,  1.0);
			newautmap.put(id,  1.0);
		}



		//Page Rank

		ArrayList<Double> hubcountlist = new ArrayList<Double>();
		ArrayList<Double> autcountlist = new ArrayList<Double>();

		for(int i = 0 ; i < 100 ; i++) {
			
			
			
			// Perplexity for the Page Rank
			double perplexity = 0.0 ;

			 Iterator<String> pitr = oldhubmap.keySet().iterator();
			double h = 0.0;
			while(pitr.hasNext()) {
				String id = pitr.next();
				Double pr = oldhubmap.get(id);
				h = h + pr*(Math.log(pr)/Math.log(2));
			}

			perplexity = Math.pow(2, -h);
			hubcountlist.add(perplexity);
			System.out.println("hub map = " + perplexity);
			
			
			double aperplexity = 0.0 ;

			Iterator<String> apitr = oldautmap.keySet().iterator();
			double  ah = 0.0;
			while(apitr.hasNext()) {
				String id = apitr.next();
				Double pr = oldautmap.get(id);
			ah = ah + pr*(Math.log(pr)/Math.log(2));
			}

			aperplexity = Math.pow(2, -ah);
			autcountlist.add(aperplexity);
			System.out.println("aut map = " + aperplexity);
			
			
			//Converges, Break the Loop
			if((convergence(hubcountlist) && convergence(autcountlist)) || (i ==50))
				break;

			
			// Computing hub
			Iterator<String> tempitr =inmap.keySet().iterator();
			
			while(tempitr.hasNext()) {
				
				String page = tempitr.next().toString();
			//	System.out.println(page);
				
				ArrayList<String> templist = new ArrayList<>() ;
				if(outmap.containsKey(page)) {
				templist = outmap.get(page);
				}

				Double hub = oldhubmap.get(page);
				
				for(String s : templist) {
					if(oldautmap.containsKey(s))
					{
						//System.out.println(s);
					hub = hub + oldautmap.get(s);
					}
				}
				newhubmap.put(page, hub);
				
				ArrayList<String> atemplist = new ArrayList<>() ;
				if(inmap.containsKey(page)) {
					atemplist = inmap.get(page);
					}

				Double aut = oldautmap.get(page);
				
				for(String st : atemplist) {
					if(oldhubmap.containsKey(st))
					aut = aut + oldhubmap.get(st);
				}
				newautmap.put(page, aut);

			}

			oldhubmap.putAll(newhubmap);
			oldautmap.putAll(newautmap);
			
			
			 pitr = oldhubmap.keySet().iterator();
			double nshub = 0.0;
			while(pitr.hasNext()) {
				String id = pitr.next();
				Double pr = oldhubmap.get(id);              
				   nshub+=Math.pow(pr, 2);
			}
			nshub=Math.sqrt(nshub);

			apitr = oldautmap.keySet().iterator();
			double  nsaut = 0.0;
			while(apitr.hasNext()) {
				String id = apitr.next();
				Double pr = oldautmap.get(id);
				            
				   nsaut+=Math.pow(pr, 2);
			}
			nsaut=Math.sqrt(nsaut);
			
			pitr = oldhubmap.keySet().iterator();
			while(pitr.hasNext()) {
				String id = pitr.next();
				Double pr = oldhubmap.get(id);
				oldhubmap.put(id, pr/nshub);
			}

			apitr = oldautmap.keySet().iterator();
			while(apitr.hasNext()) {
				String id = apitr.next();
				Double pr = oldautmap.get(id);
			oldautmap.put(id, pr/nsaut);
			}
		}

		
		HashMap hnewmap = new HashMap<>();
		HashMap anewmap = new HashMap<>();
		
		// Sort Map by Values
		hnewmap = sortByValues(oldhubmap);
		anewmap = sortByValues(oldautmap);
		
		//Write Top 500 Page Ranks
		Iterator newmapitr = hnewmap.keySet().iterator();
		int id0 = 0;
		File log = new File("C:\\Users\\AKI\\workspace\\PageRank\\hubrank.txt");

			FileWriter fileWriter = new FileWriter(log, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			while(newmapitr.hasNext())
			{
				String id = newmapitr.next().toString();
				//float val = Float.parseFloat(newmap.get(id).toString());
				bufferedWriter.write(urlmap.get(id) + "\t" + oldhubmap.get(id) + "\n"); //+ " - " + val + "\n");
			}
		
			bufferedWriter.close();
			
			
			Iterator anewmapitr = anewmap.keySet().iterator();
			File alog = new File("C:\\Users\\AKI\\workspace\\PageRank\\autrank.txt");

				FileWriter afileWriter = new FileWriter(alog, true);
				BufferedWriter abufferedWriter = new BufferedWriter(afileWriter);
				while(anewmapitr.hasNext())
				{
				
					String id = anewmapitr.next().toString();
					//float val = Float.parseFloat(newmap.get(id).toString());
					abufferedWriter.write(urlmap.get(id) + "\t" + oldautmap.get(id) + "\n"); //+ " - " + val + "\n");
				}
			
				abufferedWriter.close();
	}


	private static boolean convergence(ArrayList<Double> countlist) {
		boolean bo = true;
		if(countlist.size() >= 4) {
			int n = countlist.size() - 1;
			double a = Math.floor(countlist.get(n));
			double b = Math.floor(countlist.get(n-1));
			double c = Math.floor(countlist.get(n-2));
			double d = Math.floor(countlist.get(n-3));

			if(a == b) { // && c == d && a == c) {
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



