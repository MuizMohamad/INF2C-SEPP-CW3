/**
 *
 */

package shield;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SupermarketClientImp implements SupermarketClient {
  private String endpoint;
  private String name;
  private String postCode;
  private boolean isRegistered;

  /*
   * Class constructor
   *
   * @param endpoint
   */
  public SupermarketClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.isRegistered = false;
  }

  /**
   * Method to register supermarket
   *
   * @param name name of the business
   * @param postCode post code of the business
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean registerSupermarket(String name, String postCode) {

    if (!checkPostCodeFormat(postCode)){
      return false;
    }

    if (isRegistered()){
      return true;
    }
    // construct the endpoint request
    String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;

    // setup the response recepient

    String responseRegister = new String();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    this.isRegistered = true;
    this.name = name;
    this.postCode = postCode;
    return true;
  }

  /**
   * Method to record supermarket order on server
   *
   * @param CHI CHI number of the shiedling individual associated with this order
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {

    // construct the endpoint request
    String request = "/recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber
            + "&supermarket_business_name=" +  this.name + "&supermarket_postcode=" + this.postCode;

    // setup the response recepient

    boolean responseRecord = false;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      if (response.equals("True") || response.equals(("False"))) {
        responseRecord = new Gson().fromJson(response, boolean.class);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseRecord;
  }

  /**
   * Method to update order status of order placed
   *
   * @param orderNumber the order number
   * @param status status of the order for the requested number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    // construct the endpoint request
    String request = "/updateSupermarketOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;

    // setup the response recepient

    boolean responseUpdate = false;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      if (response.equals("True") || response.equals(("False"))) {
        responseUpdate = new Gson().fromJson(response, boolean.class);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return responseUpdate;
  }

  /*
   * Return if the company is registered or not
   *
   * @return isRegistered
   */
  @Override
  public boolean isRegistered() {
    return this.isRegistered;
  }

  /*
   * Getter for catering company name
   *
   * @return name
   */
  @Override
  public String getName() {
    return this.name;
  }

  /*
   * Getter for catering company post code
   *
   * @return post code
   */
  @Override
  public String getPostCode() {
    return this.postCode;
  }

  private boolean checkPostCodeFormat(String postCode){

    String regex = "EH([1-9]|1[0-7])_[1-9][A-Z][A-Z]";
    Pattern format = Pattern.compile(regex);
    Matcher mt = format.matcher(postCode);

    boolean result = mt.matches();

    return result;
  }
}
