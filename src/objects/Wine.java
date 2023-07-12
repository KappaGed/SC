package src.objects;

import java.util.HashMap;
import java.util.Map;

public class Wine {

	private String wineName;
	private String imagePath;
	private Map<String, Integer> classification;
	private int quantity;

	public Wine(String wineName, String imagePath) {

		this.wineName = wineName;
		this.imagePath = imagePath;
		this.classification = new HashMap<>();
		this.quantity = 0;
	}

	public Wine(String wineName, String imagePath, Map<String, Integer> rating, int exemplares) {
		this.wineName = wineName;
		this.imagePath = imagePath;
		this.classification = rating;
		this.quantity = exemplares;

	}

	public String getName() {

		return wineName;
	}

	public String getImagePath() {

		return imagePath;
	}

	public Map<String, Integer> getRatingHM() {
		return classification;
	}

	public int getQuantity() {
		return quantity;
	}

	public void addQuantity(int addedQuantity) {
		quantity += addedQuantity;
	}

	public Double getClassification() {

		double sumClassifications = 0;

		for (int i : classification.values()) {

			sumClassifications += i;
		}
		return (double) (sumClassifications / classification.values().size());
	}

	public void setRating(String user, int rating){

		this.classification.put(user, rating);
	}
}