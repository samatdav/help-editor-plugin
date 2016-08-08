package org.jenkinsci.plugins.edithelp;

import java.io.*;

import hudson.Extension;
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


@Extension
public class EditHelpRootAction implements RootAction {

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
                        System.out.print("unable create directory");
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

        if(map.containsKey(class_name+".html")){
            return map.get(class_name+".html");
        }else{
            return null;
        }
    }

    //save text area into file and array
    public String getUpdateMyString() throws IOException, FileNotFoundException {
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
                            System.out.print("unable create directory");
                        }

                    File newFile = new File(dirName + "/" + class_name + ".html");
                    //check if the file exists
                    if (!newFile.exists()) {
                        if (!newFile.createNewFile()) {
                            System.out.print("unable create directory");
                        }
                    }

                    //stream writing into file
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), "UTF-8"));
                    out.write(updated_class_name);
                    out.flush();
                    out.close();

                }
            }
        }
        return null;
    }
}
