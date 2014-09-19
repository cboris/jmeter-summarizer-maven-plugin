package com.github.cboris.jmeter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.json.simple.*;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GraphiteSummarizerExample {

	public static void parseAsArray(Reader reader) throws ParseException, IOException {
		 JSONParser parser=new JSONParser();

		  System.out.println("=======decode=======");
		                
		
		  Object obj=parser.parse(reader);
		  JSONArray array=(JSONArray)obj;
		  System.out.println("======the 1st element of array======");
		  System.out.println(array.get(0));
		  System.out.println();
		                
		  JSONObject obj2=(JSONObject)array.get(0);
		  System.out.println("======field \"1\"==========");
		  System.out.println(obj2.get("0"));    

		
	}
	
	public static void sax_like(Reader reader) throws IOException {
		 //String jsonText = "{\"first\": 123, \"second\": [{\"k1\":{\"id\":\"id1\"}}, 4, 5, 6, {\"id\": 123}], \"third\": 789, \"id\": null}";
		  JSONParser parser = new JSONParser();
		  KeyFinder finder = new KeyFinder();
		  finder.setMatchKey("datapoints");
		  try{
		    while(!finder.isEnd()){
		      
		      parser.parse(reader, finder, true);
		      if(finder.isFound()){
		        finder.setFound(false);
		        System.out.println("found id:");
		        System.out.println(finder.getValue());
		      }
		    }           
		  }
		  catch(ParseException pe){
		    pe.printStackTrace();
		  }

	}
	
	public static void container_example(Reader reader) throws IOException {
		String jsonText = "{\"first\": 123, \"second\": [4, 5, 6], \"third\": 789}";
		  JSONParser parser = new JSONParser();
		  ContainerFactory containerFactory = new ContainerFactory(){
		    public List creatArrayContainer() {
		      return new LinkedList();
		    }

		    public Map createObjectContainer() {
		      return new LinkedHashMap();
		    }
		                        
		  };
		                
		  try{
		    List datapoints = (List)
		    		((Map)
		    				((List)parser.parse(reader, containerFactory))
		    				.get(0))
		    			.get("datapoints");
		    System.out.println("==== iterate high level list");
		    for ( Object el : datapoints) {
				List l = (List)el;
				
		    	System.out.println(l.get(0).getClass()+": "+l.get(0));
				
			}
		   
		                        
		    System.out.println("==toJSONString()==");
		    System.out.println(JSONValue.toJSONString(datapoints));
		  }
		  catch(ParseException pe){
		    System.out.println(pe);
		  }
	}
	
	public static void main(String[] args) {
		BufferedReader reader = null;
		
		final String fileLoc = "/Users/bpetrovic/Documents/netcentric/testing/src-netcentric/ubs-fit-perftest/target/jmeter/results/cpu-data.json";
		
		//reader = call_saxLike(reader, fileLoc);
		
		
		
	
		
		try {
			reader = new BufferedReader(new FileReader(fileLoc));
			System.out.println("----- CONTAINER ----------");
			container_example(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		try {
			reader = new BufferedReader(new FileReader(fileLoc));
			System.out.println("----- AS ARRAY ----------");
			parseAsArray(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static BufferedReader call_saxLike(BufferedReader reader,
			final String fileLoc) {
		try {
			reader = new BufferedReader(new FileReader(fileLoc));
			System.out.println("------ SAX_LIKE -----");
			sax_like(reader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return reader;
	}

}

class KeyFinder implements ContentHandler{
	  private Object value;
	  private boolean found = false;
	  private boolean end = false;
	  private String key;
	  private String matchKey;
	        
	  public void setMatchKey(String matchKey){
	    this.matchKey = matchKey;
	  }
	        
	  public Object getValue(){
	    return value;
	  }
	        
	  public boolean isEnd(){
	    return end;
	  }
	        
	  public void setFound(boolean found){
	    this.found = found;
	  }
	        
	  public boolean isFound(){
	    return found;
	  }
	        
	  public void startJSON() throws ParseException, IOException {
	    found = false;
	    end = false;
	  }

	  public void endJSON() throws ParseException, IOException {
	    end = true;
	  }

	  public boolean primitive(Object value) throws ParseException, IOException {
		  	this.value = value;
		  	System.out.println("primitive:"+value);
	        return false;
	    
	  }

	  public boolean startArray() throws ParseException, IOException {
		System.out.println("startArray");  
	    return true;
	  }

	        
	  public boolean startObject() throws ParseException, IOException {
	    return true;
	  }

	  public boolean startObjectEntry(String key) throws ParseException, IOException {
	    this.key = key;
	    System.out.println("startObjectEntry:"+key);
	    return true;
	  }
	        
	  public boolean endArray() throws ParseException, IOException {
		System.out.println("KeyFinder.endArray()");  
	    return false;
	  }

	  public boolean endObject() throws ParseException, IOException {
	    return true;
	  }

	  public boolean endObjectEntry() throws ParseException, IOException {
	    return true;
	  }
	}
