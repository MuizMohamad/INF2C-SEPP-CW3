package shield;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

public class SystemTests {

    private final static String clientPropsFilename = "client.cfg";

    private Properties clientProps;
    private ShieldingIndividualClient individualClient;
    private CateringCompanyClient cateringClient;
    private SupermarketClient supermarketClient;

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

        individualClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
        cateringClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
        supermarketClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));

    }

    @Test
    public void testShieldingIndividualNewRegistration() {

        //Random rand = new Random();
        // valid date = 10/01/20, length = 10 and all numeric
        String chi = "1001205638";

        assertTrue(individualClient.registerShieldingIndividual(chi),"Registration Failed");
        assertTrue(individualClient.isRegistered(), "Not Registered");
        assertEquals(individualClient.getCHI(), chi);
    }

    @Test
    public void testCateringCompanyNewRegistration() {
        Random rand = new Random();
        String name = String.valueOf(rand.nextInt(10000));
        String postCode = String.valueOf(rand.nextInt(10000));

        assertTrue(cateringClient.registerCateringCompany(name, postCode));
        assertTrue(cateringClient.isRegistered());
        assertEquals(cateringClient.getName(), name);
    }

    @Test
    public void testSupermarketNewRegistration() {
        Random rand = new Random();
        String name = String.valueOf(rand.nextInt(10000));
        String postCode = String.valueOf(rand.nextInt(10000));

        assertTrue(supermarketClient.registerSupermarket(name, postCode));
        assertTrue(supermarketClient.isRegistered());
        assertEquals(supermarketClient.getName(), name);
    }

    @Test
    public void testPlaceOrderGeneral(){

        String chi = "1001205638";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(allOrders.size(),1);
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals(individualClient.getStatusForOrder(firstOrderNumber),"PLACED");

    }

    @Test
    public void testPlaceOrderNotRegistered(){

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));
        assertTrue(individualClient.pickFoodBox(randomId));
        assertFalse(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(allOrders.size(),0);
    }

    @Test
    public void testPlaceOrderEditedPickedFoodBox(){

        String chi = "2010025348";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));
        assertTrue(individualClient.pickFoodBox(randomId));

        ArrayList<Integer> itemIDs = new ArrayList<>(individualClient.getItemIdsForFoodBox(randomId));
        int randomFoodId = itemIDs.get(rand.nextInt(itemIDs.size()));

        int quantityBefore = individualClient.getItemQuantityForFoodBox(randomFoodId,randomId);
        int quantityAfter = rand.nextInt(quantityBefore);
        assertTrue(individualClient.changeItemQuantityForPickedFoodBox(randomFoodId,quantityAfter));

        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        int firstOrderNumber = allOrders.get(0);

        assertEquals(individualClient.getItemQuantityForOrder(randomFoodId,firstOrderNumber),quantityAfter);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals(individualClient.getStatusForOrder(firstOrderNumber),"PLACED");

    }
}
