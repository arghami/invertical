package main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.logging.Logger;

public class Regole {
	private static Logger logger = Logger.getLogger("main.Regole");
	
	public int puntiPerVittoria = 3;
	public double fattoreCampo = 0;
	public boolean regolaPortiere = false;
	public boolean regolaDifesa = false;
	public boolean regolaCentMedia = false;
	public boolean regolaCentDiffe = false;
	public boolean regolaDiff4 = false;
	public boolean regolaDiff10 = false;
	public boolean regolaMin60 = false;
	public boolean usaSpeciale1 = false;
	public boolean usaSpeciale2 = false;
	public boolean usaSpeciale3 = false;
	public boolean regolaAttacco = false;
	public boolean regolaDelta3 = false;
	public boolean regolaMin59 = false;
	public double regolaDiff4Valore = 0.0;
	public double regolaDiff10Valore = 0.0;
	public double regolaMin60Valore = 0.0;
	public double regolaMin60Delta = 0.0;
	public double regolaDelta3Valore = 0.0;
	public double regolaMin59Valore = 0.0;
	public double regolaMin59Delta = 0.0;
	public double regolaMin59Almeno = 0.0;
	public double VUCentrocampista = 0.0;
	public boolean regolaDifesaVU = false;
	public double VUDifensore = 0.0;
	public Hashtable<String,Modulo> moduli = new Hashtable<String,Modulo>();
	public Regole (Statement stmt, String compSel) throws SQLException{
		ResultSet rs = stmt.executeQuery("SELECT puntipervittoria, fattorecampo, regolaportiere, regoladifesa, regolacentmedia, " +
				"regolacentdiffe, regoladiff4, regoladiff10, regolamin60, usaspeciale1, " +
				"usaspeciale2, usaspeciale3, regolaattacco, regoladelta3, regolamin59," +
				"regolaDiff4Valore, regolaDiff10Valore, regolaMin60Valore, regolaMin60Delta, regolaDelta3Valore, " +
				"regolaMin59Valore, regolaMin59Delta, regolaMin59Almeno, VUCentrocampista, regolaDifesaVU," +
				"VUDifensore FROM competizione WHERE id = "+compSel);


		while (rs.next()){
			puntiPerVittoria = rs.getInt(1);
			fattoreCampo = rs.getDouble(2);
			regolaPortiere = rs.getBoolean(3);
			regolaDifesa = rs.getBoolean(4);
			regolaCentMedia = rs.getBoolean(5);
			regolaCentDiffe = rs.getBoolean(6);
			regolaDiff4 = rs.getBoolean(7);
			regolaDiff10 = rs.getBoolean(8);
			regolaMin60 = rs.getBoolean(9);
			usaSpeciale1 = rs.getBoolean(10);
			usaSpeciale2 = rs.getBoolean(11);
			usaSpeciale3 = rs.getBoolean(12);
			regolaAttacco = rs.getBoolean(13);
			regolaDelta3 = rs.getBoolean(14);
			regolaMin59 = rs.getBoolean(15);
			regolaDiff4Valore = rs.getDouble(16);
			regolaDiff10Valore = rs.getDouble(17);
			regolaMin60Valore = rs.getDouble(18);
			regolaMin60Delta = rs.getDouble(19);
			regolaDelta3Valore = rs.getDouble(20);
			regolaMin59Valore = rs.getDouble(21);
			regolaMin59Delta = rs.getDouble(22);
			regolaMin59Almeno = rs.getDouble(23);
			VUCentrocampista = rs.getDouble(24);
			regolaDifesaVU = rs.getBoolean(25);
			VUDifensore = rs.getDouble(26);
			
			logger.info("puntiPerVittoria: "+puntiPerVittoria);
			logger.info("fattoreCampo: "+fattoreCampo);
			logger.info("regolaPortiere: "+regolaPortiere);
			logger.info("regolaDifesa: "+regolaDifesa);
			logger.info("regolaCentMedia: "+regolaCentMedia);
			logger.info("regolaCentDiffe: "+regolaCentDiffe);
			logger.info("regolaDiff4: "+regolaDiff4);
			logger.info("regolaDiff10: "+regolaDiff10);
			logger.info("regolaMin60: "+regolaMin60);
			logger.info("usaSpeciale1: "+usaSpeciale1);
			logger.info("usaSpeciale2: "+usaSpeciale2);
			logger.info("usaSpeciale3: "+usaSpeciale3);
			logger.info("regolaAttacco: "+regolaAttacco);
			logger.info("regolaDelta3: "+regolaDelta3);
			logger.info("regolaMin59: "+regolaMin59);
			logger.info("regolaDiff4Valore: "+regolaDiff4Valore);
			logger.info("regolaDiff10Valore: "+regolaDiff10Valore);
			logger.info("regolaMin60Valore: "+regolaMin60Valore);
			logger.info("regolaMin60Delta: "+regolaMin60Delta);
			logger.info("regolaDelta3Valore: "+regolaDelta3Valore);
			logger.info("regolaMin59Valore: "+regolaMin59Valore);
			logger.info("regolaMin59Delta: "+regolaMin59Delta);
			logger.info("regolaMin59Almeno: "+regolaMin59Almeno);
			logger.info("VUCentrocampista: "+VUCentrocampista);
			logger.info("regolaDifesaVU: "+regolaDifesaVU);
			logger.info("VUDifensore: "+VUDifensore);
		}
		
		rs = stmt.executeQuery("select numDife, numCent, numAtta, Modif, ModifAvv " +
				"from Modulo, ModuloAmmesso "+
				"WHERE ModuloAmmesso.IDCompetizione = "+compSel+
				" and ModuloAmmesso.IDModulo=Modulo.id");


		while (rs.next()){
			String mod = rs.getInt(1)+"-"+rs.getString(2)+"-"+rs.getString(3);
			moduli.put(mod, new Modulo(rs.getDouble(4), rs.getDouble(5)));
		}
		logger.info("Moduli: "+moduli);
	}
}
