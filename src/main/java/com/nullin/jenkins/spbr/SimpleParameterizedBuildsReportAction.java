package com.nullin.jenkins.spbr;

import java.util.Collection;
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
        return "Simple Parameterized Builds Report";
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

        final ParametersDefinitionProperty paramDefProp = project.getProperty(ParametersDefinitionProperty.class);
        Set<String> latestParamKeySet = new HashSet<String>();
        for (final ParameterDefinition definition : paramDefProp.getParameterDefinitions()) {
            if (definition.getType().equals(PasswordParameterDefinition.class.getSimpleName())) {
                continue;
            }
            final String name = definition.getName();
            latestParamKeySet.add(name);
        }

        for (AbstractBuild build : builds) {
            if (build.isBuilding()) {
                //skip over builds that are still running
                continue;
            }

            Map<String, String> paramsMap = getParameterValues(build);
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

    private Map<String, String> getParameterValues(AbstractBuild build) {
        Map<String, String> paramsMap = new HashMap<String, String>();
        List<ParameterValue> paramVals = build.getAction(ParametersAction.class).getParameters();

        for (ParameterValue paramVal : paramVals) {
            if (paramVal.isSensitive()) {
                continue;
            }
            paramsMap.put(paramVal.getName(), paramVal.createVariableResolver(build).resolve(paramVal.getName()));
        }
        return paramsMap;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }
}
