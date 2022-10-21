[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![codecov](https://codecov.io/gh/twonirwana/DiceEvaluator/branch/main/graph/badge.svg?token=TTBM46YQFT)](https://codecov.io/gh/twonirwana/DiceEvaluator)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.twonirwana/dice-evaluator)](https://search.maven.org/artifact/io.github.twonirwana/dice-evaluator)

# The Dice Evaluator

Dice [infix notation](https://en.wikipedia.org/wiki/Infix_notation) (aka calculator notation) expression evaluator,
using the [shunting yard algorithm](https://en.wikipedia.org/wiki/Shunting_yard_algorithm).
The dice can have numbers or text/symbols on them. Some operators work only on numbers. It is also possible to color
dice and dice with different colors will not be combined when given to operators or functions.
This implementation was inspired by [Javaluator](https://github.com/fathzer/javaluator).
The dice evaluator works on lists, summing the results together is optional.

## Goals

* Always get the dice results in the answer, even for complex expressions
* Clear defined operator precedence and correct bracket handling
* Support for custom dice sides with text or symbols instant of numbers
* Don't always give the sum of the dice as answer but working with lists
* Operators can handle, when possible and intuitive, lists of elements/numbers
* Custom exception for errors in the expression evaluation
* Usage of as few symbols as possible

## Development Decisions

* Exception over unintuitive behavior -> It is better that the user knows what is not working then that something is
  working, but they don't know what and why
* not every operator can handel lists -> to many operators are not intuitive:
    * is max on list the count or the sum of the elements
    * how to multiply and divide lists

# Usage

The evaluator processes dice expression and returns a list of results, each containing a list of elements. Elements
have a value (a number or a text) and can have a color. For example `2d6` rolls two six-sided dice and returns a list
with two elements, each with a value between 1 and 6. To get the sum of the roll, simple add a `=` at the end, for
example in this case `2d6=`.
List can be included into the expression by using brackets. For example `1d[2/2/4/4/6/6]`will a die which has two sides
with 2, two sides with 4 and two sides with 6. The result will be a list with one element, which has a value of 2, 4 or 6.

To use text in the expression it is often necessary to escape the text with `'`. For example `1d['head'/'tail']` will
flip a coin. Without the escape characters the d in head would be interpreted as dice operator.
Multiple expression can be separated by ','. For example `3d6, 4d8` will roll two six-sided dice and return a list with
two results, the first one containing the result elements of the `3d6` and the second one the result of the `4d8`.

Operators have a precedent, which is defined by the order of the operators in the table below. Operators with a higher
precedence are evaluated first. Brackets can be used to change the order of evaluation. For example `1d4+3d6` is the
union
of result of 1d4 and 3d6 but `(1d4+3=)d6)` gets first the sum of the result of 1d4 and 3 and then rolls this number of
d6.

## Operators

| Name                | Notation                           | Example                      | Description                                                                                                                                                                                                    | Precedent | Associativity                       | Left parameter          | Right parameter                    |
|---------------------|------------------------------------|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|-------------------------------------|-------------------------|------------------------------------|
| Sum                 | `<left> = `                        | `2d6=`                       | Sums the list of on the left side of the symbol                                                                                                                                                                | 0         | left                                | a list                  | -                                  |
| Union               | `<left> + <right>`                 | `2d6 + 2`                    | Combines the results of both sides to a single list                                                                                                                                                            | 1         | left                                | one or more value       | one or more value                  |
| Negative Union      | `<left> - <right>`                 | `2 - 1` or `-d6`             | Combines the results of both sides to a single list. The right side is multiplied by -1.                                                                                                                       | 2         | left for binary and right for unary | one or more value       | one or more numbers                |
| Multiply            | `<left> * <right>`                 | `2 * 6`                      | Multiplies the right number with the left number                                                                                                                                                               | 3         | left                                | a single number         | a single number                    |
| Divide              | `<left> / <right>`                 | `4 / 2`                      | Divides the right number with the left number                                                                                                                                                                  | 4         | left                                | a single number         | a single number                    |
| Count               | `<list> c`                         | `3d6>3c`                     | Counts the number of elements in a list                                                                                                                                                                        | 5         | left                                | a list                  | -                                  |
| Greater Then Filter | `<list> > <number>`                | `3d6>3`                      | Keeps only the elements of the left list that are  bigger as the right number                                                                                                                                  | 6         | left                                | one or more numbers     | a single number                    |
| Lesser Then Filter  | `<list> < <number>`                | `3d6<3`                      | Keeps only the elements of the left list that are  lesser as the right number                                                                                                                                  | 7         | left                                | one or more numbers     | a single number                    |
| Keep Highest        | `<list> k <numberToKept>`          | `3d6k2`                      | keeps the the highest values out a list, like the result of multiple dice                                                                                                                                      | 8         | left                                | one or more numbers     | a single number                    |
| Keep Lowest         | `<list> l <numberToKept>`          | `3d6l2`                      | keeps the the lowest values out a list, like the result of multiple dice                                                                                                                                       | 9         | left                                | one or more numbers     | a single number                    |
| Exploding Add Dice  | `<numberOfDice>d!!<numberOfFaces>` | `3d!!6`                      | Throws dice and any time the max value of a die is rolled, that die is re-rolled and added to the die previous resul total. A result of the reroll the sum of the value.                                       | 10        | left for binary and right for unary | none or a single number | a single number                    |
| Exploding Dice      | `<numberOfDice>d!<numberOfFaces>`  | `4d!6` or `d!6`              | Throws dice and any time the max value of a die is rolled, that die is re-rolled and added to the dice set total. A reroll will be represented as two dice result values                                       | 11        | left for binary and right for unary | none or a single number | a single number                    |
| Regular Dice        | `<numberOfDice>d<numberOfFaces>`   | `3d20` or `d20` or 3d[2/4/8] | Throws a number of dice given by the left number. The number sides are given by the right number. If the right side a list, a element of the list is randomly picked. The result is a list with the dice throw | 12        | left for binary and right for unary | none or a single number | a single number or multiple values |

## Functions

| Name      | Notation                                 | Example            | Description                                                                                                                              |
|-----------|------------------------------------------|--------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| color     | `color(<expression>, <'color'>)`         | `color(4d6,'red')` | marks the elements of one inner expression with a text. All other operators will not combine elements of different colors.               |
| min       | `min(<expression1>, <expression2> ...)`  | `min(4d6)`         | returns the smallest elements (multiple if the smallest is not unique) of one or more inner expressions. Text is compared alphabetically |
| max       | `max(<expression1>, <expression2> ...)`  | `max(4d6)`         | returns the smallest elements (multiple if the smallest is not unique) of one or more inner expressions. Text is compared alphabetically |
| sort asc  | `asc(<expression1>, <expression2> ...)`  | `asc(4d6)`         | sorts all elements ascending of one or more inner expressions. Text is compared alphabetically                                           |
| sort desc | `desc(<expression1>, <expression2> ...)` | `desc(4d6)`        | sorts all elements descending of one or more inner expressions. Text is compared alphabetically                                          |

# TODO

* brackets into the result expression
* generalize the list expression [1/2/3/abc] to (1,2,3,abc)?
* group/aggegate operator
* intersection operator
* lesserEqual filter
* higherEqual filter
* Double evaluation for an expression to test for botch or critical success
* Functions
    * eats: elements of on set remove specific other elements (1s remove 10s)
    * AddIf/ifEquals/ifLower/ifHigher?
