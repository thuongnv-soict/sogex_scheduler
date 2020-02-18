package localsearch.applications.timetabling;

//changed , not original

import java.io.*;
import java.util.*;

import localsearch.model.*;
import localsearch.search.TabuSearch;
import localsearch.constraints.basic.*;
import localsearch.functions.basic.FuncPlus;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import localsearch.functions.conditionalsum.ConditionalSum;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

class Busy {
	public int day;
	public int slot;

	public Busy(int day, int slot) {
		this.day = day;
		this.slot = slot;
	}
}

class Room {
	public int id;
        public int name;
	public int maxStudents;
        
	public Room(int day, int name, int maxStudents) {
		this.id = id;
                this.name=name;
		this.maxStudents = maxStudents;
	}
}

class Classes {
	public int id;
	public String name;
        public int maxStudents;
	public Classes(int id, String name,int maxStudents) {
		this.id = id;
                this.name=name;
		this.maxStudents = maxStudents;
	}
}

class Course {
	public int courseID;
	public String name;
	public int instructorID;
	public int nbSlots;
	public Classes classes;

	public Course(int ID, String name, int instructorID, int nbSlots,
			Classes classes) {
		this.courseID = ID;
		this.name = name;
		this.instructorID = instructorID;
		this.nbSlots = nbSlots;
		this.classes = classes;
	}
}

class Instructor {
	public int instructorID;
	public int name;
	public ArrayList<Busy> busyList;

	public Instructor(int ID, int name, ArrayList<Busy> busyList) {
		this.instructorID = ID;
		this.name = name;
		this.busyList = busyList;
	}
}

public class TimeTabling {

	// data input
	ArrayList<Course> courses;
	Instructor[] instructors;
        Room[] room;
        Classes[] classes;

	// model
	LocalSearchManager ls;
	ConstraintSystem S;
	VarIntLS[] x;
	VarIntLS[] x_d;
	VarIntLS[] x_s;
	VarIntLS[] x_r;
	// solution store
	int[] vx;
	
	Random R;

	public TimeTabling() {

	}

	public void readData(String fn) {

		try {
			File file = new File(fn);
			DocumentBuilderFactory d = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbFactory = d.newDocumentBuilder();

			Document doc = dbFactory.parse(file);
			Element node = (Element) doc.getElementsByTagName("timetabling")
					.item(0);

			Element tmp = (Element) node
					.getElementsByTagName("instructorsList").item(0);
                        Element tmp2 = (Element) node
					.getElementsByTagName("classesList").item(0);
                        Element tmp3 = (Element) node
					.getElementsByTagName("roomsList").item(0);
			NodeList instructorsList = tmp.getElementsByTagName("instructor");

			NodeList coursesList = node.getElementsByTagName("course");
                        NodeList classesList = tmp2.getElementsByTagName("class");
                        System.out.println(classesList.getLength());
                        NodeList roomList = tmp3.getElementsByTagName("room");
			// System.out.println(""+node.getChildNodes().item(3).getNodeName());

			// read IntructorList
			// nin = instructorsList.getLength();
			// nco = coursesList.getLength();
			instructors = new Instructor[instructorsList.getLength()];
                        room= new Room[roomList.getLength()];
                        classes= new Classes[classesList.getLength()];
			courses = new ArrayList<Course>();
			// System.out.println("nin =" + nin + ",nco= " + nco);
			for (int i = 0; i < instructors.length; i++) {
				Node no = instructorsList.item(i);
				// System.out.println("i= "+i );
				if (no.getNodeType() == Node.ELEMENT_NODE) {
					Element in = (Element) no;
					String sid = in.getElementsByTagName("id").item(0)
							.getTextContent();
					String sname = in.getElementsByTagName("name").item(0)
							.getTextContent();
					int id = Integer.parseInt(sid);
					int name = Integer.parseInt(sname);
					// System.out.println("id= "+id+",name= "+sname);
					NodeList b = in.getElementsByTagName("busy");
					ArrayList<Busy> bb = new ArrayList();
					for (int j = 0; j < b.getLength(); j++) {
						Element cl = (Element) b.item(j);
						Busy busy = new Busy(Integer.parseInt(cl
								.getElementsByTagName("day").item(0)
								.getTextContent()) - 2, Integer.parseInt(cl
								.getElementsByTagName("slot").item(0)
								.getTextContent()) - 1);

						bb.add(busy);
						System.out.println("" + busy.day + " " + busy.slot);
					}
					instructors[i] = new Instructor(id, name, bb);
					// System.out.println(" "+instructors[i].getId() );
				}
			}
                        for (int i = 0; i < room.length; i++) {
				Node no = roomList.item(i);
				if (no.getNodeType() == Node.ELEMENT_NODE) {
					Element in = (Element) no;
					String sid = in.getElementsByTagName("id").item(0)
							.getTextContent();
					String sname = in.getElementsByTagName("name").item(0)
							.getTextContent();
                                        String snbrSlots = in.getElementsByTagName("nbrSlots").item(0)
							.getTextContent();
					int id = Integer.parseInt(sid);
					int name = Integer.parseInt(sname);
                                        int nbrSlots = Integer.parseInt(snbrSlots);
					
					room[i] = new Room(id, name, nbrSlots);
				}
			}
                        for (int i = 0; i < classes.length; i++) {
				Node no = classesList.item(i);
                                System.out.println(no.getTextContent());
				if (no.getNodeType() == Node.ELEMENT_NODE) {
					Element in = (Element) no;
					String sid = in.getElementsByTagName("id").item(0).getTextContent();
					String sname = in.getElementsByTagName("name").item(0)
							.getTextContent();
                                        String snbrSlots = in.getElementsByTagName("nbrSlots").item(0)
							.getTextContent();
					int id = Integer.parseInt(sid);
                                        int nbrSlots = Integer.parseInt(snbrSlots);
					
					classes[i] = new Classes(id, sname, nbrSlots);
				}
			}
			HashMap<String, Classes> map = new HashMap<String, Classes>();
                        for(int i = 0; i < classes.length; i++) map.put(classes[i].name, classes[i]);
                        int current=0;
			for (int i = 0; i < coursesList.getLength(); i++) {
				Node no = coursesList.item(i);
				if (no.getNodeType() == Node.ELEMENT_NODE) {
					Element in = (Element) no;
					String sid = in.getElementsByTagName("id").item(0)
							.getTextContent();
					String sname = in.getElementsByTagName("name").item(0)
							.getTextContent();
					String sinstructor = in.getElementsByTagName("instructor")
							.item(0).getTextContent();
					String sSlot = in.getElementsByTagName("nbrSlots").item(0)
							.getTextContent();
					int id = Integer.parseInt(sid);
					int instructor = Integer.parseInt(sinstructor);
					int slot = Integer.parseInt(sSlot);
					NodeList cl = in.getElementsByTagName("class");
					// System.out.println("" + instructor);
					for (int j = 0; j < cl.getLength(); j++) {
						Element cla = (Element) cl.item(j);
                                                Classes I = map.get(cla.getTextContent());
                                                courses.add(new Course(id, sname, instructor, slot,I ));
						current++;
                                                // System.out.println(""+cla.getTextContent());
					}
					//courses[i] = new Course(id, sname, instructor, slot, c);
				}
			}

			vx = new int[courses.size()];
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public boolean commonClass(Course c1, Course c2){
		Classes clsi = c1.classes;
		Classes clsj = c2.classes;
		if(clsi.equals(clsj)) return true;

		return false;
	}
	public void stateModel(){
		ls = new LocalSearchManager();
		S = new ConstraintSystem(ls);
		x_d = new VarIntLS[courses.size()];
		x_s = new VarIntLS[courses.size()];
                x_r= new VarIntLS[courses.size()];
		for(int i = 0; i < courses.size(); i++){
			x_d[i] = new VarIntLS(ls,0,4);
			x_s[i] = new VarIntLS(ls,0,6-courses.get(i).nbSlots);
                        x_r[i] = new VarIntLS(ls,0,room.length-1);
		}
		for(int i = 0; i < courses.size()-1; i++){
			for(int j = i+1; j < courses.size(); j++){
				boolean conflict = courses.get(i).instructorID == courses.get(j).instructorID; 
				if(!conflict) conflict = commonClass(courses.get(i), courses.get(j));
				
				if(conflict){
					S.post(new Implicate(
							new IsEqual(x_d[i],x_d[j]), 
							new NotOverLap(x_s[i],courses.get(i).nbSlots, x_s[j], courses.get(j).nbSlots)
							)
					);
				}
			}
		}
		ConditionalSum[][] s_room = new ConditionalSum[room.length][6*4];
                int[] courseSize=new int[courses.size()];
                for(int i=0;i<courses.size();i++) courseSize[i]=courses.get(i).classes.maxStudents;
                for(int i=0;i<room.length;i++)
                    for(int j=0;j<s_room.length;j++) 
                        s_room[i][j] = new ConditionalSum(x_d,courseSize,i);
                for(int i=0;i<room.length;i++)
                    for(int j=0;j<s_room.length;j++)
                        S.post(new LessOrEqual(s_room[i][j],room[i].maxStudents));
		HashMap<Integer, Instructor> map = new HashMap<Integer, Instructor>();
		for(int i = 0; i < instructors.length; i++)
			map.put(instructors[i].instructorID, instructors[i]);
		
		for(int i = 0; i < courses.size(); i++){
			Instructor I = map.get(courses.get(i).instructorID);
			for(int j = 0; j < I.busyList.size(); j++){
				Busy b = I.busyList.get(j);
				for(int k = 0; k < courses.get(i).nbSlots; k++){
					S.post(new Implicate(
							new IsEqual(x_d[i],b.day),
							new NotEqual(new FuncPlus(x_s[i],k),b.slot)
							));
					
				}
			}
		}
                
                
                
		ls.close();
	}
	

	public boolean search() {
		localsearch.search.TabuSearch ts = new localsearch.search.TabuSearch();
		ts.search(S, 30, 20, 100000, 50);

		for(int i = 0; i < courses.size(); i++){
			vx[i] = x_d[i].getValue()*6 + x_s[i].getValue();
		}
		System.out.println("Result S = " + S.violations());
		return S.violations() == 0;
	}

	public void printSolutionHTML(String fn) {
		ArrayList<Classes> cl = new ArrayList<>();
                
		for(int i=0;i<classes.length;i++) cl.add(classes[i]);
		try {

			File file = new File(fn);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head>");
			bw.write("<body><table BORDER=1 \"WIDTH:960px\" BGCOLOR=\"white\" align=center><tr><th colspan=32>Thời khóa biểu</th></tr>");
			// in cac muc
			bw.write("<tr>");
			bw.write("<TH ROWSPAN =2 colspan=2 BGCOLOR=\"gray\" \"> Lớp </ TH>");

			for (int i = 2; i < 7; i++) {
				bw.write("<th colspan=\"6\" STYLE=\"WIDTH:180px\" BGCOLOR=\"gray\">Thứ "
						+ i + "</th>");
			}
			bw.write("</tr>");
			bw.write("<tr>");
			for (int i = 0; i < 30; i++) {
				bw.write("<td colspan=\"1\" STYLE=\"WIDTH:30px \" BGCOLOR=\"gray\">tiết "
						+ (i % 6 + 1) + "</td>");
			}

			bw.write("</tr>");
			// in ra tiet hoc cac lop
			for (int i = 0; i < cl.size(); i++) {
				// System.out.println("Lớp : " + cl.get(i));
				bw.write("<tr><td colspan=2 BGCOLOR=\"gray\">" + cl.get(i).name
						+ "</td>");
				int last = 0;
				for (int t = 0; t < 30; t++) {
					if ((t) % 6 == 0 && last != t || (t == 29 && t == last)) {
						bw.write("<td STYLE=\"WIDTH:" + ((t - last) * 30)
								+ "px\" \" colspan=" + (t - last) + "></td>");
						last = t;
					}
					for (int j = 0; j < courses.size(); j++) {
						//if (t == x[j].getValue()) {
						if (t == vx[j]) {
							Classes cl1 = courses.get(j).classes;
								if (cl.get(i).equals(cl1)) {
									if (t != last) {
										bw.write("<td STYLE=\"WIDTH:"
												+ ((t - last) * 30)
												+ "px\" \" colspan="
												+ (t - last) + "></td>");
									}
									bw.write("<td STYLE=\"WIDTH:"
											+ courses.get(j).nbSlots
											+ "px\" BGCOLOR=\"lightgray\" colspan="
											+ courses.get(j).nbSlots + ">"
											+ courses.get(j).name
											+ " ,Giảng viên:  "
											+ courses.get(j).instructorID 
                                                                                        + " , Phòng học: "
                                                                                        + x_r[j].getValue()
                                                                                        +    "</td>");
									last = t + courses.get(j).nbSlots;
									break;
								}
							

						}
					}

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
	public void testBatch(String filename, int nbTrials) {
		TimeTabling TT = new TimeTabling();
		TT.readData(filename);
		double[] t = new double[nbTrials];
		double avg_t = 0;
		int nbSolved = 0;
		for (int k = 0; k < nbTrials; k++) {
			double t0 = System.currentTimeMillis();
			TT.stateModel();
			boolean ok = TT.search();
			if(ok){
				t[k] = (System.currentTimeMillis() - t0)*0.001;
				avg_t += t[k];
				nbSolved++;
			}
		}
		avg_t = avg_t*1.0/nbSolved;
		System.out.println("Time = " + avg_t + ", nbSolved = " + nbSolved);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		TimeTabling TT = new TimeTabling();
		TT.readData("C:\\Users\\Admin\\Desktop\\OpenCBLS-master\\source-code\\OpenCBLS\\data\\SIETimeTabling\\timetabling-data-10-10.xml");
		TT.stateModel();
		TT.search();
		TT.printSolutionHTML("TimeTabling.html");
		
		//TimeTabling TT = new TimeTabling();
		//TT.testBatch("data\\SIETimeTabling\\timetabling-data-46-46.xml",10);

	}

}
