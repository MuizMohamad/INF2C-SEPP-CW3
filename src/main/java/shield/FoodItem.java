package shield;

public class FoodItem {


    private int id;
    private String name;
    private int quantity;


    public String getItemName(){
        return name;
    }

    public int getQuantity(){
        return quantity;
    }

    public int getFoodItemID() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void changeQuantity(int quantity){
        this.quantity = quantity;
    }

    public String getFoodItemName(){
        return this.name;
    }


}
