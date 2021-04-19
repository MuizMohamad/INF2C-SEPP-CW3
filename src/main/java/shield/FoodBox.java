package shield;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class FoodBox {


    // a field marked as transient is skipped in marshalling/unmarshalling
    private List<FoodItem> contents;

    private String delivered_by;
    private String diet;
    private String id;
    private String name;

    public List<FoodItem> getContents(){
        return contents;
    }

    public String getFoodBoxID(){
        return this.id;
    }

    public String getFoodBoxName(){
        return name;
    }

    public String getFoodBoxDiet(){
        return diet;
    }

    public String getDelivered_by(){
        return delivered_by;
    }

    public void setContents(List<FoodItem> contents) {
        this.contents = contents;
    }

    public void setDelivered_by(String delivered_by) {
        this.delivered_by = delivered_by;
    }

    public void setDiet(String diet) {
        this.diet = diet;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void changeItemQuantity(int itemIds, int quantity){
        for (FoodItem i : contents){
            if (i.getFoodItemID() == itemIds){
                i.changeQuantity(quantity);
            }
        }
    }

    public int getItemQuantity(int itemIds){
        for (FoodItem i : contents){
            if (i.getFoodItemID() == itemIds){
                return i.getQuantity();
            }
        }

        return 0;
    }
}

