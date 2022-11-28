package de.janno.evaluator.dice;

import lombok.Value;

@Value
public class BracketPair {
    public static final BracketPair PARENTHESES = new BracketPair("(", ")");
    public static final BracketPair BRACKETS = new BracketPair("[", "]");
    public static final BracketPair BRACES = new BracketPair("{", "}");
    public static final BracketPair ANGLES = new BracketPair("<", ">");
    public static final BracketPair APOSTROPHE = new BracketPair("'", "'");

    String open;
    String close;

    public String toString() {
        return open + close;
    }
}
