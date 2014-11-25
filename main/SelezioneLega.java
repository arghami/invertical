package main;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SelezioneLega extends javax.swing.JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel1;
	private JButton annulla;
	private JLabel jLabel1;
	private JButton ok;
	private JButton sfoglia;
	private JTextField filename;

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	public static void build() {
		SelezioneLega inst = new SelezioneLega();
		inst.setVisible(true);
	}

	public SelezioneLega() {
		super();
		initGUI();
	}

	private void initGUI() {
		try {
			this.setTitle("Selezione Lega");
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jPanel1 = new JPanel();
				getContentPane().add(jPanel1, BorderLayout.CENTER);
				jPanel1.setLayout(null);
				jPanel1.setBackground(new java.awt.Color(220,228,231));
				{
					filename = new JTextField();
					jPanel1.add(filename);
					filename.setBounds(35, 70, 189, 21);
				}
				{
					sfoglia = new JButton();
					jPanel1.add(sfoglia);
					sfoglia.setText("Sfoglia...");
					sfoglia.setBounds(245, 66, 85, 28);
					sfoglia.addActionListener(this);
				}
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
					jLabel1.setText("Seleziona il file .fcm della tua lega:");
					jLabel1.setBounds(35, 28, 250, 21);
				}
			}
			pack();
			setBounds(300, 300, 400, 200);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(sfoglia)){
			JFileChooser jFileChooser1 = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Database FCM", "fcm");
			jFileChooser1.setFileFilter(filter);
			int returnVal = jFileChooser1.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				filename.setText(jFileChooser1.getSelectedFile().getPath());
			}
		}
		if (arg0.getSource().equals(ok)){
			CalendariIncrociati.calc(filename.getText(),1);
			this.dispose();
		}
		if (arg0.getSource().equals(annulla)){
			this.dispose();
			System.exit(1);
		}
	}

}
