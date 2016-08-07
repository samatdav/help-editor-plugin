package org.jenkinsci.plugins.edithelp;

import hudson.Extension;
import hudson.model.PageDecorator;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

@Extension
public class EditHelpPageDecorator extends PageDecorator {
    public EditHelpPageDecorator() {
        super(EditHelpPageDecorator.class);
        load();
    }
}
