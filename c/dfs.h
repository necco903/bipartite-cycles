//
//  dfs.h
//  
//
//  Created by Patty Neckowicz on 7/18/13.
//
//

#ifndef _DFS_h
#define _DFS_h

#include <stdio.h>

// GRAPH COMPONENTS
struct Edge
{
    // vertices
    int right; 
    int left;
    
    int is_dummy;
    int direction; // must be in [-1, 1]
};


struct AdjNode
{
    struct Edge* edge;
    struct AdjNode* next;
    struct AdjNode* prev;
    struct AdjNode* twin; // the other node that has the same edge
    int end_node; // marks the end of a sublist in the adj list
    int in_avail; // 1 when the left vertex of the edge in this adjnode is available. This is only used for adjnodes on the right.
    
};

// For vertices on the left, priority is DUMMY, -1, 0. Keep track of +1 in separate list 
// For vertices on the right, priority is +1 edges to available, 0 edges to available, +1 edges to not available, 0 edges to not available. Do not keep track of -1.

struct AdjList
{
    int label; // the vertex 
        
    // for use by right vertices
    struct AdjNode* r_avail_1;
    struct AdjNode* r_avail_0;
    struct AdjNode* r_notavail_1;
    struct AdjNode* r_notavail_0;
    
    // for use of left vertices
    struct AdjNode* l_dummy; // head of list is dummy
    struct AdjNode* l_min1;
    struct AdjNode* l_0;
    struct AdjNode* l_plus1; // this linked list is not connected to the main list
};


struct Graph
{
    int V;
    int d;
    struct AdjList* array; // Array of adjacency lists
};

// SEARCH COMPONENTS

struct Stack {    

    struct StackNode* top;
    int length;
};

struct StackNode {
    
    int vertex;
    struct AdjNode* adj_node; // pointer to the node in the adjacency list of a vertex
    struct StackNode* next; // the next node in the stack
};

// Available vertices on the left, linked list
struct Available {
    int length; 
    struct AvailableNode* head;
};

struct AvailableNode {
    int vertex;
    struct AvailableNode* next;
    struct AvailableNode* prev;
    
};


// Predecessors are kept as a list of predecessor nodes
struct PredecessorNode {
    
    int search;
    struct AdjNode* adj_node; 
    
};

// FUNCTION HEADERS
// Creating/initializing graph
struct AdjNode* create_adjnode(struct Edge* edge, int end_node);
struct Edge* create_edge(int left, int right, int is_dummy);
struct Graph* init_dummy_graph(int V, int d);
int *create_right_sides(int N, int d);
struct Graph* create_dummy_graph(int N, int d, int* right_sides);
void free_graph(struct Graph* graph);

// DFS Search - biased towards available edges, towards direction zero for edges
int search(struct Graph* graph, FILE *results);
void biased_DFS(struct Graph* graph, int search_num, int start, int end, struct PredecessorNode** predecesssor, int* edges_visited);

// Stack operations
struct Stack* init_stack();
struct StackNode* pop_stack(struct Stack* stack);
void push_stack(struct Stack* stack, int vertex, struct AdjNode* adj_node);
void free_stack(struct Stack* stack);

// AdjNode operations
void splice(struct AdjNode* node);
void insert_front(struct AdjNode* adj_node, struct AdjNode* pointer);
void print_list(struct AdjNode* beginning, int vertex, int full_list);


int rand_lim(int limit);
#endif
