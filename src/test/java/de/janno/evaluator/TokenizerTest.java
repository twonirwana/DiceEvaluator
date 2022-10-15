package de.janno.evaluator;

import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenizerTest {
    Operator<String> d = new Operator<>("d", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, 1) {

        @Override
        protected @NonNull String evaluate(@NonNull List<String> operands) {
            return "dice";
        }
    };
    Operator<String> plus = new Operator<>("+", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, 2) {
        @Override
        protected @NonNull String evaluate(@NonNull List<String> operands) {
            return "plus";
        }
    };
    Operator<String> aRightLeft = new Operator<>("a", Operator.Associativity.RIGHT, 1, Operator.Associativity.LEFT, 1) {
        @Override
        protected @NonNull String evaluate(@NonNull List<String> operands) {
            return "a";
        }
    };

    Operator<String> aLeft = new Operator<>("a", Operator.OperatorType.UNARY, Operator.Associativity.LEFT, 1) {
        @Override
        protected @NonNull String evaluate(@NonNull List<String> operands) {
            return "a";
        }
    };

    Operator<String> aRight = new Operator<>("a", Operator.OperatorType.UNARY, Operator.Associativity.RIGHT, 1) {
        @Override
        protected @NonNull String evaluate(@NonNull List<String> operands) {
            return "a";
        }
    };


    @Test
    void escapeTest1() throws ExpressionException {


        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(d)
                .operator(plus)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token<String>> res = underTest.tokenize("1d5+6+[1d2]");
        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "d", "'5'", "+", "'6'", "+", "'1d2'");
    }

    @Test
    void escapeTest2() throws ExpressionException {

        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(d)
                .operator(plus)
                .escapeBracket(BracketPair.APOSTROPHE)
                .build());

        List<Token<String>> res = underTest.tokenize("a+'dd'+'dd'");
        assertThat(res.stream().map(Token::toString)).containsExactly("'a'", "+", "'dd'", "+", "'dd'");
    }

    @Test
    void escapeTest3() throws ExpressionException {
        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(d)
                .operator(plus)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token<String>> res = underTest.tokenize("[1d2]");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1d2'");
    }

    @Test
    void trim() throws ExpressionException {

        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(d)
                .operator(plus)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token<String>> res = underTest.tokenize(" 1 d 6 + 3 ");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "d", "'6'", "+", "'3'");
    }

    @Test
    void validate() {
        Operator<String> d = new Operator<>("d", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, 20) {
            @Override
            protected @NonNull String evaluate(@NonNull List<String> operands) {
                return "null";
            }
        };
        Operator<String> d2 = new Operator<>("d", Operator.OperatorType.UNARY, Operator.Associativity.LEFT, 15) {
            @Override
            protected @NonNull String evaluate(@NonNull List<String> operands) {
                return "null";
            }
        };

        assertThatThrownBy(() -> new Tokenizer<>(Parameters.<String>builder()
                .operator(d)
                .operator(d2)
                .expressionBracket(BracketPair.PARENTHESES)
                .functionBracket(BracketPair.PARENTHESES)
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The following regex for tokenizing where used more then once: [\\Qd\\E]");

    }

    @Test
    void escape() throws ExpressionException {
        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(d)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token<String>> res = underTest.tokenize("1d[head/ torso/ left arm/ right arm/ left leg/ right leg]");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "d", "'head/ torso/ left arm/ right arm/ left leg/ right leg'");
    }

    @Test
    void operatorAssociativity_middle() throws ExpressionException {
        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(aRightLeft)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token<String>> res = underTest.tokenize("1a2");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "a", "'2'");
    }

    @Test
    void operatorAssociativity_left() throws ExpressionException {

        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(aLeft)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token<String>> res = underTest.tokenize("1a");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "a");
    }

    @Test
    void operatorAssociativity_right() throws ExpressionException {

        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(aRight)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token<String>> res = underTest.tokenize("a2");

        assertThat(res.stream().map(Token::toString)).containsExactly("a", "'2'");
    }

    @Test
    void operatorAssociativity_invalid() {

        Tokenizer<String> underTest = new Tokenizer<>(Parameters.<String>builder()
                .operator(aRight)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        assertThatThrownBy(() -> underTest.tokenize("1a"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("Operator a has right associativity but the right value was: empty");
    }
}