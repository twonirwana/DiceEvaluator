| Name                 | Notation   | Example            |
|----------------------|------------|--------------------|
| Regular Dice         | d          | d6, 3d6            |
| Custom Dice          | d[x/y/...] | 3d[Head/Tail]      |
| Exploding Dice       | d!         | 4d!6               |
| Exploding Add Dice   | d!!        | 4d!!6              |
| Keep Highest         | k          | 3d6k2              |
| Keep Lowest          | l          | 3d6l2              |
| Sum                  | =          | 3d6=               |
| Appending            | +          | 3d6+2d12           |
| Negative Appending   | -          | 3d6-2d12 or -2d6   |
| Divide               | /          | 12/6               |
| Multiply             | *          | 12*d6              |
| Greater Then Filter  | >          | 3d6>3              |
| Lesser Then Filter   | <          | 3d6<3              |
| Greater Equal Filter | >=         | 3d6>=3             |
| Lesser Equal Filter  | <=         | 3d6<=3             |
| Count                | c          | 3d6<3c             |
| Multiple Rolls       | ,          | 1d6,2d10           |
| Sort asc             | asc()      | asc(10d10)         |
| Sort desc            | desc()     | desc(10d10)        |
| Min                  | min()      | min(3d4, 8)        |
| Max                  | max()      | max(3d4, 8)        |
| Color                | color()    | color(3d4, 'red')  |
| Chancel              | chancel()  | chancel(8d10,10,1) |
| Double               | double()   | double(8d10, 10)   |
| ifEqual              | ifEqual()  | ifEqual(d6,6,Y,N)  |
