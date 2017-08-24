package fcm;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import main.Regole;
import model.Fascia;
import model.Match;
import model.Match.Team;
import model.Tabellino;

public class CalcoliHelper {

	private Regole regole;
	private List<Fascia> fasceModDifesa;
	private List<Fascia> fasceNumeroDifensori;
	private List<Fascia> fasceModCentrocampo;
	private List<Fascia> fasceGol;

	public CalcoliHelper(Regole regole, List<Fascia> fasceModDifesa, List<Fascia> fasceNumeroDifensori,
			List<Fascia> fasceModCentrocampo, List<Fascia> fasceGol){
		this.regole = regole;
		this.fasceModDifesa = fasceModDifesa;
		this.fasceNumeroDifensori = fasceNumeroDifensori;
		this.fasceModCentrocampo = fasceModCentrocampo;
		this.fasceGol = fasceGol;
	}

	public Match calcolaMatch (Tabellino tabellinoJ, Tabellino tabellinoK, boolean squadraJInCasa, boolean esisteFattoreCampo){

		Match match = new Match();
		Team squadraJ;
		Team squadraK;
		if (squadraJInCasa){
			squadraJ = match.squadra1;
			squadraK = match.squadra2;
		}
		else {
			squadraJ = match.squadra2;
			squadraK = match.squadra1;
		}
		match.squadra1.fattoreCampo = esisteFattoreCampo?regole.fattoreCampo:0;

		squadraJ.parziale = sum11(tabellinoJ.voti);
		squadraK.parziale = sum11(tabellinoK.voti);

		//i modificatori portiere rimangono inalterati. Li prelevo dal relativo tabellino
		if (regole.regolaPortiere){
			squadraJ.modPortiere = tabellinoJ.modPortiere;
			squadraK.modPortiere = tabellinoK.modPortiere;
		}

		//il mod difesa si calcola con i dati dell'avversario
		if (regole.regolaDifesa) {
			squadraJ.modDifesa = calcolaModDifesa(tabellinoK);
			squadraK.modDifesa = calcolaModDifesa(tabellinoJ);
		}

		if (regole.regolaCentDiffe){
			double modCentrocampo = calcolaModCentrocampoDifferenza (tabellinoJ, tabellinoK);
			squadraJ.modCentrocampo = modCentrocampo;
			squadraK.modCentrocampo = modCentrocampo*-1;
		}
		
		//i modificatori attacco rimangono inalterati. Li prelevo dal relativo tabellino
		if (regole.regolaAttacco){
			squadraJ.modAttacco = tabellinoJ.modAttacco;
			squadraK.modAttacco = tabellinoK.modAttacco;
		}
		
		//anche i modificatori speciali rimangono inalterati
		if (regole.usaSpeciale1){
			squadraJ.modSpeciale1 = tabellinoJ.modPers1;
			squadraK.modSpeciale1 = tabellinoK.modPers1;
		}

		if (regole.usaSpeciale2){
			squadraJ.modSpeciale2 = tabellinoJ.modPers2;
			squadraK.modSpeciale2 = tabellinoK.modPers2;
		}

		if (regole.usaSpeciale3){
			squadraJ.modSpeciale3 = tabellinoJ.modPers3;
			squadraK.modSpeciale3 = tabellinoK.modPers3;
		}


		//bonus moduli
		String moduloJ = calcModulo(tabellinoJ.ruoli);
		if (regole.moduli.get(moduloJ)!=null){
			squadraJ.modModulo += regole.moduli.get(moduloJ).modif;
			squadraK.modModulo += regole.moduli.get(moduloJ).modifAvv;
		}
		
		String moduloK = calcModulo(tabellinoK.ruoli);
		if (regole.moduli.get(moduloK)!=null){
			squadraK.modModulo += regole.moduli.get(moduloK).modif;
			squadraJ.modModulo += regole.moduli.get(moduloK).modifAvv;
		}
		
		squadraJ.numeroGol = (int) getFascia(fasceGol, squadraJ.getTotale()).valore;
		squadraK.numeroGol = (int) getFascia(fasceGol, squadraK.getTotale()).valore;
		
		affinaNumeroGol(squadraJ, squadraK);
		return match;
	}

	private double sum11(String[] strings) {
		double sum = 0;
		for (int i=0; i<11; i++){
			sum+=Double.parseDouble(strings[i].replace(',', '.'));
		}
		return sum;
	}

	private double calcolaModDifesa(Tabellino tab) {
		double totDif = 0;
		int numDif = 0;
		for (int x=0; x<11; x++){
			if (tab.ruoli[x].equals("2")||tab.ruoli[x].equals("6")){
				totDif += Double.parseDouble(tab.votipuri[x].replace(',', '.'));
				numDif++;
				if (Double.parseDouble(tab.votipuri[x].replace(',', '.'))==0){
					if ( regole.regolaDifesaVU){
						totDif += regole.VUDifensore;
					}
					else {
						numDif--;
					}
				}
			}
		}
		double medDif1 = totDif/numDif;
		double mod = getFascia(fasceModDifesa, medDif1).valore;
		mod +=  getFascia(fasceNumeroDifensori, numDif).valore;
		return mod;
	}

	private double calcolaModCentrocampoDifferenza(Tabellino tabJ, Tabellino tabK) {
		//mod centrocampo per differenza
		double totCentJ = calcolaCC(tabJ);
		double totCentK = calcolaCC(tabK);
		double diff = totCentJ-totCentK;
		double modValue = getFascia(fasceModCentrocampo, Math.abs(diff)).valore;
		return modValue*Math.signum(diff);
	}

	private double calcolaCC(Tabellino tabJ) {
		double totCent = 0;
		int numcent = 0;
		for (int x=0; x<11; x++){
			if (tabJ.ruoli[x].equals("3")||tabJ.ruoli[x].equals("7")){
				if (Double.parseDouble(tabJ.votipuri[x].replace(',', '.'))>0){
					totCent += Double.parseDouble(tabJ.votipuri[x].replace(',', '.'));
					numcent++;
				}
			}
		}
		totCent += (5-numcent)*regole.VUCentrocampista;
		return totCent;
	}

	private Fascia getFascia (List<Fascia> lista, double value){
		Collections.sort(lista, new Comparator<Fascia>() {
			@Override
			public int compare(Fascia o1, Fascia o2) {
				if (o1.min>o2.min) return -1;
				if (o1.min<o2.min) return 1;
				return 0;
			}
		});
		for (Fascia f: lista){
			if (value>=f.min){
				return f;
			}
		}
		return new Fascia();
	}

	private static String calcModulo(String[] ruoli) {
		int dif = 0;
		int cen = 0;
		int att = 0;
		for (int x=0; x<11; x++){
			if (ruoli[x].equals("2")||ruoli[x].equals("6")){
				dif ++;
			}
			if (ruoli[x].equals("3")||ruoli[x].equals("7")){
				cen ++;
			}
			if (ruoli[x].equals("4")||ruoli[x].equals("8")){
				att ++;
			}
		}
		return dif +"-"+cen+"-"+att;
	}

	private void affinaNumeroGol(Team squadraJ, Team squadraK) {
		//regola diff 4 (o valore esatto)
		if (regole.regolaDiff4 && squadraJ.numeroGol==squadraK.numeroGol && squadraJ.numeroGol>=1){
			if (squadraJ.getTotale()>=squadraK.getTotale()+regole.regolaDiff4Valore){
				squadraJ.numeroGol++;
			}
			if (squadraK.getTotale()>=squadraJ.getTotale()+regole.regolaDiff4Valore){
				squadraK.numeroGol++;
			}
		}

		//regola diff 10 (o valore esatto)
		if (regole.regolaDiff10){
			if (squadraJ.getTotale()>=squadraK.getTotale()+regole.regolaDiff10Valore){
				squadraJ.numeroGol++;
			}
			if (squadraK.getTotale()>=squadraJ.getTotale()+regole.regolaDiff10Valore){
				squadraK.numeroGol++;
			}
		}

		//regola min 60
		if (regole.regolaMin60){
			if (squadraK.getTotale()<regole.regolaMin60Valore && squadraJ.getTotale()>=squadraK.getTotale()+regole.regolaMin60Delta){
				squadraJ.numeroGol++;
			}
			if (squadraJ.getTotale()<regole.regolaMin60Valore && squadraK.getTotale()>=squadraJ.getTotale()+regole.regolaMin60Delta){
				squadraK.numeroGol++;
			}
		}

		//regola diff 3
		if (regole.regolaDelta3){
			if (squadraJ.numeroGol>squadraK.numeroGol && squadraJ.getTotale()<squadraK.getTotale()+regole.regolaDelta3Valore){
				squadraK.numeroGol++;
				if (squadraJ.numeroGol==1){
					squadraJ.numeroGol--;
					squadraK.numeroGol--;
				}
			}
			if (squadraK.numeroGol>squadraJ.numeroGol && squadraK.getTotale()<squadraJ.getTotale()+regole.regolaDelta3Valore){
				squadraJ.numeroGol++;
				if (squadraJ.numeroGol==1){
					squadraJ.numeroGol--;
					squadraK.numeroGol--;
				}
			}
		}

		//regola min 59
		if (regole.regolaMin59){
			if (squadraJ.getTotale()<regole.regolaMin59Valore && squadraK.getTotale()>=regole.regolaMin59Almeno && squadraK.getTotale()>=squadraJ.getTotale()+regole.regolaMin59Delta){
				squadraK.numeroGol++;
			}
			if (squadraK.getTotale()<regole.regolaMin59Valore && squadraJ.getTotale()>=regole.regolaMin59Almeno && squadraJ.getTotale()>=squadraK.getTotale()+regole.regolaMin59Delta){
				squadraJ.numeroGol++;
			}
		}
	}

}
