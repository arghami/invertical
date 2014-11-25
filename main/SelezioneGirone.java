package main;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.WindowConstants;


public class SelezioneGirone extends javax.swing.JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel1;
	private JButton annulla;
	private JComboBox listaGironi;
	private JLabel jLabel1;
	private JButton ok;

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	public static void build(String[] competizioni) {
		SelezioneGirone inst = new SelezioneGirone(competizioni);
		inst.setVisible(true);
	}

	public SelezioneGirone(String[] competizioni) {
		super();
		initGUI(competizioni);
	}

	private void initGUI(String[] gironi) {
		try {
			this.setTitle("Selezione Girone");
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jPanel1 = new JPanel();
				getContentPane().add(jPanel1, BorderLayout.CENTER);
				jPanel1.setLayout(null);
				jPanel1.setBackground(new java.awt.Color(220,228,231));
				{
					annulla = new JButton();
					jPanel1.add(annulla);
					annulla.setText("Annulla");
					annulla.setBounds(91, 119, 85, 28);
					annulla.addActionListener(this);
				}
				{
					ok = new JButton();
					jPanel1.add(ok);
					ok.setText("Ok");
					ok.setBounds(245, 119, 85, 28);
					ok.addActionListener(this);
				}
				{
					jLabel1 = new JLabel();
					jPanel1.add(jLabel1);
					jLabel1.setText("Seleziona il girone:");
					jLabel1.setBounds(35, 28, 250, 21);
				}
				{
					ComboBoxModel listaGironiModel = new DefaultComboBoxModel(gironi);
					listaGironi = new JComboBox();
					jPanel1.add(listaGironi);
					listaGironi.setModel(listaGironiModel);
					listaGironi.setBounds(35, 63, 315, 28);
				}
			}
			pack();
			setBounds(300, 300, 400, 200);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(ok)){
			CalendariIncrociati.calc(listaGironi.getItemAt(listaGironi.getSelectedIndex()).toString(),3);
			this.dispose();
		}
		if (arg0.getSource().equals(annulla)){
			this.dispose();
			System.exit(1);
		}
	}

}
