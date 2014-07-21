package com.nullin.jenkins.spbr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterDefinition;

/**
 * Action to display builds categorized by parameters used to run them.
 *
 * @author nullin
 */
public class SimpleParameterizedBuildsReportAction implements Action {

    private AbstractProject<?, ?> project;
    public static final int MAX_BUILDS_PER_PARAM_DEF = 10;

    SimpleParameterizedBuildsReportAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    public String getIconFileName() {
        return "notepad.png";
    }

    public String getDisplayName() {
        return "Parameterized Builds Report";
    }

    public String getUrlName() {
        return "spbr";
    }

    /**
     * Gets at most {@code MAX_BUILDS_PER_PARAM_DEF} builds per set of parameter values as a map.
     *
     * @param builds all possible builds to look at in order to generate the map
     * @return
     */
    public Multimap<Map<String, String>, AbstractBuild> getBuildsMap(Collection<AbstractBuild> builds) {
        Multimap<Map<String, String>, AbstractBuild> buildsMap = ArrayListMultimap.create();

        if (builds.isEmpty()) {
            return buildsMap;
        }

        //selected params will contain the set of properties to filter by
        List<String> selectedParams = Collections.emptyList();
        boolean isInclude = true;
        SPBRConfigJobProperty configJobProperty = project.getProperty(SPBRConfigJobProperty.class);
        if (configJobProperty != null) {
            String includes = configJobProperty.includes;
            String excludes = configJobProperty.excludes;
            if (includes != null && !includes.trim().isEmpty()) {
                selectedParams = getParameters(includes);
            } else if(excludes != null && !excludes.trim().isEmpty()) {
                selectedParams = getParameters(excludes);
                isInclude = false;
            }
        }

        ParametersDefinitionProperty paramDefProp = project.getProperty(ParametersDefinitionProperty.class);
        Set<String> latestParamKeySet = new HashSet<String>();
        if (paramDefProp != null) {
            for (final ParameterDefinition definition : paramDefProp.getParameterDefinitions()) {
                if (definition.getType().equals(PasswordParameterDefinition.class.getSimpleName())) {
                    continue;
                }
                final String name = definition.getName();
                if (shouldBeVisible(name, selectedParams, isInclude)) {
                    latestParamKeySet.add(name);
                }
            }
        }

        for (AbstractBuild build : builds) {
            if (build.isBuilding()) {
                //skip over builds that are still running
                continue;
            }

            Map<String, String> paramsMap = getParameterValues(build, selectedParams, isInclude);
            if (latestParamKeySet.equals(paramsMap.keySet())) {
                if (buildsMap.get(paramsMap).size() == MAX_BUILDS_PER_PARAM_DEF) {
                    /*
                     We don't want to display too many builds either. So, limiting it here.
                     */
                    continue;
                }
                /*
                 * We only care about the builds that are built using the parameters that
                 * match the ones used in latest build
                 */
                buildsMap.put(paramsMap, build);
            } else {
                /*
                 * Any build not matching the criteria gets added to the default null key.
                 */
                buildsMap.put(null, build);
            }

        }
        return buildsMap;
    }

    /**
     * Splits the input by commas and returns the sanitized list of parameters
     * @param str
     * @return
     */
    List<String> getParameters(String str) {
        String[] parts = str.split(",");
        List<String> params = new ArrayList<String>(parts.length);
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                params.add(part);
            }
        }
        return params;
    }

    /**
     * @param param parameter name
     * @param params list of parameters to filter by
     * @param isInclude true, if list is the include list, false if it's the exclude list
     * @return true, if the parameter should be included in list of parameters visible on the report
     */
    boolean shouldBeVisible(String param, List<String> params, boolean isInclude) {
        if (params.isEmpty()) {
            return true;
        }

        if (isInclude && params.contains(param)) {
            return true;
        } else if (!isInclude && !params.contains(param)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Map containing filtered parameters along with their values
     * @param build build
     * @param selectedParams list of parameters to filter by
     * @param isInclude true, if list is the include list, false if it's the exclude list
     * @return map containing key as the name of properties and value as the corresponding parameter values
     */
    private Map<String, String> getParameterValues(AbstractBuild build, List<String> selectedParams, boolean isInclude) {
        Map<String, String> paramsMap = new HashMap<String, String>();
        ParametersAction action = build.getAction(ParametersAction.class);

        if (action == null) {
            return Collections.emptyMap();
        }

        List<ParameterValue> paramVals = action.getParameters();
        for (ParameterValue paramVal : paramVals) {
            if (paramVal.isSensitive()) {
                continue;
            }
            if (shouldBeVisible(paramVal.getName(), selectedParams, isInclude)) {
                paramsMap.put(paramVal.getName(), paramVal.createVariableResolver(build).resolve(paramVal.getName()));
            }
        }
        return paramsMap;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }
}
