package lexicalanalyzergenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Stack;

public class LexicalAnalyzer {
	private static int iterator;
    private DFA dfa;
	private static char head;
	private String program;
    

	public LexicalAnalyzer(DFA dfa) {
        this.dfa = dfa;
        this.iterator=0;
        this.program="";
    }

//	public static int getIterator() {
//		return iterator;
//	}
//
//	public static void setIterator(int iterator) {
//		LexicalAnalyzer.iterator = iterator;
//	}
//
//	public static char getHead() {
//		return head;
//	}
//
//	public static void setHead(char head) {
//		LexicalAnalyzer.head = head;
//	}
//	

    public void setInputProgram(String pathToProgram){
    	String data = "";
		
	      File myObj = new File(pathToProgram);
	      Scanner myReader;
		try {
			myReader = new Scanner(myObj);
		
	      while (myReader.hasNextLine()) {
	         data += myReader.nextLine();
	      }
	      myReader.close();

    System.out.println(data);
		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		this.program=data;

    }

    /**
     * Gets next token from input program
     *
     * @return String
     */
    public String getNext(){
       
    	Stack<String> tokens=new Stack<>();
		Stack<String> labels=new Stack<>();
		

		String token="",label;
		// skip first space
		this.dfa.reset();
		if(this.program.charAt(this.iterator)==' ') this.iterator++;
			
		while (this.iterator<this.program.length())
		{      
			//System.out.println("token=   "+token);

			token+=this.program.charAt(this.iterator);
			head=this.program.charAt(this.iterator);
			//get the label from the DFA
			label =this.dfa.advance(this.head);
			this.iterator++;
			
			if(label!=null)		
			{
				tokens.push(token);
				labels.push(label);

			}
			else {
				
				if(!labels.isEmpty()) {
					this.iterator--;
					this.dfa.reset();
					//print token's label
					System.out.println(labels.peek());
					return labels.pop();

				}
				
			}
			
		}
		
		
		return null;
		
        
        
        
        
    }

    
    
    
    
    
    /**
     * Checks if there is a next token from
     * input program
     *
     * @return boolean
     */
    public boolean hasNext(){
        if(this.iterator<this.program.length())
        		return true;
        return false;
    }
//    public static void main(String[] args)  {
//		// TODO Auto-generated method stub
//		//Read test program from 
//    	LexicalAnalyzer x=new LexicalAnalyzer(null);
//		x.setInputProgram(System.getProperty("user.dir") + "/src" + "//TestProgram.txt");
//		x.getNext();
//	}
}
