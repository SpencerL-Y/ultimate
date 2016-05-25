				    
package java_cup.runtime;

import java.util.ArrayList;

/** This class implements a skeleton table driven LR parser.  In general,
 *  LR parsers are a form of bottom up shift-reduce parsers.  Shift-reduce
 *  parsers act by shifting input onto a parse stack until the Symbols 
 *  matching the right hand side of a production appear on the top of the 
 *  stack.  Once this occurs, a reduce is performed.  This involves removing
 *  the Symbols corresponding to the right hand side of the production
 *  (the so called "handle") and replacing them with the non-terminal from
 *  the left hand side of the production.  <p>
 *
 *  To control the decision of whether to shift or reduce at any given point, 
 *  the parser uses a state machine (the "viable prefix recognition machine" 
 *  built by the parser generator).  The current state of the machine is placed
 *  on top of the parse stack (stored as part of a Symbol object representing
 *  a terminal or non terminal).  The parse action table is consulted 
 *  (using the current state and the current lookahead Symbol as indexes) to 
 *  determine whether to shift or to reduce.  When the parser shifts, it 
 *  changes to a new state by pushing a new Symbol (containing a new state) 
 *  onto the stack.  When the parser reduces, it pops the handle (right hand 
 *  side of a production) off the stack.  This leaves the parser in the state 
 *  it was in before any of those Symbols were matched.  Next the reduce-goto 
 *  table is consulted (using the new state and current lookahead Symbol as 
 *  indexes) to determine a new state to go to.  The parser then shifts to 
 *  this goto state by pushing the left hand side Symbol of the production 
 *  (also containing the new state) onto the stack.<p>
 *
 *  This class actually provides four LR parsers.  The methods parse() and 
 *  debug_parse() provide two versions of the main parser (the only difference 
 *  being that debug_parse() emits debugging trace messages as it parses).  
 *  In addition to these main parsers, the error recovery mechanism uses two 
 *  more.  One of these is used to simulate "parsing ahead" in the input 
 *  without carrying out actions (to verify that a potential error recovery 
 *  has worked), and the other is used to parse through buffered "parse ahead" 
 *  input in order to execute all actions and re-synchronize the actual parser 
 *  configuration.<p>
 *
 *  This is an abstract class which is normally filled out by a subclass
 *  generated by the JavaCup parser generator.  In addition to supplying
 *  the actual parse tables, generated code also supplies methods which 
 *  invoke various pieces of user supplied code, provide access to certain
 *  special Symbols (e.g., EOF and error), etc.  Specifically, the following
 *  abstract methods are normally supplied by generated code:
 *  <dl compact>
 *  <dt> String[] action_table()
 *  <dd> Provides the parse table.
 *  <dt> Symbol do_action() 
 *  <dd> Executes a piece of user supplied action code.  This always comes at 
 *       the point of a reduce in the parse, so this code also allocates and 
 *       fills in the left hand side non terminal Symbol object that is to be 
 *       pushed onto the stack for the reduce.
 *  <dt> void init_actions()
 *  <dd> Code to initialize a special object that encapsulates user supplied
 *       actions (this object is used by do_action() to actually carry out the 
 *       actions).
 *  </dl>
 *  
 *  In addition to these routines that <i>must</i> be supplied by the 
 *  generated subclass there are also a series of routines that <i>may</i> 
 *  be supplied.  These include:
 *  <dl>
 *  <dt> Symbol scan()
 *  <dd> Used to get the next input Symbol from the scanner.
 *  <dt> Scanner getScanner()
 *  <dd> Used to provide a scanner for the default implementation of
 *       scan().
 *  <dt> int error_sync_size()
 *  <dd> This determines how many Symbols past the point of an error 
 *       must be parsed without error in order to consider a recovery to 
 *       be valid.  This defaults to 3.  Values less than 2 are not 
 *       recommended.
 *  <dt> void report_error(String message, Object info)
 *  <dd> This method is called to report an error.  The default implementation
 *       simply prints a message to System.err and where the error occurred.
 *       This method is often replaced in order to provide a more sophisticated
 *       error reporting mechanism.
 *  <dt> void report_fatal_error(String message, Object info)
 *  <dd> This method is called when a fatal error that cannot be recovered from
 *       is encountered.  In the default implementation, it calls 
 *       report_error() to emit a message, then throws an exception.
 *  <dt> void syntax_error(Symbol cur_token)
 *  <dd> This method is called as soon as syntax error is detected (but
 *       before recovery is attempted).  In the default implementation it 
 *       invokes: report_error("Syntax error", null);
 *  <dt> void unrecovered_syntax_error(Symbol cur_token)
 *  <dd> This method is called if syntax error recovery fails.  In the default
 *       implementation it invokes:<br> 
 *         report_fatal_error("Couldn't repair and continue parse", null);
 *  </dl>
 *
 * @see     java_cup.runtime.Symbol
 * @version last updated: 7/3/96
 * @author  Frank Flannery
 */

public abstract class LRParser {
  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /** 
   * Simple constructor. 
   */
  public LRParser() {
    this(null);
  }

  /** 
   * Constructor that sets the default scanner. [CSA/davidm] 
   */
  @SuppressWarnings("deprecation")
  public LRParser(Scanner s) {
    this(s,new DefaultSymbolFactory()); // TUM 20060327 old cup v10 Symbols as default
  }
  /** 
   * Constructor that sets the default scanner and a SymbolFactory
   */
  public LRParser(Scanner s, SymbolFactory symfac) {
    symbolFactory = symfac;
    setScanner(s);
  }
  public SymbolFactory symbolFactory;// = new DefaultSymbolFactory();
  /**
   * Whenever creation of a new Symbol is necessary, one should use this factory.
   */
  public SymbolFactory getSymbolFactory(){
    return symbolFactory;
  }
  /*-----------------------------------------------------------*/
  /*--- (Access to) Static (Class) Variables ------------------*/
  /*-----------------------------------------------------------*/

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The symbol number of the error symbol (hardcoded)
   */
  private final static int ERROR = 0;
  /** The symbol number of the eof symbol (hardcoded)
   */
  private final static int EOF = 1;

  /** The number of Symbols after an error we much match to consider it 
   *  recovered from. 
   */
  protected int error_sync_size() {return 3; }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Internal flag to indicate when parser should quit. */
  private boolean _done_parsing = false;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** This method is called to indicate that the parser should quit.  This is 
   *  normally called by an accept action, but can be used to cancel parsing 
   *  early in other circumstances if desired. 
   */
  public void done_parsing()
    {
      _done_parsing = true;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/
  /* Global parse state shared by parse(), error recovery, and 
   * debugging routines */
  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The current lookahead Symbol. */
  protected Symbol cur_token;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The parse stack itself. */
  protected ArrayList<java_cup.runtime.Symbol> stack = new ArrayList<java_cup.runtime.Symbol>();
  
  private int[] base_table; 
  private short[] action_table; 
  private short[] reduce_table; 
  private short[] production_table; 

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** This is the scanner object used by the default implementation
   *  of scan() to get Symbols.  To avoid name conflicts with existing
   *  code, this field is private. [CSA/davidm] */
  private Scanner _scanner;

  /**
   * Simple accessor method to set the default scanner.
   */
  public void setScanner(Scanner s) { _scanner = s; }

  /**
   * Simple accessor method to get the default scanner.
   */
  public Scanner getScanner() { return _scanner; }

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Perform a bit of user supplied action code (supplied by generated 
   *  subclass).  Actions are indexed by an internal action number assigned
   *  at parser generation time.
   *
   * @param act_num   the internal index of the action to be performed.
   * @param stack     the parse stack of that object.
   */
  public abstract Symbol do_action(
    int       act_num, 
    ArrayList<java_cup.runtime.Symbol> stack) 
    throws java.lang.Exception;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The action table (supplied by generated subclass).  This
   *  table is automatically generated by the parser generator.
   */
  protected abstract String[] action_table();

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** User code for initialization inside the parser.  Typically this 
   *  initializes the scanner.  This is called before the parser requests
   *  the first Symbol.  Here this is just a placeholder for subclasses that 
   *  might need this and we perform no action.   This method is normally
   *  overridden by the generated code using this contents of the "init with"
   *  clause as its body.
   */
  public void user_init() throws java.lang.Exception { }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Initialize the action object.  This is called before the parser does
   *  any parse actions. This is filled in by generated code to create
   *  an object that encapsulates all action code. 
   */ 
  protected abstract void init_actions() throws java.lang.Exception;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Get the next Symbol from the input (supplied by generated subclass).
   *  Once end of file has been reached, all subsequent calls to scan 
   *  should return an EOF Symbol (which is Symbol number 0).  By default
   *  this method returns getScanner().next_token(); this implementation
   *  can be overriden by the generated parser using the code declared in
   *  the "scan with" clause.  Do not recycle objects; every call to
   *  scan() should return a fresh object.
   */
  public Symbol scan() throws java.lang.Exception {
    final Symbol sym = getScanner().next_token();
    return (sym!=null) ? sym : getSymbolFactory().newSymbol("END_OF_FILE",0);
  }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Report a fatal error.  This method takes a  message string and an 
   *  additional object (to be used by specializations implemented in 
   *  subclasses).  Here in the base class a very simple implementation 
   *  is provided which reports the error then throws an exception. 
   *
   * @param message an error message.
   * @param info    an extra object reserved for use by specialized subclasses.
   */
  public void report_fatal_error(
    String   message, 
    Object   info)
    throws java.lang.Exception
    {
      /* stop parsing (not really necessary since we throw an exception, but) */
      done_parsing();

      /* use the normal error message reporting to put out the message */
      report_error(message, info);

      /* throw an exception */
      throw new Exception("Can't recover from previous error(s)");
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Report a non fatal error (or warning).  This method takes a message 
   *  string and an additional object (to be used by specializations 
   *  implemented in subclasses).  Here in the base class a very simple 
   *  implementation is provided which simply prints the message to 
   *  System.err. 
   *
   * @param message an error message.
   * @param info    an extra object reserved for use by specialized subclasses.
   */
  public void report_error(String message, Object info)
    {
      System.err.print(message);
      System.err.flush();
      if (info instanceof Symbol)
	{
	    if (((Symbol)info).left != -1)
	      {
	          System.err.println(" at character " + ((Symbol)info).left + 
	          		   " of input");
	        } else
	      {
	          System.err.println("");
	        }
	  } else
	{
	    System.err.println("");
	  }
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** This method is called when a syntax error has been detected and recovery 
   *  is about to be invoked.  Here in the base class we just emit a 
   *  "Syntax error" error message.  
   *
   * @param cur_token the current lookahead Symbol.
   */
  public void syntax_error(Symbol cur_token)
    {
      report_error("Syntax error", cur_token);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** This method is called if it is determined that syntax error recovery 
   *  has been unsuccessful.  Here in the base class we report a fatal error. 
   *
   * @param cur_token the current lookahead Symbol.
   */
  public void unrecovered_syntax_error(Symbol cur_token)
    throws java.lang.Exception
    {
      report_fatal_error("Couldn't repair and continue parse", cur_token);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Fetch an action from the action table.    
   *
   * @param state the state index of the action being accessed.
   * @param sym   the Symbol index of the action being accessed.
   */
  private final short get_action(int state, int sym)
    {
      final int base = base_table[state]+2*sym;
      if (action_table[base] == state)
	{
	    return action_table[base + 1];
	  }
      /* no entry; return default */
      return action_table[state];
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Fetch a state from the reduce-goto table.    
   *
   * @param state the state index of the entry being accessed.
   * @param sym   the Symbol index of the entry being accessed.
   */
  private final short get_reduce(int state, int sym)
    {
      return reduce_table[reduce_table[state]+sym];
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** This method provides the main parsing routine.  It returns only when 
   *  done_parsing() has been called (typically because the parser has 
   *  accepted, or a fatal error has been reported).  See the header 
   *  documentation for the class regarding how shift/reduce parsers operate
   *  and how the various tables are used.
   */
  public Symbol parse() throws java.lang.Exception
    {
      /* the current action code */
      int act;

      /* initialize the action encapsulation object */
      init_actions();

      /* do user initialization */
      user_init();

      /* unpack action/reduce tables */
      unpackStrings(action_table());

      /* get the first token */
      cur_token = scan(); 

      /* push dummy Symbol with start state to get us underway */
      stack.clear();
      stack.add(getSymbolFactory().startSymbol("START", 0, 0));
      int parse_state = 0;

      /* continue until we are told to stop */
      for (_done_parsing = false; !_done_parsing; )
	{
	  /* current state is always on the top of the stack and in parse_state */

	  /* look up action out of the current state with the current input */
	  act = get_action(parse_state, cur_token.sym);

	  /* decode the action: odd encodes shift */
	  if ((act & 1) != 0)
	    {
	      /* shift to the encoded state by pushing it on the stack */
	      cur_token.parse_state = parse_state = (act >> 1);
	      stack.add(cur_token);

	      /* advance to the next Symbol */
	      cur_token = scan();
	    }
	  /* if its even, then it encodes a reduce action */
	  else if (act != 0)
	    {
	      act = (act >> 1)-1;
	      /* perform the action for the reduce */
	      final Symbol lhs_sym = do_action(act, stack);

	      /* look up information about the production */
	      int handle_size = production_table[2*act+1];
	      /* pop the handle off the stack */
	      while (handle_size-- > 0)
		{
		    stack.remove(stack.size()-1);
		  }
	      
	      /* look up the state to go to from the one popped back to */
	      parse_state = get_reduce(stack.get(stack.size()-1).parse_state, lhs_sym.sym);

	      /* shift to that state */
	      lhs_sym.parse_state = parse_state;
	      stack.add(lhs_sym);
	    }
	  /* finally if the entry is zero, we have an error */
	  else
	    {
	      error_recovery(false);
	      if (!stack.isEmpty())
		{
		    parse_state = stack.get(stack.size()-1).parse_state;
		  }
	    }
	}
      /* clean-up tables to save space */
      production_table = null;
      action_table = null;
      reduce_table = null;
      return stack.isEmpty() ? null : stack.get(stack.size()-1);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Write a debugging message to System.err for the debugging version 
   *  of the parser. 
   *
   * @param mess the text of the debugging message.
   */
  public void debug_message(String mess)
    {
      System.err.println(mess);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Dump the parse stack for debugging purposes. */
  public void dump_stack()
    {
      if (stack == null)
	{
	  debug_message("# Stack dump requested, but stack is null");
	  return;
	}

      debug_message("============ Parse Stack Dump ============");

      /* dump the stack */
      for (int i=0; i<stack.size(); i++)
	{
	  debug_message("Symbol: " + stack.get(i).sym +
			" State: " + stack.get(i).parse_state);
	}
      debug_message("==========================================");
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Do debug output for a reduce. 
   *
   * @param prod_num  the production we are reducing with.
   * @param nt_num    the index of the LHS non terminal.
   * @param rhs_size  the size of the RHS.
   */
  public void debug_reduce(int prod_num, Symbol nt, int rhs_size)
    {
      debug_message("# Reduce with prod #" + prod_num + " [NT=" + nt + 
	            ", " + "SZ=" + rhs_size + "]");
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Do debug output for shift. 
   *
   * @param shift_tkn the Symbol being shifted onto the stack.
   */
  public void debug_shift(Symbol shift_tkn)
    {
      debug_message("# Shift under term " + shift_tkn + 
		    " to state #" + shift_tkn.parse_state);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Do debug output for stack state. [CSA]
   */
  public void debug_stack() {
      StringBuffer sb=new StringBuffer("## STACK:");
      for (int i=0; i<stack.size(); i++) {
	  final Symbol s = stack.get(i);
	  sb.append(" <state "+s.parse_state+", sym "+s.sym+">");
	  if ((i%3)==2 || (i==(stack.size()-1))) {
	      debug_message(sb.toString());
	      sb = new StringBuffer("         ");
	  }
      }
  }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Perform a parse with debugging output.  This does exactly the
   *  same things as parse(), except that it calls debug_shift() and
   *  debug_reduce() when shift and reduce moves are taken by the parser
   *  and produces various other debugging messages.  
   */
  public Symbol debug_parse()
    throws java.lang.Exception
    {
      /* the current action code */
      int act;

      debug_message("# Initializing parser");

      /* initialize the action encapsulation object */
      init_actions();

      /* do user initialization */
      user_init();

      /* unpack action/reduce tables */
      unpackStrings(action_table());

      /* the current Symbol */
      cur_token = scan(); 

      debug_message("# Current Symbol is #" + cur_token.sym);

      /* push dummy Symbol with start state to get us underway */
      stack.clear();
      stack.add(getSymbolFactory().startSymbol("START",0, 0));
      int parse_state = 0;

      /* continue until we are told to stop */
      for (_done_parsing = false; !_done_parsing; )
	{
	  /* Check current token for freshness. */
	  if (cur_token.used_by_parser)
	    {
		throw new Error("Symbol recycling detected (fix your scanner).");
	      }

	  /* current state is always on the top of the stack */
	  //debug_stack();

	  /* look up action out of the current state with the current input */
	  act = get_action(parse_state, cur_token.sym);
	  
	  /* decode the action: odd encodes shift */
	  if ((act & 1) != 0)
	    {
	      /* shift to the encoded state by pushing it on the stack */
	      cur_token.parse_state = parse_state = (act >> 1);
	      cur_token.used_by_parser = true;
	      debug_shift(cur_token);
	      stack.add(cur_token);

	      /* advance to the next Symbol */
	      cur_token = scan();
	      debug_message("# Current token is " + cur_token);
	    }
	  /* if its even, then it encodes a reduce action */
	  else if (act != 0)
	    {
	      act = (act >> 1)-1;
	      /* perform the action for the reduce */
	      final Symbol lhs_sym = do_action(act, stack);

	      /* look up information about the production */
	      int handle_size = production_table[2*act+1];

	      debug_reduce(act, lhs_sym, handle_size);

	      /* pop the handle off the stack */
	      while (handle_size-- > 0)
		{
		    stack.remove(stack.size()-1);
		  }
	      
	      /* look up the state to go to from the one popped back to */
	      act = get_reduce(stack.get(stack.size()-1).parse_state, lhs_sym.sym);
	      debug_message("# Reduce rule: top state " +
			     stack.get(stack.size()-1).parse_state +
			     ", lhs sym " + lhs_sym.sym + " -> state " + act); 

	      /* shift to that state */
	      lhs_sym.parse_state = parse_state = act;
	      lhs_sym.used_by_parser = true;
	      stack.add(lhs_sym);

	      debug_message("# Goto state #" + act);
	    }
	  /* finally if the entry is zero, we have an error */
	  else
	    {
	      /* try to error recover */
	      error_recovery(true);
	      if (!stack.isEmpty())
		{
		    parse_state = stack.get(stack.size()-1).parse_state;
		  }
	    }
	}
      /* clean-up tables to save space */
      production_table = null;
      action_table = null;
      reduce_table = null;
      return stack.isEmpty() ? null : stack.get(stack.size()-1);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/
  /* Error recovery code */
  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Attempt to recover from a syntax error.  This returns false if recovery 
   *  fails, true if it succeeds.  Recovery happens in 4 steps.  First we
   *  pop the parse stack down to a point at which we have a shift out
   *  of the top-most state on the error Symbol.  This represents the
   *  initial error recovery configuration.  If no such configuration is
   *  found, then we fail.  Next a small number of "lookahead" or "parse
   *  ahead" Symbols are read into a buffer.  The size of this buffer is 
   *  determined by error_sync_size() and determines how many Symbols beyond
   *  the error must be matched to consider the recovery a success.  Next, 
   *  we begin to discard Symbols in attempt to get past the point of error
   *  to a point where we can continue parsing.  After each Symbol, we attempt 
   *  to "parse ahead" though the buffered lookahead Symbols.  The "parse ahead"
   *  process simulates that actual parse, but does not modify the real 
   *  parser's configuration, nor execute any actions. If we can  parse all 
   *  the stored Symbols without error, then the recovery is considered a 
   *  success.  Once a successful recovery point is determined, we do an
   *  actual parse over the stored input -- modifying the real parse 
   *  configuration and executing all actions.  Finally, we return the the 
   *  normal parser to continue with the overall parse.
   *
   * @param debug should we produce debugging messages as we parse.
   */
  private void error_recovery(boolean debug)
    throws java.lang.Exception
    {
      /* call user syntax error reporting routine */
      syntax_error(cur_token);

      if (debug)
	{
	    debug_message("# Attempting error recovery");
	  }

      /* first pop the stack back into a state that can shift on error and 
	 do that shift (if that fails, we fail) */
      if (!find_recovery_config(debug))
	{
	  if (debug)
	    {
		debug_message("# Error recovery fails");
	      }

	  /* if that fails give up with a fatal syntax error */
	  unrecovered_syntax_error(cur_token);

	  /* just in case that wasn't fatal enough, end parse */
	  done_parsing();

	  return;
	}

      /* read ahead to create lookahead we can parse multiple times */
      final Symbol[] lookaheads = new Symbol[error_sync_size()];
      lookaheads[0] = cur_token;
      for (int i = 1; i < lookaheads.length; i++)
	{
	  lookaheads[i] = scan();
	}

      /* repeatedly try to parse forward until we make it the required dist */
      for (;;)
	{
	  /* try to parse forward, if it makes it, bail out of loop */
	  if (debug)
	    {
		debug_message("# Trying to parse ahead");
	      }
	  if (try_parse_ahead(debug, lookaheads))
	    {
	      break;
	    }

	  /* if we are now at EOF, we have failed */
	  if (lookaheads[0].sym == EOF) 
	    {
	      if (debug)
		{
		    debug_message("# Error recovery fails at EOF");
		  }
	      /* if that fails give up with a fatal syntax error */
	      unrecovered_syntax_error(cur_token);

	      /* just in case that wasn't fatal enough, end parse */
	      done_parsing();

	      return;
	    }

	  /* otherwise, we consume another Symbol and try again */
	  // BUG FIX by Bruce Hutton
	  // Computer Science Department, University of Auckland,
	  // Auckland, New Zealand.
	  // It is the first token that is being consumed, not the one 
	  // we were up to parsing
	  if (debug)
	    {
		debug_message("# Consuming Symbol #" + lookaheads[ 0 ].sym);
	      }
	  
	  /* move all the existing input over */
	  for (int i = 1; i < lookaheads.length; i++)
	    {
		lookaheads[i-1] = lookaheads[i];
	      }
	  lookaheads[lookaheads.length-1] = scan();
	}

      /* we have consumed to a point where we can parse forward */
      if (debug)
	{
	    debug_message("# Parse-ahead ok, going back to normal parse");
	  }

      /* do the real parse (including actions) across the lookahead */
      parse_lookahead(debug, lookaheads);

      /* we have success */
      return;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Put the (real) parse stack into error recovery configuration by 
   *  popping the stack down to a state that can shift on the special 
   *  error Symbol, then doing the shift.  If no suitable state exists on 
   *  the stack we return false 
   *
   * @param debug should we produce debugging messages as we parse.
   */
  private boolean find_recovery_config(boolean debug)
      throws Exception
    {
      Symbol error_token;
      int act;

      if (debug)
	{
	    debug_message("# Finding recovery state on stack");
	  }

      /* Remember the right-position of the top symbol on the stack */
      final Symbol right = stack.get(stack.size()-1);	
      Symbol left  = cur_token;

      /* Now fire reduce actions that have error as lookahead;
       * and pop when the action cannot handle errors.
       */
      while (((act = get_action(stack.get(stack.size()-1).parse_state, ERROR)) & 1) == 0)
	{
	  if (act == 0)
	    {
	      /* pop the stack */
	      if (debug)
		{
		    debug_message("# Pop stack by one, state was # "
		        + stack.get(stack.size()-1).parse_state);
		  }
	      left = stack.remove(stack.size()-1);
		  
	      /* if we have hit bottom, we fail */
	      if (stack.isEmpty())
		{
		  if (debug)
		    {
			debug_message("# No recovery state found on stack");
		      }
		  return false;
		}
	    }
	  else
	    {
	      /* reduce under error symbol */
	      act = (act >> 1) - 1;
	      /* perform the action for the reduce */
	      final Symbol lhs_sym = do_action(act, stack);

	      /* look up information about the production */
	      int handle_size = production_table[2*act+1];

	      if (debug)
		{
		    debug_reduce(act, lhs_sym, handle_size);
		  }

	      /* pop the handle off the stack */
	      while (handle_size-- > 0)
		{
		    stack.remove(stack.size()-1);
		  }

	      /* look up the state to go to from the one popped back to */
	      act = get_reduce(stack.get(stack.size()-1).parse_state, lhs_sym.sym);

	      /* shift to that state */
	      lhs_sym.parse_state = act;
	      lhs_sym.used_by_parser = true;
	      stack.add(lhs_sym);

	      if (debug)
		{
		    debug_message("# Goto state #" + act);
		  }
	    }
	}

      /* state on top of the stack can shift under error */
      if (debug) 
	{
	  debug_message("# Recover state found (#" + 
			stack.get(stack.size()-1).parse_state + ")");
	  debug_message("# Shifting on error to state #" + (act-1));
	}

      /* build and shift a special error Symbol */
      error_token = getSymbolFactory().newSymbol("ERROR",1, left, right);
      error_token.parse_state = (act>>1);
      error_token.used_by_parser = true;
      stack.add(error_token);

      return true;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Do a simulated parse forward (a "parse ahead") from the current 
   *  stack configuration using stored lookahead input and a virtual parse
   *  stack.  Return true if we make it all the way through the stored 
   *  lookahead input without error. This basically simulates the action of 
   *  parse() using only our saved "parse ahead" input, and not executing any 
   *  actions.
   *
   * @param debug should we produce debugging messages as we parse.
   */
  private boolean try_parse_ahead(boolean debug, Symbol[] lookaheads)
    throws java.lang.Exception
    {
      int act;

      /* create a virtual stack from the real parse stack */
      final virtual_parse_stack vstack = new virtual_parse_stack(stack);
      int parse_state = vstack.top();
      int lookahead_pos = 0;
      cur_token = lookaheads[lookahead_pos++];

      /* parse until we fail or get past the lookahead input */
      for (;;)
	{
	  /* look up the action from the current state (on top of stack) */
	  act = get_action(parse_state, cur_token.sym);

	  /* decode the action: odd encodes shift */
	  if ((act & 1) != 0)
	    {
	      parse_state = act>>1;
      
	      /* push the new state on the stack */
	      vstack.push(parse_state);

	      if (debug)
		{
		    debug_message("# Parse-ahead shifts Symbol #" + 
		           cur_token.sym + " into state #" + parse_state);
		  }

	      /* advance simulated input, if we run off the end, we are done */
	      if (lookahead_pos == lookaheads.length)
		{
		    return true;
		  }
	      cur_token = lookaheads[lookahead_pos++];
	    }
	  /* even encodes a reduce */
	  else if (act > 0)
	    {
	      act = (act >> 1)-1;

	      /* if this is a reduce with the start production we are done */
	      if (act == 0) 
		{
		  if (debug)
		    {
			debug_message("# Parse-ahead accepts");
		      }
		  return true;
		}

	      /* get the lhs Symbol and the rhs size */
	      final int lhs = production_table[2*act];
	      final int rhs_size = production_table[2*act+1];

	      /* pop handle off the stack */
	      vstack.pop(rhs_size);

	      if (debug)
		{
		    debug_message("# Parse-ahead reduces: handle size = " + 
		      rhs_size + " lhs = #" + lhs + " from state #" + vstack.top());
		  }

	      /* look up goto and push it onto the stack */
	      parse_state = get_reduce(vstack.top(), lhs);
	      vstack.push(parse_state);
	      if (debug)
		{
		    debug_message("# Goto state #" + vstack.top());
		  }
	    }
	  /* if its an error, we fail */ else
	    {
		return false;
	      }
	}
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Parse forward using stored lookahead Symbols.  In this case we have
   *  already verified that parsing will make it through the stored lookahead
   *  Symbols and we are now getting back to the point at which we can hand
   *  control back to the normal parser.  Consequently, this version of the
   *  parser performs all actions and modifies the real parse configuration.  
   *  This returns once we have consumed all the stored input or we accept.
   *
   * @param debug should we produce debugging messages as we parse.
   */
  private void parse_lookahead(boolean debug, Symbol[] lookaheads)
    throws java.lang.Exception
    {
      /* the current action code */
      int act;

      /* the Symbol/stack element returned by a reduce */
      Symbol lhs_sym = null;

      /* restart the saved input at the beginning */
      int lookahead_pos = 0;
      cur_token = lookaheads[lookahead_pos++];

      if (debug) 
	{
	  debug_message("# Reparsing saved input with actions");
	  debug_message("# Current Symbol is #" + cur_token.sym);
	  debug_message("# Current state is #" + 
			stack.get(stack.size()-1).parse_state);
	}

      /* continue until we accept or have read all lookahead input */
      while(!_done_parsing && lookahead_pos < lookaheads.length)
	{
	  /* current state is always on the top of the stack */

	  /* look up action out of the current state with the current input */
	  act = 
	    get_action(stack.get(stack.size()-1).parse_state, cur_token.sym);

	  /* decode the action: even encodes shift */
	  if ((act & 1) != 0)
	    {
	      /* shift to the encoded state by pushing it on the stack */
	      cur_token.parse_state = (act>>1);
	      cur_token.used_by_parser = true;
	      if (debug)
		{
		    debug_shift(cur_token);
		  }
	      stack.add(cur_token);

	      /* advance to the next Symbol */
	      cur_token = lookaheads[lookahead_pos++];

	      if (debug)
		{
		    debug_message("# Current Symbol is #" + cur_token.sym);
		  }
	    }
	  /* if its even, then it encodes a reduce action */
	  else
	    {
	      /* The action cannot be error, since try_parse_ahead succeeded */
	      act = (act >> 1) - 1;
	      /* perform the action for the reduce */
	      lhs_sym = do_action(act, stack);

	      /* look up information about the production */
	      int handle_size = production_table[2*act+1];

	      if (debug)
		{
		    debug_reduce(act, lhs_sym, handle_size);
		  }

	      /* pop the handle off the stack */
	      while (handle_size-- > 0)
		{
		    stack.remove(stack.size()-1);
		  }
	      
	      /* look up the state to go to from the one popped back to */
	      act = get_reduce(stack.get(stack.size()-1).parse_state, lhs_sym.sym);

	      /* shift to that state */
	      lhs_sym.parse_state = act;
	      lhs_sym.used_by_parser = true;
	      stack.add(lhs_sym);
	       
	      if (debug)
		{
		    debug_message("# Goto state #" + act);
		  }

	    }
	}

      if (debug)
	{
	    debug_message("# Completed reparse");
	  }
      /* go back to normal parser */
      return;
    }

  /*-----------------------------------------------------------*/

  /** Utility function: unpacks parse tables from strings */
  private void unpackStrings(String[] sa)
    {
      final TableDecoder decoder = new TableDecoder(sa);
      production_table = decoder.decodeShortArray();
      base_table = decoder.decodeIntArray();
      action_table = decoder.decodeShortArray();
      reduce_table = decoder.decodeShortArray();
    }
}

