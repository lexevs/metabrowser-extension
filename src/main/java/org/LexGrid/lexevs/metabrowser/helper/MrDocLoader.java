package org.LexGrid.lexevs.metabrowser.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MrDocLoader {
	
	private static String MRDOC_FILE = "/MRDOC.RRF";
	
	private static String RELA = "RELA";
	private static String RELA_INVERSE = "rela_inverse";

	public static void main(String[] args){
		MrDocLoader l = new MrDocLoader();
		Map<String,String> map = l.getRelasAndReverseRelas();
		System.out.println(map);
	}
	
	public Map<String,String> getRelasAndReverseRelas(){
		Map<String,String> relasAndInverses = new HashMap<String,String>();
		List<String> lines = readText(MRDOC_FILE);
		
		for(String line : lines){
			String[] tokens = line.split("\\|");
			if(tokens[0].equals(RELA) &&
					tokens[2].equals(RELA_INVERSE) && tokens.length > 3){
				relasAndInverses.put(tokens[1], tokens[3]);
			}
		}
		
		return relasAndInverses;
	}

	private List<String> readText(String file) {
		InputStream is = null;
		BufferedReader br = null;
		String line;
		ArrayList<String> list = new ArrayList<String>();

		try { 
			is = this.getClass().getResourceAsStream(file);
			br = new BufferedReader(new InputStreamReader(is));
			while (null != (line = br.readLine())) {
				list.add(line);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null) br.close();
				if (is != null) is.close();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return list;
	}
}
