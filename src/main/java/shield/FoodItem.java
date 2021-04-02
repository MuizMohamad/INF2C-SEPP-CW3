package shield;

public class FoodItem {

    private String name;
    private int quantity;
    private int id;

    public String getItemName(){
        return name;
    }

    public int getQuantity(){
        return quantity;
    }

    public int getFoodItemID() {
        return id;
    }

    public void changeQuantity(int quantity){
        this.quantity = quantity;
    }


}
