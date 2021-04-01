package shield;



public class FoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    transient List<FoodItem> contents;

    private String delivered_by;
    private String diet;
    private String id;
    private String name;

    public String getID(){
        return this.id;
    }
}

