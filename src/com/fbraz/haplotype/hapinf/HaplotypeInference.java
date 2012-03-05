package com.fbraz.haplotype.hapinf;

import java.util.ArrayList;

import com.fbraz.haplotype.hapmap.HapMapMethod;
import com.fbraz.haplotype.misc.InputFormat;
import com.fbraz.haplotype.sat.SatMethod;


public class HaplotypeInference {
	
	private InferenceMethod method;
	private HapProperties hapProperties;
	
	public HaplotypeInference() {
		hapProperties = new HapProperties();
	}
	
	public HapProperties getHapProperties() {
		return hapProperties;
	}

	public void setHapProperties(HapProperties hapProperties) {
		this.hapProperties = hapProperties;
	}	
	
	private static void printProgramInfo() {
		System.out.println("Haplotype Inference Tool");
		System.out.println("Author: Fernando Augusto Fernandes Braz");
		System.out.println("Contact: fbraz@dcc.ufmg.br");
		System.out.println();
	}
	
	private static void printUsageInfo() {
		System.out.println("Usage: java -jar hapinf.jar [OPTION]... [FILE]");
		System.out.println("Finds the minimum number of haplotypes that " + 
		                   "explain the genotypes listed in [FILE]");	
		System.out.println();
		
		System.out.println("      --k=K \t \t consider K haplotypes");
		System.out.println("  -m, --map \t \t input file in HapMap format");
		System.out.println("  -s, --sat \t \t input file in HapInf format (default)");
	}
	
	private void loadProperties() {
		hapProperties.loadProperties();
	}	
	
	private int parseOptions(String[] args) {
		int error = 0;
		int inputIndex = -1;
		ArrayList<String> argsList = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			
			if (args[i] == null) continue;
			
			argsList.add(args[i]);		
			
			if (!args[i].startsWith("-")) {
				inputIndex = i;
				continue;
			}
			
			if (args[i].startsWith("--k")) {
				String[] a = args[i].split("=");
				if (a.length > 1) {
					int b = 0;
					try {
						b = Integer.parseInt(a[1]);
						hapProperties.setConsideratedHaplotypes(b);
					}
					catch (Exception e) {
						error = 1;
					}
				}
				else {
					error = 1;
				}
				continue;
			}	
			
			if (args[i].equals("-m") || args[i].equals("--map")) {
				hapProperties.setInputFormat(InputFormat.HAPMAP);
				continue;
			}
			else if (args[i].equals("-s") || args[i].equals("--sat")) {
				hapProperties.setInputFormat(InputFormat.HAPINF);
				continue;
			}				
			
		}	
		
		if (inputIndex != -1) {
			hapProperties.generateFilenames(args[inputIndex]);
		}
		else {
			error = 1;
		}
		return error;
	}
	
	private void execute() {
		InputFormat inputFormat = getHapProperties().getInputFormat();
		if (inputFormat.equals(InputFormat.HAPINF)) {
			method = new SatMethod(hapProperties);
		}
		else if (inputFormat.equals(InputFormat.HAPMAP)) {
			HapMapMethod.mapToSat(getHapProperties());
			method = new SatMethod(hapProperties);
		}
		else if (inputFormat.equals(InputFormat.PHASE)) {
			// TODO
		}		
		method.execute();			
	}

	public static void main(String[] args) {
		printProgramInfo();
		
		if (args.length <= 0) {
			printUsageInfo();
			return;
		}
		
		HaplotypeInference hapInf = new HaplotypeInference();
		hapInf.loadProperties();
		int error = hapInf.parseOptions(args);
		
		if (hapInf.getHapProperties().getInputFilename() != null && error == 0) {
			System.out.println("Input filename given: "+hapInf.getHapProperties().getInputFilename());
		}
		else {
			printUsageInfo();
			return;
		}
		
		hapInf.execute();
	}

}
