# Hints

Open this file only when you are stuck. Each mission has the same four parts: the idea in a few words, the usual trap, how to see what is wrong, and how long it takes. The times are for a student who was at the lectures and works without long breaks. Together about 4 to 5 hours.

## How the lab console helps

Every mission has an animation on the page `output/outbreak.html`. Press Step and watch the algorithm on your own data. When your line has a red cross, watch the animation, then put a `System.out.println` in your method for the same step and compare what you print with what you see. Most mistakes are found in the first three steps where the two differ.

## Mission 1, mutations (edit distance), about 40 minutes

Idea: d[i][j] is the cheapest way to turn the first i letters of A into the first j letters of B. The last step was a replacement (or no change if the letters are equal), a deletion or an insertion. The first row and the first column are filled before the loops.
Trap: replacing a letter costs 1 (not 2). Off-by-one in the indexes: the table has one more row and one more column than the strings are long.
See it: the heatmap fills from the top left corner. If the corner of your table is bigger than the corner of the lab, your first row or column is wrong.

## Mission 2, shared core (LCS), about 30 minutes

Idea: the same table, but with a different rule. If the letters are equal, take the diagonal plus 1; otherwise take the better of the top and the left.
Trap: taking only one of the two neighbours in the "otherwise" case.
See it: on the ribbon picture, parallel lines are the common letters. If your number is smaller than the lab's, you lose some of them.

## Mission 3, the marker (KMP), about 1 hour

Idea: the prefix function pi[i] is the length of the longest border (a beginning that is also an ending) of the first i+1 letters of the marker. While searching you keep k, the number of marker letters matched; on a mismatch you go back to pi[k-1] instead of to the start.
Trap: after a full match do not stop and do not start from 0: go back to pi[m-1], because the next match may overlap the last one (marker AACAAAA is full of that).
See it: the animation shows the table pi and the pointer k. Compare it with an array printed from your prefixFunction.

## Mission 4, copied words (Rabin-Karp), about 45 minutes

Idea: the hash of a window of 12 letters is a number. When the window moves one letter, subtract the old letter times BASE^11, multiply by BASE, add the new letter, all modulo MOD. Put the hashes of all windows of B in a set; for every window of A look up its hash.
Trap 1: the modulo can make the number negative, add MOD before taking the remainder. Trap 2: equal hashes do not prove equal words: compare the letters before you count. Trap 3: count positions of A whose word is found in B, not different words (the same word at two positions counts twice).
See it: in the lab switch the hash size to tiny and watch the false alarms. If your number changes when you change MOD, you do not compare the letters.

## Mission 5, whom to test (DP on a tree), about 1 hour

Idea: root the tree in person 0. For every person keep two numbers: the cheapest cost of his subtree when he is tested, and when he is not tested. If he is not tested, every child must be tested. There are at most about 100 people, so a simple recursive DFS is fine; a loop over a BFS order, backwards, works too. The answer is the smaller of the two numbers at the root. To say who is tested, walk down from the root and use the same rule.
Longest chain: for every person find the two deepest branches below him; the best chain through him is their sum.
Trap: the tree has no direction in the file: a link `x y` does not say who is the parent. Root the tree in person 0 and keep track of where you came from, or you will walk in circles.
See it: on the page every person gets two numbers climbing from the leaves. Compare them with your arrays for a few people.

## Missions 6 and 7, git, README, timing, about 30 to 45 minutes

Commit after every mission with a message that says what changed. For the timings run `java -cp out Bench` and copy the numbers into the table. In the complexity table write the O() of your own code and one sentence why. The answer "I do not know" gets no points; a wrong but reasoned answer gets some.

## If nothing helps

Write to the lecturer in the group chat or come to the office hour. Send the mission, the line that is wrong, what you expect and what you get. That is a good question. "It does not work" is not.
