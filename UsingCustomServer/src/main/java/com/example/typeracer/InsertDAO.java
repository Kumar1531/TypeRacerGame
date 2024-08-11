package com.example.typeracer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InsertDAO {
	
	String qry = "insert into user_details(User_name,User_password,Score_History,Best_WPM) values(?,?,?,?)";
	String url = "jdbc:mysql://localhost:3306/typeraceruserdetails";
	String username = "root";
	String password = "sabari";
	
	public void insert(String uname,String pass) {
		qry = "insert into user_details(User_name,User_password,Score_History,Best_WPM) values(?,?,\"History\",1)";
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			Connection con = DriverManager.getConnection(url,username,password);
			
			PreparedStatement ps = con.prepareStatement(qry);
			
			System.out.println(uname +" "+pass);
			
			ps.setString(1, uname);
			ps.setString(2, pass);
			
			ps.executeUpdate();
			
			System.out.println("Inserted successfully");
			
			con.close();
			ps.close();
			
		} catch (SQLException e) {
			// TODO HANDLE
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public boolean checkPass(String uname,String pass) {
		
		qry = "select * from user_details where User_name = ? and User_password = ?";
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			Connection con = DriverManager.getConnection(url,username,password);
			
			PreparedStatement ps = con.prepareStatement(qry);
			
			ps.setString(1, uname);
			ps.setString(2,pass);
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				return true;
			}
			
			con.close();
			ps.close();
			
		} catch(SQLException e) {
			//TODO HANDLE
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	public boolean checkName(String uname) {
		
		String qry = "select * from user_details where User_name = ?";
		
		try {
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			Connection con = DriverManager.getConnection(url,username,password);
			
			PreparedStatement ps = con.prepareStatement(qry);
			
			ps.setString(1, uname);
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()) {
				return true;
			}
			
			con.close();
			ps.close();
			
		}catch(Exception e) {}
		
		return false;
	}
	
	public String getHistory(String uname) {
		qry = "select * from user_details where User_name = ?";

		String ans = null;

		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = DriverManager.getConnection(url, username, password);

			PreparedStatement ps = con.prepareStatement(qry);

			ps.setString(1, uname);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ans = rs.getString("Score_History");
			}
			con.close();
			ps.close();
		} catch (Exception e) {}

		return ans;
	}
	
	public int getBestWPM(String uname) {
		
		qry = "select * from user_details where User_name = ?";
		
		int ans = 0;
		
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = DriverManager.getConnection(url, username, password);

			PreparedStatement ps = con.prepareStatement(qry);

			ps.setString(1, uname);
			
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ans = rs.getInt("Best_WPM");
			}
			
			con.close();
			ps.close();

		} catch (Exception e) {}
		
		return ans;
		
	}
	
	public void updateWPM(int wpm,String uname) {
		qry = "update user_details set Best_WPM = ? where User_name = ?";
		
		System.out.println("Add history called User name "+uname);
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			Connection con = DriverManager.getConnection(url,username,password);
			
			PreparedStatement ps = con.prepareStatement(qry);
			
			ps.setInt(1, wpm);
			ps.setString(2, uname);
			
			ps.executeUpdate();
			
			con.close();
			ps.close();
			
		} catch (SQLException e) {
			// TODO HANDLE
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addHistory(String uname,String history) {
		
		qry = "update user_details set Score_History = ? where User_name = ?";
		
		System.out.println("Add history called User name "+uname);
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			Connection con = DriverManager.getConnection(url,username,password);
			
			PreparedStatement ps = con.prepareStatement(qry);
			
			ps.setString(1, history);
			ps.setString(2, uname);
			
			ps.executeUpdate();
			
			con.close();
			ps.close();
			
		} catch (SQLException e) {
			// TODO HANDLE
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

