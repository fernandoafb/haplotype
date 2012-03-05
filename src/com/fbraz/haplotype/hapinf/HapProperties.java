package com.fbraz.haplotype.hapinf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.fbraz.haplotype.misc.InputFormat;

public class HapProperties {
	
	private static final String defaultPropertiesFilePath = "config/properties";
	private String propertiesFilePath = defaultPropertiesFilePath;
	
	private InputFormat inputFormat = InputFormat.HAPINF;
	
	private String inputFilename;
	private String outputFilename;
	
	private Integer lowerBound;
	private Integer upperBound;
	private Integer consideratedHaplotypes;	
	
	private String satSolverPath;
	private String satSolverOutputFilename;
	
	private Integer sequenceSize;
	
	private ArrayList<String> headers;
	
	public HapProperties() {
		setHeaders(new ArrayList<String>());
	}
	
	public void generateFilenames(String inputFilename) {
		setInputFilename(inputFilename);
		int lastBar = inputFilename.lastIndexOf("/");
		int lastDot = inputFilename.lastIndexOf(".");
		if (lastDot-lastBar > 0) {
			inputFilename = inputFilename.substring(0,lastDot);
		}
		setOutputFilename(inputFilename+".cnf");
		setSatSolverOutputFilename(inputFilename+".out");
	}
	
	public void loadProperties() {
		try {
			FileReader fileReader = new FileReader(new File(getPropertiesFilePath()));
			BufferedReader leitor = new BufferedReader(fileReader);
			
			String linha = leitor.readLine();
			
			while (linha != null && !linha.trim().equals("")) {
				String[] tokens = linha.trim().split("=");
				if (tokens.length >= 2 && tokens[0].equals("satsolver") && !tokens[1].equals("")) {
					setSatSolverPath(tokens[1]);
				}
				linha = leitor.readLine();
			}
			leitor.close();
			fileReader.close();
		}
		catch (Exception e) {
			System.out.println("Falha ao manipular arquivo de propriedades.");
			e.printStackTrace();
		}
	}		
	
	public String getPropertiesFilePath() {
		return propertiesFilePath;
	}

	public void setPropertiesFilePath(String propertiesFilePath) {
		this.propertiesFilePath = propertiesFilePath;
	}

	public InputFormat getInputFormat() {
		return inputFormat;
	}

	public void setInputFormat(InputFormat inputFormat) {
		this.inputFormat = inputFormat;
	}

	public String getInputFilename() {
		return inputFilename;
	}
	
	public void setInputFilename(String inputFilename) {
		this.inputFilename = inputFilename;
	}
	
	public String getOutputFilename() {
		return outputFilename;
	}
	
	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}	
	
	public Integer getLowerBound() {
		return lowerBound;
	}
	
	public void setLowerBound(Integer lowerBound) {
		this.lowerBound = lowerBound;
	}
	
	public Integer getUpperBound() {
		return upperBound;
	}
	
	public void setUpperBound(Integer upperBound) {
		this.upperBound = upperBound;
	}	
	
	public Integer getConsideratedHaplotypes() {
		return consideratedHaplotypes;
	}
	
	public void setConsideratedHaplotypes(Integer consideratedHaplotypes) {
		this.consideratedHaplotypes = consideratedHaplotypes;
	}	
	
	public String getSatSolverPath() {
		return satSolverPath;
	}
	
	public void setSatSolverPath(String satSolverPath) {
		this.satSolverPath = satSolverPath;
	}
	
	public String getSatSolverOutputFilename() {
		return satSolverOutputFilename;
	}
	
	public void setSatSolverOutputFilename(String satSolverOutputFilename) {
		this.satSolverOutputFilename = satSolverOutputFilename;
	}

	public Integer getSequenceSize() {
		return sequenceSize;
	}

	public void setSequenceSize(Integer sequenceSize) {
		this.sequenceSize = sequenceSize;
	}

	public ArrayList<String> getHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}	

}
