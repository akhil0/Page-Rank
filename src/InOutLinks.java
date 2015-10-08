import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;

public class InOutLinks {
	static HashMap<String, Integer> urlmap = new HashMap<String, Integer>();
	
	public static void main(String[] args) throws Exception {
		
		Node node = nodeBuilder().client(true).clusterName("phoenixwings")
				.node();
		Client client = node.client();

		String winlink = "";
		String filePath = "C:\\Users\\AKI\\workspace\\PageRank\\urls.txt";
		try {
			winlink = String.join("\n", Files.readAllLines(Paths.get(filePath) ,Charset.forName("ISO-8859-1")));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] warray = winlink.split("\n");
		
		for(String str : warray) {
			String[] part = str.split("\t");
			urlmap.put(part[1], Integer.parseInt(part[0]));
		}
		
		PrintWriter inlinkwriter=new PrintWriter("inlinks.txt");
	    PrintWriter outlinkwriter=new PrintWriter("outlinks.txt");
		
		
		Iterator ir=urlmap.keySet().iterator();
	     int count=0;
	     while(ir.hasNext())
	    {
	    	 HashMap<String,String> inlinkslist=new HashMap<String,String>();
	 	    HashMap<String,String> outlinkslist=new HashMap<String,String>();
	    	 String docno=ir.next().toString();
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
