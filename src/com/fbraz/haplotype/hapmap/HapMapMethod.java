package com.fbraz.haplotype.hapmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.fbraz.haplotype.hapinf.HapProperties;
import com.fbraz.haplotype.hapinf.InferenceMethod;

public class HapMapMethod extends InferenceMethod {
	
	public HapMapMethod(HapProperties hapProperties) {
		super(hapProperties);
	}
	
	public static void mapToSat(HapProperties hapProperties) {
		if (hapProperties.getInputFilename().isEmpty()) {
			return;
		}
		try {
			FileReader fileReader = new FileReader(new File(hapProperties.getInputFilename()));
			BufferedReader leitor = new BufferedReader(fileReader);
			String linha = leitor.readLine();
			String[] headers = linha.split(" ");
			for (int i = 0; i < headers.length; i++) {
				hapProperties.getHeaders().add(headers[i]);
			}
			linha = leitor.readLine();
			while (linha != null && !linha.equals("")) {
				linha = linha.trim();
				
				linha = leitor.readLine();
			}
			leitor.close();
			fileReader.close();
		}
		catch (Exception e) {
			System.out.println("Falha ao manipular arquivo de entrada.");
			e.printStackTrace();
		}		
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}	

}
