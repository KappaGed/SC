package src.catalogs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import src.objects.WineForSale;

public class WineForSaleCatalog {

	private List<WineForSale> wineForSaleList;

	public WineForSaleCatalog() {
		wineForSaleList = new ArrayList<>();
		readDatabase();
	}

	public List<WineForSale> getWineForSaleList() {
		return wineForSaleList;
	}

	public void add(WineForSale wineForSale) {
		wineForSaleList.add(wineForSale);
	}

	public List<WineForSale> getAllByWineName(String wine) {
		ArrayList<WineForSale> winesForSale = new ArrayList<>();
		for (WineForSale wineForSale : wineForSaleList) {
			if (wineForSale.getWine().equals(wine)) {
				winesForSale.add(wineForSale);
			}
		}

		// if(winesForSale)

		return winesForSale;
	}

	public void readDatabase() {

		try {

			File file = new File("WineForSale.txt");
			file.createNewFile();

			Scanner reader = new Scanner(file);

			while (reader.hasNextLine()) {
				String line = reader.nextLine();

				String[] splittedLine = line.split(":");

				String username = splittedLine[0];
				String winename = splittedLine[1];
				double price = Double.parseDouble(splittedLine[2]);
				int quantity = Integer.parseInt(splittedLine[3]);

				WineForSale wine = new WineForSale(username, winename, quantity, price);

				wineForSaleList.add(wine);
			}

			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void writeDatabase() {

		try {

			File file = new File("WineForSale.txt");
			FileWriter writer = new FileWriter(file, false);

			for (WineForSale wine : wineForSaleList) {

				writer.write(
						wine.getUsername() + ":" + wine.getWine() + ":" + wine.getPrice() + ":" + wine.getQuantity());
				writer.append(System.lineSeparator());
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
