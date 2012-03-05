package com.fbraz.haplotype.sat;

import java.util.ArrayList;
import java.util.Collections;

import com.fbraz.haplotype.hapinf.HapProperties;
import com.fbraz.haplotype.hapinf.InferenceMethod;
import com.fbraz.haplotype.sat.tad.Clausula;
import com.fbraz.haplotype.sat.tad.GeneSequence;
import com.fbraz.haplotype.sat.tad.Literal;

public class SatMethod extends InferenceMethod {
	
	private ArrayList<GeneSequence> genotypes;
	private ArrayList<GeneSequence> haplotypes;
	
	private int numVar=1;
	private ArrayList<Clausula> clauses;

	private String solverOutput;	
	
	public SatMethod(HapProperties hapProperties) {
		super(hapProperties);
		haplotypes = new ArrayList<GeneSequence>();		
		genotypes = new ArrayList<GeneSequence>();
		clauses = new ArrayList<Clausula>();		
		ioHelper = new SatIOHelper();
	}
	
	public ArrayList<GeneSequence> getGenotypes() {
		return genotypes;
	}

	public void setGenotypes(ArrayList<GeneSequence> genotypes) {
		this.genotypes = genotypes;
	}	

	public ArrayList<GeneSequence> getHaplotypes() {
		return haplotypes;
	}

	public void setHaplotypes(ArrayList<GeneSequence> haplotypes) {
		this.haplotypes = haplotypes;
	}

	public ArrayList<Clausula> getClausulas() {
		return clauses;
	}

	public void setClausulas(ArrayList<Clausula> clausulas) {
		this.clauses = clausulas;
	}

	public String getSolverOutput() {
		return solverOutput;
	}

	public void setSolverOutput(String solverOutput) {
		this.solverOutput = solverOutput;
	}
	
	private void createGenotypes(ArrayList<String> gs) {
		Collections.sort(gs);
		for (String string : gs) {
			GeneSequence g = new GeneSequence(string,numVar);
			// three times because it needs vars, gA and gB			
			numVar += 3*string.length();
			getHapProperties().setSequenceSize(string.length());
			genotypes.add(g);				
		}		
	}
	
	public void execute() {
		createGenotypes(ioHelper.readInput(getHapProperties().getInputFilename()));
		
		if (getGenotypes().size() <= 0) {
			System.out.println("No genotype provided.");
			return;
		}
		
		computeHaplotypeBounds();
		createHaplotypeVars();
		createSelectorVars();
		createAdditionalVars();
		createGenotypes();
		
		((SatIOHelper) ioHelper).createCNFInstance(getHapProperties().getOutputFilename(),getClausulas(),numVar);
		solverOutput = ((SatIOHelper) ioHelper).callSatSolver(getHapProperties());
		
		if (solverOutput != null) {
			matchHaplotypes();
			explainGenotypes();
		}			
	}
	
	private void computeHaplotypeBounds() {
		// trivial lower bound
		getHapProperties().setLowerBound(1);
		// trivial upper bound		
		getHapProperties().setUpperBound(2*genotypes.size());
		
		if (getHapProperties().getConsideratedHaplotypes() == null) { 
			getHapProperties().setConsideratedHaplotypes(getHapProperties().getUpperBound());
		}
	}	
	
	private void createHaplotypeVars() {
		int k = getHapProperties().getConsideratedHaplotypes();
		int s = getHapProperties().getSequenceSize();
		for (int i = 0; i < k; i++) {
			GeneSequence g = new GeneSequence(s, numVar);
			numVar += s; // H
			haplotypes.add(g);
		}
	}
	
	private void createSelectorVars() {
		int k = getHapProperties().getConsideratedHaplotypes();
		for (int i = 0; i < genotypes.size(); i++) {
			genotypes.get(i).selectorVariable(k,numVar);
			numVar += 2*k; // SA e SB
		}	
	}
	
	private void createAdditionalVars() {
		int k = getHapProperties().getConsideratedHaplotypes();		
		for (int i = 0; i < genotypes.size(); i++) {
			genotypes.get(i).additionalVariables(numVar);
			numVar += 2*k; // VA e VB
		}	
	}		
	
	/**
	 * CNF TRANSFORM (5)
	 * não precisa fazer, o (6) o substitui
	 * CNF TRANSFORM (6) (alternativo ao 5)
	 */
	private void createGenotypes() {
		for (GeneSequence g : genotypes) {
			for (int j = 0; j < g.genes.size(); j++) {
				applyCnfTransformations(g,j);
			}
		}
		// optimizations
		// breaking symmetry
		cnfBreakingSymmetry();
	}
	
	private void cnfBreakingSymmetry() {
		// boolean comparator circuit
		// between hk and hk+1, 1<= k < r, requiring hk < hk+1
		// valuation of hk
	}	
	
	private void matchHaplotypes() {
		if (solverOutput == null) {
			System.out.println("Unable to find haplotypes.");
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
		System.out.println();		
	}	
	
	private void explainGenotypes() {
		System.out.println("Genotype Explanation");
		for (GeneSequence genotype : genotypes) {
			int indexHaplotypeA = -1;
			for (int i = 0; i < genotype.selectorSetA.size(); i++) {
				String str = " "+genotype.selectorSetA.get(i)+" ";
				//String strNeg = " -"+genotype.selectorSetA.get(i)+" ";
				if (solverOutput.contains(str)) {// || solverOutput.contains(strNeg)) {
					indexHaplotypeA = i;
				}
			}
			int indexHaplotypeB = -1;
			for (int i = 0; i < genotype.selectorSetB.size(); i++) {
				String str = " "+genotype.selectorSetB.get(i)+" ";
				//String strNeg = " -"+genotype.selectorSetB.get(i)+" ";
				if (solverOutput.contains(str)) {// || solverOutput.contains(strNeg)) {
					indexHaplotypeB = i;
				}
			}
			System.out.print(genotype+" [");
			if (indexHaplotypeA != -1 && haplotypes.size() >= indexHaplotypeA && haplotypes.get(indexHaplotypeA) != null)
				System.out.print(haplotypes.get(indexHaplotypeA).printValues());
			else if (indexHaplotypeB != -1 && haplotypes.size() >= indexHaplotypeB && haplotypes.get(indexHaplotypeB) != null) {
				System.out.println(fixHaplotype(genotype,haplotypes.get(indexHaplotypeB)));
			}
			System.out.print("/");
			if (indexHaplotypeB != -1 && haplotypes.size() >= indexHaplotypeB && haplotypes.get(indexHaplotypeB) != null)
				System.out.print(haplotypes.get(indexHaplotypeB).printValues());
			else if (indexHaplotypeA != -1 && haplotypes.size() >= indexHaplotypeA && haplotypes.get(indexHaplotypeA) != null) {
				System.out.print(fixHaplotype(genotype,haplotypes.get(indexHaplotypeA)));
			}
			System.out.println("]");
		}
	}
	
	private GeneSequence fixHaplotype(GeneSequence genotype, GeneSequence haplotype) {
		GeneSequence fixedHaplotype = new GeneSequence(genotype.numSites);
		int i = 0;
		for (Integer g : genotype.genes) {
			if (g.equals(1) || g.equals(0)) {
				fixedHaplotype.genes.add(g);
			}
			else {
				if (haplotype.values.get(i).fase.equals(Literal.NEGADO)) {
					fixedHaplotype.genes.add(1);
				}
				else {
					fixedHaplotype.genes.add(0);
				}
			}
			i++;
		}
		return fixedHaplotype;
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
	
	private void cnfTransformRule1(GeneSequence g, int j) {
		int c = getHapProperties().getConsideratedHaplotypes();
		for (int k = 0; k < c; k++) {
			Clausula cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			clauses.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));
			clauses.add(cls);
		}
	}
	
	private void cnfTransformRule2(GeneSequence g, int j) {
		int c = getHapProperties().getConsideratedHaplotypes();
		for (int k = 0; k < c; k++) {
			Clausula cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			clauses.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));
			clauses.add(cls);
		}		
	}
	
	private void cnfTransformRule3(GeneSequence g, int j) {
		Clausula cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNormal(g.gA.get(j)));
		cls.addLiteral(Literal.getLiteralNormal(g.gB.get(j)));
		clauses.add(cls);
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNegado(g.gA.get(j)));
		cls.addLiteral(Literal.getLiteralNegado(g.gB.get(j)));
		clauses.add(cls);		
	}
	
	private void cnfTransformRule4(GeneSequence g, int j) {
		int c = getHapProperties().getConsideratedHaplotypes();
		for (int k = 0; k < c; k++) {
			Clausula cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.gA.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			clauses.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNormal(g.gA.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));						
			clauses.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.gB.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));						
			clauses.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(haplotypes.get(k).vars.get(j)));
			cls.addLiteral(Literal.getLiteralNormal(g.gB.get(j)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));						
			clauses.add(cls);
		}			
	}
	
	// simplified adder circuit
	private void cnfTransformRule6(GeneSequence g, int j) {
		// A
		Clausula cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNormal(g.selectorSetA.get(0)));
		cls.addLiteral(Literal.getLiteralNegado(g.additionalA.get(0)));
		clauses.add(cls);
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(0)));
		cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(0)));
		clauses.add(cls);		
		// B
		cls.addLiteral(Literal.getLiteralNormal(g.selectorSetB.get(0)));
		cls.addLiteral(Literal.getLiteralNegado(g.additionalB.get(0)));
		clauses.add(cls);
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(0)));
		cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(0)));
		clauses.add(cls);			
		
		int c = getHapProperties().getConsideratedHaplotypes();
		for (int k = 0; k < c; k++) {
			// A
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.additionalA.get(k)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			clauses.add(cls);
			// B
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.additionalB.get(k)));
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));
			clauses.add(cls);			
		}
		
		for (int k = 0; k < c-1; k++) {
			// A
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(g.selectorSetA.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(k)));
			cls.addLiteral(Literal.getLiteralNegado(g.additionalA.get(k+1)));
			clauses.add(cls);					
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetA.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(k+1)));
			clauses.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.additionalA.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(k+1)));
			clauses.add(cls);
			// B
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNormal(g.selectorSetB.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(k)));
			cls.addLiteral(Literal.getLiteralNegado(g.additionalB.get(k+1)));
			clauses.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.selectorSetB.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(k+1)));
			clauses.add(cls);
			cls = new Clausula();
			cls.addLiteral(Literal.getLiteralNegado(g.additionalB.get(k)));
			cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(k+1)));
			clauses.add(cls);			
		}
		
		// A
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNormal(g.additionalA.get(c-1)));
		clauses.add(cls);	
		// B
		cls = new Clausula();
		cls.addLiteral(Literal.getLiteralNormal(g.additionalB.get(c-1)));
		clauses.add(cls);				
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
		clauses.add(cls);		
	}	

}
