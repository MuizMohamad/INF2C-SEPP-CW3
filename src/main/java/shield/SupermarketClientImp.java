/**
 *
 */

package shield;

import com.google.gson.Gson;

public class SupermarketClientImp implements SupermarketClient {
  private String endpoint;
  private String name;
  private String postCode;
  private boolean isRegistered;

  public SupermarketClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.isRegistered = false;
  }

  @Override
  public boolean registerSupermarket(String name, String postCode) {
    // construct the endpoint request
    String request = " /registerSupermarket?business_name=" + name + "&postcode=" + postCode + "'";

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
    this.name = name;
    this.postCode = postCode;
    return true;
  }

  // **UPDATE**
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    // construct the endpoint request
    String request = " /updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status + "'";

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

    return true;
  }

  @Override
  public boolean isRegistered() {
    return this.isRegistered;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getPostCode() {
    return this.postCode;
  }
}
