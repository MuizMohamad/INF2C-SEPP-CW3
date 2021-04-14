/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class CateringCompanyClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private CateringCompanyClient client;

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

    client = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  public void testCateringCompanyNewRegistrationValidPostcodeFormat() {

    String name = "ValidPostcodeCompany";
    String postCode = "EH8_7NG";

    assertTrue(client.registerCateringCompany(name, postCode));
  }

  @Test
  public void testCateringCompanyNewRegistrationInvalidPostcodeFormat(){
    String name = "InvalidPostcodeCompany";
    String postCode = "128_7NG";

    assertFalse(client.registerCateringCompany(name, postCode));
  }
}
