package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import model.Fascia;
import model.Incontro;
import model.Match;
import model.Tabellino;
import dao.FcmDao;
import exception.InvalidGiornataException;
import fcm.CalcoliHelper;

public class CalendariIncrociati {

	private static String filename = "";
	private static String compSel = "";
	private static Logger logger = Logger.getLogger("main.CalendariIncrociati");
	private static String rootPath = "";
	private static String idGirone = "";

	public static void main(String args[]){
		try {
			logger = Logger.getLogger("main.CalendariIncrociati");
			logger.setLevel(Level.ALL);
			FileHandler fh = new FileHandler("log.txt");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("argomenti in input: "+args.length);
		for (int i=0; i<args.length; i++){
			logger.info("arg "+(i+1)+": "+args[i]);
		}
		if (args.length==3){
			//il nome della lega è nel formato "/L=C:\Programmi\FCM\DATA\MiaLega-1-2004.fcm"
			String filename = args[2].split("=")[1].replace("\"", "");
			rootPath = args[1].split("=")[1].replace("\"", "")+"\\";
			logger.info("Filename:"+ filename);
			CalendariIncrociati.chooseCompetizione(filename);
		}
		else {
			SelezioneLega.build();
		}
	}

	public static void chooseCompetizione(String filename){
		CalendariIncrociati.filename = filename;
		try (FcmDao dao = new FcmDao(filename)){
			SelezioneCompetizione.build(dao.getListaCompetizioni());
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
	}

	public static void chooseGirone(String optionCompetizione){
		compSel = optionCompetizione.split(" - ")[0];
		try (FcmDao dao = new FcmDao(filename)){
			SelezioneGirone.build(dao.getListaGironi(compSel));
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
	}

	public static void doCalcolo(String optionGirone) {

		try (FcmDao dao = new FcmDao(filename)){
			logger.info("Creazione cartella files");
			File dir = new File(rootPath+"incrodet");
			if (!dir.exists()){
				dir.mkdir(); 
			}

			idGirone = optionGirone.split(" - ")[0];

			Hashtable<Integer,String> nomiteam = dao.getSquadreGirone(idGirone);
			Regole r = dao.getRegoleCompetizione(compSel);
			ArrayList<Integer> giornate = dao.getGiornate(idGirone);
			Integer[] squadreArray = dao.getSquadreIscritte(idGirone);
			List<Fascia> fasceModDifesa = dao.getFasceModificatoreDifesa(compSel);
			List<Fascia> fasceNumeroDifensori = dao.getContributoNumeroDifensoriModificatoreDifesa(compSel);
			List<Fascia> fasceModCentrocampo = dao.getFasceModificatoreCentrocampo(compSel);
			List<Fascia> fasceGol = dao.getFasceConversioneGol(compSel);
			CalcoliHelper calcoliHelper = new CalcoliHelper(r, fasceModDifesa, fasceNumeroDifensori, fasceModCentrocampo, fasceGol);

			logger.info("Inizializzazione totale punti");
			int numSquadre = squadreArray.length;
			int [][] superTotalePunti = new int[numSquadre][numSquadre];
			//inizializzo superTotalePunti
			for (int i=0; i<numSquadre; i++){
				for (int j=0; j<numSquadre; j++){
					superTotalePunti[i][j]=0;
				}
			}

			logger.info("Inizio analisi");
			for (int i=0; i<giornate.size(); i++){
				logger.info("Provo a creare il file "+rootPath+"incrodet/"+giornate.get(i)+".txt");
				BufferedWriter w = new BufferedWriter(new FileWriter(rootPath+"incrodet/"+giornate.get(i)+".txt"));
				//mi estraggo gli id degli incontri della giornata i-esima
				ArrayList<String> idIncontriFiltro = new ArrayList<String>();
				Hashtable<String,String> avversari = new Hashtable<String,String>();
				boolean esisteFattoreCampo = false;
				List<Incontro> incontri = dao.getIncontri(idGirone, giornate.get(i));
				for (Incontro inc: incontri){
					if (inc.fattoreCampo){
						esisteFattoreCampo=true;
					}
					idIncontriFiltro.add(inc.idIncontro);
					avversari.put(inc.casa,inc.trasferta+"C");
					avversari.put(inc.trasferta,inc.casa);
				}
				logger.info("ID incontri: "+idIncontriFiltro);
				logger.info("Accoppiamenti: "+avversari);

				//mi estraggo i tabellini delle squadre della giornata in questione, sfruttando gli ID precedenti

				logger.info("Estrazione dati squadre");
				Map<Integer, Tabellino> tabellini = null;
				try {
					tabellini = dao.getTabellini(idIncontriFiltro);
				}
				//se non ho risultati in quella giornata, vado allo step successivo
				catch (InvalidGiornataException e) {
					continue;
				}

				//ciclo squadra per squadra

				String ris[][] = new String[squadreArray.length][squadreArray.length];
				String gol[][] = new String[squadreArray.length][squadreArray.length];
				int punti[][] = new int[squadreArray.length][squadreArray.length];

				String[][] risPerRender = new String[squadreArray.length][squadreArray.length];
				logger.info("Inizio inversione calendari");
				for(int j=0; j<squadreArray.length; j++){
					//per ogni squadra ciclo su tutti i possibili calendari avversari
					for (int k=0; k<squadreArray.length; k++){
						String squadraJ = Integer.toString(squadreArray[j]);
						String squadraK = Integer.toString(squadreArray[k]);
						String currAvv = (String)avversari.get(squadraK);

						//se home è true, la squadra J gioca in casa, altrimenti gioca in trasferta
						boolean home = currAvv.contains("C");
						currAvv = currAvv.replace("C", "");
						if (currAvv.equals(squadraJ)){
							//se l'avversario coincide con la squadra stessa, gli assegno l'avversario reale e inverto il fattore campo
							currAvv = squadraK;
							home=!home;
						}

						//determinato l'avversario, calcolo il punteggio della partita

						Match match = calcoliHelper.calcolaMatch (tabellini.get(Integer.parseInt(squadraJ)), tabellini.get(Integer.parseInt(currAvv)), home, esisteFattoreCampo);
						ris[j][k] = match.squadra1.getTotale()+ "-"+match.squadra2.getTotale();
						gol[j][k] = match.squadra1.numeroGol + "-"+ match.squadra2.numeroGol;
						
						if (match.squadra1.numeroGol>match.squadra2.numeroGol){
							punti[j][k] = r.puntiPerVittoria;
						}
						else if (match.squadra1.numeroGol<match.squadra2.numeroGol){
							punti[j][k] = 0;
						}
						else {
							punti[j][k] = 1;
						}
						if (!home){
							if (punti[j][k]==3){
								punti[j][k]=0;
							}
							else if (punti[j][k]==0){
								punti[j][k]=3;
							}
						}
						risPerRender[j][k] = calcolaRisPerRender (r, nomiteam.get(Integer.parseInt(squadraJ)), nomiteam.get(Integer.parseInt(currAvv)), home, match);
						risPerRender[j][k] += ris[j][k]+ "("+gol[j][k]+")   ";
						w.append(risPerRender[j][k]);
					}
					w.newLine();
				}
				w.flush();
				w.close();

				renderHTML(risPerRender,rootPath+"incrodet/"+giornate.get(i)+".html", squadreArray, nomiteam);

				for(int j=0; j<squadreArray.length; j++){
					//per ogni squadra ciclo su tutti i possibili calendari avversari
					for (int k=0; k<squadreArray.length; k++){
						superTotalePunti[j][k] += punti[j][k];
					}
				}

			}

			logger.info("Scrittura totali");
			BufferedWriter w = new BufferedWriter(new FileWriter(rootPath+"incrodet/totale.txt"));
			for(int j=1; j<=squadreArray.length; j++){
				//per ogni squadra ciclo su tutti i possibili calendari avversari
				for (int k=1; k<=squadreArray.length; k++){
					w.append(superTotalePunti[j-1][k-1]+ "    ");
				}
				w.newLine();
			}
			w.flush();
			w.close();

			renderHTML(superTotalePunti,rootPath+"incrodet/totale.html", squadreArray, nomiteam);
			//per sicurezza creo la cartella js
			new File(rootPath+"js").mkdir(); 
			renderJS(superTotalePunti,rootPath+"js/incrociacalendari.js", squadreArray, nomiteam);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static String calcolaRisPerRender(Regole r, String squadra1, String squadra2, boolean home, Match match) {
		String result = squadra1+ " - " + squadra2 + " "+(home?"(C)":"")+ "<br><br>";
		result += "parz: "+match.squadra1.parziale + "-"+ match.squadra2.parziale+"<br>";
		if (r.regolaPortiere){
			result += "MP C: "+match.squadra1.modPortiere+" MP T: "+match.squadra2.modPortiere+"<br>";
		}
		if (r.regolaDifesa){
			result += "MD C:"+match.squadra1.modDifesa+" MD T:"+match.squadra2.modDifesa+"<br>";
		}
		if (r.regolaCentDiffe) {
			result += "MC C:"+match.squadra1.modCentrocampo+" MC T: "+match.squadra2.modCentrocampo+"<br>";
		}
		if (r.regolaAttacco){
			result += "MA C:"+match.squadra1.modAttacco+" MA T: "+match.squadra2.modAttacco+"<br>";
		}
		if (r.usaSpeciale1){
			result += "MS1 C:"+match.squadra1.modSpeciale1+" MS1 T: "+match.squadra2.modSpeciale1+"<br>";
		}
		if (r.usaSpeciale2){
			result += "MS2 C:"+match.squadra1.modSpeciale2+" MS2 T: "+match.squadra2.modSpeciale2+"<br>";
		}
		if (r.usaSpeciale3){
			result += "MS3 C:"+match.squadra1.modSpeciale3+" MS3 T: "+match.squadra2.modSpeciale3+"<br>";
		}
		result += "MM C: "+match.squadra1.modModulo+" MM T: "+match.squadra2.modModulo+"<br>";
		return result;
		
	}

	private static void renderHTML(String[][] risPerRender, String filename, Integer[] squadreArray, Hashtable<Integer,String> nomiteam) throws IOException {
		//		render html
		CalendariIncrociati.logger.info("Provo a salvare il file: "+filename);
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		w.append("<table border='1' cellpadding='3' cellspacing='0'>");
		w.append("<tr><td align='center' width='100'>&nbsp;</td>");
		for (int k=0; k<squadreArray.length; k++){
			w.append("<td>"+nomiteam.get(squadreArray[k])+"</td>");
		}
		w.newLine();
		w.append("</tr>");
		for(int j=0; j<squadreArray.length; j++){
			w.append("<tr><td align='center'>"+nomiteam.get(squadreArray[j])+"</td>");
			//per ogni squadra ciclo su tutti i possibili calendari avversari
			for (int k=0; k<squadreArray.length; k++){
				w.append("<td align='center'>");
				if (j==k){
					w.append("<b>");
				}
				w.append(risPerRender[j][k]);
				if (j==k){
					w.append("</b>");
				}
				w.append("</td>");
			}
			w.append("</tr>");
			w.newLine();
		}
		w.append("</table>");
		w.flush();
		w.close();
	}

	private static void renderHTML(int[][] superTotalePunti, String filename, Integer[] squadreArray, Hashtable<Integer,String> nomiteam) throws IOException {
		//		render html
		CalendariIncrociati.logger.info("Provo a salvare il file: "+filename);
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		w.append("<table border='1' cellpadding='3' cellspacing='0'>");
		w.append("<tr><td align='center'>&nbsp;</td>");
		for (int k=0; k<squadreArray.length; k++){
			w.append("<td>"+nomiteam.get(squadreArray[k])+"</td>");
		}
		w.newLine();
		w.append("</tr>");
		for(int j=0; j<squadreArray.length; j++){
			w.append("<tr><td align='center'>"+nomiteam.get(squadreArray[j])+"</td>");
			//per ogni squadra ciclo su tutti i possibili calendari avversari
			for (int k=0; k<squadreArray.length; k++){
				w.append("<td align='center'>");
				if (j==k){
					w.append("<b>");
				}
				w.append(Integer.toString(superTotalePunti[j][k]));
				if (j==k){
					w.append("</b>");
				}
				w.append("</td>");
			}
			w.append("</tr>");
			w.newLine();
		}
		w.append("</table>");
		w.flush();
		w.close();
	}

	private static void renderJS(int[][] superTotalePunti, String filename, Integer[] squadreArray, Hashtable<Integer,String> nomiteam) throws IOException {
		//		render js
		CalendariIncrociati.logger.info("Provo a salvare il file: "+filename);
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		w.append("var tableIncroci = new Array()");
		w.newLine();
		w.append("var tableIncrociTeams = new Array()");
		w.newLine();
		w.append("var a=tableIncrociTeams");
		w.newLine();
		for (int k=0; k<squadreArray.length; k++){
			w.append("a["+(k+1)+"]=\""+nomiteam.get(squadreArray[k])+"\"");
			w.newLine();
		}
		w.newLine();
		w.append("var a=tableIncroci");
		w.newLine();
		for(int j=0; j<squadreArray.length; j++){
			w.append("a["+(j+1)+"]=new Array()");
			w.newLine();

			//per ogni squadra ciclo su tutti i possibili calendari avversari
			for (int k=0; k<squadreArray.length; k++){
				w.append("a["+(j+1)+"]["+(k+1)+"]="+Integer.toString(superTotalePunti[j][k]));
				w.newLine();
			}
		}
		w.flush();
		w.close();
	}




}