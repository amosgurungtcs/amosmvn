package libs.foundation.components.page.productfeed.sp;

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
import com.adobe.cq.commerce.pim.api.ProductFeedService;
import com.adobe.cq.commerce.pim.api.ProductListDecorator;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.webservicesupport.Configuration;
import com.day.cq.wcm.webservicesupport.ConfigurationManager;
import io.sightly.java.api.Use;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Use bean for the Sightly scripts that generate the feed in S+P format
 */
public class FeedPropertiesProvider implements Use {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedPropertiesProvider.class);

    private static final String CLOUDCONFIG_ROOT = "/etc/cloudservices/search-promote";
    private static final String PN_LAST_GENERATED = "lastgenerated";

    private ProductFeedService feedService = null;

    private SlingHttpServletRequest request = null;

    private SlingHttpServletResponse response = null;

    private ProductListDecorator productProcessor = null;

    private Configuration configuration = null;

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

        ResourceResolver resolver = request.getResourceResolver();

        feedService = helper.getService(ProductFeedService.class);
        productProcessor = helper.getService(ProductListDecorator.class);
        rootPage = (Page) bindings.get("currentPage");

        feedType = FeedType.fromSelectors(request.getRequestPathInfo().getSelectors());
        LOGGER.debug("Feed type is ", feedType.toString());

        // retrieve the cloud configuration so we can write the lastgenerated timestamp
        ValueMap vm = rootPage.getProperties();
        InheritanceValueMap inVm = new HierarchyNodeInheritanceValueMap(vm);

        String[] cloudConfigs = findCloudConfig(rootPage);
        //do we have a S+P cloud configuration?
        String cloudConfigLocation = null;
        for (String config : cloudConfigs) {
            if (config.startsWith(CLOUDCONFIG_ROOT)) {
                cloudConfigLocation = config;
            }
        }

        if (cloudConfigLocation != null) {
            LOGGER.debug("Found cloud service configuration {}", cloudConfigLocation);
            ConfigurationManager configManager = resolver.adaptTo(ConfigurationManager.class);
            configuration = configManager.getConfiguration(cloudConfigLocation);
        }


        if (feedType == FeedType.INCREMENTAL) {
            if (configuration != null) {
                timestamp = configuration.getProperties().get(PN_LAST_GENERATED, Long.class);
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
            return error("No Search&Promote cloud configuration found for root page" + rootPage.getPath());
        }

        try {
            products = this.feedType.getProducts(feedService, rootPage, timestamp);

            Map<String, Product> tempProducts = new HashMap<String, Product>();
            // products may not be unique, so let's make it so
            for (Product prod : products) {
                tempProducts.put(prod.getSKU(), prod);
            }
            productsData = productProcessor.process(request, response, new ArrayList<Product>(tempProducts.values()));
            writeLastGeneratedTimestamp();
        } catch (RepositoryException e) {
            return error(e.getMessage());
        } catch (PersistenceException e) {
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
    private void writeLastGeneratedTimestamp() throws PersistenceException {
        LOGGER.debug("Updating last generated timestamp...");
        ResourceResolver resolver = configuration.getResource().getResourceResolver();
        ModifiableValueMap mvm = configuration.getResource().getChild(JcrConstants.JCR_CONTENT).adaptTo
                (ModifiableValueMap.class);
        mvm.put(PN_LAST_GENERATED, System.currentTimeMillis());
        resolver.commit();
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
