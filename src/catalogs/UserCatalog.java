package src.catalogs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import src.objects.User;
import src.objects.WineForSale;

public class UserCatalog {

	private List<User> userCatalog = new ArrayList<>();

	public UserCatalog() {
		readDatabase();
	}

	public void addUser(String user) {
		User newUser = new User(user);
		userCatalog.add(newUser);
		writeDatabase();
	}

	public User getUser(String username) {

		for (User u : userCatalog) {
			if (u.getUsername().equals(username)) {
				// System.out.println("Entrei!");
				return u;
			}
		}
		return null;
	}

	public List<User> getUserCatalog() {
		return userCatalog;
	}

	public void readDatabase() {

		File usersDB = new File("database.txt");

		try {
			usersDB.createNewFile();
			Scanner dbSc = new Scanner(usersDB);
			while (dbSc.hasNextLine()) {

				String line = dbSc.nextLine();
				String[] atributes = line.split(":");

				String userID = atributes[0];
				double balance = Double.parseDouble(atributes[1]);
				String rawSales = atributes[2];

				ArrayList<WineForSale> sales = new ArrayList<>();

				if (!rawSales.equals(" ")) {
					String[] salesWithUser = rawSales.split(",");

					for (String us : salesWithUser) {
						String[] saleSplitted = us.split("-");
						WineForSale wfs = new WineForSale(userID, saleSplitted[0], Integer.parseInt(saleSplitted[2]),
								Double.parseDouble(saleSplitted[1]));
						sales.add(wfs);
					}
				}
				User user = new User(userID, balance, sales);
				userCatalog.add(user);
			}
			dbSc.close();
		} catch (IOException e) {
			System.out.println("Couldn't find users database file!");
			e.printStackTrace();
		}
	}

	public void writeDatabase() {

		File usersDB = new File("database.txt");

		try {
			FileWriter fileWriter = new FileWriter(usersDB, false);

			for (User user : userCatalog) {
				fileWriter.write(user.getUsername() + ":" + user.getBalance() + ":");
				if (!user.getWinesForSale().isEmpty()) {
					for (WineForSale wfs : user.getWinesForSale()) {
						fileWriter.write(wfs.getWine() + "-" + wfs.getPrice() + "-" + wfs.getQuantity());
						if (userCatalog.indexOf(user) != userCatalog.size() - 1) {
							fileWriter.append(",");
						}
					}
				} else {
					fileWriter.write(" ");
				}
				fileWriter.write(System.lineSeparator());
			}
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Couldn't find users database file!");
			e.printStackTrace();
		}
	}
}