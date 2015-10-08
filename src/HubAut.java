import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;

public class HubAut {

	public static HashMap<Integer,String> completeset=new HashMap<Integer,String>();
	public static HashMap<String,Integer> readurls()
	{
		HashMap<String,Integer> urls=new HashMap<String,Integer>();
		
		try {
			BufferedReader br=new BufferedReader(new FileReader("urls.txt"));
			String line=null;
			while((line=br.readLine())!=null)
			{
				String[] temp=line.split("\t");
				urls.put(temp[1],Integer.parseInt(temp[0])-1);
				completeset.put(Integer.parseInt(temp[0])-1, temp[1]);
			}
			
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return urls;
		
	}
	
	
	public static void calc_scores()
	{
		HashMap<String, ArrayList> inlinksmap = new HashMap<String, ArrayList>();
		HashMap<String, ArrayList> outlinksmap = new HashMap<String, ArrayList>();
		HashMap<String,Integer> urlmap=new HashMap<String,Integer>();
		
		urlmap=readurls();
		
		Node node = nodeBuilder().client(true).clusterName("phoenixwings")
				.node();
		Client client = node.client();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("field", "text");
		params.put("term", "latin music");
		try {
			SearchResponse response = client
					.prepareSearch("crawlweb")
					.setQuery(
							new FunctionScoreQueryBuilder(QueryBuilders
									.matchQuery("text", "latin music")))
					.setSize(1000).setNoFields().execute().actionGet();
			JSONObject json = new JSONObject(response.toString());
			System.out.println("hi");
			JSONObject hits = json.getJSONObject("hits");
			JSONArray jarry = hits.getJSONArray("hits");
			// System.out.println(jarry.length());
			HashMap<String,Integer> rootset = new HashMap<String,Integer>();
			ArrayList<String> urls = new ArrayList<String>();
			for (int i = 0; i < jarry.length(); i++) {
				System.out.println(i);
				// System.out.println(i);
				JSONObject obj = jarry.getJSONObject(i);
				urls.add(obj.get("_id").toString());
				rootset.put(obj.get("_id").toString(), urlmap.get(obj.get("_id").toString()));
			}
		
			Iterator ir = urls.iterator();
			System.out.println(urls.size());
			int count = 0;
			
			
			while (ir.hasNext()) {
				
				//System.out.println("hello");
				//System.out.println(count);
				HashMap<String, String> inlinkslist = new HashMap<String, String>();
				HashMap<String, String> outlinkslist = new HashMap<String, String>();
				
				String docno = ir.next().toString();
				GetResponse getResponse = client
						.prepareGet("crawlweb", "webdoc", docno).execute()
						.actionGet();
				Map<String, Object> source = getResponse.getSource();
				System.out.println(count);
				count++;

				if (source.get("in_links") != null) {
					String[] inlinks = source.get("in_links").toString()
							.split("[\\r\\n]");

					// System.out.println("length is "+outlinks.length);

				   
					int indexer=0;
					for (int j = 0; j < inlinks.length; j++) 
					{
						
						if (urlmap.containsKey(inlinks[j]) && !inlinkslist.containsKey(inlinks[j])) 
						{
							indexer++;
							inlinkslist.put(inlinks[j], inlinks[j]);
							rootset.put(inlinks[j], urlmap.get(inlinks[j]));
						}
						if(indexer==50)
							break;
					}
					

				}
			
				if (source.get("out_links") != null) {
					String[] outlinks = source.get("out_links").toString()
							.split("[\\n]");
					for (int j = 0; j < outlinks.length; j++) {
						if (urlmap.containsKey(outlinks[j])
								&& !outlinkslist.containsKey(outlinks[j])) {
							outlinkslist.put(outlinks[j], outlinks[j]);
							// System.out.println(outlinks[j]);
							rootset.put(outlinks[j], urlmap.get(outlinks[j]));
						}
					}
					
				}

			}

			//HashMap<String,Integer> baseset=construct_links(rootset,client,urlmap);
			System.out.println("rootset size is" +rootset.size());
			
			calculate_scores(rootset,rootset,urlmap);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static HashMap<String,Integer> construct_links(HashMap<String,Integer> baseset,Client client, HashMap<String,Integer> urlsmap) throws FileNotFoundException
	{
		
	
		Iterator ir=baseset.keySet().iterator();
		
	    	System.out.println(baseset.size());
		
		    HashMap<String,Integer> newbaseset= new HashMap<String,Integer>();
		    newbaseset.putAll(baseset);
		    int count=0;
		    for(Entry<String, Integer> e : baseset.entrySet()) {
		  
			 	HashMap<String,String> inlinkslist=new HashMap<String,String>();
		 	    HashMap<String,String> outlinkslist=new HashMap<String,String>();
		 		System.out.println("count is"+ count++);
		    	//System.out.println("value "+ir.next());
		    	String docno=e.getKey();
		    
		    	 
		    	 GetResponse getResponse = client
							.prepareGet("crawlweb", "webdoc", docno)
							.execute().actionGet();
		    	 Map<String, Object> source = getResponse.getSource();
		    	
		    	
		    	if(source.get("in_links")!=null)
		    	{
		    		
		    		String [] inlinks=source.get("in_links").toString().split("\\R");
		     
		      
		        for(int j=0;j<inlinks.length;j++)
		        {
		        	 
		        	if(urlsmap.containsKey(inlinks[j]) && !inlinkslist.containsKey(inlinks[j]))
		        	{
		        		inlinkslist.put(inlinks[j],inlinks[j]);
		        		newbaseset.put(inlinks[j],urlsmap.get(inlinks[j]));
		        	 	
		        	}
		        }
		       
		    	}
		    	if(source.get("out_links")!=null)
		    	{
		    		  
		    		   String [] outlinks=source.get("out_links").toString().split("[\\n]");
		    		   
		    		   for(int j=0;j<outlinks.length;j++)
		        {
		        	
		        	if(urlsmap.containsKey(outlinks[j]) && !outlinkslist.containsKey(outlinks[j]))
		        	{
		        		outlinkslist.put(outlinks[j],outlinks[j]);
		        		//System.out.println(outlinks[j]);
		        		newbaseset.put(outlinks[j],urlsmap.get(outlinks[j]));
		        		
		        	}
		        }
		    	}
		        
		    	
		    }
		    System.out.println("end");
		   
			return newbaseset;
			}
	
	
	public static void calculate_scores(HashMap<String,Integer> baseset,HashMap<String,Integer>rootset, HashMap<String,Integer> totalset) throws Exception
	{
		HashMap<String,Integer> urls=hits.readurls();
		HashMap<String,String>inlinkslist=new HashMap<String,String>();
		HashMap<String,String>outlinklist=new HashMap<String,String>();
		BufferedReader inlinkreader=null;
		BufferedReader outlinkreader=null;
		try
		{
	          inlinkreader=new BufferedReader(new FileReader("inlinks.txt"));
		      outlinkreader=new BufferedReader(new FileReader("outlinks.txt"));
		      String line=null;
		      while((line=inlinkreader.readLine())!=null)
		      {
		    	 // System.out.println("inside the loop");
		    	  if(line.split(" ").length > 1)
		    	  {
		    	  String link=line.substring(0, line.indexOf(" "));
		    	  String inlinks=line.substring(line.indexOf(" ")+1);
		    	  
		    	  inlinkslist.put(link,inlinks);
		    	  }
		      }
		      
		      while((line=outlinkreader.readLine())!=null)
		      {
		    	//  System.out.println("inside the outlink loop");
		    	  line=line.trim();
		    	  if(line.split(" ").length>1)
		    	  {
		    	  String link=line.substring(0, line.indexOf(" "));
		    	  String inlinks=line.substring(line.indexOf(" ")+1);
		    	  
		    	  outlinklist.put(link,inlinks);
		    	  }
		      }
		      
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Set<String> pages=baseset.keySet();
		 HashMap<String,Double> auth_scores=new HashMap<String,Double>();
		 HashMap<String,Double> hub_scores=new HashMap<String,Double>();
		
		 
		 Iterator ir=pages.iterator();
		 while(ir.hasNext())
		 {
			 String page=ir.next().toString();
			 auth_scores.put(page,1.0);
			 hub_scores.put(page,1.0);
		 }
		
		 int counter1=1;
		
		 double auth_perplexity=0.0;
			double hub_perplexity=0.0;
		do
		{
			 HashMap<String,Double> new_auth_scores=new HashMap<String,Double>();
			 HashMap<String,Double> new_hub_scores=new HashMap<String,Double>();
			 
			 double prev_auth_perplexity=auth_perplexity;
			 double prev_hub_perplexity=hub_perplexity;
		
			 auth_perplexity=0.0;
			 hub_perplexity=0.0;
			
			double norm_auth_score=0.0;
			double norm_hub_score=0.0;
			for(java.util.Map.Entry<String, Double> e : auth_scores.entrySet()) {
				String id = e.getKey();
				
				double score = e.getValue();
                
			   norm_auth_score+=Math.pow(score, 2);
			  // System.out.println("inside fist loop");
			}
			norm_auth_score=Math.sqrt(norm_auth_score);
			
			
			Iterator tempir=auth_scores.keySet().iterator();
			
			HashMap<String,Double> tempauth=new HashMap<String,Double>();
			while(tempir.hasNext())
			{
				//System.out.println("inside sec loop");
			   String temp=tempir.next().toString();
			   
			   double tempscore=auth_scores.get(temp);
			   
			   tempscore=tempscore/norm_auth_score;
			   
			   tempauth.put(temp,tempscore);
			}
			
			auth_scores.putAll(tempauth);
			
			 tempir=hub_scores.keySet().iterator();
			 HashMap<String,Double> temphub=new HashMap<String,Double>();
			for(java.util.Map.Entry<String, Double> e : hub_scores.entrySet()) {
				//System.out.println("inside 3 loop");
				String id = e.getKey();
				double score = e.getValue();
                
			   norm_hub_score+=Math.pow(score, 2);
			}
			norm_hub_score=Math.sqrt(norm_hub_score);
			
			
			while(tempir.hasNext())
			{
				//System.out.println("inside 4 loop");
			   String temp=tempir.next().toString();
			   
			   double tempscore=hub_scores.get(temp);
			   
			   tempscore=tempscore/norm_hub_score;
			   
			   temphub.put(temp,tempscore);
			}
			hub_scores.putAll(temphub);
				
			for(java.util.Map.Entry<String, Double> e : auth_scores.entrySet()) {
				//System.out.println("inside 5 loop");
				String id = e.getKey();
				double score = e.getValue();

				auth_perplexity = auth_perplexity  + score *(Math.log(score)/Math.log(2));
			}
			System.out.println(hub_scores.size());
			for(java.util.Map.Entry<String, Double> e : hub_scores.entrySet()) {
				//System.out.println("inside 6 loop");
				String id = e.getKey();
				double score = e.getValue();

				hub_perplexity = hub_perplexity  + score *(Math.log(score)/Math.log(2));
			}
			auth_perplexity =Math.pow(2,-auth_perplexity);
			hub_perplexity =Math.pow(2,-hub_perplexity);
			
			 System.out.println("auth perplexity" +auth_perplexity);
			 System.out.println("hub perplexity" + hub_perplexity);
			 
	        Iterator itir=baseset.keySet().iterator();
			while(itir.hasNext())
			{
				//System.out.println("inside 7 loop");
				String page=itir.next().toString();
				//System.out.println(page);
				double authscore=auth_scores.get(page);
				if(inlinkslist.containsKey(totalset.get(page).toString()))
				{
				String [] inlinks=inlinkslist.get(totalset.get(page).toString()).split(" ");
			///	System.out.println(inlinks.length);
				for(int i=0;i<inlinks.length;i++)
				{
				//	System.out.println("inside 8 loop");
					if(hub_scores.containsKey(completeset.get(Integer.parseInt(inlinks[i]))))
					authscore+=hub_scores.get(completeset.get(Integer.parseInt(inlinks[i])));
				}
				new_auth_scores.put(page, authscore);
				}
				double hubscore=hub_scores.get(page);
				//System.out.println("End of 8th");
			//	System.out.println(totalset.get(page).toString());
				if(outlinklist.containsKey(totalset.get(page).toString()))
				{
				String [] outlinks=outlinklist.get(totalset.get(page).toString()).split(" ");
				for(int i=0;i<outlinks.length;i++)
				{
					//System.out.println("inside 9 loop");
					//System.out.println(outlinks[i] + " "+page);
					if(auth_scores.containsKey(completeset.get(Integer.parseInt(outlinks[i]))))
					hubscore+=auth_scores.get(completeset.get(Integer.parseInt(outlinks[i])));
				}
				new_hub_scores.put(page,hubscore);
				}
			}
			
			auth_scores.putAll(new_auth_scores);
			hub_scores.putAll(new_hub_scores);
			
			 
			System.out.println(Math.floor(prev_auth_perplexity));
			System.out.println(Math.floor(prev_hub_perplexity));
			
			 
			System.out.println((int)(auth_perplexity));
			System.out.println(Math.floor(hub_perplexity));
			
			if((Math.floor(prev_auth_perplexity)==Math.floor(auth_perplexity)) &&
					(Math.floor(prev_hub_perplexity)==Math.floor(hub_perplexity)))
			{
				System.out.println("inside if");
				counter1++;
			}
			else
				counter1=1;
			
			
		}while(counter1!=4);
		
		
		PrintWriter auth_writer=new PrintWriter(new FileWriter("auth_scores.txt"));
		PrintWriter hub_writer=new PrintWriter(new FileWriter("hub_scores.txt"));
		Set<java.util.Map.Entry<String, Double>> set = auth_scores.entrySet();
		List<java.util.Map.Entry<String, Double>> list = new ArrayList<java.util.Map.Entry<String, Double>>(
				set);
		Collections.sort(list,
				new Comparator<Map.Entry<String, Double>>() {
					public int compare(Map.Entry<String, Double> o1,
							Map.Entry<String, Double> o2) {
						return (o2.getValue()).compareTo(o1.getValue());
					}
				});
		
		for (Map.Entry<String, Double> entry : list) {
			 auth_writer.println(entry.getKey() + "\t" + entry.getValue());
			
		}

		set=hub_scores.entrySet();
		list=new ArrayList<java.util.Map.Entry<String, Double>>(
				set);
		Collections.sort(list,
				new Comparator<Map.Entry<String, Double>>() {
					public int compare(Map.Entry<String, Double> o1,
							Map.Entry<String, Double> o2) {
						return (o2.getValue()).compareTo(o1.getValue());
					}
				});
		
		for (Map.Entry<String, Double> entry : list) {
			 hub_writer.println(entry.getKey() + "\t" + entry.getValue());
			
		}
	}
	
	public static void main(String[] args)
	{
		calc_scores();
	}
		
	}