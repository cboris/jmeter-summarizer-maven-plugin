package com.github.cboris.jmeter;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.json.simple.JSONValue;
import org.slf4j.impl.StaticLoggerBinder;




@Mojo(name = "summary")
public class SummarizerMojo extends AbstractMojo {

	private Map<String,Object> summaryKeys = new HashMap<String,Object>();
   
	private List<ReportHandler> handlers = new ArrayList<ReportHandler>();

    @Parameter(defaultValue = "${project.build.directory}/jmeter")
    File workingDirectory;
    
    @Parameter(required = true)
    String keyTransaction;
    
    @Parameter(defaultValue = "summary")
    String outputName;

    @Component
    MavenProject mavenProject;

    @Component
    MavenSession mavenSession;

    @Component
    BuildPluginManager pluginManager;

    @Component
    PluginDescriptor plugin;

    
    public SummarizerMojo() {
		handlers.add(new SummariesHandler());
		handlers.add(new GraphiteCPUHandler());
	}
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
       
    	// bind slf4j to maven log
    	StaticLoggerBinder.getSingleton().setLog(getLog());



        File resultsDir = new File(workingDirectory.getAbsolutePath() + File.separator + "results");
        
        for (File file :resultsDir.listFiles() ) {
        	for ( ReportHandler handler : handlers) {
        		if (handler.handlesKey(file)) {
        			handler.processFile(file);
        		}
        	}
        }
        
        
        // fix to have target path in the context
        File topDir = new File(workingDirectory.getAbsolutePath() + File.separator + "..");
        
        for (File file :topDir.listFiles() ) {
        	for ( ReportHandler handler : handlers) {
        		if (handler.handlesKey(file)) {
        			handler.processFile(file);
        		}
        	}
        }
        
        try {
			persistSummaries(resultsDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	private void persistSummaries(File resultsDir) throws IOException {
		
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(new File(resultsDir,outputName+".json")));
			
		
			out.append(JSONValue.toJSONString(summaryKeys));
			
			
		} finally {
			if (out != null)
				out.close();
		}
		
		
		
		
	}

	private void readSummariesFromCSVReport(File file) throws IOException {
		
			final int COLUMN_90PERC = 5;
			final String KEY_90PERC = "90_percentiles";
					
			final int COLUMNS_TPS 	= 8;
			final String KEY_TPS 	= "TPS_rate";
		
			BufferedReader in = null;
			
			try {
				in = new BufferedReader(new FileReader(file));
				String line = null;
				while (( line = in.readLine()) != null) {
					if (line.contains(keyTransaction)) {
						String[] elements = line.split(",");
						
						String ninetee_perc = elements[COLUMN_90PERC-1];
						summaryKeys.put(KEY_90PERC, ninetee_perc);
						
						String tps_rate = elements[COLUMNS_TPS];
						summaryKeys.put(KEY_TPS, tps_rate);
						
					}
				}
			} finally  {
				if (in != null) {
					in.close();
				}
			}
			
		
	}

  
	abstract class ReportHandler {
		abstract boolean handlesKey(File file);
		abstract void processFile(File file) throws MojoExecutionException;
	}
    
	class SummariesHandler extends ReportHandler {

		@Override
		boolean handlesKey(File file) {
			
			return file.getAbsolutePath().endsWith("Summary.csv");
		}

		@Override
		void processFile(File file) throws MojoExecutionException {
			try {
				readSummariesFromCSVReport(file);
			} catch (IOException e) {
				throw new MojoExecutionException("Error reading summaries CSV file", e);
				
			}
			
		}}
	

   class GraphiteCPUHandler extends ReportHandler {

	@Override
	boolean handlesKey(File file) {
		
		return file.getAbsolutePath().endsWith("-CPU.json");
	}

	@Override
	void processFile(File file) throws MojoExecutionException {
		try {
			Double value = GraphiteSummarizer.averageValue(file);
			summaryKeys.put("Avg.CPU", value);
		} catch (IOException e) {
			throw new MojoExecutionException("Error reading JSON CPU file", e);
		}
		
	}}
   
}
