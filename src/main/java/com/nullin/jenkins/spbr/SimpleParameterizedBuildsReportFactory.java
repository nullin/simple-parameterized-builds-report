package com.nullin.jenkins.spbr;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * @author nullin
 */
@Extension
public class SimpleParameterizedBuildsReportFactory extends TransientProjectActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractProject target) {
        if (target.isParameterized()) {
            return Collections.singleton(new SimpleParameterizedBuildsReportAction(target));
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
