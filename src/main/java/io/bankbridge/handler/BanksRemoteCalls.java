package io.bankbridge.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.model.BankModel;
import io.bankbridge.model.Constants;
import io.bankbridge.model.Views;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BanksRemoteCalls {

    private static final Logger logger = LoggerFactory.getLogger(BanksRemoteCalls.class);
    private static Map<String, String> config;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initializes the configuration by loading bank data from a JSON file.
     *
     * @throws Exception if there is an error during initialization or reading the JSON file
     */
    public static void init() throws Exception {
        config = objectMapper
                .readValue(Thread.currentThread().getContextClassLoader().getResource("banks-v2.json"), Map.class);
    }

    /**
     * Handles the incoming request to retrieve bank models based on filter parameters and pagination,
     * and returns the result as a JSON string.
     *
     * @param request  the request object containing filter and pagination parameters
     * @param response the response object for setting the HTTP status in case of an error or no content
     * @return a JSON string representation of the filtered and paginated list of BankModel objects,
     * or an appropriate message if no results are found or an error occurs
     */
    public static String handle(Request request, Response response) {
        String resultAsString = null;

        try {
            List<BankModel> bankModels = getBanks(request);
            if (!bankModels.isEmpty())
                resultAsString = objectMapper.writerWithView(Views.RemoteCall.class).writeValueAsString(bankModels);
            else {
                response.status(HttpStatus.NO_CONTENT_204);
                resultAsString = "{\"message\": \"No Results Found.\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            resultAsString = Constants.MSG_INTERNAL_SERVER;
        }
        return resultAsString;
    }

    /**
     * Retrieves a list of bank models based on the filter and pagination parameters provided in the request.
     * Fetches bank data from external sources configured in the config map.
     *
     * @param request the request object containing filter and pagination parameters
     * @return a list of BankModel objects that match the filter criteria
     * @throws Exception if there is an error during the process of fetching or parsing bank data
     */
    private static List<BankModel> getBanks(Request request) throws Exception {
        String countryCode = request.queryParams(Constants.QUERY_PARAM_COUNTRYCODE);
        String nameParam = request.queryParams(Constants.QUERY_PARAM_NAME);
        String bicParam = request.queryParams(Constants.QUERY_PARAM_BIC);
        String authParam = request.queryParams(Constants.QUERY_PARAM_AUTH);
        String pageStr = request.queryParams(Constants.QUERY_PARAM_PAGE);
        String pageSizeStr = request.queryParams(Constants.QUERY_PARAM_PAGESIZE);

        int page = StringUtils.isNotBlank(pageStr) ? Integer.parseInt(pageStr) : 0;
        int pageSize = StringUtils.isNotBlank(pageSizeStr) ? Integer.parseInt(pageSizeStr) : Constants.DEFAULT_PAGE_SIZE;

        List<BankModel> bankModels = new ArrayList<>();

        int fromIndex = (page - 1) * pageSize;
        int toIndex = page * pageSize;
        for (Entry<String, String> entry : config.entrySet()) {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpGet httpget = new HttpGet(entry.getValue());
                try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (isSuccessfulResponse(statusCode)) {
                        String responseString = EntityUtils.toString(response.getEntity());
                        BankModel bankModel = objectMapper.readValue(responseString, BankModel.class);
                        if (isValidBankModel(bankModel, countryCode, nameParam, bicParam, authParam)) {
                            bankModels.add(bankModel);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching bank data from {}", entry.getValue(), e);
                throw e;
            }
        }

        return paginate(bankModels, fromIndex, toIndex);
    }

    /**
     * Validates if the given BankModel matches the provided filter parameters.
     *
     * @param bankModel   the bank model to be checked against the filters
     * @param countryCode the country code to filter by, can be blank
     * @param nameParam   the name to filter by, can be blank
     * @param bicParam    the BIC to filter by, can be blank
     * @param authParam   the authorization parameter to filter by, can be blank
     * @return true if the bank model matches all non-blank filter parameters, false otherwise
     */
    private static boolean isValidBankModel(BankModel bankModel, String countryCode, String nameParam, String bicParam, String authParam) {
        return (StringUtils.isBlank(countryCode) || countryCode.equals(bankModel.getCountryCode())) &&
                (StringUtils.isBlank(bicParam) || bicParam.equals(bankModel.getBic())) &&
                (StringUtils.isBlank(authParam) || authParam.equals(bankModel.getAuth())) &&
                (StringUtils.isBlank(nameParam) || bankModel.getName().equals(nameParam));
    }

    /**
     * Checks if the given HTTP status code indicates a successful response.
     *
     * @param statusCode the HTTP status code to check
     * @return true if the status code is 200, 201, or 202, false otherwise
     */
    private static boolean isSuccessfulResponse(int statusCode) {
        return statusCode == HttpStatus.OK_200 || statusCode == HttpStatus.CREATED_201 || statusCode == HttpStatus.ACCEPTED_202;
    }

    /**
     * Paginates the given list of bank models based on the specified indices.
     *
     * @param bankModels the full list of bank models to paginate
     * @param fromIndex  the starting index of the sublist
     * @param toIndex    the ending index of the sublist
     * @return a sublist of BankModel objects based on the specified indices, or an empty list if the indices are out of bounds
     */
    private static List<BankModel> paginate(List<BankModel> bankModels, int fromIndex, int toIndex) {
        int total = bankModels.size();
        if (fromIndex >= 0) {
            toIndex = Math.min(toIndex, total);
            return fromIndex < total ? bankModels.subList(fromIndex, toIndex) : new ArrayList<BankModel>();
        }
        return bankModels;
    }
}
