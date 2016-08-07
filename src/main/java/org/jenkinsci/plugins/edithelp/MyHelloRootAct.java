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
public class MyHelloRootAct implements RootAction {
    public transient final Class<?> clazz = (Class)getClass();
    Map<String, String> map = new TreeMap<String, String>();
    private static ArrayList<File> listWithFileNames = new ArrayList<>();
    public MyHelloRootAct() throws IOException {
        String dirName = Jenkins.getInstance().getRootDir().toString()+"/helpmanager";
        File f2 = new File(dirName);
        if(!f2.exists())
            f2.mkdirs();
        getListFiles(dirName);

        for (File fil : listWithFileNames) {
            StringBuilder sb = new StringBuilder();
            BufferedReader in = new BufferedReader(new FileReader(dirName+"/"+fil.getName()));
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }

            map.put(fil.getName(),sb.toString());
        }

    }


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

    public String getMyString() throws IOException {

        //process get request
        String class_name = Stapler.getCurrentRequest().getParameter("class");

        ;
        if(map.containsKey(class_name+".html")){
            return map.get(class_name+".html");
        }else{
            return null;
        }
    }

    public String getUpdateMyString() throws IOException {

        //process get request
        String class_name = Stapler.getCurrentRequest().getParameter("class");
        String updated_class_name = Stapler.getCurrentRequest().getParameter("textArea");
        map.put(class_name+".html",updated_class_name);
        if (class_name != null) {

            String dirName = Jenkins.getInstance().getRootDir().toString()+"/helpmanager";
            File f2 = new File(dirName);
            if(!f2.exists())
                f2.mkdirs();

            File newFile = new File(dirName + "/" + class_name + ".html");
            //check if the file exists
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileWriter fw = new FileWriter(newFile);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(updated_class_name);
            out.flush();
            out.close();

        }


        return null;
        //return (class_name+updated_class_name);
    }
}
