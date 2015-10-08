import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;


public class BaseSetBuilder {
	static HashMap<String,String> urlmap = new HashMap<String, String>();
	static HashMap<String,String> revurlmap = new HashMap<String, String>();
	static HashMap<String,Integer> basemap = new HashMap<String,Integer>();
	static HashMap<String, ArrayList<String>> inmap = new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> outmap = new HashMap<String, ArrayList<String>>();

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
			urlmap.put(part[1], part[0]);
		}

		String winlink = "";
		String filePath = "C:\\Users\\AKI\\workspace\\PageRank\\rootset.txt";
		try {
			winlink = String.join("\n", Files.readAllLines(Paths.get(filePath) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Rootset Loaded");

		String[] warray = winlink.split("\n");
		ArrayList check = new ArrayList<>();

		for(String s : warray) {
			check.add(urlmap.get(s));
		}
		
		String inlink = "";
		String filePatho = "C:\\Users\\AKI\\workspace\\PageRank\\urls.txt";
		try {
			inlink = String.join("\n", Files.readAllLines(Paths.get(filePatho) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] warrayo = inlink.split("\n");
		
		for(String str : warrayo) {
			String[] part = str.split("\t");
			revurlmap.put(part[0], part[1]);
		}



		Node node = nodeBuilder().client(true).clusterName("phoenixwings")
				.node();
		Client client = node.client();


		for(String s : warray) {
			String st = urlmap.get(s);
			basemap.put(st, 0);
			HashMap<String,String> inlinkslist=new HashMap<String,String>();
			HashMap<String,String> outlinkslist=new HashMap<String,String>();
			String docno=s;
			GetResponse getResponse = client
					.prepareGet("crawlweb", "webdoc", docno)
					.execute().actionGet();
			Map<String, Object> source = getResponse.getSource();
			System.out.println(s);


			if(source.get("in_links")!=null)
			{
				String [] inlinks=source.get("in_links").toString().split("\\R");


				for(int j=0;j<inlinks.length;j++)
				{
					if(urlmap.containsKey(inlinks[j]) && !inlinkslist.containsKey(inlinks[j]))
					{
						String sto = urlmap.get(inlinks[j]);
						basemap.put(sto, 0);
					}
					if (j == 49)
		        		break;
				}

			}
			if(source.get("out_links")!=null)
			{
				String [] outlinks=source.get("out_links").toString().split("[\\n]");
				for(int j=0;j<outlinks.length;j++)
				{
					if(urlmap.containsKey(outlinks[j]) && !outlinkslist.containsKey(outlinks[j]))
					{
						String sto = urlmap.get(outlinks[j]);
						basemap.put(sto, 0);
					}
				}
			}
		}



		System.out.println(basemap.size());
		
		Iterator<String> bitr = basemap.keySet().iterator();
		File log = new File("C:\\Users\\AKI\\workspace\\PageRank\\baseset.txt");
		FileWriter fileWriter = new FileWriter(log, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		while(bitr.hasNext()) {
			bufferedWriter.write(bitr.next() + "\n");
		}
		bufferedWriter.close();
		
		
		
		PrintWriter inlinkwriter=new PrintWriter("baseinlinks.txt");
	    PrintWriter outlinkwriter=new PrintWriter("baseoutlinks.txt");

		Iterator ir=basemap.keySet().iterator();
	     int count=0;
	     while(ir.hasNext())
	    {
	    	 HashMap<String,String> inlinkslist=new HashMap<String,String>();
	 	    HashMap<String,String> outlinkslist=new HashMap<String,String>();
	    	 String docno=revurlmap.get(ir.next().toString());
	    	 
	    	 GetResponse getResponse = client
						.prepareGet("crawlweb", "webdoc", docno)
						.execute().actionGet();
	    	 Map<String, Object> source = getResponse.getSource();
	    	System.out.println(count);
	    	count++;
	    	 
	    	if(source.get("in_links")!=null)
	    	{
	        String [] inlinks=source.get("in_links").toString().split("\\R");
	     
	       // System.out.println("length is "+outlinks.length);
	        inlinkwriter.write(urlmap.get(docno)+" ");
	         outlinkwriter.write(urlmap.get(docno)+" ");
	        for(int j=0;j<inlinks.length;j++)
	        {
	        	if(urlmap.containsKey(inlinks[j]) && !inlinkslist.containsKey(inlinks[j]))
	        	{
	        		inlinkslist.put(inlinks[j],inlinks[j]);
	        		inlinkwriter.write(urlmap.get(inlinks[j])+" ");
	        	}
	        	
	        }
	        
	    	}
	    	if(source.get("out_links")!=null)
	    	{
	    		   String [] outlinks=source.get("out_links").toString().split("[\\n]");
	        for(int j=0;j<outlinks.length;j++)
	        {
	        	if(urlmap.containsKey(outlinks[j]) && !outlinkslist.containsKey(outlinks[j]))
	        	{
	        		outlinkslist.put(outlinks[j],outlinks[j]);
	        		//System.out.println(outlinks[j]);
	        		outlinkwriter.write(urlmap.get(outlinks[j])+" ");
	        	}
	        }
	    	}
	        inlinkwriter.println();
	        outlinkwriter.println();
	        
	        
	    	
	    }
	    System.out.println("end");
	    inlinkwriter.close();
	    outlinkwriter.close();




	}
}
