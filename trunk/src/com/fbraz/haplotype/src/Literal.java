package com.fbraz.haplotype.src;

public class Literal {
	
	public static final Boolean NEGADO = Boolean.TRUE;
	public static final Boolean NORMAL = Boolean.FALSE;
	
	public Integer var;
	public Boolean fase;
	
	public Literal() {
		
	}
	
	public static Literal getLiteralNegado(Integer var) {
		return new Literal(var, NEGADO);
	}
	
	public static Literal getLiteralNormal(Integer var) {
		return new Literal(var, NORMAL);
	}	
	
	public Literal(Integer var, Boolean fase) {
		this.var = var;
		this.fase = fase;
	}
	
	public String toString() {
		return (fase.equals(NEGADO)?"-":" ")+var;
	}
	
	public String getBinary() {
		return (fase.equals(NEGADO)?"0":"1");
	}

}
