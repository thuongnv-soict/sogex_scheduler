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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import localsearch.functions.conditionalsum.ConditionalSum;

import org.w3c.dom.*;

/**
 *
 * @author Phạm Tiến Dũng
 */


public class BCAgreedytabu {
    private static final String  fileInput="BCArandom.txt";
    private static final String  fileOutput="BCAgreedytabu.html";
    
    ArrayList<Classes> classes;
    ArrayList<Instructor> instructors;
    ArrayList<Course> courses;
    ArrayList<InstructorTime> intructor_time_list;
    // model
    LocalSearchManager ls;
    ConstraintSystem S;
    VarIntLS[] x;       
    ConditionalSum[] intructor_time;
    FuncMinus difTime;
    
    int[][] teach;
    int[][] busy ;
    int[][] class_slot;
    int[] class_weight;
    int[][] overlap;
    
    public void readData(String fn) {
        try {
			File file = new File(fn);
			DocumentBuilderFactory d = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbFactory = d.newDocumentBuilder();

			Document doc = dbFactory.parse(file);
			Element node = (Element) doc.getElementsByTagName("bca").item(0);

			Element tmp = (Element) node.getElementsByTagName("instructorsList").item(0);
                        Element tmp2 = (Element) node.getElementsByTagName("classesList").item(0);
                        Element tmp3 = (Element) node.getElementsByTagName("coursesList").item(0);
			
                        NodeList instructorsList = tmp.getElementsByTagName("instructor");
			NodeList classesList = tmp2.getElementsByTagName("class");
                        NodeList coursesList = tmp3.getElementsByTagName("course");
                        System.out.println(classesList.getLength());
			// System.out.println(""+node.getChildNodes().item(3).getNodeName());

			// read IntructorList
			// nin = instructorsList.getLength();
			// nco = coursesList.getLength();
			instructors = new ArrayList<Instructor>();
                        classes= new ArrayList<Classes>();
			courses = new ArrayList<Course>();
			// System.out.println("nin =" + nin + ",nco= " + nco);
			for (int i = 0; i < instructorsList.getLength(); i++) {
				Node no = instructorsList.item(i);
                                int[] busySlot=new int[1];
				// System.out.println("i= "+i );
				if (no.getNodeType() == Node.ELEMENT_NODE) {
					Element in = (Element) no;
					String sid = in.getElementsByTagName("id").item(0).getTextContent();
					String sname = in.getElementsByTagName("name").item(0).getTextContent();
					int id = Integer.parseInt(sid);
                                        
					NodeList b = in.getElementsByTagName("busy");
					for (int j = 0; j < b.getLength(); j++) {
                                            
						Element cl = (Element) b.item(j);
                                                NodeList NLslot = cl.getElementsByTagName("slot");
                                                busySlot=new int[NLslot.getLength()];
                                                for (int k = 0; k < NLslot.getLength(); k++) {
                                                        busySlot[k]=Integer.parseInt(NLslot.item(k).getTextContent()) ;
                                                }
                                                
					}
                                        NodeList t = in.getElementsByTagName("teach");
                                        NodeList t2= ((Element)t.item(0)).getElementsByTagName("id");
                                        int[] courseIDTeached=new int[t2.getLength()];
					for (int j = 0; j < t2.getLength(); j++) {
						courseIDTeached[j]=Integer.parseInt(t2.item(j).getTextContent());
					}
					instructors.add(new Instructor(id, sname, busySlot, courseIDTeached)) ;
				}
			}
                        
                        for (int i = 0; i < classesList.getLength(); i++) {
				Node no = classesList.item(i);
                                int[] slot=new int[1];
                                //System.out.println(no.getTextContent());
				if (no.getNodeType() == Node.ELEMENT_NODE) {
					Element in = (Element) no;
					int id=Integer.parseInt(in.getElementsByTagName("id").item(0).getTextContent());
					int courseId=Integer.parseInt(in.getElementsByTagName("courseiD").item(0).getTextContent());
                                        NodeList t = in.getElementsByTagName("time");
                                        
                                        for (int j = 0; j < t.getLength(); j++) {
						Element cl = (Element) t.item(j);
                                                NodeList NLslot = cl.getElementsByTagName("slot");
                                                slot=new int[NLslot.getLength()];
                                                for (int k = 0; k < NLslot.getLength(); k++) {
                                                        slot[k]=Integer.parseInt(NLslot.item(k).getTextContent());
                                                }
                                               
					}
                                        
				classes.add(new Classes(id,courseId,slot)) ;
				}
			}
			for (int i = 0; i < coursesList.getLength(); i++) {
				Node no = coursesList.item(i);
				if (no.getNodeType() == Node.ELEMENT_NODE) {
					Element in = (Element) no;
					int id=Integer.parseInt(in.getElementsByTagName("id").item(0).getTextContent());
					String sname = in.getElementsByTagName("name").item(0).getTextContent();
                                        courses.add(new Course(id,sname));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    
    
        
    }

    public void stateModel() {
        
        ls = new LocalSearchManager();
	S = new ConstraintSystem(ls);
        x = new VarIntLS[classes.size()]; // instructor
        
        teach = new int[instructors.size()][classes.size()];
        busy = new int[instructors.size()][classes.size()];
        class_slot = new int[classes.size()][60];
        class_weight=new int[classes.size()];
        overlap = new int[classes.size()][classes.size()];
        
        for (int i = 0; i < classes.size(); i++) {
            x[i] = new VarIntLS(ls, 0, instructors.size()-1);
            class_weight[i]=classes.get(i).weight;
        }
        
        for (int i = 0; i < classes.size(); i++) {
            Classes a=classes.get(i);
            for (int j = 0; j < a.slot.length; j++) {
            class_slot[i][a.slot[j]] = 1;
            }
            
        }
        // Tinh overlap
        for (int i = 0; i < classes.size() - 1; i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                for (int k = 0; k < 60; k++)
                if (class_slot[i][k]==1 &&class_slot[j][k]==1) {
                    overlap[i][j] = 1;
                } 
            }
        }
        
        // Tinh teach
        for (int i = 0; i < instructors.size() ; i++) {
            Instructor I = instructors.get(i);
            for (int j = 0; j < classes.size(); j++){
                Classes C = classes.get(j);
                for (int k = 0; k < I.courseIDTeached.length ; k++){
                    if (I.courseIDTeached[k]==C.courseiD) {
                        teach[i][j]=1;
                       
                    }
                }
            }
        }
         // Tinh busy
        for (int i = 0; i < instructors.size(); i++) {
            Instructor I = instructors.get(i);
            for (int j = 0; j < classes.size(); j++) {
                Classes C = classes.get(j);
                for (int m = 0; m < I.busySlot.length; m++) {
                    for (int k = 0; k < C.slot.length; k++) {
                        if(C.slot[k]==I.busySlot[m]) busy[i][j]=1;   
                    }
                    
                }
            }
        }

        //condition 1: same intrustor diffrent time
        for (int i = 0; i < classes.size() - 1; i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                if (overlap[i][j] == 1) {
                    S.post(new NotEqual(x[i], x[j]));
                }
            }
        }
        

      
        //condition 2 : classes!= busy day
        
        for (int i = 0; i < instructors.size() - 1; i++) {
            for (int j = 0; j < classes.size(); j++) {
                if (busy[i][j] == 1) {
                    S.post(new NotEqual(x[j], i));
                }
            }
        }
        
        //condition 3 : classes in the teached course
        for (int i = 0; i < instructors.size(); i++) {
            for (int j = 0; j < classes.size(); j++) {
                if (teach[i][j] == 0) {
                    S.post(new NotEqual(x[j], i));
                }
            }
        }
        
        //Tong thoi gian day cua Giang vien
        intructor_time = new ConditionalSum[instructors.size()];
        intructor_time_list=new  ArrayList<InstructorTime>();
        for (int i = 0; i < instructors.size(); i++) {
            intructor_time[i] = new ConditionalSum(x, class_weight, i);
            intructor_time_list.add(new InstructorTime(i,intructor_time[i]));
        }
        
        
        FMax maxTime=new FMax(intructor_time);
        FMin minTime=new FMin(intructor_time);
        difTime=new FuncMinus(maxTime,minTime);
        ls.close();
    }

    public boolean tabuSearch() {
        double time;
        TabuSearch ts = new localsearch.search.TabuSearch();
        //while(S.violations()>0){
            for(int i=0;i<classes.size();i++) {
                if(x[i].getValue()==-1) x[i].setValuePropagate(Service.rand(0, instructors.size()-1));
                
            }
             time=ts.searchMaintainConstraintsMinimize2(difTime,S, 5000, 5000, 5000, 200);
        //}
       
        
        
        System.out.println("Result S = " + S.violations()+" Time Different: "+difTime.getValue());
        System.out.println("Time:"+time);
        for(int i=0;i<classes.size();i++) System.out.print(x[i].getValue()+"  ");
        
        return S.violations() == 0;
        
    }
    
    
    public boolean greedySearch() {
        
        
        for(int i=0;i<classes.size();i++) {
                x[i].setValuePropagate(-1);
                
        }
        
        
        for(int i=0;i<classes.size();i++) {
            Collections.sort(intructor_time_list);
            for(int j=0;j<instructors.size();j++) {
                int instructorId=intructor_time_list.get(j).instructorID;
                if(busy[instructorId][i]==0&&teach[instructorId][i]==1) {
                    x[i].setValuePropagate(instructorId);
                    for(int m=0;m<classes.size();m++) {
                        if(overlap[i][m]==1) busy[instructorId][m]=1;
                    }
                    break;
                }
                
            }
        }
        System.out.println("Result S = " + S.violations()+" Time Different: "+difTime.getValue());
        
        for(int i=0;i<classes.size();i++) System.out.print(x[i].getValue()+"  ");
        
        return S.violations() == 0;
    }
    
    public void printSolutionHTML(String fn) {
		try {   
                        File file = new File(fn);
                        HashMap<Integer, Course> map = new HashMap<Integer, Course>();
                        for(int i = 0; i < courses.size(); i++) map.put(courses.get(i).courseiD, courses.get(i));

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head>");
			bw.write("<body><table BORDER=1 \"WIDTH:1920px\" BGCOLOR=\"white\" align=center><tr><th colspan=64>Thời khóa biểu</th></tr>");
			// in cac muc
			bw.write("<tr>");
			bw.write("<TH ROWSPAN =2 colspan=2 BGCOLOR=\"gray\" \"> Giang vien </ TH>");

			for (int i = 2; i < 7; i++) {
				bw.write("<th colspan=\"12\" STYLE=\"WIDTH:360px\" BGCOLOR=\"gray\">Thứ "
						+ i + "</th>");
			}
			bw.write("</tr>");
			bw.write("<tr>");
			for (int i = 0; i < 60; i++) {
				bw.write("<td colspan=\"1\" STYLE=\"WIDTH:30px \" BGCOLOR=\"gray\">tiết "
						+ (i % 12 + 1) + "</td>");
			}

			bw.write("</tr>");
			// in ra tiet hoc cac lop
			for (int i = 0; i < instructors.size(); i++) {
				// System.out.println("Lớp : " + classes.get(i));
				bw.write("<tr><td colspan=2 BGCOLOR=\"gray\">" + instructors.get(i).name
						+ "</td>");
				int last = 0;
				for (int t = 0; t < 59; t++) {
					if ((t) % 12 == 0 && last != t ) {
						bw.write("<td STYLE=\"WIDTH:" + ((t - last) * 30)
								+ "px\" \" colspan=" + (t - last) + "></td>");
						last = t;
					}
					for (int j = 0; j < classes.size(); j++) {
						//if (t == x[j].getValue()) {
						if (class_slot[j][t]==1&&i==x[j].getValue()) {
                                                    int weight=1;
                                                    int m=t;
                                                    while((m+1)!=60&&class_slot[j][m+1]==1){
                                                        weight++;
                                                        m++;
                                                        
                                                    }
						
						if (t != last) {
							bw.write("<td STYLE=\"WIDTH:"+ ((t - last) * 30)+ "px\" \" colspan="+ (t - last) + "></td>");
						}
						bw.write("<td STYLE=\"WIDTH:"+ weight+ "px\" BGCOLOR=\"lightgray\" colspan="+ weight+ ">"
                                                            + " Ma lop hoc:  "+ classes.get(j).classID+ " ,Ten mon hoc:  "+ map.get(classes.get(j).courseiD).courseName +    "</td>");
                                                                        
						last = t + weight;
                                                t=m;
						break;
								
							

						}
                                                
					}
                                       
				}
                                if(last!=60){
                                    bw.write("<td STYLE=\"WIDTH:" + ((60 - last) * 30) + "px\" \" colspan=" + (60 - last) + "></td>");
                                }
                           
				bw.write("</tr> ");
			}
			bw.write("</table></body></html>");
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    public static void main(String[] args) {
		
		BCAgreedytabu bca = new BCAgreedytabu();
		bca.readData(fileInput);
		bca.stateModel();
		bca.greedySearch();
                bca.tabuSearch();
                System.out.println();
		bca.printSolutionHTML(fileOutput);
		System.out.println(bca.classes.get(3).slot[0]);
                
	}
}
