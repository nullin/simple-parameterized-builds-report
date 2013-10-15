package com.nullin.jenkins.spbr;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;

import java.util.*;

/**
 * Action to display builds categorized by parameters used to run them.
 *
 * @author nullin
 */
public class MatrixBuildsReportAction implements Action {

    private MatrixProject project;
    public static final int MAX_BUILDS_PER_PARAM_DEF = 10;

    MatrixBuildsReportAction(MatrixProject project) {
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
    public Multimap<Map<String, String>, MatrixRun> getBuildsMap(Collection<MatrixBuild> builds) {
        Multimap<Map<String, String>, MatrixRun> buildsMap = ArrayListMultimap.create();

        if (builds.isEmpty()) {
            return buildsMap;
        }

        List<MatrixRun> mr =  project.getLastBuild().getRuns();
        Set<String>  latestParamKeySet =  mr.get(mr.size()-1).getBuildVariables().keySet();

        for (MatrixBuild build : builds){
            for (MatrixRun r: build.getRuns()){

                Map<String, String> vars = r.getBuildVariables();

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
                    buildsMap.put(vars, r);
                } else {
                    /*
                    * Any build not matching the criteria gets added to the default null key.
                    */
                    buildsMap.put(null, r);
                }
            }

        }
        return buildsMap;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }
}
