package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

public class EstraiVoti {
	
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		java.sql.Connection conn = FcmConnection.getAccessDBConnection("C:/Users/Dario/Documents/Fantacalcio Manager/data/Materdei League 2014-1-2014.fcm");
		while (true){
			try {
				System.out.print("\n>");
				BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
				String s = bufferRead.readLine();

				//SELECT TABLE_NAME FROM information_schema.tables where TABLE_SCHEMA='PUBLIC'
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(s);
				ResultSetMetaData md = rs.getMetaData();
				StringBuffer output = new StringBuffer();
				for (int i=1; i<=md.getColumnCount(); i++){
					System.out.print("\t"+md.getColumnName(i)+"("+md.getColumnTypeName(i)+")");
					output.append("\t"+md.getColumnName(i)+"("+md.getColumnTypeName(i)+")");
				}
				for (int i=1; i<=md.getColumnCount(); i++){
					System.out.print("\t"+rs.getObject(i));
					output.append("\t"+rs.getObject(i));
				}
				while (rs.next()){
					System.out.println();
					output.append("\n");
					for (int i=1; i<=md.getColumnCount(); i++){
						System.out.print("\t"+rs.getObject(i));
						output.append("\t"+rs.getObject(i));
					}
				}
				PrintWriter writer = new PrintWriter("out.txt", "UTF-8");
				writer.print(output);
				writer.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}


	public static void main2(String[] args) throws SQLException {
		
		NumberFormat nf = NumberFormat.getInstance(Locale.ITALY);
		
		// TODO Auto-generated method stub
		java.sql.Connection conn = FcmConnection.getAccessDBConnection("C:/Users/Dario/Documents/Fantacalcio Manager/data/ArchivioA2016SerieA.fca");
		try {
			HashMap<Integer, PL> players = new HashMap<Integer, PL>();
			//SELECT TABLE_NAME FROM information_schema.tables where TABLE_SCHEMA='PUBLIC'
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select g.nome, g.ruolo, i.giornata, p.voto2, p.golfatti2, p.golfattisurigore2, p.rigpar, p.golsubiti, p.amm, p.esp, i.idgiocatore from giocain i, giocatorea g, punteggio p where i.idgiocatore = g.id and i.idpunteggio = p.id");
			while (rs.next()){
				if (!players.containsKey(rs.getInt(11))){
					players.put(rs.getInt(11), new PL());
				}
				PL pl = players.get(rs.getInt(11));
				pl.nome = rs.getString(1);
				String[] ruoli = new String[]{"","P","D","C","A"};
				pl.ruolo = ruoli[rs.getInt(2)];
				if (rs.getFloat(4)>0){
					pl.vtot += rs.getFloat(4);
					pl.ftot += rs.getFloat(4);
					float puntiaz = 0;
					pl.golTot += rs.getInt(5);
					pl.golSubitiTot += rs.getInt(8);
					puntiaz += 3*(rs.getInt(5)-rs.getInt(6));
					puntiaz += 2*rs.getInt(6);
					puntiaz += 3*rs.getInt(7);
					puntiaz -= rs.getInt(8);
					puntiaz -= rs.getBoolean(9)?0.5:0;
					puntiaz -= rs.getBoolean(10)?1.0:0;
					pl.ftot += puntiaz;
					pl.presenze++;
					pl.atot += rs.getFloat(4);
					
					if (rs.getInt(2)==1){
						//portiere
						if (rs.getFloat(4)>6 && rs.getFloat(7)==0)
							pl.atot += rs.getFloat(4)-6;
					}
					if (rs.getInt(2)==4){
						//attaccante
						if (rs.getFloat(4)>6 && rs.getFloat(5)==0)
							pl.atot += rs.getFloat(4)-6;
					}
					if (rs.getInt(2)==2 || rs.getInt(2)==3){
						//dif e cc
						pl.atot += rs.getFloat(4)-6;
					}
					
					pl.atot += puntiaz;
					
					pl.vmed = pl.vtot/pl.presenze;
					pl.fmed = pl.ftot/pl.presenze;
					pl.amed = pl.atot/pl.presenze;
				}
			}
			conn.close();
			conn = FcmConnection.getAccessDBConnection("C:/Users/Dario/Documents/Fantacalcio Manager/data/Materdei League 2016-1-2016.fcm");
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select t.IDGIOCATORE, t.COSTOACQ, f.NOME from tesserato t, fantasquadra f where t.IDSQUADRA=f.id");
			while (rs.next()){
				players.get(rs.getInt(1)).acq = rs.getInt(2);
				players.get(rs.getInt(1)).fantasq =  rs.getString(3);
			}
			conn.close();

			for (PL p: players.values()){
				System.out.println(p.nome+"\t"+p.ruolo+"\t"+p.presenze+"\t"+nf.format(p.vmed)+"\t"+nf.format(p.fmed)+"\t"+nf.format(p.amed)+"\t"+p.acq+(p.fantasq!=null?"\t"+p.fantasq:"\t")+"\t"+p.golTot+"\t"+p.golSubitiTot);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
			
	}

}

class PL {
	public String ruolo;
	public String nome;
	public float vtot;
	public float ftot;
	public float atot;
	public float vmed;
	public float fmed;
	public float amed;
	public int presenze;
	public int acq;
	public int golTot;
	public int golSubitiTot;
	public String fantasq;
}