package dcr2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Executor {
	
	private static final String pathToFiles = "D:\\Università\\Digital Content Retrieval B\\Progetto DCR\\wiki_storage";
	
	private HashMap<Integer, Term> map;
	
	private Stemmer stemmer;
	
	private ArrayList<String> stopList;
	
	public Executor() {
		
		this.map = new HashMap<Integer, Term>();
		this.stemmer = new Stemmer();
		this.stopList = new ArrayList<String>();
		
		populateStopList();
		
	}
	
	public void populateStopList() {
		
		try {
			
			File file = new File("stoplist.txt");
		
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String content;
			
			while ((content = br.readLine()) != null) {
				
				String w = content.split(";")[0];
				
				stopList.add(w);
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<String> search(){
		
		if(map.size()<1) {
			
			System.out.println("\nYou have to create/read the index first\n");
			return null;
			
		}
		
		System.out.print("\nDivide the words with space for conjunctive queries and with \"!\" for disjunctive queries\nEnter what you want to search for: ");
		
		Scanner in = new Scanner(System.in);
		String s = in.nextLine();
		
		String[] orSplitted = s.split("!");
		String[] splitted = s.split(" ");
		
		boolean disjunctive=false;

		if(orSplitted.length>1) {
			splitted=orSplitted;
			disjunctive=true;
		}
		
		ArrayList<String> tempList = new ArrayList<String>();
		ArrayList<ArrayList<String>> allPostings = new ArrayList<ArrayList<String>>();
		
		for(String single: splitted) {
			
			single=cleanTerm(single);
			
			if(checkStopList(single)) {
				continue;
			}
			
			Integer hashTemp = single.hashCode();
			
			Term obtainedTerm = map.get(hashTemp);
			
			if(obtainedTerm!=null) {
				tempList=obtainedTerm.getPosting();
				allPostings.add(tempList);
			}
			
		}
		
		if(allPostings.size()==0) {
			return null;
		}
		
		ArrayList<String> finalList = allPostings.get(0);
		
		for(ArrayList<String> a: allPostings) {
			
			if(disjunctive) {
				finalList=(ArrayList<String>) union(finalList,a);
			}else {
				finalList=(ArrayList<String>) intersection(finalList,a);
			}
			
		}

		return finalList;
		
	}
	
	public void storeIndex() {
		
		if(map.size()<1) {
			
			System.out.println("\nYou have to create the index first\n");
			return;
			
		}
		    
		try {
			
			FileOutputStream f;
			f = new FileOutputStream(new File("index.txt"));
		
		    ObjectOutputStream o = new ObjectOutputStream(f);

		    o.writeObject(this.map);
		    o.close();
		    f.close();
	    
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void readIndex() {
		
		try {
			
			FileInputStream fi = new FileInputStream(new File("index.txt"));
			ObjectInputStream oi = new ObjectInputStream(fi);
	
			this.map = (HashMap<Integer, Term>) oi.readObject();
	
			oi.close();
			fi.close();
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void buildIndex() {
		
		ArrayList<String> terms = tokenize();
		
		map.clear();
		
		for(String s: terms) {
			
			String value = s.split("&")[0];

			String document = s.split("&")[1];
			
			Integer hashTemp = value.hashCode();
			
			Term searchedTerm = map.get(hashTemp);
			
			if(searchedTerm==null) {
				
				Term termTemp = new Term(value);
				
				termTemp.insertDocument(document);
				
				map.put(hashTemp, termTemp);

			}else {
				
				searchedTerm.insertDocument(document);
				
			}
				
		}
		
	}
	
	public ArrayList<String> tokenize() {
		
		ArrayList<String> terms = new ArrayList<String>();
		
		File folder = new File(pathToFiles);
		File[] listOfFiles = folder.listFiles();
		
		for(int i=0;i<listOfFiles.length;i++) {
		//for(int i=0;i<1;i++) {
			File file = listOfFiles[i];
			if(file.isFile() && file.getName().endsWith(".txt")) {
				
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					
					String content;
					
					while ((content = br.readLine()) != null) {
						
						String[] splitted = content.split(" ");
						
						for(String s: splitted) {
							
							String temp = cleanTerm(s);
							
							if((temp.length()<2)||(checkStopList(temp))) {
								continue;
							}
							
							terms.add(temp+"&"+file.getName());
						}
			            
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
					 
			}
			
		}
		
		return terms;
		
	}
	
	public String cleanTerm(String s) {
		
		s=s.toLowerCase().replaceAll("[^a-zA-Z]", "");
		stemmer.add(s.toCharArray(),s.length());
		stemmer.stem();
		
		return stemmer.toString();
		
	}
	
	public <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }

    public <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }
    
    public boolean checkStopList(String s) {
    	
    	if(stopList.contains(s)) {
    		return true;
    	}
    	
    	return false;
    	
    }
	
	public void menu() {
		
		String menu = "=====================================\n"
				+"Choose an option:\n"
				+"1. Build the index\n"
				+"2. Store the index\n"
				+"3. Read the index\n"
				+"4. Perform a search\n"
				+"5. End the program\n"
				+"=====================================\n>> ";
		
		loop: while(true) {
		
			System.out.print(menu);
			
			Scanner in = new Scanner(System.in);
			int num = in.nextInt();
			
			switch(num) {
			
				case 1:{
					buildIndex();
					
					System.out.println("\nDone\n");
					
					break;
				}
				case 2:{
					storeIndex();
					
					System.out.println("\nDone\n");
					
					break;
				}
				case 3:{
					readIndex();
					
					System.out.println("\nDone\n");
					
					break;
				}
				case 4:{
					
					ArrayList<String> r = search();
					
					System.out.println("\nResulting documents:\n");
					
					if(r==null) {
						break;
					}
					
					for(String s: r) {
						System.out.println(s);
					}
					
					System.out.println("\n");
					
					break;
				}
				case 5:{
					break loop;
				}
				default:{
					break loop;
				}
			
			}
		
		}
		
	}

}
