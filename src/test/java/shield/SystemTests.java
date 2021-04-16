package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Type;
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

        // construct the endpoint request
        String request = "/registerShieldingIndividual?CHI=" + chi ;

        // setup the response recepient
        String response = "";
        try {
            // perform request
            response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        assertEquals(response,"already registered");
    }

    @Test
    public void testCateringCompanyNewRegistration() {

        String name = "ValidCatering";
        String postCode = "EH8_7NG";

        assertTrue(cateringClient.registerCateringCompany(name, postCode));
        assertTrue(cateringClient.isRegistered());
        assertEquals(cateringClient.getName(), name);

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

        assertEquals(response,"already registered");
    }

    @Test
    public void testSupermarketNewRegistration() {

        String name = "ValidSupermarket";
        String postCode = "EH8_8NG";

        assertTrue(supermarketClient.registerSupermarket(name, postCode));
        assertTrue(supermarketClient.isRegistered());
        assertEquals(supermarketClient.getName(), name);

        String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;

        // setup the response recepient

        String response = "";

        try {
            // perform request
            response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(response,"already registered");
    }

    @Test
    public void testPlaceOrderGeneral(){

        String chi = "1001205638";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        String name = "CateringCompany1";
        String postCode = "EH10_7PS";

        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

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

        String name = "CateringCompany2";
        String postCode = "EH16_3NS";

        assertTrue(cateringClient.registerCateringCompany(name, postCode));

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


    @Test
    public void testEditFoodBox(){

        String chi = "1009205638";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        String name = "CateringCompany3";
        String postCode = "EH4_8SD";

        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        int firstOrderNumber = allOrders.get(0);

    }

    // In general case where the order has been packed, it should fail
    @Test
    public void testEditFooxBoxAfterPacked(){

    }


    // In general case where the order has not been dispatched, it should be successful
    @Test
    public void testCancelOrder(){
        String chi = "1001205639";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        String name = "CateringCompany4";
        String postCode = "EH10_8PS";

        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(individualClient.cancelOrder(firstOrderNumber));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("CANCELLED",individualClient.getStatusForOrder(firstOrderNumber));
    }

    // In general case where the order has not been packed, it should be successful
    @Test
    public void testCancelOrderPacked(){
        String chi = "1001205640";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        String name = "CateringCompany5";
        String postCode = "EH10_9PS";

        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "packed"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PACKED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(individualClient.cancelOrder(firstOrderNumber));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("CANCELLED",individualClient.getStatusForOrder(firstOrderNumber));
    }

    // Order cancel should failed if it is cancelled after dispatched
    @Test
    public void testCancelOrderAfterDispatched(){
        String chi = "1001205641";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        String name = "CateringCompany6";
        String postCode = "EH11_9PS";

        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "packed"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PACKED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "dispatched"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("DISPATCHED",individualClient.getStatusForOrder(firstOrderNumber));

        assertFalse(individualClient.cancelOrder(firstOrderNumber));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("DISPATCHED",individualClient.getStatusForOrder(firstOrderNumber));
    }

    // Test that the request order status give the correct order status,
    // by updating the order status on specific value and check if the requested status is the same
    @Test
    public void testRequestOrderStatus(){

    }

    // Test that update order status will update the server correctly
    // and also when we request the status, it will be the same status as the updated one.
    @Test
    public void testUpdateOrderStatus(){
        String chi = "1001205643";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        String name = "CateringCompany8";
        String postCode = "EH13_9PS";

        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "packed"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PACKED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "dispatched"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("DISPATCHED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "delivered"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("DELIVERED",individualClient.getStatusForOrder(firstOrderNumber));
    }
}
