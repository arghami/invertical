package main;

public class Modulo {
	public Modulo(double modif, double modifAvv) {
		this.modif = modif;
		this.modifAvv = modifAvv;
	}
	public double modif = 0.0;
	public double modifAvv = 0.0;
	
	@Override
	public String toString() {
		
		return "Mod:"+modif+",modAvv:"+modifAvv;
	}
}
