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
  public void testPlaceFoodBoxOrder() {
    Assertions.fail();
  }


}
