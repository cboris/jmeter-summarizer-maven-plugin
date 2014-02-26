package com.github.cboris.jmeter;

import java.io.BufferedReader;
import java.io.File;
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

public class GraphiteSummarizer {

	
	/**
	 * reads and extracts values from graphite json containing datapoints
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	static AggregatedAvg readDatapoints(Reader reader) throws IOException {
		  
		  JSONParser parser = new JSONParser();
		  ContainerFactory containerFactory = new ContainerFactory(){
		    public List creatArrayContainer() {
		      return new LinkedList();
		    }

		    public Map createObjectContainer() {
		      return new LinkedHashMap();
		    }
		                        
		  };
		  
		  AggregatedAvg avg = null;
		  try{
		    List datapoints = (List)
		    		((Map)
		    				((List)parser.parse(reader, containerFactory))
		    				.get(0))
		    			.get("datapoints");
		    avg = new AggregatedAvg();
		    
		    for ( Object el : datapoints) {
				List l = (List)el;
				Double value = (Double)l.get(0);
				avg.add(value);
		    	//System.out.println(l.get(0).getClass()+": "+l.get(0));
				
			}
		   
		                        
		    
		  }
		  catch(ParseException pe){
		    System.out.println(pe);
		  }
		return avg;
	}
	
	public static Double averageValue(File graphiteJSONFile) throws IOException {
		BufferedReader reader = null;
		AggregatedAvg avg = null;	
		
		try {
			reader = new BufferedReader(new FileReader(graphiteJSONFile));
			avg = readDatapoints(reader);
			
		} finally {
			try {
				if (reader != null)  reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return (avg.avg());
	}
	
	public static void main(String[] args) {
		BufferedReader reader = null;
		
		final String fileLoc = "/Users/bpetrovic/Documents/netcentric/testing/src-netcentric/ubs-fit-perftest/target/jmeter/results/cpu-data.json";
		
		//reader = call_saxLike(reader, fileLoc);
		
		
		
	
		
		try {
			reader = new BufferedReader(new FileReader(fileLoc));
			System.out.println("----- CONTAINER ----------");
			AggregatedAvg avg = readDatapoints(reader);
			System.out.println("==== average is:"+avg.avg()+" ======");
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
		
		
		
	}

	 static class AggregatedAvg {
		private Double sum = 0.0;
		private long count = 0l;
		
		public void add(Double d) {
			this.sum+=d;
			this.count++;
		}
		
		public Double avg() {
			return (sum/count);
		}
		
		
	}

}


