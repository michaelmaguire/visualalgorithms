// Algorithm_Support.java - Michael Maguire - 14 Apr 96

// all the functions the algorithms
//  should ever need for talking to the rest of the program


interface Algorithm_Support
{

double input_graph_diameter();
// algorithms should use this to input the graph diameter

double input_initial_disc_size( short node_number );
// algorithms should use this to set their initial disc radiuses
// 	so that maximum weight problems can easily be accomodated

void output_augmenting_now( boolean status );
// algorithms should call this once each time through step to indicate whether
// 	they are in the process of augmenting

double input_distance( short node_number_1, short node_number_2);
// the algorithm should use this function to find the real distance between points as
// 	this will more easily allow for future enhancements involving different metrics
// 	-- also, it means algorithms never need to know x-y coordinates

boolean input_is_node_selected( short node_number );
/* needed for forest perfect matching */

boolean input_is_node_blue( short node_number );
// non-bipartite algorithms should never need to call this functions

boolean input_is_zero_growth_being_shown();
// input whether user wants degenerate tree growth to be shown

void output_waiting_for_next_node( boolean status );
// an algorithm should call this each time it's bump function is called
// 	to inform the rest of the program whether or not it's waiting for a new node
// 	to grow from

void output_ready_for_post_opt();
// an algorithm should call this once, when finnished, but awaiting a post_opt call

void output_done();
// an algorithm should call this once, when it is completely finished

void output_grow( short node_number);

void output_shrink( short node_number);

void output_change_by( double distance );

void output_add_tight_line( short node_number1, short node_number2);

void output_remove_tight_line( short node_number1, short node_number2);

void output_marry( short nodeNumber1, short nodeNumber2 );

void output_divorce(  short node_number1, short node_number2 );

void output_highlite_node(  short node_number, boolean new_status );

void output_set_outer_colour( short nodeNumber, short colour);
// bipartite algorithms should never need to call this functions
// 	since the user will be selecting whether nodes are blue or red
// 	-- instead, bipartite algorithms should call algorithm_output_is_node_blue
// 	to GET the user selected colours

void output_add_disc( short node_number );
// only algorithms needing moats should use this

void output_remove_disc( short node_number );
// only algorithms needing moats should use this

}
