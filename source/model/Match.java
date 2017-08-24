package model;

public class Match {
	
	public Team squadra1 = new Team();
	public Team squadra2 = new Team();
	
	public class Team {
		
		public double parziale;
		public double fattoreCampo;
		public double modPortiere;
		public double modDifesa;
		public double modCentrocampo;
		public double modAttacco;
		public double modSpeciale1;
		public double modSpeciale2;
		public double modSpeciale3;
		public double modModulo;
		public int numeroGol;
		
		public double getTotale() {
			return
				parziale+
				fattoreCampo+
				modPortiere+
				modDifesa+
				modCentrocampo+
				modAttacco+
				modSpeciale1+
				modSpeciale2+
				modSpeciale3+
				modModulo;
		}
	}
}