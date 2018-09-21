#!//Library/Frameworks/Python.framework/Versions/2.7/bin/python

from random import shuffle, seed
from sys import argv, exit

class Edge:
    def __init__(self, left, right, is_dummy):
        self.left = left
        self.right = right
        self.is_dummy = is_dummy
        self.order = []
        self.direction = 0
    
    def __str__(self):
        string =  "(" + str(self.left) + ", " + str(self.right) + ") " + str(self.direction) + " " + str(self.order)
        if self.is_dummy:
            string += " DUMMY"
        return string

def print_edges(edges):
    for e in edges:
        print e,
        if e.left in available:
            print "AVAILABLE"
        else:
            print ""

# Do a BFS from vertex.
def bfs(vertex, first):
    global predecessor, incident, edges_visited

    queue = [vertex]
    other = None

    while len(queue) > 0 and other != first:
        # Search from first vertex in the queue.
        vertex = queue.pop(0)

        for edge in incident[vertex]:
            if vertex < N:
                direction = 1
                if -1 <= edge.direction + direction <= 1:
                    other = edge.right
                else:
                    continue
            else:
                direction = -1
                if 1 >= edge.direction + direction >= -1:
                    other = edge.left
                else:
                    continue

#             print "Searching", edge

            if predecessor[other] == None:
                edges_visited += 1
                predecessor[other] = edge

                if other == first:
                    break
                else:
                    queue.append(other)


def compare_L_to_R(e1, e2):
    if e1.is_dummy != e2.is_dummy:
        if e1.is_dummy:
            return -1
        else:
            return 1
    else:
        return e1.direction - e2.direction

def compare_R_to_L(e1, e2):
    e1_avail = e1.left in available
    e2_avail = e2.left in available
    if e1_avail != e2_avail:
        if e1_avail:
            return -1
        else:
            return 1
    else:
        return e2.direction - e1.direction

def dfs_dfs(vertex, first, bias, dir):
    
    global predecessor, incident, edges_visited, available
    right = vertex
    
    stack_L = [[first, 1]] # Start at 1 to skip the initial dummy edge 
    stack_R = [[vertex, 1]]
    
    # predecessor lists will eventually be combined into predecessor
    predecessor_L = 2*N*[None]
    predecessor_R = 2*N*[None]
    predecessor_R[vertex] = predecessor[vertex]
    
    # O = R, 1 = L
    R_or_L = 0
    
    while True:
        if R_or_L == 0: # coming from the right
        
            #print "Coming from the right."
            popped = stack_R.pop()
            vertex = popped[0]
            edge_index = popped[1]
            #print "Vertex " + str(vertex) + " popped from stack R."

            # Pop from stack until you get a vertex with an edge that has not been taken yet
            while edge_index >= len(incident[vertex]):
                if len(stack_R) == 0:
                    break
                else:
                    popped = stack_R.pop()
                    vertex = popped[0]
                    edge_index = popped[1]

            edge = incident[vertex][edge_index]
            #print "Edge being looked at: \n" + str(edge)
            stack_R.append([vertex, edge_index+1])
        
            if edge.is_dummy and edge.left not in available: # It's already used
                continue
            if vertex < N: # On the left
                direction = 1
                if edge.direction + direction <= 1:
                    other = edge.right
                else:
                    continue # Pop another vertex from this side
            else:
                direction = -1
                if (edge.direction + direction) >= -1 and not edge.is_dummy:
                    other = edge.left
                else:
                    continue

            edges_visited += 1

            #print "other = " + str(other)

            # If this other vertex has not been visited by this side yet
            if predecessor_R[other] == None:
                predecessor_R[other] = edge
                if predecessor_L[other] != None or other == first: # This vertex has been visited from the left!
                    break
    
                # ADD BIASES HERE
                if other < N:
                    # No bias needed, always take dummy first
                    if dir == 0: # Away from 0
                        incident[other] = sum([[incident[other][0]], [e for e in incident[other] if e.direction == 0 and not e.is_dummy], [e for e in incident[other] if e.direction == -1 and not e.is_dummy], [e for e in incident[other] if e.direction == 1 and not e.is_dummy]], [])
                    elif dir == 1: # Towards 0
                        incident[other] = sum([[incident[other][0]], [e for e in incident[other] if e.direction == -1 and not e.is_dummy], [e for e in incident[other] if e.direction == 0 and not e.is_dummy], [e for e in incident[other] if e.direction == 1 and not e.is_dummy]], [])

                else: # Other is on the right side
                    if bias:
                        if dir == None:
                            incident[other] = sum([[e for e in incident[other] if e.left in available], [e for e in incident[other] if e.left not in available]], [] )
                        elif dir == 0:
                            incident[other] = sum([[e for e in incident[other] if e.left in available and e.direction == 0], [e for e in incident[other] if e.left in available and e.direction == 1], [e for e in incident[other] if e.left not in available and e.direction == 0], [e for e in incident[other] if e.left not in available and e.direction == 1], [e for e in incident[other] if e.direction == -1]], [])
                        else: # Direction towards 0
                            incident[other] = sum([[e for e in incident[other] if e.left in available and e.direction == 1], [e for e in incident[other] if e.left in available and e.direction == 0], [e for e in incident[other] if e.left not in available and e.direction == 1], [e for e in incident[other] if e.left not in available and e.direction == 0],[e for e in incident[other] if e.direction == -1]], [])
                    else: # No bias
                        if dir == 0:
                            incident[other] = sum([[e for e in incident[other] if e.direction == 0], [e for e in incident[other] if e.direction == 1], [e for e in incident[other] if e.direction == -1]], [])
                        elif dir == 1:
                            incident[other] = sum([[e for e in incident[other] if e.direction == 1], [e for e in incident[other] if e.direction == 0], [e for e in incident[other] if e.direction == -1]], [])

                stack_R.append([other, 0])
                #print str(other) + " added to the stack with edge index 0."
                R_or_L = (R_or_L+1)%2

        else: # Coming from the left
            #print "Coming from the left."
            popped = stack_L.pop()
            vertex = popped[0]
            edge_index = popped[1]
            #print "Vertex " + str(vertex) + " popped from stack L."

            while edge_index >= len(incident[vertex]):
                if len(stack_L) == 0:
                    break
                else:
                    popped = stack_L.pop()
                    vertex = popped[0]
                    edge_index = popped[1]

            edge = incident[vertex][edge_index]
            #print "Edge being looked at: \n" + str(edge)
            stack_L.append([vertex, edge_index + 1])

            if edge.is_dummy and edge.left not in available: # It's already used
                continue
            if vertex < N:
                direction = -1
                if edge.direction + direction >= -1 and not edge.is_dummy:
                    other = edge.right
                else:
                    continue
            else:
                direction = 1
                if edge.direction + direction <= 1:
                    other = edge.left
                else:
                    continue
            
            edges_visited += 1

            # Skip this edge - we don't want to come back to first from the left
            if other == first:
                continue 
            #print "other = " + str(other)
            if predecessor_L[other] == None:
                predecessor_L[other] = edge
                if predecessor_R[other] != None:
                    break

                # We are now at a vertex that we haven't been to before
                # ADD BIASES HERE
                if other < N: 
                    if bias:
                    # Bias towards available vertices on the right, coming from the left
                        if dir == None:
                            incident[other] = sum([[e for e in incident[other] if (e.right - N) in available], [e for e in incident[other] if (e.right - N) not in available]], [])
                        elif dir == 0: # Away from zero
                            incident[other] = sum([[e for e in incident[other] if (e.right - N) in available and e.direction == 0], [e for e in incident[other] if (e.right - N) in available and e.direction == 1], [e for e in incident[other] if (e.right - N) not in available and e.direction == 0], [e for e in incident[other] if (e.right - N) not in available and e.direction == 1], [e for e in incident[other] if e.direction == -1]], [])
                        else: # dir == 1, towards zero
                            incident[other] = sum([[e for e in incident[other] if (e.right - N) in available and e.direction == 1], [e for e in incident[other] if (e.right -N) in available and e.direction == 0], [e for e in incident[other] if (e.right - N) not in available and e.direction == 1], [e for e in incident[other] if (e.right -N) not in available and e.direction == 0], [e for e in incident[other] if e.direction == -1]], [])

                    else: # No bias towards available edges
                        if dir == 0: # away from zero 
                            incident[other] = sum([[e for e in incident[other] if e.direction == 0], [e for e in incident[other] if e.direction == 1], [e for e in incident[other] if e.direction == -1]], [])
                        elif dir == 1:
                            incident[other] = sum([[e for e in incident[other] if e.direction == 1], [ e for e in incident[other] if e.direction == 0], [e for e in incident[other] if e.direction == -1]], [])
                                
                else: # Other is on the right, no bias, but must take dummy edge if available
                    if dir == 0:
                        incident[other] = sum([[incident[other][0]], [e for e in incident[other] if e.direction == 0 and not e.is_dummy], [e for e in incident[other] if e.direction == -1 and not e.is_dummy], [e for e in incident[other] if e.direction == 1 and not e.is_dummy]], [])
                    elif dir == 1:
                        incident[other] = sum([[incident[other][0]], [e for e in incident[other] if e.direction == -1 and not e.is_dummy], [e for e in incident[other] if e.direction == 0 and not e.is_dummy], [e for e in incident[other] if e.direction == 1 and not e.is_dummy]], [])

                stack_L.append([other, 0])
                #print str(other) + " added to the stack with edge index 0."
                R_or_L = (R_or_L+1)%2
 
    #print "I hope we make it here..."
    # Trace back predecessors
    
    if predecessor_R[other] != None and predecessor_L[other] != None:
        #print "Paths met at " + str(other)
        
        # Trace back by switching direction of backpointers coming from the left
        current = other
        while current != first:
            if current < N:
                before = predecessor_L[current].right
            else:
                before = predecessor_L[current].left
            predecessor[before] = predecessor_L[current]
        #print "predecessor " + str(before) + " is " + str(predecessor[before])
            current = before

        # Now just directly update predecessor with predecessor_R
        current = other
        while current != right:
            predecessor[current] = predecessor_R[current]
            #print "predecessor of " + str(current) + " is " + str(predecessor[current])
            if current < N:
                before = predecessor[current].right
            else:
                before = predecessor[current].left
            current = before
        
    # The R side search found "first" and not any of the other vertices visited from the L
    elif other == first and predecessor_R[other] != None:
        #print "Paths met at " + str(first)
        current = other

        while current != right:
            predecessor[current] = predecessor_R[current]
            #print "predecessor of " + str(current) + " is " + str(predecessor[current])
            if current < N:
                before = predecessor[current].right
            else:
                before = predecessor[current].left
            current = before

    else: # one of the stacks must be empty
        print "No path exists between " + str(vertex) + " and " + str(first)

    #print "END OF DFS-DFS"
                

# Pop from first stack, then explore all incident edges/vertices in the ord

# Visit from vertex, but stop if we visit first.  Works the same as
# bipartite-cycles5.py, but uses an explicit stack for the recursion,
# so that we don't blow out the runtime stack.  bias is True if biased
# toward going to available vertices from the right.  dir is 0 if
# biased toward taking edges with direction 0, it's 1 if biased toward
# taking edges with direction +1 or -1, and it's None if there's no
# such bias.
def dfs_nonrec(vertex, first, bias, dir):
    global predecessor, incident, edges_visited, available

    stack = [[vertex, 0]]

    while True:
        popped = stack.pop()
        vertex = popped[0]
        edge_index = popped[1]
        while edge_index >= len(incident[vertex]):
            if len(stack) == 0:
                return
            else:
                popped = stack.pop()
                vertex = popped[0]
                edge_index = popped[1]
                
        edge = incident[vertex][edge_index]
        stack.append([vertex, edge_index+1])

        if vertex < N:
            direction = 1
            if edge.direction + direction <= 1:
                other = edge.right
            else:
                continue

        else:
            direction = -1
            if edge.direction + direction >= -1:
                other = edge.left
            else:
                continue

#         print "Searching", edge

        edges_visited += 1

    
        if predecessor[other] == None:
            predecessor[other] = edge
            
            if other == first:
                return
            else:
                # If biased and other is on the right, arrange the
                # incident list of other so that available vertices
                # appear first.
#                 print "Pushing", other, ":"

                if other < N:
                    if dir == 0:
                        incident[other] = sum([[incident[other][0]], [e for e in incident[other] if e.direction == 0 and not e.is_dummy], [e for e in incident[other] if e.direction == -1 and not e.is_dummy], [e for e in incident[other] if e.direction == 1 and not e.is_dummy]], [])
                    elif dir == 1:
                        incident[other].sort(compare_L_to_R)
#                         incident[other] = sum([[incident[other][0]], [e for e in incident[other] if e.direction == -1 and not e.is_dummy], [e for e in incident[other] if e.direction == 0 and not e.is_dummy], [e for e in incident[other] if e.direction == 1 and not e.is_dummy]], [])
                else:
                    if bias:
                        if dir == None:
                            incident[other] = sum([[e for e in incident[other] if e.left in available], [e for e in incident[other] if e.left not in available]], [])
                        elif dir == 0:
#                             print "List 1"
#                             print_edges([e for e in incident[other] if e.left in available and e.direction == 0])
#                             print "List 2"
#                             print_edges([e for e in incident[other] if e.left in available and e.direction == 1])
#                             print "List 3"
#                             print_edges([e for e in incident[other] if e.left not in available and e.direction == 0])
#                             print "List 4"
#                             print_edges([e for e in incident[other] if e.left not in available and e.direction == 1])
#                             print "List 5"
#                             print_edges([e for e in incident[other] if e.direction == -1])
#                             print "After rearranging"

                            incident[other] = sum([[e for e in incident[other] if e.left in available and e.direction == 0], [e for e in incident[other] if e.left in available and e.direction == 1], [e for e in incident[other] if e.left not in available and e.direction == 0], [e for e in incident[other] if e.left not in available and e.direction == 1], [e for e in incident[other] if e.direction == -1]], [])
                        else:
                            incident[other].sort(compare_R_to_L)
#                             incident[other] = sum([[e for e in incident[other] if e.left in available and e.direction == 1], [e for e in incident[other] if e.left in available and e.direction == 0], [e for e in incident[other] if e.left not in available and e.direction == 1], [e for e in incident[other] if e.left not in available and e.direction == 0], [e for e in incident[other] if e.direction == -1]], [])
                    else:
                        if dir == 0:
                            incident[other] = sum([[e for e in incident[other] if e.direction == 0], [e for e in incident[other] if e.direction == 1], [e for e in incident[other] if e.direction == -1]], [])
                        elif dir == 1:
                            incident[other] = sum([[e for e in incident[other] if e.direction == 1], [e for e in incident[other] if e.direction == 0], [e for e in incident[other] if e.direction == -1]], [])

#                 print_edges(incident[other])
                stack.append([other, 0])

def dfs_nonrec_nonbiased_nodir(vertex, first):
    dfs_nonrec(vertex, first, False, None)

def dfs_nonrec_nonbiased_dir0(vertex, first):
    dfs_nonrec(vertex, first, False, 0)

def dfs_nonrec_nonbiased_dir1(vertex, first):
    dfs_nonrec(vertex, first, False, 1)

def dfs_nonrec_biased_nodir(vertex, first):
    dfs_nonrec(vertex, first, True, None)

def dfs_nonrec_biased_dir0(vertex, first):
    dfs_nonrec(vertex, first, True, 0)

def dfs_nonrec_biased_dir1(vertex, first):
    dfs_nonrec(vertex, first, True, 1)

def dfs_dfs_nonbiased_nodir(vertex, first):
    dfs_dfs(vertex, first, False, False)

def dfs_dfs_biased_dir1(vertex, first):
    dfs_dfs(vertex, first, True, 1)

# Command line has two arguments: N (number of vertices per side) and
# d (degree).

if len(argv) == 1:
    print "Enter N, d:",
    params = raw_input().split()
    N = int(params[0])
    d = int(params[1])
else:
    N = int(argv[1])
    d = int(argv[2])

print N, d

right_sides = []
for right in range(N):
    for i in range(d):
        right_sides.append(N + right)
# seed(17)
shuffle(right_sides)

# print right_sides

# At this point, incident gives us the bipartite graph structure,
# except that only vertices on the left have dummy edges in their
# incident lists.

# This bizarre construction gives a single list of 2N values whose
# first N values are d+1 (degrees of left vertices) and whose last N
# values are d (degrees of right vertices).
# degree = sum([(N * [d+1]), (N * [d])], [])

# Perform a search.  style says which search function to call.
def search(style, two_ended):
    global incident, predecessor, edges_visited, edges_traversed, available

    incident = []
    for i in range(2 * N):
        incident.append([])

    # Add in dummy edges, to edge lists of both vertices.
    for i in range(N):
        dum_edge = Edge(i, i+N, True)
        incident[i].append(dum_edge)
        if two_ended:
            incident[i+N].append(dum_edge)


    i = 0
    for left in range(N):
        for e in range(d):
            right = right_sides[i]
            edge = Edge(left, right, False)
            incident[left].append(edge)
            incident[right].append(edge)
            i += 1
    
    order = 0
    edges_visited = 0
    edges_traversed = 0
    cycles = 0
    dummies = []

    # Initialize the list of vertices on left whose dummy edge is not
    # yet taken.
    available = range(N)
    
    while len(available) > 0:
    # Using DFS, find a cycle from the first available vertex on the
    # left back to itself that starts with the dummy edge.  The cycle
    # may include dummy edges going left to right, but not dummy edges
    # going right to left.

#     print "Available:", available

        cycles += 1

        first = available[0]
        right = first + N           # corresponding vertex on the right

        # Take the dummy edge from first to right.
        edge = incident[first][0]
        edges_visited += 1
        if not edge.is_dummy:
            print "Uh, oh!  Should have found a dummy edge from", first, "to", right
            exit(1)

        # predecessor gives the edge (not the vertex) preceding a
        # vertex in the search tree.
        predecessor = (2 * N) * [None]
        predecessor[right] = edge
        
    
        #Print all of the edges in the graph
        #for list in incident:
            #for i in range(len(list)):
                #print list[i]

        #print "Searching from", first
        style(right, first)

        # I hope we got here because we got back to the first vertex.
        if predecessor[first] == None:
            print "Didn't find a cycle from", first
            exit(1)

        # Trace out the cycle to find how many edges it has.
        left = first
        cycle_length = 0
        while True:
            edge = predecessor[left]
            #print "edge = " + str(edge)
            
            right = edge.right
            edge = predecessor[right]
            #print "edge = " + str(edge)

            left = edge.left
            cycle_length += 2
            if left == first:
                break
    
        #print "cycle length = " + str(cycle_length)
        order += cycle_length

        dummies.append(0)

        # Now trace out the cycle and direct the edges.
        #     print "New cycle:"
        left = first
        while True:
            edge = predecessor[left]
            edges_traversed += 1
            edge.direction -= 1
            edge.order.append(order)
            order -= 1
#             print edge

            right = edge.right
            edge = predecessor[right]
            edges_traversed += 1
            edge.direction += 1
            edge.order.append(order)
            order -= 1
#             print edge
            left = edge.left

            if edge.is_dummy:
                dummies[len(dummies)-1] += 1
                available.remove(left)

            if left == first:
                break

        order += cycle_length

    # Check the result.  Directions for each vertex on the left should
    # add to 0, and directions for each vertex on the right should add
    # to -1 (because it doesn't have the dummy edge in its incident
    # list).

    non_canceled = 0        # number of edges with direction != 0
    max_traversals = 0      # max number of times an edge is traversed

    for i in range(N):
        total = sum([e.direction for e in incident[i]])
        #if total != 0:
            #print "Total for", i, "is", total
        non_canceled += len([e for e in incident[i] if e.direction != 0])
        max_traversals = max(max_traversals, max([len(e.order) for e in incident[i]]))

    for i in range(N):
        total = sum([e.direction for e in incident[i+N]])
        #if total != -1:
            #print "Total for", (i+N), "is", total

#     print "Result:"
#     for i in range(2*N):
#         for edge in incident[i]:
#             print edge

    # Print out how many edges were visited and how many were added to
    # cycles.
    total_edges = N * (d+1)
    print "Total edges =", total_edges
    print "Cycles =", cycles
    print "Edges visited =", edges_visited, ", ratio =", (float(edges_visited) / total_edges)
    print "Edges traversed =", edges_traversed, ", ratio =", (float(edges_traversed) / total_edges)
    print "Non-canceled edges =", non_canceled, ", ratio =", (float(non_canceled) / total_edges)
    print "Max traversals =", max_traversals
    print "Dummy edges in each cycle = ", dummies

# print "\nRecursive DFS"
# search(dfs_rec)

                   # print "\nNon-biased DFS, no direction"
                   #search(dfs_nonrec_nonbiased_nodir)

                   #print "\nNon-biased DFS, direction 0"
                   #search(dfs_nonrec_nonbiased_dir0)

                   #print "\nNon-biased DFS, direction 1"
                   #search(dfs_nonrec_nonbiased_dir1)

                   #print "\nBiased DFS, no direction"
                   #search(dfs_nonrec_biased_nodir)

                   #print "\nBiased DFS, direction 0"
                   ## print "Cycles:"
                   #search(dfs_nonrec_biased_dir0)

                   #print "\nBiased DFS, direction 1"
                   # print "Cycles:"
                   #search(dfs_nonrec_biased_dir1)

                   #print "\nBFS"
                   #search(bfs)

print "\nDFS nonbiased, no direction bias"
search(dfs_nonrec_nonbiased_nodir, False)

print "\nDFS biased, towards 0 for edges"
search(dfs_nonrec_biased_dir1, False)

print "\nDFS-DFS nonbiased, no direction bias"
search(dfs_dfs_nonbiased_nodir, True)

print "\nDFS-DFS biased, towards 0 for edges"
search(dfs_dfs_biased_dir1, True) # Towards 0


# # Print the result.    
# for i in range(2 * N):
#     print i, ":"
#     for e in incident[i]:
#         print "\t", e
