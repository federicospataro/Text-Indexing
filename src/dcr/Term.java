package dcr2;

import java.io.Serializable;
import java.util.ArrayList;

public class Term implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String value;
	private ArrayList<String> posting;
	
	public Term(String value) {
		
		this.value = value;
		
		this.posting = new ArrayList<String>();
		
	}
	
	public String getValue() {
		
		return value;
		
	}
	
	public ArrayList<String> getPosting(){
		
		return posting;
		
	}
	
	public void insertDocument(String s) {
		
		posting.add(s);
		
	}
	
	@Override
	public String toString() {
		return "Value: "+value+"\n"+posting.toString();
	}

}
