package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//First argument is database file name
//Second argument is your query in quotes
public class CalendariIncrociati {
	static java.sql.Connection conn = null;
	private static String filename = "";
	static String compSel = "";
	private static Regole r;
	public static Logger logger;
	public static String rootPath = "";
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
			CalendariIncrociati.calc(filename,1);
		}
		else {
			SelezioneLega.build();
		}
	}

	public static void calc(String par, int step) {

		try {
			if (step==1){
				CalendariIncrociati.filename  = par;

				logger.info("Tentativo di connessione al db");
				conn = Connection.getAccessDBConnection(filename);
				Statement stmt = conn.createStatement();

				//estraggo l'elenco delle competizioni
				logger.info("Estrazione dell'elenco delle competizioni");
				ResultSet rs = stmt.executeQuery("SELECT id, nome FROM competizione");
				ArrayList<String> comps = new ArrayList<String>();
				while (rs.next()){
					comps.add(rs.getString(1)+" - "+rs.getString(2));
				}
				String [] compsArray = new String[comps.size()];
				comps.toArray(compsArray);
				rs.close();
				stmt.close();
				conn.close();

				logger.info("Invio elenco competizioni alla GUI");
				SelezioneCompetizione.build(compsArray);
			}
			else if (step==2){

				logger.info("Tentativo di connessione al db");
				conn = Connection.getAccessDBConnection(filename);
				Statement stmt = conn.createStatement();

				//mi piglio la competizione selezionata
				compSel = par.split(" - ")[0];
				//estraggo l'elenco dei gironi
				logger.info("Estrazione dell'elenco dei gironi");
				ResultSet rs = stmt.executeQuery("SELECT id, nome FROM girone WHERE idcompetizione = "+compSel);
				ArrayList<String> girs = new ArrayList<String>();
				while (rs.next()){
					String girName = rs.getString(2);
					girName = girName!=null?girName:"Senza Nome";
					girs.add(rs.getString(1)+" - "+girName);
				}
				String [] girsArray = new String[girs.size()];
				girs.toArray(girsArray);
				rs.close();
				stmt.close();
				conn.close();

				logger.info("Invio elenco gironi alla GUI");
				SelezioneGirone.build(girsArray);
			}
			else {
				logger.info("Creazione cartella files");
				File dir = new File(rootPath+"incrodet");
				if (!dir.exists()){
					dir.mkdir(); 
				}
				logger.info("Tentativo di connessione al db");
				conn = Connection.getAccessDBConnection(filename);
				Statement stmt = conn.createStatement();

				//mi seleziono il girone relativo
				logger.info("Selezione girone");
				idGirone = par.split(" - ")[0];
				
				//seleziono le squadre
				logger.info("Estrazione squadre");
				Hashtable<Integer,String> nomiteam = new Hashtable<Integer,String>();
				ResultSet rs = stmt.executeQuery("SELECT i.idsquadra, f.nome FROM iscritta i, fantasquadra f WHERE i.idsquadra=f.id AND i.idgirone = "+idGirone);
				while (rs.next()){
					nomiteam.put(rs.getInt(1),rs.getString(2));
				}
				logger.info("Squadre: "+nomiteam);


				//raccolgo le regole della competizione
				logger.info("Estrazione regole");
				r = new Regole(stmt, compSel);

				//vado a spulciarmi il calendario
				logger.info("Estrazione calendario");
				rs = stmt.executeQuery("SELECT idGiornata FROM incontro WHERE idGirone = "+idGirone+ " GROUP BY idGiornata ");
				ArrayList<Integer> giornate = new ArrayList<Integer>();
				while (rs.next()){
					giornate.add(rs.getInt(1));
				}
				logger.info("ID Giornate: "+giornate);

				//numero squadre iscritte
				logger.info("Estrazione IDsquadre"); //TODO: da ottimizzare
				ArrayList<Integer> squadre = new ArrayList<Integer>();
				rs = stmt.executeQuery("SELECT idSquadra FROM iscritta where idgirone="+idGirone);
				while (rs.next()){
					squadre.add(rs.getInt(1));
				}
				Integer[] squadreArray = new Integer[squadre.size()];
				squadre.toArray(squadreArray);

				logger.info("Inizializzazione totale punti");
				int [][] superTotalePunti = new int[squadre.size()][squadre.size()];
				//inizializzo superTotalePunti
				for (int i=0; i<squadre.size(); i++){
					for (int j=0; j<squadre.size(); j++){
						superTotalePunti[i][j]=0;
					}
				}
				stmt.close();

				logger.info("Inizio analisi");
				for (int i=0; i<giornate.size(); i++){
					stmt = conn.createStatement();
					logger.info("Provo a creare il file "+rootPath+"incrodet/"+giornate.get(i)+".txt");
					BufferedWriter w = new BufferedWriter(new FileWriter(rootPath+"incrodet/"+giornate.get(i)+".txt"));
					//mi estraggo gli id degli incontri della giornata i-esima
					ArrayList<String> idIncontriFiltro = new ArrayList<String>();
					Hashtable<String,String> avversari = new Hashtable<String,String>();
					boolean fattoreCampoBol = false;
					logger.info("Estrazione ID incontri");
					rs = stmt.executeQuery("SELECT id, idcasa, idfuori, idtipo FROM incontro WHERE idgirone = "+idGirone+" AND idGiornata = "+giornate.get(i));
					while (rs.next()){
						if (rs.getInt(4)==1){
							fattoreCampoBol = true;
						}
						idIncontriFiltro.add(rs.getString(1));
						String casa = rs.getString(2);
						String trasferta = rs.getString(3);
						avversari.put(casa,trasferta+"C");
						avversari.put(trasferta,casa);
					}
					logger.info("ID incontri: "+idIncontriFiltro);
					String filtro = idIncontriFiltro.toString().replace("[", "(").replace("]", ")");
					logger.info("Accoppiamenti: "+avversari);

					//mi estraggo i tabellini delle squadre della giornata in questione, sfruttando gli ID precedenti

					logger.info("Estrazione dati squadre");
					rs = stmt.executeQuery("SELECT idsquadra, tot, ruolo, modportiere, modattacco, moddifesa, voto, modm1pers, modm2pers, modm3pers FROM tabellino WHERE idincontro IN "+filtro+" ORDER BY idsquadra");

					//se non ho risultati in quella giornata, vado allo step successivo
					String[][] voti = new String[squadreArray.length][];
					String[][] votipuri = new String[squadreArray.length][];
					String[][] ruoli = new String[squadreArray.length][];
					double[] modPortiere = new double[squadreArray.length];
					double[] modAttacco = new double[squadreArray.length];
					double[] modDifesa = new double[squadreArray.length];
					double[] modPers1 = new double[squadreArray.length];
					double[] modPers2 = new double[squadreArray.length];
					double[] modPers3 = new double[squadreArray.length];

					boolean saltaGiornata = true;
					while (rs.next()){
						int idsq = squadre.indexOf(rs.getInt(1));
						String votiString = rs.getString(2);
						if (votiString!=null){
							voti[idsq] = votiString.split("%");
							saltaGiornata = false;
						}
						String ruoliString = rs.getString(3);
						if (ruoliString!=null){
							ruoli[idsq] = ruoliString.split("%");
							saltaGiornata = false;
						}
						modPortiere[idsq] = rs.getDouble(4);
						modAttacco[idsq] = rs.getDouble(5);
						modDifesa[idsq] = rs.getDouble(6);

						String votipuriString = rs.getString(7);
						if (votipuriString!=null){
							votipuri[idsq] = votipuriString.split("%");
							saltaGiornata = false;
						}
						
						modPers1[idsq] = rs.getDouble(8);
						modPers2[idsq] = rs.getDouble(9);
						modPers3[idsq] = rs.getDouble(10);
					}
					stmt.close();
					if (saltaGiornata){
						continue;
					}
					//ciclo squadra per squadra

					String ris[][] = new String[squadreArray.length][squadreArray.length];
					String gol[][] = new String[squadreArray.length][squadreArray.length];
					int punti[][] = new int[squadreArray.length][squadreArray.length];

					String[][] risPerRender = new String[squadreArray.length][squadreArray.length];
					logger.info("Inizio inversione calendari");
					for(int j=1; j<=squadreArray.length; j++){
						stmt = conn.createStatement();
						//per ogni squadra ciclo su tutti i possibili calendari avversari
						for (int k=1; k<=squadreArray.length; k++){
							String currAvv = (String)avversari.get(Integer.toString(squadreArray[k-1]));
							boolean home = currAvv.contains("C");
							currAvv = currAvv.replace("C", "");
							if (currAvv.equals(Integer.toString(squadreArray[j-1]))){
								//se l'avversario coincide con la squadra stessa, gli assegno l'avversario reale e inverto il fattore campo
								currAvv = Integer.toString(squadreArray[k-1]);
								home=!home;
							}

							currAvv = Integer.toString(squadre.indexOf(Integer.parseInt(currAvv))+1);
							//determinato l'avversario, calcolo il punteggio della partita

							double homeScore = 0;
							double awayScore = 0;
							if (home){
								homeScore = sum11(voti[j-1]) + (fattoreCampoBol?r.fattoreCampo:0);
								awayScore = sum11(voti[Integer.parseInt(currAvv)-1]);
							}
							else {
								awayScore = sum11(voti[j-1]);
								homeScore = sum11(voti[Integer.parseInt(currAvv)-1]) + (fattoreCampoBol?r.fattoreCampo:0);
							}

							risPerRender[j-1][k-1] = nomiteam.get(squadreArray[j-1])+ " - " + nomiteam.get(Integer.parseInt(currAvv)) + " "+(home?"(C)":"")+ "<br><br>";
							risPerRender[j-1][k-1] += "parz: "+homeScore + "-"+ awayScore+"<br>";
							//contributo dei modificatori

							//mod portiere
							if (r.regolaPortiere){
								if (home){
									homeScore += modPortiere[j-1];
									awayScore += modPortiere[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MP C: "+modPortiere[j-1]+" MP T: "+modPortiere[Integer.parseInt(currAvv)-1]+"<br>";
								}
								else{
									awayScore += modPortiere[j-1];
									homeScore += modPortiere[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MP C: "+modPortiere[Integer.parseInt(currAvv)-1]+" MP T: "+modPortiere[j-1]+"<br>";
								}
							}


							//mod difesa
							if (r.regolaDifesa){

								double mod1 = calcolaModDifesa(ruoli[j-1], votipuri[j-1], stmt);
								if (home){
									awayScore += mod1;
									risPerRender[j-1][k-1] += "MD T:"+mod1;
								}
								else {
									homeScore += mod1;
									risPerRender[j-1][k-1] += "MD C:"+mod1;
								}

								double mod2 = calcolaModDifesa(ruoli[Integer.parseInt(currAvv)-1], votipuri[Integer.parseInt(currAvv)-1], stmt);

								if (home){
									homeScore += mod2;
									risPerRender[j-1][k-1] += " MD C:"+mod2+"<br>";
								}
								else {
									awayScore += mod2;
									risPerRender[j-1][k-1] += " MD T:"+mod2+"<br>";
								}

							}


							//mod centrocampo per differenza
							if (r.regolaCentDiffe) {
								double totCent1 = 0;
								int numcent1 = 0;
								double totCent2 = 0;
								int numcent2 = 0;
								for (int x=0; x<11; x++){
									if (ruoli[j-1][x].equals("3")||ruoli[j-1][x].equals("7")){
										if (Double.parseDouble(votipuri[j-1][x].replace(',', '.'))>0){
											totCent1 += Double.parseDouble(votipuri[j-1][x].replace(',', '.'));
											numcent1++;
										}
									}
								}
								totCent1 += (5-numcent1)*r.VUCentrocampista;

								for (int x=0; x<11; x++){
									if (ruoli[Integer.parseInt(currAvv)-1][x].equals("3")||ruoli[Integer.parseInt(currAvv)-1][x].equals("7")){
										if (Double.parseDouble(votipuri[Integer.parseInt(currAvv)-1][x].replace(',', '.'))>0){
											totCent2 += Double.parseDouble(votipuri[Integer.parseInt(currAvv)-1][x].replace(',', '.'));
											numcent2++;
										}
									}
								}
								totCent2 += (5-numcent2)*r.VUCentrocampista;
								double diff = totCent1-totCent2;

								rs = stmt.executeQuery("SELECT f.valore FROM tabellacentrocampodiffe d, fascia f WHERE d.idcompetizione = "+compSel+" AND d.idfascia=f.id AND "+Math.abs(diff)+">=f.min AND "+Math.abs(diff)+"<f.max");
								double mod = 0.0;
								while (rs.next()){
									mod = rs.getDouble(1);
								}
								if ((diff>0 && home) || (diff<0 && !home)){
									homeScore += mod;
									awayScore -= mod;
									risPerRender[j-1][k-1] += "MC C:"+mod;
									risPerRender[j-1][k-1] += " MC T: -"+mod+"<br>";
								}
								else {
									homeScore -= mod;
									awayScore += mod;
									risPerRender[j-1][k-1] += "MC C: -"+mod;
									risPerRender[j-1][k-1] += " MC T:"+mod+"<br>";
								}
							}

							//mod attaccante
							if (r.regolaAttacco){
								if (home) {
									homeScore += modAttacco[j-1];
									awayScore += modAttacco[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MA C:"+modAttacco[j-1];
									risPerRender[j-1][k-1] += " MA T:"+modAttacco[Integer.parseInt(currAvv)-1]+"<br>";
								}
								else {
									awayScore += modAttacco[j-1];
									homeScore += modAttacco[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MA C:"+modAttacco[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += " MA T:"+modAttacco[j-1]+"<br>";
								}
							}
							
							//mod speciali
							if (r.usaSpeciale1){
								if (home) {
									homeScore += modPers1[j-1];
									awayScore += modPers1[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MS1 C:"+modPers1[j-1];
									risPerRender[j-1][k-1] += " MS1 T:"+modPers1[Integer.parseInt(currAvv)-1]+"<br>";
								}
								else {
									awayScore += modPers1[j-1];
									homeScore += modPers1[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MS1 C:"+modPers1[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += " MS1 T:"+modPers1[j-1]+"<br>";
								}
							}
							if (r.usaSpeciale2){
								if (home) {
									homeScore += modPers2[j-1];
									awayScore += modPers2[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MS2 C:"+modPers2[j-1];
									risPerRender[j-1][k-1] += " MS2 T:"+modPers2[Integer.parseInt(currAvv)-1]+"<br>";
								}
								else {
									awayScore += modPers2[j-1];
									homeScore += modPers2[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MS2 C:"+modPers2[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += " MS2 T:"+modPers2[j-1]+"<br>";
								}
							}
							if (r.usaSpeciale3){
								if (home) {
									homeScore += modPers3[j-1];
									awayScore += modPers3[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MS3 C:"+modPers3[j-1];
									risPerRender[j-1][k-1] += " MS3 T:"+modPers3[Integer.parseInt(currAvv)-1]+"<br>";
								}
								else {
									awayScore += modPers3[j-1];
									homeScore += modPers3[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += "MS3 C:"+modPers3[Integer.parseInt(currAvv)-1];
									risPerRender[j-1][k-1] += " MS3 T:"+modPers3[j-1]+"<br>";
								}
							}

							//bonus moduli
							String modulo1 = calcModulo(ruoli[j-1]);
							String modulo2 = calcModulo(ruoli[Integer.parseInt(currAvv)-1]);
							double modifModulo1 = 0.0;
							double modifModulo2 = 0.0;
							if (r.moduli.get(modulo1)!=null){
								modifModulo1 += r.moduli.get(modulo1).modif;
								modifModulo2 += r.moduli.get(modulo1).modifAvv;
							}
							if (r.moduli.get(modulo2)!=null){
								modifModulo2 += r.moduli.get(modulo2).modif;
								modifModulo1 += r.moduli.get(modulo2).modifAvv;
							}
							
							if (home){
								homeScore += modifModulo1;
								awayScore += modifModulo2;
								risPerRender[j-1][k-1] += "MM C: "+modifModulo1+" MM T: "+modifModulo2+"<br>";
							}
							else{
								awayScore += modifModulo1;
								homeScore += modifModulo2;
								risPerRender[j-1][k-1] += "MM C: "+modifModulo2+" MM T: "+modifModulo1+"<br>";
							}
							
							ris[j-1][k-1] = homeScore + "-"+ awayScore;

							//converto il punteggio numerico in punteggio gol
							{
								//conversione brutale in base alla fascia
								int homeGol = 0;
								int awayGol = 0;
								rs = stmt.executeQuery("SELECT f.valore FROM tabellagol g, fascia f WHERE g.idCompetizione = "+compSel+" AND g.idFascia=f.id AND f.min>"+homeScore+ " ORDER BY f.min");
								if (rs.next()){
									homeGol = rs.getInt(1)-1;
								}
								rs = stmt.executeQuery("SELECT f.valore FROM tabellagol g, fascia f WHERE g.idCompetizione = "+compSel+" AND g.idFascia=f.id AND f.min>"+awayScore+ " ORDER BY f.min");
								if (rs.next()){
									awayGol = rs.getInt(1)-1;
								}

								//aggiustamenti in base alle varie regolette

								//regola diff 4 (o valore esatto)
								if (r.regolaDiff4 && homeGol==awayGol && homeGol>=1){
									if (homeScore>=awayScore+r.regolaDiff4Valore){
										homeGol++;
									}
									if (awayScore>=homeScore+r.regolaDiff4Valore){
										awayGol++;
									}
								}

								//regola diff 10 (o valore esatto)
								if (r.regolaDiff10){
									if (homeScore>=awayScore+r.regolaDiff10Valore){
										homeGol++;
									}
									if (awayScore>=homeScore+r.regolaDiff10Valore){
										awayGol++;
									}
								}

								//regola min 60
								if (r.regolaMin60){
									if (awayScore<r.regolaMin60Valore && homeScore>=awayScore+r.regolaMin60Delta){
										homeGol++;
									}
									if (homeScore<r.regolaMin60Valore && awayScore>=homeScore+r.regolaMin60Delta){
										awayGol++;
									}
								}

								//regola diff 3
								if (r.regolaDelta3){
									if (homeGol>awayGol && homeScore<awayScore+r.regolaDelta3Valore){
										awayGol++;
										if (homeGol==1){
											homeGol--;
											awayGol--;
										}
									}
									if (awayGol>homeGol && awayScore<homeScore+r.regolaDelta3Valore){
										homeGol++;
										if (homeGol==1){
											homeGol--;
											awayGol--;
										}
									}
								}

								//regola min 59
								if (r.regolaMin59){
									if (homeScore<r.regolaMin59Valore && awayScore>=r.regolaMin59Almeno && awayScore>=homeScore+r.regolaMin59Delta){
										awayGol++;
									}
									if (awayScore<r.regolaMin59Valore && homeScore>=r.regolaMin59Almeno && homeScore>=awayScore+r.regolaMin59Delta){
										homeGol++;
									}
								}


								//popolo la tabella dei punti
								gol[j-1][k-1] = homeGol + "-"+ awayGol;
								if (homeGol>awayGol){
									punti[j-1][k-1] = r.puntiPerVittoria;
								}
								else if (homeGol<awayGol){
									punti[j-1][k-1] = 0;
								}
								else {
									punti[j-1][k-1] = 1;
								}
								if (!home){
									if (punti[j-1][k-1]==3){
										punti[j-1][k-1]=0;
									}
									else if (punti[j-1][k-1]==0){
										punti[j-1][k-1]=3;
									}
								}
							}
							risPerRender[j-1][k-1] += ris[j-1][k-1]+ "("+gol[j-1][k-1]+")   ";
							w.append(risPerRender[j-1][k-1]);
						}
						w.newLine();
						rs.close();
						stmt.close();
					}
					w.flush();
					w.close();

					renderHTML(risPerRender,rootPath+"incrodet/"+giornate.get(i)+".html", squadre, squadreArray, nomiteam);

					for(int j=1; j<=squadreArray.length; j++){
						//per ogni squadra ciclo su tutti i possibili calendari avversari
						for (int k=1; k<=squadreArray.length; k++){
							superTotalePunti[j-1][k-1] += punti[j-1][k-1];
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

				renderHTML(superTotalePunti,rootPath+"incrodet/totale.html", squadre, squadreArray, nomiteam);
				//per sicurezza creo la cartella js
				new File(rootPath+"js").mkdir(); 
				renderJS(superTotalePunti,rootPath+"js/incrociacalendari.js", squadre, squadreArray, nomiteam);

				stmt.close();
			}

		} catch(SQLException s) {
			s.printStackTrace();
			logger.severe(s.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch(SQLException ignore) {}
			}
		}
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

	private static double calcolaModDifesa(String[] ruoli, String[] votipuri, Statement stmt) throws SQLException {
		double totDif = 0;
		int numDif = 0;
		for (int x=0; x<11; x++){
			if (ruoli[x].equals("2")||ruoli[x].equals("6")){
				totDif += Double.parseDouble(votipuri[x].replace(',', '.'));
				numDif++;
				if (Double.parseDouble(votipuri[x].replace(',', '.'))==0){
					if ( r.regolaDifesaVU){
						totDif += r.VUDifensore;
					}
					else {
						numDif--;
					}
				}
			}
		}
		double medDif1 = totDif/numDif;
		ResultSet rs = stmt.executeQuery("SELECT f.valore FROM tabelladifesa d, fascia f WHERE d.idcompetizione = "+compSel+" AND d.idfascia=f.id AND "+medDif1+">=f.min AND "+medDif1+"<f.max");
		double mod = 0.0;
		if (rs.next()){
			mod = rs.getDouble(1);
		}
		rs = stmt.executeQuery("SELECT f.valore FROM tabellanumdifensori d, fascia f WHERE d.idcompetizione = "+compSel+" AND d.idfascia=f.id AND "+numDif+"=f.min");
		if (rs.next()){
			mod += rs.getDouble(1);
		}
		return mod;
	}

	private static void renderHTML(String[][] risPerRender, String filename, ArrayList<Integer> squadre, Integer[] squadreArray, Hashtable<Integer,String> nomiteam) throws IOException {
//		render html
		CalendariIncrociati.logger.info("Provo a salvare il file: "+filename);
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		w.append("<table border='1' cellpadding='3' cellspacing='0'>");
		w.append("<tr><td align='center' width='100'>&nbsp;</td>");
		for (int k=1; k<=squadreArray.length; k++){
			w.append("<td>"+nomiteam.get((Integer)squadre.get(k-1))+"</td>");
		}
		w.newLine();
		w.append("</tr>");
		for(int j=1; j<=squadreArray.length; j++){
			w.append("<tr><td align='center'>"+nomiteam.get((Integer)squadre.get(j-1))+"</td>");
			//per ogni squadra ciclo su tutti i possibili calendari avversari
			for (int k=1; k<=squadreArray.length; k++){
				w.append("<td align='center'>");
				if (j==k){
					w.append("<b>");
				}
				w.append(risPerRender[j-1][k-1]);
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

	private static void renderHTML(int[][] superTotalePunti, String filename, ArrayList<Integer> squadre, Integer[] squadreArray, Hashtable<Integer,String> nomiteam) throws IOException {
//		render html
		CalendariIncrociati.logger.info("Provo a salvare il file: "+filename);
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		w.append("<table border='1' cellpadding='3' cellspacing='0'>");
		w.append("<tr><td align='center'>&nbsp;</td>");
		for (int k=1; k<=squadreArray.length; k++){
			w.append("<td>"+nomiteam.get((Integer)squadre.get(k-1))+"</td>");
		}
		w.newLine();
		w.append("</tr>");
		for(int j=1; j<=squadreArray.length; j++){
			w.append("<tr><td align='center'>"+nomiteam.get((Integer)squadre.get(j-1))+"</td>");
			//per ogni squadra ciclo su tutti i possibili calendari avversari
			for (int k=1; k<=squadreArray.length; k++){
				w.append("<td align='center'>");
				if (j==k){
					w.append("<b>");
				}
				w.append(Integer.toString(superTotalePunti[j-1][k-1]));
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

	private static void renderJS(int[][] superTotalePunti, String filename, ArrayList<Integer> squadre, Integer[] squadreArray, Hashtable<Integer,String> nomiteam) throws IOException {
//		render js
		CalendariIncrociati.logger.info("Provo a salvare il file: "+filename);
		BufferedWriter w = new BufferedWriter(new FileWriter(filename));
		w.append("var tableIncroci = new Array()");
		w.newLine();
		w.append("var tableIncrociTeams = new Array()");
		w.newLine();
		w.append("var a=tableIncrociTeams");
		w.newLine();
		for (int k=1; k<=squadreArray.length; k++){
			w.append("a["+k+"]=\""+nomiteam.get((Integer)squadre.get(k-1))+"\"");
			w.newLine();
		}
		w.newLine();
		w.append("var a=tableIncroci");
		w.newLine();
		for(int j=1; j<=squadreArray.length; j++){
			w.append("a["+j+"]=new Array()");
			w.newLine();
			
			//per ogni squadra ciclo su tutti i possibili calendari avversari
			for (int k=1; k<=squadreArray.length; k++){
				w.append("a["+j+"]["+k+"]="+Integer.toString(superTotalePunti[j-1][k-1]));
				w.newLine();
			}
		}
		w.flush();
		w.close();
	}

	private static double sum11(String[] strings) {
		double sum = 0;
		for (int i=0; i<11; i++){
			sum+=Double.parseDouble(strings[i].replace(',', '.'));
		}
		return sum;
	}


}