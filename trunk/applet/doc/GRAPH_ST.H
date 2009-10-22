// graph_structures.h -- Michael Maguire -- June 93

// a data structures header file to be included in all the source files which
// 	share the graph data

// not to be included in the algorithm source files -- they shouldn't see this 



#include "flags.h"



// for the algorithm structure, which is the first part of a graph structure
// 	-- these are separated so that not everything which needs to see the
// 	algorithm structure will also have to see the graph structure

#include "algorithm_structure.h"



#if NEW_LOOKUP_TABLE_WAY_FOR_EDGES

#include "graph_edges_pr.h"

#endif



struct disc
{

	Rect			disc_rectangle;				// for speed in drawing

	RGBColor		colour;						// for speed in changing colours while drawing

	
	double			real_disc_radius;			// for accuracy in recalculating after zooms, etc.


	struct disc		**next_disc_in;				// to maintain a linked list of the discs
												// 	in the order they need to be drawn
	
	struct disc		**next_disc_to_be_drawn;	// to maintain a linked list of discs
												// 	belonging to a particular node

};

typedef struct disc disc;


struct node 
{

	short				grow_direction	:2;	// set to -1,0,1 for shrink, don't change and grow, respectively

	Boolean				is_selected 	:1;
	Boolean				is_highlited 	:1;
	Boolean				is_blue			:1;	// we store the node's bipartite colour
											// 	so that we can switch back and forth
											// 	between bipartite and nonbipartite
											// 	and still preserve the blues and reds


	double				x,y;				// for accuracy user input, algorithms, and  in drawing last
											// 	frames and redraws after zooms, etc.

	double				real_disc_radius;	// for accuracy
	

	Point				location;			// for speed in drawing lines and edges

	Rect				node_rect;			// for speed drawing nodes 

	Rect				disc_rectangle;		// for speed for drawing outermost disc around node 

	RGBColor			colour;				// for speed drawing in the node's outermost disc's colour

	
	disc				**next_disc_in;		// used in problems which need moats -- not used in bipartite problems
	
	struct node 		**next_node;		// used to maintain a linked list of all the nodes

};

typedef struct node node;


struct line
{

	node 			**node1, **node2;	
	
	struct line 	**next_line;

	Boolean 		newly_added		:1;
	
	Boolean 		to_be_removed	:1;
	
	Boolean			not_in_graph	:1;
};

typedef struct line line;



#if ! NEW_LOOKUP_TABLE_WAY_FOR_EDGES

struct edge			// for starting with incomplete graphs
{

	node	**node1, **node2;

	struct	edge **left, **right;

};

typedef struct edge edge;

#endif




struct graph 
{
	
	// at times we will cast a graph handle into an algorithm handle,
	// 	so it is important to keep associated_algorithm first in the
	// 	list of elements which make up a graph

	algorithm			associated_algorithm;


	WindowPtr			associated_window;

	Boolean				assume_complete_graph		:1;

	Boolean				animation_in_progress		:1;

	Boolean				pause_for_next_node			:1;
	Boolean				pause_after_bumps			:1;
	Boolean				pause_for_augmenting		:1;

	Boolean				show_node_numbers			:1;
	Boolean				show_grow					:1;
	Boolean				show_zero_growth			:1;
	Boolean				show_final_answer_only		:1;


	// for real<->screen conversions -- stores the current view of the real plane 
	double				real_view_bottom; 
	double 				real_view_left;
	double				real_view_width;

#if 0
	// for scroll thumb bounds -- to be implemented later
	double 				furthest_left;
	double				furthest_bottom;
	double				furthest_right;
	double				furthest_top;
#endif

	double				diameter;

#if LOOK_UP_TABLES_USED
  //Goddyn's addition
	double				**distance_table_handle;
	short				number_of_nodes;
#endif
	
	node				**first_node;
	
	disc				**first_disc_to_be_drawn;

	line				**first_tight_line;
	line				**first_marriage;


#if ! NEW_LOOKUP_TABLE_WAY_FOR_EDGES
	edge				**first_edge;		// for starting with incomplete graphs
#else
	graph_edges			*the_edges;
#endif


	struct graph		**next_graph;
};


typedef struct graph graph;

// this is to cause current_graph to be only actually declared in the file 
// 	where _GRAPH_ is defined, namely graph.c, but to allow other files that include
// 	this header to know of the existence of current_graph so they can access it

#ifdef _GRAPH_
	graph **current_graph = NULL;
#else
	extern graph **current_graph;
#endif
