package org.jenkinsci.plugins.edithelp;

import hudson.Extension;
import hudson.model.PageDecorator;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

@Extension
public class pagedec extends PageDecorator {
    public pagedec() {
        super(pagedec.class);
        load();
    }
}
