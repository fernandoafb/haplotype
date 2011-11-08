package com.fbraz.haplotype.src;

import java.util.ArrayList;

public class Clausula {
	
	private ArrayList<Literal> literais; 
	
	public Clausula() {
		literais = new ArrayList<Literal>();
	}
	
	public void addLiteral(Literal l) {
		literais.add(l);
	}
	
	public Integer numLiterais() {
		return literais.size();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Literal l : literais)  {
			sb.append(l.toString()+" ");
		}
		sb.append("0"); // marca o fim de uma cláusula
		return sb.toString();
	}

}
