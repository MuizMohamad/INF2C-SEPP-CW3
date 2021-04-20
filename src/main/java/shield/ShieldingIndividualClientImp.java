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

  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.isRegistered = false;
    this.dietaryPreference = "none";
    this.orderHistory = new ArrayList<>();
    this.defaultFoodBoxList = getDefaultFoodBoxListFromServer();
  }

  @Override
  public boolean registerShieldingIndividual(String CHI) {

    Objects.requireNonNull(CHI);

    if (!checkCHIFormat(CHI)){
      return false;
    }

    if (isRegistered()){
      return true;
    }

    // construct the endpoint request
    String request = "/registerShieldingIndividual?CHI=" + CHI ;

    // setup the response recepient

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

    this.isRegistered = true;
    this.CHI = CHI;

    return true;
  }

  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference ;

    // setup the response recepient
    List<FoodBox> responseBoxes;

    List<String> boxIds = new ArrayList<>();

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

  // **UPDATE2** REMOVED PARAMETER
  @Override
  public boolean placeOrder() {

    if (!isRegistered()){
      return false;
    }

    Collection<String> allCatererList = getCateringCompanies();
    ArrayList<ArrayList<String>> caterersInfo = processCatererList(allCatererList);

    String closestCateringName = getClosestCateringCompany();
    String closestCateringPostCode = getPostCodefromCaterersList(caterersInfo,closestCateringName);
    // construct the endpoint request
    String request = "placeOrder?individual_id="+ this.CHI + "&catering_business_name=" + closestCateringName +
            "&catering_postcode=" + closestCateringPostCode ;

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

    Order placedOrder = new Order(tempPickedFoodBox,"0",responseOrderID);
    orderHistory.add(placedOrder);

    return true;
  }

  @Override
  public boolean editOrder(int orderNumber) {
    // construct the endpoint request
    String request = "/editOrder?order_id=" + orderNumber ;

    boolean responseEdit = false;

    requestOrderStatus(orderNumber);
    String orderStatus = getStatusForOrder(orderNumber);
    if (!orderStatus.equals("PLACED")){
      return false;
    }

    Order placedOrder = getOrdersOrderNumber(orderNumber);

    FoodBox editedFoodBox = placedOrder.getOrderedFoodBox();
    Gson gson = new Gson();
    String foodBoxInfoJson = "{\"contents\":"+gson.toJson(editedFoodBox.getContents()) + "}";
    System.out.println(foodBoxInfoJson);

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
    Objects.requireNonNull(getOrdersOrderNumber(orderNumber)).setOrderStatus(response);

    return true;
  }

  // **UPDATE**
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

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {

    if (!checkPostCodeFormat(postCode1) || !checkPostCodeFormat(postCode2)){
      return 0;
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

  @Override
  public boolean isRegistered() {
    return this.isRegistered;
  }

  @Override
  public String getCHI() {
    return this.CHI;
  }

  @Override
  public void setDefaultFoodBoxList(ArrayList<FoodBox> defaultFoodBoxList){
    this.defaultFoodBoxList = defaultFoodBoxList;
  }

  @Override
  public int getFoodBoxNumber() {
    return defaultFoodBoxList.size();
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxId);
    assert foundFoodBox != null;
    return foundFoodBox.getFoodBoxDiet();
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {

    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxId);
    assert foundFoodBox != null;
    return foundFoodBox.getContents().size();
  }

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

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    FoodItem foundFoodItem = getFoodItemfromID(itemId,foodBoxId);
    assert foundFoodItem != null;
    return foundFoodItem.getItemName();
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    FoodItem foundFoodItem = getFoodItemfromID(itemId,foodBoxId);
    assert foundFoodItem != null;
    return foundFoodItem.getQuantity();
  }

  private FoodItem getFoodItemfromID(int itemID,int foodBoxID){

    FoodBox foundFoodBox = getFoodBoxfromID(foodBoxID);

    assert foundFoodBox != null;
    for (FoodItem f : foundFoodBox.getContents()){
      if(f.getFoodItemID() == itemID){
            return f;
      }
    }
    return null;
  }

  @Override
  public FoodBox getPickedFoodBox(){
    return tempPickedFoodBox;
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    tempPickedFoodBox = getFoodBoxfromID(foodBoxId);
    Objects.requireNonNull(tempPickedFoodBox);
    return true;
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {

    for (FoodItem c : tempPickedFoodBox.getContents()){
      if (c.getFoodItemID() == itemId){
        c.changeQuantity(quantity);
      }
    }

    return true;
  }

  @Override
  public void setOrderHistory(ArrayList<Order> orderHistory){
    this.orderHistory = orderHistory;
  }

  @Override
  public Collection<Integer> getOrderNumbers() {

    List<Integer> orderIDs = new ArrayList<>();

    for (Order o : orderHistory){
      orderIDs.add(o.getOrderNumber());
    }
    return orderIDs;
  }

  @Override
  public String getStatusForOrder(int orderNumber) {

    Order chosenOrder = getOrdersOrderNumber(orderNumber);
    Objects.requireNonNull(chosenOrder);
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

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    Order chosenOrder = getOrdersOrderNumber(orderNumber);

    List<Integer> itemIDs = new ArrayList<Integer>();

    assert chosenOrder != null;
    FoodBox chosenFoodBox =chosenOrder.getOrderedFoodBox();
    List<FoodItem> foodBoxContent = chosenFoodBox.getContents();

    for (FoodItem i : foodBoxContent){
      itemIDs.add(i.getFoodItemID());
    }
    return itemIDs;
  }

  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    Order chosenOrder = getOrdersOrderNumber(orderNumber);

    assert chosenOrder != null;
    FoodBox chosenFoodBox =chosenOrder.getOrderedFoodBox();
    List<FoodItem> foodBoxContent = chosenFoodBox.getContents();

    for (FoodItem i : foodBoxContent){
      if (i.getFoodItemID() == itemId){
        return i.getItemName();
      }
    }

    return null;
  }

  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    Order chosenOrder = getOrdersOrderNumber(orderNumber);

    assert chosenOrder != null;
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

  @Override
  public String getPostcode(){
    return this.postcode;
  }

  @Override
  public void setPostcode(String postcode){
    this.postcode = postcode;
  }

  // **UPDATE**
  @Override
  public String getClosestCateringCompany() {

    Collection<String> caterersListCollection = getCateringCompanies();
    ArrayList<ArrayList<String>> caterersInfo = processCatererList(caterersListCollection);

    float currentMinDist = (float) Double.POSITIVE_INFINITY;
    String currentClosestCaterer = "";

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
    assert goodLength;
    boolean validDate = validateJavaDate(CHI.substring(0,6));

    String regex = "[0-9]+";
    Pattern format = Pattern.compile(regex);
    Matcher mt = format.matcher(CHI);

    boolean allDigits = mt.matches();

    boolean result = goodLength && validDate && allDigits;

    return result;
  }

  private boolean validateJavaDate(String strDate) {

    SimpleDateFormat format = new SimpleDateFormat("ddMMyy");

    try {
      Date javaDate = format.parse(strDate);
    }
    /* Date format is invalid */
    catch (ParseException e)
    {
      return false;
    }
      /* Return true if date format is valid */
    return true;
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
