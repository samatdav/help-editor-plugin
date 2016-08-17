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
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.nio.file.Files.readAllLines;
import static java.util.logging.Level.FINE;

@Extension
public class EditHelpRootAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(PrivateKeyProvider.class.getName());

    //hashMap for cache storage
    private transient Map<String, String> hashMapOfHelp = new HashMap<String, String>();

    //reading files into cache
    public EditHelpRootAction() throws IOException, FileNotFoundException {
        File dirfile = Jenkins.getActiveInstance().getRootDir();
        if(dirfile != null) {

            //check if the directory exists
            File helpManagerFile = new File(dirfile, "helpmanager");

            if(!helpManagerFile.exists())
                if (!helpManagerFile.mkdirs()) {
                    LOGGER.log(FINE,"Unable create directory: "+helpManagerFile.getAbsolutePath());
                }

            //reading files from the directory
            if (helpManagerFile.getAbsolutePath() != null) {
                File dir = new File(helpManagerFile.getAbsolutePath());
                File[] files = dir.listFiles();
                if (files != null)
                    for (File file : files)
                        if (file.isFile())
                            hashMapOfHelp.put(file.getName(),readFromFile(file));
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

    //copying from HashMap
    public String getHelpInfo() {
        //process get request
        String className = Stapler.getCurrentRequest().getParameter("class");
        String Help = hashMapOfHelp.get(className+".html");
        if(Help != null)
            return Help;
        else
            return "";
    }

    private void writeInFile(File file, String info){
        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))){
            out.write(info);
            out.flush();
            out.close();
        } catch (IOException exname){
            LOGGER.log(FINE,"file was not written:"+file.getAbsolutePath());
        }
    }

    private String readFromFile(File file) throws IOException{
        try{
            List<String> getStringList = readAllLines(file.toPath(),StandardCharsets.UTF_8);
            return StringUtils.join(getStringList, "\n");

        } catch (IOException exname){
            LOGGER.log(FINE,"help file was not read:"+file.getAbsolutePath());
            return "";
        }
    }

    //save text area into file and array
    public void doUpdateHelpInfo(@QueryParameter("class") String className, @QueryParameter("textArea") String updatedClassText) {
        
        //process get request
        if (className != null) {
            //replacement or creation of the string
            hashMapOfHelp.put(className+".html",updatedClassText);

            //writing into a file
            File dirfile = Jenkins.getActiveInstance().getRootDir();
            if(dirfile != null) {

                //check if the directory exists
                File helpManagerFile = new File(dirfile, "helpmanager");
                if(!helpManagerFile.exists())
                    if (!helpManagerFile.mkdirs())
                        LOGGER.log(FINE,"unable create helpmanager directory");

                File newFile = new File(helpManagerFile.getAbsolutePath() + "/" + className + ".html");
                //check if the file exists
                if (!newFile.exists()) {
                    try{
                        if (!newFile.createNewFile())
                            LOGGER.log(FINE,"help file was not created:"+newFile.getAbsolutePath());
                    } catch (IOException exname){
                        LOGGER.log(FINE,"help file was not created:"+newFile.getAbsolutePath());
                    }
                }

                writeInFile(newFile, updatedClassText);
            }

        } 
    }
}
