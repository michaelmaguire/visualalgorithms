// Bipartite_Perfect_Matching.java - Michael Maguire - 28 Apr 96

package ca.michaelmaguire.client.visualalgorithms.bipartite_perfect_matching;


import java.awt.*; //for debugging

import ca.michaelmaguire.client.visualalgorithms.AlgorithmSupport;
import ca.michaelmaguire.client.visualalgorithms.AlgorithmsProvide;

// rougly, we use a multi-linked binary tree:
//	each node Y has a pointer "left" called tight_away_from_tree_root,
// 	a pointer "right" called sibling -- these two are used for traversing the binary tree
//	-- and a pointer sort of back "up" the tree called tight_towards_tree_root
// 	to the node with which node Y is tight -- used in augmenting only
//  -- sibling is used only in nodes of colour opposite to tree_root and indicates
//	other nodes which have been bumped by the node "up" indicated in tight_towards_tree_root
//
//											
// the picture:				tree_root:                X
//                                                                _
//                                                               /| ^  ^
//                                                              /  /|\/|\
//                                                             |_   |  |
//                                                                  |  |
//                                                    O             |  |
//		    tight_towards_tree_root _   \	|   |
//									/|   \	|   |
//								   /      _||	| tight_towards_tree_root	
//	  	tight_away_from_tree_root |_        |	|
//										    O	|
//								X		 _   \	 |
//										 /|   \  |
//										/      _||
//									  |_         | 
//											     O
//									 X		   _  \ sibling
//								  _			   /|  \
//								  /|/|\		  /     _|	
//								 /	 |		|_
//								|_	 |			      O
//									 |	   X
//							  O      |
//								\    | 
//						 sibling \   | tight_towards_tree_root
//								 _|  |
//									 O
//
//									etc.
//
//

public class Algorithm implements AlgorithmsProvide
{
	private static class Node
	{

		short	node_number;

		float	radius;

		Node	spouse;

		Node	tight_towards_tree_root, tight_away_from_tree_root, sibling;

		Node	next_node;

	}

	private AlgorithmSupport	support;		// what we can
	// rely on for
	// input and
	// output

	private Node				first_node;

	private Node				tree_root;

	private Node				the_bumper;					// the node in
	// the tree
	// which grows
	// to bump some
	// node out of
	// the tree

	private Node				the_bumpee;					// the node out
	// of the tree
	// which gets
	// bumped

	private float				distance_by_which_to_change;

	private boolean				augmenting_needs_doing;

	private Node				current_augment_node;

	public Algorithm(AlgorithmSupport the_support, short number_of_nodes)
	{

		support = the_support;

		short node_number_counter;

		first_node = null;

		tree_root = null;

		the_bumper = null;

		the_bumpee = null;

		current_augment_node = null;

		augmenting_needs_doing = false;

		for (node_number_counter = 1; node_number_counter <= number_of_nodes; node_number_counter++)
		{
			add_node();
		}

	}

	public void close()
	{

	}

	public boolean step(short node_number)
	{

		// safety check to see if we are totally done

		if (find_first_eligible_node() == null && !augmenting_needs_doing)
		{

			return (false);

		}

		if (augmenting_needs_doing)
		{

			// do one more step in the augmentation

			augment();

			// check to see if we are back at the tree root now, in which case
			// clean

			// up the tree and tell the world we are done augmenting, and

			// check to see if we are possibly totally done

			if (current_augment_node == tree_root)
			{

				augmenting_needs_doing = false;

				reset_tree(tree_root); // we want no more tree, i.e.: tree_root
				// = null, and

				// all tight_towards_tree_root's, tight_away_from_tree_root's
				// and sibling's = null

				support.output_augmenting_now(false);

				// check to see if this augmentation was the last one
				// i.e. are we totally done?

				if (find_first_eligible_node() == null)
				{

					// tell the world we are done

					support.output_waiting_for_next_node(false);

					support.output_done();

				}
				else
				{
					// there are more eligible nodes, so tell the world we're
					// waiting

					support.output_waiting_for_next_node(true);

				}

			}
			else
			{
				// we still have more augmenting to do

				support.output_augmenting_now(true);

				support.output_waiting_for_next_node(false);

			}

		}
		else
		{
			// no augmenting needs doing at the moment

			// safety checks

			if (node_number < 0)
			{
				return (false); // invalid node_number
			}

			// check first to see if we need a new node to start from

			// i.e.: check to see if there isn't already a tree root

			if (tree_root == null)
			{

				// check to see if we were told to find our own node

				if (node_number == 0)
				{

					tree_root = find_first_eligible_node();

				}
				else
				{
					// we were given a node, let's see if it's valid

					tree_root = find_node_with_number(node_number);

					// do we have a node at all?

					if (tree_root != null)
					{

						// is it married?

						if (tree_root.spouse != null)
						{

							// reset tree_root, so we look for a new one next
							// time

							tree_root = null;

							return (false); // the node has a spouse, so it is
							// invalid

						}

					}
					else
					{
						// tree_root was null, so the
						// node_number was invalid
						return (false);
					}
				}

				support.output_highlite_node(tree_root.node_number, true);

			}

			// find the first node in the tree with the same colour as tree_root
			// which

			// will bump a node of opposite colour not in the tree

			distance_by_which_to_change = 10000000.0f; // something big so that
			// checking for minimum
			// works

			the_bumper = null;

			the_bumpee = null;

			find_the_next_bump(tree_root);

			// we set the radiuses of the nodes in the trees and set up the
			// animation shrinks and grows

			// this should be done before we add the bumpee (and possibly it's
			// spouse) to the tree

			shrink_and_grow_the_tree(tree_root);

			// we set the distance by which to change for the animation now --
			// it doesn't matter

			// when we do this as long as it's done before this function we're
			// in now returns

			support.output_change_by(distance_by_which_to_change);

			// safety check

			if (the_bumper != null && the_bumpee != null)
			{

				// set up the animation to show a tight line to the node with
				// which we've bumped

				support.output_add_tight_line(the_bumper.node_number, the_bumpee.node_number);

				// add the bumpee to the tree

				the_bumpee.tight_towards_tree_root = the_bumper;

				if (the_bumper.tight_away_from_tree_root == null)
				{

					// the Bumper is not already tight with any other node, so
					// just do this:

					the_bumper.tight_away_from_tree_root = the_bumpee;

				}
				else
				{

					// the_bumper is already tight with some nodes, so call a
					// routine to make

					// it tight with the_bumpee as well

					make_node_tight_with_a_further_node(the_bumper.tight_away_from_tree_root, the_bumpee);

				}

				// check to see whether the bumpee is married or not and act
				// accordingly

				if (the_bumpee.spouse != null)
				{

					// add the bumpee's spouse to the tree as well

					the_bumpee.tight_away_from_tree_root = the_bumpee.spouse;

					the_bumpee.spouse.tight_towards_tree_root = the_bumpee;

					support.output_waiting_for_next_node(false);

					support.output_augmenting_now(false);

				}
				else
				{

					// we have a breakthrough, so set the ball rolling to
					// augment the marriages

					// in the path from the bumpee all the way back to tree_root

					current_augment_node = the_bumpee;

					augmenting_needs_doing = true;

					support.output_waiting_for_next_node(false);

					support.output_augmenting_now(true);

				}

			}

		}

		// if we made it this far, then either the node number we were passed
		// was valid or

		// we weren't looking for a node number anyway, so tell the world that
		// the node number

		// we were passed was o.k.

		return (true);

	}

	/**
	 * 
	 * returns null if no node of node_number was found
	 * 
	 * @param node_number
	 * @return
	 */
	private Node find_node_with_number(short node_number)
	{

		Node temp_node;

		short node_number_counter;

		// safety check

		if (node_number <= 0)
		{
			return (null); // we were passed an invalid node_number
		}

		// now look through the nodes until temp_node == null or counter >=
		// node_number

		temp_node = first_node;

		node_number_counter = 1;

		while (temp_node != null && node_number_counter < node_number)
		{

			temp_node = temp_node.next_node;

			node_number_counter++;

		}

		// now temp_node will either handle to the correct node, or to null

		// if a node of node_number isn't in the list

		return (temp_node);

	}

	private Node find_first_eligible_node()
	{

		// returns null if no unmarried node was found

		Node temp_node, eligible_node = null;

		// now look through the nodes until we find an unmarried node

		for (temp_node = first_node; temp_node != null && eligible_node == null; temp_node = temp_node.next_node)
		{

			if (temp_node.spouse == null)
			{
				eligible_node = temp_node;
			}
		}

		/*
		 * 
		 * // we don't want to check for this anymore, just let things go to
		 * infinity
		 * 
		 * // so that a maximal minimum weight implementation can be done later
		 * 
		 * 
		 * 
		 * // now look through the nodes, making sure there is a node of
		 * opposite
		 * 
		 * // colour for the first node to marry
		 * 
		 * 
		 * 
		 * for( temp_node = first_node; temp_node; temp_node=temp_node.next_node
		 * )
		 * 
		 * {
		 * 
		 * if( temp_node.spouse == null )
		 * 
		 * {
		 * 
		 * // now check to see if it's of opposite colour
		 * 
		 * 
		 * 
		 * if( ! same_colour( temp_node, eligible_node ) )
		 * 
		 * 
		 * 
		 * return( eligible_node );
		 * 
		 * 
		 * 
		 * }
		 * 
		 * 
		 * 
		 * }
		 * 
		 * 
		 * 
		 * // if we got to this point, there isn't an unmarried node in the list
		 * 
		 * // or there is an eligible node, but there is no one whom it could
		 * marry
		 * 
		 * 
		 * 
		 * return( null );
		 */

		return (eligible_node);

	}

	private boolean same_colour(Node node1, Node node2)
	{

		// safety check

		if (node1 == null || node2 == null)

			return (false); // one or more invalid nodes

		// now check their colours

		if (support.input_is_node_blue(node1.node_number) == support.input_is_node_blue(node2.node_number))
		{
			return (true);
		}
		else
		{
			return (false);
		}

	}

	private void shrink_and_grow_the_tree(Node the_node)
	{

		// safety check

		if (the_node == null)
		{
			return; // we are at the end of the tree, so do nothing
		}

		// check the_node's colour and act appropriately

		if (same_colour(the_node, tree_root))
		{

			the_node.radius += distance_by_which_to_change;

			// set up for animation

			support.output_grow(the_node.node_number);

		}
		else
		{

			the_node.radius -= distance_by_which_to_change;

			// set up for animation

			support.output_shrink(the_node.node_number);

		}

		// recursively shrink or grow the nodes in the rest of the tree

		shrink_and_grow_the_tree(the_node.tight_away_from_tree_root);

		shrink_and_grow_the_tree(the_node.sibling);

	}

	// adds the_node_to_be_put to the last ("rightmost") sibling node linked to
	// "where"

	// -- used to add an item to a tree which has just bumped some node X in the
	// tree,

	// where X is already tight with some nodes -- i.e.
	// X.tight_away_from_tree_root

	// is non-null
	private void make_node_tight_with_a_further_node(Node where, Node the_node_to_be_put)
	{

		// safety check

		if (where == null || the_node_to_be_put == null)
		{
			return; // we were passed a null pointer, so do nothing
		}

		// check to see if this is the siblingmost node

		if (where.sibling == null)
		{

			// this node is the siblingmost node, so put the_node_to_be_put to
			// the sibling

			where.sibling = the_node_to_be_put;

		}
		else
		{

			// we are not at the siblingmost node, so continue looking for it
			// recursively

			make_node_tight_with_a_further_node(where.sibling, the_node_to_be_put);

		}

	}

	private void find_the_next_bump(Node the_node)
	{

		Node temp_node;

		float temp_distance;

		// safety check

		if (the_node == null)
		{
			return;
		}

		// we check only nodes the same colour as the tree root

		if (same_colour(the_node, tree_root))
		{

			for (temp_node = first_node; temp_node != null; temp_node = temp_node.next_node)

			{

				// we check the_node only against nodes of opposite colour which
				// aren't in the tree

				if (!same_colour(temp_node, the_node) && temp_node.tight_towards_tree_root == null)

				{

					temp_distance = support.input_distance(temp_node.node_number, the_node.node_number)

					- (temp_node.radius + the_node.radius);

					if (temp_distance < distance_by_which_to_change)

					{

						distance_by_which_to_change = temp_distance;

						the_bumper = the_node;

						the_bumpee = temp_node;

					}

				}

			}

		}

		// now recursively go down through the tree

		find_the_next_bump(the_node.tight_away_from_tree_root);

		find_the_next_bump(the_node.sibling);

	}

	private void augment()
	{

		// safety check

		if (current_augment_node == null)
		{
			return;
		}

		if (current_augment_node.tight_towards_tree_root == null)
		{
			return;
		}

		if (same_colour(current_augment_node, tree_root))
		{

			// set the animation up to make the marriage line disappear and the
			// tight line appear

			support.output_divorce(current_augment_node.node_number,
					current_augment_node.tight_towards_tree_root.node_number);

			support.output_add_tight_line(current_augment_node.node_number,
					current_augment_node.tight_towards_tree_root.node_number);

		}
		else
		{

			// change our data to reflect the marriage

			current_augment_node.spouse = current_augment_node.tight_towards_tree_root;

			current_augment_node.tight_towards_tree_root.spouse = current_augment_node;

			// set the animation up to make the marriage line appear and the
			// tight line disappear

			support.output_marry(current_augment_node.node_number, current_augment_node.spouse.node_number);

			support.output_remove_tight_line(current_augment_node.node_number, current_augment_node.spouse.node_number);

		}

		// now set things to augment next time starting one node further towards
		// the tree root

		current_augment_node = current_augment_node.tight_towards_tree_root;

	}

	private void reset_tree(Node the_node)
	{

		// safety check

		if (the_node == null)
		{
			return; // we were passed a null pointer, so do nothing
		}

		// go further down the tree -- reset_tree will just come back out if the
		// pointer

		// is null, so no need to check

		if (the_node.tight_towards_tree_root != null)
		{
			support.output_remove_tight_line(the_node.node_number, the_node.tight_towards_tree_root.node_number);
		}

		reset_tree(the_node.tight_away_from_tree_root);

		reset_tree(the_node.sibling);

		// now we should have reset all the nodes below this node, so reset this
		// node's pointers to null

		the_node.tight_away_from_tree_root = null;

		the_node.tight_towards_tree_root = null;

		the_node.sibling = null;

		// a special case

		if (the_node == tree_root)
		{

			support.output_highlite_node(tree_root.node_number, false);

			tree_root = null;

		}

	}

	// node numbering starts at 1

	private void add_node()
	{

		Node temp_node, previous_node;

		short node_number_counter;

		// it matters what order nodes are stored, as the user may want to see
		// node numbers

		// so move to the first null handle in the linked list of nodes
		// belonging to the current graph

		node_number_counter = 1;

		temp_node = first_node;

		previous_node = first_node; // just to make Java happy

		while (temp_node != null)
		{

			node_number_counter++;

			previous_node = temp_node;

			temp_node = temp_node.next_node;

		}

		temp_node = new Node();

		// take special action if the new node is the first node to be added to
		// the graph

		if (first_node == null)
		{

			first_node = temp_node;
		}
		else
		{
			// the graph already has nodes, so make the node before the new node
			// point to that new node

			previous_node.next_node = temp_node;
		}

		// initialize the new node

		temp_node.node_number = node_number_counter;

		temp_node.radius = support.input_initial_disc_size(node_number_counter);

		temp_node.spouse = null;

		temp_node.tight_towards_tree_root = null;

		temp_node.tight_away_from_tree_root = null;

		temp_node.sibling = null;

		temp_node.next_node = null;

	}

}
