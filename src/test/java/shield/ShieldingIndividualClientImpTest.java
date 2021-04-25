/**
 *
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;

import java.lang.reflect.Type;
import java.util.*;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClient client;
  private CateringCompanyClient catering;

  private Properties loadProperties(String propsFilename) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Properties props = new Properties();

    try {
      InputStream propsStream = loader.getResourceAsStream(propsFilename);
      props.load(propsStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return props;
  }

  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);

    client = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  public void testShieldingIndividualNewRegistrationValidCHI() {

    Random rand = new Random();
    String chi = String.valueOf(1001994338);

    assertTrue(client.registerShieldingIndividual(chi),"Registration Failed");
  }

  @Test
  public void testShieldingIndividualNewRegistrationInvalidCHIWrongLength() {

    //Random rand = new Random();
    String chi = "10099300";

    assertFalse(client.registerShieldingIndividual(chi),"Registration Should Fail");
  }

  @Test
  public void testShieldingIndividualNewRegistrationInvalidCHIWrongDate() {

    //Random rand = new Random();
    String chi = "4112203400";

    assertFalse(client.registerShieldingIndividual(chi),"Registration Failed");
  }

  @Test
  public void testShowFoodBoxCorrectDiet(){

    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference='";

    // setup the response recepient
    List<FoodBox> responseBoxes = new ArrayList<FoodBox>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

      // unmarshal response
      Type listType = new TypeToken<List<FoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

    } catch (Exception e) {
      e.printStackTrace();
    }

    ArrayList<FoodBox> allFoodBox = new ArrayList<FoodBox>(responseBoxes);

    ArrayList<String> polloFoodBoxIDs = new ArrayList<>(client.showFoodBoxes("pollotarian"));
    for (String id : polloFoodBoxIDs){
      for (FoodBox b : allFoodBox){
        assertEquals(b.getFoodBoxDiet(),"pollotarion");
      }
    }

    ArrayList<String> veganFoodBoxIDs = new ArrayList<>(client.showFoodBoxes("vegan"));
    for (String id : veganFoodBoxIDs){
      for (FoodBox b : allFoodBox){
        assertEquals(b.getFoodBoxDiet(),"vegan");
      }
    }

  }

  @Test
  public void testGetCateringCompanies(){

    // register a few company directly through http request
    // use method to get list and then check
    String company1 = "TestCC1";
    String postcode1 = "EH1_3PN";

    String company2 = "TestCC2";
    String postcode2 = "EH2_3PN";

    String company3 = "TestCC3";
    String postcode3 = "EH3_3PN";

    assertEquals("registered new", registerCateringCompanyEndpoint(company1, postcode1));
    assertEquals("registered new", registerCateringCompanyEndpoint(company2, postcode2));
    assertEquals("registered new", registerCateringCompanyEndpoint(company3, postcode3));

    ArrayList<ArrayList<String>> expected = processCatererList(client.getCateringCompanies());

    ArrayList<ArrayList<String>> answer = new ArrayList<>();

    ArrayList<String> infoCateringCompany1 = new ArrayList<>();
    infoCateringCompany1.add(company1);
    infoCateringCompany1.add(postcode1);

    ArrayList<String> infoCateringCompany2 = new ArrayList<>();
    infoCateringCompany2.add(company2);
    infoCateringCompany2.add(postcode2);

    ArrayList<String> infoCateringCompany3 = new ArrayList<>();
    infoCateringCompany3.add(company3);
    infoCateringCompany3.add(postcode3);

    answer.add(infoCateringCompany1);
    answer.add(infoCateringCompany2);
    answer.add(infoCateringCompany3);

    assertTrue(expected.containsAll(answer));
  }

  private String registerCateringCompanyEndpoint(String name, String postCode) {
    // construct the endpoint request
    String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;

    // setup the response recepient

    String response = "";

    try {
      // perform request
      response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return response;
  }

  private ArrayList<ArrayList<String>> processCatererList(Collection<String> caterersInfo) {

    ArrayList<String> caterersList = new ArrayList<>(caterersInfo);

    caterersList.remove("");

    ArrayList<ArrayList<String>> processedList = new ArrayList<>();

    for (String caterer : caterersList) {
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

  @Test
  public void testGetDistanceWrongPostcodeFormat() {
    String postcode1 = "EH934";
    String postcode2 = "EH8_9PS";

    assertEquals(Float.POSITIVE_INFINITY,client.getDistance(postcode1,postcode2));
  }

  @Test
  public void testGetFoodBoxNumber(){

    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";

    // setup the response recepient
    List<FoodBox> responseBoxes = new ArrayList<>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

      // unmarshal response
      Type listType = new TypeToken<List<FoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

    } catch (Exception e) {
      e.printStackTrace();
    }

    ArrayList<FoodBox> testFoodBox = new ArrayList<>(responseBoxes);

    int test_length = testFoodBox.size();
    client.setDefaultFoodBoxList(testFoodBox);

    assertEquals(test_length,client.getFoodBoxNumber());
  }

  @Test
  public void testGetDietaryPreferenceForFoodBox(){

    // create specific list and use setter
    // check if the dietary preference is the same

    ArrayList<FoodBox> testFoodBoxList = new ArrayList<>();

    // Foodbox 1
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    FoodBox foodBox2 = new FoodBox();
    foodBox2.setDiet("none");
    foodBox2.setId("45");

    FoodBox foodBox3 = new FoodBox();
    foodBox3.setDiet("pollotarian");
    foodBox3.setId("98");

    testFoodBoxList.add(foodBox1);
    testFoodBoxList.add(foodBox2);
    testFoodBoxList.add(foodBox3);

    client.setDefaultFoodBoxList(testFoodBoxList);

    assertEquals("vegan",client.getDietaryPreferenceForFoodBox(120));
    assertEquals("none",client.getDietaryPreferenceForFoodBox(45));
    assertEquals("pollotarian",client.getDietaryPreferenceForFoodBox(98));

  }

  @Test
  public void testGetItemsNumberForFoodBox(){

    // create specific list and use setter
    // check if items number is the same

    ArrayList<FoodBox> testFoodBoxList = new ArrayList<>();

    // Foodbox 1
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    FoodItem foodItem2 = new FoodItem();
    FoodItem foodItem3 = new FoodItem();

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    testFoodBoxList.add(foodBox1);

    client.setDefaultFoodBoxList(testFoodBoxList);

    assertEquals(3,client.getItemsNumberForFoodBox(120));

  }

  @Test
  public void testGetItemIdsForFoodBox(){
    // create specific list and use setter
    // check if items ids is the same

    ArrayList<FoodBox> testFoodBoxList = new ArrayList<>();

    // Foodbox 1
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    foodItem1.setId(3);

    FoodItem foodItem2 = new FoodItem();
    foodItem2.setId(4);

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    testFoodBoxList.add(foodBox1);

    client.setDefaultFoodBoxList(testFoodBoxList);

    List<Integer> testIds = new ArrayList<>();
    testIds.add(3);
    testIds.add(4);
    testIds.add(6);

    assertEquals(testIds,client.getItemIdsForFoodBox(120));
  }

  @Test
  public void testGetItemNameForFoodBox(){
    // create specific food boox list
    // check if item name is the same

    ArrayList<FoodBox> testFoodBoxList = new ArrayList<>();

    // Foodbox 1
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    foodItem1.setId(3);
    foodItem1.setName("Name1");

    FoodItem foodItem2 = new FoodItem();
    foodItem2.setId(4);
    foodItem2.setName("Name2");

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);
    foodItem3.setName("Name3");

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    testFoodBoxList.add(foodBox1);

    client.setDefaultFoodBoxList(testFoodBoxList);

    assertEquals("Name1",client.getItemNameForFoodBox(3,120));
    assertEquals("Name2",client.getItemNameForFoodBox(4,120));
    assertEquals("Name3",client.getItemNameForFoodBox(6,120));

  }

  @Test
  public void testGetItemQuantityForFoodBox(){
    //check if item quantity the same
    ArrayList<FoodBox> testFoodBoxList = new ArrayList<>();

    // Foodbox 1
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    foodItem1.setId(3);
    foodItem1.setQuantity(9);

    FoodItem foodItem2 = new FoodItem();
    foodItem2.setId(4);
    foodItem2.setQuantity(16);

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);
    foodItem3.setQuantity(1);

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    testFoodBoxList.add(foodBox1);

    client.setDefaultFoodBoxList(testFoodBoxList);

    assertEquals(9,client.getItemQuantityForFoodBox(3,120));
    assertEquals(16,client.getItemQuantityForFoodBox(4,120));
    assertEquals(1,client.getItemQuantityForFoodBox(6,120));

  }

  @Test
  public void testPickFoodBox(){

    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";

    // setup the response recepient
    List<FoodBox> responseBoxes = new ArrayList<FoodBox>();

    ArrayList<Integer> boxIds = new ArrayList<>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

      // unmarshal response
      Type listType = new TypeToken<List<FoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (FoodBox b : responseBoxes) {
        int id = Integer.parseInt(b.getFoodBoxID());
        System.out.println(id);
        boxIds.add(id);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    Random rand = new Random();
    System.out.println(boxIds.size());
    int randomId = boxIds.get(rand.nextInt(boxIds.size()));

    client.pickFoodBox(randomId);

    assertEquals(randomId,Integer.parseInt(client.getPickedFoodBox().getFoodBoxID()));

  }

  @Test
  public void testChangeItemQuantityForPickedFoodBox(){
    // check if the item quantity does changed
    ArrayList<FoodBox> testFoodBoxList = new ArrayList<>();

    // Foodbox 1
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    foodItem1.setId(3);
    foodItem1.setQuantity(9);

    FoodItem foodItem2 = new FoodItem();
    foodItem2.setId(4);
    foodItem2.setQuantity(16);

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);
    foodItem3.setQuantity(1);

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    testFoodBoxList.add(foodBox1);

    client.setDefaultFoodBoxList(testFoodBoxList);

    client.pickFoodBox(120);

    assertEquals(9, client.getItemQuantityForFoodBox(3,120));
    assertEquals(16, client.getItemQuantityForFoodBox(4,120));
    assertEquals(1, client.getItemQuantityForFoodBox(6,120));

    client.changeItemQuantityForPickedFoodBox(3, 10);
    client.changeItemQuantityForPickedFoodBox(4, 11);
    client.changeItemQuantityForPickedFoodBox(6, 3);

    assertEquals(10, client.getItemQuantityForFoodBox(3,120));
    assertEquals(11, client.getItemQuantityForFoodBox(4,120));
    assertEquals(3, client.getItemQuantityForFoodBox(6,120));
  }

  @Test
  public void testGetOrderNumbers(){
    // create a order history list and use setter
    // test if same number of order
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    FoodBox foodBox2 = new FoodBox();
    foodBox2.setDiet("vegan");
    foodBox2.setId("121");

    FoodBox foodBox3 = new FoodBox();
    foodBox3.setDiet("vegan");
    foodBox3.setId("122");

    ArrayList<Order> orderHistory = new ArrayList<>();

    Order placedOrder1 = new Order(foodBox1,"0",1);
    orderHistory.add(placedOrder1);
    Order placedOrder2 = new Order(foodBox2,"0",2);
    orderHistory.add(placedOrder2);
    Order placedOrder3 = new Order(foodBox3,"0",3);
    orderHistory.add(placedOrder3);

    client.setOrderHistory(orderHistory);

    ArrayList<Integer> expected = new ArrayList<>();

    expected.add(1);
    expected.add(2);
    expected.add(3);

    assertEquals(3,client.getOrderNumbers().size());
    assertEquals(expected,client.getOrderNumbers());

  }

  @Test
  public void testGetStatusForOrder(){
    // test if order has same status as created
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    ArrayList<Order> orderHistory = new ArrayList<>();

    Order placedOrder1 = new Order(foodBox1,"0",1);
    orderHistory.add(placedOrder1);
    Order placedOrder2 = new Order(foodBox1,"1",2);
    orderHistory.add(placedOrder2);
    Order placedOrder3 = new Order(foodBox1,"2",3);
    orderHistory.add(placedOrder3);
    Order placedOrder4 = new Order(foodBox1,"3",4);
    orderHistory.add(placedOrder4);
    Order placedOrder5 = new Order(foodBox1,"4",5);
    orderHistory.add(placedOrder5);

    client.setOrderHistory(orderHistory);

    assertEquals("PLACED", client.getStatusForOrder(1));
    assertEquals("PACKED", client.getStatusForOrder(2));
    assertEquals("DISPATCHED", client.getStatusForOrder(3));
    assertEquals("DELIVERED", client.getStatusForOrder(4));
    assertEquals("CANCELLED", client.getStatusForOrder(5));
  }

  @Test
  public void testGetItemIdsForOrder(){
    // test same item ids as created object
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    foodItem1.setId(3);

    FoodItem foodItem2 = new FoodItem();
    foodItem2.setId(4);

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    ArrayList<Order> orderHistory = new ArrayList<>();

    Order placedOrder1 = new Order(foodBox1,"0",1);
    orderHistory.add(placedOrder1);

    client.setOrderHistory(orderHistory);

    List<Integer> itemIDS = List.of(3, 4, 6);
    assertEquals(itemIDS, client.getItemIdsForOrder(1));
  }

  @Test
  public void testGetItemNameForOrder(){
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    foodItem1.setId(3);
    foodItem1.setName("Name1");

    FoodItem foodItem2 = new FoodItem();
    foodItem2.setId(4);
    foodItem2.setName("Name2");

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);
    foodItem3.setName("Name3");

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    ArrayList<Order> orderHistory = new ArrayList<>();

    Order placedOrder1 = new Order(foodBox1,"0",1);
    orderHistory.add(placedOrder1);

    client.setOrderHistory(orderHistory);

    assertEquals("Name1", client.getItemNameForOrder(3, 1));
    assertEquals("Name2", client.getItemNameForOrder(4, 1));
    assertEquals("Name3", client.getItemNameForOrder(6, 1));
  }

  @Test
  public void testGetItemQuantityForOrder(){
    // check if item quantity same for order
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    foodItem1.setId(3);
    foodItem1.setQuantity(9);

    FoodItem foodItem2 = new FoodItem();
    foodItem2.setId(4);
    foodItem2.setQuantity(16);

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);
    foodItem3.setQuantity(1);

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    ArrayList<Order> orderHistory = new ArrayList<>();

    Order placedOrder1 = new Order(foodBox1,"0",1);
    orderHistory.add(placedOrder1);

    client.setOrderHistory(orderHistory);

    assertEquals(9, client.getItemQuantityForOrder(3, 1));
    assertEquals(16, client.getItemQuantityForOrder(4, 1));
    assertEquals(1, client.getItemQuantityForOrder(6, 1));
  }

  @Test
  public void testSetItemQuantityForOrder(){
    // check if item quantity is same as changed
    FoodBox foodBox1 = new FoodBox();
    foodBox1.setDiet("vegan");
    foodBox1.setId("120");

    List<FoodItem> test_contents = new ArrayList<>();

    FoodItem foodItem1 = new FoodItem();
    foodItem1.setId(3);
    foodItem1.setQuantity(9);

    FoodItem foodItem2 = new FoodItem();
    foodItem2.setId(4);
    foodItem2.setQuantity(16);

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);
    foodItem3.setQuantity(1);

    test_contents.add(foodItem1);
    test_contents.add(foodItem2);
    test_contents.add(foodItem3);

    foodBox1.setContents(test_contents);

    ArrayList<Order> orderHistory = new ArrayList<>();

    Order placedOrder1 = new Order(foodBox1,"0",1);
    orderHistory.add(placedOrder1);

    client.setOrderHistory(orderHistory);

    assertEquals(9, client.getItemQuantityForOrder(3, 1));
    assertEquals(16, client.getItemQuantityForOrder(4, 1));
    assertEquals(1, client.getItemQuantityForOrder(6, 1));

    client.setItemQuantityForOrder(3, 1, 7);
    client.setItemQuantityForOrder(4, 1, 16);
    client.setItemQuantityForOrder(6, 1, 5);

    assertEquals(7, client.getItemQuantityForOrder(3, 1));
    assertEquals(16, client.getItemQuantityForOrder(4, 1));
    assertEquals(1, client.getItemQuantityForOrder(6, 1));
  }

}
