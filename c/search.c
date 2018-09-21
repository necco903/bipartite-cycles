//
//  search.c
//  
//
//  Created by Patricia Neckowicz on 7/29/13.
//
//

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "dfs.h"

int main(int argc, char *argv[]) {
    
    assert (argc == 3);
    
    int N = atoi(argv[1]);
    int d = atoi(argv[2]);
    
    FILE *results;
    results = fopen("results.txt", "a");

    //fprintf(results, "DFS, biased towards available, edges to dir 0:\n");
    fprintf(results, "edges, edges_visited, edges_traversed, visited/edges, traversed/edges, max cycle edges/dummies");

    int i;
    int* right_sides;
    struct Graph* graph;

    for (N =100; N < 10000; N+=50) {
        for (i = 0; i < 1000; i++) { // 1000 iterations of each

            fprintf(results, "%d, ", N*d);
            right_sides = create_right_sides(N, d);
            graph = create_dummy_graph(N, d, right_sides);
        }
        
        search(graph, results); // will call dfs search

    }
    fclose(results);
    
    free(right_sides);
    free_graph(graph);
    
    return 0;
}