package com.github.cboris.jmeter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



import org.junit.Test;
import org.junit.BeforeClass;
import org.slf4j.impl.StaticLoggerBinder;

import com.github.cboris.jmeter.GraphiteSummarizer.AggregatedAvg;

public class GraphiteSummarizerTest {

	@BeforeClass
	public static void beforeClass() {
		// bind slf4j to maven log
    	//StaticLoggerBinder.getSingleton().setLog(new SimpleLo);
	}
	
	@Test
	public void testAverageValue() throws IOException {
		InputStream is = getClass().getResourceAsStream("/author-home-perf-CPU.json");
		InputStreamReader isr =new InputStreamReader(is);
		AggregatedAvg avg = GraphiteSummarizer.readDatapoints(isr);
		assertEquals(86.41666666666667f, avg.avg(), 0.001f); 
		
		
	}

}
