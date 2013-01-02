package com.nullin.jenkins.spbr;

import com.google.common.collect.Multimap;
import hudson.model.*;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nullin
 */
public class SimpleParameterizedBuildsReportActionTest extends HudsonTestCase {

    public void testBuildMapContents() throws Exception {
        AbstractProject project = createFreeStyleProject();
        SimpleParameterizedBuildsReportAction action = new SimpleParameterizedBuildsReportAction(project);

        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(
                new StringParameterDefinition("string", "defaultValue", "string description"));
        project.addProperty(pdp);

        WebClient wc = new WebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        wc.goTo("/job/" + project.getName() + "/buildWithParameters?delay=0sec");

        Queue.Item q = jenkins.getQueue().getItem(project);
        if (q != null) q.getFuture().get();
        else Thread.sleep(1000);

        Multimap<Map<String, String>, AbstractBuild> buildsMap = action.getBuildsMap(project.getBuilds());
        assertEquals(buildsMap.keySet().size(), 1);
        assertEquals(buildsMap.values().size(), 1);
    }

    public void testChangedParameterSet() throws Exception {
        AbstractProject project = createFreeStyleProject();
        SimpleParameterizedBuildsReportAction action = new SimpleParameterizedBuildsReportAction(project);

        ParametersDefinitionProperty pdp = new ParametersDefinitionProperty(
                new StringParameterDefinition("string", "defaultValue", "string description"));
        project.addProperty(pdp);

        WebClient wc = new WebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        wc.goTo("/job/" + project.getName() + "/buildWithParameters?delay=0sec");

        Queue.Item q = jenkins.getQueue().getItem(project);
        if (q != null) q.getFuture().get();
        else Thread.sleep(1000);

        project.removeProperty(pdp);
        pdp = new ParametersDefinitionProperty(
                new StringParameterDefinition("string", "defaultValue", "string description"),
                new StringParameterDefinition("string1", "defaultValue1", "string description"));
        project.addProperty(pdp);

        wc.goTo("/job/" + project.getName() + "/buildWithParameters?delay=0sec");
        q = jenkins.getQueue().getItem(project);
        if (q != null) q.getFuture().get();
        else Thread.sleep(1000);

        wc.goTo("/job/" + project.getName() + "/buildWithParameters?delay=0sec");
        q = jenkins.getQueue().getItem(project);
        if (q != null) q.getFuture().get();
        else Thread.sleep(1000);

        wc.goTo("/job/" + project.getName() + "/buildWithParameters?delay=0sec&string=newValue");
        q = jenkins.getQueue().getItem(project);
        if (q != null) q.getFuture().get();
        else Thread.sleep(1000);

        Multimap<Map<String, String>, AbstractBuild> buildsMap = action.getBuildsMap(project.getBuilds());
        assertEquals(3, buildsMap.keySet().size());
        assertTrue(buildsMap.keySet().contains(null));
        assertEquals(4, buildsMap.values().size());

        Map<String, String> buildVars = new HashMap<String, String>();
        buildVars.put("string", "defaultValue");
        assertEquals(1, buildsMap.get(null).size());
        assertEquals(buildVars, buildsMap.get(null).iterator().next().getBuildVariables());

        buildVars.put("string1", "defaultValue1");
        assertEquals(2, buildsMap.get(buildVars).size());

        buildVars.put("string", "newValue");
        assertEquals(1, buildsMap.get(buildVars).size());
    }

}
