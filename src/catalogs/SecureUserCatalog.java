package src.catalogs;

import src.objects.SecureUser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SecureUserCatalog {

	private Map<String, SecureUser> secureUserCatalog;

	public SecureUserCatalog() {
		this.secureUserCatalog = new HashMap<>();
		readDatabase();
	}

	public SecureUser getUser(String username) {
		return secureUserCatalog.get(username);
	}

	public boolean userExists(String username) {
		return secureUserCatalog.keySet().contains(username);
	}

	public void addUser(SecureUser secureUser) {
		secureUserCatalog.put(secureUser.getUsername(), secureUser);
		writeDatabase();
	}

	public void readDatabase() {

		try {
			File secureUsersFile = new File("users.txt");
			// secureUsersFile.createNewFile();

			Scanner suFileReader = new Scanner(secureUsersFile);

			while (suFileReader.hasNextLine()) {
				String[] lineSplit = suFileReader.nextLine().split(":");

				SecureUser secureUser = new SecureUser(lineSplit[0], lineSplit[1]);
				secureUserCatalog.put(lineSplit[0], secureUser);
			}
			suFileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeDatabase() {

		try {
			File secureUsersFile = new File("users.txt");
			FileWriter fileWriter = new FileWriter(secureUsersFile);

			for (SecureUser secureUser : secureUserCatalog.values()) {

				fileWriter.write(secureUser.getUsername() + ":" + secureUser.getPublicKey());
				fileWriter.write(System.lineSeparator());
				fileWriter.flush();
			}

			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String toString() {

		String result = "";

		for (SecureUser su : secureUserCatalog.values()) {
			result += su.toString() + System.lineSeparator();
		}

		return result;
	}
}
