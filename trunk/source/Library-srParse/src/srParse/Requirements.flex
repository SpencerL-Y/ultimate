/* Boogie 2 lexer */
package srParse;
import com.github.jhoenicke.javacup.runtime.Symbol;

/**
 * This is a autogenerated lexer for Boogie 2.
 * It is generated from Boogie.flex by JFlex.
 */
%%

%class ReqLexer
%unicode
%implements com.github.jhoenicke.javacup.runtime.Scanner
%type com.github.jhoenicke.javacup.runtime.Symbol
%function next_token
%line
%column
%ignorecase
%public

%{
  private MySymbolFactory symFactory = new MySymbolFactory();
  
  private Symbol symbol(int type) {
    return symFactory.newSymbol(yytext(), type, yyline+1, yycolumn, yyline+1, yycolumn+yylength());
  }
  private Symbol symbol(int type, String value) {
    return symFactory.newSymbol(value, type, yyline+1, yycolumn, yyline+1, yycolumn+yylength(), value);
  }
  private Symbol number(String value) {
    Integer i = Integer.valueOf(value);
    return symFactory.newSymbol(yytext(), ReqSymbols.NUMBER, yyline+1, yycolumn, yyline+1, yycolumn+yylength(), i);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment   = "/*" ~"*/" 
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}?
BoogieLetter = [:letter:] | ['~#$\^_?\\]
BoogieLetterDigit = {BoogieLetter} | [:digit:]
Identifier = {BoogieLetter} {BoogieLetterDigit}*

DecIntegerLiteral = 0 | [1-9][0-9]*
RealIntegerLiteral = {DecIntegerLiteral} "." [0-9]+

%%

<YYINITIAL>  {
   "execution"      { return symbol(ReqSymbols.EXECUTION); }
   "initially"      { return symbol(ReqSymbols.INITIALLY); }
   "Input"      	{ return symbol(ReqSymbols.INPUT); }
   "Output"      	{ return symbol(ReqSymbols.OUTPUT); }
   "Internal"     	 { return symbol(ReqSymbols.INTERNAL); }
   "one"            { return symbol(ReqSymbols.ONE); }
   "sequence"       { return symbol(ReqSymbols.SEQUENCE); }
   "such"           { return symbol(ReqSymbols.SUCH); }
   "there"          { return symbol(ReqSymbols.THERE); }
   "after"          { return symbol(ReqSymbols.AFTER); }
   "always"         { return symbol(ReqSymbols.ALWAYS); }
   "and"            { return symbol(ReqSymbols.AND); }
   "as"             { return symbol(ReqSymbols.AS); }
   "at"             { return symbol(ReqSymbols.AT); }
   "becomes"        { return symbol(ReqSymbols.BECOMES); }
   "before"         { return symbol(ReqSymbols.BEFORE); }
   "between"        { return symbol(ReqSymbols.BETWEEN); }
   "by"             { return symbol(ReqSymbols.BY); }
   "case"           { return symbol(ReqSymbols.CASE); }
   "does"           { return symbol(ReqSymbols.DOES); }
   "eventually"     { return symbol(ReqSymbols.EVENTUALLY); }
   "every"          { return symbol(ReqSymbols.EVERY); }
   "for"            { return symbol(ReqSymbols.FOR); }
   "globally"       { return symbol(ReqSymbols.GLOBALLY); }
   "held"           { return symbol(ReqSymbols.HELD); }
   "hold"           { return symbol(ReqSymbols.HOLD); }
   "holds"          { return symbol(ReqSymbols.HOLDS); }
   "if"             { return symbol(ReqSymbols.IF); }
   "in"             { return symbol(ReqSymbols.IN); }
   "is"             { return symbol(ReqSymbols.IS); }
   "it"             { return symbol(ReqSymbols.IT); }
   "least"          { return symbol(ReqSymbols.LEAST); }
   "initially"      { return symbol(ReqSymbols.INITIALLY); }
   "less"           { return symbol(ReqSymbols.LESS); }
   "most"           { return symbol(ReqSymbols.MOST); }
   "msec"           { return symbol(ReqSymbols.MSEC); }
   "ms"             { return symbol(ReqSymbols.MSEC); }
   "never"          { return symbol(ReqSymbols.NEVER); }
   "not"            { return symbol(ReqSymbols.NOT); }
   "occur"          { return symbol(ReqSymbols.OCCUR); }
   "once"           { return symbol(ReqSymbols.ONCE); }
   "preceded"       { return symbol(ReqSymbols.PRECEDED); }
   "previously"     { return symbol(ReqSymbols.PREVIOUSLY); }
   "satisfied"      { return symbol(ReqSymbols.SATISFIED); }
   "sec"            { return symbol(ReqSymbols.SEC); }
   "states"         { return symbol(ReqSymbols.STATES); }
   "succeeded"      { return symbol(ReqSymbols.SUCCEEDED); }
   "than"           { return symbol(ReqSymbols.THAN); }
   "that"           { return symbol(ReqSymbols.THAT); }
   "the"            { return symbol(ReqSymbols.THE); }
   "then"           { return symbol(ReqSymbols.THEN); }
   "time"           { return symbol(ReqSymbols.TIME); }
   "to"             { return symbol(ReqSymbols.TO); }
   "transitions"    { return symbol(ReqSymbols.TRANSITIONS); }
   "twice"          { return symbol(ReqSymbols.TWICE); }
   "units"          { return symbol(ReqSymbols.UNITS); }
   "until"          { return symbol(ReqSymbols.UNTIL); }
   "usec"           { return symbol(ReqSymbols.USEC); }
   "us"             { return symbol(ReqSymbols.USEC); }
   "µsec"           { return symbol(ReqSymbols.USEC); }
   "µs"             { return symbol(ReqSymbols.USEC); }
   "was"            { return symbol(ReqSymbols.WAS); }
   "well"           { return symbol(ReqSymbols.WELL); }
   "where"          { return symbol(ReqSymbols.WHERE); }
   "which"          { return symbol(ReqSymbols.WHICH); }

  /* Other Symbols */
  "("             { return symbol(ReqSymbols.LPAR); }
  ")"             { return symbol(ReqSymbols.RPAR); }
  ","             { return symbol(ReqSymbols.COMMA); }
  "."             { return symbol(ReqSymbols.DOT); }
  
  "true"             { return symbol(ReqSymbols.TRUE); }
  "false"             { return symbol(ReqSymbols.FALSE); }
  
  "<"             { return symbol(ReqSymbols.LE); }
  ">"             { return symbol(ReqSymbols.GREATER); }
  "<="            { return symbol(ReqSymbols.LTEQ); }
  "\u2264"        { return symbol(ReqSymbols.LTEQ); }
  ">="            { return symbol(ReqSymbols.GTEQ); }
  "\u2265"        { return symbol(ReqSymbols.GTEQ); }
  "!="            { return symbol(ReqSymbols.NEQ); }
  "\u2260"        { return symbol(ReqSymbols.NEQ); }
  "=="            { return symbol(ReqSymbols.EQ); }
  
  "+"             { return symbol(ReqSymbols.PLUS); }
  "-"             { return symbol(ReqSymbols.MINUS); }
  "*"             { return symbol(ReqSymbols.TIMES); }
  "/"             { return symbol(ReqSymbols.DIVIDE); }
  "%"             { return symbol(ReqSymbols.MOD); }
  
  "!"             { return symbol(ReqSymbols.LNOT); }
  "\u00ac"        { return symbol(ReqSymbols.LNOT); }
  "&&"            { return symbol(ReqSymbols.LAND); }
  "\u2227"        { return symbol(ReqSymbols.LAND); }
  "||"            { return symbol(ReqSymbols.LOR); }
  "\u2228"        { return symbol(ReqSymbols.LOR); }

  "\""            { return symbol(ReqSymbols.QUOTE); }

  /* Numbers, Ids and Strings */

  /* identifiers */ 
  {Identifier}                   { return symbol(ReqSymbols.ID, yytext().intern()); }
 
  /* literals */
  {DecIntegerLiteral}            { return symbol(ReqSymbols.NUMBER, yytext().intern()); }
  {RealIntegerLiteral}           { return symbol(ReqSymbols.REALNUMBER, yytext().intern()); }
  
  /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}


/* error fallback */
.|\n                             { return symbol(ReqSymbols.error, yytext()); }

<<EOF>>                          { return symbol(ReqSymbols.EOF); }