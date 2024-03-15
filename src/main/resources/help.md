| Name                 | Notation   | Example            |
|----------------------|------------|--------------------|
| Regular Dice         | d          | d6, 3d6            |
| Custom Dice          | d[x,y,...] | 3d[Head,Tail]      |
| Exploding Dice       | d!         | 4d!6               |
| Exploding Add Dice   | d!!        | 4d!!6              |
| Keep Highest         | k          | 3d6k2              |
| Keep Lowest          | l          | 3d6l2              |
| Sum                  | =          | 3d6=               |
| Add to List          | +          | 3d6+2d12           |
| Concatenate          | _          | 3d6 _ 'dmg'        |
| Negative add to List | -          | 3d6-2d12 or -2d6   |
| Decimal Divide       | //         | 2/6                |
| Divide               | /          | 12/6               |
| Multiply             | *          | 12*d6              |
| Modulo               | mod        | d6 mod 2           |
| Equal Filter         | ==         | 3d6==3             |
| Greater Then Filter  | >          | 3d6>3              |
| Lesser Then Filter   | <          | 3d6<3              |
| Greater Equal Filter | >=         | 3d6>=3             |
| Lesser Equal Filter  | <=         | 3d6<=3             |
| Lesser Equal Filter  | <=         | 3d6<=3             |
| Count                | c          | 3d6<3c             |
| Multiple Rolls       | ,          | 1d6,2d10           |
| Repeat               | x          | 3x2d6              |
| Repeat List          | r          | 3r2d6              |
| Reroll               | rr         | 3d10 rr 1          |
| Equal                | =?         | d6=?6              |
| Greater              | >?         | d6>?5              |
| Greater Equal        | >=?        | d6>=?5             |
| Lower                | <?         | d6<?4              |
| Lower Equal          | <=?        | d6<=?4             |
| In                   | in         | d6 in [1/3/5]      |
| And                  | &&         | d6>?1 && d6<5      |
| Or                   | ||         | d6>?1 || d6<5      |
| Negate               | !          | !d6>?1             |
| Color                | col        | d6 col 'red'       |
| Tag                  | tag        | d6 tag 'marked'    |
| Brackets             | ()         | (2d4=)d6           |
| Text                 | ''         | 'Damage ' + d6     |
| Sort asc             | asc()      | asc(10d10)         |
| Sort desc            | desc()     | desc(10d10)        |
| Min                  | min()      | min(3d4, 8)        |
| Max                  | max()      | max(3d4, 8)        |
| Chancel              | chancel()  | chancel(8d10,10,1) |
| Replace              | replace()  | replace(6d10,1,2)  |
| Explode              | exp()      | exp(d6,1,2)        |
| If                   | if()       | if(d6=?6,'Y','N')  |
| Group Count          | groupC()   | groupC(20d6)       |
| Concatenate          | concat()   | concat('Att:',d20) |
| Value                | val()      | val('$1',10d10)    |
