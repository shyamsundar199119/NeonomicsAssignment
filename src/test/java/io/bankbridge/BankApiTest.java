package io.bankbridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.bankbridge.model.BankModel;
import io.bankbridge.model.Constants;
import spark.utils.IOUtils;
import static spark.Spark.stop;
import static spark.Spark.awaitInitialization;

//Run MockRemotes before this TestClass 
public class BankApiTest {

	@BeforeClass
    public static void setUp() throws Exception {
		Main.main(null);
		awaitInitialization();
    }

	@AfterClass
    public static void tearDown() throws Exception {
        stop();
    }
	
	private String v1BanksUrl = "/v1/banks/all";
	private String v2BanksUrl = "/v2/banks/all";
	
	@Test
	public void testV1BanksAllSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v1BanksUrl);
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		AssertBanksAreEqual(expectedV1Banks(), returnBanks);
	}
	
	@Test
	public void testV1BanksWithCountryCodeFilterSuccess() throws Exception {
		List<BankModel> expectedBanks = expectedV1BanksForCountryCode();
		TestResponse response = request(Constants.GET, v1BanksUrl + "?countryCode=SE");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});	
		assertEquals(expectedBanks.size(), returnBanks.size());
		AssertBanksAreEqual(expectedBanks, returnBanks);
	}

	@Test
	public void testV1BanksWithBicFilterSuccess() throws Exception {
		List<BankModel> expectedBanks = expectedV1BanksForBIC();
		TestResponse response = request(Constants.GET, v1BanksUrl + "?bic=DODEU8XXX");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(expectedBanks.size(), returnBanks.size());
		AssertBanksAreEqual(expectedBanks, returnBanks);
	}

	@Test
	public void testV1BanksWithNameFilterSuccess() throws Exception {
		List<BankModel> expectedBanks = expectedV1BanksForNameParam();
		TestResponse response = request(Constants.GET, v1BanksUrl + "?name=Credit");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(expectedBanks.size(), returnBanks.size());
		AssertBanksAreEqual(expectedBanks, returnBanks);
	}
	
	@Test
	public void testV1BanksWithPageSizeFilterSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v1BanksUrl + "?page=2&size=2");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(2, returnBanks.size());
		AssertBanksAreEqual(expectedV1BanksForPageSize(), returnBanks);
	}
	
	@Test
	public void testV2BanksAllSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl);
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		AssertBanksAreEqualForV2(expectedV2Banks(), returnBanks);
	}
	
	@Test
	public void testV2BanksWithCountryCodeFilterSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?countryCode=CH");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		AssertBanksAreEqualForV2(expectedV2BanksForCountryCode(), returnBanks);
	}
	
	@Test
	public void testV2BanksWithPageSizeFilterSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?page=3&size=2");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(2, returnBanks.size());
		AssertBanksAreEqualForV2(expectedV2BanksForPageSize(), returnBanks);
	}

	@Test
	public void testV2BanksWithNameFilterSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?name=Mbanken");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(2, returnBanks.size());
		AssertBanksAreEqualForV2(expectedV2BanksForWNameFilter(), returnBanks);
	}

	@Test
	public void testV2BanksWithAuthFilterSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?auth=open-id");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(3, returnBanks.size());
		AssertBanksAreEqualForV2(expectedV2BanksForAuthFilter(), returnBanks);
	}

	@Test
	public void testV2BanksWithBicFilterSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?bic=MOLLITNOR4XXX");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(1, returnBanks.size());
		AssertBanksAreEqualForV2(expectedV2BanksForBicFilter(), returnBanks);
	}

	@Test
	public void testV2BanksWithAuthAndCountryCodeFilterSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?auth=open-id&countryCode=CH");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(2, returnBanks.size());
		AssertBanksAreEqualForV2(expectedV2BanksForAuthAndCountryCodeFilter(), returnBanks);
	}

	@Test
	public void testV2BanksWithAuthAndPageFilterSuccess() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?auth=open-id&page=2&size=2");
		assertEquals(200, response.status);
		assertNotNull(response.body);
		List<BankModel> returnBanks = new ObjectMapper().readValue(response.body,
				new TypeReference<List<BankModel>>() {
				});
		assertEquals(1, returnBanks.size());
		AssertBanksAreEqualForV2(expectedV2BanksForAuthAndPageFilter(), returnBanks);
	}

	@Test
	public void testV2BanksWithCountryCodeNoContent() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?countryCode=IN");
		assertEquals(204, response.status);
		assertNotNull(response.body);
	}
	
	@Test
	public void testV2BanksNoContent() throws Exception {
		TestResponse response = request(Constants.GET, v2BanksUrl + "?page=30&size=2");
		assertEquals(204, response.status);
		assertNotNull(response.body);
	}
	
	private static List<BankModel> expectedV1Banks() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("DOLORENOR9XXX");
		bank.setName("Bank Dolores");
		bank.setCountryCode("NO");
		bank.setAuth("ssl-certificate");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts", "payments")));
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("CONSSWE10XXX");
		bank.setName("Constantie Bank");
		bank.setCountryCode("SE");
		bank.setAuth("oauth");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts", "payments")));
		mockBanks.add(bank);
		return mockBanks;
	}

	private static List<BankModel> expectedV1BanksForBIC() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("DODEU8XXX");
		bank.setName("Bank Dariatur");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts", "payments")));
		mockBanks.add(bank);
		return mockBanks;
	}
	private static List<BankModel> expectedV1BanksForNameParam() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("SOARCDEU18XXX");
		bank.setName("Soar Credit Union");
		bank.setCountryCode("DE");
		bank.setAuth("oauth");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts", "payments")));
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("CUPIDATATSP1XXX");
		bank.setName("Credit Sweets");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts", "payments")));
		mockBanks.add(bank);
		return mockBanks;
	}
	
	private static List<BankModel> expectedV1BanksForCountryCode() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("CONSSWE10XXX");
		bank.setName("Constantie Bank");
		bank.setCountryCode("SE");
		bank.setAuth("oauth");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts", "payments")));
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("MOLLITSWE5XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("SE");
		bank.setAuth("oauth");
		bank.setProducts(new ArrayList<String>( Arrays.asList("payments")));
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("ETSWE19XXX");
		bank.setName("Cash Financial");
		bank.setCountryCode("SE");
		bank.setAuth("oauth");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts")));
		mockBanks.add(bank);
		return mockBanks;
	}
	
	private static List<BankModel> expectedV1BanksForPageSize() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("FIRSTSP15XXX");
		bank.setName("First Guarantee Group");
		bank.setCountryCode("PT");
		bank.setAuth("ssl-certificate");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts", "payments")));
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("DODEU8XXX");
		bank.setName("Bank Dariatur");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		bank.setProducts(new ArrayList<String>( Arrays.asList("accounts", "payments")));
		mockBanks.add(bank);
		return mockBanks;
	}
	
	private static List<BankModel> expectedV2Banks() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("DOLORENOR2XXX");
		bank.setName("Royal Bank of Fun");
		bank.setCountryCode("GB");
		bank.setAuth("oauth");
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("CUPIDATATSP1XXX");
		bank.setName("Credit Sweets");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		return mockBanks;
	}
	
	private static List<BankModel> expectedV2BanksForCountryCode() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("CUPIDATATSP1XXX");
		bank.setName("Credit Sweets");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("DODEU8XXX");
		bank.setName("Bank Dariatur");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		return mockBanks;
	}
	
	private static List<BankModel> expectedV2BanksForPageSize() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("MOLLITNOR4XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("NO");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("MOLLITSWE5XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("SE");
		bank.setAuth("oauth");
		mockBanks.add(bank);
		return mockBanks;
	}

	private static List<BankModel> expectedV2BanksForWNameFilter() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("MOLLITNOR4XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("NO");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("MOLLITSWE5XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("SE");
		bank.setAuth("oauth");
		mockBanks.add(bank);

		return mockBanks;
	}


	private static List<BankModel> expectedV2BanksForAuthAndCountryCodeFilter() {
		List<BankModel> mockBanks = new ArrayList<>();

		BankModel bank = new BankModel();
		bank.setBic("CUPIDATATSP1XXX");
		bank.setName("Credit Sweets");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		mockBanks.add(bank);

		bank = new BankModel();
		bank.setBic("DODEU8XXX");
		bank.setName("Bank Dariatur");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		mockBanks.add(bank);

		return mockBanks;
	}

	private static List<BankModel> expectedV2BanksForAuthFilter() {
		List<BankModel> mockBanks = new ArrayList<>();

		BankModel bank = new BankModel();
		bank.setBic("CUPIDATATSP1XXX");
		bank.setName("Credit Sweets");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("MOLLITNOR4XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("NO");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("DODEU8XXX");
		bank.setName("Bank Dariatur");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		mockBanks.add(bank);

		return mockBanks;
	}

	private static List<BankModel> expectedV2BanksForAuthAndPageFilter() {
		List<BankModel> mockBanks = new ArrayList<>();

		BankModel bank = new BankModel();
		bank.setBic("DODEU8XXX");
		bank.setName("Bank Dariatur");
		bank.setCountryCode("CH");
		bank.setAuth("open-id");
		mockBanks.add(bank);

		return mockBanks;
	}

	private static List<BankModel> expectedV2BanksForBicFilter() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("MOLLITNOR4XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("NO");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		return mockBanks;
	}

	private static List<BankModel> expectedV2BanksForNameFilter() {
		List<BankModel> mockBanks = new ArrayList<>();
		BankModel bank = new BankModel();
		bank.setBic("MOLLITNOR4XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("NO");
		bank.setAuth("open-id");
		mockBanks.add(bank);
		bank = new BankModel();
		bank.setBic("MOLLITSWE5XXX");
		bank.setName("Mbanken");
		bank.setCountryCode("SE");
		bank.setAuth("oauth");
		mockBanks.add(bank);
		return mockBanks;
	}
	
	private static void AssertBanksAreEqual(List<BankModel> expected, List<BankModel> actual) {
		int i = 0;
		for (BankModel expectedBank : expected) {
			assertEquals(expectedBank.getBic(), actual.get(i).getBic());
			assertEquals(expectedBank.getName(), actual.get(i).getName());
			assertEquals(expectedBank.getCountryCode(), actual.get(i).getCountryCode());
			AssertBankProductsAreEqual(expectedBank.getProducts(), actual.get(i).getProducts());
		    i++;
		}
	}

	private static void AssertBanksAreEqualForV2(List<BankModel> expected, List<BankModel> actual) {
		int i = 0;
		for (BankModel expectedBank : expected) {
			assertEquals(expectedBank.getBic(), actual.get(i).getBic());
			assertEquals(expectedBank.getName(), actual.get(i).getName());
			assertEquals(expectedBank.getCountryCode(), actual.get(i).getCountryCode());
			assertEquals(expectedBank.getAuth(), actual.get(i).getAuth());
			i++;
		}
	}
	
	private static void AssertBankProductsAreEqual(ArrayList<String> expected, ArrayList<String> actual) {
		int i = 0;
		for (String expectedProduct : Optional.ofNullable(expected).orElse(new ArrayList<>())) {
			assertEquals(expectedProduct, actual.get(i));
		    i++;
		}	
	}
	
	private TestResponse request(String method, String path) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL("http://localhost:8080" + path);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setDoOutput(true);
			connection.connect();
			String body = IOUtils.toString(connection.getInputStream());
			return new TestResponse(connection.getResponseCode(), body);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(connection != null)
				connection.disconnect();
		}
	}
	
	private static class TestResponse {

		public final String body;
		public final int status;

		public TestResponse(int status, String body) {
			this.status = status;
			this.body = body;
		}
	}
}
