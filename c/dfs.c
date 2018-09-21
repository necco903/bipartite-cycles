//
//  dfs.c
//  
//
//  Created by Patricia Neckowicz on 7/18/13.
//
//

#include "dfs.h"
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

// A utility function to create a new adjacency list node
struct AdjNode* create_adjnode(struct Edge* edge, int end_node)
{
    struct AdjNode* node = (struct AdjNode*) malloc(sizeof(struct AdjNode));
    node->next = NULL;
    node->prev = NULL;
    node->edge = edge;
    node->twin = NULL;
    node->end_node = end_node;
    node->in_avail = 1; // initialize as in available
    return node;
}

// Creates a new edge, to be connected to two separate adj list nodes
struct Edge* create_edge(int left, int right, int is_dummy) {
    struct Edge* edge = (struct Edge*) malloc(sizeof(struct Edge));
    edge->left = left;
    edge->right = right;
    edge->is_dummy = is_dummy;
    edge->direction = 0;
    return edge;
}

// A utility function that creates a dummy graph of V vertices
struct Graph* init_dummy_graph(int V, int d)
{
    
    struct Graph* graph = (struct Graph*) malloc(sizeof(struct Graph));
    graph->V = V;
    graph->d = d;
    
    // Create an array of adjacency lists.  Size of array will be V
    graph->array = (struct AdjList*) malloc(V * sizeof(struct AdjList));
    
    // Initialize each adjacency list
    int i;
    for (i = 0; i < V; ++i) { 
        
        graph->array[i].label = i;
        
        if (i < V/2) {
            
            graph->array[i].r_avail_1 = NULL;
            graph->array[i].r_avail_0 = NULL;
            graph->array[i].r_notavail_1 = NULL;
            graph->array[i].r_notavail_0 = NULL;
            
            // we do not create an endnode for l_dummy because we know it will always be there
            graph->array[i].l_min1 = create_adjnode(NULL, 1);
            graph->array[i].l_0 = create_adjnode(NULL, 1);
            graph->array[i].l_plus1 = create_adjnode(NULL, 1); 
            
            graph->array[i].l_min1->next = graph->array[i].l_0;
            graph->array[i].l_0->prev = graph->array[i].l_min1;
        }
        
        else {
            
            graph->array[i].l_dummy = NULL;
            graph->array[i].l_min1 = NULL;
            graph->array[i].l_0 = NULL;
            graph->array[i].l_plus1 = NULL;
            
            graph->array[i].r_avail_1 = create_adjnode(NULL, 1);
            graph->array[i].r_avail_0 = create_adjnode(NULL, 1);
            graph->array[i].r_notavail_1 = create_adjnode(NULL, 1);
            graph->array[i].r_notavail_0 = create_adjnode(NULL, 1);
            
            graph->array[i].r_avail_1->next = graph->array[i].r_avail_0;
            graph->array[i].r_avail_0->prev = graph->array[i].r_avail_1;
            graph->array[i].r_avail_0->next = graph->array[i].r_notavail_1;
            graph->array[i].r_notavail_1->prev = graph->array[i].r_avail_0;
            graph->array[i].r_notavail_1->next = graph->array[i].r_notavail_0;
            graph->array[i].r_notavail_0->prev = graph->array[i].r_notavail_1;

        }

    }
    
    return graph;
}

// Right sides is an array of integers that represent each vertex on the right side
// of the graph d times
int *create_right_sides(int N, int d)
{
    int* right_sides = malloc(N * d * sizeof(int));
    int i, j;
    for (i=0; i<N; ++i) {
        for (j=0; j<d; ++j) {
            right_sides[i*d+j] = i+N;
        }
    }
    
    // shuffle right_sides
    int temp,r;
    for (i=N*d-1; i>1; --i) {
        temp = right_sides[i];
        r = rand_lim(i);
        right_sides[i] = right_sides[r];
        right_sides[r] = temp;
    }
    return right_sides;
}

// Creates a graph with dummy edges
struct Graph* create_dummy_graph(int N, int d, int* right_sides)
{

    int i,j;
    struct Graph* graph = init_dummy_graph(N*2, d);
    struct AdjList* array = graph->array; // the array of adjacency lists
    
    // Create dummy edges first, only add to adjacency lists of only left vertices
    for (i = 0; i < N; i++) {
        array[i].l_dummy = create_adjnode(create_edge(i, i+N, 1), 0);
    }
    
    // Not dummy edges, create adjlist nodes 
    struct Edge* edge;
    struct AdjNode *left_node, *right_node;
    for (i = 0; i < N; i++) {
        for (j = 0; j < d; j++) {
            edge = create_edge(i, right_sides[i*d+j], 0); // not dummy
            
            left_node = create_adjnode(edge, 0);
            // insert to the front
            insert_front(left_node, array[i].l_0);
            array[i].l_0 = left_node;
            
            right_node = create_adjnode(edge, 0);
            insert_front(right_node, array[ right_sides[i*d+j]].r_avail_0);
            array[right_sides[i*d+j]].r_avail_0 = right_node;
             
            // Need to connect twins
            left_node->twin = right_node;
            right_node->twin = left_node;
   
        }
        // the beginning of the adjlist of vertex i is the dummy, and we want to connect it to l_min1
        array[i].l_dummy->next = array[i].l_min1;
        array[i].l_min1->prev = array[i].l_dummy;
        
    }
    return graph;
}

void free_graph(struct Graph* graph) {
    
    int N = (graph->V)/2;
    struct AdjList* array = graph->array;
    struct AdjNode *node, *next;
    int i;
    
    for (i = 0; i < N; i++) {
        node = array[i].l_dummy;
        next = node->next;
        free(node->edge);
        free(node);
        node = next;
        
        while (node != NULL) {
            if (node->end_node == 0) {
                free(node->edge);
                free(node->twin);
            }
            
            next = node->next;    
            free(node);
            node = next;
            
        }
        
        // now do l_plus1
        node = array[i].l_plus1;
        while (node != NULL) {
            if (node->end_node == 0) {
                free(node->edge);
                free(node->twin);
            }
            next = node->next;
            free(node);
            node = next;
        }
        
    }
    
    free(array);
    free(graph);
}



int search(struct Graph* graph, FILE *results) {

    int N = (graph->V)/2; 
    int d = graph->d;
    struct AdjList* array = graph->array;
    
    //List of pointers to nodes in available
    struct AvailableNode** avail_pointers =  (struct AvailableNode**) malloc(N*sizeof(struct AvailableNode*));
    
    struct AvailableNode* prev_node = NULL;
    struct AvailableNode* node;
    int i;

    for (i= N-1; i >= 0; i--) {
        // create node in available for vertex
        node = (struct AvailableNode*) malloc(sizeof(struct AvailableNode));
        node->vertex = i;
        node->next = prev_node; 
        if (node->next != NULL) {
            node->next->prev = node;
        }
        prev_node = node;
        // add pointer to this node to avail_pointers
        avail_pointers[i] = node;
    }
    node->prev = NULL; // the head of the list has no previous
    
    struct Available* available = (struct Available*) malloc(sizeof(struct Available));
    available->length = N; // all left vertices are initially available
    available->head = node;
    
    // INITIALIZE PREDECESSOR LIST - a list of predecessor nodes
    struct PredecessorNode** predecessor = (struct PredecessorNode**) malloc(2*N*sizeof(struct PredecessorNode*));
    for (i = 0; i < 2*N; i++) {
        struct PredecessorNode* node = (struct PredecessorNode*) malloc(sizeof(struct PredecessorNode));
        node->search = 0;
        node->adj_node = NULL;
        predecessor[i] = node;
    }

    // our stats
    int edges_visited = 0;
    int edges_traversed = 0;
    int search_num = 1;
    int* cycle_lengths = (int*) malloc(N*sizeof(int));
    int* dummies_per_cycle = (int*) malloc(N*sizeof(int));
    
    // Do the search
    while (available->length > 0) {
                
        struct AvailableNode* next_avail = available->head;
        int end = next_avail->vertex;
        struct AdjNode* dummy_node = array[end].l_dummy;
        if ((dummy_node->edge)->is_dummy == 0) {
            fprintf(stderr, "We didn't get a dummy edge...");
            return 1;
        }
        
        int start = (dummy_node->edge)->right;
        predecessor[start]->search = search_num;
        predecessor[start]->adj_node = dummy_node;
        
        biased_DFS(graph, search_num, start, end, predecessor, &edges_visited);
        
        if (predecessor[end]->search < search_num) { // we did not find a cycle
            fprintf(stderr, "No cycle found... Something is wrong.");
            return 1;
        }
        // loop through path - if edge is dummy, remove corresponding left vertex from available, decrement length!
        
        struct AdjNode* adj_node;
        struct Edge* edge;
        int left = end;
        int right;
        while (1) {
                        
            adj_node = predecessor[left]->adj_node; // note that adj_node is in an adj list on the RIGHT
            edge = adj_node->edge;
            (edge->direction)--; // we took this edge right to left
            right = edge->right;
            
            
            //printf("Traversing (%d, %d), direction = %d\n", left, right, edge->direction);
            
            if (edge->direction == 0) { // it was 1 before
                
                // node is in adj list on the right
                splice(adj_node);                
                
                if (adj_node->in_avail == 1) {
                    if (adj_node == array[right].r_avail_1)
                        array[right].r_avail_1 = adj_node->next;
                    
                    insert_front(adj_node, array[right].r_avail_0);
                    array[right].r_avail_0 = adj_node;
                }
                else {
                    if (adj_node == array[right].r_notavail_1)
                        array[right].r_notavail_1 = adj_node->next;
                    
                    insert_front(adj_node, array[right].r_notavail_0);
                    array[right].r_notavail_0 = adj_node;
                }
     
                // add node's twin back to adjlist on the left, l_0
                splice(adj_node->twin); // from l_plus1
                if (adj_node->twin == array[left].l_plus1)
                    array[left].l_plus1 = adj_node->twin->next;
                
                insert_front(adj_node->twin, array[left].l_0);
                array[left].l_0 = adj_node->twin;
                
            }
            
            if ( edge->direction == -1) { // was direction 0
                
                // we don't care about keeping track of adj_node on the right because it is now -1
                splice(adj_node);

                if (adj_node == array[right].r_avail_0) 
                    array[right].r_avail_0 = adj_node->next;
                if (adj_node == array[right].r_notavail_0)
                        array[right].r_notavail_0 = adj_node->next;
                
                // adj_node's twin on the left went from 0 to -1
                splice(adj_node->twin);
                
                if (adj_node->twin == array[left].l_0)
                    array[left].l_0 = adj_node->twin->next;
                
                insert_front(adj_node->twin, array[left].l_min1);
                array[left].l_min1 = adj_node->twin;
               
            }
            
            adj_node = predecessor[right]->adj_node; // adj_node is now on the left
            edge = adj_node->edge;
            (edge->direction)++;
            left = edge->left;
            
            
            //printf("Traversing (%d, %d), direction = %d\n", left, right, edge->direction);
            
            if (edge->direction == 0) { // it was -1 before
                
                // adj_node is now on the left
                splice(adj_node);
        
                if (adj_node == array[left].l_min1)
                    array[left].l_min1 = adj_node->next;
                
                insert_front(adj_node, array[left].l_0);
                array[left].l_0 = adj_node;
                
                // adj_node's twin is on the right
                if (adj_node->twin->in_avail == 1) {
                    insert_front(adj_node->twin, array[right].r_avail_0);
                    array[right].r_avail_0 = adj_node->twin;
                }
                
                else {
                    insert_front(adj_node->twin, array[right].r_notavail_0);
                    array[right].r_notavail_0 = adj_node->twin;
                }
            }
            
            if (edge->direction == 1) { // went from 0 to 1
                
                if (edge->is_dummy == 1) {
                    
                    // we want to remove edge->left from available and make all adjacent edges to left no longer available
                    
                    // splice available node out of available
                    struct AvailableNode* avail_node = avail_pointers[left];
                    
                    if (avail_node->next)
                        avail_node->next->prev = avail_node->prev;
                    if (avail_node->prev)
                        avail_node->prev->next = avail_node->next;
                    
                    if (avail_node == available->head)
                        available->head = avail_node->next;
                    
                    (available->length)--;
                    dummies_per_cycle[search_num-1] += 1;
                    free(avail_node);
                    avail_pointers[left] = NULL;
                    
                    // Now we need to loop through every adj node adjacent to left
                    // switch buckets from avail to not avail
                    
                    struct AdjNode* curr_node = array[left].l_min1; // skip the dummy, which has no twin
                    while (curr_node->end_node == 0) {
                        // the twin is not in a list on the right, so it does not need to be spliced/moved
                        curr_node->twin->in_avail = 0;
                        curr_node = curr_node->next;
                        
                    }
                    curr_node = curr_node->next; // get the start of l_0
                    
                    while (curr_node->end_node == 0) {
                        // change bucket to notavail_0
                        splice(curr_node->twin);
                        
                        if (curr_node->twin == array[curr_node->edge->right].r_avail_0)
                            array[curr_node->edge->right].r_avail_0 = curr_node->twin->next;
                        
                        insert_front(curr_node->twin, array[curr_node->edge->right].r_notavail_0);
                        array[curr_node->edge->right].r_notavail_0 == curr_node->twin;
                        
                        curr_node->twin->in_avail = 0;
                        curr_node = curr_node->next;
                        
                    }
                    
                    // Now loop through bucket of disconnected nodes.
                    curr_node = array[left].l_plus1;
                    
                    while (curr_node->end_node == 0) {
                        
                        splice(curr_node->twin);
                        
                        if (curr_node->twin == array[curr_node->edge->right].r_avail_1)
                            array[curr_node->edge->right].r_avail_1 = curr_node->twin->next;
                        
                        insert_front(curr_node->twin, array[curr_node->edge->right].r_notavail_1);
                        array[curr_node->edge->right].r_notavail_1 = curr_node->twin;
                            
                        curr_node->twin->in_avail = 0;
                        curr_node = curr_node->next;
                        
                    }
                }

                else { // treat like normal adjnode.
                    // went from 0 to 1 so move from l_0 to l_plus1
                    splice(adj_node);
                    if (adj_node == array[left].l_0)
                        array[left].l_0 = adj_node->next;
                    
                    insert_front(adj_node, array[left].l_plus1);
                    array[left].l_plus1 = adj_node;
                    
                    // now move it's twin from the right - from 0 to 1
                    splice(adj_node->twin);

                    if (adj_node->twin->in_avail == 1) {
                        if (adj_node->twin == array[right].r_avail_0)
                            array[right].r_avail_0 = adj_node->twin->next;
                        
                        insert_front(adj_node->twin, array[right].r_avail_1);
                        array[right].r_avail_1 = adj_node->twin;

                        
                    }
                    else {
                        if (adj_node->twin == array[right].r_notavail_0)
                            array[right].r_notavail_0 = adj_node->twin->next;
                        
                        insert_front(adj_node->twin, array[right].r_notavail_1);
                        array[right].r_notavail_1 = adj_node->twin;

                    }
                }
            }
            
            cycle_lengths[search_num-1] += 2;
            edges_traversed += 2;
            
            if (left == end)
                break;
            
        }
        
        search_num++;
        
    }

    // Let's print out our results:
    fprintf(results, "%d, %d, ", edges_visited, edges_traversed);
    fprintf(results, "%d, %d, ", (double) edges_visited/(N*d), (double) edges_traversed/(N*d));

    double max_ratio = -1;

    for (i = 0; i < search_num-1 ; i++) {
        if (max_ratio == -1 || (double) cycle_lengths[i]/dummies_per_cycle[i] > max_ratio) {
            max_ratio = (double) cycle_lengths[i]/dummies_per_cycle[i];
        }
    }

    fprintf(results, "%f\n", max_ratio);

    /*
    fprintf(results, "Total number of cycles: %d\n", search_num-1);
    fprintf(results, "Cycle length, dummies in cycle, ratio: [");
    for (i = 0; i < search_num-1 ; i++) {
        if (i == search_num-2)
            fprintf(results, "(%d, %d, %f)]\n", cycle_lengths[i], dummies_per_cycle[i], (double) cycle_lengths[i]/dummies_per_cycle[i]);
        else
            fprintf(results, "(%d, %d, %f); ", cycle_lengths[i], dummies_per_cycle[i], (double) cycle_lengths[i]/dummies_per_cycle[i]);
    })*/
    


    // free predecessor nodes
    struct PredecessorNode* pred_node, *next;
    for (i = 0; i < 2*N; i++) {
        free(predecessor[i]);
    }
    
    free(avail_pointers); // every node should have already been freed
    free(dummies_per_cycle);
    free(cycle_lengths);
    free(predecessor);
    free(available);

    return 0;
}


// Given a graph with dummy edges, performs a biased DFS search
// towards available edges when on right, alwayds towards direction 0
void biased_DFS(struct Graph* graph, int search_num, int start, int end, struct PredecessorNode** predecessor, int* edges_visited) {
    
    int direction;
    struct AdjNode* adj_node;
    struct AdjNode* next;
    
    struct Edge* edge;
    int other;
    int vertex;
    struct StackNode* popped;
    
    struct AdjList* array = graph->array;
    int N = (graph->V)/2;
    
    // Get the first edge in start's adj list
    adj_node = array[start].r_avail_1;
    
    // initialize stack
    struct Stack* stack = init_stack();
    push_stack(stack, start, adj_node);
    
    while (1) {
                
        popped = pop_stack(stack);
        vertex = popped->vertex;
        adj_node = popped->adj_node;
        
        // while it is null, pop from the stack
        while (adj_node == NULL) {
            if (stack->length == 0) {
                return;
            }
            else {
                popped = pop_stack(stack);
                vertex = popped->vertex;
                adj_node = popped->adj_node;
            }
        }

        // Loop until we find the next non end_node. if we reach the end of a list,
        // we'll just go back and pop again
        while (adj_node->end_node == 1) {
            adj_node = adj_node->next;
            if (adj_node == NULL)
                break;
        }
        if (adj_node == NULL) {
            continue; // we will pop from the stack again at the top
        }
        
        edge = adj_node->edge;
        (*edges_visited)++;
        
        next = adj_node->next;
        push_stack(stack, vertex, next);
        
        if (vertex < N) 
            other = edge->right;
        else 
            other = edge->left;
                
        if (predecessor[other]->search < search_num) { // then the vertex hasn't been visited in this search
            predecessor[other]->search = search_num; // now has been visited
            predecessor[other]->adj_node = adj_node;
    
            if (other == end) { // cycle is complete, predecessor is updated
                free_stack(stack);
                return;
            }
            else {
                if (other < N) {
                    push_stack(stack, other, array[other].l_dummy);

                }
                else {
                    push_stack(stack, other, array[other].r_avail_1);

                }
            }
     
        }
    }
        
}

// Stack operations

struct Stack* init_stack() {
    struct Stack* stack = (struct Stack*) malloc(sizeof(struct Stack));
    stack->length = 0;
    stack->top = NULL;
    return stack;
    
}

// Give a vertex and an edge pointer - to edge in the adjacency list of 
void push_stack(struct Stack* stack, int vertex, struct AdjNode* adj_node) {
    
    // create the stack node to push onto the stack
    struct StackNode* node = (struct StackNode* ) malloc(sizeof(struct StackNode));
    node->adj_node = adj_node;
    node->vertex = vertex;
    node->next = stack->top;
    stack->top = node;
    (stack->length)++;
    
}

struct StackNode* pop_stack(struct Stack* stack) {
    
    struct StackNode* popped = stack->top;
    stack->top = popped->next;
    (stack->length)--;
    return popped;
    
}
    
void free_stack(struct Stack* stack) {
    struct StackNode* next;
    struct StackNode* current = stack->top;
    while (current != NULL) {
        next = current->next;
        free(current);
        current = next; 
    }
    free(stack);
        
}


// AdjNode operations
void splice(struct AdjNode* node) {
    if (node->next)
        node->next->prev = node->prev;
    if (node->prev)
        node->prev->next = node->next;
    
}

void insert_front(struct AdjNode* adj_node, struct AdjNode* pointer) {
        
    adj_node->next = pointer;
    adj_node->prev = pointer->prev;
    
    if (adj_node->prev)
        adj_node->prev->next = adj_node;
    
    // we know there will always be a next, because the pointer cannot be NULL
    adj_node->next->prev = adj_node;
    
}

void print_list(struct AdjNode* beginning, int vertex, int full_list) {
    
    struct AdjNode* current = beginning;
    while (current != NULL) {
        if (current->end_node == 0) {
            printf("Vertex %d, Edge (%d,%d) with direction %d, dummy = %d\n", vertex, current->edge->left, current->edge->right, current->edge->direction, current->edge->is_dummy);
            
        }
        else { // we've reached an endnode
            if (full_list == 0)
                break;
        }
        
        current = current->next;

    }
    
}
                   
// return a random number between 0 and limit exclusive.  Remember to seed (srand).
int rand_lim(int limit)
{
    return random() % limit;
}