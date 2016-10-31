package libs.foundation.components.page.productfeed.recs;

/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2014 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

import com.adobe.cq.commerce.api.Product;
import com.adobe.cq.commerce.api.CommerceException;
import com.adobe.cq.commerce.api.collection.ProductCollection;
import com.adobe.cq.commerce.api.collection.ProductCollectionManager;
import com.adobe.cq.commerce.pim.api.ProductFeedService;
import com.adobe.cq.commerce.pim.api.ProductListDecorator;
import com.adobe.cq.commerce.pim.common.ProductDataConstants;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;
import io.sightly.java.api.Use;

import java.util.*;
import javax.jcr.RepositoryException;
import javax.script.Bindings;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrUtil;
import org.apache.commons.lang.StringUtils;

/**
 * Use bean for the Sightly scripts that generate the feed for Target Recommendations (google product feed format)
 */
public class RecommendationsFeedPropertiesProvider implements Use {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationsFeedPropertiesProvider.class);

    private static final String CLOUDCONFIG_ROOT = "/etc/cloudservices/testandtarget";
    private static final String PN_LAST_GENERATED = "lastgenerated";

    private ProductFeedService feedService = null;

    private SlingHttpServletRequest request = null;

    private SlingHttpServletResponse response = null;

    private ProductListDecorator productProcessor = null;

    private Configuration configuration = null;

    private ProductCollectionManager productCollectionManager = null;

    private FeedType feedType = FeedType.FULL;

    private long timestamp = 0L;

    private Page rootPage = null;

    /**
     * Initializes the Use-bean. This is called when the data-sly-use property is encountered
     *
     * @param bindings
     */
    public void init(Bindings bindings) {
        LOGGER.debug("Initializing Use-bean...");
        request = (SlingHttpServletRequest) bindings.get(SlingBindings.REQUEST);
        response = (SlingHttpServletResponse) bindings.get(SlingBindings.RESPONSE);
        SlingScriptHelper helper = (SlingScriptHelper) bindings.get(SlingBindings.SLING);

        feedService = helper.getService(ProductFeedService.class);
        productProcessor = helper.getService(ProductListDecorator.class);
        rootPage = (Page) bindings.get("currentPage");

        feedType = FeedType.fromSelectors(request.getRequestPathInfo().getSelectors());
        LOGGER.debug("Feed type is ", feedType.toString());

        // retrieve the cloud configuration so we can write the lastgenerated timestamp
        ValueMap vm = rootPage.getProperties();
        InheritanceValueMap inVm = new HierarchyNodeInheritanceValueMap(vm);

        String[] cloudConfigs = findCloudConfig(rootPage);
        //check for a cloud configuration
        String cloudConfigLocation = null;
        for (String config : cloudConfigs) {
            if (config.startsWith(CLOUDCONFIG_ROOT)) {
                cloudConfigLocation = config;
            }
        }

        productCollectionManager = rootPage.getContentResource().getResourceResolver().adaptTo(ProductCollectionManager.class);

        if (cloudConfigLocation != null) {
            LOGGER.debug("Found cloud service configuration {}", cloudConfigLocation);
            ConfigurationManager configManager = rootPage.getContentResource().getResourceResolver().adaptTo(ConfigurationManager.class);
            configuration = configManager.getConfiguration(cloudConfigLocation);
        }


        if (feedType == FeedType.INCREMENTAL) {
            if (configuration != null
                    && configuration.getProperties() != null) {
                timestamp = configuration.getProperties().get(PN_LAST_GENERATED, 0L);
                LOGGER.debug("Reference timestamp is {} ( {} )", new Date(timestamp), new Date(timestamp));
            }
        }
        LOGGER.debug("Use-bean initialized");
    }


    /**
     * Retrieves the product data
     *
     * @return a {@link List} objects containing a {@link Map} object for every product. The {@link Map} object
     *         contains the product data which will be renedered by the script
     */
    public List<Map<String, Object>> getProductsData() {
        List<Product> products = null;
        List<Map<String, Object>> productsData = null;

        response.setContentType("text/xml");

        if (configuration == null) {
            //no configuration - return an error tag
            return error("No Target cloud configuration found for root page" + rootPage.getPath());
        }

        try {
            products = this.feedType.getProducts(feedService, rootPage, timestamp);

            Map<String, Product> tempProducts = new HashMap<String, Product>();
            // products may not be unique, so let's make it so
            for (Product prod : products) {
                tempProducts.put(prod.getSKU(), prod);
            }
            productsData = productProcessor.process(request, response, new ArrayList<Product>(tempProducts.values()));

            // add ids for the product pages
            for (Map<String, Object> productData : productsData) {
                String pageUrl = productData.get(ProductDataConstants.URL.toString()).toString();
                String priceStr = productData.get(ProductDataConstants.PRICE.toString()).toString();

                pageUrl = pageUrl.replaceAll("\\.html(.*)?", "");
                String recPageId = JcrUtil.createValidName(pageUrl);
                productData.put("recPageId", recPageId);
                productData.put(ProductDataConstants.PRICE.toString(), priceStr.replaceAll("[^0-9\\.,]*", ""));
                Product currentProduct = tempProducts.get(productData.get(ProductDataConstants.SKU.toString()));
                productData.put(ProductDataConstants.URL.toString(), pageUrl + ".html");

                // add product collections
                List<String> collections = new ArrayList<String>();
                if (productCollectionManager != null) {
                    Product pimProduct = null;
                    try {
                        pimProduct = currentProduct.getPIMProduct();
                        if (pimProduct == null) {
                            pimProduct = currentProduct;
                        }
                    } catch (CommerceException ce) {
                        LOGGER.warn("No PIM product found!", ce);
                    }
                    Iterator<ProductCollection> productCollections =  productCollectionManager.getProductCollections(pimProduct);
                    while (productCollections.hasNext()) {
                        ProductCollection productCollection = productCollections.next();
                        collections.add(productCollection.getTitle());
                    }
                }

                collections.addAll(Arrays.asList(currentProduct.getProperty("cq:tags", String[].class)));

                productData.put(ProductDataConstants.TAGS.toString(), StringUtils.join(collections.toArray(new String[0]), ","));
            }

            if (feedType == FeedType.INCREMENTAL) {
                writeLastGeneratedTimestamp();
            }
        } catch (RepositoryException e) {
            return error(e.getMessage());
        }

        LOGGER.debug("Products retrieved {}", products.size());
        return productsData;
    }

    /*
     * Crafts an error message into the structure that the Sightly script accepts.
     */
    private List<Map<String, Object>> error(String message) {
        LOGGER.error(message);
        Map<String, Object> errorMap = new HashMap<String, Object>();
        errorMap.put("error", message);
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        data.add(errorMap);
        return data;
    }

    /*
     * Updates the lastgenerated property of the cloudconfiguration
     */
    private void writeLastGeneratedTimestamp() {
        try {
            LOGGER.debug("Updating last generated timestamp...");
            ResourceResolver resolver = configuration.getResource().getResourceResolver();
            ModifiableValueMap mvm = configuration.getResource().getChild(JcrConstants.JCR_CONTENT).adaptTo
                    (ModifiableValueMap.class);
            mvm.put(PN_LAST_GENERATED, System.currentTimeMillis());
            resolver.commit();
        } catch (Exception e) {
            LOGGER.warn("Could not update the last generated timestamp! ", e);
        }
    }

    /*
     * Ascends recursively through the page hierarchy until it finds a cloudservice configuration definition.
     */
    private String[] findCloudConfig(Page page) {

        String[] configs = page.getProperties().get("cq:cloudserviceconfigs", String[].class);

        if (configs == null) {
            configs = findCloudConfig(page.getParent());
        }

        return configs;
    }

    /*
     * The feed type enum.
     * Each enum entry contains its own feed generation logic.
     */
    private enum FeedType {
        FULL("full") {
            @Override
            public List<Product> getProducts(ProductFeedService service, Page rootPage,
                                             long timestamp) throws RepositoryException {
                return service.getFullProductsList(rootPage);
            }
        },
        INCREMENTAL("incremental") {
            @Override
            public List<Product> getProducts(ProductFeedService service, Page rootPage,
                                             long timestamp) throws RepositoryException {
                LOGGER.debug("Retrieving incremental list, timestamp is {}", timestamp);
                try {
                    return service.getIncrementalProductsList(rootPage, timestamp);
                } catch (RepositoryException e) {
                    LOGGER.error(e.getMessage(), e);
                    return new ArrayList<Product>();
                }

            }
        };

        private String selector;

        private FeedType(String selector) {
            this.selector = selector;
        }

        public String getSelector() {
            return selector;
        }

        /**
         * Retrieves the list of products from the {@link ProductFeedService}
         *
         * @param service   a reference to the {@link ProductFeedService}
         * @param rootPage  the root page for which we retrieve the products
         * @param timestamp the reference timestamp for the incrementalfeed
         * @return a {@link List} of {@link Product} objects
         * @throws RepositoryException
         */
        public abstract List<Product> getProducts(ProductFeedService service, Page rootPage,
                                                  long timestamp) throws RepositoryException;

        /**
         * Determines the feed type from the page's selectors. If the selectors do not contain neither "full",
         * nor "incremental" a default is returned
         *
         * @param selectors the selectors of the page
         * @return the {@link FeedType} object corresponding to the selector. If there no feed type identified among
         * the selectors it returns {@link FeedType#FULL}
         */
        public static FeedType fromSelectors(String[] selectors) {
            for (String selector : selectors) {
                for (FeedType feedType : values()) {
                    if (feedType.getSelector().equals(selector)) {
                        return feedType;
                    }
                }
            }
            // defaults to full
            return FULL;
        }
    }

}
