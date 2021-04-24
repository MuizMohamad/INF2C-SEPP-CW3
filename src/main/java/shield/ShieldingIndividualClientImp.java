/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private String endpoint;

  private String CHI;
  private String dietaryPreference;
  private boolean isRegistered;
  private String postcode;
  private String name;
  private String surname;
  private String phoneNumber;

  private ArrayList<Order> orderHistory;
  private List<FoodBox> defaultFoodBoxList;
  private FoodBox tempPickedFoodBox;


  /**
   * Class constructor
   *
   * @param endpoint client endpoint
   */
  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.isRegistered = false;
    this.dietaryPreference = "none";
    this.orderHistory = new ArrayList<>();
    this.defaultFoodBoxList = getDefaultFoodBoxListFromServer();
  }

  /**
   * Returns true if the operation occurred correctly.
   *
   * This method returns true if the operation occurred correctly (this includes
   * re-registrations) and false if input incorrect (null or CHI number not
   * respecting this format:
   * https://datadictionary.nhs.uk/attributes/community_health_index_number.html)
   * or any of the data retrieved from the server for the shielding individual is
   * null.
   *
   * @param CHI CHI number of the shiedling individual
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean registerShieldingIndividual(String CHI) {

    // If object is null return false
    if (Objects.isNull(CHI)){
      return false;
    }

    // Wrong format return false
    if (!checkCHIFormat(CHI)){
      return false;
    }

    // If already registered return true
    if (isRegistered()){
      return true;
    }


    String request = "/registerShieldingIndividual?CHI=" + CHI ;

    ArrayList<String> responseRegister;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      if (!response.equals("already registered")) {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        responseRegister = new Gson().fromJson(response, listType);

        this.postcode = processPostCode(responseRegister.get(0));
        this.name = responseRegister.get(1);
        this.surname = responseRegister.get(2);
        this.phoneNumber = responseRegister.get(3);

        String printTest = "[" + this.postcode + "]";
      }

      // unmarshal response
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    // Set attribute values
    this.isRegistered = true;
    this.CHI = CHI;

    return true;
  }

  /**
   * Returns collection of food box ids if the operation occurred correctly
   *
   * @param dietaryPreference
   * @return collection of food box ids
   */
  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {

    if (Objects.isNull(dietaryPreference)){
      return new ArrayList<>();
    }
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference ;

    List<FoodBox> responseBoxes;

    List<String> boxIds = new ArrayList<>();

    try {

      String response = ClientIO.doGETRequest(endpoint + request);

      Type listType = new TypeToken<List<FoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // Gather all food box ids
      for (FoodBox b : responseBoxes) {
        boxIds.add(b.getFoodBoxID());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return boxIds;
  }

  /**
   * Place order function to place order on the server
   *
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean placeOrder() {

    if (!isRegistered()){
      return false;
    }

    // Get closest catering company to the individual
    Collection<String> allCatererList = getCateringCompanies();
    ArrayList<ArrayList<String>> caterersInfo = processCatererList(allCatererList);

    String closestCateringName = getClosestCateringCompany();
    String closestCateringPostCode = getPostCodefromCaterersList(caterersInfo,closestCateringName);

    // construct the endpoint request
    String request = "placeOrder?individual_id="+ this.CHI + "&catering_business_name=" + closestCateringName +
            "&catering_postcode=" + closestCateringPostCode ;

    // Convert picked food box to JSON
    Gson gson = new Gson();
    String foodBoxInfoJson = "{\"contents\":"+gson.toJson(tempPickedFoodBox.getContents()) + "}";
    System.out.println(foodBoxInfoJson);
    int responseOrderID = 0;

    try {
      // perform request
      String response = ClientIO.doPOSTRequest(endpoint + request,foodBoxInfoJson);

      // unmarshal response

      responseOrderID = new Gson().fromJson(response, Integer.class);

    } catch (Exception e) {
      e.printStackTrace();
    }

    // Add to order history
    Order placedOrder = new Order(tempPickedFoodBox,"0",responseOrderID);
    orderHistory.add(placedOrder);

    return true;
  }

  /**
   * Edit order function for specific order number
   *
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean editOrder(int orderNumber) {

    // construct the endpoint request
    String request = "/editOrder?order_id=" + orderNumber ;

    boolean responseEdit = false;

    // update the local order status
    requestOrderStatus(orderNumber);
    String orderStatus = getStatusForOrder(orderNumber);

    // if order status not PLACED return false
    if (!orderStatus.equals("PLACED")){
      return false;
    }

    // Get order object
    Order placedOrder = getOrdersOrderNumber(orderNumber);

    // Get edited food box
    FoodBox editedFoodBox = placedOrder.getOrderedFoodBox();
    Gson gson = new Gson();
    String foodBoxInfoJson = "{\"contents\":"+gson.toJson(editedFoodBox.getContents()) + "}";

    try {
      // perform request
      String response = ClientIO.doPOSTRequest(endpoint + request,foodBoxInfoJson);

      // unmarshal response

      responseEdit = new Gson().fromJson(response, boolean.class);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseEdit;
  }

  /**
   * Cancel order for specific order number
   *
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean cancelOrder(int orderNumber) {
    // construct the endpoint request
    String request = "/cancelOrder?order_id=" + orderNumber;

    boolean responseCancel = false;
    if (getStatusForOrder(orderNumber).equals("CANCELLED")){
      return false;
    }

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response

      responseCancel = new Gson().fromJson(response, boolean.class);

    } catch (Exception e) {
      e.printStackTrace();
    }

    Objects.requireNonNull(getOrdersOrderNumber(orderNumber)).setOrderStatus("4");
    return responseCancel;
  }

  /**
   * Update local order status
   *
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean requestOrderStatus(int orderNumber) {

    // construct the endpoint request
    String request = "/requestStatus?order_id=" + orderNumber;

    // setup the response recepient
    String response;

    try {
      // perform request
      response = ClientIO.doGETRequest(endpoint + request);

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    if (response.equals("-1")){
      return false;
    }

    // Set order status in order history
    Objects.requireNonNull(getOrdersOrderNumber(orderNumber)).setOrderStatus(response);

    return true;
  }

  /**
   * Get collection of catering companies and their locations
   *
   * @return collection of catering companies and their locations
   */
  @Override
  public Collection<String> getCateringCompanies() {
    // construct the endpoint request
    String request = "/getCaterers";

    // setup the response recepient

    List<String> responseCaterers = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      System.out.println(response);
      // unmarshal response
      Type listType = new TypeToken<List<String>>() {} .getType();
      responseCaterers = new Gson().fromJson(response, listType);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseCaterers;
  }

  /**
   * Get the distance between two locations based on their post codes from post code
   *
   * @param postCode1 post code of one location
   * @param postCode2 post code of another location
   * @return the distance as a float between the two locations
   */
  @Override
  public float getDistance(String postCode1, String postCode2) {

    Objects.requireNonNull(postCode1);
    Objects.requireNonNull(postCode2);

    if (!checkPostCodeFormat(postCode1) || !checkPostCodeFormat(postCode2)){

      System.out.println("Wrong format, Infinity value will be returned");
      return Float.POSITIVE_INFINITY;

    }

    // construct the endpoint request
    String request = "/distance?postcode1=" + postCode1 + "&postcode2=" + postCode2 ;
    System.out.println(request);
    // setup the response recepient

    float responseDistance = 0;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      responseDistance = new Gson().fromJson(response, float.class);


    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseDistance;
  }

  /**
   * Returns if the individual using the client is registered with the server
   *
   * @return true if the individual using the client is registered with the server
   */
  @Override
  public boolean isRegistered() {
    return this.isRegistered;
  }

  /**
   * Returns the CHI number of the shiedling individual
   *
   * @return CHI number of the shiedling individual
   */
  @Override
  public String getCHI() {
    return this.CHI;
  }

  /**
   * Setter method for default food box list
   * for testing
   *
   * @param defaultFoodBoxList list of food box to be set
   */
  @Override
  public void setDefaultFoodBoxList(ArrayList<FoodBox> defaultFoodBoxList){
    this.defaultFoodBoxList = defaultFoodBoxList;
  }

  /**
   * Returns the number of available food boxes after quering the server
   *
   * @return number of available food boxes after quering the server
   */
  @Override
  public int getFoodBoxNumber() {
    return defaultFoodBoxList.size();
  }

  /**
   * Returns the dietary preference that this specific food box satisfies
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return dietary preference
   */
  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxId);

    // if food box id is not found return empty string
    if (Objects.isNull(foundFoodBox)){
      return "";
    }

    return foundFoodBox.getFoodBoxDiet();
  }

  /**
   * Returns the number of items in this specific food box.
   *
   * This method returns the number of items in each food box (not the quantity
   * of each item).
   * For example if a box has:
   *  - 3 bananas
   *  - 5 bottles of milk
   * it should return 2.
   * @param  foodBoxId the food box id as last returned from the server
   * @return number of items in the food box
   */
  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {

    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxId);

    // if not found return 0
    if (Objects.isNull(foundFoodBox)){
      return 0;
    }

    return foundFoodBox.getContents().size();
  }

  /**
   * Returns the collection of item ids of the requested foodbox
   *
   * @param  foodboxId the food box id as last returned from the server
   * @return collection of item ids of the requested foodbox
   */
  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {

    List<Integer> itemIDs = new ArrayList<>();

    FoodBox foundFoodBox = getFoodBoxfromID(foodboxId);

    assert foundFoodBox != null;
    for (FoodItem f : foundFoodBox.getContents()){
          itemIDs.add(f.getFoodItemID());
    }

    return itemIDs;
  }

  private FoodBox getFoodBoxfromID(int foodBoxId){

    for (FoodBox b : defaultFoodBoxList) {
      int id = Integer.parseInt(b.getFoodBoxID());
      if (id == foodBoxId){
        return b;
      }
    }

    return null;
  }

  /**
   * Returns the item name of the item in the requested foodbox
   *
   * @param  itemId the food box id as last returned from the server
   * @param  foodBoxId the food box id as last returned from the server
   * @return the requested item name
   */
  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {

    FoodItem foundFoodItem = getFoodItemfromID(itemId,foodBoxId);

    // is not found return empty string
    if (Objects.isNull(foundFoodItem)){
      return "";
    }
    return foundFoodItem.getItemName();
  }

  /**
   * Returns the item quantity of the item in the requested foodbox
   *
   * @param  itemId the food box id as last returned from the server
   * @param  foodBoxId the food box id as last returned from the server
   * @return the requested item quantity
   */
  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    FoodItem foundFoodItem = getFoodItemfromID(itemId,foodBoxId);

    // is not found return empty string
    if (Objects.isNull(foundFoodItem)){
      return 0;
    }

    return foundFoodItem.getQuantity();
  }

  private FoodItem getFoodItemfromID(int itemID,int foodBoxID){

    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxID);

    // is not found return null
    if (Objects.isNull(foundFoodBox)){
      return null;
    }

    for (FoodItem f : foundFoodBox.getContents()){
      if(f.getFoodItemID() == itemID){
            return f;
      }
    }
    return null;
  }

  /**
   * Getter method for picked food box
   * for testing
   *
   * @return picked food box
   */
  @Override
  public FoodBox getPickedFoodBox(){
    return tempPickedFoodBox;
  }

  /**
   * Returns true if the requested foodbox was picked.
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return true if the requested foodbox was picked
   */
  @Override
  public boolean pickFoodBox(int foodBoxId) {
    tempPickedFoodBox = getFoodBoxfromID(foodBoxId);

    if(Objects.isNull(tempPickedFoodBox)){
      return false;
    }

    return true;
  }

  /**
   * Returns true if the item quantity for the picked foodbox was changed
   *
   * @param  itemId the food box id as last returned from the server
   * @param  quantity the food box item quantity to be set
   * @return true if the item quantity for the picked foodbox was changed
   */
  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {

    for (FoodItem c : tempPickedFoodBox.getContents()){
      if (c.getFoodItemID() == itemId){
        c.setQuantity(quantity);
      }
    }

    return true;
  }

  /**
   * Setter for order history list
   * for testing
   */
  @Override
  public void setOrderHistory(ArrayList<Order> orderHistory){
    this.orderHistory = orderHistory;
  }

  /**
   * Returns the collection of the order numbers placed.
   *
   * This method queries the order ids for a placed order as stored locally by
   * the client.
   * @return collection of the order numbers placed
   */
  @Override
  public Collection<Integer> getOrderNumbers() {

    List<Integer> orderIDs = new ArrayList<>();

    for (Order o : orderHistory){
      orderIDs.add(o.getOrderNumber());
    }
    return orderIDs;
  }

  /**
   * Returns the status of the order for the requested number.
   *
   * This method queries the status for a placed order as stored locally by
   * the client.
   * @param orderNumber the order number
   * @return status of the order for the requested number
   */
  @Override
  public String getStatusForOrder(int orderNumber) {

    Order chosenOrder = getOrdersOrderNumber(orderNumber);

    // if order not found return empty string
    if (Objects.isNull(chosenOrder)){
      return "";
    }

    switch (chosenOrder.getOrderStatus()) {
      case "0" :
        return "PLACED";
      case "1" :
        return "PACKED";
      case "2" :
        return "DISPATCHED";
      case "3":
        return "DELIVERED";
      case "4" :
        return "CANCELLED";
      default :
        return "NOT EXIST";
    }
  }

  /**
   * Returns the item ids for the items of the requested order.
   *
   * This method queries the item ids for a placed order as stored locally by
   * the client.
   * @param  orderNumber the order number
   * @return item ids for the items of the requested order
   */
  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    Order chosenOrder = getOrdersOrderNumber(orderNumber);

    List<Integer> itemIDs = new ArrayList<Integer>();

    if (Objects.isNull(chosenOrder)){
      return new ArrayList<>();
    }
    FoodBox chosenFoodBox =chosenOrder.getOrderedFoodBox();
    List<FoodItem> foodBoxContent = chosenFoodBox.getContents();

    for (FoodItem i : foodBoxContent){
      itemIDs.add(i.getFoodItemID());
    }
    return itemIDs;
  }

  /**
   * Returns the name of the item for the requested order.
   *
   * This method queries the item name for a placed order as stored locally by
   * the client.
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @return name of the item for the requested order
   */
  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    Order chosenOrder = getOrdersOrderNumber(orderNumber);

    if (Objects.isNull(chosenOrder)){
      return "";
    }
    FoodBox chosenFoodBox =chosenOrder.getOrderedFoodBox();
    List<FoodItem> foodBoxContent = chosenFoodBox.getContents();

    for (FoodItem i : foodBoxContent){
      if (i.getFoodItemID() == itemId){
        return i.getItemName();
      }
    }

    return null;
  }

  /**
   * Returns the quantity of the item for the requested order.
   *
   * This method queries the quantities for a placed order as stored locally by
   * the client.
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @return quantity of the item for the requested order
   */
  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    Order chosenOrder = getOrdersOrderNumber(orderNumber);

    if (Objects.isNull(chosenOrder)){
      return 0;
    }

    FoodBox chosenFoodBox =chosenOrder.getOrderedFoodBox();
    List<FoodItem> foodBoxContent = chosenFoodBox.getContents();

    for (FoodItem i : foodBoxContent){
      if (i.getFoodItemID() == itemId){
        return i.getQuantity();
      }
    }
    return 0;
  }

  private Order getOrdersOrderNumber(int orderNumber){
    for (Order o : orderHistory){
      if (o.getOrderNumber() == orderNumber){
        return o;
      }
    }
    return null;
  }

  /**
   * Returns true if quantity of the item for the requested order was changed.
   *
   * This method changes the quantities for a placed order as stored locally
   * by the client.
   * In order to sync with the server, one needs to call the editOrder()
   * method separately.
   *
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @param  quantity the food box item quantity to be set
   * @return true if quantity of the item for the requested order was changed
   */
  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {

    for (Order o:orderHistory){
      if (o.getOrderNumber() == orderNumber){
        int initialQuantity = o.getOrderedFoodBox().getItemQuantity(itemId);
        if (quantity > initialQuantity){
          return false;
        }
        o.getOrderedFoodBox().changeItemQuantity(itemId,quantity);
      }
    }

    return true;
  }

  /**
   * Getter for individual postcode
   * for testing
   *
   * @return postcode attribute of the individual
   */
  @Override
  public String getPostcode(){
    return this.postcode;
  }

  /**
   * Setter for individual postcode
   * for testing
   *
   * @param postcode postcode to be set
   */
  @Override
  public void setPostcode(String postcode){
    this.postcode = postcode;
  }

  /**
   * Returns closest catering company serving orders based on our location
   *
   * @return business name of catering company
   */
  @Override
  public String getClosestCateringCompany() {

    Collection<String> caterersListCollection = getCateringCompanies();
    ArrayList<ArrayList<String>> caterersInfo = processCatererList(caterersListCollection);

    // initially max distance is infinity
    float currentMinDist = (float) Double.POSITIVE_INFINITY;
    String currentClosestCaterer = "";

    // find minimum
    for (ArrayList<String> info : caterersInfo){

      String catererName = info.get(0);
      String catererPostCode = info.get(1);

      float dist = getDistance(this.postcode,catererPostCode);

      if (dist < currentMinDist){
        currentMinDist = dist;
        currentClosestCaterer = catererName;
      }
    }

    return currentClosestCaterer;
  }

  private List<FoodBox> getDefaultFoodBoxListFromServer(){
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";

    // setup the response recepient
    List<FoodBox> responseBoxes = new ArrayList<>();

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

  private ArrayList<ArrayList<String>> processCatererList(Collection<String> caterersInfo){

    ArrayList<String> caterersList = new ArrayList<>(caterersInfo);

    caterersList.remove("");

    ArrayList<ArrayList<String>> processedList = new ArrayList<>();

    for (String caterer : caterersList){
      String[] catererInfo = caterer.split(",");

      // 0th element is nth caterer which might be useless
      String catererName = catererInfo[1];
      String catererPostCode = catererInfo[2];

      ArrayList<String> infos = new ArrayList<>();
      infos.add(catererName);
      infos.add(catererPostCode);

      processedList.add(infos);
    }

    return processedList;
  }

  private String getPostCodefromCaterersList(ArrayList<ArrayList<String>> caterersInfo, String name){

    for (ArrayList<String> info : caterersInfo){
      if (info.get(0).equals(name)){
        return info.get(1);
      }
    }

    return null;
  }

  private boolean checkCHIFormat(String CHI){

    boolean goodLength = CHI.length() == 10 ;

    if (!goodLength){
      return false;
    }
    boolean validDate = validateJavaDate(CHI.substring(0,6));

    String regex = "[0-9]+";
    Pattern format = Pattern.compile(regex);
    Matcher mt = format.matcher(CHI);

    boolean allDigits = mt.matches();

    boolean result = goodLength && validDate && allDigits;

    return result;
  }

  private boolean validateJavaDate(String strDate) {

    try {
      SimpleDateFormat df = new SimpleDateFormat("ddMMyy");
      df.setLenient(false);
      df.parse(strDate);
      return true;
    } catch (ParseException e) {
      return false;
    }

  }

  private String processPostCode(String postcode){
    String[] splittedPostCode = postcode.split(" ");
    return splittedPostCode[0] + "_" + splittedPostCode[1];
  }

  private boolean checkPostCodeFormat(String postCode){

    String regex = "EH([1-9]|1[0-7])_[1-9][A-Z][A-Z]";
    Pattern format = Pattern.compile(regex);
    Matcher mt = format.matcher(postCode);

    boolean result = mt.matches();

    return result;
  }
}
