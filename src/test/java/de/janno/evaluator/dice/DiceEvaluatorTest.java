package de.janno.evaluator.dice;

import de.janno.evaluator.dice.random.GivenDiceNumberSupplier;
import de.janno.evaluator.dice.random.GivenNumberSupplier;
import de.janno.evaluator.dice.random.RandomNumberSupplier;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DiceEvaluatorTest {
    private static final String VAMPIRE_V5 = "val('$r',3d10) val('$h',3d10) val('$s',('$r'+'$h')>=6c) val('$rt','$r'==10c) val('$ht','$h'==10c) val('$ho','$h'==1c) val('$2s',((('$rt'+'$ht'=))/2)*2) val('$ts',('$s'+'$2s'=)) concat('successes: ', '$ts', ifE('$ts',0,ifG('$ho',1,' bestial failure' , ''),''), ifE('$rt' mod 2, 1, ifE('$ht' mod 2, 1, ' messy critical', ''), ''))";
    private static final String THE_ONE_RING = "val('$w', 0) val('$f',   replace(1d12,11, 0,12,100) ) val('$s',  0d6 ) val('$SR',  0 ) if('$w'=?1, val('$s', '$s'>=4)) val('$t', ('$s'>5)c) val('$total', '$f' + '$s'=)  concat (if('$f'=?100||'$f'=?-666, '', '$f'=?200, '  =  '_'$s'=_'  =', '  =  '_'$total'_'  ='), if('$f'=?200||'$SR'=?0, '', if('$total'>=?'$SR', ' +++ SUCCESS ! +++', ' --- FAILURE ! ---')), if('$f'=?200, '', concat(' ⬟= ', if('$f'=?0||'$f'=?-666, '\uD83D\uDC41', '$f'=?100, 'ᚠ', '$f'))), if('$total'>=?'$SR'&&'$t'>?0, ' ✶successes= '_'$t', ''))";

    private static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("1d6", List.of(3), List.of(3)),
                Arguments.of("1 d 6", List.of(3), List.of(3)),
                Arguments.of("1d6 ", List.of(3), List.of(3)),
                Arguments.of(" 1d6", List.of(3), List.of(3)),
                Arguments.of("1D6", List.of(3), List.of(3)),
                Arguments.of("D6", List.of(3), List.of(3)),
                Arguments.of("d6", List.of(3), List.of(3)),
                Arguments.of("-d6", List.of(3), List.of(-3)),
                Arguments.of("-2d6", List.of(3, 4), List.of(-3, -4)),
                Arguments.of("0d6", List.of(), List.of()),
                Arguments.of("3d6", List.of(3, 4, 5), List.of(3, 4, 5)),
                Arguments.of("3d6c", List.of(3, 4, 5), List.of(3)),
                Arguments.of("3d!6c", List.of(3, 2, 6, 6, 5), List.of(5)),
                Arguments.of("3d6>3", List.of(3, 4, 5), List.of(4, 5)),
                Arguments.of("3d6+6>3", List.of(3, 4, 5), List.of(4, 5, 6)),
                Arguments.of("3d6>3c", List.of(3, 4, 5), List.of(2)),
                Arguments.of("3d6>=3", List.of(1, 3, 4), List.of(3, 4)),
                Arguments.of("3d6>=3c", List.of(1, 3, 4), List.of(2)),
                Arguments.of("3d6<3", List.of(3, 4, 5), List.of()),
                Arguments.of("3d6<3c", List.of(3, 4, 5), List.of(0)),
                Arguments.of("3d6<=3", List.of(1, 3, 4), List.of(1, 3)),
                Arguments.of("3d6<=3c", List.of(1, 3, 4), List.of(2)),
                Arguments.of("3d6==3", List.of(1, 3, 4), List.of(3)),
                Arguments.of("3d6==3c", List.of(1, 3, 4), List.of(1)),
                Arguments.of("1d6+1d4 + 1d5", List.of(3, 4, 5), List.of(3, 4, 5)),
                Arguments.of("1d6+1d4 - 1d5", List.of(3, 4, 5), List.of(3, 4, -5)),
                Arguments.of("1d6 + 3", List.of(3), List.of(3, 3)),
                Arguments.of("3d6 + 2d8", List.of(6, 6, 6, 8, 8), List.of(6, 6, 6, 8, 8)),
                Arguments.of("3d6 - 2d8", List.of(6, 6, 6, 8, 8), List.of(6, 6, 6, -8, -8)),
                Arguments.of("1d6 + 3=", List.of(3), List.of(6)),
                Arguments.of("1d(6 + 3=)", List.of(), List.of(9)),
                Arguments.of("1d(6 + 3)", List.of(), List.of(3)),
                Arguments.of("1d6 * 3", List.of(3), List.of(9)),
                Arguments.of("(1d6 * 3) -3", List.of(3), List.of(9, -3)),
                Arguments.of("1d6 * (3 -3=)", List.of(3), List.of(0)),
                Arguments.of("1d6 - 2", List.of(3), List.of(3, -2)),
                Arguments.of("1d6 / 2", List.of(4), List.of(2)),
                Arguments.of("2d6k1", List.of(3, 4), List.of(4)),
                Arguments.of("2d6k0", List.of(3, 4), List.of()),
                Arguments.of("2d6k3", List.of(3, 4), List.of(4, 3)),
                Arguments.of("2d6l1", List.of(3, 4), List.of(3)),
                Arguments.of("2d6l0", List.of(3, 4), List.of()),
                Arguments.of("2d6l3", List.of(3, 4), List.of(3, 4)),
                Arguments.of("(2d6l1)+2", List.of(3, 4), List.of(3, 2)),
                Arguments.of("d!6", List.of(6, 6, 4), List.of(6, 6, 4)),
                Arguments.of("D!6", List.of(6, 6, 4), List.of(6, 6, 4)),
                Arguments.of("2d!6", List.of(3, 6, 6, 4), List.of(3, 6, 6, 4)),
                Arguments.of("2D!6", List.of(3, 6, 6, 4), List.of(3, 6, 6, 4)),
                Arguments.of("2d!6 + 1d6", List.of(3, 6, 6, 4, 1), List.of(3, 6, 6, 4, 1)),
                Arguments.of("d!!6", List.of(6, 6, 4), List.of(16)),
                Arguments.of("D!!6", List.of(6, 6, 4), List.of(16)),
                Arguments.of("2d!!6", List.of(3, 6, 6, 4), List.of(3, 16)),
                Arguments.of("2D!!6", List.of(3, 6, 6, 4), List.of(3, 16)),
                Arguments.of("2d!!6 + 1d6", List.of(3, 6, 6, 4, 1), List.of(3, 16, 1)),
                Arguments.of("min(2d6= , 3d10=)", List.of(3, 6, 6, 4, 1), List.of(9)),
                Arguments.of("max(2d6= , 3d10=)", List.of(3, 6, 6, 4, 1), List.of(11)),
                Arguments.of("min(2d6=,11)", List.of(3, 6), List.of(9)),
                Arguments.of("max(2d6=,11)", List.of(3, 6), List.of(11)),
                Arguments.of("max(11,11)", List.of(), List.of(11, 11)),
                Arguments.of("min(11,11)", List.of(), List.of(11, 11)),
                Arguments.of("[1/2/3]", List.of(), List.of(1, 2, 3)),
                Arguments.of("[1,2,3]", List.of(), List.of(1, 2, 3)),
                Arguments.of("[1,2/3]", List.of(), List.of(1, 2, 3)),
                Arguments.of("d[1/2/3]", List.of(2), List.of(2)),
                Arguments.of("2d[1/2/3]", List.of(2, 1), List.of(2, 1)),
                Arguments.of("10d100", List.of(), List.of(100, 100, 100, 100, 100, 100, 100, 100, 100, 100)),
                Arguments.of("2+-3", List.of(), List.of(2, -3)),
                Arguments.of("6 / 3", List.of(), List.of(2)),
                Arguments.of("-1", List.of(), List.of(-1)),
                Arguments.of("3-1", List.of(), List.of(3, -1)),
                Arguments.of("min((max(((10-6=)*2),7,(15))),19+min(1,5)=)", List.of(), List.of(15)),
                Arguments.of("6 (5)", List.of(), List.of(6, 5)),
                Arguments.of("(1d6) (1d6)", List.of(3, 4), List.of(3, 4)),
                Arguments.of("min(1,min(3+2,2))+-(max(4)*2)", List.of(), List.of(1, -8)),
                Arguments.of("min(10,15),20", List.of(), List.of(10, 20)),
                Arguments.of("3+2+1+1=", List.of(), List.of(7)),
                Arguments.of("cancel(9+10+1+2,10,1)", List.of(), List.of(9, 2)),
                Arguments.of("cancel(9+10+1+2+1,10,1)", List.of(), List.of(9, 2, 1)),
                Arguments.of("cancel(9+10+1+2+10,10,1)", List.of(), List.of(9, 2, 10)),
                Arguments.of("double(9+10+1,10)", List.of(), List.of(9, 10, 10, 1)),
                Arguments.of("double(9+10+1,10+9)", List.of(), List.of(9, 9, 10, 10, 1)),
                Arguments.of("double(9+10+1,10+9+9)", List.of(), List.of(9, 9, 10, 10, 1)),
                Arguments.of("+6+6", List.of(), List.of(6, 6)),
                Arguments.of("+6*6", List.of(), List.of(36)),
                Arguments.of("+6*+6", List.of(), List.of(36)),
                Arguments.of("+6*-6", List.of(), List.of(-36)),
                Arguments.of("5 mod 2", List.of(), List.of(1)),
                Arguments.of("5 mod 3", List.of(), List.of(2)),
                Arguments.of("5 mod 1", List.of(), List.of(0)),
                Arguments.of("5 mod 7", List.of(), List.of(5)),
                Arguments.of("-5 mod 2", List.of(), List.of(-1)),
                Arguments.of("5 mod -2", List.of(), List.of(1)),
                Arguments.of("0 mod 4", List.of(), List.of(0)),

                //val
                Arguments.of("val('$1', 3d6) '$1'", List.of(1, 2, 3), List.of(1, 2, 3)),
                Arguments.of("val('$1', 3d6) '$1' + '$1'", List.of(1, 2, 3), List.of(1, 2, 3, 1, 2, 3)),
                Arguments.of("val('$1', 3d6) '$1', '$1'", List.of(1, 2, 3), List.of(1, 2, 3, 1, 2, 3)),
                Arguments.of("val('$1', 3d6) '$1'= , '$1'c", List.of(1, 2, 3), List.of(6, 3)),
                Arguments.of("val('$1', 3d6) '$1'= , ('$1'>2)c", List.of(1, 2, 3), List.of(6, 1)),
                Arguments.of("val('$1', val('$2', 3d6) + 7) '$1' , '$2'", List.of(1, 2, 3), List.of(7, 1, 2, 3)),
                Arguments.of("val('$1', 3d6) val('$2', 7) '$1' , '$2'", List.of(1, 2, 3), List.of(1, 2, 3, 7)),
                Arguments.of("val('$1', '$2') val('$2', '$1') '$1' , '$2'", List.of(1, 2, 3), List.of()),
                Arguments.of("val('$1', '$1') '$1'", List.of(1, 2, 3), List.of()),
                Arguments.of("val('$1', 1d6) 2d6", List.of(1, 2, 3), List.of(2, 3)),
                Arguments.of("val('$1',6d6), '$1'=, ('$1'>4)c", List.of(1, 2, 3, 4, 5, 6), List.of(21, 2)),
                Arguments.of("val('$1', 2d6) val('$1', 1d6) '$1'", List.of(1, 2, 3), List.of(3)),
                Arguments.of("val(2, 'abc'),d6", List.of(2), List.of(2)), //the replacement happens only in the formular, not in results
                Arguments.of("4 + val('a',3) 'a'", List.of(), List.of(4, 3)),
                Arguments.of("val('a',3) 4 + 'a'", List.of(), List.of(4, 3)),
                Arguments.of("val('a',3) val('a',4) 'a'", List.of(), List.of(4)),
                Arguments.of("val('a',1d6) val('a',2d10) 'a'", List.of(1, 7, 8), List.of(7, 8)),
                //if val
                Arguments.of("val('$1', '0') if('true', val('$1', '1')) '$1'", List.of(), List.of(1)),
                Arguments.of("val('$1', '0') if('false', val('$1', '1')) '$1'", List.of(), List.of(0)),
                Arguments.of("val('$1', '0') if('true', val('$1', '1'), val('$1', '2')) '$1'", List.of(), List.of(1)),
                Arguments.of("val('$1', '0') if('false', val('$1', '1'), val('$1', '2')) '$1'", List.of(), List.of(2)),
                Arguments.of("val('$1', '0') if('true', val('$1', '1'),'true', val('$1', '2')) '$1'", List.of(), List.of(1)),
                Arguments.of("val('$1', '0') if('false', val('$1', '1'),'true', val('$1', '2')) '$1'", List.of(), List.of(2)),
                Arguments.of("val('$1', '0') if('false', val('$1', '1'),'false', val('$1', '2')) '$1'", List.of(), List.of(0)),
                Arguments.of("val('$1', '0') if('false', val('$1', '1'),'true', val('$1', '2'), val('$1', '3')) '$1'", List.of(), List.of(2)),
                Arguments.of("val('$1', '0') if('false', val('$1', '1'),'false', val('$1', '2'), val('$1', '3')) '$1'", List.of(), List.of(3)),


                //repeat list
                Arguments.of("1rd4", List.of(3), List.of(3)),
                Arguments.of("2rd4", List.of(3, 2), List.of(3, 2)),
                Arguments.of("0rd4", List.of(), List.of()),

                //explode with
                Arguments.of("exp(d6,6,1)", List.of(1), List.of(1)),
                Arguments.of("exp(d6,6,1)", List.of(6, 6, 6), List.of(6, 6)),
                Arguments.of("exp(d6,6,0)", List.of(6, 6, 6), List.of(6)),
                Arguments.of("exp(d6,6,2)", List.of(6, 6, 6, 6), List.of(6, 6, 6)),
                Arguments.of("exp(d6,6,2)", List.of(6, 5), List.of(6, 5)),
                Arguments.of("exp(d[1/2/3],3,2)", List.of(3, 3, 2), List.of(3, 3, 2)),
                Arguments.of("exp(2d6,6,2)", List.of(6, 5, 3, 4), List.of(6, 5, 3, 4)),
                Arguments.of("exp(d6+d6,6,2)", List.of(6, 5, 3, 4), List.of(6, 5, 3, 4)),
                Arguments.of("exp(d6+d6,6,2)", List.of(6, 5, 5, 6, 1, 2), List.of(6, 5, 5, 6, 1, 2)),
                Arguments.of("exp(d6,6)", List.of(6, 5), List.of(6, 5)),
                Arguments.of("exp(d6,6)=", List.of(), List.of(606)),
                Arguments.of("exp(d6,[5/6],2)", List.of(6, 5, 6, 5), List.of(6, 5, 6)),

                Arguments.of("6r replace(exp(d[0/0/1/1/'2#'/2],2),'2#','2')", List.of(1, 2, 3, 4, 5, 6, 6, 1), List.of(0, 0, 1, 1, 2, 2, 0)),


                //empty
                Arguments.of("", null, List.of())

        );
    }

    private static List<String> values(List<Roll> in) {
        return in.stream()
                .flatMap(r -> r.getElements().stream())
                .map(RollElement::toString)
                .toList();
    }

    private static Stream<Arguments> generateStringDiceData() {
        return Stream.of(
                //custom dice
                Arguments.of("d[head/ torso/ left arm/ right arm/ left leg/ right leg]", List.of(3), List.of("left arm")),
                Arguments.of("d[head]", List.of(1), List.of("head")),
                Arguments.of("3d[head/ torso/ left arm/ right arm/ left leg/ right leg]", List.of(3, 2, 1), List.of("left arm", "torso", "head")),
                Arguments.of("3d[head/ torso/ left arm/ right arm/ left leg/ right leg] + 2d6", List.of(3, 2, 1, 4, 5), List.of("left arm", "torso", "head", "4", "5")),
                Arguments.of("3d[10/20/30] + 2d6", List.of(3, 2, 1, 4, 5), List.of("30", "20", "10", "4", "5")),
                Arguments.of("d('head'+'torso'+'left arm'+'right arm'+'left leg'+'right leg')", List.of(3), List.of("left arm")),
                Arguments.of("d('head')", List.of(1), List.of("head")),
                Arguments.of("3d('head'+'torso'+'left arm'+'right arm'+'left leg'+'right leg')", List.of(3, 2, 1), List.of("left arm", "torso", "head")),
                Arguments.of("3d('head'+'torso'+'left arm'+'right arm'+'left leg'+'right leg') + 2d6", List.of(3, 2, 1, 4, 5), List.of("left arm", "torso", "head", "4", "5")),
                Arguments.of("3d(10+20+30) + 2d6", List.of(3, 2, 1, 4, 5), List.of("30", "20", "10", "4", "5")),
                Arguments.of("+d[a/b/c]", List.of(2), List.of("b")),
                Arguments.of("+1d[a/b/c]", List.of(2), List.of("b")),
                Arguments.of("1d[a/b/c]", List.of(2), List.of("b")),

                //sum
                Arguments.of("[1/2]=", List.of(), List.of("3")),
                Arguments.of("[1/-2]=", List.of(), List.of("-1")),
                Arguments.of("[]=", List.of(), List.of("0")),


                //Add to List
                Arguments.of("1+1", List.of(), List.of("1", "1")),
                Arguments.of("1++1", List.of(), List.of("1", "1")),
                Arguments.of("++1", List.of(), List.of("1")),
                Arguments.of("+d6", List.of(1), List.of("1")),
                Arguments.of("++d6", List.of(1), List.of("1")),
                Arguments.of("1+d6", List.of(2), List.of("1", "2")),
                Arguments.of("d6+d6", List.of(1, 2), List.of("1", "2")),
                Arguments.of("d6++d6", List.of(1, 2), List.of("1", "2")),
                Arguments.of("1+2d6", List.of(2, 3), List.of("1", "2", "3")),
                Arguments.of("d6+1", List.of(2), List.of("2", "1")),
                Arguments.of("2d6+1", List.of(2, 3), List.of("2", "3", "1")),
                Arguments.of("+1+d6", List.of(2), List.of("1", "2")),
                Arguments.of("+1+2d6", List.of(2, 3), List.of("1", "2", "3")),
                Arguments.of("+d6+1", List.of(2), List.of("2", "1")),
                Arguments.of("+2d6+1", List.of(2, 3), List.of("2", "3", "1")),
                Arguments.of("1+d6k1", List.of(2), List.of("2")),
                Arguments.of("1+d6k+1", List.of(2), List.of("2")),
                Arguments.of("if('false','a') + 'b' ", List.of(), List.of("b")),
                Arguments.of("'b' + if('false','a') ", List.of(), List.of("b")),


                //Add negative to List
                Arguments.of("1-1", List.of(), List.of()),
                Arguments.of("1--1", List.of(), List.of("1", "1")),
                Arguments.of("--1", List.of(), List.of("1")),
                Arguments.of("-d6", List.of(1), List.of("-1")),
                Arguments.of("--d6", List.of(1), List.of("1")),
                Arguments.of("1-d6", List.of(2), List.of("1", "-2")),
                Arguments.of("d6-d6", List.of(1, 2), List.of("1", "-2")),
                Arguments.of("d6--d6", List.of(1, 2), List.of("1", "2")),
                Arguments.of("1-2d6", List.of(2, 3), List.of("1", "-2", "-3")),
                Arguments.of("d6-1", List.of(2), List.of("2", "-1")),
                Arguments.of("2d6-1", List.of(2, 3), List.of("2", "3", "-1")),
                Arguments.of("-1-d6", List.of(2), List.of("-1", "-2")),
                Arguments.of("-1-2d6", List.of(2, 3), List.of("-1", "-2", "-3")),
                Arguments.of("-d6-1", List.of(2), List.of("-2", "-1")),
                Arguments.of("-2d6-1", List.of(2, 3), List.of("-2", "-3", "-1")),
                Arguments.of("1-d6k1", List.of(2), List.of("1")),
                Arguments.of("1-d6l1", List.of(2), List.of("-2")),
                Arguments.of("[a/b/a]-'a'", List.of(), List.of("b", "a")),
                Arguments.of("[a/b/a]-[b/a]", List.of(), List.of("a")),
                Arguments.of("[a/b/a]-1", List.of(), List.of("a", "b", "a", "-1")),
                Arguments.of("[1/2/1]-1", List.of(), List.of("2", "1")),
                Arguments.of("[1/2/1]-[2/1]", List.of(), List.of("1")),
                Arguments.of("[1/3/1]-[2/1]", List.of(), List.of("3", "1", "-2")),


                Arguments.of("1d6 + 'a' + 1", List.of(3), List.of("3", "a", "1")),
                Arguments.of("1d6 + [x/y] + 1", List.of(3), List.of("3", "x", "y", "1")),
                Arguments.of("1d6 + [1d6] + 1", List.of(3), List.of("3", "1d6", "1")),
                Arguments.of("1d6 + [1D6] + 1", List.of(3), List.of("3", "1D6", "1")),
                Arguments.of("1d6 + ('a'+'y') + 1", List.of(3), List.of("3", "a", "y", "1")),
                Arguments.of("1d6 + '1d6' + 1", List.of(3), List.of("3", "1d6", "1")),
                Arguments.of("1d6 + '1D6' + 1", List.of(3), List.of("3", "1D6", "1")),
                Arguments.of("ifE(1d6,3,'+3','!3')", List.of(3), List.of("+3")),
                Arguments.of("ifE(1d6,3,'+3')", List.of(2), List.of("2")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4')", List.of(2), List.of("2")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4')", List.of(3), List.of("+3")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4')", List.of(4), List.of("+4")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4','else')", List.of(2), List.of("else")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4','else')", List.of(3), List.of("+3")),
                Arguments.of("ifE(1d6,3,'+3',4,'+4','else')", List.of(4), List.of("+4")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one')", List.of(3), List.of("three")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one')", List.of(2), List.of("two")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one')", List.of(1), List.of("one")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one')", List.of(4), List.of("4")),
                Arguments.of("ifE(ifE(ifE(1d6,3,'three'),2,'two'),1,'one','else')", List.of(4), List.of("else")),
                Arguments.of("ifE(1d6,3,'three','not three')", List.of(2), List.of("not three")),
                Arguments.of("ifG(1d6,3,'three','not three')", List.of(5), List.of("three")),
                Arguments.of("ifG(1d6,3,'three','not three')", List.of(2), List.of("not three")),
                Arguments.of("ifG(1d6,3,'three')", List.of(2), List.of("2")),
                Arguments.of("ifG(1d6,3,'>3',2,'>2')", List.of(2), List.of("2")),
                Arguments.of("ifG(1d6,3,'>3',2,'>2')", List.of(3), List.of(">2")),
                Arguments.of("ifG(1d6,3,'>3',2,'>2')", List.of(4), List.of(">3")),
                Arguments.of("ifG(1d6,2,'>2',3,'>3')", List.of(4), List.of(">2")),
                Arguments.of("ifG(1d6,3,'>3',2,'>2','else')", List.of(1), List.of("else")),
                Arguments.of("ifL(1d6,3,'three','not three')", List.of(2), List.of("three")),
                Arguments.of("ifL(1d6,3,'three','not three')", List.of(4), List.of("not three")),
                Arguments.of("ifL(1d6,3,'three')", List.of(4), List.of("4")),
                Arguments.of("ifIn(1d6,[2/3],'2or3','!2or3')", List.of(3), List.of("2or3")),
                Arguments.of("ifIn(1d6,2,'2','!2')", List.of(2), List.of("2")),
                Arguments.of("ifIn(1d6,2,'2')", List.of(5), List.of("5")),
                Arguments.of("ifIn(1d6,[2/3],'2or3','!2or3')", List.of(4), List.of("!2or3")),

                //replace
                Arguments.of("replace(8d10, [9/10], 'bonus')", List.of(9, 10, 3, 4, 5, 6, 7, 1), List.of("bonus", "bonus", "3", "4", "5", "6", "7", "1")),
                Arguments.of("replace(8d10, [9/10], 2d20)", List.of(9, 10, 3, 4, 5, 6, 7, 1, 19, 18, 17, 16), List.of("19", "18", "17", "16", "3", "4", "5", "6", "7", "1")),
                Arguments.of("replace(2d6, d6+d4, d10+d12)", List.of(1, 2, 2, 3, 9, 10), List.of("1", "9", "10")),
                Arguments.of("replace(8d10, [9/10], '')", List.of(9, 10, 3, 4, 5, 6, 7, 1), List.of("3", "4", "5", "6", "7", "1")),
                Arguments.of("replace(8d10, [9/10], [])", List.of(9, 10, 3, 4, 5, 6, 7, 1), List.of("3", "4", "5", "6", "7", "1")),
                Arguments.of("replace(8d10, [9/10], [a/a])", List.of(9, 10, 3, 4, 5, 6, 7, 1), List.of("a", "a", "a", "a", "3", "4", "5", "6", "7", "1")),
                Arguments.of("replace(8d10, [9/10], 'bonus')", List.of(1, 2, 3, 4, 5, 6, 7, 8), List.of("1", "2", "3", "4", "5", "6", "7", "8")),
                Arguments.of("replace(8d10, [1/2], 'a', [2/3], 'b')", List.of(1, 2, 3, 4, 5, 6, 7, 8), List.of("a", "a", "b", "4", "5", "6", "7", "8")),
                Arguments.of("replace(8d10, [1/2], 'a', 'a', 'b')", List.of(1, 2, 3, 4, 5, 6, 7, 8), List.of("b", "b", "3", "4", "5", "6", "7", "8")),

                //concat operator
                Arguments.of("'1'_'1'", List.of(), List.of("11")),
                Arguments.of("1d6_'1'", List.of(3), List.of("31")),
                Arguments.of("1d6_' damage'", List.of(3), List.of("3 damage")),
                Arguments.of("1d6 + 1d8 _' damage'", List.of(3, 7), List.of("3, 7 damage")),
                Arguments.of("1d6 + 1d8= _' damage'", List.of(3, 7), List.of("10 damage")),
                Arguments.of(" [] _' damage'", List.of(), List.of(" damage")),
                Arguments.of(" 'damage ' _ []", List.of(), List.of("damage ")),

                // concat function
                Arguments.of("concat('Attack: ', 3d6)", List.of(1, 2, 3), List.of("Attack: 1, 2, 3")),
                Arguments.of("concat('Attack:', '')", List.of(), List.of("Attack:")),
                Arguments.of("concat('Attack:', ' ')", List.of(), List.of("Attack: ")),
                Arguments.of("concat('Attack:', [])", List.of(), List.of("Attack:")),
                Arguments.of("concat('Attack:')", List.of(), List.of("Attack:")),
                Arguments.of("concat('')", List.of(), List.of("")),
                Arguments.of("concat('Attack: ', 1d20, ' Damage: ', 2d10+5=) ", List.of(1, 2, 3), List.of("Attack: 1 Damage: 10")),
                Arguments.of("concat(if('false','a'), 'only') ", List.of(), List.of("only")),
                Arguments.of("concat(val('v','a') 'v', ' only') ", List.of(), List.of("a only")),

                Arguments.of("[b/2/a]k2", List.of(), List.of("b", "a")),
                Arguments.of("[b/2/a]l2", List.of(), List.of("2", "a")),
                Arguments.of("'3.5'+'2.5'", List.of(), List.of("3.5", "2.5")),
                Arguments.of("val(1, ('a'+'b'+'c')) 3d1", List.of(1, 2, 3), List.of("a", "b", "c")),

                Arguments.of("1", List.of(), List.of("1")),
                Arguments.of("1 2", List.of(), List.of("1", "2")),
                Arguments.of("1 2 3", List.of(), List.of("1", "2", "3")),
                Arguments.of("1 1d6 3", List.of(2), List.of("1", "2", "3")),
                Arguments.of("1d6 1d6 1d6", List.of(1, 2, 3), List.of("1", "2", "3")),
                Arguments.of("3x1d6", List.of(1, 2, 3), List.of("1", "2", "3")),
                Arguments.of("3r1d6", List.of(1, 2, 3), List.of("1", "2", "3")),
                Arguments.of("val('$1',1d6) 3x'$1'", List.of(1, 2, 3), List.of("1", "1", "1")),
                Arguments.of("3x(val('$1',1d6)+'$1')", List.of(1, 2, 3), List.of("1", "2", "3")),
                Arguments.of("min(3x1d6)", List.of(1, 2, 3), List.of("1")),
                Arguments.of("1d6 rr 1", List.of(1, 2), List.of("2")),
                Arguments.of("1d6 rr 2+1", List.of(1, 2), List.of("1", "1")),
                Arguments.of("1d6 rr 2+1d6", List.of(1, 2), List.of("1", "2")),
                Arguments.of("1d6 rr 2+d6", List.of(1, 2), List.of("1", "2")),
                Arguments.of("1d6 rr 1+d6", List.of(1, 2, 3), List.of("2", "3")),
                Arguments.of("1d6 rr 3", List.of(1, 2), List.of("1")),
                Arguments.of("1d6rr1", List.of(1, 1), List.of("1")),
                Arguments.of("2d6 rr [1/5]", List.of(3, 5, 3, 4), List.of("3", "4")),
                Arguments.of("1r1d10rr['8, 9, 10']", List.of(1), List.of("1")),
                Arguments.of("2r1d10rr[8, 9, 10]", List.of(1, 8, 2), List.of("1", "2")),

                //decimal
                Arguments.of("1/2", List.of(), List.of("0")),
                Arguments.of("1//2", List.of(), List.of("0.5")),
                Arguments.of("1//3", List.of(), List.of("0.33333")),
                Arguments.of("-1//3", List.of(), List.of("-0.33333")),
                Arguments.of("1//2*-1//2", List.of(), List.of("-0.25")),
                Arguments.of("0.333333*3", List.of(), List.of("0.999999")),
                Arguments.of("(1//3)+(1//3)+(1//3)=", List.of(), List.of("0.99999")),
                Arguments.of("(1/3)+(1/3)+(1/3)=", List.of(), List.of("0")),
                Arguments.of("(4//3)+(1//3)+1>1", List.of(), List.of("1.33333")),
                Arguments.of("(4//3)+(1//3)+1>=1", List.of(), List.of("1.33333", "1")),
                Arguments.of("(4//3)+(1//3)+1<1", List.of(), List.of("0.33333")),
                Arguments.of("(4//3)+(1//3)+1<=1", List.of(), List.of("0.33333", "1")),

                //rounding
                Arguments.of("round(0.4, 'UP')", List.of(), List.of("1")),
                Arguments.of("round(0.4, 'UP', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.5, 'UP', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.6, 'UP', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.4, 'UP', 1)", List.of(), List.of("0.4")),
                Arguments.of("round(0.4, 'UP', -1)", List.of(), List.of("1E+1")),
                Arguments.of("round(0.4, 'UP', 2)", List.of(), List.of("0.40")),

                Arguments.of("round(0.4, 'DOWN')", List.of(), List.of("0")),
                Arguments.of("round(0.4, 'DOWN', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.5, 'DOWN', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.6, 'DOWN', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.4, 'DOWN', 1)", List.of(), List.of("0.4")),
                Arguments.of("round(0.4, 'DOWN', -1)", List.of(), List.of("0E+1")),
                Arguments.of("round(0.4, 'DOWN', 2)", List.of(), List.of("0.40")),

                Arguments.of("round(0.4, 'CEILING')", List.of(), List.of("1")),
                Arguments.of("round(0.4, 'CEILING', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.5, 'CEILING', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.6, 'CEILING', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.4, 'CEILING', 1)", List.of(), List.of("0.4")),
                Arguments.of("round(0.4, 'CEILING', -1)", List.of(), List.of("1E+1")),
                Arguments.of("round(0.4, 'CEILING', 2)", List.of(), List.of("0.40")),

                Arguments.of("round(0.4, 'FLOOR')", List.of(), List.of("0")),
                Arguments.of("round(0.4, 'FLOOR', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.5, 'FLOOR', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.6, 'FLOOR', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.4, 'FLOOR', 1)", List.of(), List.of("0.4")),
                Arguments.of("round(0.4, 'FLOOR', -1)", List.of(), List.of("0E+1")),
                Arguments.of("round(0.4, 'FLOOR', 2)", List.of(), List.of("0.40")),

                Arguments.of("round(0.4, 'HALF_UP')", List.of(), List.of("0")),
                Arguments.of("round(0.4, 'HALF_UP', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.5, 'HALF_UP', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.6, 'HALF_UP', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.4, 'HALF_UP', 1)", List.of(), List.of("0.4")),
                Arguments.of("round(0.4, 'HALF_UP', -1)", List.of(), List.of("0E+1")),
                Arguments.of("round(0.4, 'HALF_UP', 2)", List.of(), List.of("0.40")),

                Arguments.of("round(0.4, 'HALF_DOWN')", List.of(), List.of("0")),
                Arguments.of("round(0.4, 'HALF_DOWN', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.5, 'HALF_DOWN', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.6, 'HALF_DOWN', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.4, 'HALF_DOWN', 1)", List.of(), List.of("0.4")),
                Arguments.of("round(0.4, 'HALF_DOWN', -1)", List.of(), List.of("0E+1")),
                Arguments.of("round(0.4, 'HALF_DOWN', 2)", List.of(), List.of("0.40")),

                Arguments.of("round(0.4, 'HALF_EVEN')", List.of(), List.of("0")),
                Arguments.of("round(0.4, 'HALF_EVEN', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.5, 'HALF_EVEN', 0)", List.of(), List.of("0")),
                Arguments.of("round(0.6, 'HALF_EVEN', 0)", List.of(), List.of("1")),
                Arguments.of("round(0.4, 'HALF_EVEN', 1)", List.of(), List.of("0.4")),
                Arguments.of("round(0.4, 'HALF_EVEN', -1)", List.of(), List.of("0E+1")),
                Arguments.of("round(0.4, 'HALF_EVEN', 2)", List.of(), List.of("0.40")),

                //bool
                Arguments.of("!'false'", List.of(), List.of("true")),
                Arguments.of("!'true'", List.of(), List.of("false")),
                Arguments.of("'true'&&'false'", List.of(), List.of("false")),
                Arguments.of("'true'&&'true'", List.of(), List.of("true")),
                Arguments.of("'false'&&'false'", List.of(), List.of("false")),
                Arguments.of("'false'||'false'", List.of(), List.of("false")),
                Arguments.of("'false'||'true'", List.of(), List.of("true")),
                Arguments.of("'true'||'true'", List.of(), List.of("true")),
                Arguments.of("!('true'||'true')", List.of(), List.of("false")),
                Arguments.of("!'true'||'true'", List.of(), List.of("true")),
                Arguments.of("if('true','a','b')", List.of(), List.of("a")),
                Arguments.of("if('true','a')", List.of(), List.of("a")),
                Arguments.of("if('false','a')", List.of(), List.of()),
                Arguments.of("if('false','a','b')", List.of(), List.of("b")),
                Arguments.of("if('true','a','false','b','c')", List.of(), List.of("a")),
                Arguments.of("if('true','a','false','b')", List.of(), List.of("a")),
                Arguments.of("if('false','a','true','b')", List.of(), List.of("b")),
                Arguments.of("if('false','a','true','b','c')", List.of(), List.of("b")),
                Arguments.of("if('false','a','false','b')", List.of(), List.of()),
                Arguments.of("if('false','a','false','b','c')", List.of(), List.of("c")),

                Arguments.of("3>?2", List.of(), List.of("true")),
                Arguments.of("2>?2", List.of(), List.of("false")),
                Arguments.of("3>=?2", List.of(), List.of("true")),
                Arguments.of("2>=?2", List.of(), List.of("true")),
                Arguments.of("1>=?2", List.of(), List.of("false")),
                Arguments.of("2<?3", List.of(), List.of("true")),
                Arguments.of("2<?2", List.of(), List.of("false")),
                Arguments.of("2<=?3", List.of(), List.of("true")),
                Arguments.of("2<=?2", List.of(), List.of("true")),
                Arguments.of("3<=?2", List.of(), List.of("false")),
                Arguments.of("1=?2", List.of(), List.of("false")),
                Arguments.of("1=?1", List.of(), List.of("true")),
                Arguments.of("'a'=?'a'", List.of(), List.of("true")),
                Arguments.of("'a'=?'ab'", List.of(), List.of("false")),
                Arguments.of("[a/b/c]=?[a/b/c]", List.of(), List.of("true")),
                Arguments.of("[a/b/c]=?[a/c/b]", List.of(), List.of("false")),
                Arguments.of("[a/b] in [a/b/c]", List.of(), List.of("true")),
                Arguments.of("[a/b]in[c/b]", List.of(), List.of("false")),
                Arguments.of("val('$1',2d6) if('$1'= >=?9, 'crit', '$1'=)", List.of(4, 6), List.of("crit")),
                Arguments.of("val('$1',2d6) '$1'==6c =? 1", List.of(4, 6), List.of("true")),
                Arguments.of("val('$1',2d6) if('$1'==6c =? 1, 'crit', '$1'=)", List.of(4, 6), List.of("crit")),
                Arguments.of("val('$1',2d6) if('$1'= >=?9 && '$1'==6c =? 1, 'crit', '$1'=)", List.of(4, 6), List.of("crit")),
                Arguments.of("val('$1',2d6) if('$1'= >=?9 && '$1'==6c =? 1, 'crit', '$1'=)", List.of(5, 5), List.of("10")),

                //color function
                Arguments.of("color(3d6,'red')", List.of(3, 2, 1), List.of("red:3", "red:2", "red:1")),
                Arguments.of("(color(3d6,'red')+color(3d4,'black'))c", List.of(), List.of("red:3", "black:3")),
                Arguments.of("(color(3d6,'red')+color(3d4,'black'))=", List.of(), List.of("red:18", "black:12")),
                Arguments.of("(color(3d6,'red')+color(3d4,'black'))k2", List.of(1, 2, 3, 2, 2, 2), List.of("red:1", "red:2", "red:3", "black:2", "black:2", "black:2")),
                Arguments.of("(color(3d6,'red')+color(3d4,'black'))l2", List.of(1, 2, 3, 2, 2, 2), List.of("red:1", "red:2", "red:3", "black:2", "black:2", "black:2")),
                Arguments.of("asc((color(3d6,'red')+color(3d4,'black')))", List.of(1, 2, 3, 2, 2, 2), List.of("black:2", "black:2", "black:2", "red:1", "red:2", "red:3")),
                Arguments.of("desc((color(3d6,'red')+color(3d4,'black')))", List.of(1, 2, 3, 2, 2, 2), List.of("red:3", "red:2", "red:1", "black:2", "black:2", "black:2")),

                //tag
                Arguments.of("6tag'red'>=10", List.of(), List.of("red:6")),
                Arguments.of("6tag'red'>=6", List.of(), List.of("red:6")),
                Arguments.of("6tag'red'>=10tag'red'", List.of(), List.of()),
                Arguments.of("6tag'red'>=6tag'red'", List.of(), List.of("red:6")),

                Arguments.of("6tag'red'>10", List.of(), List.of("red:6")),
                Arguments.of("6tag'red'>5", List.of(), List.of("red:6")),
                Arguments.of("6tag'red'>10tag'red'", List.of(), List.of()),
                Arguments.of("6tag'red'>5tag'red'", List.of(), List.of("red:6")),

                Arguments.of("6tag'red'<5", List.of(), List.of("red:6")),
                Arguments.of("6tag'red'<7", List.of(), List.of("red:6")),
                Arguments.of("6tag'red'<5tag'red'", List.of(), List.of()),
                Arguments.of("6tag'red'<7tag'red'", List.of(), List.of("red:6")),

                Arguments.of("6tag'red'<=5", List.of(), List.of("red:6")),
                Arguments.of("6tag'red'<=7", List.of(), List.of("red:6")),
                Arguments.of("6tag'red'<=5tag'red'", List.of(), List.of()),
                Arguments.of("6tag'red'<=7tag'red'", List.of(), List.of("red:6")),

                Arguments.of("(7 tag 'red' + 6+5 tag 'red' +4) k 1", List.of(), List.of("6", "red:7", "red:5")),
                Arguments.of("(7 tag 'red' + 6+5 tag 'red' +4) k 1 tag 'red'", List.of(), List.of("red:7", "6", "4")),

                Arguments.of("(7 tag 'red' + 6+5 tag 'red' +4) l 1", List.of(), List.of("4", "red:7", "red:5")),
                Arguments.of("(7 tag 'red' + 6+5 tag 'red' +4) l 1 tag 'red'", List.of(), List.of("red:5", "6", "4")),

                Arguments.of("groupC((7 + 3+ 2+3+3+7) tag 'red' + (7 + 3+ 2+3+3+7))", List.of(), List.of("3x3", "red:3x3", "2x7", "red:2x7", "red:1x2", "1x2")),

                //multiLine
                Arguments.of("'a\nb\nc'", List.of(), List.of("a\nb\nc")),
                Arguments.of("d[a\nb\nc]", List.of(1), List.of("a\nb\nc")),
                Arguments.of("d[a\nb\nc,d,e]", List.of(1), List.of("a\nb\nc")),
                Arguments.of("d[a\nb\nc/d/e]", List.of(1), List.of("a\nb\nc")),
                Arguments.of("d[a\nb\nc,d,e]", List.of(2), List.of("d")),
                Arguments.of("[a\nb\nc,d,e]", List.of(0), List.of("a\nb\nc", "d", "e")),
                Arguments.of("[a\nb\nc/d/e]", List.of(0), List.of("a\nb\nc", "d", "e")),
                Arguments.of("'a\nb\nc,d,e'", List.of(0), List.of("a\nb\nc", "d", "e")),
                Arguments.of("'a\nb\nc/d/e'", List.of(0), List.of("a\nb\nc", "d", "e")),
                Arguments.of("[a\nb\nc,\nd,e\n]", List.of(0), List.of("a\nb\nc", "d", "e")),
                Arguments.of("[a\nb\nc/\nd/e\n]", List.of(0), List.of("a\nb\nc", "d", "e")),
                Arguments.of("'a\nb\nc,\nd,e\n'", List.of(0), List.of("a\nb\nc", "d", "e")),
                Arguments.of("'a\nb\nc/\nd/e\n'", List.of(0), List.of("a\nb\nc", "d", "e")),
                Arguments.of("'test'_'\n'_'test'", List.of(), List.of("test\ntest")),

                //list repeat
                Arguments.of("2r concat(val('r',2d6) 'r'>5c _ ' ' _ 'r'= )", List.of(3, 6, 1, 2), List.of("1 9", "0 3")),

                //Exalted 3e
                Arguments.of("val('$1', cancel(double(10d10,10),1,[7/8/9/10])), ifE(('$1'>=7)c,0,ifG(('$1'<=1)c,0,'Botch'))", List.of(3, 2, 3, 1, 5, 9, 6, 6, 6, 6, 6), List.of("0")),
                Arguments.of("val('$1', cancel(double(10d10,10),1,[7/8/9/10])), ifE(('$1'>=7)c,0,ifG(('$1'<=1)c,0,'Botch'))", List.of(3, 2, 3, 3, 5, 9, 6, 6, 6, 6, 6), List.of("1")),
                Arguments.of("val('$1', cancel(double(10d10,10),1,[7/8/9/10])), ifE(('$1'>=7)c,0,ifG(('$1'<=1)c,0,'Botch'))", List.of(3, 2, 1, 3, 5, 9, 10, 6, 6, 6, 6), List.of("2")),
                Arguments.of("val('$1', cancel(double(10d10,10),1,[7/8/9/10])), ifE(('$1'>=7)c,0,ifG(('$1'<=1)c,0,'Botch'))", List.of(3, 2, 1, 3, 5, 5, 5, 6, 6, 6, 6), List.of("Botch")),

                //Vampire V5
                Arguments.of(VAMPIRE_V5, List.of(1, 6, 7, 1, 6, 7), List.of("successes: 4")),
                Arguments.of(VAMPIRE_V5, List.of(5, 10, 10, 5, 6, 7), List.of("successes: 6")),
                Arguments.of(VAMPIRE_V5, List.of(5, 10, 10, 5, 10, 10), List.of("successes: 8")),
                Arguments.of(VAMPIRE_V5, List.of(5, 4, 4, 5, 1, 1), List.of("successes: 0 bestial failure")),
                Arguments.of(VAMPIRE_V5, List.of(5, 4, 10, 5, 1, 1), List.of("successes: 1")),
                Arguments.of(VAMPIRE_V5, List.of(5, 4, 10, 5, 1, 10), List.of("successes: 4 messy critical")),

                //One Roll Engine
                Arguments.of("groupc(4d10+(4r10)>=6)", List.of(5, 4, 10, 6, 1, 10), List.of("5x10", "1x6")),

                //Anima: Beyond Fantasy
                Arguments.of("val('$r1',1d100) ifG('$r1',90,val('$r2',1d100) ifG('$r2',91,val('$r3',1d100) '$r3'+'$r2'+'$r1'=,'$r1'+'$r2'= ),'$r1' )", List.of(92, 92, 8), List.of("192")),

                //Ironsworn
                Arguments.of("ifE((2d10<(1d6+1=))c,0,'failure',1, 'mixed results', 'total success')", List.of(5, 10, 6), List.of("mixed results")),

                //Cyberpunk Red
                Arguments.of("val('$roll', 1d10) ifE('$roll', 1, '$roll'-1d10, 10, '$roll'+1d10, '$roll')+2=", List.of(5, 4, 10, 6, 1, 10), List.of("7")),

                // traveler game with doubles crit system
                Arguments.of(" val('$roll',3d6) val('$skill',3) (ifG('$roll'==1c,1, ifG('$skill', 0, 11, '$roll'k2=), ifG('$roll'==2c,1,ifG('$skill', 1, 12, '$roll'k2=), ifG('$roll'==3c,1,ifG('$skill', 2, 13, '$roll'k2=), ifG('$roll'==4c,1,ifG('$skill', 3, 14, '$roll'k2=),  ifG('$roll'==5c,1,ifG('$skill', 4, 15, '$roll'k2=),  ifG('$roll'==6c,1,ifG('$skill', 5, 16, '$roll'k2=), '$roll'k2=)))))))=", List.of(3, 3, 2), List.of("13")),

                Arguments.of("1d0", List.of(), List.of())
        );
    }

    private static Stream<Arguments> generateStringDiceDataColor() {
        return Stream.of(
                Arguments.of("d6 col 'red'", List.of(6), List.of("6"), "red"),
                Arguments.of("replace(d6 col 'red',6,7)", List.of(6), List.of("7"), "red"),
                Arguments.of("cancel(3d6 col 'red',[6],[1])", List.of(1, 6, 2), List.of("2"), "red"),
                Arguments.of("ifIn(d6 col 'red',[5,6],7)", List.of(6), List.of("7"), "red"),
                Arguments.of("d6 col 'red' in [5,6]", List.of(6), List.of("true"), "red"),
                Arguments.of("(d6 col 'red') rr 6", List.of(6, 1), List.of("1"), "red")
        );
    }

    private static Stream<Arguments> generateErrorData() {
        return Stream.of(
                Arguments.of("=1", "Operator = has left associativity but the left value was: empty"),
                Arguments.of("abc", "No matching operator for 'ab', non-functional text and value names must to be surrounded by '' or []"),
                Arguments.of("1-", "Operator - has right associativity but the right value was: empty"),
                Arguments.of("1*", "Operator * does not support unary operations"),
                Arguments.of("*1", "Operator * does not support unary operations"),
                Arguments.of("10 5 +", "Operator + has right associativity but the right value was: empty"),
                Arguments.of("10**5", "Operator * does not support unary operations"),
                Arguments.of("3d6==[1/2]", "'==' requires as right a single element but was '[1, 2]'. Try to sum the numbers together like ([1/2]=)"),
                Arguments.of("min()", "empty brackets are not allowed"),
                Arguments.of("min(,2)", "A separator can't be followed by another separator or open bracket"),
                Arguments.of("min(1,)", "argument is missing"),
                Arguments.of("1d6+(5,6)", "Separator ',' in bracket '()' without leading function is not allowed"),
                Arguments.of("()", "empty brackets are not allowed"),
                Arguments.of("min((10,15),20)", "Separator ',' in bracket '()' without leading function is not allowed"),
                Arguments.of("1d6,(1d6,1)", "Separator ',' in bracket '()' without leading function is not allowed"),
                Arguments.of("min3(45)", "A function, in this case 'min', must be followed a open function bracket: ("),
                Arguments.of(")", "expression can't start with a close bracket"),
                Arguments.of("(", "Parentheses mismatched"),
                Arguments.of("1)", "Parentheses mismatched"),
                Arguments.of(",3", "expression can't start with a separator"),
                Arguments.of("10*", "Operator * does not support unary operations"),
                Arguments.of("10*a", "No matching operator for 'a', non-functional text and value names must to be surrounded by '' or []"),
                Arguments.of("1/0", "/ by zero"),
                Arguments.of("1//0", "/ by zero"),
                Arguments.of("color(3d6,[a/b])", "'color' requires as second argument a single element but was '[a, b]'"),
                Arguments.of("ifL(2d6,3,'three','not three')", "'ifL' requires as 1 argument a single element but was '[2, 3]'. Try to sum the numbers together like ((2d6=)"),
                Arguments.of("ifIn(2d6,3,'three','not three')", "'ifIn' requires as 1 argument a single element but was '[2, 3]'. Try to sum the numbers together like ((2d6=)"),
                Arguments.of("ifL(1d6,2d6,'three','not three')", "'ifL' requires as 2 argument a single element but was '[3, 1]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("ifG(1d6,2d6,'three','not three')", "'ifG' requires as 2 argument a single element but was '[3, 1]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("ifG(1d6,2d6,'three','not three')", "'ifG' requires as 2 argument a single element but was '[3, 1]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("ifG(1d6,6,'three',2d6,'not three')", "'ifG' requires as 4 argument a single element but was '[3, 1]'. Try to sum the numbers together like (2d6=)"),
                Arguments.of("1d-1", "Not enough values, d needs 2"),
                Arguments.of("d-1", "Not enough values, d needs 1"),
                Arguments.of("2d6k-1", "The number to keep can not be negativ but was -1"),
                Arguments.of("2d6l-1", "The number to keep can not be negativ but was -1"),
                Arguments.of("d!1", "The number of sides of a die must be greater then 1 but was 1"),
                Arguments.of("d!!1", "The number of sides of a die must be greater then 1 but was 1"),
                Arguments.of("1d!1", "The number of sides of a die must be greater then 1 but was 1"),
                Arguments.of("1d!!1", "The number of sides of a die must be greater then 1 but was 1"),
                Arguments.of("1001d2", "The number of dice must be less or equal then 1000 but was 1001"),
                Arguments.of("1001d!2", "The number of dice must be less or equal then 1000 but was 1001"),
                Arguments.of("1001d!!2", "The number of dice must be less or equal then 1000 but was 1001"),
                Arguments.of("(-6)d2", "The number of dice can not be negativ but was -6"),
                Arguments.of("(-6)d!2", "The number of dice can not be negativ but was -6"),
                Arguments.of("(-6)d!!2", "The number of dice can not be negativ but was -6"),
                Arguments.of("d'-1'", "Sides of dice to roll must be positive"),
                Arguments.of("d!'-1'", "The number of sides of a die must be greater then 1 but was -1"),
                Arguments.of("d!!'-1'", "The number of sides of a die must be greater then 1 but was -1"),
                Arguments.of("5 mod 0", "/ by zero"),
                Arguments.of("11x(1d6)", "The number of repeat must between 1-10 but was 11"),
                Arguments.of("0x(1d6)", "The number of repeat must between 1-10 but was 0"),
                Arguments.of("21r(1d6)", "The number of list repeat must between 0-20 but was 21"),
                Arguments.of("-1r(1d6)", "The number of list repeat must between 0-20 but was -1"),
                Arguments.of("3d6x(1d6)", "'x' requires as left input a single integer but was '[2, 3, 1]'. Try to sum the numbers together like (3d6=)"),
                Arguments.of("'a'x(1d6)", "'x' requires as left input a single integer but was '[a]'"),
                Arguments.of("x(1d6)", "Operator x does not support unary operations"),
                Arguments.of("(3d[a/b/c])=", "'=' requires as left input only decimals but was '[b, c, a]'"),
                Arguments.of("color(2,'red')*color(2,'black')", "'*' requires all elements to be the same tag, the tags where '[red, black]'"),
                Arguments.of("1000d9999999999", "The number '9999999999' is too big"),
                Arguments.of("9.9999999999", "The number '9.9999999999' is too big"),
                Arguments.of("(3x2d6)=", "'=' requires as 1 inputs but was '[[2, 3], [1, 4], [1, 1]]'"),
                Arguments.of("3&&'ab'", "'&&' requires as left input a single boolean but was '[3]'"),
                Arguments.of("3||'ab'", "'||' requires as left input a single boolean but was '[3]'"),
                Arguments.of("1<?'ab'", "'<?' requires as right input a single decimal but was '[ab]'"),
                Arguments.of("'ab'<?'1'", "'<?' requires as left input a single decimal but was '[1]'. Try to sum the numbers together like ('1'=)"),
                Arguments.of("1<=?'ab'", "'<=?' requires as right input a single decimal but was '[ab]'"),
                Arguments.of("'ab'<=?'1'", "'<=?' requires as left input a single decimal but was '[1]'. Try to sum the numbers together like ('1'=)"),
                Arguments.of("1>?'ab'", "'>?' requires as right input a single decimal but was '[ab]'"),
                Arguments.of("'ab'>?'1'", "'>?' requires as left input a single decimal but was '[1]'. Try to sum the numbers together like ('1'=)"),
                Arguments.of("1>=?'ab'", "'>=?' requires as right input a single decimal but was '[ab]'"),
                Arguments.of("'ab'>=?'1'", "'>=?' requires as left input a single decimal but was '[ab]'"),
                Arguments.of("!'ab'", "'!' requires as right input a single boolean but was '[ab]'"),
                Arguments.of("if('false', 'a','','b') 'a'", "'if' requires as position 3 input a single boolean but was '[]'"),
                Arguments.of("if('false', val('a',10) '', val('a',-10) '') +'a'", "'if' requires as position 3 input a single boolean but was '[]'"), //the value produce the wrong number of arguments
                Arguments.of("replace(3d6,'1','2','3')", "'replace' requires an odd number of arguments but was 4"),
                Arguments.of("if([], 'true', 'false')", "'if' requires as position 1 input a single boolean but was '[]'"),
                Arguments.of("if('false', 'false', [], 'true')", "'if' requires as position 3 input a single boolean but was '[]'"),
                Arguments.of("if('false', 'false', , 'true')", "A separator can't be followed by another separator or open bracket"),
                Arguments.of(" if('false', 'false', val('$v',1d6) , 'true')", "'if' requires a non-empty input as 3 argument"),
                Arguments.of("1d+6", "Not enough values, d needs 2"),
                Arguments.of("concat('test' 'test')", "All brackets need to be closed be for starting a new expression or missing ','"),
                Arguments.of("exp(1d6)", "'exp' requires 2 or 3 arguments but was 1"),
                Arguments.of("exp(1d6, val('a', 1), 3)", "'exp' requires a non-empty input as second argument"),
                Arguments.of("exp(1d6, 3, -1)", "'exp' requires as third argument a number between 0 and 100"),
                Arguments.of("exp(1d6, 3, val('a', 1))", "'exp' requires a non-empty input as third argument"),
                Arguments.of("exp(1d6, 3, 101)", "'exp' requires as third argument a number between 0 and 100"),
                Arguments.of("1 - [a]", "'-' requires as right input only decimals or elements that are on the left side '[1]' but was '[a]'"),
                Arguments.of("1 - [3/a]", "'-' requires as right input only decimals or elements that are on the left side '[1]' but was '[3, a]'"),
                Arguments.of("val('a')", "'val' requires as 2 inputs but was '[[a]]'"),
                Arguments.of("val('',d6)", "'val' requires a non-empty input as first argument"),
                Arguments.of("colorOn(2d6,'white')", "'colorOn' requires an odd number of arguments but was 2"),
                Arguments.of("colorOn(1,1,[white/red])", "'colorOn' requires as 3 argument a single or no element but was '[white, red]'"),
                Arguments.of("colorOn(1,val('a',1),'white')", "'colorOn' requires as 2 inputs but was empty"),
                Arguments.of("colorOn(1,2x2,'white')", "'colorOn' requires a single argument as 2 input but was '[[2], [2]]'"),
                Arguments.of("colorOn(1,1,val('a',1))", "'colorOn' requires as 3 inputs but was empty"),

                Arguments.of("round(2d6,'UP')", "'round' requires as first argument input a single decimal but was '[2, 3]'. Try to sum the numbers together like ((2d6=)"),
                Arguments.of("round('a','UP')", "'round' requires as first argument input a single decimal but was '[a]'"),
                Arguments.of("round(1.4)", "'round' requires as 2-3 inputs but was '[[1.4]]'"),
                Arguments.of("round(1.4, 2)", "The second element must be a single value of: UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN but was: 2"),
                Arguments.of("round(1.4, 'upper')", "The second element must be a single value of: UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN but was: upper"),

                Arguments.of("d", "Operator d has right associativity but the right value was: empty")

        );
    }

    private static Stream<Arguments> generateRandomDiceData() {
        return Stream.of(
                Arguments.of("ifE(1d20,1d20,1d20)", List.of(1, 1, 2), List.of(List.of("1"), List.of("1"), List.of("2"))),
                Arguments.of("ifE(1d20,1d20,1d20)", List.of(3, 2), List.of(List.of("3"), List.of("2"), List.of("20"))),
                Arguments.of("ifE(1d20,1d20,1d20,1d20)", List.of(3, 4, 2, 4), List.of(List.of("3"), List.of("4"), List.of("2"), List.of("4"))),
                Arguments.of("ifE(1d20,1,2)", List.of(1, 1, 2), List.of(List.of("1"))),
                Arguments.of("ifE(1d20,2,3)", List.of(3, 2), List.of(List.of("3"))),
                Arguments.of("ifE(1d20,4,1d20,4)", List.of(3, 4, 2, 4), List.of(List.of("3"), List.of("4"))),
                Arguments.of("ifE(3,4,1d20,4)", List.of(3, 4, 2, 4), List.of(List.of("3"))),
                Arguments.of("1d6 + 3d20 + 10", List.of(3, 2, 1, 4), List.of(List.of("3"), List.of("2", "1", "4")))
        );
    }

    private static Stream<Arguments> resultSizeDate() {
        return Stream.of(
                Arguments.of("val(2, 'abc'),d6", 1),
                Arguments.of("1d6,d6", 2),
                Arguments.of("d6", 1),
                Arguments.of("1d6", 1),
                Arguments.of("3x1d6", 3),
                Arguments.of("", 0)
        );
    }

    private static Stream<Arguments> generateHasOperatorOrFunction() {
        return Stream.of(
                Arguments.of("=1", true),
                Arguments.of("1", false),
                Arguments.of("20", false),
                Arguments.of("u", false),
                Arguments.of("  ud  ", true),
                Arguments.of("  D  ", true)
        );
    }

    private static Stream<Arguments> generateStringDiceDataWithRandomElements() {
        return Stream.of(
                //if
                Arguments.of("if(d6>?3,d8)", List.of(4), "8", "[3de0i0r0=4∈[1...6], 9de0i0r0=8∈[1...8]]"),
                Arguments.of("if(d6>?3,d8)", List.of(2), "", "[3de0i0r0=2∈[1...6]]"),
                Arguments.of("if(d6>?3,d4,d8)", List.of(4), "4", "[3de0i0r0=4∈[1...6], 9de0i0r0=4∈[1...4]]"),
                Arguments.of("if(d6>?3,d4,d8)", List.of(2), "8", "[3de0i0r0=2∈[1...6], 12de0i0r0=8∈[1...8]]"),
                Arguments.of("if(d6>?3,d4,d12>?3,d8)", List.of(4), "4", "[3de0i0r0=4∈[1...6], 9de0i0r0=4∈[1...4]]"),
                Arguments.of("if(d6>?3,d4,d12<?3,d8)", List.of(2), "", "[3de0i0r0=2∈[1...6], 12de0i0r0=12∈[1...12]]"),
                Arguments.of("if(d6>?3,d4,d12>?3,d8)", List.of(2), "8", "[3de0i0r0=2∈[1...6], 12de0i0r0=12∈[1...12], 19de0i0r0=8∈[1...8]]"),
                Arguments.of("if(d6>?3,d4,d12<?3,d8,d20)", List.of(2), "20", "[3de0i0r0=2∈[1...6], 12de0i0r0=12∈[1...12], 22de0i0r0=20∈[1...20]]"),
                Arguments.of("if(0=?1, '') 1", List.of(), "1", "[]"),

                //val
                Arguments.of("val('$a',1d6),'$a' +'$a'", List.of(3), "3, 3", "[10de0i0r0=3∈[1...6]]"),
                Arguments.of("val('$a',1d6),if('$a'>?3,'$a' + 3, '$a' -1)", List.of(3), "3, -1", "[10de0i0r0=3∈[1...6]]"),
                Arguments.of("val('$a',1d6),if('$a'>?3,'$a' + 3, '$a' -1)", List.of(4), "4, 3", "[10de0i0r0=4∈[1...6]]"),

                //exp
                Arguments.of("exp(d6,d6,d6)", List.of(2, 2, 2), "2, 6", "[4de0i0r0=2∈[1...6], 4de1i0r0=6∈[1...6], 7de0i0r0=2∈[1...6], 10de0i0r0=2∈[1...6]]"),
                Arguments.of("exp(2d4,d4,d4)", List.of(), "4, 4, 4, 4, 4, 4, 4, 4, 4, 4", "[5de0i0r0=4∈[1...4], 5de0i1r0=4∈[1...4], 5de1i0r0=4∈[1...4], 5de1i1r0=4∈[1...4], 5de2i0r0=4∈[1...4], 5de2i1r0=4∈[1...4], 5de3i0r0=4∈[1...4], 5de3i1r0=4∈[1...4], 5de4i0r0=4∈[1...4], 5de4i1r0=4∈[1...4], 8de0i0r0=4∈[1...4], 11de0i0r0=4∈[1...4]]"),
                Arguments.of("exp(d6,d6)", List.of(2, 2), "2, 6", "[4de0i0r0=2∈[1...6], 4de1i0r0=6∈[1...6], 7de0i0r0=2∈[1...6]]"),

                //bool
                Arguments.of("d[0,1]&&d[0,1]", List.of(), "true", "[0de0i0r0=1∈[0, 1], 8de0i0r0=1∈[0, 1]]"),
                Arguments.of("d6=?6", List.of(), "true", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6=?1", List.of(), "false", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6>?6", List.of(), "false", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6>?1", List.of(), "true", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6>=?7", List.of(), "false", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6>=?6", List.of(), "true", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6>=?1", List.of(), "true", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 in d6", List.of(6, 1), "false", "[0de0i0r0=6∈[1...6], 6de0i0r0=1∈[1...6]]"),
                Arguments.of("d6 in 6", List.of(), "true", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6<?6", List.of(), "false", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6<?1", List.of(), "false", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6<=?7", List.of(), "true", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6<=?6", List.of(), "true", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6<=?1", List.of(), "false", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("!d[0,1]", List.of(), "false", "[1de0i0r0=1∈[0, 1]]"),
                Arguments.of("!d[0,1]", List.of(1), "true", "[1de0i0r0=0∈[0, 1]]"),
                Arguments.of("d[0,1]||d[0,1]", List.of(), "true", "[0de0i0r0=1∈[0, 1], 8de0i0r0=1∈[0, 1]]"),

                //dice
                Arguments.of("d6", List.of(3), "3", "[0de0i0r0=3∈[1...6]]"),
                Arguments.of("d[a,b,c,d,e,f]", List.of(3), "c", "[0de0i0r0=c∈[a, b, c, d, e, f]]"),
                Arguments.of("d!6", List.of(6, 3), "6, 3", "[0d!e0i0r0=6∈[1...6], 0d!e0i0r1=3∈[1...6]]"),
                Arguments.of("d!!6", List.of(6, 3), "9", "[0d!!e0i0r0=6∈[1...6], 0d!!e0i0r1=3∈[1...6]]"),
                Arguments.of("(d4)d(d6)", List.of(2, 6, 6, 4), "6, 4", "[1de0i0r0=2∈[1...4], 4de0i0r0=6∈[1...6], 4de0i1r0=4∈[1...6], 6de0i0r0=6∈[1...6]]"),
                Arguments.of("(d4)d!(d6)", List.of(2, 6, 6, 4, 3), "6, 4, 3", "[1de0i0r0=2∈[1...4], 4d!e0i0r0=6∈[1...6], 4d!e0i0r1=4∈[1...6], 4d!e0i1r0=3∈[1...6], 7de0i0r0=6∈[1...6]]"),
                Arguments.of("(d4)d!!(d6)", List.of(2, 6, 6, 4, 3), "10, 3", "[1de0i0r0=2∈[1...4], 4d!!e0i0r0=6∈[1...6], 4d!!e0i0r1=4∈[1...6], 4d!!e0i1r0=3∈[1...6], 8de0i0r0=6∈[1...6]]"),

                Arguments.of("d6 col 'red' ", List.of(), "6-c:red", "[0de0i0r0=6-c:red∈[1...6]]"),
                Arguments.of("d6 col d6 ", List.of(), "6-c:6", "[0de0i0r0=6-c:6∈[1...6], 7de0i0r0=6-c:6∈[1...6]]"),
                Arguments.of("d[a,b,c,d,e,f] col 'red'", List.of(), "f-c:red", "[0de0i0r0=f-c:red∈[a, b, c, d, e, f]]"),
                Arguments.of("d!6 col 'red'", List.of(6, 3), "6-c:red, 3-c:red", "[0d!e0i0r0=6-c:red∈[1...6], 0d!e0i0r1=3-c:red∈[1...6]]"),
                Arguments.of("d!!6 col 'red'", List.of(6, 3), "9-c:red", "[0d!!e0i0r0=6-c:red∈[1...6], 0d!!e0i0r1=3-c:red∈[1...6]]"),

                Arguments.of("d6 tag 'red' ", List.of(), "6-t:red", "[0de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 tag d6 ", List.of(), "6-t:6", "[0de0i0r0=6∈[1...6], 7de0i0r0=6∈[1...6]]"),
                Arguments.of("d[a,b,c,d,e,f] tag 'red'", List.of(), "f-t:red", "[0de0i0r0=f∈[a, b, c, d, e, f]]"),
                Arguments.of("d!6 tag 'red'", List.of(6, 3), "6-t:red, 3-t:red", "[0d!e0i0r0=6∈[1...6], 0d!e0i0r1=3∈[1...6]]"),
                Arguments.of("d!!6 tag 'red'", List.of(6, 3), "9-t:red", "[0d!!e0i0r0=6∈[1...6], 0d!!e0i0r1=3∈[1...6]]"),
                Arguments.of("d6 rr d6", List.of(), "6", "[0de0i0r0=6∈[1...6], 0de1i0r0=6∈[1...6], 6de0i0r0=6∈[1...6]]"),

                //list
                Arguments.of("d6 + d6", List.of(), "6, 6", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 _ d6", List.of(), "66", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 == d6", List.of(), "6", "[0de0i0r0=6∈[1...6], 6de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 == d6", List.of(3, 6), "", "[0de0i0r0=3∈[1...6], 6de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 >= d6", List.of(3, 6), "", "[0de0i0r0=3∈[1...6], 6de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 >= d6", List.of(6, 6), "6", "[0de0i0r0=6∈[1...6], 6de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 >= d6", List.of(6, 5), "6", "[0de0i0r0=6∈[1...6], 6de0i0r0=5∈[1...6]]"),
                Arguments.of("d6 > d6", List.of(3, 6), "", "[0de0i0r0=3∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 > d6", List.of(6, 6), "", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 > d6", List.of(6, 5), "6", "[0de0i0r0=6∈[1...6], 5de0i0r0=5∈[1...6]]"),
                Arguments.of("d6 <= d6", List.of(3, 6), "3", "[0de0i0r0=3∈[1...6], 6de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 <= d6", List.of(6, 6), "6", "[0de0i0r0=6∈[1...6], 6de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 <= d6", List.of(6, 5), "", "[0de0i0r0=6∈[1...6], 6de0i0r0=5∈[1...6]]"),
                Arguments.of("d6 < d6", List.of(3, 6), "3", "[0de0i0r0=3∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 < d6", List.of(6, 6), "", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 < d6", List.of(6, 5), "", "[0de0i0r0=6∈[1...6], 5de0i0r0=5∈[1...6]]"),
                Arguments.of("d6 k d6", List.of(), "6", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 l d6", List.of(), "6", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 x d6", List.of(3), "6, 6, 6", "[0de0i0r0=3∈[1...6], 5de0i0r0=6∈[1...6], 5de1i0r0=6∈[1...6], 5de2i0r0=6∈[1...6]]"),
                Arguments.of("d6 r d6", List.of(3), "6, 6, 6", "[0de0i0r0=3∈[1...6], 5de0i0r0=6∈[1...6], 5de1i0r0=6∈[1...6], 5de2i0r0=6∈[1...6]]"),

                //math
                Arguments.of("3d6=", List.of(), "18", "[1de0i0r0=6∈[1...6], 1de0i1r0=6∈[1...6], 1de0i2r0=6∈[1...6]]"),
                Arguments.of("d6 / d6", List.of(), "1", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 // d6", List.of(), "1", "[0de0i0r0=6∈[1...6], 6de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 mod d6", List.of(), "0", "[0de0i0r0=6∈[1...6], 7de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 * d6", List.of(), "36", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 - d6", List.of(), "", "[0de0i0r0=6∈[1...6], 5de0i0r0=6∈[1...6]]"),
                Arguments.of("d6 - d6", List.of(5), "5, -6", "[0de0i0r0=5∈[1...6], 5de0i0r0=6∈[1...6]]"),

                //function
                Arguments.of("cancel(3d6, d6, d4)", List.of(4, 5, 6), "5", "[8de0i0r0=4∈[1...6], 8de0i1r0=5∈[1...6], 8de0i2r0=6∈[1...6], 12de0i0r0=6∈[1...6], 16de0i0r0=4∈[1...4]]"),
                Arguments.of("concat(d6, d4)", List.of(), "64", "[7de0i0r0=6∈[1...6], 11de0i0r0=4∈[1...4]]"),
                Arguments.of("exp(d6, d6)", List.of(6, 6, 6, 5), "6, 6, 5", "[4de0i0r0=6∈[1...6], 4de1i0r0=6∈[1...6], 4de2i0r0=5∈[1...6], 8de0i0r0=6∈[1...6]]"),
                Arguments.of("exp(d6, d6, d6)", List.of(6, 6, 6, 6, 5), "6, 6, 5", "[4de0i0r0=6∈[1...6], 4de1i0r0=6∈[1...6], 4de2i0r0=5∈[1...6], 8de0i0r0=6∈[1...6], 12de0i0r0=6∈[1...6]]"),
                Arguments.of("groupC(3d6)", List.of(), "3x6", "[8de0i0r0=6∈[1...6], 8de0i1r0=6∈[1...6], 8de0i2r0=6∈[1...6]]"),
                Arguments.of("if(d6=?6,d4)", List.of(), "4", "[3de0i0r0=6∈[1...6], 9de0i0r0=4∈[1...4]]"),
                Arguments.of("if(d6=?6,d4,d10)", List.of(), "4", "[3de0i0r0=6∈[1...6], 9de0i0r0=4∈[1...4]]"),
                Arguments.of("if(d6=?5,d4,d10)", List.of(), "10", "[3de0i0r0=6∈[1...6], 12de0i0r0=10∈[1...10]]"),
                Arguments.of("if(d6=?6,d4,d6=?6,d10,d12)", List.of(), "4", "[3de0i0r0=6∈[1...6], 9de0i0r0=4∈[1...4]]"),
                Arguments.of("if(d6=?5,d4,d6=?6,d10,d12)", List.of(), "10", "[3de0i0r0=6∈[1...6], 12de0i0r0=6∈[1...6], 18de0i0r0=10∈[1...10]]"),
                Arguments.of("if(d6=?5,d4,d6=?5,d10,d12)", List.of(), "12", "[3de0i0r0=6∈[1...6], 12de0i0r0=6∈[1...6], 22de0i0r0=12∈[1...12]]"),
                Arguments.of("if(d6=?5,d4,d6=?6,d10)", List.of(), "10", "[3de0i0r0=6∈[1...6], 12de0i0r0=6∈[1...6], 18de0i0r0=10∈[1...10]]"),
                Arguments.of("if(d6=?5,d4,d6=?5,d10)", List.of(), "", "[3de0i0r0=6∈[1...6], 12de0i0r0=6∈[1...6]]"),
                Arguments.of("max(d6,d4)", List.of(), "6", "[4de0i0r0=6∈[1...6], 7de0i0r0=4∈[1...4]]"),
                Arguments.of("min(d6,d4)", List.of(), "4", "[4de0i0r0=6∈[1...6], 7de0i0r0=4∈[1...4]]"),
                Arguments.of("replace(d6,d6,d4)", List.of(), "4", "[8de0i0r0=6∈[1...6], 11de0i0r0=6∈[1...6], 14de0i0r0=4∈[1...4]]"),
                Arguments.of("asc(d6,d4,d6)", List.of(), "4, 6, 6", "[4de0i0r0=6∈[1...6], 7de0i0r0=4∈[1...4], 10de0i0r0=6∈[1...6]]"),
                Arguments.of("desc(d6,d4,d6)", List.of(), "6, 6, 4", "[5de0i0r0=6∈[1...6], 8de0i0r0=4∈[1...4], 11de0i0r0=6∈[1...6]]"),
                Arguments.of("val('$1',d6), '$1' + '$1'", List.of(), "6, 6", "[9de0i0r0=6∈[1...6]]"),
                Arguments.of("val('$1',d6), val('$1',d4), '$1'", List.of(), "4", "[9de0i0r0=6∈[1...6], 23de0i0r0=4∈[1...4]]"),
                Arguments.of("val('$1',d6), val('$2',d4 + '$1'), '$2'", List.of(), "4, 6", "[9de0i0r0=6∈[1...6], 23de0i0r0=4∈[1...4]]"),
                Arguments.of("val('$s',1), if(0=?1, '') + '$s'", List.of(), "1", "[]"),
                Arguments.of("val('$s',1) if(0=?1, '') '$s'", List.of(), "1", "[]"),

                Arguments.of("colorOn(3d6,[1/2],'white')", List.of(6, 2, 1), "6, 2-c:white, 1-c:white", "[9de0i0r0=6∈[1...6], 9de0i1r0=2-c:white∈[1...6], 9de0i2r0=1-c:white∈[1...6]]"),
                Arguments.of("colorOn(3d6 col 'red', [1/2],'')", List.of(6, 2, 1), "6-c:red, 2, 1", "[9de0i0r0=6-c:red∈[1...6], 9de0i1r0=2∈[1...6], 9de0i2r0=1∈[1...6]]"),
                Arguments.of("colorOn('',[1/2],'white')", List.of(), "", "[]"),
                Arguments.of("colorOn(val('a',1),1,'white')", List.of(), "", "[]"),
                Arguments.of("colorOn(3d6,[1/2],'white')", List.of(6, 5, 5), "6, 5, 5", "[9de0i0r0=6∈[1...6], 9de0i1r0=5∈[1...6], 9de0i2r0=5∈[1...6]]"),
                Arguments.of("colorOn(4d6 col 'red',[1/2],'white', 3, 'black', 1, 'green')", List.of(4, 3, 2, 1), "4-c:red, 3-c:black, 2-c:white, 1-c:green", "[9de0i0r0=4-c:red∈[1...6], 9de0i1r0=3-c:black∈[1...6], 9de0i2r0=2-c:white∈[1...6], 9de0i3r0=1-c:green∈[1...6]]"),

                //systems
                Arguments.of(THE_ONE_RING, List.of(), " ⬟= ᚠ", "[34de0i0r0=12∈[1...12]]"),
                Arguments.of(VAMPIRE_V5, List.of(), "successes: 12 messy critical", "[10de0i0r0=10∈[1...10], 10de0i1r0=10∈[1...10], 10de0i2r0=10∈[1...10], 25de0i0r0=10∈[1...10], 25de0i1r0=10∈[1...10], 25de0i2r0=10∈[1...10]]")
        );
    }

    private static String getRandomElementsString(Roll roll) {
        return groupRandomElements(roll).stream()
                .map(l -> l.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList())
                .map(List::toString)
                .collect(Collectors.joining(" "));
    }

    private static List<List<RandomElement>> groupRandomElements(Roll roll) {
        List<RollId> rollIds = roll.getRandomElementsInRoll().stream()
                .map(RandomElement::getDieId)
                .map(DieId::getRollId)
                .distinct()
                .sorted()
                .toList();

        Map<RollId, List<RandomElement>> rollIdListMap = roll.getRandomElementsInRoll().stream()
                .collect(Collectors.groupingBy(r -> r.getDieId().getRollId()));

        return rollIds.stream()
                .map(rid -> rollIdListMap.get(rid).stream().sorted(Comparator.comparing(RandomElement::getDieId)).toList())
                .toList();
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2},{3}")
    @MethodSource("generateStringDiceDataWithRandomElements")
    void rollDiceExpressionWithRandomElements(String diceExpression, List<Integer> diceNumbers, String expectedResult, String expectedRandomElements) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceNumbers), 1000, 10_000, true);
        RollResult res = underTest.evaluate(diceExpression);

        String result = res.getRolls().stream().map(Roll::getResultStringWithTagAndColor).collect(Collectors.joining(", "));
        String randomElements = res.getAllRandomElements().toString();
        //String diceNumbersOut = diceNumbers.stream().map(String::valueOf).collect(Collectors.joining(", "));
        //System.out.printf("Arguments.of(\"%s\", List.of(%s), \"%s\", \"%s\"),%n", diceExpression, diceNumbersOut, result, randomElements);


        SoftAssertions.assertSoftly(s -> {
            s.assertThat(result).isEqualTo(expectedResult);
            s.assertThat(randomElements).isEqualTo(expectedRandomElements);

            //x will not be part of the result expression
            if (!diceExpression.contains("x")) {
                s.assertThat(res.getExpression().replace(" ", "")).isEqualTo(diceExpression.replace(" ", ""));
            }
        });
    }

    @Test
    void debug() throws ExpressionException {
        GivenNumberSupplier numberSupplier = new GivenNumberSupplier(3, 1, 2);
        DiceEvaluator underTest = new DiceEvaluator(numberSupplier, 1000, 10_000, true);

        // List<Roll> res = underTest.evaluate("3d!6+(2r(2d8))");
        RollResult res = underTest.evaluate("colorOn(3d6,[1/2],'white')");
        System.out.println(res.getRolls().size());
        res.getRolls().forEach(System.out::println);
        res.getRolls().forEach(r -> System.out.println(r.getResultString()));
        System.out.println(res);
        System.out.println(res.getExpression());
        System.out.println(res.getAllRandomElements());
        res.getRolls().forEach(r -> System.out.println(r.getRandomElementsInRoll()));
        res.getRolls().forEach(r -> System.out.println(getRandomElementsString(r)));
        System.out.println(res.getRolls().stream().flatMap(r -> r.getElements().stream()).map(RollElement::getValue).toList());
    }

    @Test
    void giveDiceNumber() throws ExpressionException {
        GivenDiceNumberSupplier givenDiceNumberSupplier = new GivenDiceNumberSupplier(new GivenNumberSupplier(), List.of(
                DieIdAndValue.of(DieId.of(1, "d", 0, 2, 0), 4),
                DieIdAndValue.of(DieId.of(9, "d", 1, 1, 0), 1))
        );
        DiceEvaluator underTest = new DiceEvaluator(givenDiceNumberSupplier, 1000, 10_000, true);

        RollResult res = underTest.evaluate("3d6+(2r(2d8))");

        assertThat(res.getRolls().size()).isEqualTo(1);
        assertThat(values(res.getRolls())).containsExactly("6", "6", "4", "8", "8", "8", "1");
        assertThat(res.getAllRandomElements().stream()
                .toList().toString())
                .isEqualTo("[1de0i0r0=6∈[1...6], 1de0i1r0=6∈[1...6], 1de0i2r0=4∈[1...6], 9de0i0r0=8∈[1...8], 9de0i1r0=8∈[1...8], 9de1i0r0=8∈[1...8], 9de1i1r0=1∈[1...8]]");
        assertThat((res.getRolls().stream().map(Roll::getRandomElementsInRoll).flatMap(r -> r.stream()
                .map(re -> re.getDieId() + "=" + re.getRollElement().getValue())))).containsExactly(
                "1de0i0r0=6",
                "1de0i1r0=6",
                "1de0i2r0=4",
                "9de0i0r0=8",
                "9de0i1r0=8",
                "9de1i0r0=8",
                "9de1i1r0=1");
        assertThat(getRandomElementsString(res.getRolls().getFirst())).isEqualTo("[6, 6, 4] [8, 8] [8, 1]");
    }

    @Test
    void giveDiceNumberRandom() throws ExpressionException {
        GivenDiceNumberSupplier givenDiceNumberSupplier = new GivenDiceNumberSupplier(List.of(
                DieIdAndValue.of(DieId.of(1, "d", 0, 2, 0), 10),
                DieIdAndValue.of(DieId.of(9, "d", 1, 1, 0), 11))
        );
        DiceEvaluator underTest = new DiceEvaluator(givenDiceNumberSupplier, 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("3d6+(2r(2d8))").getRolls();

        assertThat(res.size()).isEqualTo(1);
        assertThat(values(res)).contains("10", "11");
        assertThat(res.getFirst().getRandomElementsInRoll().stream()
                .map(Objects::toString))
                .contains("1de0i2r0=10∈[1...6]", "9de1i1r0=11∈[1...8]");
    }

    @Test
    void giveDiceNumber_additionalyStoredValue() throws ExpressionException {
        GivenDiceNumberSupplier givenDiceNumberSupplier = new GivenDiceNumberSupplier(new GivenNumberSupplier(), List.of(
                DieIdAndValue.of(DieId.of(1, "d", 0, 2, 0), 4),
                DieIdAndValue.of(DieId.of(9, "d", 1, 1, 0), 1),
                DieIdAndValue.of(DieId.of(10, "d", 1, 1, 0), 1))
        );
        DiceEvaluator underTest = new DiceEvaluator(givenDiceNumberSupplier, 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("3d6+(2r(2d8))").getRolls();

        assertThat(res.size()).isEqualTo(1);
        assertThat(values(res)).containsExactly("6", "6", "4", "8", "8", "8", "1");
        assertThat(res.getFirst().getRandomElementsInRoll().stream()
                .toList().toString())
                .isEqualTo("[1de0i0r0=6∈[1...6], 1de0i1r0=6∈[1...6], 1de0i2r0=4∈[1...6], 9de0i0r0=8∈[1...8], 9de0i1r0=8∈[1...8], 9de1i0r0=8∈[1...8], 9de1i1r0=1∈[1...8]]");
        assertThat((res.stream().map(Roll::getRandomElementsInRoll).flatMap(r -> r.stream()
                .map(re -> re.getDieId() + "=" + re.getRollElement().getValue())))).containsExactly(
                "1de0i0r0=6",
                "1de0i1r0=6",
                "1de0i2r0=4",
                "9de0i0r0=8",
                "9de0i1r0=8",
                "9de1i0r0=8",
                "9de1i1r0=1");
        assertThat(getRandomElementsString(res.getFirst())).isEqualTo("[6, 6, 4] [8, 8] [8, 1]");
    }

    @Test
    void rollerRollIdentical() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(5, 5), 1000, 10_000, true);

        Roller roller = underTest.buildRollSupplier("1d6");

        assertThat(roller.roll()).isEqualTo(roller.roll());
    }

    @Test
    void valRandomElementsInCorrectOrder() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 3, 4, 5, 6), 1000, 10_000, true);

        RollResult res = underTest.evaluate("val('$r',color(1d9,'blue')) val('$h',color(1d10,'purple_dark')) val('$s',('$r'+'$h')>=6c) val('$rt','$r'==10c) val('$ht','$h'==10c) val('$ho','$h'==1c) val('$2s',((('$rt'+'$ht'=))/2)*2) val('$ts',('$s'+'$2s'=)) concat('successes: ', '$ts', ifE('$ts',0,ifG('$ho',1,' bestial failure' , ''),''), ifE('$rt' mod 2, 1, ifE('$ht' mod 2, 1, ' messy critical', ''), ''))");

        assertThat(values(res.getRolls())).containsExactly("successes: 0, blue:1, purple_dark:1");
        assertThat(res.getRolls().size()).isEqualTo(1);
        assertThat(res.getGroupedRandomElements().toString()).isEqualTo("[[16de0i0r0=1-c:blue∈[1...9]], [44de0i0r0=2-c:purple_dark∈[1...10]]]");
        assertThat(getRandomElementsString(res.getRolls().getFirst())).isEqualTo("[1] [2]");
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("resultSizeDate")
    void resultSize(String expression, int size) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate(expression).getRolls();

        assertThat(res).hasSize(size);
    }

    @Test
    void testColDontCopyRandomElements() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 3, 4, 5, 6), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("val('1',1d6) if('1' =? 1,  '1' col 'red', '1')").getRolls();

        assertThat(res.getFirst().getRandomElementsInRoll()).hasSize(1);
        assertThat(res.getFirst().getRandomElementsInRoll().getFirst().getRollElement().getColor()).isEqualTo("red");
    }

    @Test
    void testRegularDieHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("1000d999999999").getRolls();

        assertThat(res.getFirst().getElements()).hasSize(1000);
    }

    @Test
    void disableRollChildren() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0), 1000, 10_000, false);

        List<Roll> res = underTest.evaluate("(2d6=)d(2d6=)").getRolls();
        assertThat(res.getFirst().getElements()).hasSize(5);
        assertThat(res.getFirst().getRandomElementsInRoll()).hasSize(9);
        assertThat(res.getFirst().getChildrenRolls().stream().mapToLong(this::getNumberOfChildrenRolls).sum()).isEqualTo(0);
    }

    @Test
    void enableRollChildren() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("(2d6=)d(2d6=)").getRolls();
        assertThat(res.getFirst().getElements()).hasSize(5);
        assertThat(res.getFirst().getRandomElementsInRoll()).hasSize(9);
        assertThat(res.getFirst().getChildrenRolls().stream().mapToLong(this::getNumberOfChildrenRolls).sum()).isEqualTo(6);
    }


    @Test
    void testMaxResultElements() {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000, 10, false);

        assertThatThrownBy(() -> underTest.evaluate("10d10 + 5"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("To many elements in roll '10d10+5', max is 10 but there where 11")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(6, "+"));
    }

    @Test
    void testMaxRandomElements() {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000, 10, false);

        assertThatThrownBy(() -> underTest.evaluate("(6d6k1)+(6d6k1)"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("To many random elements in roll '(6d6k1)+(6d6k1)', max is 10 but there where 12")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(7, "+"));
    }

    private long getNumberOfChildrenRolls(Roll roll) {
        return roll.getChildrenRolls().size() + roll.getChildrenRolls().stream().mapToLong(this::getNumberOfChildrenRolls).sum();
    }

    @Test
    void testCustomDieHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("1000d[" + IntStream.range(0, 1000).mapToObj(i -> "1").collect(Collectors.joining("/")) + "]").getRolls();

        assertThat(res.getFirst().getElements()).hasSize(1000);
    }

    @Test
    void testExplodingDieMaxHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("1000d!999999999").getRolls();

        assertThat(res.getFirst().getElements()).hasSize(1000);
    }

    @Test
    void testExplodingDieTwoHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("1000d!2").getRolls();

        assertThat(res.getFirst().getElements()).hasSizeGreaterThan(1000);
    }

    @Test
    void testExplodingDieHeap() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        assertThatThrownBy(() -> underTest.evaluate("d!6"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("To many elements in roll 'd!6', max is 10000 but there where 10001")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(0, "d!"));

    }

    @Test
    void testExplodingDiceHeap() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        assertThatThrownBy(() -> underTest.evaluate("1000d!6"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("To many elements in roll '1000d!6', max is 10000 but there where 10001")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(4, "d!"));

    }

    @Test
    void testExplodingAddDieHeap() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        assertThatThrownBy(() -> underTest.evaluate("d!!6"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("To many elements in roll 'd!!6', max is 10000 but there where 10001")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(0, "d!!"));

    }

    @Test
    void testExplodingAddDiceHeap() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        assertThatThrownBy(() -> underTest.evaluate("1000d!!6"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("To many elements in roll '1000d!!6', max is 10000 but there where 10001")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(4, "d!!"));

    }


    @Test
    void testExplodingAddDieMaxHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("1000d!!999999999").getRolls();

        assertThat(res.getFirst().getElements()).hasSize(1000);
    }

    @Test
    void testExplodingAddDieTwoHeap() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("1000d!!2").getRolls();

        assertThat(res.getFirst().getElements()).hasSize(1000);
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("generateData")
    void rollExpression(String diceExpression, List<Integer> diceNumbers, List<Integer> expected) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceNumbers), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate(diceExpression).getRolls();

        assertThat(res.stream().flatMap(r -> r.getElements().stream()).flatMap(e -> e.asInteger().stream())).containsExactlyElementsOf(expected);
    }

    @Test
    void sortAsc() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 20, 1, 12), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("asc(4d20)").getRolls();

        assertThat(values(res)).containsExactly("1", "3", "12", "20");
    }

    @Test
    void sortDesc() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 20, 1, 12), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("desc(4d20)").getRolls();

        assertThat(values(res)).containsExactly("20", "12", "3", "1");
    }

    @Test
    void sortAlphaAsc() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 20, 1, 12), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("asc(4d20 + '5a' +'b')").getRolls();

        assertThat(values(res)).containsExactly("1", "3", "12", "20", "5a", "b");
    }

    @Test
    void sortAlphaDesc() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 20, 1, 12), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("desc(4d20 + '5a' + 'b')").getRolls();

        assertThat(values(res)).containsExactly("b", "5a", "20", "12", "3", "1");
    }

    @Test
    void groupCount() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        List<Roll> res = underTest.evaluate("groupC(4d20 + 10d10 + 3d6 + 10 + color(3d6,'red')+color(3d4,'black'))").getRolls();

        assertThat(res.stream().flatMap(r -> r.getElements().stream()).map(Object::toString)).containsExactly("11x10", "4x20", "black:3x4", "3x6", "red:3x6");
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("generateStringDiceData")
    void rollStringDiceExpression(String diceExpression, List<Integer> diceNumbers, List<String> expected) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceNumbers), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate(diceExpression).getRolls();

        assertThat(values(res)).containsExactlyElementsOf(expected);
        //val is not given in the dice expression because it is on a roll without result
        if (res.size() == 1 && !diceExpression.contains("val(")) {
            assertThat(res.getFirst().getExpression().replace(" ", "")).isEqualTo(diceExpression.replace(" ", ""));
        }
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("generateStringDiceDataColor")
    void rollStringDiceExpressionColor(String diceExpression, List<Integer> diceNumbers, List<String> expected, String expectedColor) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceNumbers), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate(diceExpression).getRolls();

        assertThat(values(res)).containsExactlyElementsOf(expected);
        assertThat(res.stream()
                .flatMap(e -> e.getRandomElementsInRoll().stream())
                .map(RandomElement::getRollElement)
                .map(RollElement::getColor)
        ).allMatch(expectedColor::equals);
    }


    @Test
    void integerDevide_NotSingleIntegerException() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(4, 1), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("2d6 / 3"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("'/' requires as left input a single integer but was '[4, 1]'. Try to sum the numbers together like (2d6=)")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(4, "/"));

    }

    @Test
    void decimalDevide_NotSingleDecimalException() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(4, 1), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("2d6 // 3"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("'//' requires as left input a single decimal but was '[4, 1]'. Try to sum the numbers together like (2d6=)")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(4, "//"));

    }

    @Test
    void divisorZero() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("10 / 0"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("/ by zero")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(3, "/"));
    }

    @ParameterizedTest(name = "{index} input:{0}, diceRolls:{1} -> {2}")
    @MethodSource("generateRandomDiceData")
    void getRandomElements(String expression, List<Integer> diceThrows, List<List<String>> expectedRandomElements) throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(diceThrows), 1000, 10_000, true);
        RollResult res = underTest.evaluate(expression);

        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactlyElementsOf(expectedRandomElements);
    }

    @Test
    void getRandomElements_regularDice() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 4, 4, 5), 1000, 10_000, true);
        RollResult res = underTest.evaluate("(2d4=)d6");

        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactly(List.of("1", "2"), List.of("4", "4", "5"));

        assertThat(res.getGroupedRandomElements().stream()
                .map(r -> r.stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(4, 4), List.of(6, 6, 6));
    }

    @Test
    void getRandomElements_customDice() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 4, 4, 5), 1000, 10_000, true);
        RollResult res = underTest.evaluate("(2d4=)d[a/b/c/d/e/f]");

        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactly(List.of("1", "2"), List.of("d", "d", "e"));

        assertThat(res.getGroupedRandomElements().toString())
                .isEqualTo("[[2de0i0r0=1∈[1...4], 2de0i1r0=2∈[1...4]], [6de0i0r0=d∈[a, b, c, d, e, f], 6de0i1r0=d∈[a, b, c, d, e, f], 6de0i2r0=e∈[a, b, c, d, e, f]]]");
    }

    @Test
    void getRandomElements_value() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(5, 10, 11), 1000, 10_000, true);
        RollResult res = underTest.evaluate("val('$1',d100) ifG('$1', 95, (d100 + '$1'=), ifL('$1', 6, ('$1' - d100=)))");

        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getRolls().getFirst().getElements().stream().map(RollElement::getValue)).containsExactly("-6");

        assertThat(res.getAllRandomElements().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getValue))
                .containsExactly("5", "10", "11");

    }


    @Test
    void getRandomElements_explode() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 2, 8, 3, 4), 1000, 10_000, true);
        RollResult res = underTest.evaluate("exp(2d6,1d8,1d10)");


        assertThat(res.getRolls().stream().flatMap(r -> r.getElements().stream()).map(RollElement::getValue)).containsExactly("1", "2", "3", "4");

        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactly(List.of("1", "2"), List.of("3", "4"), List.of("2"), List.of("8"));

        assertThat(res.getGroupedRandomElements().stream()
                .map(r -> r.stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(6, 6), List.of(6, 6), List.of(8), List.of(10));
    }

    @Test
    void getRandomElements_replace_no_match() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 5), 1000, 10_000, true);
        RollResult res = underTest.evaluate("replace(3d6,[6],2d20)");


        assertThat(res.getRolls().stream().flatMap(r -> r.getElements().stream()).map(RollElement::getValue)).containsExactly("1", "2", "5");

        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactly(List.of("1", "2", "5"));

        assertThat(res.getGroupedRandomElements().stream()
                .map(r -> r.stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(6, 6, 6));
    }

    @Test
    void getRandomElements_replace() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 5, 19, 18, 17, 16), 1000, 10_000, true);
        RollResult res = underTest.evaluate("replace(3d6,[1,5],2d20)");

        assertThat(res.getRolls().stream().flatMap(r -> r.getElements().stream()).map(RollElement::getValue)).containsExactly("19", "18", "2", "17", "16");

        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactly(List.of("1", "2", "5"), List.of("19", "18"), List.of("17", "16"));

        assertThat(res.getGroupedRandomElements().stream()
                .map(r -> r.stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(6, 6, 6), List.of(20, 20), List.of(20, 20));
    }

    @Test
    void getRandomElements_replaceRollFind() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 1, 2, 3), 1000, 10_000, true);
        RollResult res = underTest.evaluate("replace(1d6, 1d6, 1d6, 1d8, 1d10)");

        assertThat(res.getRolls().stream().flatMap(r -> r.getElements().stream()).map(RollElement::getValue)).containsExactly("2");

        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactly(List.of("1"), List.of("1"), List.of("2"), List.of("3"));

        assertThat(res.getGroupedRandomElements().stream()
                .map(r -> r.stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(6), List.of(6), List.of(6), List.of(8));
    }

    @Test
    void separatorTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2), 1000, 10_000, true);
        RollResult res = underTest.evaluate("1d6,1d8");

        assertThat(res.getRolls()).hasSize(2);
        assertThat(res.getRolls().getFirst().getElements().stream().map(RollElement::getValue)).containsExactly("1");
        assertThat(res.getRolls().get(1).getElements().stream().map(RollElement::getValue)).containsExactly("2");

        assertThat(res.getAllRandomElements().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getValue))
                .containsExactly("1", "2");
        assertThat(res.getRolls().stream().map(Roll::getExpression))
                .containsExactly("1d6", "1d8");
    }

    @Test
    void spaceSeparatorTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2), 1000, 10_000, true);
        RollResult res = underTest.evaluate("1d6 1d8");

        assertThat(res.getRolls()).hasSize(2);
        assertThat(res.getRolls().getFirst().getElements().stream().map(RollElement::getValue)).containsExactly("1");
        assertThat(res.getRolls().get(1).getElements().stream().map(RollElement::getValue)).containsExactly("2");

        assertThat(res.getAllRandomElements().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getValue))
                .containsExactly("1", "2");
        assertThat(res.getRolls().stream().map(Roll::getExpression))
                .containsExactly("1d6", "1d8");
    }

    @Test
    void getRandomElements_Repeat() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 3, 4, 5), 1000, 10_000, true);
        RollResult res = underTest.evaluate("5x1d6");

        assertThat(res.getRolls()).hasSize(5);
        assertThat(res.getRolls().getFirst().getElements().stream().map(RollElement::getValue)).containsExactly("1");
        assertThat(res.getRolls().get(1).getElements().stream().map(RollElement::getValue)).containsExactly("2");
        assertThat(res.getRolls().get(2).getElements().stream().map(RollElement::getValue)).containsExactly("3");
        assertThat(res.getRolls().get(3).getElements().stream().map(RollElement::getValue)).containsExactly("4");
        assertThat(res.getRolls().get(4).getElements().stream().map(RollElement::getValue)).containsExactly("5");

        assertThat(res.getAllRandomElements().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getValue))
                .containsExactly("1", "2", "3", "4", "5");
        assertThat(res.getRolls().stream().map(Roll::getExpression))
                .containsExactly("1d6", "1d6", "1d6", "1d6", "1d6");
    }

    @Test
    void getRandomElements_ListRepeat() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 3, 4, 5), 1000, 10_000, true);
        RollResult res = underTest.evaluate("5r1d6");

        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getRolls().getFirst().getElements().stream().map(RollElement::getValue)).containsExactly("1", "2", "3", "4", "5");

        assertThat(res.getAllRandomElements().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getValue))
                .containsExactly("1", "2", "3", "4", "5");
        assertThat(res.getRolls().getFirst().getExpression()).isEqualTo("5r1d6");
    }

    @Test
    void getRandomElements_reroll() throws ExpressionException {
        //roll 4d6, reroll 1s once, drop lowest, rolled three times

        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000, 10_000, true);
        RollResult res = underTest.evaluate("3x4d6rr1k3");

        assertThat(res.getRolls()).hasSize(3);
        assertThat(res.getRolls().getFirst().getElements().stream().map(RollElement::getValue)).containsExactly("6", "3", "1");
        assertThat(res.getRolls().get(1).getElements().stream().map(RollElement::getValue)).containsExactly("6", "3", "3");
        assertThat(res.getRolls().get(2).getElements().stream().map(RollElement::getValue)).containsExactly("4", "4", "3");

        assertThat(getRandomElementsString(res.getRolls().getFirst())).isEqualTo("[2, 3, 1, 4] [1, 1, 6, 3]");
        assertThat(getRandomElementsString(res.getRolls().get(1))).isEqualTo("[2, 3, 6, 3]");
        assertThat(getRandomElementsString(res.getRolls().get(2))).isEqualTo("[3, 2, 4, 4]");
        assertThat(res.getRolls().stream().map(Roll::getExpression))
                .containsExactly("4d6rr1k3", "4d6rr1k3", "4d6rr1k3");

    }

    @Test
    void getRandomElements_explodingDice() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(2, 1, 4, 6, 6, 1, 5), 1000, 10_000, true);
        RollResult res = underTest.evaluate("(1d!2=)d!6");

        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getRolls().getFirst().getElements().stream().map(RollElement::getValue)).containsExactly("4", "6", "6", "1", "5");
        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactly(List.of("2", "1"), List.of("4", "6", "6", "1", "5"));
        assertThat(res.getGroupedRandomElements().stream()
                .map(r -> r.stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(2, 2), List.of(6, 6, 6, 6, 6));
        assertThat(res.getRolls().getFirst().getExpression()).isEqualTo("(1d!2=)d!6");
    }

    @Test
    void getRandomElements_explodingAddDice() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(2, 1, 4, 6, 6, 1, 5), 1000, 10_000, true);
        RollResult res = underTest.evaluate("(1d!!2=)d!!6");

        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getRolls().getFirst().getElements().stream().map(RollElement::getValue)).containsExactly("4", "13", "5");
        assertThat(res.getGroupedRandomElements().stream()
                .map(rl -> rl.stream()
                        .map(RandomElement::getRollElement)
                        .map(RollElement::getValue)
                        .toList()
                ))
                .containsExactly(List.of("2", "1"), List.of("4", "6", "6", "1", "5"));
        assertThat(res.getGroupedRandomElements().stream()
                .map(r -> r.stream().map(RandomElement::getMaxInc).collect(Collectors.toList())))
                .containsExactly(List.of(2, 2), List.of(6, 6, 6, 6, 6));
        assertThat(res.getRolls().getFirst().getExpression()).isEqualTo("(1d!!2=)d!!6");
    }

    @Test
    void toStringTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate("1d6 + 3d20 + 10 +min(2d6,3d4)").getRolls();

        assertThat(res).hasSize(1);
        assertThat(getRandomElementsString(res.getFirst())).isEqualTo("[3] [2, 1, 4] [6, 6] [4, 4, 4]");
        assertThat(res.getFirst().getResultString()).isEqualTo("3, 2, 1, 4, 10, 4, 4, 4");
        assertThat(res.getFirst().getExpression()).isEqualTo("1d6+3d20+10+min(2d6,3d4)");
    }

    @Test
    void toStringBracketTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate("3d(20 + 10=)").getRolls();

        assertThat(res).hasSize(1);
        assertThat(getRandomElementsString(res.getFirst())).isEqualTo("[3, 2, 1]");
        assertThat(res.getFirst().getResultString()).isEqualTo("3, 2, 1");
        assertThat(res.getFirst().getExpression()).isEqualTo("3d(20+10=)");
    }

    @Test
    void toStringColorTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000, 10_000, true);
        RollResult res = underTest.evaluate("color(1d6,'red') + color(3d20,'blue')");

        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getGroupedRandomElements().toString()).isEqualTo("[[7de0i0r0=3-c:red∈[1...6]], [26de0i0r0=2-c:blue∈[1...20], 26de0i1r0=1-c:blue∈[1...20], 26de0i2r0=4-c:blue∈[1...20]]]");
        assertThat(getRandomElementsString(res.getRolls().getFirst())).isEqualTo("[3] [2, 1, 4]");
        assertThat(res.getRolls().getFirst().getResultString()).isEqualTo("red:3, blue:2, blue:1, blue:4");
        assertThat(res.getRolls().getFirst().getExpression()).isEqualTo("color(1d6,'red')+color(3d20,'blue')");
    }

    @Test
    void diceIdTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(5, 5), 1000, 10_000, true);
        RollResult res = underTest.evaluate("  d6 + ' test  t t' + d!6  ");

        assertThat(res.getRolls()).hasSize(1);
        DieId diceId1 = res.getRolls().getFirst().getRandomElementsInRoll().getFirst().getDieId();
        DieId diceId2 = res.getRolls().getFirst().getRandomElementsInRoll().getLast().getDieId();
        assertThat(diceId1.toString()).isEqualTo("0de0i0r0");
        assertThat(diceId2.toString()).isEqualTo("20d!e0i0r0");
        assertThat(res.getExpression()).isEqualTo("d6 + ' test  t t' + d!6");
        assertThat(res.getExpression().substring(diceId1.getRollId().getExpressionPosition().getStartInc(), diceId1.getRollId().getExpressionPosition().getStartInc() + diceId1.getRollId().getExpressionPosition().getValue().length())).isEqualTo(diceId1.getRollId().getExpressionPosition().getValue());
        assertThat(res.getExpression().substring(diceId2.getRollId().getExpressionPosition().getStartInc(), diceId2.getRollId().getExpressionPosition().getStartInc() + diceId2.getRollId().getExpressionPosition().getValue().length())).isEqualTo(diceId2.getRollId().getExpressionPosition().getValue());
    }


    @Test
    void colTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000, 10_000, true);
        RollResult res = underTest.evaluate("1d6 col 'red' +  3d20 col 'blue'");

        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getGroupedRandomElements().toString()).isEqualTo("[[1de0i0r0=3-c:red∈[1...6]], [18de0i0r0=2-c:blue∈[1...20], 18de0i1r0=1-c:blue∈[1...20], 18de0i2r0=4-c:blue∈[1...20]]]");
        assertThat(res.getRolls().getFirst().getRandomElementsInRoll().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getColor)).containsExactly("red", "blue", "blue", "blue");
        assertThat(res.getRolls().getFirst().getRandomElementsInRoll().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getTag)).containsExactly("", "", "", "");
        assertThat(getRandomElementsString(res.getRolls().getFirst())).isEqualTo("[3] [2, 1, 4]");
        assertThat(res.getRolls().getFirst().getResultString()).isEqualTo("3, 2, 1, 4");
        assertThat(res.getRolls().getFirst().getExpression()).isEqualTo("1d6col'red'+3d20col'blue'");
    }

    @Test
    void tagTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000, 10_000, true);
        RollResult res = underTest.evaluate("1d6 tag 'red' +  3d20 tag 'blue'");

        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getGroupedRandomElements().toString()).isEqualTo("[[1de0i0r0=3∈[1...6]], [18de0i0r0=2∈[1...20], 18de0i1r0=1∈[1...20], 18de0i2r0=4∈[1...20]]]");
        assertThat(getRandomElementsString(res.getRolls().getFirst())).isEqualTo("[3] [2, 1, 4]");
        assertThat(res.getRolls().getFirst().getRandomElementsInRoll().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getColor)).containsExactly("", "", "", "");
        assertThat(res.getRolls().getFirst().getRandomElementsInRoll().stream()
                .map(RandomElement::getRollElement)
                .map(RollElement::getTag)).containsExactly("", "", "", "");
        assertThat(res.getRolls().getFirst().getResultString()).isEqualTo("red:3, blue:2, blue:1, blue:4");
        assertThat(res.getRolls().getFirst().getExpression()).isEqualTo("1d6tag'red'+3d20tag'blue'");
    }

    @Test
    void toStringValColorTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate("val('$r', 1d6) color('$r','red') + color('$r','blue')").getRolls();

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getResultString()).isEqualTo("red:3, blue:3");
        assertThat(res.getFirst().getRandomElementsInRoll().toString()).isEqualTo("[11de0i0r0=3-c:blue∈[1...6]]");
        assertThat(getRandomElementsString(res.getFirst())).isEqualTo("[3]");
        assertThat(res.getFirst().getExpression()).isEqualTo("val('$r',1d6), color('$r','red')+color('$r','blue')");
    }

    @Test
    void toStringValTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate("val('$r', 1d6) '$r' + '$r'").getRolls();

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getResultString()).isEqualTo("3, 3");
        assertThat(res.getFirst().getRandomElementsInRoll().toString()).isEqualTo("[11de0i0r0=3∈[1...6]]");
        assertThat(getRandomElementsString(res.getFirst())).isEqualTo("[3]");
        assertThat(res.getFirst().getExpression()).isEqualTo("val('$r',1d6), '$r'+'$r'");
    }

    @Test
    void ifBoolTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate("val('$r',1d6) if('$r'=?1,'a','$r'=?2,'b','c')").getRolls();

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getResultString()).isEqualTo("c");
        assertThat(res.getFirst().getRandomElementsInRoll().toString()).isEqualTo("[10de0i0r0=3∈[1...6]]");
        assertThat(getRandomElementsString(res.getFirst())).isEqualTo("[3]");
        assertThat(res.getFirst().getExpression()).isEqualTo("val('$r',1d6), if('$r'=?1,'a','$r'=?2,'b','c')");
    }

    @Test
    void toStringMultiExpressionTest() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(3, 2, 1, 4), 1000, 10_000, true);
        List<Roll> res = underTest.evaluate("1d6 + 3d20, 10 +min(2d6,3d4)").getRolls();

        assertThat(res).hasSize(2);
        assertThat(getRandomElementsString(res.getFirst())).isEqualTo("[3] [2, 1, 4]");
        assertThat(res.getFirst().getResultString()).isEqualTo("3, 2, 1, 4");
        assertThat(res.getFirst().getExpression()).isEqualTo("1d6+3d20");
        assertThat(getRandomElementsString(res.get(1))).isEqualTo("[6, 6] [4, 4, 4]");
        assertThat(res.get(1).getResultString()).isEqualTo("10, 4, 4, 4");
        assertThat(res.get(1).getExpression()).isEqualTo("10+min(2d6,3d4)");
    }

    @Test
    void maxDice() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        assertThatThrownBy(() -> underTest.evaluate("1001d6"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("The number of dice must be less or equal then 1000 but was 1001")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(4, "d"));
    }

    @Test
    void maxNegativeDice() {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("-1001d6"))
                .isInstanceOf(ExpressionException.class)
                .hasMessage("The number of dice must be less or equal then 1000 but was 1001")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(5, "d"));

    }

    @ParameterizedTest(name = "{index} {0} -> {1}")
    @MethodSource("generateErrorData")
    void testError(String input, String expectedMessage) {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate(input))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void testNoMatch_start() {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("asefa+78901"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("No matching operator for 'asefa', non-functional text and value names must to be surrounded by '' or []")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(0, "asefa"));

    }

    @Test
    void testNoMatch_middle() {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("123456+asefa+78901"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("No matching operator for 'asefa', non-functional text and value names must to be surrounded by '' or []")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(7, "asefa"));

    }

    @Test
    void testNoMatch_middle_returnOnlyFirstError() {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("123456+asefa999999999999999999+1"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("No matching operator for 'asefa', non-functional text and value names must to be surrounded by '' or []")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(7, "asefa"));

    }

    @Test
    void testNoMatch_end() {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("123456+asefa"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("No matching operator for 'asefa', non-functional text and value names must to be surrounded by '' or []")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(7, "asefa"));

    }

    @Test
    void testNoMatch_all() {
        DiceEvaluator underTest = new DiceEvaluator(new RandomNumberSupplier(0L), 1000, 10_000, true);
        assertThatThrownBy(() -> underTest.evaluate("asefa"))
                .isInstanceOfAny(ExpressionException.class)
                .hasMessage("No matching operator for 'asefa', non-functional text and value names must to be surrounded by '' or []")
                .extracting(e -> ((ExpressionException) e).getExpressionPosition()).isEqualTo(ExpressionPosition.of(0, "asefa"));

    }

    @Test
    void rollTwice_1d6() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2), 1000, 10_000, true);

        Roller res = underTest.buildRollSupplier("1d6");

        assertThat(res.roll().getRolls().getFirst().getElements().toString()).isEqualTo("[1]");
        assertThat(res.roll().getRolls().getFirst().getElements().toString()).isEqualTo("[2]");
    }

    @Test
    void rollTwice_value() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(1, 2, 3, 2, 3, 4), 1000, 10_000, true);

        Roller res = underTest.buildRollSupplier("val('$1', 3d6), (('$1')=) + (('$1'>2)c)");


        assertThat(res.roll().getRolls().getFirst().getElements().toString()).isEqualTo("[6, 1]");
        assertThat(res.roll().getRolls().getFirst().getElements().toString()).isEqualTo("[9, 2]");
    }

    @Test
    void testHelp() {
        assertThat(DiceEvaluator.getHelpText())
                .contains("Regular Dice");
    }

    @ParameterizedTest(name = "{index} expression:{0} -> {1}")
    @MethodSource("generateHasOperatorOrFunction")
    void resultSize(String expression, boolean hasOperatorOrFunction) {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        assertThat(underTest.expressionContainsOperatorOrFunction(expression)).isEqualTo(hasOperatorOrFunction);
    }

    @Test
    void overwriteNumberSupplier() throws ExpressionException {
        DiceEvaluator underTest = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        Roller roller = underTest.buildRollSupplier("2d6");
        RollResult res = roller.roll();
        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getRolls().getFirst().getResultString()).isEqualTo("6, 6");

        RollResult res2 = roller.roll(new GivenNumberSupplier(3, 3));
        assertThat(res2.getRolls()).hasSize(1);
        assertThat(res2.getRolls().getFirst().getResultString()).isEqualTo("3, 3");

        RollResult res3 = roller.roll(new GivenNumberSupplier(1, 1));
        assertThat(res3.getRolls()).hasSize(1);
        assertThat(res3.getRolls().getFirst().getResultString()).isEqualTo("1, 1");

    }

    @Test
    void givenDiceNumberSupplierCustomDice() throws ExpressionException {
        DiceEvaluator diceEvaluator = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, true);

        Roller roller = diceEvaluator.buildRollSupplier("2d[a/b/c/d]");
        RollResult res = roller.roll();
        assertThat(res.getRolls()).hasSize(1);
        assertThat(res.getRolls().getFirst().getResultString()).isEqualTo("d, d");

        RollResult res2 = roller.roll(new GivenNumberSupplier(1, 1));
        assertThat(res2.getRolls()).hasSize(1);
        assertThat(res2.getRolls().getFirst().getResultString()).isEqualTo("a, a");

        List<DieIdAndValue> givenRolls = res.getRolls().getFirst().getRandomElementsInRoll().stream()
                .map(RandomElement::getDiceIdAndValue)
                .toList();

        GivenDiceNumberSupplier underTest = new GivenDiceNumberSupplier(new GivenNumberSupplier(1, 1), givenRolls);


        RollResult res3 = roller.roll(underTest);
        assertThat(res3.getRolls()).hasSize(1);
        assertThat(res3.getRolls().getFirst().getResultString()).isEqualTo("d, d");

    }

    @Test
    void keepChildrenRollsFalse() throws ExpressionException {
        DiceEvaluator diceEvaluator = new DiceEvaluator(new GivenNumberSupplier(), 1000, 10_000, false);
        RollResult res = diceEvaluator.evaluate("(d2)d6");

        assertThat(res.getRolls().getFirst().getChildrenRolls()).isEmpty();
    }


}
