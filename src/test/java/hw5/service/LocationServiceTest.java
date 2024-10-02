package hw5.service;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
//import hw5.IntegrationConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
//@Import(IntegrationConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationServiceTest {

//    @BeforeAll
//    public void setup() {
//
//    }

}
