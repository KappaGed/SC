package src.catalogs;

import src.objects.Wine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class WineCatalog {

	private List<Wine> wineCatalog = new ArrayList<>();

	public WineCatalog() {
		readWinesDB();
	}

	public void addWine(Wine wine) {
		wineCatalog.add(wine);
	}

	public List<Wine> getWineCatalog() {
		return wineCatalog;
	}

	public Boolean wineExists(Wine wine) {
		if (wineCatalog.contains(wine)) {
			return true;
		} else {
			return false;
		}
	}

	public Wine getWine(String wineName) {
		for (Wine wine : wineCatalog) {
			if (wine.getName().equals(wineName)) {
				return wine;
			}
		}
		return null;
	}

	public void readWinesDB() {

		File wineDB = new File("wines.txt");
		try {
			boolean fileCreated = wineDB.createNewFile();
			if (fileCreated) {
				System.out.println("Wine database file was created");
			}
			Scanner dbSc = new Scanner(wineDB);
			while (dbSc.hasNextLine()) {

				String line = dbSc.nextLine();
				String[] atributes = line.split(":");

				String name = atributes[0];
				String imgPath = atributes[1];
				String rawRatings = atributes[2];
				int exemplares = Integer.parseInt(atributes[3]);

				Map<String, Integer> rating = new HashMap<>();

				if (!rawRatings.equals(" ")) {
					String[] ratingWithUser = rawRatings.split(",");

					for (String ur : ratingWithUser) {
						String[] idkbroitstoomuch = ur.split("-");
						rating.put(idkbroitstoomuch[0], Integer.parseInt(idkbroitstoomuch[1]));
					}
				}
				Wine wine = new Wine(name, imgPath, rating, exemplares);
				addWine(wine);
			}
			dbSc.close();
		} catch (IOException e) {
			System.out.println("Couldn't find wine database file!");
			e.printStackTrace();
		}
	}

	public void writeWineDB() {

		File wineDB = new File("wines.txt");
		try {
			FileWriter fileWriter = new FileWriter(wineDB, false);
			fileWriter.write("");
			for (Wine wine : wineCatalog) {
				fileWriter.append(wine.getName() + ":" + wine.getImagePath() + ":");
				if (!wine.getRatingHM().isEmpty()) {
					Map<String, Integer> map = wine.getRatingHM();
					ArrayList<String> keys = new ArrayList<>(map.keySet());
					for (String key : keys) {
						fileWriter.append(key + "-" + map.get(key));
						if (keys.indexOf(key) != keys.size() - 1) {
							fileWriter.append(",");
						}
					}
				} else {
					fileWriter.append(" ");
				}
				fileWriter.append(":" + wine.getQuantity());

				fileWriter.append(System.lineSeparator());
			}
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Couldn't find wine database file!");
			e.printStackTrace();
		}

	}
}
