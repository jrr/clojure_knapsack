# doll-smuggler

Solution for the "Doll Smuggler" knapsack problem at https://github.com/micahalles/doll-smuggler

## Notes

I started with brute force because i) I love combinatorial explosion and ii) it'd be useful for developing (small) testcases. With the given testcase, the exhaustive solver runs for about 2.5 minutes before `java.lang.OutOfMemoryError` on my system.

The first heuristic approach that comes to mind is to sort the items by descending efficiency, and start loading the knapsack from the top of the list. (i.e., favor (value/weight).) I can easily contrive a counterexample, though, so I'll have to do better. (see `break_heuristics.input`)

To build a correct solution that runs in reasonable time, I turned to Wikipedia. I studied the [0/1 dynamic programming approach](http://en.wikipedia.org/wiki/Knapsack_problem#0.2F1_knapsack_problem) (not the pseudocode, but rather the recursive definitions above it) and executed it a couple times on paper to wrap my head around it.  Finally, with no small amount of println debugging, I managed to implement it in Clojure.

## Usage

Run it like this:

    $ lein run testcases/given.input

Or, to compare results for different approaches (brute force, heuristics, recursive):

    $ lein run -v testcases/break_heuristics.input
    max_weight:    200
    total weight:  551
    total value:   345
    
    by weight:          $65 (151)
    by value:           $100 (200)
    by efficiency:      $130 (171)
    exhaustively:       $180 (200)
    recursive solution: $180 (200)
    
    packed dolls:
    
    name weight value
    doc    100    90
    seth   100    90

To run all the tests:

    $ lein test
    
    lein test doll-smuggler.core-test
    
    Ran 9 tests containing 9 assertions.
    0 failures, 0 errors.

