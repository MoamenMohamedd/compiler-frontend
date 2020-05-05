package parsergenerator;

import java.util.ArrayList;
import java.util.Stack;

import lexicalanalyzergenerator.LexicalAnalyzer;
import lexicalanalyzergenerator.LexicalAnalyzerGenerator;

public class Parser {
    private ParseTable parseTable;
    private LexicalAnalyzer lexicalAnalyzer;
    
    private int iterator;
    public Parser(ParseTable parseTable) {
        this.parseTable = parseTable;
        
    }

    public void setLexicalAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }
    

    // Parse
    public boolean parse() {
    	this.iterator=0;
    	Stack<String> stack=new Stack();
    	stack.push("$");		//initializing the stack of symbols
    	//stack.push(this.parseTable.getStartSymbol());
    	stack.push("METHOD_BODY");
    	ArrayList<String> input=new ArrayList<>();
    	String temp;
    	// getting input tokens from lexical analyzer
    	while(lexicalAnalyzer.hasNext())
    			{
    					temp =lexicalAnalyzer.getNext();
    					if(temp!=null)
    						input.add(temp);
    			}
        	//input.add("}");

    	input.add("$");
	

    	
    	
    	
    	while(!stack.isEmpty()&& this.iterator<input.size())
    	{
    		if(stack.peek().equals("$")&&input.get(this.iterator).equals("$"))        //Both the stack and the input string  are empty
    			{
    					System.out.println("Successful Completion)");
    					return true;
    			
    			}
    		
    		else if(stack.peek().equals(input.get(this.iterator)))							//A match do exists for 2 terminals
    			{
    								
    					iterator++;
    					stack.pop();
    					continue;
    			}
    		
    		//look up for the non terminal in the hash map keys to check if the current symbol is a non terminal
  		else if(parseTable.isNonTerminal(stack.peek()))					//the top of the stack is a non terminal  
   		{
    		
			    			ArrayList<String> production=(ArrayList<String>) this.parseTable.get(stack.peek(), input.get(this.iterator)) ;
			    			// if the production table is empty 
			    			if(production ==null) {
			    				
			    				System.out.println("Error:(illegal "+stack.peek()+") – discard "+input.get(this.iterator));
			    				this.iterator++;
			    			}
			    			
			    			else if(production.size()==0)	//Recovery from error Synch
			    			{
			    				stack.pop();
			    				System.out.println("ERROR");
			    			}
			    			else if(production.size()>0&& production.get(0).equals("epsilon"))
			    			{	
			    				temp=stack.pop();
			    				System.out.println(temp+" ->"+"epsilon");
				    			
			    			}
			    			else if(production.size()>0)
			    			{	 

			    				temp= stack.pop();
				    			for (int i = 0; i < production.size(); i++) {
				    							stack.push(production.get(production.size()-1-i));
								}
				    					System.out.print(temp+" ->");
							    			for (int i = 0; i < production.size(); i++) {
														System.out.print(" "+production.get(i));
							    					}
							    			System.out.println();
							    			continue;
			    			}
			    			
			    			
			    			
 		}// if the top of stack is a terminal symbol and there was no match
    		else  {	
    			
    		
			    			System.out.println("Error: missing "+stack.peek() +", inserted");
			    			stack.pop();
			    			
  		}
    		
    		
    	}

    	
    	
    	
    	
    	
    	
    	
    	
    
    	
    	
    	return false;
    }
//    public static void main(String[] args)  {
//    			LexicalAnalyzerGenerator lexicalAnalyzerGenerator = new LexicalAnalyzerGenerator(System.getProperty("user.dir") + "/src" + "//input-rules-3.txt");
//
//
//    	        LexicalAnalyzer lexicalAnalyzer = lexicalAnalyzerGenerator.getLexicalAnalyzer();
//    	        lexicalAnalyzer.setInputProgram(System.getProperty("user.dir") + "/src" + "//input-program-3.txt");
//    	        Parser p=new Parser(null,"s");
//    	        p.setLexicalAnalyzer(lexicalAnalyzer);
//    	        p.parse();
//
//    	}
}
