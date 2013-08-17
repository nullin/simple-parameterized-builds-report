package com.nullin.jenkins.spbr;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;

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
     * @param builds all possible builds to look at to generate the map
     * @return
     */
    public Multimap<Map<String, String>, AbstractBuild> getBuildsMap(Collection<AbstractBuild> builds) {
        Multimap<Map<String, String>, AbstractBuild> buildsMap = ArrayListMultimap.create();

        if (builds.isEmpty()) {
            return buildsMap;
        }

        Set<String> latestParamKeySet = project.getLastBuild().getBuildVariables().keySet();

        for (AbstractBuild build : builds) {
            if (build.isBuilding()) {
                //skip over builds that are still running
                continue;
            }

            Map<String, String> vars = build.getBuildVariables();
            if (latestParamKeySet.equals(vars.keySet())) {
                if (buildsMap.get(vars).size() == MAX_BUILDS_PER_PARAM_DEF) {
                    /*
                     We don't want to display too many builds either. So, limiting it here.
                     */
                    continue;
                }
                /*
                 * We only care about the builds that are built using the parameters that
                 * match the ones used in latest build
                 */
                buildsMap.put(vars, build);
            } else {
                /*
                 * Any build not matching the criteria gets added to the default null key.
                 */
                buildsMap.put(null, build);
            }

        }
        return buildsMap;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }
}
