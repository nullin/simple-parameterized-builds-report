package com.nullin.jenkins.spbr;

import java.io.Serializable;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * BuildWrapper to allow configuration of SPBR project action
 *
 * NOTE: not sure whats the right way to get in configuration of
 * ProjectActions into the build configuration. This seems to be an easy way
 * even though we are not doing anything specifically with the build
 */
public class SPBRBuildWrapper extends BuildWrapper implements Serializable {

    public transient final String includes;
    public transient final String excludes;

    @DataBoundConstructor
    public SPBRBuildWrapper(String includes, String excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {

        @Override
        public BuildWrapper newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            return req.bindJSON(SPBRBuildWrapper.class, formData);
        }

        @Override
        public boolean isApplicable(final AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Customize Simple Parameteried Builds Report";
        }

    }

}