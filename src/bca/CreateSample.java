/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bca;
import java.io.*;
import java.util.*;
import localsearch.functions.max_min.*;
import localsearch.model.*;
import localsearch.search.TabuSearch;
import localsearch.constraints.basic.*;
import localsearch.functions.basic.*;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
/**
 *
 * @author Phạm Tiến Dũng
 */
public class CreateSample {
    ArrayList<Classes> classes;

    public CreateSample() {
        this.classes = new ArrayList<>();
        this.instructors = new ArrayList<>();
        this.courses = new ArrayList<>();
    }
    ArrayList<Instructor> instructors;
    ArrayList<Course> courses;
    public static final int nCourse=8;  // Number of unique course
    public static final int nTeach=6;   // Number of course teach by each teacher.
    public static final int nBusy=10;    // Number of busy slot
    public static final int nTeacher=20; // Number of teacher
    public static final int nClass=70; // Number of class
    public static final String  fileName="BCArandom.txt"; 
    public void create(){
        
        // Create class
        int[] slot;
        int courseId;
        int start1 ;
        int start2;
        for(int i=0;i<nClass;i++){
            slot=new int[Service.rand(2,6)];
            if(slot.length==4){
                if(Service.rand(0, 100)>20){
                start1=Service.rand(0,9)*6; 
                 for(int j=0;j<slot.length;j++){
                    slot[j]=start1+j;
                }
                }
                else{
                    start1=Service.rand(0,29)*2; 
                     for(int j=0;j<2;j++){
                        slot[j]=start1+j;
                    }
                    start2=Service.rand(0,29)*2;
                    while(((start2-start1)<2)&&(start1-start2<2)){
                        
                    start2=Service.rand(0,29)*2;
                    };
                    
                    for(int j=2;j<4;j++){
                        slot[j]=start2+j-2;
                    }
                }
            }
            
            else if(slot.length==6){
                    start1=Service.rand(0,19)*3; 
                    for(int j=0;j<3;j++){
                        slot[j]=start1+j;
                    }
                    start2=Service.rand(0,19)*3;
                    while(((start2-start1)<6)&&(start1-start2<6)){
                        
                    start2=Service.rand(0,19)*3;
                    };
                    
                    for(int j=3;j<6;j++){
                        slot[j]=start2+j-3;
                    }
                
            }
            else{
                slot=new int[3];
                start1=Service.rand(0,19)*3;
                
                for(int j=0;j<slot.length;j++){
                    slot[j]=start1+j;
                }
                
            }
            courseId=Service.rand(0,7);
            classes.add(new Classes(i,courseId , slot));
        }
         
        Collections.sort(classes);
        // Create teacher
        
        for(int i=0;i<nTeacher;i++){
            int[] busySlot=new int[nBusy];
            for(int j=0;j<nBusy;j++){
                    busySlot[j]=Service.rand(0,59);
            }
            ArrayList<Integer> a=new ArrayList<Integer>();
            for(int j=0;j<nCourse;j++){
                  a.add(j);
            }
            
            int[] courseTeached=new int[nTeach];
            for(int j=0;j<nTeach;j++){
                    courseTeached[j]=a.remove(Service.rand(0,a.size()-1));
            }
            instructors.add(new Instructor(i, "teacher"+i, busySlot,courseTeached));
        }
    }
    
    public void makeXMLfile(String fileName){
        try {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("bca");
		doc.appendChild(rootElement);

		
		Element insturctorList = doc.createElement("instructorsList");
		rootElement.appendChild(insturctorList);
                    
		
		for(int i=0;i<nTeacher;i++){
                    Element insturctor = doc.createElement("instructor");
                    Instructor a=instructors.get(i);
                    insturctorList.appendChild(insturctor);
                    Element id = doc.createElement("id");
                    Element busy = doc.createElement("busy");
                    Element name = doc.createElement("name");
                    Element teach = doc.createElement("teach");
                    insturctor.appendChild(id);
                    insturctor.appendChild(busy);
                    insturctor.appendChild(teach);
                    insturctor.appendChild(name);
                    id.appendChild(doc.createTextNode(String.valueOf(a.instructorID)));
                    name.appendChild(doc.createTextNode(a.name));
                    for(int j=0;j<a.busySlot.length;j++){
                        Element slot= doc.createElement("slot");
                        busy.appendChild(slot);
                        slot.appendChild(doc.createTextNode(String.valueOf(a.busySlot[j])));
                    }
                    for(int j=0;j<a.courseIDTeached.length;j++){
                        Element slot= doc.createElement("id");
                        teach.appendChild(slot);
                        slot.appendChild(doc.createTextNode(String.valueOf(a.courseIDTeached[j])));
                    }
                }
                
                
                Element classesList = doc.createElement("classesList");
		rootElement.appendChild(classesList);
                
                for(int i=0;i<nClass;i++){
                    Element classi = doc.createElement("class");
                    Classes a=classes.get(i);
                    classesList.appendChild(classi);
                    Element id = doc.createElement("id");
                    Element courseiD = doc.createElement("courseiD");
                    Element time = doc.createElement("time");
                    classi.appendChild(id);
                    classi.appendChild(courseiD);
                    classi.appendChild(time);
                    id.appendChild(doc.createTextNode(String.valueOf(a.classID)));
                    courseiD.appendChild(doc.createTextNode(String.valueOf(a.courseiD)));
                    for(int j=0;j<a.slot.length;j++){
                        Element slot= doc.createElement("slot");
                        time.appendChild(slot);
                        slot.appendChild(doc.createTextNode(String.valueOf(a.slot[j])));
                    }
                    
                }
		
                String[] nameCourse= new String[8];
                nameCourse[0]="Duong loi CM cua Dang CS VN";
                nameCourse[1]="Ly thuyet mach";
                nameCourse[2]="Nguyen Ly may";
                nameCourse[3]="Tieng Anh";
                nameCourse[4]="Tieng Duc";
                nameCourse[5]="Nhap mon co dien tu";
                nameCourse[6]="Co so KT do luong";
                nameCourse[7]="Co hoc KT II";
                
                
                Element coursesList = doc.createElement("coursesList");
		rootElement.appendChild(coursesList);
                      
                
                for(int i=0;i<nCourse;i++){
                    Element course = doc.createElement("course");
                    coursesList.appendChild(course);
                    Element id = doc.createElement("id");
                    Element name = doc.createElement("name");
                    course.appendChild(id);
                    course.appendChild(name);
                    id.appendChild(doc.createTextNode(String.valueOf(i)));
                    name.appendChild(doc.createTextNode(String.valueOf(nameCourse[i])));
                    
                    
                }
                
                
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(fileName));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		System.out.println("File saved!");

	  } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  } catch (TransformerException tfe) {
		tfe.printStackTrace();
	  }
    }
    
    public void checkViolation(){
        int violationSum=0;
        int violationMax=0;
        int violation;
        int overlap=0;
        for(int i=0;i<nClass;i++){
            Classes A=classes.get(i);
            violation=0;
            for(int j=0;j<nClass;j++){
                if(i==j) continue;
                Classes B=classes.get(j);
                for(int m=0;m<A.weight;m++){
                    for(int k=0;k<B.weight;k++){
                            if(A.slot[m]==B.slot[k]){
                                overlap=1;
                            } 
                
                    }
                
                }
                if(overlap==1){
                    overlap=0;
                    violation++;
                    violationSum++;
                    if(violation>violationMax) violationMax=violation;
                    
                }
                
            }
            
            
        }
        System.out.println("Violation max: "+violationMax);
        System.out.println("Violation sum: "+violationSum);
    }
    
    public static void main(String[] args) {
        Random rn = new Random();
	CreateSample a= new CreateSample();
        a.create();
        a.makeXMLfile(fileName);
        a.checkViolation();
        
    }

}

 class Service {
	public static int rand(int min, int max) {
        try {
            Random rn = new Random();
            int range = max - min + 1;
            int randomNum = min + rn.nextInt(range);
            return randomNum;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
	
	
	
}