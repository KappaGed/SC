package src.objects;

import java.util.ArrayList;

public class User {

	private String user;
	private double balance;
	private ArrayList<WineForSale> winesForSale;

	public User(String user, double balance, ArrayList<WineForSale> winesForSale) {

		this.user = user;
		this.balance = balance;
		this.winesForSale = winesForSale;
	}

	public User(String user) {
		this.user = user;
		this.balance = 200;
		this.winesForSale = new ArrayList<WineForSale>();
	}

	public String getUsername() {
		return user;
	}

	public double getBalance() {
		return balance;
	}

	public ArrayList<WineForSale> getWinesForSale() {
		return winesForSale;
	}

}