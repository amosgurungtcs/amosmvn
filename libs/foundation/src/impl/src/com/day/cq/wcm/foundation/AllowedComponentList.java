package com.day.cq.wcm.foundation;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.TemplatedResource;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyMapping;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fauchere on 12/11/15.
 */
public class AllowedComponentList extends WCMUsePojo {

    private static final String PLACEHOLDER_COMPONENT_PATH = "wcm/foundation/components/parsys/placeholder";
    private static final String PN_POLICY = "cq:policy";
    private static final String PN_COMPONENTS = "components";
    private static final String COMPONENT_GROUP_PREFIX = "group:";
    public static final String STRUCTURE_JCR_CONTENT = "/structure/jcr:content/";
    public static final String POLICIES_JCR_CONTENT = "/policies/jcr:content/";

    private Collection<Component> components;

    private String[] componentPaths = null;

    private class ComponentComparator implements Comparator<Component> {

        @Override
        public int compare(Component comp1, Component comp2) {
            return comp1.getTitle().compareTo(comp2.getTitle());
        }
    }

    public static class ComponentMapping {

        private String path;
        private String resourceType;
        private String cssClass;

        public ComponentMapping (String path, String resourceType, String cssClass) {
            this.path = path;
            this.resourceType = resourceType;
            this.cssClass = cssClass;
        }

        public String getPath() {
            return path;
        }

        public String getResourceType() {
            return resourceType;
        }

        public String getCssClass() {
            return cssClass;
        }
    }

    @Override
    public void activate() throws Exception {
        ComponentManager componentManager = getResourceResolver().adaptTo(ComponentManager.class);
        components = componentManager.getComponents();
    }

    public String getTitle()
    {
        return (getComponentPathList().length > 0 ? "Allowed Components" : "No allowed component") + " for " + getComponent().getTitle();
    }

    /**
     * Is the allowed component list applicable in the current context
     *
     * @return
     */
    public boolean isApplicable () {
        Page page = this.getCurrentPage();

        // Is this resource parent an authored template structure resource
        // And is it set as editable
        if (page != null) {
            Template template = page.getTemplate();

            if (template != null && template.hasStructureSupport()) {
                ValueMap valueMap = getResource().getValueMap();

                if (valueMap != null && valueMap.containsKey(TemplatedResource.PN_EDITABLE)) {
                    return valueMap.get(TemplatedResource.PN_EDITABLE, Boolean.class);
                }
            }
        }

        return false;
    }

    /**
     * Returns the list of allowed components from the parent
     *
     * @return
     */
    private String[] getComponentPathList() {
        if (componentPaths != null) {
            return  componentPaths;
        }

        ResourceResolver resourceResolver = getResourceResolver();
        Resource resource = getResource();
        String resourcePath = resource.getPath();

        String policyMappingPath = resourcePath.replace(STRUCTURE_JCR_CONTENT, POLICIES_JCR_CONTENT);
        Resource policyMappingResource = resourceResolver.getResource(policyMappingPath);

        if (policyMappingResource != null) {
            ValueMap policyMappingValueMap = policyMappingResource.getValueMap();

            // If their is no policy property set on the current policy mapping
            // get the policy from the parents mapping
            if (!policyMappingValueMap.containsKey(PN_POLICY)) {
                Resource parent = policyMappingResource.getParent();

                while (!policyMappingValueMap.containsKey(PN_POLICY) && parent != null) {
                    ValueMap parentMappingVM = parent.getValueMap();

                    // If the parent node doesn't have a policy associated continue with the next parent
                    if (!parentMappingVM.containsKey(PN_POLICY)) {
                        parent = parent.getParent();
                        continue;
                    }

                    policyMappingResource = parent;
                    break;
                }
            }

            ContentPolicyMapping contentPolicyMapping = policyMappingResource.adaptTo(ContentPolicyMapping.class);

            if (contentPolicyMapping != null) {
                ContentPolicy policy = contentPolicyMapping.getPolicy();

                if (policy != null) {
                    ValueMap policyValueMap = policy.getProperties();

                    if (policyValueMap != null && policyValueMap.containsKey(PN_COMPONENTS)) {
                        componentPaths = policyValueMap.get(PN_COMPONENTS, String[].class);

                        return componentPaths;
                    }
                }
            }
        }

        componentPaths = new String[0];

        return componentPaths;
    }

    private String getRelativePath(Resource resource) {
        if (resource == null) {
            return null;
        }

        String[] searchPaths = resource.getResourceResolver().getSearchPath();

        for (int i = 0; i < searchPaths.length; i++) {
            String searchPath = searchPaths[i];
            if (resource.getPath().startsWith(searchPath)) {
                String resourcePath = resource.getPath();
                return resourcePath.substring(searchPath.length(), resourcePath.length());
            }
        }

        return null;
    }

    /**
     * Returns a list of Pojos for Sightly resource instantiation usage
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    public List getComponents() throws UnsupportedEncodingException {
        java.util.List<ComponentMapping> resources = new ArrayList<ComponentMapping>();
        String[] componentPaths = getComponentPathList();
        List<Component> sortedComponents = new ArrayList<Component>();

        if (componentPaths != null && componentPaths.length > 0) {
            String resourcePath = getResource().getPath();

            for (int i = 0; i < componentPaths.length; i++) {
                String compPath = componentPaths[i];
                compPath = compPath != null && compPath.startsWith(COMPONENT_GROUP_PREFIX) ? compPath.substring(COMPONENT_GROUP_PREFIX.length(), compPath.length()) : compPath;

                for (Iterator<Component> iter = components.iterator(); iter.hasNext();) {
                    Component component = iter.next();
                    String componentGroup = component.getComponentGroup();

                    if (component.getPath().endsWith(compPath) || (!StringUtils.isEmpty(componentGroup) && componentGroup.equals(compPath))) {
                        sortedComponents.add(component);
                    }
                }
            }

            Collections.sort(sortedComponents, new ComponentComparator());

            for (Component component: sortedComponents) {
                Resource componentResource = component.adaptTo(Resource.class);
                String key = resourcePath + "/" + getRelativePath(componentResource);
                getRequest().setAttribute(key, componentResource);
                resources.add(new ComponentMapping(key, PLACEHOLDER_COMPONENT_PATH, "aem-AllowedComponent--component"));
            }
        }


        return resources;
    }

    /**
     * Returns the specific css class names
     *
     * @return
     */
    public String getCssClass () {
        return isApplicable() ? "aem-AllowedComponent--list" : "";
    }
}
