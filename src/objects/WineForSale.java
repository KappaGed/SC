package src.objects;

public class WineForSale {

    private String username;
    private String wine;
    private int quantity;
    private double price;

    public WineForSale(String user, String wine, int quantity, double price) {

        this.username = user;
        this.wine = wine;
        this.quantity = quantity;
        this.price = price;
    }

    public String getUsername() {
        return username;
    }

    public String getWine() {
        return wine;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void addQuantity(int addedQuantity) {
		quantity += addedQuantity;
	}

    public void newPrice(double newPrice){

        this.price = newPrice;
    }

    public void newQuantity(int newQuantity){

        this.quantity = newQuantity;
    }
}
