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

public class SupermarketClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private SupermarketClient client;

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

    client = new SupermarketClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  public void testSupermarketNewRegistrationValidPostcodeFormat() {

    String name = "ValidPostcodeCompany";
    String postCode = "EH8_7NG";

    assertTrue(client.registerSupermarket(name, postCode));
  }

  @Test
  public void testSupermarketNewRegistrationInvalidPostcodeFormat(){
    String name = "InvalidPostcodeCompany";
    String postCode = "128_7NG";

    assertFalse(client.registerSupermarket(name, postCode));
  }

  // TODO add test record supermarket order

}
