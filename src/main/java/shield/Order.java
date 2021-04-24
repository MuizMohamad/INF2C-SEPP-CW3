package shield;

/*
 * Class representation for Order
 */
public class Order{

    private FoodBox orderedFoodBox;
    private String orderStatus;
    private int orderNumber;

    /*
     * Class constructor
     *
     * @param orderedFoodBox
     * @param orderStatus
     * @param orderNumber
     *
     */
    public Order(FoodBox orderedFoodBox,String orderStatus,int orderNumber){
        this.orderedFoodBox = orderedFoodBox;
        this.orderStatus = orderStatus;
        this.orderNumber = orderNumber;
    }

    /*
     * Getter for ordered food box
     *
     * @return ordered food box
     */
    public FoodBox getOrderedFoodBox(){
        return orderedFoodBox;
    }

    /*
     * Getter for order status
     *
     * @return order status
     */
    public String getOrderStatus(){
        return orderStatus;
    }

    /*
     * Getter for order number
     *
     * @return order number
     */
    public int getOrderNumber(){
        return orderNumber;
    }

    /*
     * Setter for order status
     *
     * @param order status
     */
    public void setOrderStatus(String orderStatus){
        this.orderStatus = orderStatus;
    }
}
