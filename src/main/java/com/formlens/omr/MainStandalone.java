package com.formlens.omr;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.formlens.omr.ImageManipulation.Field;

public class MainStandalone {
	
	public static void main(String[] args) {
		
		String template = "src/resources/form1.png";
	    String fields = "a row single foodQuality 1 2 3 4 5\nb row single internetCoverage 1 2 3 4 5\nc row single wantFreeSake yes no";

	    OMR omr = new OMR();
	    String[] s = omr.learnForm(template, fields);
	    String asc = s[0];
	    String config = s[1];
	    
	    System.out.println(s[1]);

	    String form = "src/resources/form1-3.tif";

	    Hashtable fieldsTable = omr.processForm(template, form, config, fields, asc);
	    
        Enumeration e = fieldsTable.keys();
        while(e.hasMoreElements()) {
            Field field = (Field)(fieldsTable.get(e.nextElement()));
            System.out.println(field.getName() + "=" + field.getFieldValues());
        }
	}

}
