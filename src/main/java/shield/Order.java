package shield;

public class Order{

    private FoodBox orderedFoodBox;
    private String orderStatus;
    private int orderNumber;

    public Order(FoodBox orderedFoodBox,String orderStatus,int orderNumber){
        this.orderedFoodBox = orderedFoodBox;
        this.orderStatus = orderStatus;
        this.orderNumber = orderNumber;
    }
    public FoodBox getOrderedFoodBox(){
        return orderedFoodBox;
    }

    public String getOrderStatus(){
        return orderStatus;
    }

    public int getOrderNumber(){
        return orderNumber;
    }

    public void setOrderStatus(String orderStatus){
        this.orderStatus = orderStatus;
    }
}
