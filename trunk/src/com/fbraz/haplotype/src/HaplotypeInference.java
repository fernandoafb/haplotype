package com.fbraz.haplotype.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class HaplotypeInference {
	
	private static final String propertiesFilePath = "config/propriedades";
	
	private String inputFilename;
	private String outputFilename;
	
	private String satSolverPath;
	private String satSolverOutputFilename;
	
	private Integer sequenceSize;
	private ArrayList<GeneSequence> genotypes;
	
	private Integer lowerBound;
	private Integer upperBound;
	private Integer consideratedHaplotypes;
	private ArrayList<GeneSequence> haplotypes;
	
	private int numVar=1;
	//private int getVar() { return numVar++; }
	private ArrayList<Clausula> clausulas;
	
	private String solverOutput;
	
	public HaplotypeInference() {
		haplotypes = new ArrayList<GeneSequence>();		
		genotypes = new ArrayList<GeneSequence>();
		clausulas = new ArrayList<Clausula>();
	}
	
	public void readInput() {
		if (inputFilename.isEmpty()) {
			return;
		}
		try {
			FileReader fileReader = new FileReader(new File(inputFilename));
			BufferedReader leitor = new BufferedReader(fileReader);
			String linha = leitor.readLine();
			ArrayList<String> gs = new ArrayList<String>();
			while (linha != null && !linha.equals("")) {
				linha = linha.trim();
				gs.add(linha);
				linha = leitor.readLine();
			}
			Collections.sort(gs);
			for (String string : gs) {
				GeneSequence g = new GeneSequence(string,numVar);
				numVar += 3*string.length(); // 3 vezes porque preciso de vars, gA e gB
				sequenceSize = string.length();
				genotypes.add(g);				
			}
			leitor.close();
			fileReader.close();
		}
		catch (Exception e) {
			System.out.println("Falha ao manipular arquivo de entrada.");
			e.printStackTrace();
		}
	}
	
	private void selectorVariables() {
		for (int i = 0; i < genotypes.size(); i++) {
			genotypes.get(i).selectorVariable(consideratedHaplotypes,numVar);
			numVar += 2*consideratedHaplotypes; // SA e SB
		}	
	}
	
	private void additionalVariables() {
		for (int i = 0; i < genotypes.size(); i++) {
			genotypes.get(i).additionalVariables(numVar);
			numVar += 2*consideratedHaplotypes; // VA e VB
		}	
	}	
	
	private void generateHaplotypesVars() {
		for (int i = 0; i < consideratedHaplotypes; i++) {
			GeneSequence g = new GeneSequence(sequenceSize, numVar);
			numVar += sequenceSize; // H
			haplotypes.add(g);
		}
	}
	
	private void cnfTransformRule1(GeneSequence g, int j) {
		for (int k = 0; k < consideratedHaplotypes; k++) {
			Clausula cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			clausulas.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));
			clausulas.add(cls);
		}
	}
	
	private void cnfTransformRule2(GeneSequence g, int j) {
		for (int k = 0; k < consideratedHaplotypes; k++) {
			Clausula cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			clausulas.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));
			clausulas.add(cls);
		}		
	}
	
	private void cnfTransformRule3(GeneSequence g, int j) {
		Clausula cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNormal(g.gA.get(j)));
		cls.addLiteral(Literal.getLiteralNormal(g.gB.get(j)));
		clausulas.add(cls);
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNegado(g.gA.get(j)));
		cls.addLiteral(Literal.getLiteralNegado(g.gB.get(j)));
		clausulas.add(cls);		
	}
	
	private void cnfTransformRule4(GeneSequence g, int j) {
		for (int k = 0; k < consideratedHaplotypes; k++) {
			Clausula cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.gA.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			clausulas.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNormal(g.gA.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));						
			clausulas.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.gB.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));						
			clausulas.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNormal(g.gB.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));						
			clausulas.add(cls);
		}			
	}
	
	// simplified adder circuit
	private void cnfTransformRule6(GeneSequence g, int j) {
		// A
		Clausula cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNormal(g.selectorSetA.get(0)));
		cls.addLiteral(Literal.getLiteralNegado(g.additionalA.get(0)));
		clausulas.add(cls);
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(0)));
		cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(0)));
		clausulas.add(cls);		
		// B
		cls.addLiteral(Literal.getLiteralNormal(g.selectorSetB.get(0)));
		cls.addLiteral(Literal.getLiteralNegado(g.additionalB.get(0)));
		clausulas.add(cls);
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(0)));
		cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(0)));
		clausulas.add(cls);			
		
		for (int k = 0; k < consideratedHaplotypes; k++) {
			// A
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.additionalA.get(k)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			clausulas.add(cls);
			// B
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.additionalB.get(k)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));
			clausulas.add(cls);			
		}
		
		for (int k = 0; k < consideratedHaplotypes-1; k++) {
			// A
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(g.selectorSetA.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(k)));
			cls.addLiteral(Literal.getLiteralNegado(g.additionalA.get(k+1)));
			clausulas.add(cls);					
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(k+1)));
			clausulas.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.additionalA.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(k+1)));
			clausulas.add(cls);
			// B
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(g.selectorSetB.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(k)));
			cls.addLiteral(Literal.getLiteralNegado(g.additionalB.get(k+1)));
			clausulas.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(k+1)));
			clausulas.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.additionalB.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(k+1)));
			clausulas.add(cls);			
		}
		
		// A
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(consideratedHaplotypes-1)));
		clausulas.add(cls);	
		// B
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(consideratedHaplotypes-1)));
		clausulas.add(cls);				
	}
	
	private void cnfGenotype(GeneSequence g, int j, Boolean fase) {
		Clausula cls = new Clausula();
		if (fase.equals(Literal.NEGADO)) {
			cls.addLiteral(Literal.getLiteralNegado(g.vars.get(j)));
		}
		else if (fase.equals(Literal.NORMAL)) {
			cls.addLiteral(Literal.getLiteralNormal(g.vars.get(j)));
		}
		else {
			return;
		}
		clausulas.add(cls);		
	}
	
	private void applyCnfTransformations(GeneSequence g, int j) {
		if (g.genes.get(j).equals(0)) {
			cnfGenotype(g,j,Literal.NEGADO);
			cnfTransformRule1(g,j);
		}
		else if (g.genes.get(j).equals(1)) {
			cnfGenotype(g,j,Literal.NORMAL);
			cnfTransformRule2(g,j);					
		}
		else if (g.genes.get(j).equals(2)) {
			cnfTransformRule3(g,j);			
			cnfTransformRule4(g,j);
			cnfTransformRule6(g,j);
		}		
	}
	
	private void cnfBreakingSymmetry() {
		// boolean comparator circuit
		// between hk and hk+1, 1<= k < r, requiring hk < hk+1
		// valuation of hk
	}

	/**
	 * CNF TRANSFORM (5)
	 * não precisa fazer, o (6) o substitui
	 * CNF TRANSFORM (6) (alternativo ao 5)
	 */
	private void generateGenotypes() {
		for (GeneSequence g : genotypes) {
			for (int j = 0; j < g.genes.size(); j++) {
				applyCnfTransformations(g,j);
			}
		}
		// optimizations
		// breaking symmetry
		cnfBreakingSymmetry();
	}
	
	public void printOutput() {
		if (getOutputFilename() == null) return;
		if (getOutputFilename().isEmpty()) return;
		try {
			FileWriter writer = new FileWriter(new File(getOutputFilename()));
			PrintWriter saida = new PrintWriter(writer,true);
			saida.println("c cnf generated file for haplotype inference");
			saida.println("c author: Fernando Augusto Fernandes Braz");
			saida.println("c contact: fernando.afb@gmail.com");
			saida.println("p cnf "+(this.numVar-1)+" "+clausulas.size());
			for (int i = 0; i < clausulas.size(); i++)
				saida.println(clausulas.get(i));
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}		
	
	private void callSatSolver() {
		if (this.getSatSolverPath() == null) return;
		if (this.getSatSolverPath().trim().equals("")) return;
		if (this.getOutputFilename() == null) return;
		if (this.getOutputFilename().trim().equals("")) return;
		if (this.getSatSolverOutputFilename() == null) return;
		if (this.getSatSolverOutputFilename().trim().equals("")) return;					
		
		String command = this.getSatSolverPath()+" ./"+this.getOutputFilename();
		if (this.getSatSolverPath().toLowerCase().contains("minisat")) {
			command = command+" ./"+this.getSatSolverOutputFilename();
		}
		solverOutput = null;
		try {
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
			if (this.getSatSolverPath().toLowerCase().contains("minisat")) {
				FileReader fileReader = new FileReader(new File(getSatSolverOutputFilename()));
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void matchHaplotypes() {
		if (solverOutput == null) {
			System.out.println("Não foi possível achar haplótipos.");
			return;
		}
		System.out.println("Haplotypes");
		for (int k = 0; k < haplotypes.size(); k++) {
			GeneSequence haplotype = haplotypes.get(k);
			for (int i = 0; i < haplotype.vars.size(); i++) {
				Integer var = haplotype.vars.get(i);
				int a = solverOutput.indexOf(""+var+" ");
				if (a != -1) {
					Literal lit = new Literal();
					lit.var = haplotype.vars.get(i);
					lit.fase = solverOutput.charAt(a-1) == '-';
					haplotype.values.add(lit);
				}
			}
		}
		for (int k = 0; k < haplotypes.size(); k++) {
			System.out.println(haplotypes.get(k).printValues()+" ("+haplotypes.get(k).printLiterais()+")");
		}
	}

	public static void main(String[] args) {
		System.out.println("Haplotype Inference");
		System.out.println("Author: Fernando Augusto Fernandes Braz");
		System.out.println("Contact: fbraz@dcc.ufmg.br");
		System.out.println();
		
		if (args.length <= 0) {
			System.out.println("Erro: fornecer arquivo de entrada h[n].h como parâmetro.");
			return;
		}
		
		HaplotypeInference hapInf = new HaplotypeInference();
		hapInf.loadProperties();
		hapInf.setFilenames(args[0]);
		System.out.println("Arquivo de entrada fornecido: "+hapInf.getInputFilename());
		
		hapInf.readInput();
		if (hapInf.genotypes.size() > 0) {
			
			hapInf.computeBounds();
			hapInf.generateHaplotypesVars();
			hapInf.selectorVariables();
			hapInf.additionalVariables();
			
			hapInf.generateGenotypes();
			System.out.println("Gerando instância SAT CNF... ");
			hapInf.printOutput();
			System.out.println("Arquivo com instância SAT CNF gerada: "+hapInf.getOutputFilename());
			
			System.out.println();
			hapInf.callSatSolver();
			System.out.println("Resultado do solucionador SAT: "+hapInf.getSatSolverOutputFilename());
			
			System.out.println();
			hapInf.matchHaplotypes();
			
			System.out.println();
			hapInf.explainGenotypes();
		}
		else {
			System.out.println("Nenhum genótipo fornecido.");
		}
	}
	
	private void computeBounds() {
		setLowerBound(1);
		setUpperBound(2*genotypes.size()); // trivial
		setConsideratedHaplotypes(getUpperBound());
	}

	private void explainGenotypes() {
		System.out.println("Genotype Explanation");
		for (GeneSequence genotype : genotypes) {
			int indexHaplotypeA = -1;
			for (int i = 0; i < genotype.selectorSetA.size(); i++) {
				String str = " "+genotype.selectorSetA.get(i)+" ";
				if (solverOutput.contains(str)) {
					indexHaplotypeA = i;
				}
			}
			int indexHaplotypeB = -1;
			for (int i = 0; i < genotype.selectorSetB.size(); i++) {
				String str = " "+genotype.selectorSetB.get(i)+" ";
				if (solverOutput.contains(str)) {
					indexHaplotypeB = i;
				}
			}
			System.out.print(genotype+" [");
			if (indexHaplotypeA != -1 && haplotypes.size() >= indexHaplotypeA && haplotypes.get(indexHaplotypeA) != null)
				System.out.print(haplotypes.get(indexHaplotypeA).printValues());
			System.out.print("/");
			if (indexHaplotypeB != -1 && haplotypes.size() >= indexHaplotypeB && haplotypes.get(indexHaplotypeB) != null)
				System.out.print(haplotypes.get(indexHaplotypeB).printValues());
			System.out.println("]");
		}
	}

	private void setFilenames(String string) {
		setInputFilename(string);
		int a = string.lastIndexOf("/");
		int b = string.lastIndexOf(".");
		if (b-a > 0) {
			setOutputFilename(string.substring(0,b)+".cnf");
			setSatSolverOutputFilename(string.substring(0,b)+".out");
		}
		else {
			setOutputFilename(string+".cnf");
			setSatSolverOutputFilename(string+".out");
		}
	}

	private void loadProperties() {
		try {
			FileReader fileReader = new FileReader(new File(propertiesFilePath));
			BufferedReader leitor = new BufferedReader(fileReader);
			
			String linha = leitor.readLine();
			
			while (linha != null && !linha.trim().equals("")) {
				String[] tokens = linha.trim().split("=");
				if (tokens.length >= 2 && tokens[0].equals("satsolver") && !tokens[1].equals("")) {
					this.setSatSolverPath(tokens[1]);
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
	public Integer getConsiderateHaplotypes() {
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

}
