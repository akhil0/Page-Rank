import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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



public class GetURLs {
	static HashMap<String,Integer> urlmap  =  new HashMap<String, Integer>();
	public static void getDataFromES()
	{
		System.out.println("inside function");

		Node node = nodeBuilder().client(true).clusterName("phoenixwings")
				.node();
		Client client = node.client();
		SearchResponse response = client.prepareSearch("crawlweb")
				.setQuery(QueryBuilders.matchAllQuery()).setSize(50000).setNoFields().setTimeout("10000")
				.execute().actionGet();
		try
		{
			System.out.println("inside try");
			System.out.println(response.getHits());

			JSONObject json = new JSONObject(response.toString());
			JSONObject hits = json.getJSONObject("hits");
			JSONArray jarry = hits.getJSONArray("hits");
			//System.out.println(jarry.length());
			ArrayList<String>  urls=new ArrayList<String>();

			for(int i=0;i<jarry.length();i++)
			{
				System.out.println(i);
				//System.out.println(i);
				JSONObject obj = jarry.getJSONObject(i);
				urls.add(obj.get("_id").toString());
				urlmap.put(obj.get("_id").toString(), 0);
				//System.out.println(i+1);
			}

			System.out.println(urls.size()); 

			File log = new File("C:\\Users\\AKI\\workspace\\PageRank\\urls.txt");
			FileWriter fileWriter = new FileWriter(log, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			int id = 0;
			for(String s : urls) {
				id++;
				bufferedWriter.write(id + "\t" + s + "\n");
			}

			bufferedWriter.close();
			System.out.println("Success");

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}



	public static void main(String [] args)
	{
		getDataFromES();	
	}
}