package shield;

/*
 * Representation of food item class
 */
public class FoodItem {


    private int id;
    private String name;
    private int quantity;


    /*
     * Get food item quantity
     *
     * @return quantity
     */
    public int getQuantity(){
        return quantity;
    }

    /*
     * Get food item id
     *
     * @return id
     */
    public int getFoodItemID() {
        return id;
    }

    /*
     * Set food item id
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /*
     * Set food item name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * Set food item quantity
     *
     * @param quantity
     */
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    /*
     * Get food item name
     *
     * @return name
     */
    public String getItemName(){
        return this.name;
    }


}
