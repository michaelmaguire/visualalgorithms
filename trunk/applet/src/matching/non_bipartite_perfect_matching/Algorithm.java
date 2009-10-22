// non_bipartite_perfect_matching.java 
// May 97 Java version of:
// nonbip_perfect_matching.c -- Michael Maguire -- August 93


// nonbipartite perfect matching algorithm

package matching.non_bipartite_perfect_matching;

import matching.Algorithm_Support;
import matching.Algorithms_Provide;



class connection
{

	public blossom node_in_other_to_which_i_am_connected;
	
	public boolean is_a_marriage;

}




class blossom
{
	
	// width means total radius if blossom is a trivial one (just a node)
	// 	-- otherwise it means the width of that blossom's moat
	
	public float width;
	
	

	// usually used only if blossom is trivial (i.e. the blossom is just a node)
	// 	-- although we do number the moats when debugging
	
	public short node_number;
	
		
	// these are used to keep track of the way blossoms contain other blossoms
	
	public blossom i_am_contained_in;
	
	public blossom i_contain;

	public blossom this_is_contained_with_me;

	
	
	// these are used to keep track of the way blossoms are interconnected
	
	public connection	first_connection, second_connection;			


	
	// these are used to keep track of the tree structure when the blossom
	// 	is an outermost blossom in a tree
	
	public blossom node_in_me_connected_out_towards_tree_root_node;

	public boolean i_am_in_tree;
	
	public boolean i_am_expanding;	// used only when i_am_in_tree is true

	public boolean is_a_node()								
	{	
		return( i_contain == null );
	}
	
	
	public blossom()
	{
		first_connection = new connection();
		second_connection = new connection();
	}
}




public class Algorithm implements Algorithms_Provide
{

	private Algorithm_Support support;	// what we can rely on for input and output

	// the basic actions to be taken
	private static final short NOTHING = 0;
	private static final short DISSOLVE_BLOSSOM  = 1;
	private static final short MAKE_NEW_BLOSSOM = 2;
	private static final short ADD_TO_TREE = 3;
	private static final short AUGMENT = 4;
	
	// some constants
	private static final float UNBOUNDED_GROW_WIDTH = 0.00f;
	private static final float DISTANCE_TO_NODE_AT_INFINITY_FACTOR = 2.5f;
	private static final short VIRTUAL_NODE_NUMBER = -1;
	
	// some options
	private static final boolean ZERO_SHOW = false;

	private augment_queue_for_non_bipartite_perfect_matching our_augment_queue;
	
	
	private blossom first_blossom;
	private blossom tree_root_node;
	
	private float distance_by_which_to_change;
	
	private blossom	first_affected_blossom;
	private blossom	second_affected_blossom;
	
	private blossom node_we_are_opting_on;

	
	private short action_to_be_taken;

	// for ZERO_SHOW
	private short previous_action_to_be_taken; // only used when testing for zero-growth iterations


	private boolean bumped_into_infinity;

	private short current_colour;
	




public Algorithm( Algorithm_Support the_support, short number_of_nodes )
{
	
	support = the_support;

	short node_number_counter;
		
	first_blossom 			= null;
	tree_root_node 			= null;
	first_affected_blossom 	= null;
	second_affected_blossom	= null;
	node_we_are_opting_on		= null;
	
	action_to_be_taken 		= NOTHING;

	current_colour			= 1;
	bumped_into_infinity		= false;
				
	for(node_number_counter = 1; node_number_counter <= number_of_nodes; node_number_counter++)
	{
		add_node();
	}

	our_augment_queue = new augment_queue_for_non_bipartite_perfect_matching();

	
}


public boolean step_post_op( short node_number)
{

	blossom virtual_node, opt_node, temp_node;
	short number_of_nodes;
	boolean value;
			
	
	if( VIRTUAL_NODE_NUMBER == node_number )
	{
		if( null == node_we_are_opting_on )
			// no post-opt has started
			return( true );

		//user is finished with this tree. reset everything
		
		if( action_to_be_taken == MAKE_NEW_BLOSSOM )
		{
			// the user decided not to make the new blossom
			support.output_remove_tight_line( (first_affected_blossom).node_number, (second_affected_blossom).node_number );
		
		}
		
		reset_tree();
			
		support.output_highlite_node( (node_we_are_opting_on).node_number , false );

		virtual_node = first_blossom;
		first_blossom = (first_blossom).this_is_contained_with_me;
		//DisposHandle( virtual_node );
		node_we_are_opting_on = null;
		
		return( true );
	}

	
	if( 0 == node_number )
	{
		// no node has been submitted for opting.
		
		if( null == node_we_are_opting_on )
		{
			//  post_opting has not started yet
			return(false);
		}
		else
		{
			//use same node_number as in previous call
			node_number = (node_we_are_opting_on).node_number;
		}
		
	}
	else
	{
		//opt on node_number as passed
	}
	
	
	//find node we are supposed to opt on
		
	if( null == (opt_node = find_node_with_number( node_number )) )
		return( false );  //no such node exists. BAD!
				
		
	if( null == node_we_are_opting_on )
	{
		//this is the first call to post_op so we add new virtual node
		
		//if( (virtual_node = (blossom ) NewHandle( sizeof( blossom ) ) ) == null )
		//	return(false);	// not enough memory for the virtual node 
		virtual_node = new blossom();
	
		// place virtual node at front of list
	
		temp_node = first_blossom;
		
		first_blossom = virtual_node;
		 
		
		node_we_are_opting_on										= opt_node; //for now
 
		 // initialize the virtual node and set as a tree_root_node
		 // distinguishing feature is that its node number is VIRTUAL_NODE_NUMBER
		 
		(virtual_node).this_is_contained_with_me									= temp_node;
		
		(virtual_node).width			= 0.0f;
		(virtual_node).node_number			= VIRTUAL_NODE_NUMBER;
	
		(virtual_node).i_am_contained_in											= null;
		(virtual_node).i_contain 													= null;
		(virtual_node).first_connection.node_in_other_to_which_i_am_connected		= null;			
		(virtual_node).first_connection.is_a_marriage 							= false;			
		(virtual_node).second_connection.node_in_other_to_which_i_am_connected	= null;
		(virtual_node).second_connection.is_a_marriage 							= false;
		(virtual_node).node_in_me_connected_out_towards_tree_root_node 			= null;
		(virtual_node).i_am_in_tree 												= false;
		(virtual_node).i_am_expanding 											= false;
	
		if( ! set_up_new_tree_root_node(VIRTUAL_NODE_NUMBER ) )
				return( false ); //something bad happened
				
		//attach opt_node and its spouse to new tree
		add_blossom_to_tree( opt_node , virtual_node );
		
		action_to_be_taken = ADD_TO_TREE;

					
	}
	else
	{
		//we have already been post_opting
		
		support.output_highlite_node( (node_we_are_opting_on).node_number , false );
		
		if( opt_node !=  node_we_are_opting_on )
		{
			//start a new tree to opt on the new outermost blossom
			
			if( action_to_be_taken == MAKE_NEW_BLOSSOM )
			{
				// the user decided not to make the new blossom
				support.output_remove_tight_line( (first_affected_blossom).node_number, (second_affected_blossom).node_number );
			}
			reset_tree();
			
			
			if( ! set_up_new_tree_root_node( VIRTUAL_NODE_NUMBER ) )
				return( false ); //something bad happened
			
			node_we_are_opting_on = opt_node;
			
			virtual_node = find_node_with_number( VIRTUAL_NODE_NUMBER );
				
			//attach opt_node and its spouse to new tree
			add_blossom_to_tree( opt_node , virtual_node );
			action_to_be_taken = ADD_TO_TREE;
			
		}
		else
		{
			//everything should be set to continue as normal
		}
		
	}
	

	// ask it to grow tree starting from virtual node.  Be sure to disolve any
	//blossoms that are meant to be dissolved
		
	//action_to_be_taken 		= NOTHING;

	support.output_highlite_node( (node_we_are_opting_on).node_number , true );
	
	value = step( VIRTUAL_NODE_NUMBER );
	
	switch( action_to_be_taken )
	{
	
	case AUGMENT :
		//this was set because we had an unbounded grow.
		// just warn the user and continue as usual
		
//		SysBeep(1);
		action_to_be_taken = NOTHING;
		support.output_waiting_for_next_node(	true );
		break;
	    
	case DISSOLVE_BLOSSOM : 
		support.output_waiting_for_next_node( false );	
		//SysBeep(0);
		break;
			
	default :
		support.output_waiting_for_next_node(	true );
	
	}
	
	return( value );

}


public boolean step( short node_number)
{

	boolean	dont_show_next_iteration;   //when true, we are doing a degenerate tree grow

	

	// safety check to see if we are totally done
	
	if( null == find_first_tree() && NOTHING == action_to_be_taken && null == node_we_are_opting_on )
	{
	
		//support.output_done();
		reset_tree();
		support.output_ready_for_post_opt();
	
		return( false );

	}



	
	switch( action_to_be_taken )
	{
	
		case DISSOLVE_BLOSSOM:
		
		
			
			dissolve_blossom();
						
			
			// we don't to continue on through the rest of the function and do the next bump
	
			action_to_be_taken = NOTHING;

			support.output_augmenting_now( false );

			support.output_waiting_for_next_node( false );

			return( true );
	
			//break;
			
		case MAKE_NEW_BLOSSOM:
		
			
			make_new_blossom();
		
		
			// we want to continue on through the rest of the function and do the next bump

			break;
		
		case AUGMENT:

			if( our_augment_queue.is_there_an_item_to_do() )
			{
			
				our_augment_queue.do_next_augment_item(support);
						
			}
			else	// there was no item to do in the augment queue
			{
			
				augment();
					
			}
			
			// check to see if we have more augmenting left to do
			
			if( our_augment_queue.is_there_an_item_to_do() )
			{
			
				support.output_augmenting_now( true );
			
				support.output_waiting_for_next_node( false );

				action_to_be_taken = AUGMENT;

			}
			else	// the augment queue is now empty
			{

				reset_tree();

			
				// check to see if we just finished completely
		
				if( null == find_first_tree() )
				{
					
					support.output_augmenting_now( false );
	
					support.output_waiting_for_next_node( false );

					support.output_done();
					//support.output_ready_for_post_opt();
	
				}
				else	// we have more to do
				{
		
					support.output_augmenting_now( false );
	
					support.output_waiting_for_next_node( true );

					action_to_be_taken = NOTHING;

				}
			
			}

			// we DON'T want to continue on through this function when augmenting
						
			return( true );
			
	
			//break;		

		default: 

			// check to see if we need a new tree root

			if( null == tree_root_node  )
			{

				// set up a new tree_root_node
			
				if( ! set_up_new_tree_root_node( node_number ) )
				{
					
					// if this failed then we were given an invalid node_number
				
	
					support.output_augmenting_now( false );
	
					support.output_waiting_for_next_node( true );

					return( false );
				
				}
							
			}		

			break;
		
	}
	

	distance_by_which_to_change = find_next_bump();

	bumped_into_infinity = false;	
		
	if( DISTANCE_TO_NODE_AT_INFINITY_FACTOR*support.input_graph_diameter() == distance_by_which_to_change )
	// FUNNY    something might be funny here.  Perhaps the == should be <= ?
	{
		// the tree has only one expanding moat and no nodes are outside of the tree
		// the node with largest radius should marry infinity
		 
		// 	find the node IN the tree which is affected -- augment uses
		// 	second_affected_blossom as this node in the tree

		second_affected_blossom = find_expanding_node_in_tree_with_biggest_radius();
		bumped_into_infinity = true;
	
		action_to_be_taken = ADD_TO_TREE;
	
	}	
		
	
	// modify our disks radius's and tell the rest of the program what to shrink and grow


	shrink_and_grow_the_tree();
	

	support.output_change_by( distance_by_which_to_change );
	
		
	// act on what find_next_bump found
	
	switch( action_to_be_taken )
	{

		case MAKE_NEW_BLOSSOM:

			//  find-next_bump modified some of our global variables:
			//  first_affected_blossom and second_affected_blossom are now both
			//  nodes located in outermost blossoms in the tree which are colliding

			// add the tight line where the two nodes in expanding blossoms collided

			support.output_add_tight_line( (first_affected_blossom).node_number, (second_affected_blossom).node_number );
			

			// we will make the new blossom at the start of the next step

			break;

		case ADD_TO_TREE:
		
			
			if( bumped_into_infinity )
			{

					action_to_be_taken = AUGMENT;
	
					support.output_augmenting_now( true );
	
					support.output_waiting_for_next_node( false );
	
					return( true );
						
			}
			else	// it's a normal grow
			{
				
				// add the tight line where the two nodes collided
	
				support.output_add_tight_line( (first_affected_blossom).node_number, (second_affected_blossom).node_number );
						
				
				// find_next_bump modified some of our global variables:
				//  first_affected_blossom is now the node not in the tree which is
				// 	bumped by the node second_affected_blossom in the tree
				
				// add_blossom_to_tree will add the outermost blossom containing the
				// 	node first_affected_blossom to the outermost blossom in the tree
				// 	containing the node second_affected_blossom
				
				add_blossom_to_tree( first_affected_blossom, second_affected_blossom );
				
				if( is_single( first_affected_blossom ) )
				{
	
					action_to_be_taken = AUGMENT;
	
					support.output_augmenting_now( true );
	
					support.output_waiting_for_next_node( false );
	
					return( true );
	
				}
			
			}
	
			
			break;
	
		case DISSOLVE_BLOSSOM:
		
			// do nothing special at this point, just continue on with the rest of the function			
			// 	we will dissolve the new blossom at the start of the next step

			break;


		case NOTHING:
			
			if( distance_by_which_to_change >=  DISTANCE_TO_NODE_AT_INFINITY_FACTOR*support.input_graph_diameter() )
			{
				// we had an unbounded grow
				action_to_be_taken = AUGMENT;
			}
			else
			{
				//do nothing  probably a disc went to zero and we are just pausing
			}
			
			break;

		default:
		
			// there's nothing we can do
			
			support.output_done();
			//support.output_ready_for_post_opt();
			
			return( false );
			
			//break;	

	
	}

	
	// if we made it this far, then either the node number we were passed was valid or
	// 	we weren't looking for a node number anyway, so tell the world that the node number
	// 	we were passed was o.k.


	support.output_augmenting_now( false );

	support.output_waiting_for_next_node( false );

if (ZERO_SHOW)
{

	previous_action_to_be_taken = action_to_be_taken;
	
	// we need to remember the previous action, because we really want to do the
	// action that find_next_bump suggests in the following test, only if it
	// actually is a zero growth iteration.
	
	while( ! support.input_is_zero_growth_being_shown( ) && 
		    ADD_TO_TREE == action_to_be_taken  &&
		    0 == find_next_bump() )   				//changes action_to_be_taken
	{
		previous_action_to_be_taken = action_to_be_taken;

		support.output_add_tight_line( (first_affected_blossom).node_number, (second_affected_blossom).node_number );
		
		add_blossom_to_tree( first_affected_blossom, second_affected_blossom );
		
		if( is_single( first_affected_blossom ) )
		{
			action_to_be_taken = AUGMENT;
			support.output_augmenting_now( true );
			support.output_waiting_for_next_node( false );
			return( true );
		}
	}
	
	action_to_be_taken = previous_action_to_be_taken;

} // end zero show

	return( true );

}

public void close()
{





}

private float find_next_bump()
{

	blossom current_blossom, temp_blossom;
	float	distance_to_change;


	// we want to look for any of the following cases:
	//
	//  -- any node in an expanding outermost blossom colliding with any node in another
	// 		expanding outermost blossom 
	//  -- any node in an expanding outermost blossom colliding with any node in an
	// 		outermost blossom which isn't in the tree (so it's neither expanding or shrinking)
	// 	-- any blossom's width going to zero
	//
	// modifies first_blossom and second_blossom
	// whose meaning depends on what bumpped.
	// and action_to_be_taken.

	// returns distance by which to change.


	// so let current_blossom move along the outermost blossoms
	
	distance_to_change = DISTANCE_TO_NODE_AT_INFINITY_FACTOR*support.input_graph_diameter() ;	// something big so that checking for minimum works
	first_affected_blossom = null;
	second_affected_blossom = null;

	for( current_blossom = first_blossom; null != current_blossom; current_blossom = (current_blossom).this_is_contained_with_me )
	{
		
		if( current_blossom.is_a_node() && VIRTUAL_NODE_NUMBER == (current_blossom).node_number )
			continue; // we do not want to compare the virtual node with anything!
			
		if( (current_blossom).i_am_in_tree )
		{

			if( (current_blossom).i_am_expanding )
			{
			
				// look at all the outermost blossoms to the "right" which are not in tree or are expanding
				
				for( temp_blossom = (current_blossom).this_is_contained_with_me; null != temp_blossom; temp_blossom = (temp_blossom).this_is_contained_with_me )
				{
				
					if( (temp_blossom).i_am_in_tree )
					{
					
						if( (temp_blossom).i_am_expanding )
						{
						
							distance_to_change = compare_blossoms( current_blossom, temp_blossom, distance_to_change, true );
					
						}
						
						// else temp_blossom is shrinking so ignore it
					
					}
					else	// temp_blossom is not in tree
					{
					
						// notice we make sure the blossom out of the tree is always first
						// 	--this is so that later we will know which one to add to the tree

						//System.out.println("in find_next_bump" + distance_to_change);
						distance_to_change = compare_blossoms( temp_blossom, current_blossom, distance_to_change, false );

					}
					
				}
			
			}
			else	// current_blossom is shrinking
			{
			
				// check to see of the current_blossom is not a trivial blossom
				//  -- because nodes can go negative in non-triangle inequality metrics
				
				if( ! current_blossom.is_a_node() )
				{
	
					// check to see if it's width will go to zero
				
					if( (current_blossom).width < distance_to_change )
					{
					
						distance_to_change = (current_blossom).width;
						action_to_be_taken = DISSOLVE_BLOSSOM;
						first_affected_blossom = current_blossom;
						second_affected_blossom = null;
					
					}
				}
				else
				{
					//  it is a node, and it is about to go negative, then do nothing so we get a pause for visual effect.

					if( (current_blossom).width < distance_to_change && (current_blossom).width > 0 )
					{
						
						distance_to_change = (current_blossom).width;
						action_to_be_taken = NOTHING;
						first_affected_blossom = null;
						second_affected_blossom = null;


					}
					
				}
								
			}

		}
		else	// current_blossom isn't in tree
		{
		
			for( temp_blossom = (current_blossom).this_is_contained_with_me; null != temp_blossom; temp_blossom = (temp_blossom).this_is_contained_with_me )
			{
			
				if( (temp_blossom).i_am_in_tree )
				{
				
					if( (temp_blossom).i_am_expanding )
					{
						
						// notice we make sure the blossom out of the tree is always first
						// 	--this is so that later we will know which one to add to the tree
										
						distance_to_change = compare_blossoms( current_blossom, temp_blossom, distance_to_change, false );
				
					}
					
					// else temp_blossom is shrinking so ignore it
				
				}

				// else temp_blossom is not in tree so ignore it
		
			}
		
		}
	
	}
	

	return( distance_to_change );

}


private float compare_blossoms(	blossom blossom_one,
				blossom blossom_two,
				float distance_to_change,
				boolean are_both_expanding)
{
	float temp_distance;
	blossom temp_node_one, temp_node_two;

	// safety check
		
	if( null == blossom_one || null == blossom_two )
	{
		return( 0.0f );
	}
		
	//find first node in blossom_one
	
	temp_node_one = blossom_one; 
	
	while( ! temp_node_one.is_a_node() )  
	{
		temp_node_one = (temp_node_one).i_contain;
	}			
		
	while( true )
	{
		//find first node in blossom_two
		
		temp_node_two = blossom_two; 
		
		while( ! temp_node_two.is_a_node() )
		{
			temp_node_two = (temp_node_two).i_contain;
		}			
			
		while( true )
		{
			// lets actually compare the two temp_nodes

			temp_distance = support.input_distance( (temp_node_one).node_number, (temp_node_two).node_number )
								- ( (temp_node_one).width + (temp_node_two).width );

			//System.out.println("in compare_blossoms, temp_distance " + temp_distance);
			//System.out.println("in compare_blossoms, distance_to_change " + distance_to_change);
			
			if( are_both_expanding )
			{
				temp_distance /= 2f;
			}

			if( temp_distance < distance_to_change )
			{
			
				distance_to_change = temp_distance;
				action_to_be_taken = ( are_both_expanding ? MAKE_NEW_BLOSSOM: ADD_TO_TREE );

				// elsewhere, when we called this function, we made sure that if one of
				// 	the blossoms was not in the tree, it was the first argument
				// 	we preserve this order now so that later we will know that 
				// 	(if applicable) first_affected_blossom is the one to add to the tree

				first_affected_blossom = temp_node_one;
				second_affected_blossom = temp_node_two;
			}
			
			
			//we now find the next node in blossom_two
			
			while( temp_node_two != blossom_two  && null == (temp_node_two).this_is_contained_with_me )
			{
				temp_node_two = (temp_node_two).i_am_contained_in;
			}
			
			if( temp_node_two == blossom_two )
			{
				break;  //  finished traversing the blossom_two
			}
			
			temp_node_two = (temp_node_two).this_is_contained_with_me;
			
			while( ! temp_node_two.is_a_node() )
			{
				temp_node_two = (temp_node_two).i_contain ;
			}
						
		} // end while  We have found a new temp_node_two 
			
		
		//we now find the next node in blossom_one
		
		while( temp_node_one != blossom_one  && null == (temp_node_one).this_is_contained_with_me )
		{
			temp_node_one = (temp_node_one).i_am_contained_in;
		}
		
		if( temp_node_one == blossom_one )
		{
			break;  //  finished traversing the blossom_one
		}
		
		temp_node_one = (temp_node_one).this_is_contained_with_me;
		
		while( ! temp_node_one.is_a_node() )
		{
			temp_node_one = (temp_node_one).i_contain;
		}			
		
	} // end while  we have found a new temp_node_one to compare with all of blossom_two
	
	//System.out.println("end of compare_blossoms" + distance_to_change);

	return( distance_to_change );
}




private void make_new_blossom()
{

	blossom new_blossom, temp_blossom, previous_blossom;
	blossom outermost_blossom_containing_first_affected_node;
	blossom outermost_blossom_containing_second_affected_node;
	blossom blossom_in_tree_where_their_paths_meet;
	blossom in_first_arm_before_meeting_place, in_second_arm_before_meeting_place;
	blossom outermost_blossom_containing_tree_root;
	blossom temp_towards_tree_root;
	
	// safety check -- try to allocate memory for the new blossom

	new_blossom = new blossom();
	

	// initialize the new blossom
	
//if (DEBUGGING)
//{	
//	// for debugging pusposes, we number blossoms
//
//	(new_blossom).node_number	= blossom_counter++;
//}	

	(new_blossom).width				= 0.0f;
	
	(new_blossom).i_am_contained_in										= null;
	(new_blossom).i_contain 												= null;
	(new_blossom).this_is_contained_with_me								= null;
	(new_blossom).first_connection.node_in_other_to_which_i_am_connected	= null;			
	(new_blossom).first_connection.is_a_marriage 							= false;			
	(new_blossom).second_connection.node_in_other_to_which_i_am_connected	= null;
	(new_blossom).second_connection.is_a_marriage 						= false;
	(new_blossom).node_in_me_connected_out_towards_tree_root_node 		= null;

	(new_blossom).i_am_in_tree 											= true;
	
	(new_blossom).i_am_expanding 											= true;

	
	// put the new blossom into the list of outermost blossoms

	make_blossom_be_contained( new_blossom, null );


	outermost_blossom_containing_first_affected_node = find_outermost_blossom_containing( first_affected_blossom );
	outermost_blossom_containing_second_affected_node = find_outermost_blossom_containing( second_affected_blossom );
	
	blossom_in_tree_where_their_paths_meet = find_meeting_outermost_blossom( outermost_blossom_containing_first_affected_node, outermost_blossom_containing_second_affected_node );
	
	
	current_colour++;

	add_connection_in_blossom( new_blossom, get_spouse_node_of( blossom_in_tree_where_their_paths_meet ), true );
	remove_connection_in_blossom( blossom_in_tree_where_their_paths_meet, get_spouse_node_of( blossom_in_tree_where_their_paths_meet ) );

	// deal with the two affected blossoms first, unless one of them is the actually the
	// 	blossom in the tree where the two paths meet, in which case we want to leave
	// 	it alone until later

	add_connection_in_blossom( outermost_blossom_containing_first_affected_node, second_affected_blossom, false );
	add_connection_in_blossom( outermost_blossom_containing_second_affected_node, first_affected_blossom, false );

	if( outermost_blossom_containing_first_affected_node != blossom_in_tree_where_their_paths_meet )
	{

		// we need to get save this handle before we modify temp_node because
		// 	modifying it will ruin our chances of getting any information out of it
		//  -- next_outermost_blossom_towards_tree_root_from relies on the fact that
		// 	the given blossom and the sought after blossom will be contained
		// 	in the same blossom -- this won't be true once we move temp_blossom into new_blossom

		temp_towards_tree_root = next_outermost_blossom_towards_tree_root_from( outermost_blossom_containing_first_affected_node );

		make_blossom_not_be_contained_in_anything( outermost_blossom_containing_first_affected_node );
		make_blossom_be_contained( outermost_blossom_containing_first_affected_node, new_blossom );
		add_discs_to_all_nodes_in_blossom( outermost_blossom_containing_first_affected_node );

		// now go around the first arm of the tree, removing the blossoms from being
		// 	in the outermost level, and adding them to the new moat
		
		temp_blossom = temp_towards_tree_root;
		previous_blossom = outermost_blossom_containing_first_affected_node;
		
		while( null != temp_blossom && temp_blossom != blossom_in_tree_where_their_paths_meet )
		{
		
			if( (temp_blossom).i_am_expanding )
			{
			
				add_connection_in_blossom( temp_blossom, (previous_blossom).node_in_me_connected_out_towards_tree_root_node, false);
			
			}
			
			// else, if temp_blossom is shrinking, there is no need to add a new connection down the tre,
			// 	it should already be there, because we added the connection to when we added
			// 	that blosssom to the tree in the first place
		
			temp_towards_tree_root = next_outermost_blossom_towards_tree_root_from( temp_blossom );

			make_blossom_not_be_contained_in_anything( temp_blossom );
			make_blossom_be_contained( temp_blossom, new_blossom );
			add_discs_to_all_nodes_in_blossom( temp_blossom );
		
		
			previous_blossom = temp_blossom;	
			temp_blossom = temp_towards_tree_root;
		
		}
		
		// store previous_blossom for connecting up the meeting blossom
		
		in_first_arm_before_meeting_place = previous_blossom;

		add_connection_in_blossom( blossom_in_tree_where_their_paths_meet, (in_first_arm_before_meeting_place).node_in_me_connected_out_towards_tree_root_node, false);

	}

	
	
	// now go around the second arm, doing the same thing

	if( outermost_blossom_containing_second_affected_node != blossom_in_tree_where_their_paths_meet )
	{

		temp_towards_tree_root = next_outermost_blossom_towards_tree_root_from( outermost_blossom_containing_second_affected_node );

		make_blossom_not_be_contained_in_anything( outermost_blossom_containing_second_affected_node );
		make_blossom_be_contained( outermost_blossom_containing_second_affected_node, new_blossom );
		add_discs_to_all_nodes_in_blossom( outermost_blossom_containing_second_affected_node );
	
		
		
		temp_blossom = temp_towards_tree_root;
		previous_blossom = outermost_blossom_containing_first_affected_node;
		
		while( null != temp_blossom && temp_blossom != blossom_in_tree_where_their_paths_meet )
		{
		
			if( (temp_blossom).i_am_expanding )
			{
			
				add_connection_in_blossom( temp_blossom, (previous_blossom).node_in_me_connected_out_towards_tree_root_node, false);
			
			}
			
			// else, if temp_blossom is shrinking, there is no need to add a new connection down the tre,
			// 	it should already be there, because we added the connection to when we added
			// 	that blosssom to the tree in the first place

			temp_towards_tree_root = next_outermost_blossom_towards_tree_root_from( temp_blossom );
		
			make_blossom_not_be_contained_in_anything( temp_blossom );
			make_blossom_be_contained( temp_blossom, new_blossom );
			add_discs_to_all_nodes_in_blossom( temp_blossom );
		
		
			previous_blossom = temp_blossom;	
			temp_blossom = temp_towards_tree_root;
		
		}
	
		// store previous_blossom for connecting up the meeting blossom
		
		in_second_arm_before_meeting_place = previous_blossom;
	
		add_connection_in_blossom( blossom_in_tree_where_their_paths_meet, (in_second_arm_before_meeting_place).node_in_me_connected_out_towards_tree_root_node, false);

	}	

	
	make_blossom_not_be_contained_in_anything( blossom_in_tree_where_their_paths_meet );
	make_blossom_be_contained( blossom_in_tree_where_their_paths_meet, new_blossom );
	add_discs_to_all_nodes_in_blossom( blossom_in_tree_where_their_paths_meet );


	// we don't need to worry about the way the new blossom interacts with outermost blossoms
	//  in the tree that are related to constituents of the new blossom because
	// 	the way we stored these relations -- through the specific node to which a blossom
	// 	is connected -- means that nothing has changed for them

}


private void dissolve_blossom()
{


	blossom blossom_to_dissolve;
	blossom current_blossom, next_blossom;
	blossom first_blossom, last_blossom;
	blossom saved_non_spouse_node=null;
	blossom temp_node_in_next_which_is_tight_out;
	
	
	boolean odd_number_of_blossoms_away_from_last_blossom;
	
	
	// get the information we need from the "global" variables
	// 	-- first_affected_blossom contains the blossom who's width has shrunk to 0
	
	blossom_to_dissolve = first_affected_blossom;
	
	remove_discs_from_all_nodes_in_blossom( blossom_to_dissolve );
	
	
	first_blossom = find_containing_blossom_which_is_inside_given_blossom( (blossom_to_dissolve).node_in_me_connected_out_towards_tree_root_node, blossom_to_dissolve );

	last_blossom = find_containing_blossom_which_is_inside_given_blossom( get_node_in_me_which_is_married_out( blossom_to_dissolve ), blossom_to_dissolve );

	// we have to check whether or not in blossom_to_dissolve the tree jumps into the 
	// 	same blossom as it jumps out of -- this will affect how we treat the 
	// 	rest of the blossoms in blossom_to_dissolve
	
	if( first_blossom == last_blossom )
	{
	
		// special case when tree jumps into blossom_to_dissolve in same place it jumps out
		// 	all the blossoms but that one are booted out of the tree
		
		// just pick one way to go around the cycle -- get_non_spouse_of returns the first connection
	
		current_blossom = first_blossom;
		next_blossom = get_non_spouse_of( current_blossom );
		
		// set this up for the first even blossom we'll be doing

		temp_node_in_next_which_is_tight_out = get_non_spouse_node_of( current_blossom );

		odd_number_of_blossoms_away_from_last_blossom = true;
		
		do
		{
			
			current_blossom = next_blossom;
			
			odd_number_of_blossoms_away_from_last_blossom = ! odd_number_of_blossoms_away_from_last_blossom;

			if( odd_number_of_blossoms_away_from_last_blossom )
			{
			
				next_blossom = get_non_spouse_of( current_blossom );

				// set this up for the next blossom -- and for ourselves
				
				temp_node_in_next_which_is_tight_out = get_non_spouse_node_of( current_blossom );
				
				support.output_remove_tight_line( (get_node_in_me_which_is_tight_out( current_blossom )).node_number, (temp_node_in_next_which_is_tight_out).node_number );
			
			}
			else	// even number of blossoms away from spouse
			{
	
				// we want to be careful to save things first before we modify a blossom
	
				next_blossom = get_spouse_of( current_blossom );
				

				// the function get_node_in_me_which_is_tight_out won't work anymore
				// 	because we just came from disconnecting the blossom to which
				// 	the current_node is tight -- we use temp_node_in_next_which_is_tight_out
				// 	which the last blossom left for us
			
				support.output_remove_tight_line( (temp_node_in_next_which_is_tight_out).node_number, (get_non_spouse_node_of( current_blossom )).node_number );
				
			}
				
			// now modify the blossom:
			
			// it should no longer have tight connections
	

			remove_connection_in_blossom( current_blossom, get_non_spouse_node_of( current_blossom ) );

			
			// it should no longer be in blossom_to_dissolve, and should be
			// 	put back into the pool of outermost blossoms as not in the tree
			
			make_blossom_not_be_contained_in_anything( current_blossom );
			
			make_blossom_be_contained( current_blossom, null );
		
		
			// we should make sure it doesn't think it's in the tree anymore
		
			(current_blossom).i_am_in_tree = false;
		
		}
		while( next_blossom != last_blossom );
	
	
		// now take care of the last blossom
	

		// last_blossom will need to take over some information about the tree structure from blossom_to_dissolve
		
		(last_blossom).node_in_me_connected_out_towards_tree_root_node = (blossom_to_dissolve).node_in_me_connected_out_towards_tree_root_node;
		
		
		remove_connection_in_blossom( last_blossom, get_non_spouse_node_of( last_blossom ) );
		
		
		// put the marriage of the blossom_to_dissolve in it's place
		
		add_spouse_to_blossom( last_blossom, get_spouse_node_of( blossom_to_dissolve ) );
		
		
		// now remove what used to be the second non-spouse connection of last blossom
		
		remove_connection_in_blossom( last_blossom, get_non_spouse_node_of( last_blossom ) );


		// put the non-spouse of the blossom_to_dissolve in it's place
		
		add_non_spouse_to_blossom( last_blossom, get_non_spouse_node_of( blossom_to_dissolve ) );
	
	}
	else	// the tree passes through more than one blossom of blossom_to_dissolve
	{
	
		// we need to start at the place where the tree comes into blossom_to_dissolve
		// 	and make an augmenting path of blossoms which will remain in the tree
		// 	-- the rest are booted out
				
		
		// we do the augmenting path first
		
		current_blossom = first_blossom;
		next_blossom = get_spouse_of( current_blossom );
		
		odd_number_of_blossoms_away_from_last_blossom = false;
		
		do
		{
			
			current_blossom = next_blossom;
			
			odd_number_of_blossoms_away_from_last_blossom = ! odd_number_of_blossoms_away_from_last_blossom;

			if( odd_number_of_blossoms_away_from_last_blossom )
			{
	
				// we want to be careful to save things first before we modify a blossom
	
				next_blossom = get_non_spouse_of( current_blossom );

				saved_non_spouse_node = get_non_spouse_node_of( current_blossom );
				
				(current_blossom).i_am_expanding = true;
							
				remove_connection_in_blossom( current_blossom, get_non_spouse_node_of( current_blossom ) );
			
			}
			else	// even number of blossoms away from spouse
			{
			
				next_blossom = get_spouse_of( current_blossom );

				(current_blossom).i_am_expanding = false;
			
				(current_blossom).node_in_me_connected_out_towards_tree_root_node = saved_non_spouse_node;
		
				
			
			}
				
			// now modify the blossom:
			
			// it should no longer be in blossom_to_dissolve, and should be
			// 	put back into the pool of outermost blossoms as part of the tree
			
			make_blossom_not_be_contained_in_anything( current_blossom );
			
			make_blossom_be_contained( current_blossom, null );
		
		
			// we should make sure it thinks it's in the tree anymore
		
			(current_blossom).i_am_in_tree = true;
		
		}
		while( next_blossom != last_blossom );
	
		
		// now go along the other side, removing things from the tree
		
		current_blossom = first_blossom;
		next_blossom = get_non_spouse_of( current_blossom );


		// set this up for the first even blossom we'll be doing

		temp_node_in_next_which_is_tight_out = get_non_spouse_node_of( current_blossom );
		
		
		odd_number_of_blossoms_away_from_last_blossom = true;
		
		// this time we have to check first because there's no garantee that
		// 	there will actually be blossoms on this arm of the cycle between
		// 	the first_blossom and the last_blossom
		
		while( next_blossom != last_blossom )
		{
			
			current_blossom = next_blossom;
			
			odd_number_of_blossoms_away_from_last_blossom = ! odd_number_of_blossoms_away_from_last_blossom;

			if( odd_number_of_blossoms_away_from_last_blossom )
			{
	
				// we want to be careful to save things first before we modify a blossom
	
				next_blossom = get_non_spouse_of( current_blossom );
				
				
				// set this up for the next blossom -- and for ourselves
				
				temp_node_in_next_which_is_tight_out = get_non_spouse_node_of( current_blossom );
				
				support.output_remove_tight_line( (get_node_in_me_which_is_tight_out( current_blossom )).node_number, (temp_node_in_next_which_is_tight_out).node_number );
				
			}
			else	// even number of blossoms away from spouse
			{
			
				next_blossom = get_spouse_of( current_blossom );
			
			
				// the function get_node_in_me_which_is_tight_out won't work anymore
				// 	because we just came from disconnecting the blossom to which
				// 	the current_node is tight -- we use temp_node_in_next_which_is_tight_out
				// 	which the last blossom left for us
			
				support.output_remove_tight_line( (temp_node_in_next_which_is_tight_out).node_number, (get_non_spouse_node_of( current_blossom )).node_number );
			
			}
				
			// now modify the blossom:
			
			// it should no longer have tight connections

			remove_connection_in_blossom( current_blossom, get_non_spouse_node_of( current_blossom ) );
			
			// it should no longer be in blossom_to_dissolve, and should be
			// 	put back into the pool of outermost blossoms as not in the tree
			
			make_blossom_not_be_contained_in_anything( current_blossom );
			
			make_blossom_be_contained( current_blossom, null );
		
		
			// we should make sure it doesn't think it's in the tree anymore
		
			(current_blossom).i_am_in_tree = false;
		
		}
				
		
		// special case when the blossom only has three blossoms in it -- we will
		//  never have had a chance to make the display of the tight line between
		//  the first_blossom and and last_blossom disappear, because there
		// 	are no intervening blossoms between them (it is usually while
		// 	doing the intervening blossoms that we remove the display of the obsolete
		// 	tight lines
		
		if( last_blossom == get_non_spouse_of( first_blossom ) )
		{
		
			support.output_remove_tight_line( (get_node_in_me_which_is_tight_out( first_blossom )).node_number, (get_non_spouse_node_of( first_blossom )).node_number );
		
		}
		
		
		
		// now deal with first_blossom
		
		// it should no longer be in blossom_to_dissolve, and should be
		// 	put back into the pool of outermost blossoms as not in the tree
		
		make_blossom_not_be_contained_in_anything( first_blossom );
		
		make_blossom_be_contained( first_blossom, null );
	
		
		remove_connection_in_blossom( first_blossom, get_non_spouse_node_of( first_blossom ) );

		add_non_spouse_to_blossom( first_blossom, get_non_spouse_node_of( blossom_to_dissolve ) );
		
	
		// we should make sure it thinks it's in the tree
	
		(first_blossom).i_am_in_tree = true;
	
		(first_blossom).i_am_expanding = false;
	
		
		// first_blossom will need to take over some information about the tree structure from blossom_to_dissolve
	
		(first_blossom).node_in_me_connected_out_towards_tree_root_node = (blossom_to_dissolve).node_in_me_connected_out_towards_tree_root_node;

		
		
		// now deal with last_blossom
		
		// last_blossom will need to take over some information about the tree structure
	
		(last_blossom).node_in_me_connected_out_towards_tree_root_node = saved_non_spouse_node;
		

		// remove the tight connection to the arm of the blossom which will get left out
		// 	of the tree -- current_blossom is still pointing to the first blossom in
		// 	that direction -- we use the fact that current_blossom is now an outermost blossom
		// 	to figure out which of the two non-spouse connections in last_blossom needs
		// 	to be disconnected
		
		
		if( current_blossom == find_outermost_blossom_containing( get_non_spouse_node_of( last_blossom ) ) )
		{
		
			remove_connection_in_blossom( last_blossom, get_non_spouse_node_of( last_blossom ) );
		
		}
		else	// we want to remove the second connection
		{
		
			remove_connection_in_blossom( last_blossom, (last_blossom).second_connection.node_in_other_to_which_i_am_connected );
		
		}


		// put the marriage of the blossom_to_dissolve in it's place
		
		add_spouse_to_blossom( last_blossom, get_spouse_node_of( blossom_to_dissolve ) );
		
		
	
	
	}

	
	// a little bit of stuff that has to be done to last_blossom
	// 	in both of the two main cases above
	
	// it should no longer be in blossom_to_dissolve, and should be
	// 	put back into the pool of outermost blossoms as not in the tree
	
	make_blossom_not_be_contained_in_anything( last_blossom );
	
	make_blossom_be_contained( last_blossom, null );


	// make sure the tree structure is preserved

	(last_blossom).i_am_in_tree = true;
	
	(last_blossom).i_am_expanding = false;
	
	
	// now ditch the blossom_to_dissolve
	
	make_blossom_not_be_contained_in_anything( blossom_to_dissolve );
	
	//DisposeHandle( blossom_to_dissolve );
	
}


private void augment()
{

	// augmentation through the outermost blossoms is ALMOST like augmentation
	// 	through a normal blossom, except that we must make sure that we start
	// 	the augmentation at the node IN THE TREE which bumped


	// so take care of the augmentation between the node in the tree and the node
	// 	out of the tree which bumped right now -- but don't add the new spouse to
	// 	the node in the tree yet -- leave it alone so that recursive_subroutine_augment_blossom
	// 	will know the correct spouse to go down the tree -- just add the new spouse
	// 	to the node out of the tree -- it shouldn't affect things
	//  Don't do this if we are marrying the node in tree to infinity
	
	
	if( ! bumped_into_infinity )
	{
	
		// note that we know that the node out of the tree which bumped is an outermost blossom
		
		remove_connection_in_blossom( first_affected_blossom, second_affected_blossom );
		add_connection_in_blossom( first_affected_blossom, second_affected_blossom, true );
		
		
		// we can SHOW the marriage between the two nodes which bumed, even if we
		// 	haven't totally changed our data to reflect that change yet
		
		support.output_remove_tight_line( (first_affected_blossom).node_number, (second_affected_blossom).node_number );
		support.output_marry( (first_affected_blossom).node_number, (second_affected_blossom).node_number );

	}


	// now work our way through the outermost blossoms from the node in the tree
	// 	(second_affected_blossom) which just bumped the node out of the tree
	// 	(first_affected_blossom), all the way back to the tree_root_node
	// -- passing null to recursive_subroutine_augment_blossom is our way of saying that
	// 	the blossom it must augment is the universe of outermost blossoms

	recursive_subroutine_augment_blossom( null, second_affected_blossom, tree_root_node );


		
	add_connection_in_blossom( find_outermost_blossom_containing( second_affected_blossom ), first_affected_blossom, true );

}


private void recursive_subroutine_augment_blossom( blossom blossom_to_augment, blossom from_node, blossom to_node )
{

	blossom current_blossom, next_blossom;
	blossom node_in_me_which_connects_me_away_from_tree_root;
	blossom temp_spouse_node, another_temp_spouse_node, temp_non_spouse_node=null;
	blossom temp_node_in_me_which_is_tight_out=null;
	
	blossom last_blossom_to_augment;
	blossom first_blossom_to_augment;

	boolean	odd_number_of_blossoms_away_from_last_blossom;
	boolean we_are_augmenting_at_outermost_level;
	
	// safety checks
	
	if( from_node == to_node || null == from_node || null == to_node )
	{
		return;
	}

	
	if( null == blossom_to_augment )
	{
	
		// we are augmenting through the outermost blossoms in the tree , which is
		// 	special in some ways and requires different actions later
		
		we_are_augmenting_at_outermost_level = true;
	
	}
	else	// we are inside a blossom
	{
	
		we_are_augmenting_at_outermost_level = false;
	
	}
	
	first_blossom_to_augment = find_containing_blossom_which_is_inside_given_blossom( from_node, blossom_to_augment );

	last_blossom_to_augment = find_containing_blossom_which_is_inside_given_blossom( to_node, blossom_to_augment );

	if( first_blossom_to_augment == last_blossom_to_augment )
	{
	
		recursive_subroutine_augment_blossom( first_blossom_to_augment, from_node, to_node );
	
		// the tree jumped into the same blossom in blossom_to_augment as it jumped out of
	
		return;
	
	}
	
	// if we got to here, we know we are supposed to do some augmenting in this blossom
		
	// now, if (in blossom_to_augment) the blossom the tree jumped into isn't the
	// 	same as the blossom the tree jumped out of, we know from the structure of
	// 	blossoms that the blossoms in blossom_to_augment will come in pairs:
	// -- first_blossom_to_augment will be married to the next one toward the tree root,
	// 	then that one will be tight with the next one, then married, then tight, 
	// 	then married, until we come to a blossom which will be tight with
	// 	last_blossom_to_augment
	
	current_blossom = first_blossom_to_augment;
	
	next_blossom = get_spouse_of( current_blossom );

	temp_spouse_node = get_spouse_node_of( current_blossom );
	
	//another_temp_spouse_node = get_node_in_me_which_is_married_out( current_blossom );


	our_augment_queue.put_new_augment_item_into_queue( (temp_spouse_node).node_number, (get_node_in_me_which_is_married_out( current_blossom )).node_number, false );

	recursive_subroutine_augment_blossom( current_blossom, from_node, get_node_in_me_which_is_married_out( current_blossom ) );
	
	remove_connection_in_blossom( current_blossom, temp_spouse_node );
	
	add_non_spouse_to_blossom( current_blossom, temp_spouse_node );
	
	//remove_connection_in_blossom( next_blossom, another_temp_spouse_node );
	
	//add_non_spouse_to_blossom( next_blossom, another_temp_spouse_node );
	

	odd_number_of_blossoms_away_from_last_blossom = false;

	while( next_blossom != last_blossom_to_augment )
	{

		odd_number_of_blossoms_away_from_last_blossom = ! odd_number_of_blossoms_away_from_last_blossom;
		

		if( odd_number_of_blossoms_away_from_last_blossom )
		{
			
			current_blossom = next_blossom; //shrinking
	
			// get this from the last blossom's temp variables
			
			node_in_me_which_connects_me_away_from_tree_root = temp_spouse_node;
			
					
			// save this right away because we are going to screw up current blossom's marital status
			
			next_blossom = get_non_spouse_of( current_blossom );

			temp_spouse_node = get_spouse_node_of( current_blossom );
			temp_non_spouse_node = get_non_spouse_node_of( current_blossom );
			
			
			
			// special case here if we are in an outermost blossom

			if( we_are_augmenting_at_outermost_level )
			{
				
				// we are in an outermost blossom, so we can't rely on the function
				// 	get_node_in_me_which_is_tight_out to work because it depends
				// 	on the fact that the node i am tight with is also tight with me,
				// 	which is not the case in the outermost blossom tree, so use
				// 	the special field we have just to make up for this problem
			
				temp_node_in_me_which_is_tight_out = (current_blossom).node_in_me_connected_out_towards_tree_root_node;
				
		
			}
			else	// we are not in an outermost blossom
			{
			
				// we can rely on the cycle of spouse, non-spouse's, so
				// 	get_node_in_me_which_is_tight_out will work
				
				temp_node_in_me_which_is_tight_out = get_node_in_me_which_is_tight_out( current_blossom );
			
			}						


			our_augment_queue.put_new_augment_item_into_queue( (temp_non_spouse_node).node_number, (temp_node_in_me_which_is_tight_out).node_number, true );

			// when calling recursive_subroutine_augment_blossom, we must always remember to make sure that
			// 	the from_node is NOT the node in the blossom which is married out -- this is the node
			// 	which will have no spouse in the blossom, making the first step in the augmentation
			// 	of that blossom (which is to go towards the spouse) impossible


			recursive_subroutine_augment_blossom( current_blossom, temp_node_in_me_which_is_tight_out, node_in_me_which_connects_me_away_from_tree_root );


			remove_connection_in_blossom( current_blossom, temp_spouse_node );
			add_non_spouse_to_blossom( current_blossom, temp_spouse_node );
	
			remove_connection_in_blossom( current_blossom, temp_non_spouse_node );
			add_spouse_to_blossom( current_blossom, temp_non_spouse_node );
			
			// we change this pointer so that the tight edge out of current blossom
			//can be removed in reset_tree
			
			(current_blossom).node_in_me_connected_out_towards_tree_root_node = node_in_me_which_connects_me_away_from_tree_root;
				
		}
		else	// we are an even number of blossoms away from last_blossom_to_augment
		{
		
			// we aren't going to move current_blossom forward yet
			// 	because if we are augmenting at the outermost level,
			// 	we still need some information from
			
			// get this from the last blossom's temp variables
			
			node_in_me_which_connects_me_away_from_tree_root = temp_non_spouse_node;
						
						

			// special case here if we are in an outermost blossom

			if( we_are_augmenting_at_outermost_level )
			{
				
				// we are in an outermost blossom, so we can't rely on the cycle
				// 	of spouse, non-spouse to work -- this blossom (the even one which
				// 	is about to become the current_blossom, but not yet) will in
				// 	fact have no non-spouse, so we must use the special
				// 	field of the current_blossom used in trees for this purpose

				temp_non_spouse_node = temp_node_in_me_which_is_tight_out;
				
			}
			else	// we are not in an outermost blossom
			{
			
				// we can rely on the cycle of spouse, non-spouse's
				// 	but we must remember that we haven't moved
				// 	forward yet so we check next_blossom
				
				temp_non_spouse_node = get_non_spouse_node_of( next_blossom );
			
			}						

							
			// now move forward
			
			current_blossom = next_blossom; //expanding
			
			
			// save this right away because we are going to screw up current blossom's marital status
			
			next_blossom = get_spouse_of( current_blossom );
			
			temp_spouse_node = get_spouse_node_of( current_blossom );
			
			//another_temp_spouse_node = get_spouse_node_of( next_blossom );
							
			// when calling recursive_subroutine_augment_blossom, we must always remember to make sure that
			// 	the from_node is NOT the node in the blossom which is married out -- this is the node
			// 	which will have no spouse in the blossom, making the first step in the augmentation
			// 	of that blossom (which is to go towards the spouse) impossible

			if( null != next_blossom )
			{
				our_augment_queue.put_new_augment_item_into_queue( (temp_spouse_node).node_number, (get_node_in_me_which_is_married_out( current_blossom )).node_number, false );
			}

			recursive_subroutine_augment_blossom( current_blossom, node_in_me_which_connects_me_away_from_tree_root, get_node_in_me_which_is_married_out( current_blossom ) );


			remove_connection_in_blossom( current_blossom, temp_spouse_node );
			add_non_spouse_to_blossom( current_blossom, temp_spouse_node );
	
			remove_connection_in_blossom( current_blossom, temp_non_spouse_node );
			add_spouse_to_blossom( current_blossom, temp_non_spouse_node );
		
			//remove_connection_in_blossom( next_blossom, another_temp_spouse_node );
			//add_non_spouse_to_blossom( next_blossom, another_temp_spouse_node );

		}
		
	}
	
	
	// now finish up the last blossom in blossom_to_augment
	
	
	// get this from the last blossom's temp variables
	
	node_in_me_which_connects_me_away_from_tree_root = temp_non_spouse_node;
	

	// augment the last blossom in blossom_to_augment, passing it blossom_to_augment's to_node
					
	recursive_subroutine_augment_blossom(last_blossom_to_augment, node_in_me_which_connects_me_away_from_tree_root, to_node );


	// in what follows, we have to check for the special case when the actual tree_root is involved

	if( we_are_augmenting_at_outermost_level )
	{
	
		// last_blossom_to_augment is an outermost blossom
	
		// an outermost blossom won't have ever had any connections (usually base blossoms in blossoms
		// 	have two non-married connections we can check for) so to figure out what this last
		// 	blossom must marry, we use the temp_node_in_me_which_is_tight_out
		// 	in the second last blossom -- which current_blossom was left pointing to
		
		add_spouse_to_blossom( last_blossom_to_augment, temp_node_in_me_which_is_tight_out );

	}
	else	// the last_blossom_to_augment is not an outermost blossom
	{
	
		// since last_blossom_to_augment is not outermost, it is in a blossom, so we know that
		// 	it will have two connections, unfortunately both are non-spouse, so we have to check
		// 	to see which one of it's connections connects it to the second last
		// 	blossom -- which current_blossom was left pointing to

		// use the fact that get_non_spouse_of and get_non_spouse_node_of
		// 	return the FIRST non-spouse

		if( get_non_spouse_of( last_blossom_to_augment ) == current_blossom )
		{
			temp_non_spouse_node = get_non_spouse_node_of( last_blossom_to_augment );
		}
		else
		{
			temp_non_spouse_node = (last_blossom_to_augment).second_connection.node_in_other_to_which_i_am_connected;
		}
		
		remove_connection_in_blossom( last_blossom_to_augment, temp_non_spouse_node );
		add_spouse_to_blossom( last_blossom_to_augment, temp_non_spouse_node );
	
	}
	
}


private void shrink_and_grow_the_tree()
{

	blossom temp_outermost_blossom;
	
	
	for( temp_outermost_blossom = first_blossom; null != temp_outermost_blossom; temp_outermost_blossom = (temp_outermost_blossom).this_is_contained_with_me )
	{
	
		if( (temp_outermost_blossom).i_am_in_tree )
		{

			if( (temp_outermost_blossom).i_am_expanding )
			{
			
				(temp_outermost_blossom).width += distance_by_which_to_change;
			
			}
			else	// the blossom is shrinking
			{
			
				(temp_outermost_blossom).width -= distance_by_which_to_change;
			
			}
		
			if( temp_outermost_blossom.is_a_node() )
			{
				
				if( (temp_outermost_blossom).i_am_expanding )
				{	
					support.output_grow( (temp_outermost_blossom).node_number );
				}
				else	// the blossom is shrinking
				{
					support.output_shrink( (temp_outermost_blossom).node_number );
				}
			
			}
			else	// the blossom is not just a node
			{
							
				recursive_change_all_nodes_in_blossom( (temp_outermost_blossom).i_contain, distance_by_which_to_change, (temp_outermost_blossom).i_am_expanding );

			}
			
			
		}
	
		// else if this blossom isn't in the tree, leave it alone

	}

}


private boolean set_up_new_tree_root_node( short node_number )
{

	// check to see if we were told to find our own node
	
	if( 0 == node_number )
	{

		tree_root_node = find_first_tree();		

	}	
	else	// we were given a node_number to use, let's see if it's valid
	{
	
		tree_root_node = find_eligible_node_with_number(  node_number );

	}

	
	if( null == tree_root_node )
	{
	
		return( false );
		
	}
	else	// we found a new tree_root_node
	{

		(tree_root_node).i_am_in_tree = true;
		(tree_root_node).i_am_expanding = true;
		support.output_highlite_node( (tree_root_node).node_number , true );
		return( true );

	}

}


private void recursive_change_all_nodes_in_blossom( blossom the_blossom, float distance, boolean expand_it )
{

	if( null == the_blossom )
	
		return;
		
	recursive_change_all_nodes_in_blossom( (the_blossom).i_contain, distance, expand_it );
	recursive_change_all_nodes_in_blossom( (the_blossom).this_is_contained_with_me, distance, expand_it );
	
	if( the_blossom.is_a_node() )
	{


		if( expand_it )
		{

			support.output_grow( (the_blossom).node_number );

			(the_blossom).width += distance;
			
		}
		else	// the node is in a shrinking blossom
		{

			support.output_shrink( (the_blossom).node_number );

			(the_blossom).width -= distance;
		
		}

	}

}


private void add_blossom_to_tree( blossom the_node_of_the_blossom_to_add, blossom the_node_of_the_blossom_to_receive )
{

	blossom the_blossom_to_add, the_blossom_to_receive;
	blossom spouse_of_blossom_to_add;
	
	

	the_blossom_to_add = find_outermost_blossom_containing( the_node_of_the_blossom_to_add );

	if( null != ( spouse_of_blossom_to_add = get_spouse_of( the_blossom_to_add ) ) )
	{
	
		// add the_blossom_to_add's spouse to the tree as well
		
		(spouse_of_blossom_to_add).i_am_in_tree = true;
		(spouse_of_blossom_to_add).i_am_expanding = true;
		
		
	}
	
	add_connection_in_blossom( the_blossom_to_add, the_node_of_the_blossom_to_receive, false );
	
	(the_blossom_to_add).node_in_me_connected_out_towards_tree_root_node = the_node_of_the_blossom_to_add;

	(the_blossom_to_add).i_am_in_tree = true;
	(the_blossom_to_add).i_am_expanding = false;

}


private blossom find_containing_blossom_which_is_inside_given_blossom( blossom contained_blossom, blossom given_blossom )
{
	// safety check

	if( null == contained_blossom )
	{	
		return( null );
	}
	
	
		
	if( (contained_blossom).i_am_contained_in == given_blossom )
	{
	
		// we have found the containing blossom which is inside the given blossom
	
		return( contained_blossom );
	
	}
	else	// we are not at the outermost blossom yet
	{

		return( find_containing_blossom_which_is_inside_given_blossom( (contained_blossom).i_am_contained_in, given_blossom ) );
	
	}

}


private void add_connection_in_blossom( blossom me, blossom the_node_in_the_other_to_which_i_am_connected, boolean make_it_a_marriage )
{

	// safety check
	
//#if 0 	// old stuff from before implementation of maximal minimum weight pm
//
//	if( null == me || null == the_node_in_the_other_to_which_i_am_connected )
//
//#else

	if( null == me )

//#endif

		return;


	// find an unused connection -- there had better be one

	if( null == (me).first_connection.node_in_other_to_which_i_am_connected )
	{
	
		(me).first_connection.node_in_other_to_which_i_am_connected = the_node_in_the_other_to_which_i_am_connected;
		
		(me).first_connection.is_a_marriage = make_it_a_marriage;
		
	}
	else if( null == (me).second_connection.node_in_other_to_which_i_am_connected )
	{
	
		(me).second_connection.node_in_other_to_which_i_am_connected = the_node_in_the_other_to_which_i_am_connected;
		
		(me).second_connection.is_a_marriage = make_it_a_marriage;
		
	}

//#if DEBUGGING	
//
//	else	// both connections were already used -- this is bad and shouldn't have happened
//	{
//	
//		// error detection -- we have a blossom that should have had an unused connection but didn't
//
//		printf( "You just tried to connect something to a blossom which already has two connections\n");
//	
//	}
//
//#endif	

}


private void remove_connection_in_blossom( blossom me, blossom the_node_in_the_other_to_which_i_am_connected )
{

	// safety check
	
	if( null == me || null == the_node_in_the_other_to_which_i_am_connected )
	
		return;
		

	if( the_node_in_the_other_to_which_i_am_connected == (me).first_connection.node_in_other_to_which_i_am_connected )
	{
	
		(me).first_connection.node_in_other_to_which_i_am_connected = null;
		(me).first_connection.is_a_marriage = false;
		
	}
	else if( the_node_in_the_other_to_which_i_am_connected == (me).second_connection.node_in_other_to_which_i_am_connected )
	{
	
		(me).second_connection.node_in_other_to_which_i_am_connected = null;
		(me).second_connection.is_a_marriage = false;
		
	}

//#if DEBUGGING		
//
//	else	// the node wasn't connected that way in the first place -- this is bad and shouldn't have happened
//	{
//	
//		// error detection -- somewhere, we thought something was connectected when it wasn't
//
//		printf( "You just tried to disconnect two blossoms that weren't connected\n");
//	}
//
//#endif

}


private blossom get_spouse_of( blossom the_blossom )
{
	blossom temp;//Luis
	// assumes that blossoms are contained in the same blossom (or that both are outermost)

	if( null == the_blossom )
	
		return( null );
		
	if( (the_blossom).first_connection.is_a_marriage )
	{
		temp = find_containing_blossom_which_is_inside_given_blossom( (the_blossom).first_connection.node_in_other_to_which_i_am_connected, (the_blossom).i_am_contained_in );
		return( temp );
		//return(  find_containing_blossom_which_is_inside_given_blossom( (the_blossom).first_connection.node_in_other_to_which_i_am_connected, (the_blossom).i_am_contained_in ) );
		
	}
	else if( (the_blossom).second_connection.is_a_marriage )
	{
	
		temp = find_containing_blossom_which_is_inside_given_blossom( (the_blossom).second_connection.node_in_other_to_which_i_am_connected, (the_blossom).i_am_contained_in );
		return( temp );
		//return(  find_containing_blossom_which_is_inside_given_blossom( (the_blossom).second_connection.node_in_other_to_which_i_am_connected, (the_blossom).i_am_contained_in ) );
	
	}
//	else	// the blossom doesn't have a spouse
//	{
	
		return( null );
		
//	}
	
}


private blossom get_non_spouse_of( blossom the_blossom )
{

	// returns the FIRST blossom to which the_blossom is connected but not married


	if( null == the_blossom )
	
		return( null );
		
	if( ! (the_blossom).first_connection.is_a_marriage )
	{
	
		return(  find_containing_blossom_which_is_inside_given_blossom( (the_blossom).first_connection.node_in_other_to_which_i_am_connected, (the_blossom).i_am_contained_in ) );
		
	}
//	else if( ! (the_blossom).second_connection.is_a_marriage )
//	{
	
		return(  find_containing_blossom_which_is_inside_given_blossom( (the_blossom).second_connection.node_in_other_to_which_i_am_connected, (the_blossom).i_am_contained_in ) );
	
//	}

//#if DEBUGGING
//
//	else	// the blossom has two spouses -- bad!
//	{
//	
//		printf( "Some blossom has two spouses\n");
//	
//		return( null );
//		
//	}
//
//#endif
	
}


private blossom get_non_spouse_node_of( blossom the_blossom )
{

	// returns the FIRST node in a blossom to which the_blossom is connected but not married

	if( null == the_blossom )
	{
		return( null );
	}	
	if( ! (the_blossom).first_connection.is_a_marriage )
	{
	
		return(  (the_blossom).first_connection.node_in_other_to_which_i_am_connected );
		
	}
//	else if( ! (the_blossom).second_connection.is_a_marriage )
//	{
	
		return(  (the_blossom).second_connection.node_in_other_to_which_i_am_connected );
		
//	}

//#if DEBUGGING
//
//	else	// the blossom has two spouses -- bad!
//	{
//	
//		printf( "Some blossom has two spouses\n");
//	
//		return( null );
//		
//	}
//
//#endif
	
}


private void add_node()
{

	blossom temp_node, previous_node=null;
	short node_number_counter=1;

	
	// it matters what order nodes are stored, as the user may want to see node numbers
	// 	so move to the first null handle in the linked list of nodes belonging to the current graph 

	
	temp_node = first_blossom;
//	if( ! temp_node.is_a_node() || ! VIRTUAL_NODE_NUMBER == (temp_node).node_number )
//	{
//		node_number_counter++; // since the first node is a virtual node
//	}
	

	while( temp_node != null )
	{
		node_number_counter++;
		previous_node = temp_node;
		temp_node=(temp_node).this_is_contained_with_me;
	}	


	// safety checks -- try to allocate memory 

	//if( (temp_node = (blossom ) NewHandle( sizeof( blossom ) ) ) == null )
	//
	//	return;	// not enough memory for a new node 

	temp_node = new blossom();


	// take special action if the new node is the first node to be added to the graph

	if( first_blossom == null)

		first_blossom = temp_node;

	else	// the graph already has nodes, so make the node before the new node point to that new node 

		(previous_node).this_is_contained_with_me = temp_node;

	
	// initialize the new node 

	(temp_node).width														= support.input_initial_disc_size( node_number_counter );

	(temp_node).node_number												= node_number_counter;

	
	(temp_node).i_am_contained_in											= null;
	
	(temp_node).i_contain 												= null;

	(temp_node).this_is_contained_with_me									= null;

	
	(temp_node).first_connection.node_in_other_to_which_i_am_connected	= null;			
	(temp_node).first_connection.is_a_marriage 							= false;			

	(temp_node).second_connection.node_in_other_to_which_i_am_connected	= null;
	(temp_node).second_connection.is_a_marriage 							= false;

	
	(temp_node).node_in_me_connected_out_towards_tree_root_node 			= null;

	(temp_node).i_am_in_tree 												= false;
	
	(temp_node).i_am_expanding 											= false;

	support.output_set_outer_colour( temp_node.node_number, current_colour );
	
}


private blossom find_first_tree()
{


	blossom temp_node;
	
	
	for( temp_node = first_blossom; null != temp_node; temp_node = (temp_node).this_is_contained_with_me )
	{
	
		// check to see if the current blossom is actually a node
	
		if( (temp_node).node_number > 0 )
		{
	
			if( ! (temp_node).first_connection.is_a_marriage && ! (temp_node).second_connection.is_a_marriage )
			{
			
				return( temp_node );	
		
			}	

		}
	
	}
	
	
	// if we made it this far, then we didn't find an eligible node
	
	return( null );

}


private blossom find_eligible_node_with_number( short node_number )
{

	blossom temp_node;
	
	
	// we are looking for an ELIGIBLE node with the desired node_number
	// 	-- for it to be eligible, it must still be among the outermost blossoms
	// 	so we only look for it among the outermost blossoms
	
	for( temp_node = first_blossom; null != temp_node; temp_node = (temp_node).this_is_contained_with_me ) 
	{
	
		// check to see if the current blossom is actually the node we want
	
		if( (temp_node).node_number == node_number )
		{
	
			if( ! (temp_node).first_connection.is_a_marriage && ! (temp_node).second_connection.is_a_marriage )
			{
			
				return( temp_node );	
		
			}
			else	// it is the node we want, but it is no longer eligible
			{
			
			
				return( null );
			
			}

		}
	
	}


	// if we made it this far, then we didn't find an eligible node
	
	return( null );

}	


private void recursive_remove_blossoms( blossom the_blossom )
{

	if( null == the_blossom )

		return;
		
	recursive_remove_blossoms( (the_blossom).i_contain );
	recursive_remove_blossoms( (the_blossom).this_is_contained_with_me );
	
	//DisposeHandle( the_blossom );
	the_blossom = null;

}


private void make_blossom_not_be_contained_in_anything( blossom the_blossom )
{

	blossom temp_blossom, containing_blossom;

	// safety checks

	if( null == the_blossom )
	
		return;

		
	if( null == (the_blossom).i_am_contained_in )
	{
	
		// we are removing a blossom from the list of outermost blossoms
		
		if( the_blossom == first_blossom )
		{
		
			// repair the linked list, making it skip over the_blossom
		
			first_blossom = (the_blossom).this_is_contained_with_me;
			
		}
		else	// the blossom to be removed isn't the first blossom in the tree
		{
		
			if( null == first_blossom )
			
				return;
		
			for( temp_blossom = first_blossom; null != (temp_blossom).this_is_contained_with_me && the_blossom != (temp_blossom).this_is_contained_with_me; temp_blossom = (temp_blossom).this_is_contained_with_me )
			{
				// do nothing, just move forward through the list of blossoms
			}		
			
			// now check to see what we found
			
			if( null != temp_blossom )
			{
			
				// repair the linked list, making it skip over the_blossom
			
				(temp_blossom).this_is_contained_with_me = (the_blossom).this_is_contained_with_me;
			
			}
			
				
		}
	
	}
	else	// the blossom we are removing isn't an outermost blossom
	{
	
		containing_blossom = (the_blossom).i_am_contained_in;

		// safety check
		
		if( null == (containing_blossom).i_contain )
			
			return;


		if( (containing_blossom).i_contain == the_blossom )
		{
		
			// the_blossom is the first in the list of blossoms belonging to the
			// 	containing blossom -- repair the linked list, making it skip over the_blossom
		
			(containing_blossom).i_contain = (the_blossom).this_is_contained_with_me;
			
		} 
		else	// the blossom isn't the first in any list of blossoms
		{
		
			for( temp_blossom = (containing_blossom).i_contain; null != (temp_blossom).this_is_contained_with_me && the_blossom != (temp_blossom).this_is_contained_with_me; temp_blossom = (temp_blossom).this_is_contained_with_me )
			{
				// do nothing, just move forward through the list of blossoms
			}		
			
			// now check to see what we found
			
			if( null != temp_blossom )
			{
			
				// repair the linked list, making it skip over the_blossom
			
				(temp_blossom).this_is_contained_with_me = (the_blossom).this_is_contained_with_me;
			
			}
		}
	
	}

}


private blossom find_meeting_outermost_blossom( blossom outermost_blossom_in_tree_one, blossom outermost_blossom_in_tree_two )
{
	blossom temp_blossom, return_blossom=null;
	

	// we use the i_am_in_tree flag to our advantage
	// 	we will always be in the tree with these blossoms, but will will modify the i_am_in_tree
	// 	flags temporarily so that we can tell where we have visited 
	
	
	for( temp_blossom = outermost_blossom_in_tree_one; null != temp_blossom; temp_blossom = next_outermost_blossom_towards_tree_root_from( temp_blossom ) )
	{
	
		(temp_blossom).i_am_in_tree = false;
	
	}
	
	for( temp_blossom = outermost_blossom_in_tree_two; null != temp_blossom; temp_blossom = next_outermost_blossom_towards_tree_root_from( temp_blossom ) )
	{
	
		if( ! (temp_blossom).i_am_in_tree )
		{
			
			// we have reached a blossom already visited
		
			return_blossom = temp_blossom;
			
			// it's important that we break out here so that we won't change
			// 	return_value to all the rest of the blossoms down the line which
			// 	will also be already visited
			
			break;

		}	
	
	}


	// now restore the flags back to their original values

	for( temp_blossom = outermost_blossom_in_tree_one; null != temp_blossom; temp_blossom = next_outermost_blossom_towards_tree_root_from( temp_blossom ) )
	{
	
		(temp_blossom).i_am_in_tree = true;
	
	}
	

	return( return_blossom );

}


private blossom next_outermost_blossom_towards_tree_root_from( blossom the_blossom )
{
	blossom temp;//Luis
	// this function assumes that the_blossom is in the tree

	if( null == the_blossom )
	{
		return( null );
	}
	
	if( the_blossom == find_outermost_blossom_containing( tree_root_node ) )
	{
		return( null );
	}
	
	if( (the_blossom).i_am_expanding )	
	{

		temp = get_spouse_of( the_blossom );
		return( temp );
		//return( get_spouse_of( the_blossom ) );
	
	}
	else	// the_blossom is shrinking
	{
		temp = get_non_spouse_of( the_blossom );
		return( temp );
		//return( get_non_spouse_of( the_blossom ) );
	
	}

}


private void make_blossom_be_contained( blossom blossom_to_be_contained, blossom containing_blossom )
{

	// safety check 
	
	if( null == blossom_to_be_contained )
	{	
		return;	
	}

	// add the blossom to be contained into the containing blossom first -- order
	// 	of containment doesn't mean anything, and it's easier to do it this way

	// note: any blossom_to_be_contained's this_is_contained_with_me field is overwriten
	// 	and lost, but there should never be a context where it is meaningful as 
	// 	a node should always have just been removed from another blossom before it is made
	// 	to be contained in another one -- blossoms are always removed one at a time so
	// 	the this_is_contained_with_me_field doesn't matter


	if( null == containing_blossom )
	{
	
		// special case, we are being told to add the blossom to the list of outermost blossoms

// FUNNY -- possible bug

		if( first_blossom.is_a_node() && VIRTUAL_NODE_NUMBER == (first_blossom).node_number)
		{
			//we will add it as the second in list as the first is a virtual node
			
			(blossom_to_be_contained).this_is_contained_with_me = (first_blossom).this_is_contained_with_me;
			(first_blossom).this_is_contained_with_me = blossom_to_be_contained;
		}
		else
		{
			//as as first in list
			
			(blossom_to_be_contained).this_is_contained_with_me = first_blossom;
			first_blossom = blossom_to_be_contained;
		}
		
	}
	else	// we are just adding a blossom to an ordinary blossom
	{

		(blossom_to_be_contained).this_is_contained_with_me = (containing_blossom).i_contain;
		(containing_blossom).i_contain = blossom_to_be_contained;
	
	}

	(blossom_to_be_contained).i_am_contained_in = containing_blossom;

}


private void recursive_subroutine_add_discs_to_all_nodes_in_blossom( blossom the_blossom )
{

	if( the_blossom == null )
	{
		return;
	}

	recursive_subroutine_add_discs_to_all_nodes_in_blossom( (the_blossom).this_is_contained_with_me );
	
	
	if( the_blossom.is_a_node() )
	{
		
		support.output_add_disc( (the_blossom).node_number );
		
		support.output_set_outer_colour( (the_blossom).node_number, current_colour );

	}
	else	// the_blossom is not a node
	{
	
		recursive_subroutine_add_discs_to_all_nodes_in_blossom( (the_blossom).i_contain );
	
	}

}


private blossom get_spouse_node_of( blossom the_blossom )
{

	if( null == the_blossom )
	
		return( null );
		
	
	if( (the_blossom).first_connection.is_a_marriage )
	{
	
		return(  (the_blossom).first_connection.node_in_other_to_which_i_am_connected );
		
	}
	else if( (the_blossom).second_connection.is_a_marriage )
	{
	
		return(  (the_blossom).second_connection.node_in_other_to_which_i_am_connected );

	}
	else	// the blossom doesn't have a spouse
	{
	
		return( null );
		
	}
	
}


private void  reset_tree()
{

	blossom temp_blossom, another_temp_blossom, another_temp_node;

	// we want remove any traces of a tree from any of the outermost blossoms
	
	for( temp_blossom = first_blossom; null != temp_blossom; temp_blossom = (temp_blossom).this_is_contained_with_me )
	{
	
		
		if( (temp_blossom).i_am_in_tree )
		{
		
			(temp_blossom).i_am_in_tree = false;
			
			
			// now remove any non-spouse tightness between blossoms which are at the outermost level
			// 	-- only blossoms (and nodes) within other blossoms retain their tight lines when
			// 	the tree is reset.  Only shringing blossoms need be looked at.
			
			if( ! (temp_blossom).i_am_expanding && null != ( another_temp_node = get_non_spouse_node_of( temp_blossom ) ) )
			{
			
				another_temp_blossom = get_non_spouse_of( temp_blossom );
				
				remove_connection_in_blossom( temp_blossom, another_temp_node );
				remove_connection_in_blossom( another_temp_blossom, get_non_spouse_node_of( another_temp_blossom ) );
		
				// we are about to access some handles which ought to be non-null
				// 	we won't check it because if it is null, then something is wrong with
				// 	the algorithm somewhere else and this way we'll find out about it pretty fast
				
				support.output_remove_tight_line( ((temp_blossom).node_in_me_connected_out_towards_tree_root_node).node_number, (another_temp_node).node_number );
				//support.output_remove_tight_line( (get_non_spouse_node_of(find_outermost_blossom_containing(another_temp_node))).node_number, (another_temp_node).node_number );
			
			}
			
			// other tree related variables like i_am_expanding and node_in_me_connected_out_towards_tree_root_node
			// 	shouldn't trouble us because they are ignored unless a blossom is put in a tree
			// 	again and when that happens those variables  will be set to new proper values anyway
		
		}
		
	}

	//support.output_highlite_node( (tree_root_node).node_number , false );
	
	tree_root_node = null;
	
//#if 0
//	// experimental,  we reset EVERY blossom, just to make sure of cleanliness
//	//decend to first node in blossom
//	
//	temp_blossom = first_blossom;
//	
//	while( ! temp_blossom.is_a_node() )  
//		{
//			(temp_blossom).i_am_expanding = false;
//			(temp_blossom).i_am_in_tree = false;
//			
//			temp_blossom = (temp_blossom).i_contain;
//		}			
//		
//	while( true )
//	{
//		while( temp_blossom != null  && null == (temp_blossom).this_is_contained_with_me )
//		{
//			temp_blossom = (temp_blossom).i_am_contained_in;
//		}
//		
//		if( temp_blossom == null )
//		{
//			break;  //  search complete 
//		}
//		
//		temp_blossom = (temp_blossom).this_is_contained_with_me;
//		
//		while( ! temp_blossom.is_a_node() )
//		{
//			(temp_blossom).i_am_expanding = false;
//			(temp_blossom).i_am_in_tree = false;
//			
//			temp_blossom = (temp_blossom).i_contain;
//		}			
//		
//	} // end while  we have fully decended 
//
//#endif
	
}


private boolean is_single( blossom the_node )
{

	if( null == the_node )
	
		return( false );
		
		
	if( null == (the_node).i_am_contained_in )
	{
	
		return( ! ((the_node)).first_connection.is_a_marriage && ! ((the_node)).second_connection.is_a_marriage );
	
	}
	else	// the node is contained in some blossom, so it must be married
	{
	
		// note: the tree root node can be in a blossom while at the same time
		// 	being single, but we won't ever be asking whether the tree_root_node
		// 	is single or not
	
		return( false );
	
	}

}


private blossom get_node_in_me_which_is_tight_out( blossom the_blossom )
{

	// this function will get the first node in the_blossom which
	// 	is connected out to a non_spouse

	// make sure you don't try to use it when dealing with the outermost
	// 	blossoms -- they don't always store their tight out (only 
	// 	shrinking ones do)


	blossom non_spouse_blossom;
	
	
	non_spouse_blossom = get_non_spouse_of( the_blossom );
	
	
	// now we must be careful because non_spouse_blossom might itself 
	// 	have more than one non_spouse blossom -- we check to see which
	// 	one will take us back to the_blossom
	
	// this relies on the fact that get_non_spouse_of and
	// 	get_non_spouse_node_of both return the FIRST non-spouse
	
	if( get_non_spouse_of( non_spouse_blossom ) == the_blossom )
	{
	
		return( get_non_spouse_node_of( non_spouse_blossom ) );
		
	}
	else	// the non_spouse_blossom itself had more than one non-spouses, and the first one wasn't the correct one
	{
	
		return( (non_spouse_blossom).second_connection.node_in_other_to_which_i_am_connected );
	
	}

}


private void recursive_subroutine_remove_discs_from_all_nodes_in_blossom( blossom the_blossom )
{

	if( null == the_blossom )

		return;
		
	if( null == (the_blossom).i_contain )
	{
	
		// the_blossom is an actual node
		
		support.output_remove_disc( (the_blossom).node_number );
		
	}
	else	// the blossom contains stuff
	{
	
		recursive_subroutine_remove_discs_from_all_nodes_in_blossom( (the_blossom).i_contain );
		
	}
	
	recursive_subroutine_remove_discs_from_all_nodes_in_blossom( (the_blossom).this_is_contained_with_me );	

}


private void add_discs_to_all_nodes_in_blossom( blossom the_blossom )
{

	// we need a special case function to check to see if the_blossom itself is a node
	//  -- we want to use recursion but can't right away because we don't want
	// 	to go off and add_discs to everything down the right side of the_blossom
	// 	i.e. we have to ignore stuff in the this_is_contained_with_me field the first time
	
	if( null == the_blossom )
	
		return;
		
	if( null == (the_blossom).i_contain )
	{
		
		// the blossom is an actual node
	
		support.output_add_disc( (the_blossom).node_number );
	
		support.output_set_outer_colour( (the_blossom).node_number, current_colour );
	
	
	}
	else	// the blossom contains stuff
	{
	
		recursive_subroutine_add_discs_to_all_nodes_in_blossom( (the_blossom).i_contain );
	
	}
	
}



private void remove_discs_from_all_nodes_in_blossom( blossom the_blossom )
{

	// we need a special case function to check to see if the_blossom itself is a node
	//  -- we want to use recursion but can't right away because we don't want
	// 	to go off and add_discs to everything down the right side of the_blossom
	// 	i.e. we have to ignore stuff in the this_is_contained_with_me field the first time
	
	if( null == the_blossom )
	
		return;
		
	if( null == (the_blossom).i_contain )
	{
		
		// the blossom is an actual node
	
		support.output_remove_disc( (the_blossom).node_number );
	
	}
	else	// the blossom contains stuff
	{
	
		recursive_subroutine_remove_discs_from_all_nodes_in_blossom( (the_blossom).i_contain );
	
	}
	
}

private blossom find_node_not_in_tree_with_biggest_radius()
{

	// places this node in second_affected_blossom 
	// and also in first_affected_blossom
	// as this indicates that this node shall marry infinity
	
	blossom temp_outermost_blossom;

	blossom widest_node_so_far;
	
	widest_node_so_far = null;

	for( temp_outermost_blossom = first_blossom; null != temp_outermost_blossom; temp_outermost_blossom = (temp_outermost_blossom).this_is_contained_with_me )
	{
	
	if( !(temp_outermost_blossom).i_am_in_tree )
		widest_node_so_far = recursive_subroutine_find_node_in_blossom_with_biggest_radius( temp_outermost_blossom, widest_node_so_far );
		
	}

	return( widest_node_so_far );
	
}


private blossom find_node_with_number( short node_number )
{
	blossom temp_node;
	
	//find first node in blossom
	
	temp_node = first_blossom;
	
	while( ! temp_node.is_a_node() )  
		{
			temp_node = (temp_node).i_contain;
		}			
		
	while( true )
	{
		if(	 node_number == (temp_node).node_number )
			return( temp_node );
		
		//we now find the next node
		
		while( temp_node != null  && null == (temp_node).this_is_contained_with_me )
		{
			temp_node = (temp_node).i_am_contained_in;
		}
		
		if( temp_node == null )
		{
			return( null );  //  search complete -- no such node exists!
		}
		
		temp_node = (temp_node).this_is_contained_with_me;
		
		while( ! temp_node.is_a_node() )
		{
			temp_node = (temp_node).i_contain;
		}			
		
	} // end while  we have found a new temp_node to test

}



private blossom find_expanding_node_in_tree_with_biggest_radius()
{

	// places this node in second_affected_blossom 
	// and also in first_affected_blossom
	// as this indicates that this node shall marry infinity
	
	blossom temp_outermost_blossom;

	blossom widest_node_so_far;
	
	widest_node_so_far = null;

	for( temp_outermost_blossom = first_blossom; null != temp_outermost_blossom; temp_outermost_blossom = (temp_outermost_blossom).this_is_contained_with_me )
	{
	
	if( (temp_outermost_blossom).i_am_in_tree && (temp_outermost_blossom).i_am_expanding )
		widest_node_so_far = recursive_subroutine_find_node_in_blossom_with_biggest_radius( temp_outermost_blossom, widest_node_so_far );
		
	}

	return( widest_node_so_far );
	
}


private blossom recursive_subroutine_find_node_in_blossom_with_biggest_radius( blossom the_blossom, blossom widest_node_so_far )
{

	if( null == the_blossom )
	
	    // no more blossoms contained with me
	
		return(widest_node_so_far);
	
	if( null == (the_blossom).i_contain )
	{
	
		// it's just a node, compare and recurse to next blossom contained with
		if( null == widest_node_so_far )
		{
			widest_node_so_far = the_blossom;
		
		} else if(  (the_blossom).width > (widest_node_so_far).width )
			{
			
				// we found a node with a bigger total disc radius, 
						
				widest_node_so_far = the_blossom;
			}
	

	}
	else	// its a blossom, recurse inward
	{
	
		widest_node_so_far = recursive_subroutine_find_node_in_blossom_with_biggest_radius( (the_blossom).i_contain, widest_node_so_far );
	}
	
	return( recursive_subroutine_find_node_in_blossom_with_biggest_radius((the_blossom).this_is_contained_with_me, widest_node_so_far ) );

}


private blossom find_outermost_blossom_containing( blossom the_blossom )
{
	return( find_containing_blossom_which_is_inside_given_blossom( the_blossom, null ) );
}


private blossom get_node_in_me_which_is_married_out( blossom the_blossom )
{
	return( get_spouse_node_of( get_spouse_of( the_blossom ) ) );
}


private void add_spouse_to_blossom( blossom the_blossom, blossom the_node_to_which_i_am_espoused )
{
	add_connection_in_blossom( the_blossom, the_node_to_which_i_am_espoused, true );
}


private void add_non_spouse_to_blossom( blossom the_blossom, blossom the_node_to_which_i_am_not_espoused )
{
	add_connection_in_blossom( the_blossom, the_node_to_which_i_am_not_espoused, false );
}


//#if DEBUGGING
//
//#if 0 	// only when dumping after DISSOLVE blossom
//
//	// although it shouldn't matter to the program, because it should be ignoring this
//	// 	value at this point, print_memory_dump will try to print out the blossom in
//	// 	first_affected_blossom which will be garbage after dissolve blossom has nuked it
//
//	first_affected_blossom = null;
//
//#endif
//
//	if( debugging_dump )
//		print_memory_dump();
//
//#endif
//
//
//
//#if DEBUGGING	// useful routines when debugging
//
//private void print_blossom( blossom the_blossom )
//{
//
//	if( null == the_blossom )
//	{
//		
//		printf( "you just tried to print out a null blossom\n");
//		
//		return;
//
//	}
//
//
//#if 0	// this is an extensive printout
//
//	printf( "blossom # %d     address: %x     w: %e\n", (the_blossom).node_number, the_blossom, (the_blossom).width );
//
//	if( (the_blossom).i_am_in_tree )
//		printf( "   i am in tree" );
//	else
//		printf( "   i am NOT in tree" );
//	
//	if( (the_blossom).i_am_expanding )
//		printf( "   i am expanding is true\n" );
//	else
//		printf( "   i am expanding is false\n" );
//
//	
//	if( null != (the_blossom).i_am_contained_in )
//		printf( "   i am contained in: %4d", ((the_blossom).i_am_contained_in).node_number );
//	else
//		printf( "   i am contained in: null" );
//		
//	if( null != (the_blossom).i_contain )
//		printf( "   i contain: %4d", ((the_blossom).i_contain).node_number );
//	else
//		printf( "   i contain: null" );
//		
//	if( null != (the_blossom).this_is_contained_with_me )
//		printf( "   this_is_contained_with_me: %4d\n", ((the_blossom).this_is_contained_with_me).node_number );
//	else
//		printf( "   this_is_contained_with_me: null\n" );
//		
//	
//	
//	if( null != (the_blossom).first_connection.node_in_other_to_which_i_am_connected )
//		printf( "   first connection: %4d", ((the_blossom).first_connection.node_in_other_to_which_i_am_connected).node_number );
//	else
//		printf( "   first connection: null" );
//	
//	printf( "     married: %d\n", (the_blossom).first_connection.is_a_marriage );
//
//
//	if( null != (the_blossom).second_connection.node_in_other_to_which_i_am_connected )
//		printf( "   second connection: %4d", ((the_blossom).second_connection.node_in_other_to_which_i_am_connected).node_number );
//	else
//		printf( "   second connection: null" );
//	
//
//	printf( "     married: %d\n", (the_blossom).second_connection.is_a_marriage );
//
//	
//	if( null != (the_blossom).node_in_me_connected_out_towards_tree_root_node )
//		printf( "   node_in_me_connected_out_towards_tree_root_node: %4d\n", ((the_blossom).node_in_me_connected_out_towards_tree_root_node).node_number );
//	else
//		printf( "   node_in_me_connected_out_towards_tree_root_node: null\n" );
//
//#else	// the bare essentials
//
//
//	printf("  %10d    ", (the_blossom).node_number );
//	
//	if( null != (the_blossom).first_connection.node_in_other_to_which_i_am_connected )
//	{
//		if( (the_blossom).first_connection.is_a_marriage )
//		
//			printf( "married:   %4d", ((the_blossom).first_connection.node_in_other_to_which_i_am_connected).node_number );
//
//		else
//		
//			printf( "connected: %4d", ((the_blossom).first_connection.node_in_other_to_which_i_am_connected).node_number );
//	
//	}
//	else
//		printf    ( "null          " );
//	
//
//	if( null != (the_blossom).second_connection.node_in_other_to_which_i_am_connected )
//	{
//		if( (the_blossom).second_connection.is_a_marriage )
//		
//			printf( "      married:   %4d", ((the_blossom).second_connection.node_in_other_to_which_i_am_connected).node_number );
//
//		else
//		
//			printf( "      connected: %4d", ((the_blossom).second_connection.node_in_other_to_which_i_am_connected).node_number );
//	
//	}
//	else
//		printf    ( "      null          " );
//	
//
//	if( null != (the_blossom).node_in_me_connected_out_towards_tree_root_node )
//	{
//		printf( "      in me down the tree:   %4d\n", ((the_blossom).node_in_me_connected_out_towards_tree_root_node).node_number );
//	}
//	else
//		printf( "      in me down the tree:   null\n" );
//
//#endif
//
//
//
//}
//
//
//private void recursive_print_all_blossoms( blossom current_blossom )
//{
//
//	if( null == current_blossom )
//	
//		return;
//	
//	
//	print_blossom( current_blossom );
//	
//	recursive_print_all_blossoms( (current_blossom).this_is_contained_with_me );
//	recursive_print_all_blossoms( (current_blossom).i_contain);
//
//}
//
//
//private void print_memory_dump()
//{
//
//	blossom temp_blossom;
//
//	freopen( "dump_file", "a", stdout );
//
//	
//	printf( "action to be taken:  %d\n", action_to_be_taken );
//	
//	printf( "first affected blossom:\n");
//	print_blossom( first_affected_blossom );
//	
//	printf( "second affected blossom:\n");
//	print_blossom( second_affected_blossom );
//
//
//	printf( "minimum distance so far:  %e\n", distance_by_which_to_change );
//	
//	recursive_print_all_blossoms( first_blossom );
//
//	fclose( stdout );
//
//}
//
//#endif	// DEBUGGING

} // end non_bipartite_perfect_matching
