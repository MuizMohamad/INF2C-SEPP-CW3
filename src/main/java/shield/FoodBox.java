package shield;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/*
 * Class representation of food box
 */
public class FoodBox {


    // a field marked as transient is skipped in marshalling/unmarshalling
    private List<FoodItem> contents;

    private String delivered_by;
    private String diet;
    private String id;
    private String name;

    /*
     * Get food box contents
     *
     * @return content
     */
    public List<FoodItem> getContents(){
        return contents;
    }

    /*
     * Get food box id
     *
     * @return id
     */
    public String getFoodBoxID(){
        return this.id;
    }

    /*
     * Get food box name
     *
     * @return name
     */
    public String getFoodBoxName(){
        return name;
    }

    /*
     * Get food box diet
     *
     * @return diet
     */
    public String getFoodBoxDiet(){
        return diet;
    }

    /*
     * Get food box delivered by
     *
     * @return delivered_by
     */
    public String getDelivered_by(){
        return delivered_by;
    }

    /*
     * Set food box content
     *
     * @param contents
     */
    public void setContents(List<FoodItem> contents) {
        this.contents = contents;
    }

    /*
     * Set food box delivered by
     *
     * @param delivered by
     */
    public void setDelivered_by(String delivered_by) {
        this.delivered_by = delivered_by;
    }

    /*
     * Set food box's diet
     *
     * @param diet
     */
    public void setDiet(String diet) {
        this.diet = diet;
    }

    /*
     * Set food box id
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /*
     * Set food box name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * Change item quantity from item id of content
     *
     * @param item id
     * @param quantity
     */
    public void changeItemQuantity(int itemIds, int quantity){
        for (FoodItem i : contents){
            if (i.getFoodItemID() == itemIds){
                i.setQuantity(quantity);
            }
        }
    }

    /*
     * Get item quantity with item id from content
     *
     * @param item id
     * @return quantity
     */
    public int getItemQuantity(int itemIds){
        for (FoodItem i : contents){
            if (i.getFoodItemID() == itemIds){
                return i.getQuantity();
            }
        }

        return 0;
    }
}

