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
import org.kohsuke.stapler.Stapler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import static java.nio.file.Files.readAllLines;
import static java.util.logging.Level.FINE;


@Extension
public class EditHelpRootAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(PrivateKeyProvider.class.getName());
    //reading the catalog of helpmanager
    private static ArrayList<File> listWithFileNames = new ArrayList<>();
    public static void doListFiles(String str) {
        File f = new File(str);
        File[] mydr = f.listFiles();
        if (mydr == null)
            return;
        for (File s : mydr) {
            if (s.isFile()) {
                listWithFileNames.add(s);
            } else if (s.isDirectory()) {
                doListFiles(s.getAbsolutePath());
            }
        }
    }

    //array for cache storage
    Map<String, String> arrayOfHelp = new TreeMap<String, String>();

    //reading files into cache
    public EditHelpRootAction() throws IOException, FileNotFoundException {
        Jenkins jn = Jenkins.getInstance();
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

                //reading files from the directory
                doListFiles(helpManagerFile.getAbsolutePath());

                //stream reading from files
                for (File fil : listWithFileNames) {
                    List<String> getStringList = readAllLines(fil.toPath(),StandardCharsets.UTF_8);
                    String getListtoStr = StringUtils.join(getStringList, "\n");
                    arrayOfHelp.put(fil.getName(),getListtoStr);
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
    public String getMyString() {
        //process get request
        String class_name = Stapler.getCurrentRequest().getParameter("class");
        return arrayOfHelp.get(class_name+".html");
    }

    //save text area into file and array
    public void doUpdateMyString() {
        //process get request
        String class_name = Stapler.getCurrentRequest().getParameter("class");
        String updated_class_name = Stapler.getCurrentRequest().getParameter("textArea");
        //replacement or creation of the string
        arrayOfHelp.put(class_name+".html",updated_class_name);

        //writing into a file
        if (class_name != null) {
            Jenkins jn = Jenkins.getInstance();
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

                    File newFile = new File(helpManagerFile.getAbsolutePath() + "/" + class_name + ".html");
                    //check if the file exists
                    if (!newFile.exists()) {
                        try{
                            if (!newFile.createNewFile()) {
                                LOGGER.log(FINE,"unable create directory");
                            }
                        }
                        catch (IOException exname){}

                    }

                    //stream writing into file
                    try{
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), StandardCharsets.UTF_8));
                        out.write(updated_class_name);
                        out.flush();
                        out.close();
                    }
                    catch (IOException exname){}

                }
            }
        }
    }
}
