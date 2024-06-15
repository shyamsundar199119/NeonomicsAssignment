package io.bankbridge.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;
import io.bankbridge.model.Constants;
import io.bankbridge.model.Views;
import org.eclipse.jetty.http.HttpStatus;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BanksCacheBased {


    private static final Logger logger = LoggerFactory.getLogger(BanksCacheBased.class);
    private static CacheManager cacheManager;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initializes the cache manager and loads bank data into the cache.
     *
     * @throws Exception if there is an error during initialization or loading cache data
     */
    public static void init() throws Exception {
        cacheManager = CacheManagerBuilder
                .newCacheManagerBuilder().withCache("banks", CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(String.class, BankModel.class, ResourcePoolsBuilder.heap(20)))
                .build();
        cacheManager.init();
        Cache cache = cacheManager.getCache("banks", String.class, BankModel.class);
        try {
            BankModelList models = objectMapper.readValue(
                    Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), BankModelList.class);
            for (BankModel model : models.getBanks()) {
                cache.put(model.getBic(), model);
            }
        } catch (Exception e) {
            logger.error("Error loading cache data", e);
            throw e;
        }
    }

    /**
     * Handles the incoming request to filter and paginate bank models, and returns the result as a JSON string.
     *
     * @param request  the request object containing filter and pagination parameters
     * @param response the response object for setting the HTTP status in case of an error
     * @return a JSON string representation of the filtered and paginated list of BankModel objects
     */
    public static String handle(Request request, Response response) {

        try {
            List<BankModel> filteredBanks = filterBanks(request);
            List<BankModel> bankModel = getBanksBasedOnPagination(request, filteredBanks);

            return objectMapper.writerWithView(Views.Cache.class).writeValueAsString(bankModel);
        } catch (Exception e) {
            logger.error("Error processing request", e);
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            return Constants.MSG_INTERNAL_SERVER;
        }
    }

    /**
     * Filters the list of banks based on the query parameters provided in the request.
     *
     * @param request the request object containing filter parameters
     * @return a list of BankModel objects that match the filter criteria
     */
    private static List<BankModel> filterBanks(Request request) {
        String countryCodeParam = request.queryParams(Constants.QUERY_PARAM_COUNTRYCODE);
        String nameParam = request.queryParams(Constants.QUERY_PARAM_NAME);
        String bicParam = request.queryParams(Constants.QUERY_PARAM_BIC);
        String productParam = request.queryParams(Constants.QUERY_PARAM_PRODUCT);

        List<BankModel> result = new ArrayList<>();
        Cache<String, BankModel> cache = cacheManager.getCache("banks", String.class, BankModel.class);

        cache.forEach(entry -> {
            BankModel bankModel = entry.getValue();
            if (matchesFilter(bankModel, countryCodeParam, nameParam, bicParam, productParam)) {
                result.add(bankModel);
            }
        });

        return result;
    }

    /**
     * Checks if the given BankModel matches the provided filter parameters.
     *
     * @param bankModel the bank model to be checked against the filters
     * @param countryCodeParam the country code to filter by, this variable can be blank
     * @param nameParam the name to filter by, this variable can be blank
     * @param bicParam the BIC to filter by, this variable can be blank
     * @param productParam the product to filter by, this variable can be blank
     * @return true if the bank model matches all non-blank filter parameters, false otherwise
     */
    private static boolean matchesFilter(BankModel bankModel, String countryCodeParam, String nameParam, String bicParam, String productParam) {
        return (StringUtils.isBlank(countryCodeParam) || countryCodeParam.equals(bankModel.getCountryCode())) &&
                (StringUtils.isBlank(bicParam) || bicParam.equals(bankModel.getBic())) &&
                (StringUtils.isBlank(productParam) || bankModel.getProducts().contains(productParam)) &&
                (StringUtils.isBlank(nameParam) || bankModel.getName().contains(nameParam));
    }

    /**
     * Retrieves a sublist of {@link BankModel} objects based on pagination parameters provided in the request.
     *
     * @param request the request object containing pagination parameters
     * @param banks   the full list of banks to paginate
     * @return a sublist of banks based on the specified page and page size
     */
    private static List<BankModel> getBanksBasedOnPagination(Request request, List<BankModel> banks) {

        String pageStr = request.queryParams(Constants.QUERY_PARAM_PAGE);
        String pageSizeStr = request.queryParams(Constants.QUERY_PARAM_PAGESIZE);

        int page = StringUtils.isNotBlank(pageStr) ? Integer.parseInt(pageStr) : 0;
        int pageSize = StringUtils.isNotBlank(pageSizeStr) ? Integer.parseInt(pageSizeStr) : Constants.DEFAULT_PAGE_SIZE;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = page * pageSize;
        if (fromIndex >= 0 && toIndex > 0)
            return banks.subList(fromIndex, toIndex);
        return banks;
    }


}
