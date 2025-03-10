package org.processmining.multilayeralignment.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class Write2File {
    public  Write2File(List l,String fileName,boolean type,String title,boolean makeZip){
    	               
        try
        {
            if (new File(fileName+".txt").exists())
                new File(fileName+".txt").delete();
            File outfile = new File(fileName+".temp");
            //System.out.println("make directory\t"+outfile.getParentFile().toString());
            if(!outfile.getParentFile().isDirectory()){
                //System.out.println("make directory"+outfile.getParentFile().toString());
                outfile.getParentFile().mkdirs();//outfile.getParent()
            }
            String ReportName = fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());
            System.out.println("Start Writing to "+ReportName+" File ");
            FileWriter fw = new FileWriter(outfile,type);
            fw.write(title);
            for(int i=0;i<l.size();i++)
                fw.write(l.get(i)+"");
            fw.flush();
            fw.close();

            outfile.renameTo(new File(fileName+".xes"));
            

        }
        catch(IOException e)
        {
            System.out.println("Error in oppenning logfile:"+fileName);
        }
    }
    
    public  Write2File(Map<String,String> resultMap,String methodName,String fileName,boolean type,String title,boolean makeZip) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        try           
        {

            if (new File(fileName+".txt").exists())
                new File(fileName+".txt").delete();
            File outfile = new File(fileName+".temp");
            //System.out.println("make directory\t"+outfile.getParentFile().toString());
            if(!outfile.getParentFile().isDirectory()){
                //System.out.println("make directory"+outfile.getParentFile().toString());
                outfile.getParentFile().mkdirs();//outfile.getParent()
            }
            String ReportName = fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());
            System.out.println("Start Writing to "+ReportName+" File ");
            FileWriter fw = new FileWriter(outfile,type);
            //fw.write(title);
            
            for (Map.Entry<String,String> entry : resultMap.entrySet()) {
				
				String record = entry.getValue();
                fw.append(record+ "\r\n");
			}

            

            fw.flush();
            fw.close();

            outfile.renameTo(new File(fileName+".txt"));
            
            
            //treeMap.clear();

        }
        catch(IOException e)
        {
            System.out.println("Error in oppenning logfile:" + fileName);
        }
    }
    
    public  Write2File(Map<Integer,String> resultMap,String methodName,String fileName,boolean type,String title) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        try           
        {

            if (new File(fileName+".txt").exists())
                new File(fileName+".txt").delete();
            File outfile = new File(fileName+".temp");
            //System.out.println("make directory\t"+outfile.getParentFile().toString());
            if(!outfile.getParentFile().isDirectory()){
                //System.out.println("make directory"+outfile.getParentFile().toString());
                outfile.getParentFile().mkdirs();//outfile.getParent()
            }
            String ReportName = fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());
            System.out.println("Start Writing to "+ReportName+" File ");
            FileWriter fw = new FileWriter(outfile,type);
            fw.write(title);
            
            for (Map.Entry<Integer,String> entry : resultMap.entrySet()) {
				
				String record = entry.getValue();
                fw.append(record+ "\r\n");
			}

            

            fw.flush();
            fw.close();

            outfile.renameTo(new File(fileName+".txt"));
            
            
            //treeMap.clear();

        }
        catch(IOException e)
        {
            System.out.println("Error in oppenning logfile:" + fileName);
        }
    }
    
}

