package org.jenkinsci.plugins.edithelp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import hudson.Extension;
import hudson.cli.PrivateKeyProvider;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Stapler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import static java.nio.file.Files.readAllLines;
import static java.util.logging.Level.FINE;

@Extension
public class EditHelpRootAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(PrivateKeyProvider.class.getName());

    //array for cache storage
    private Map<String, String> arrayOfHelp = new TreeMap<String, String>();

    //reading files into cache
    public EditHelpRootAction() throws IOException, FileNotFoundException {
        Jenkins jn = Jenkins.getActiveInstance();
        if (jn != null) {
            File dirfile = jn.getRootDir();         
            if(dirfile != null) {
                //check if the directory exists
                File helpManagerFile = new File(dirfile, "helpmanager");

                if(!helpManagerFile.exists())
                    if (!helpManagerFile.mkdirs()) {
                        LOGGER.log(FINE,"unable create directory");
                    }

                //reading files from the directory
                if (helpManagerFile.getAbsolutePath() != null) {
                    File dir = new File(helpManagerFile.getAbsolutePath());
                    File[] files = dir.listFiles();
                    if (files != null){
                        for (File file : files) {
                            if (file.isFile()) {
                                List<String> getStringList = readAllLines(file.toPath(),StandardCharsets.UTF_8);
                                String getListtoStr = StringUtils.join(getStringList, "\n");
                                arrayOfHelp.put(file.getName(),getListtoStr);
                            }
                        }
                    }
                }
            }            
        }            
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "helpmanager";
    }

    //copying from array
    public String getHelpInfo() {
        //process get request
        String className = Stapler.getCurrentRequest().getParameter("class");
        return arrayOfHelp.get(className+".html");
    }

    //save text area into file and array
    public void doUpdateHelpInfo() {
        //process get request
        String className = Stapler.getCurrentRequest().getParameter("class");
        String updatedClassText = Stapler.getCurrentRequest().getParameter("textArea");
        //replacement or creation of the string
        arrayOfHelp.put(className+".html",updatedClassText);

        //writing into a file
        if (className != null) {
            Jenkins jn = Jenkins.getActiveInstance();
            if (jn != null) {
                File dirfile = jn.getRootDir();         
                String dirName = null;         
                if(dirfile != null) {
                    //check if the directory exists
                    File helpManagerFile = new File(dirfile, "helpmanager");
                    if(!helpManagerFile.exists())
                        if (!helpManagerFile.mkdirs()) {
                            LOGGER.log(FINE,"unable create directory");
                        }

                    File newFile = new File(helpManagerFile.getAbsolutePath() + "/" + className + ".html");
                    //check if the file exists
                    if (!newFile.exists()) {
                        try{
                            if (!newFile.createNewFile()) {
                                LOGGER.log(FINE,"help file was not created");
                            }
                        } catch (IOException exname){
                            LOGGER.log(FINE,"help file was not created");
                        }
                    }

                    //stream writing into file
                    try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), StandardCharsets.UTF_8))){
                        out.write(updatedClassText);
                        out.flush();
                        out.close();
                    } catch (IOException exname){
                        LOGGER.log(FINE,"file was not written");
                    }
                }
            }
        }
    }
}
