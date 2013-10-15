package com.formlens.omr;

import java.util.Hashtable;
import java.util.Map;

import net.sourceforge.jiu.data.Gray8Image;

public class OMR {

	public String[] learnForm(String template, String fields)
	{
		Gray8Image grayimage = ImageUtil.readImage(template);
        
        ImageManipulation image = new ImageManipulation(grayimage);
        image.locateConcentricCircles();
        image.locateMarks();
        
        String[] s = new String[2];
        //image.writeAscTemplate(template + ".asc");
        //image.writeConfig(template + ".config");
        
        s[0] = image.getAscTemplate(fields);
        System.out.println(s[0]);
        s[1] = image.getConfig();
        return s;
	}
	
	public Hashtable processForm(String template, String form, String config, String fields, String asc)
	{
        Gray8Image grayimage = ImageUtil.readImage(form);

        ImageManipulation image = new ImageManipulation(grayimage);
        image.locateConcentricCircles();

        //image.readConfig(template + ".config");
        //image.readFields(template + ".fields");
        //image.readAscTemplate(template+ ".asc");
        image.readConfigFromString(config);
        image.readFieldsFromString(fields);
        image.readAscTemplateFromString(asc);
        image.searchMarks();
        //image.saveData(form + ".dat");
        
		return image.getFields();
	}
}
