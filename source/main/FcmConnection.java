package main;

import java.sql.*;
import java.util.logging.Logger;

public class FcmConnection {
	//private static final String accessDBURLPrefix = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
	private static final String accessDBURLPrefix = "jdbc:ucanaccess://";
	//private static final String accessDBURLSuffix = ";DriverID=22;READONLY=true}";
	private static final String accessDBURLSuffix = "";
	private static Logger logger = Logger.getLogger("main.FcmConnection");
	static {
		try {
			//Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		} catch(ClassNotFoundException e) {
			logger.info("UcanaccessDriver not found!");
		}
	}

	/** Creates a Connection to a Access Database 
	 * @param logger */
	public static java.sql.Connection getAccessDBConnection(String filename) throws SQLException {
		filename = filename.replace('\\', '/').trim();
		String databaseURL = accessDBURLPrefix + filename + accessDBURLSuffix;
		logger.info("Stringa di connessione: "+databaseURL);
		return DriverManager.getConnection(databaseURL, "", "");
	}  
}