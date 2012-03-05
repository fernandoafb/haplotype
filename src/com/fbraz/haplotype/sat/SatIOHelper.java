package com.fbraz.haplotype.sat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.fbraz.haplotype.hapinf.HapProperties;
import com.fbraz.haplotype.misc.IOHelper;
import com.fbraz.haplotype.sat.tad.Clausula;

public class SatIOHelper extends IOHelper {

	@Override
	public ArrayList<String> readInput(String inputFilename) {
		if (inputFilename == null) return null; 
		if (inputFilename.isEmpty()) return null;
		ArrayList<String> lines = new ArrayList<String>();		
		try {
			FileReader fileReader = new FileReader(new File(inputFilename));
			BufferedReader buffReader = new BufferedReader(fileReader);
			String line = buffReader.readLine();
			while (line != null && !line.equals("")) {
				lines.add(line.trim());
				line = buffReader.readLine();
			}
			buffReader.close();
			fileReader.close();
		}
		catch (Exception e) {
			System.out.println("Failure to handle input file.");
			e.printStackTrace();
		}
		return lines;
	}
	
	public void createCNFInstance(String outputFilename, ArrayList<Clausula> clauses, int numVar) {
		System.out.println("Creating SAT CNF instance... ");		
		if (outputFilename == null) return;
		if (outputFilename.isEmpty()) return;
		try {
			FileWriter fileWriter = new FileWriter(new File(outputFilename));
			PrintWriter printWriter = new PrintWriter(fileWriter,true);
			printWriter.println("c cnf generated file for haplotype inference");
			printWriter.println("c author: Fernando Augusto Fernandes Braz");
			printWriter.println("c contact: fernando.afb@gmail.com");
			printWriter.println("p cnf "+(numVar-1)+" "+clauses.size());
			for (int i = 0; i < clauses.size(); i++)
				printWriter.println(clauses.get(i));
			System.out.println("SAT CNF instance file created: "+outputFilename);			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error to create SAT CNF instance file");
		}  
		System.out.println();
	}			
	
	private boolean satSolverSanityCheck(HapProperties hapProperties) {
		if (hapProperties.getSatSolverPath() == null) return false;
		if (hapProperties.getSatSolverPath().trim().equals("")) return false;
		if (hapProperties.getOutputFilename() == null) return false;
		if (hapProperties.getOutputFilename().trim().equals("")) return false;
		if (hapProperties.getSatSolverOutputFilename() == null) return false;
		if (hapProperties.getSatSolverOutputFilename().trim().equals("")) return false;		
		return true;
	}
	
	private String getSatSolverCommand(HapProperties hapProperties) {
		String command = hapProperties.getSatSolverPath()+" ./"+hapProperties.getOutputFilename();
		if (hapProperties.getSatSolverPath().toLowerCase().contains("minisat")) {
			command = command+" ./"+hapProperties.getSatSolverOutputFilename();
		}		
		return command;
	}
	
	private String processSatSolver(String command) throws IOException {
		String solverOutput = null;
		Process p = Runtime.getRuntime().exec(command);
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String line = null;
		while ( (line = br.readLine()) != null) {
			if (line.length() > 0 && line.charAt(0) == 'v') {
				solverOutput = line; 
			}
			System.out.println(line);
		}
		System.out.println();		
		return solverOutput;
	}
	
	private String outputSatSolver(HapProperties hapProperties) throws IOException {
		String solverOutput = null;
		if (hapProperties.getSatSolverPath().toLowerCase().contains("minisat")) {
			FileReader fileReader = new FileReader(new File(hapProperties.getOutputFilename()));
			BufferedReader leitor = new BufferedReader(fileReader);
			String linha = leitor.readLine().trim();
			while (linha != null && !linha.equals("")) {
				if (!linha.toLowerCase().contains("sat")) {
					solverOutput = linha;
				}
				linha = leitor.readLine();
			}
			leitor.close();
			fileReader.close();
		}
		System.out.println("SAT solver result: "+hapProperties.getSatSolverOutputFilename());
		return solverOutput;
	}
	
	public String callSatSolver(HapProperties hapProperties) {		
		if (!satSolverSanityCheck(hapProperties)) return null;
		String command = getSatSolverCommand(hapProperties);
		String solverOutput = null;
		try {
			solverOutput = processSatSolver(command);
			String aux = outputSatSolver(hapProperties);
			if (aux != null) {
				solverOutput = aux;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error in SAT solver execution");
		}
		System.out.println();	
		return solverOutput;
	}		
	
}
