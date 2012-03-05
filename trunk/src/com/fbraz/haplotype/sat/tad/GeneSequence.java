package com.fbraz.haplotype.sat.tad;

import java.util.ArrayList;

public class GeneSequence {
	
	public int numSites;
	
	public ArrayList<Integer> genes;
	public ArrayList<Integer> vars;
	public ArrayList<Literal> values;

	public ArrayList<Integer> gA;
	public ArrayList<Integer> gB;
	
	public ArrayList<Integer> selectorSetA;
	public ArrayList<Integer> selectorSetB;
	
	public ArrayList<Integer> additionalA;
	public ArrayList<Integer> additionalB;	
	
	private Integer consideratedHaplotypes;
	
	public GeneSequence(Integer seqSize) {
		if (seqSize > 0) {
			genes = new ArrayList<Integer>();
			vars = new ArrayList<Integer>();
			gA = new ArrayList<Integer>();
			gB = new ArrayList<Integer>();
			values = new ArrayList<Literal>();
		}
	}
	
	public GeneSequence(String seq, Integer v) {
		numSites = seq.length();
		if (numSites > 0) {
			genes = new ArrayList<Integer>();
			vars = new ArrayList<Integer>();
			gA = new ArrayList<Integer>();
			gB = new ArrayList<Integer>();
			values = new ArrayList<Literal>();
			for (int i = 0; i < seq.length(); i++) {
				try{
					genes.add(Integer.valueOf(""+seq.charAt(i)));
				}
				catch(NumberFormatException nfex) {
					genes.add(2);
				}
				vars.add(v++);
				gA.add(v++);
				gB.add(v++);
			} 
		}
	}
	
	public GeneSequence(Integer m, Integer v) {
		numSites = m;
		if (numSites > 0) {
			genes = new ArrayList<Integer>();
			vars = new ArrayList<Integer>();
			gA = new ArrayList<Integer>();
			gB = new ArrayList<Integer>();	
			values = new ArrayList<Literal>();
			for (int i = 0; i < m; i++) {
				vars.add(v++);
			} 
		}
	}	
	
	public void selectorVariable(Integer c, Integer v) {
		consideratedHaplotypes = c;
		selectorSetA = new ArrayList<Integer>();
		selectorSetB = new ArrayList<Integer>();
		for (int i = 0; i < consideratedHaplotypes; i++)
			selectorSetA.add(v++);
		for (int j = 0; j < consideratedHaplotypes; j++)
			selectorSetB.add(v++);			
	}
	
	public void additionalVariables(Integer v) {
		additionalA = new ArrayList<Integer>();
		additionalB = new ArrayList<Integer>();
		for (int i = 0; i < consideratedHaplotypes; i++)
			additionalA.add(v++);
		for (int j = 0; j < consideratedHaplotypes; j++)
			additionalB.add(v++);			
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Integer gene : genes)
			sb.append(gene);
		return sb.toString();
	}
	
	public String printLiterais() {
		StringBuffer sb = new StringBuffer();
		for (Literal lit : values)
			sb.append(lit+" ");
		return sb.substring(0,sb.length() > 0 ? sb.length()-1 : 0).toString();
	}
	
	public String printValues() {
		StringBuffer sb = new StringBuffer();
		for (Literal lit : values)
			sb.append(lit.getBinary());
		return sb.toString();
	}
	
	
}
