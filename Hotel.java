import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.*;
import javax.swing.event.*;

import java.io.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;

class MenuSectionFrame extends JInternalFrame implements ActionListener, InternalFrameListener
{
	boolean resizable = false;
	boolean closable = true;
	boolean maximizable = false;
	boolean iconifiable = true;

	private JSplitPane jsp;
	private JPanel left_panel, right_panel;
	private JLabel lbl, lbl_msg;
	private JTextField txt_search, txt_dish_no, txt_dish_name, txt_dish_price;
	private JList <String> lst_dishes;
	private Vector <String> vector_dish_names;
	private JScrollPane sp;
	private JButton b;

	private static Connection con;

	public MenuSectionFrame()
	{
		//super("MENU SECTION", resizable, closable, maximizable, iconifiable);
		super("MENU SECTION", false, true, false, true);
		// left_panel

		left_panel = new JPanel();
		left_panel.setLayout(null);

		lbl = new JLabel("Search:");
		lbl.setBounds(10, 10, 180, 30);
		left_panel.add(lbl);

		txt_search = new JTextField();
		txt_search.setBounds(10, 50, 180, 30);

		txt_search.addActionListener(
			new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String search_text = txt_search.getText().trim().toUpperCase();
						String sql = "select dish_name from dishes where dish_name like '"+search_text+"%'";
						connect();
						PreparedStatement ps = con.prepareStatement(sql);
						ResultSet rs = ps.executeQuery();
						vector_dish_names.clear();

						while(rs.next())
						vector_dish_names.add(rs.getString("dish_name"));

						rs.close();
						ps.close();
						disconnect();

						lst_dishes.setListData(vector_dish_names);
					}
					catch(Exception ex)
					{
						JOptionPane.showMessageDialog(null, ex);
					}
				}
			});

		left_panel.add(txt_search);

		vector_dish_names = new Vector <String>();
		lst_dishes = new JList <String>(vector_dish_names);

		lst_dishes.addListSelectionListener(
			new ListSelectionListener(){
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					try
					{
						if(vector_dish_names.size() > 0)
						{
							Object dnm = lst_dishes.getSelectedValue();
							if(dnm != null)
							{
								String dish_name = dnm.toString();
								String sql = "select * from dishes where dish_name = ?";
								connect();
								PreparedStatement ps = con.prepareStatement(sql);
								ps.setString(1, dish_name);
								ResultSet rs = ps.executeQuery();
								if(rs.next())							
								{
									int dish_no = rs.getInt("dish_no");
									dish_name = rs.getString("dish_name");
									double dish_price = rs.getDouble("dish_price");

									txt_dish_no.setText(dish_no+"");
									txt_dish_name.setText(dish_name);
									txt_dish_price.setText(dish_price+"");
								}
								rs.close();
								ps.close();
								disconnect();
							}	
						}
					}
					catch(Exception ex)
					{
						JOptionPane.showMessageDialog(null, ex);
					}
				}
		});

		sp = new JScrollPane(lst_dishes);
		sp.setBounds(10, 90, 180, 250);
		left_panel.add(sp);

		// right_panel

		right_panel = new JPanel();
		right_panel.setLayout(null);

		lbl = new JLabel("Dish Number");
		lbl.setBounds(10, 10, 150, 30);
		right_panel.add(lbl);

		txt_dish_no = new JTextField();
		txt_dish_no.setBounds(170, 10, 160, 30);
		txt_dish_no.setEditable(false);
		right_panel.add(txt_dish_no);

		lbl = new JLabel("Dish Name");
		lbl.setBounds(10, 50, 150, 30);
		right_panel.add(lbl);

		txt_dish_name = new JTextField();
		txt_dish_name.setBounds(170, 50, 160, 30);
		right_panel.add(txt_dish_name);

		lbl = new JLabel("Price");
		lbl.setBounds(10, 90, 150, 30);
		right_panel.add(lbl);

		txt_dish_price = new JTextField();
		txt_dish_price.setBounds(170, 90, 160, 30);
		right_panel.add(txt_dish_price);

		b = new JButton("Insert");
		b.setBounds(10, 130, 75, 35);
		right_panel.add(b);
		b.addActionListener(this);

		b = new JButton("Update");
		b.setBounds(95, 130, 75, 35);
		right_panel.add(b);
		b.addActionListener(this);

		b = new JButton("Delete");
		b.setBounds(180, 130, 75, 35);
		right_panel.add(b);
		b.addActionListener(this);

		b = new JButton("Clear");
		b.setBounds(265, 130, 75, 35);
		right_panel.add(b);
		b.addActionListener(this);

		lbl_msg = new JLabel("..");
		lbl_msg.setBounds(10, 175, 330, 30);
		right_panel.add(lbl_msg);

		// split_pane
		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left_panel, right_panel);
		this.add(jsp, BorderLayout.CENTER);
		jsp.setDividerLocation(200);
		jsp.setDividerSize(2);

		this.setVisible(true);
		this.setSize(575, 400);
		this.addInternalFrameListener(this);
		loadDishNames();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			String cmd = e.getActionCommand().toUpperCase();

			int dish_no = 0;
			String dish_name = null;
			double dish_price = 0;
			int n;
			PreparedStatement ps = null;
			String sql, err_msg = "";

			switch(cmd)
			{
				case "INSERT":
					dish_name = txt_dish_name.getText().trim().toUpperCase();
					String dp = txt_dish_price.getText().trim();

					err_msg = "";
					if(dish_name.length() == 0)
					err_msg = err_msg +"Dish Name is required\n";

					if(dp.length() == 0)
					err_msg = err_msg + "Dish Price is required\n";

					if(err_msg.length() == 0)
					{
						dish_price = Double.parseDouble(dp);
						sql = "insert into dishes (dish_name, dish_price) values(?, ?)";
						connect();
						ps = con.prepareStatement(sql);
						ps.setString(1, dish_name);
						ps.setDouble(2, dish_price);
						n = ps.executeUpdate();
						ps.close();
						disconnect();

						if(n == 1)
						{
							lbl_msg.setForeground(Color.BLUE);
							lbl_msg.setText("New Dish Saved..");
							loadDishNames();							
						}
						else
						{
							lbl_msg.setForeground(Color.RED);
							lbl_msg.setText("Oops! not saved..");
						}
					}
					else
					JOptionPane.showMessageDialog(this, err_msg);
					
					break;

				case "UPDATE":
					if(txt_dish_no.getText().trim().length() > 0)
					{
						dish_name = txt_dish_name.getText().trim().toUpperCase();
						dp = txt_dish_price.getText().trim();
						
						err_msg = "";

						if(dish_name.length() == 0)
						err_msg = err_msg + "Dish name is required\n";

						if(dp.length() == 0)
						err_msg = err_msg + "Dish price is required\n";

						if(err_msg.length() == 0)
						{
							dish_no = Integer.parseInt(txt_dish_no.getText());
							dish_price = Double.parseDouble(dp);
							sql = "update dishes set dish_name = ?, dish_price = ? where dish_no =?";
							connect();
							ps = con.prepareStatement(sql);
							ps.setString(1, dish_name);
							ps.setDouble(2, dish_price);
							ps.setInt(3, dish_no);
							n = ps.executeUpdate();
							ps.close();
							disconnect();

							if(n == 1)
							{
								lbl_msg.setForeground(Color.BLUE);
								lbl_msg.setText("Changes saved..");
								loadDishNames();
							}
							else
							{
								lbl_msg.setForeground(Color.RED);
								lbl_msg.setText("Changes not saved..");
							}
						}
						else
						JOptionPane.showMessageDialog(this, err_msg);
					}
					break;

				case "DELETE":
					if(txt_dish_no.getText().trim().length() > 0)
					{
						dish_no = Integer.parseInt(txt_dish_no.getText());
						sql = "delete from dishes where dish_no = ?";
						connect();
						ps = con.prepareStatement(sql);
						ps.setInt(1, dish_no);
						n = ps.executeUpdate();
						ps.close();
						disconnect();

						if(n == 1)
						{
							lbl_msg.setForeground(Color.BLUE);
							lbl_msg.setText("Dish deleted..");
							txt_dish_no.setText("");
							txt_dish_name.setText("");
							txt_dish_price.setText("");
							loadDishNames();
						}
					}
					break;

				case "CLEAR":
					txt_dish_no.setText("");
					txt_dish_name.setText("");
					txt_dish_price.setText("");
					lbl_msg.setText("..");
					break;
			}
		}
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(this, ex);
		}
	}

	public void loadDishNames()
	{
		try
		{
			String sql = "select dish_name from dishes order by dish_name asc";
			connect();
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			vector_dish_names.clear();

			while(rs.next())		
			vector_dish_names.add(rs.getString("dish_name"));
			
			rs.close();
			ps.close();
			disconnect();
			lst_dishes.setListData(vector_dish_names);
		}
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(this, ex);
		}
	}

	public static void connect() throws ClassNotFoundException, SQLException
	{					
		Class.forName("com.mysql.cj.jdbc.Driver");
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hoteldb", "root", "super");		
	}

	public static void disconnect() throws ClassNotFoundException, SQLException
	{		
		if(!con.isClosed())
		con.close();		
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e){}

	@Override
	public void internalFrameActivated(InternalFrameEvent e){}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e){}

	@Override
	public void internalFrameIconified(InternalFrameEvent e){}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e){}

	@Override
	public void internalFrameClosing(InternalFrameEvent e){}

	@Override
	public void internalFrameClosed(InternalFrameEvent e)
	{		
		MainFrame.msf = null;
	}

}

class BillingSectionFrame extends JInternalFrame implements InternalFrameListener
{
	private JPanel left_panel, right_panel;
	private JSplitPane jsp;
	private JScrollPane sp, sp_tbl;
	private JLabel lbl, lbl_msg;
	private JTextField txt_search, txt_table_no, txt_date;
	private JTextField txt_dish_name, txt_dish_price, txt_quantity, txt_total_bill;
	private JList <String> lst_dishes;
	private JTable tbl_bill_items;
	private JButton b;
	private Vector <String> vector_dish_names;
	private static Connection con;
	private Vector <String> cols;
	private Vector <Vector> rows;
	private javax.swing.table.DefaultTableModel tbl_model;

	public BillingSectionFrame()
	{
		//super("BILLING SECTION", resizable, closable, maximizable, iconifiable);
		super("BILLING SECTION", false, true, false, true);
		// left_panel

		left_panel = new JPanel();
		left_panel.setLayout(null);

		lbl = new JLabel("Search:");
		lbl.setBounds(10, 10, 180, 30);
		left_panel.add(lbl);

		txt_search = new JTextField();
		txt_search.setBounds(10, 50, 180, 30);

		txt_search.addActionListener(
			new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						String search_text = txt_search.getText().trim().toUpperCase();
						String sql = "select dish_name from dishes where dish_name like '"+search_text+"%'";
						connect();
						PreparedStatement ps = con.prepareStatement(sql);
						ResultSet rs = ps.executeQuery();
						vector_dish_names.clear();

						while(rs.next())
						vector_dish_names.add(rs.getString("dish_name"));

						rs.close();
						ps.close();
						disconnect();

						lst_dishes.setListData(vector_dish_names);
					}
					catch(Exception ex)
					{
						JOptionPane.showMessageDialog(null, ex);
					}
				}
			});

		left_panel.add(txt_search);

		vector_dish_names = new Vector <String>();
		lst_dishes = new JList <String>(vector_dish_names);

		lst_dishes.addListSelectionListener(
			new ListSelectionListener(){
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					try
					{
						if(vector_dish_names.size() > 0)
						{
							Object dnm = lst_dishes.getSelectedValue();
							if(dnm != null)
							{
								String dish_name = dnm.toString();
								String sql = "select * from dishes where dish_name = ?";
								connect();
								PreparedStatement ps = con.prepareStatement(sql);
								ps.setString(1, dish_name);
								ResultSet rs = ps.executeQuery();
								if(rs.next())							
								{
									int dish_no = rs.getInt("dish_no");
									dish_name = rs.getString("dish_name");
									double dish_price = rs.getDouble("dish_price");

									//txt_dish_no.setText(dish_no+"");
									txt_dish_name.setText(dish_name);
									txt_dish_price.setText(dish_price+"");
								}
								rs.close();
								ps.close();
								disconnect();
							}	
						}
					}
					catch(Exception ex)
					{
						JOptionPane.showMessageDialog(null, ex);
					}
				}
		});

		sp = new JScrollPane(lst_dishes);
		sp.setBounds(10, 90, 180, 250);
		left_panel.add(sp);

		// right_panel
		right_panel = new JPanel();
		right_panel.setLayout(null);

		lbl = new JLabel("New Bill");
		lbl.setBounds(100, 10, 300, 50);
		lbl.setFont(new java.awt.Font("Gabriola", Font.BOLD, 30));
		lbl.setForeground(Color.RED);
		right_panel.add(lbl);

		lbl = new JLabel("Table No.");
		lbl.setBounds(10, 70, 150, 30);
		right_panel.add(lbl);

		txt_table_no = new JTextField();
		txt_table_no.setBounds(170, 70, 100, 30);
		right_panel.add(txt_table_no);

		lbl = new JLabel("Date");
		lbl.setBounds(280, 70, 50, 30);
		right_panel.add(lbl);

		txt_date = new JTextField(new java.util.Date().toString());
		txt_date.setBounds(340, 70, 180, 30);
		right_panel.add(txt_date);


		lbl = new JLabel("Dish Name");
		lbl.setBounds(10, 110, 150, 30);
		right_panel.add(lbl);

		txt_dish_name = new JTextField();
		txt_dish_name.setBounds(170, 110, 150, 30);
		txt_dish_name.setEditable(false);
		right_panel.add(txt_dish_name);

		lbl = new JLabel("Dish Price");
		lbl.setBounds(10, 150, 150, 30);
		right_panel.add(lbl);

		txt_dish_price = new JTextField();
		txt_dish_price.setBounds(170, 150, 150, 30);
		txt_dish_price.setEditable(false);
		right_panel.add(txt_dish_price);

		lbl = new JLabel("Quantity");
		lbl.setBounds(10, 190, 150, 30);
		right_panel.add(lbl);

		txt_quantity = new JTextField();
		txt_quantity.setBounds(170, 190, 150, 30);
		right_panel.add(txt_quantity);
		txt_quantity.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					String q = txt_quantity.getText().trim();
					if(q.length() > 0)
					{
						String dn = txt_dish_name.getText().trim();
						double price = Double.parseDouble(txt_dish_price.getText().trim());
						int qty = Integer.parseInt(q);
						double amount = price * qty;
						Vector <String> one_row = new Vector <String>();
						one_row.add(dn);
						one_row.add(price+"");
						one_row.add(qty+"");
						one_row.add(amount+"");

						rows.add(one_row);
						tbl_model = new javax.swing.table.DefaultTableModel(rows, cols);
						tbl_bill_items.setModel(tbl_model);

						txt_dish_name.setText("");
						txt_dish_price.setText("");
						txt_quantity.setText("");

						double total_bill = Double.parseDouble(txt_total_bill.getText()) + amount;
						txt_total_bill.setText(total_bill+"");
					}
				}
				catch(Exception ex)
				{}
			}
		});

		lbl = new JLabel("Bill Details:");
		lbl.setBounds(10, 250, 150, 30);
		right_panel.add(lbl);

		cols = new Vector <String> ();
		String arr[] = {"DISH NAME", "RATE", "QUANTITY", "AMOUNT"};
		for(int i = 0; i < arr.length; i++)
		cols.add(arr[i]);

		rows = new Vector <Vector>();

	 	tbl_model = new javax.swing.table.DefaultTableModel(rows, cols);
		tbl_bill_items = new JTable(tbl_model);
		sp_tbl = new JScrollPane(tbl_bill_items);
		sp_tbl.setBounds(10, 290, 480, 200);
		right_panel.add(sp_tbl);		
		tbl_bill_items.getTableHeader().setBackground(Color.WHITE);

		lbl = new JLabel("Total Bill");	
		lbl.setBounds(10, 500, 150, 30);
		right_panel.add(lbl);

		txt_total_bill = new JTextField("0");
		txt_total_bill.setBounds(170, 500, 150, 30);
		right_panel.add(txt_total_bill);

		b = new JButton("Save & Print");
		b.setBounds(330, 500, 160, 30);
		right_panel.add(b);
		b.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if(txt_total_bill.getText().trim().equals("0") == false)
					{
						String table_no = txt_table_no.getText().trim().toUpperCase();
						if(table_no.length() > 0)
						{
							String bill_date = txt_date.getText().trim().toUpperCase();
							
							double total_bill = Double.parseDouble(txt_total_bill.getText().trim());
							String sql = "insert into bills (bill_date, table_no, total_bill_amount) values(?, ?, ?)";
							connect();
							PreparedStatement ps = con.prepareStatement(sql);
							ps.setString(1, bill_date);
							ps.setString(2, table_no);
							ps.setDouble(3, total_bill);
							int n = ps.executeUpdate();
							ps.close();
							disconnect();
							
							sql = "select max(bill_no) from bills";
							connect();
							ps = con.prepareStatement(sql);
							ResultSet rs = ps.executeQuery();
							rs.next();
							int max_bill_no = rs.getInt(1);
							rs.close();
							ps.close();
							disconnect();
							// System.out.println("max_bill_no = "+max_bill_no);

							sql = "insert into bill_details values(?, ?, ?, ?, ?)";
							connect();
							ps = con.prepareStatement(sql);							
							for(Vector <String> v : rows)
							{
								String dish_name = v.get(0);
								double dish_price = Double.parseDouble(v.get(1));
								int quantity = Integer.parseInt(v.get(2));
								double amount = Double.parseDouble(v.get(3));

								//System.out.println("\ndish_name = "+dish_name);
								//System.out.println("dish_price = "+dish_price);
								//System.out.println("quantity = "+quantity);
								//System.out.println("amount = "+amount);

								ps.setInt(1, max_bill_no);
								ps.setString(2, dish_name);
								ps.setDouble(3, dish_price);
								ps.setInt(4, quantity);
								ps.setDouble(5, amount);
								n = ps.executeUpdate();
								ps.clearParameters();
							}
							ps.close();
							disconnect();

							if(n > 0)
							{
								// creating pdf of bill
								Document doc = new Document();
								PdfWriter w = PdfWriter.getInstance(doc, new FileOutputStream("bills_pdf/"+max_bill_no+".pdf"));
								doc.open();
								
								// Title
								Font f = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.RED);
								Paragraph p = new Paragraph("*** BILL ***", f);
								p.setAlignment(1);
	           					/*
	           					Alignment 
	           					0 = left
	           					1 = center
	           					2 = right
	           					*/
	           					doc.add(p);

	           					doc.add(new Paragraph("Table Number: "+table_no));
	           					doc.add(new Paragraph("Bill Number: "+max_bill_no));
	           					doc.add(new Paragraph("Bill Date: "+bill_date));
	           					doc.add(new Paragraph("\n\n"));
	           					doc.add(new Paragraph("Bill Details:"));

	           					PdfPTable bill_table = new PdfPTable(4);
            					bill_table.setWidthPercentage(100);
            					bill_table.setSpacingBefore(11f);
            					bill_table.setSpacingAfter(11f);

            					float col_width[] = {3f, 2f, 2f, 2f};
            					bill_table.setWidths(col_width);

            					String cols[] = {"Dish Name", "Dish Rate", "Quantity", "Amount"};

            					for(int i = 0; i < cols.length; i++)
            					{
                					PdfPCell c = new PdfPCell(new Paragraph(cols[i], new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.BOLD, BaseColor.BLACK)));
                					c.setHorizontalAlignment(Element.ALIGN_CENTER);       
                					bill_table.addCell(c);
            					}

            					for(Vector <String> single_row : rows)
            					{
            						for(Object item : single_row)
            						{
            							PdfPCell c = new PdfPCell(new Paragraph((String)item));
                    					c.setHorizontalAlignment(Element.ALIGN_CENTER);       
                    					bill_table.addCell(c);
            						}
            					}

            					doc.add(bill_table);

            					p = new Paragraph("Total Bill Amount: "+total_bill+" Rs.");
            					p.setAlignment(2);
            					doc.add(p);

            					p = new Paragraph("*** Thank You!!! ***");
            					p.setAlignment(1);
            					doc.add(p);

            					p = new Paragraph("*** Visit Again!!!***");
            					p.setAlignment(1);
            					doc.add(p);

	           					doc.close();
           	 					w.close();	
								JOptionPane.showMessageDialog(null, "Bill Saved..");
							}
							else
							JOptionPane.showMessageDialog(null, "Bill Not Saved..");
						}
						else
						JOptionPane.showMessageDialog(null, "Table number is required..");
					}
				}
				catch(Exception ex)
				{
					JOptionPane.showMessageDialog(null, ex);
				}
			}
		});


		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left_panel, right_panel);
		jsp.setDividerLocation(200);
		jsp.setDividerSize(2);
		this.add(jsp, BorderLayout.CENTER);

		this.setVisible(true);
		this.setSize(750, 625);
		this.addInternalFrameListener(this);
		loadDishNames();
	}

	public void loadDishNames()
	{
		try
		{
			String sql = "select dish_name from dishes order by dish_name asc";
			connect();
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			vector_dish_names.clear();

			while(rs.next())		
			vector_dish_names.add(rs.getString("dish_name"));
			
			rs.close();
			ps.close();
			disconnect();
			lst_dishes.setListData(vector_dish_names);
		}
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(this, ex);
		}
	}

	public static void connect() throws ClassNotFoundException, SQLException
	{					
		Class.forName("com.mysql.cj.jdbc.Driver");
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hoteldb", "root", "super");		
	}

	public static void disconnect() throws ClassNotFoundException, SQLException
	{		
		if(!con.isClosed())
		con.close();		
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e){}

	@Override
	public void internalFrameActivated(InternalFrameEvent e){}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e){}

	@Override
	public void internalFrameIconified(InternalFrameEvent e){}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e){}

	@Override
	public void internalFrameClosing(InternalFrameEvent e){}

	@Override
	public void internalFrameClosed(InternalFrameEvent e)
	{		
		MainFrame.bsf = null;
	}
}

class MainFrame extends JFrame implements ActionListener
{
	private JToolBar tbar;
	private JDesktopPane jdp;
	public static MenuSectionFrame msf;
	public static BillingSectionFrame bsf;

	public MainFrame()
	{
		tbar = new JToolBar();
		this.add(tbar, BorderLayout.NORTH);
		tbar.setFloatable(false);

		String arr[] = {"MENU SECTION", "BILLING SECTION"};

		for(int i = 0; i < arr.length; i++)
		{
			JButton b = new JButton(arr[i]);
			tbar.add(b);
			b.addActionListener(this);
		}

		jdp = new JDesktopPane();
		this.add(jdp, BorderLayout.CENTER);

		this.setVisible(true);
		this.setSize(850, 700);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		switch(cmd)
		{
			case "MENU SECTION":
				if(msf == null)
				{
					msf = new MenuSectionFrame();
					jdp.add(msf);
				}
				break;
			case "BILLING SECTION":
				if(bsf == null)
				{
					bsf = new BillingSectionFrame();
					jdp.add(bsf);
				}
				break;
		}
	}
}

class Hotel
{
	public static void main(String[] args) 
	{
		MainFrame f = new MainFrame();	
	}
}



