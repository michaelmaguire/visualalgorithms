// graph_edges_pr.h -- Michael Maguire -- August 93


// global function prototypes for graph_edges.c



#include "flags.h"

#pragma once


#if NEW_LOOKUP_TABLE_WAY_FOR_EDGES





#define EDGE_EXISTS	Boolean

class graph_edges
{

		EDGE_EXISTS	**lookup_table_handle;

		short number_of_nodes_in_table;

		short total_edges;

	public:
		
		graph_edges( void );
		
		~graph_edges( void );
		
		Boolean is_there_edge_between( short first_node_number, short second_node_number );

		void add_edge_between( short first_node_number, short second_node_number );

		void remove_edge_between( short first_node_number, short second_node_number );

		void make_table_one_node_bigger( void );
		
		void remove_all_edges( void );

		void remove_all_edges_for_node( short node_number );
		
		void draw( void );
		
		short number_of_edges( void );
	
};






#else	// old way


void add_edge( node **node1, node **node2 );

void remove_edge( edge **the_edge );

void remove_all_edges_from_graph( void );

void remove_all_edges_for_node( node **the_node );

edge **find_edge_close_to( Point where );

edge **find_edge_between( node **node1, node **node2 );


#endif