/**
 *
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;

import java.lang.reflect.Type;
import java.util.*;
import java.time.LocalDateTime;
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
    String chi = String.valueOf(1001205638);

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
  }

  @Test
  public void testGetDistanceWrongPostcodeFormat() {
    String postcode1 = "EH934";
    String postcode2 = "EH8_9PS";

    assertEquals(0,client.getDistance(postcode1,postcode2));
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
    foodItem1.setQuantity(16);

    FoodItem foodItem3 = new FoodItem();
    foodItem3.setId(6);
    foodItem1.setQuantity(1);

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

    // check if pickFoodBox is the same
    ArrayList<FoodBox> testFoodBoxList = new ArrayList<>();


  }

  @Test
  public void testChangeItemQuantityForPickedFoodBox(){
    // check if the item quantity does changed
  }

  @Test
  public void testGetOrderNumbers(){
    // create a order history list and use setter
    // test if same number of order
  }

  @Test
  public void testGetStatusForOrder(){
    // test if order has same status as created
  }

  @Test
  public void testGetItemIdsForOrder(){
    // test same item ids as created object
  }

  @Test
  public void testGetItemNameForOrder(){
    // check if item name same as order
  }

  @Test
  public void testGetItemQuantityForOrder(){
    // check if item quantity same for order
  }

  @Test
  public void testSetItemQuantityForOrder(){
    // check if item quantity is same as changed
  }




}
