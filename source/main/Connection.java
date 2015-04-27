package main;

import java.sql.*;

public class Connection {
	//private static final String accessDBURLPrefix = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
	private static final String accessDBURLPrefix = "jdbc:ucanaccess://";
	//private static final String accessDBURLSuffix = ";DriverID=22;READONLY=true}";
	private static final String accessDBURLSuffix = "";
	static {
		try {
			//Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		} catch(ClassNotFoundException e) {
			CalendariIncrociati.logger.info("JdbcOdbc Bridge Driver not found!");
		}
	}

	/** Creates a Connection to a Access Database 
	 * @param logger */
	public static java.sql.Connection getAccessDBConnection(String filename) throws SQLException {
		filename = filename.replace('\\', '/').trim();
		String databaseURL = accessDBURLPrefix + filename + accessDBURLSuffix;
		CalendariIncrociati.logger.info("Stringa di connessione: "+databaseURL);
		return DriverManager.getConnection(databaseURL, "", "");
	}  
}