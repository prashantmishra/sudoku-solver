#Sudoku Solver

Hello there!

This is a JAVA implementation of the method discussed in <a href="http://norvig.com/sudoku.html">this article by Peter Norvig</a> which uses constraint-propogation and backtracking for solving any Sudoku puzzle. The input can be in the form of an image or a text file.

##Installating and running

Maven install : ```mvn install```

Run using an image as input : ```mvn exec:java -Dexec.args="0 {location of image file}"```

Run using a text file as input : ```mvn exec:java -Dexec.args="1 {location of text file}"```

##Example runs

###Image

Let's use this image as our input :

![img1](https://raw.githubusercontent.com/prashantmishra/Sudoku/master/src/main/resources/img.png)

We load and convert it to a binary image using adaptive-thresholding :

![img2](https://raw.githubusercontent.com/prashantmishra/Sudoku/master/src/main/resources/ImgMAT-1.png)

Then we find the largest connected area, assume it is our box,and fill out the rest :

![img3](https://raw.githubusercontent.com/prashantmishra/Sudoku/master/src/main/resources/ImgMAT-2.png)

After finding the edges, putting back the white pixels inside them which are our numbers and warping the image to make it a square :

![img4](https://raw.githubusercontent.com/prashantmishra/Sudoku/master/src/main/resources/ImgMAT-3.png)

For better processing though, we fill out the grid before we warp the image to a square, finally getting :

![img5](https://raw.githubusercontent.com/prashantmishra/Sudoku/master/src/main/resources/ImgMAT-4.png)

We divide this into 9x9 squares, extract the digits, compare them to our training set and find the nearest match (<a href="https://en.wikipedia.org/wiki/K-nearest_neighbors_algorithm">k-NN</a> with k=1) and finally solve the puzzle! The console output for the above image :

![solution](https://raw.githubusercontent.com/prashantmishra/Sudoku/master/src/main/resources/solution.png)

###Text

The same puzzle could have been denoted by the text file :

```
530070000
600195000
098000060
800060003
400803001
700020006
060000280
000419005
000080079
```

##Improvements

The most obvious improvements I can see is to :
* Make the grid detection process more robust. 
* Make digit recognition more precise.
