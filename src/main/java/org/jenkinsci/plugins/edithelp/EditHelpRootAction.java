package org.jenkinsci.plugins.edithelp;

import java.io.*;

import hudson.Extension;
import hudson.cli.PrivateKeyProvider;
import hudson.model.Action;
import hudson.model.RootAction;
import hudson.model.View;
import jenkins.model.Jenkins;
import org.apache.tools.ant.taskdefs.Recorder;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.lang.Klass;
import sun.misc.URLClassPath;

import javax.swing.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;


@Extension
public class EditHelpRootAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(PrivateKeyProvider.class.getName());
    //reading the catalog of helpmanager
    private static ArrayList<File> listWithFileNames = new ArrayList<>();
    public static void getListFiles(String str) {
        File f = new File(str);
        File[] mydr = f.listFiles();
        if (mydr == null)
            return;
        for (File s : mydr) {
            if (s.isFile()) {
                listWithFileNames.add(s);
            } else if (s.isDirectory()) {
                getListFiles(s.getAbsolutePath());
            }
        }
    }

    //array for cache storage
    Map<String, String> map = new TreeMap<String, String>();

    //reading files into cache
    public EditHelpRootAction() throws IOException, FileNotFoundException {
        Jenkins jn = Jenkins.getInstance();
        if (jn != null) {
            File dirfile = jn.getRootDir();         
            String dirName = null;         
            if(dirfile != null) {
                dirName = dirfile.toString()+"/helpmanager";
            //check if the directory exists
                File helpManagerFile = new File(dirName);

                if(!helpManagerFile.exists())
                    if (!helpManagerFile.mkdirs()) {
                        LOGGER.log(FINE,"unable create directory");
                    }

                //reading files from the directory
                getListFiles(dirName);

                //stream reading from files
                for (File fil : listWithFileNames) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fil), "UTF8"));
                    try {
                        String s;
                        while ((s = br.readLine()) != null) {
                            sb.append(s);
                            sb.append("\n");
                        }
                    } finally {
                        br.close();
                    }
                    map.put(fil.getName(),sb.toString());
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
        return map.get(class_name+".html");
    }

    //save text area into file and array
    public String getUpdateMyString() {
        //process get request
        String class_name = Stapler.getCurrentRequest().getParameter("class");
        String updated_class_name = Stapler.getCurrentRequest().getParameter("textArea");
        //replacement or creation of the string
        map.put(class_name+".html",updated_class_name);

        //writing into a file
        if (class_name != null) {
            Jenkins jn = Jenkins.getInstance();
            if (jn != null) {
                File dirfile = jn.getRootDir();         
                String dirName = null;         
                if(dirfile != null) {
                    dirName = dirfile.toString()+"/helpmanager";
                    
                    //check if the directory exists
                    File helpManagerFile = new File(dirName);
                    if(!helpManagerFile.exists())
                        if (!helpManagerFile.mkdirs()) {
                            LOGGER.log(FINE,"unable create directory");
                        }

                    File newFile = new File(dirName + "/" + class_name + ".html");
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
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), "UTF-8"));
                        out.write(updated_class_name);
                        out.flush();
                        out.close();
                    }
                    catch (IOException exname){}

                }
            }
        }
        return null;
    }
}
