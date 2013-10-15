/*
 * ProcessImage.java
 *
 * Created on June 29, 2007, 10:11 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.formlens.omr;

import net.sourceforge.jiu.codecs.*;
import net.sourceforge.jiu.data.*;
import net.sourceforge.jiu.color.reduction.*;
import net.sourceforge.jiu.filters.*;

/**
 *
 * @author Aaditeshwar Seth
 */
public class MainProcessForm {
    
    public static void main(String[] args) {
        String imgfilename = args[0];
        String templatefilename = args[1];
        
        Gray8Image grayimage = ImageUtil.readImage(imgfilename);
//        Gray8Image grayimage = ImageUtil.readImage("../../2circle-4.tif");

        ImageManipulation image = new ImageManipulation(grayimage);
        image.locateConcentricCircles();

        image.readConfig(templatefilename + ".config");
        image.readFields(templatefilename + ".fields");
        image.readAscTemplate(templatefilename + ".asc");
        image.searchMarks();
        image.saveData(imgfilename + ".dat");
//        image.readConfig("2circle-org-colored-whole.config");
//        image.readFields("2circle-org-colored-whole.fields");
//        image.readAscTemplate("2circle-org-colored-whole.asc");
//        image.searchMarks();
//        image.saveData("2circle-4.dat");
    }

}
