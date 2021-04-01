/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private String endpoint;
  private String CHI;
  private String dietaryPreference;
  private boolean isRegistered;
  private ArrayList<Order> orderHistory;
  private List<FoodBox> defaultFoodBoxList;

  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.isRegistered = false;
    this.dietaryPreference = "none";
    this.orderHistory = new ArrayList<Order>();
    this.defaultFoodBoxList = getDefaultFoodBoxListFromServer();
  }

  @Override
  public boolean registerShieldingIndividual(String CHI) {
    // construct the endpoint request
    String request = " /registerShieldingIndividual?CHI=" + CHI + "'";

    // setup the response recepient

    String responseRegister = new String();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      responseRegister = new Gson().fromJson(response, String.class);


    } catch (Exception e) {
      e.printStackTrace();
    }

    this.isRegistered = true;
    this.CHI = CHI;
    return true;
  }

  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference + "'";

    // setup the response recepient
    List<FoodBox> responseBoxes = new ArrayList<FoodBox>();

    List<String> boxIds = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<FoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (FoodBox b : responseBoxes) {
        boxIds.add(b.getFoodBoxID());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return boxIds;
  }

  @Override
  public boolean placeOrder(LocalDateTime deliveryDateTime) {
    return false;
  }

  @Override
  public boolean editOrder(int orderNumber) {
    return false;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    return false;
  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    // construct the endpoint request
    String request = " /requestStatus?order id=" + orderNumber + "'";

    // setup the response recepient

    String responseRegister = new String();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      responseRegister = new Gson().fromJson(response, String.class);


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    // construct the endpoint request
    String request = " /getCaterers";

    // setup the response recepient

    List<String> responseCaterers = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<String>>() {} .getType();
      responseCaterers = new Gson().fromJson(response, listType);


    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseCaterers;
  }

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {
    return 0;
  }

  @Override
  public boolean isRegistered() {
    return this.isRegistered;
  }

  @Override
  public String getCHI() {
    return this.CHI;
  }

  @Override
  public int getFoodBoxNumber() {
    ArrayList<String> boxIds = showFoodBoxes(this.dietaryPreference);
    return boxIds.length();
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxId);
    return foundFoodBox.getFoodBoxDiet();
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {

    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxId);
    return foundFoodBox.getContents().length();
  }



  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {

    List<Integer> itemIDs = new ArrayList<Integer>();

    FoodBox foundFoodBox = getFoodBoxfromID(foodboxId);

    for (FoodItem f : foundFoodBox.getContents()){
          itemIDs.add(f.getFoodItemID());
    }

    return itemIDs;
  }

  private FoodBox getFoodBoxfromID(int foodBoxId){
    for (FoodBox b : defaultFoodBoxList) {
      if (b.getFoodBoxID().equals(foodBoxId)){
        return b;
      }
    }

    return null;
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    FoodItem foundFoodItem = getFoodItemfromID(itemId,foodBoxId);
    return foundFoodItem.getItemName();
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    FoodItem foundFoodItem = getFoodItemfromID(itemId,foodBoxId);
    return foundFoodItem.getQuantity();
  }

  private FoodItem getFoodItemfromID(int itemID,int foodBoxID){

    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxID);

    for (FoodItem f : foundFoodBox.getContents()){
      if(f.getFoodItemID() == itemID){
            return f;
      }
    }
    return null;
  }


  @Override
  public boolean pickFoodBox(int foodBoxId) {
    return false;
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    return false;
  }

  @Override
  public Collection<Integer> getOrderNumbers() {
    return null;
  }

  @Override
  public String getStatusForOrder(int orderNumber) {
    return null;
  }

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    return null;
  }

  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    return null;
  }

  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    return 0;
  }

  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
    return false;
  }

  @Override
  public LocalDateTime getDeliveryTimeForOrder(int orderNumber) {
    return null;
  }

  // **UPDATE**
  @Override
  public String getClosestCateringCompany() {
    return null;
  }

  private List<FoodBox> getDefaultFoodBoxListFromServer(){
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=none";

    // setup the response recepient
    List<FoodBox> responseBoxes = new ArrayList<FoodBox>();

    List<Integer> itemIDs = new ArrayList<Integer>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<FoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseBoxes;
  }

  public void setDietaryPreference(String dietaryPreference){
    this.dietaryPreference = dietaryPreference;
  }
}
