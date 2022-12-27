package de.janno.evaluator.dice;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenizerTest {

    Operator d = new Operator("d", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, 1) {

        @Override
        public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) {
            return constants -> ImmutableList.of(new Roll(operands.toString(), ImmutableList.of(new RollElement("dice", RollElement.NO_COLOR)), UniqueRandomElements.empty(), ImmutableList.of()));
        }
    };
    Operator plus = new Operator("+", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, 2) {
        @Override
        public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) {
            return constants -> ImmutableList.of(new Roll(operands.toString(), ImmutableList.of(new RollElement("plus", RollElement.NO_COLOR)), UniqueRandomElements.empty(), ImmutableList.of()));
        }
    };
    Operator aRightLeft = new Operator("a", Operator.Associativity.RIGHT, 1, Operator.Associativity.LEFT, 1) {
        @Override
        public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) {
            return constants -> ImmutableList.of(new Roll(operands.toString(), ImmutableList.of(new RollElement("a", RollElement.NO_COLOR)), UniqueRandomElements.empty(), ImmutableList.of()));
        }
    };

    Operator aLeft = new Operator("a", Operator.OperatorType.UNARY, Operator.Associativity.LEFT, 1) {
        @Override
        public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) {
            return constants -> ImmutableList.of(new Roll(operands.toString(), ImmutableList.of(new RollElement("a", RollElement.NO_COLOR)), UniqueRandomElements.empty(), ImmutableList.of()));

        }
    };

    Operator aRight = new Operator("a", Operator.OperatorType.UNARY, Operator.Associativity.RIGHT, 1) {
        @Override
        public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) {
            return constants -> ImmutableList.of(new Roll(operands.toString(), ImmutableList.of(new RollElement("a", RollElement.NO_COLOR)), UniqueRandomElements.empty(), ImmutableList.of()));
        }
    };


    @Test
    void escapeTest1() throws ExpressionException {


        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(d)
                .operator(plus)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token> res = underTest.tokenize("1d5+6+[1d2]");
        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "d", "'5'", "+", "'6'", "+", "'1d2'");
    }

    @Test
    void escapeTest2() throws ExpressionException {

        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(d)
                .operator(plus)
                .escapeBracket(BracketPair.APOSTROPHE)
                .build());

        List<Token> res = underTest.tokenize("'a'+'dd'+'dd'");
        assertThat(res.stream().map(Token::toString)).containsExactly("'a'", "+", "'dd'", "+", "'dd'");
    }

    @Test
    void escapeTest3() throws ExpressionException {
        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(d)
                .operator(plus)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token> res = underTest.tokenize("[1d2]");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1d2'");
    }

    @Test
    void trim() throws ExpressionException {

        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(d)
                .operator(plus)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token> res = underTest.tokenize(" 1 d 6 + 3 ");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "d", "'6'", "+", "'3'");
    }

    @Test
    void validate() {
        Operator d = new Operator("d", Operator.OperatorType.BINARY, Operator.Associativity.LEFT, 20) {
            @Override
            public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) {
                return constants -> ImmutableList.of(new Roll(operands.toString(), ImmutableList.of(new RollElement("null", RollElement.NO_COLOR)), UniqueRandomElements.empty(), ImmutableList.of()));
            }
        };
        Operator d2 = new Operator("d", Operator.OperatorType.UNARY, Operator.Associativity.LEFT, 15) {
            @Override
            public @NonNull RollBuilder evaluate(@NonNull List<RollBuilder> operands) {
                return constants -> ImmutableList.of(new Roll(operands.toString(), ImmutableList.of(new RollElement("null", RollElement.NO_COLOR)), UniqueRandomElements.empty(), ImmutableList.of()));
            }
        };

        assertThatThrownBy(() -> new Tokenizer(Parameters.builder()
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
        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(d)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token> res = underTest.tokenize("1d[head/ torso/ left arm/ right arm/ left leg/ right leg]");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "d", "'head/ torso/ left arm/ right arm/ left leg/ right leg'");
    }

    @Test
    void operatorAssociativity_middle() throws ExpressionException {
        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(aRightLeft)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token> res = underTest.tokenize("1a2");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "a", "'2'");
    }

    @Test
    void operatorAssociativity_left() throws ExpressionException {

        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(aLeft)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token> res = underTest.tokenize("1a");

        assertThat(res.stream().map(Token::toString)).containsExactly("'1'", "a");
    }

    @Test
    void operatorAssociativity_right() throws ExpressionException {

        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(aRight)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        List<Token> res = underTest.tokenize("a2");

        assertThat(res.stream().map(Token::toString)).containsExactly("a", "'2'");
    }

    @Test
    void operatorAssociativity_invalid() {

        Tokenizer underTest = new Tokenizer(Parameters.builder()
                .operator(aRight)
                .escapeBracket(BracketPair.BRACKETS)
                .build());

        assertThatThrownBy(() -> underTest.tokenize("1a"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("Operator a has right associativity but the right value was: empty");
    }
}