package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Type;
import java.util.*;
import java.time.LocalDateTime;
import java.io.InputStream;

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
    /**
     *   Setup client by creating client implementation instance
     *   to be used in test
     */
    @BeforeEach
    public void setup() {
        clientProps = loadProperties(clientPropsFilename);

        individualClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
        cateringClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
        supermarketClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));

    }

    /**
     *  Test register shielding individual registration use case
     */
    @Test
    public void testShieldingIndividualNewRegistration() {

        // valid date = 10/01/20, length = 10 and all numeric
        String chi = "1001205638";

        // Test registration process
        assertTrue(individualClient.registerShieldingIndividual(chi),"Registration Failed");
        assertTrue(individualClient.isRegistered(), "Not Registered");
        assertEquals(individualClient.getCHI(), chi);

        // Attempt registration again

        String request = "/registerShieldingIndividual?CHI=" + chi ;

        // setup the response recepient
        String response = "";
        try {
            // perform request
            response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ensure that it is already registered on the server
        assertEquals(response,"already registered");
    }

    /**
     *  Test register catering company registration use case
     */
    @Test
    public void testCateringCompanyNewRegistration() {

        String name = "ValidCatering";
        String postCode = "EH8_7NG";

        // Test registration process
        assertTrue(cateringClient.registerCateringCompany(name, postCode));
        assertTrue(cateringClient.isRegistered());
        assertEquals(cateringClient.getName(), name);

        // Attempt registration again
        String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;

        String response = "";

        try {
            // perform request
            response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ensure that it is already registered on the server
        assertEquals(response,"already registered");
    }

    /**
     *  Test register supermarket registration use case
     */
    @Test
    public void testSupermarketNewRegistration() {

        String name = "ValidSupermarket";
        String postCode = "EH8_8NG";

        // Test registration process
        assertTrue(supermarketClient.registerSupermarket(name, postCode));
        assertTrue(supermarketClient.isRegistered());
        assertEquals(supermarketClient.getName(), name);

        // Attempting registration again
        String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;

        String response = "";

        try {
            // perform request
            response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ensure that it is already registered on the server
        assertEquals(response,"already registered");
    }

    /**
     *  Test place order use case in general case
     *  No edit food box involved
     */
    @Test
    public void testPlaceOrderGeneral(){

        // Register individual
        String chi = "1001205639";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register catering company
        String name = "CateringCompany1";
        String postCode = "EH10_7PS";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        // Get a list of food box ids based on dietary preference
        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));

        // Get random food box to be picked
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        // Pick food box and place order
        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        // Ensure that there is order placed and this should be the first one
        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        // Order status should be PLACED
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

    }

    /**
     *  Test place order use case in the case of individual not registered
     *  Should fail
     */
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

    /**
     *  Test place order use case in general case
     *  with edit food box involved
     */
    @Test
    public void testPlaceOrderEditedPickedFoodBox(){

        // Register individual
        String chi = "2010025348";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register company
        String name = "CateringCompany2";
        String postCode = "EH16_3NS";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));
        assertTrue(individualClient.pickFoodBox(randomId));

        // Edit food box
        ArrayList<Integer> itemIDs = new ArrayList<>(individualClient.getItemIdsForFoodBox(randomId));
        int randomFoodId = itemIDs.get(rand.nextInt(itemIDs.size()));

        int quantityBefore = individualClient.getItemQuantityForFoodBox(randomFoodId,randomId);
        int quantityAfter = rand.nextInt(quantityBefore);
        assertTrue(individualClient.changeItemQuantityForPickedFoodBox(randomFoodId,quantityAfter));

        // Place order
        assertTrue(individualClient.placeOrder());

        // Check if successful
        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        int firstOrderNumber = allOrders.get(0);

        assertEquals(individualClient.getItemQuantityForOrder(randomFoodId,firstOrderNumber),quantityAfter);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals(individualClient.getStatusForOrder(firstOrderNumber),"PLACED");

    }

    /**
     * Test edit food box after order is placed
     */
    @Test
    public void testEditFoodBox(){

        // Register individual
        String chi = "1009205128";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register company
        String name = "CateringCompany3";
        String postCode = "EH4_8SD";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        // Choose random food box
        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        // Place and pick order
        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        // Edit food box
        ArrayList<Integer> itemIdsForOrder = new ArrayList<>(individualClient.getItemIdsForOrder(firstOrderNumber));
        int randomItemId = itemIdsForOrder.get(rand.nextInt(itemIdsForOrder.size()));

        int currentQuantity = individualClient.getItemQuantityForOrder(randomItemId,firstOrderNumber);
        int changedQuantity = rand.nextInt(currentQuantity);

        assertTrue(individualClient.setItemQuantityForOrder(randomItemId,changedQuantity,firstOrderNumber));
        assertTrue(individualClient.editOrder(firstOrderNumber));

    }

    /**
     *  Test edit food box after it has been packed
     *  which should fail
     */
    @Test
    public void testEditFooxBoxAfterPacked(){

        // Register individual
        String chi = "1009201438";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register catering company
        String name = "CateringCompany3";
        String postCode = "EH4_8SD";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        // Choose random box
        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        // Pick food box and place order
        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        // Set status as "PACKED"
        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "packed"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PACKED",individualClient.getStatusForOrder(firstOrderNumber));

        // Try to edit food box
        ArrayList<Integer> itemIdsForOrder = new ArrayList<>(individualClient.getItemIdsForOrder(firstOrderNumber));
        int randomItemId = itemIdsForOrder.get(rand.nextInt(itemIdsForOrder.size()));

        int currentQuantity = individualClient.getItemQuantityForOrder(randomItemId,firstOrderNumber);
        int changedQuantity = rand.nextInt(currentQuantity);

        assertTrue(individualClient.setItemQuantityForOrder(randomItemId,changedQuantity,firstOrderNumber));
        assertFalse(individualClient.editOrder(firstOrderNumber));
    }


    /**
     * Test for cancel order use case
     */
    @Test
    public void testCancelOrder(){

        // Register individual
        String chi = "1001207639";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register catering company
        String name = "CateringCompany4";
        String postCode = "EH10_8PS";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        // Choose random food box
        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        // Pick food box and place order
        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        // Cancel order
        assertTrue(individualClient.cancelOrder(firstOrderNumber));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("CANCELLED",individualClient.getStatusForOrder(firstOrderNumber));
    }

    /**
     * Test cancel order when the order is packed
     */
    @Test
    public void testCancelOrderPacked(){

        // Register individual
        String chi = "1001205640";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register catering company
        String name = "CateringCompany5";
        String postCode = "EH10_9PS";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        // Choose random food box
        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        // Pick food box and place order
        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        // Change order status
        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "packed"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PACKED",individualClient.getStatusForOrder(firstOrderNumber));

        // Cancel order
        assertTrue(individualClient.cancelOrder(firstOrderNumber));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("CANCELLED",individualClient.getStatusForOrder(firstOrderNumber));
    }

    /**
     * Test cancel order should fail after the order was dispatched
     */
    @Test
    public void testCancelOrderAfterDispatched(){

        // Register individual
        String chi = "1001205641";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register catering company
        String name = "CateringCompany6";
        String postCode = "EH11_9PS";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        // Choose random food box
        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        // Pick food box and place order
        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        // Change order status
        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "packed"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PACKED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "dispatched"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("DISPATCHED",individualClient.getStatusForOrder(firstOrderNumber));

        // Cancel order should fail
        assertFalse(individualClient.cancelOrder(firstOrderNumber));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("DISPATCHED",individualClient.getStatusForOrder(firstOrderNumber));
    }

    /**
     *  Test that the request order status give the correct order status,
     *  by updating the order status on specific value and check if the requested status is the same
     */
    @Test
    public void testRequestOrderStatus(){

        // Register individual
        String chi = "1010805643";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register catering company
        String name = "CateringCompany8";
        String postCode = "EH13_5PS";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        // Choose random food box
        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        // Pick food box and place dummy order
        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        // Change status using update order endpoint directly with the server (not CateringClientImp function)
        // for dummy order and check if same status
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PLACED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(updateOrderEndpoint(firstOrderNumber, "packed"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("PACKED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(updateOrderEndpoint(firstOrderNumber, "dispatched"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("DISPATCHED",individualClient.getStatusForOrder(firstOrderNumber));

        assertTrue(updateOrderEndpoint(firstOrderNumber, "delivered"));
        assertTrue(individualClient.requestOrderStatus(firstOrderNumber));
        assertEquals("DELIVERED",individualClient.getStatusForOrder(firstOrderNumber));


    }

    // Helper function to interact directly with server
    private boolean updateOrderEndpoint(int orderNumber, String status){
        // construct the endpoint request
        String request = "/updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

        // setup the response recepient

        boolean responseUpdate = false;

        try {
            // perform request
            String response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

            if (response.equals("True") || response.equals(("False"))) {
                responseUpdate = new Gson().fromJson(response, boolean.class);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseUpdate;
    }

    /**
     *  Test that update order status will update the server correctly
     *   and also when we request the status, it will be the same status as the updated one.
    */
    @Test
    public void testUpdateOrderStatus(){

        // Register individual
        String chi = "1001205643";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        // Register catering company
        String name = "CateringCompany9";
        String postCode = "EH13_9PS";
        assertTrue(cateringClient.registerCateringCompany(name, postCode));

        // Choose random food box
        ArrayList<String> foodBoxIds = new ArrayList<>(individualClient.showFoodBoxes("none"));
        Random rand = new Random();
        int randomId = Integer.parseInt(foodBoxIds.get(rand.nextInt(foodBoxIds.size())));

        // Place dummy order
        assertTrue(individualClient.pickFoodBox(randomId));
        assertTrue(individualClient.placeOrder());

        ArrayList<Integer> allOrders = new ArrayList<>(individualClient.getOrderNumbers());
        assertEquals(1,allOrders.size());
        int firstOrderNumber = allOrders.get(0);

        // Update order status with client implementation
        // but check order status directly with server
        assertEquals("PLACED",requestOrderEndpoint(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "packed"));
        assertEquals("PACKED",requestOrderEndpoint(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "dispatched"));
        assertEquals("DISPATCHED",requestOrderEndpoint(firstOrderNumber));

        assertTrue(cateringClient.updateOrderStatus(firstOrderNumber, "delivered"));
        assertEquals("DELIVERED",requestOrderEndpoint(firstOrderNumber));
    }

    // Helper function to help test request order endpoint
    private String requestOrderEndpoint(int orderNumber){
        // construct the endpoint request
        String request = "/requestStatus?order_id=" + orderNumber;

        // setup the response recepient
        String response;

        try {
            response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED";
        }

        switch (response) {
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
     * Test that the function get closest catering company will return the closest one
     */
    @Test
    public void testGetClosestCateringCompany(){

        // register a few company with specific postcode

        String company1 = "Company1";
        String postcode1 = "EH2_9PS";

        String company2 = "Company2";
        String postcode2 = "EH8_9PS";

        String company3 = "Company3";
        String postcode3 = "EH9_9PS";

        assertEquals("registered new", registerCateringCompanyEndpoint(company1, postcode1));
        assertEquals("registered new", registerCateringCompanyEndpoint(company2, postcode2));
        assertEquals("registered new", registerCateringCompanyEndpoint(company3, postcode3));

        // register individual with and change the postcode to something specific
        String chi = "1001205644";
        assertTrue(individualClient.registerShieldingIndividual(chi));

        individualClient.setPostcode("EH1_9PS");

        // check if the the closest catering company is true
        assertEquals("Company1", individualClient.getClosestCateringCompany());
    }

    // Helper function for register catering company directly
    private String registerCateringCompanyEndpoint(String name, String postCode) {

        String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;

        String response = "";

        try {

            response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
}
