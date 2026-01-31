package br.com.labmax.guntherPDF;

import javax.swing.SwingUtilities;

import br.com.labmax.guntherPDF.visao.Visualizador;

public class GuntherPDF {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		SwingUtilities.invokeLater(() -> {
			new Visualizador().setVisible(true);
		});
	}

}
