package shield;

public class Order{

    private FoodBox orderedFoodBox;
    private int orderStatus;
    private int orderNumber;

    public FoodBox getOrderedFoodBox(){
        return orderedFoodBox;
    }

    public int getOrderStatus(){
        return orderStatus;
    }

    public int getOrderNumber(){
        return orderNumber;
    }
}
