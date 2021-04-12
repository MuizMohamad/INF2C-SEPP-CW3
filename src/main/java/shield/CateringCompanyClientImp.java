/**
 *
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CateringCompanyClientImp implements CateringCompanyClient {
  private String endpoint;
  private String name;
  private String postCode;
  private boolean isRegistered;

  public CateringCompanyClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.isRegistered = false;
  }

  @Override
  public boolean registerCateringCompany(String name, String postCode) {

    if (checkPostCodeFormat(postCode) == false){
      return false;
    }
    // construct the endpoint request
    String request = " /registerCateringCompany?business_name=" + name + "&postcode=" + postCode + "'";

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

  private boolean checkPostCodeFormat(String postCode){

    String regex = "EH[1-17]_[1-9][A-Z][A-Z]";
    Pattern format = Pattern.compile(regex);
    Matcher mt = format.matcher(postCode);

    boolean result = mt.matches();

    return result;
  }
}
