package com.ivarkronos.trickster.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBController {

	private Connection con;
	private String host, database, username, password;
	private int port;
	
	public DBController() {
		host = "nonyadamnbusiness";
		port = 420;
		database = "nonyadamnbusiness";
		username = "nonyadamnbusiness";
		password = "nonyadamnbusiness";
		
		try {
			openConnection();
			System.out.println("Trickster ::: Successfully connected to the database.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	
	
	private void openConnection() throws SQLException {
		if (con != null && !con.isClosed()) {
			return;
		}
		con = DriverManager.getConnection("jdbc:mysql://"+
				this.host + ":"+
				this.port + "/"+
				this.database, 
				this.username, 
				this.password);
	}
	
	public PreparedStatement prepareStatement (String query) {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ps;
	}
	
	public Connection getConnection() {
		return this.con;
	}
	
	
	
	
	
	
	
}
