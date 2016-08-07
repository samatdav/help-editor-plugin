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
        for (File s : f.listFiles()) {
            if (s.isFile()) {
                listWithFileNames.add(s);
            } else if (s.isDirectory()) {
                getListFiles(s.getAbsolutePath());
            }
        }
    }

    //array for cash storage
    Map<String, String> map = new TreeMap<String, String>();

    //reading files into cash
    public EditHelpRootAction() throws IOException {
        String dirName = Jenkins.getInstance().getRootDir().toString()+"/helpmanager";

        //check if the directory exists
        File helpManagerFile = new File(dirName);
        if(!helpManagerFile.exists())
            helpManagerFile.mkdirs();

        //reading files from the directory
        getListFiles(dirName);

        //stream reading from files
        for (File fil : listWithFileNames) {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(dirName+"/"+fil.getName()));
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
    public String getUpdateMyString() throws IOException {
        //process get request
        String class_name = Stapler.getCurrentRequest().getParameter("class");
        String updated_class_name = Stapler.getCurrentRequest().getParameter("textArea");

        //replacement or creation of the string
        map.put(class_name+".html",updated_class_name);

        //writing into a file
        if (class_name != null) {

            String dirName = Jenkins.getInstance().getRootDir().toString()+"/helpmanager";
            
            //check if the directory exists
            File helpManagerFile = new File(dirName);
            if(!helpManagerFile.exists())
                helpManagerFile.mkdirs();

            File newFile = new File(dirName + "/" + class_name + ".html");
            //check if the file exists
            if (!newFile.exists()) {
                newFile.createNewFile();
            }

            //stream writing into file
            FileWriter fw = new FileWriter(newFile);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(updated_class_name);
            out.flush();
            out.close();
        }

        return null;
    }
}
