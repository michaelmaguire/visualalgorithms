// Nonbipartite_Spanning_Tree.java - Michael Maguire - 14 Apr 96

package matching.non_bipartite_spanning_tree;

import matching.AlgorithmSupport;
import matching.AlgorithmsProvide;

import java.awt.*; //for debugging

public class Algorithm implements AlgorithmsProvide
{
	private static class Node
	{

		short	node_number;

		float	total_radius;

		Node	next_in_same_group;

		Node	next_on_top_level;

	}

	private Node				first_node;

	private Node				group_needing_moat;

	private Node				bumper_one;

	private Node				bumper_ones_group;

	private Node				bumper_two;

	private Node				bumper_twos_group;

	private float				nearest_distance_so_far;

	private short				current_colour;

	private boolean				first_time;

	private AlgorithmSupport	support;					// what we can rely

	// on for input and
	// output

	public Algorithm(AlgorithmSupport the_support, short number_of_nodes)

	{

		support = the_support;

		first_node = null;

		bumper_one = null;

		bumper_ones_group = null;

		bumper_two = null;

		bumper_twos_group = null;

		current_colour = 1;

		for (short node_number_counter = 1; node_number_counter <= number_of_nodes; node_number_counter++)

		{

			add_node();

		}

		first_time = true;

	}

	public boolean step(short node_number)

	{

		// ignore the node_number we are passed, it has no meaning for this
		// algorithm

		// safety check

		if (null == first_node)

			return false;

		if (null == first_node.next_on_top_level)

		{

			support.output_done();

			return false;

		}

		nearest_distance_so_far = 10000000.0f;

		find_next_bump();

		if (null != bumper_one && null != bumper_two)

		{

			if (first_time)

			{

				first_time = false;

			}

			else
			// it's not the first time we've been called

			{

				current_colour++;

				recursive_grow_moat(first_node);

			}

			support.output_change_by(nearest_distance_so_far / 2.0f);

			recursive_grow_node(first_node);

			support.output_marry(bumper_one.node_number, bumper_two.node_number);

			consolidate_groups(bumper_ones_group, bumper_twos_group);

		}

		support.output_augmenting_now(false);

		support.output_waiting_for_next_node(false);

		// check to see if we just finished

		if (null == first_node.next_on_top_level)

		{

			support.output_done();

			return true;

		}

		return false;

	}

	public void close()

	{

	}

	private void find_next_bump()

	{

		Node temp_top_level;

		Node temp_node_in_group;

		Node temp_start_of_everything_else;

		for (temp_top_level = first_node; null != temp_top_level; temp_top_level = temp_top_level.next_on_top_level)

		{

			// we want to compare everything in a group with everything not in
			// that group

			for (temp_node_in_group = temp_top_level; null != temp_node_in_group; temp_node_in_group = temp_node_in_group.next_in_same_group)

			{

				// we only want to compare with everything further than us on
				// the top level,

				// because everything before us has already been checked against
				// us

				compare_with_everything_else(temp_top_level, temp_node_in_group, temp_top_level.next_on_top_level);

			}

		}

	}

	private void compare_with_everything_else(Node the_group_we_are_checking, Node the_node_we_are_checking,
			Node the_start_of_everything_else)

	{

		Node temp_top_level;

		Node temp_node_in_group;

		float temp_distance;

		// safety check

		if (null == the_node_we_are_checking || null == the_start_of_everything_else
				|| null == the_group_we_are_checking)

		{

			return;

		}

		for (temp_top_level = the_start_of_everything_else; null != temp_top_level; temp_top_level = temp_top_level.next_on_top_level)

		{

			for (temp_node_in_group = temp_top_level; null != temp_node_in_group; temp_node_in_group = temp_node_in_group.next_in_same_group)

			{

				// System.out.println("in compare " + "node1 " +
				// the_node_we_are_checking.node_number + " node2 " +
				// temp_node_in_group.node_number);

				if ((temp_distance = support.input_distance(the_node_we_are_checking.node_number,
						temp_node_in_group.node_number)

						- (the_node_we_are_checking.total_radius + temp_node_in_group.total_radius))

				< nearest_distance_so_far)

				{

					nearest_distance_so_far = temp_distance;

					bumper_one = the_node_we_are_checking;

					bumper_ones_group = the_group_we_are_checking;

					bumper_two = temp_node_in_group;

					bumper_twos_group = temp_top_level;

				}

			}

		}

	}

	private void consolidate_groups(Node group_one, Node group_two)

	{

		Node temp_top_level;

		Node temp_node_in_group;

		Node temp_rest_of_group_one;

		// safety check

		if (null == group_one || null == group_two || (group_one == group_two))

			return;

		// we will leave group_one alone, and just insert group_two into
		// group_one,

		// right after the first node in group_one

		if (group_two == first_node)

		{

			// special case

			first_node = group_two.next_on_top_level; // we know that this is
			// o.k. because of
			// safety checks above

		}

		else
		// group_two is not first_node, so we must find the top_level node just
		// before

		{ // so that we can repair the linked list of top_levels properly

			for (temp_top_level = first_node; null != temp_top_level; temp_top_level = temp_top_level.next_on_top_level)

			{

				if (temp_top_level.next_on_top_level == group_two)

				{

					temp_top_level.next_on_top_level = group_two.next_on_top_level;

					break;

				}

			}

		}

		group_two.next_on_top_level = null;

		// insert group_two into group_one, just after group_one's top_level
		// node

		temp_rest_of_group_one = group_one.next_in_same_group;

		group_one.next_in_same_group = group_two;

		// still inserting, finding the end of group two

		// -- loop condition looks risky but isn't because we know group_two
		// exists

		for (temp_node_in_group = group_two; null != temp_node_in_group.next_in_same_group; temp_node_in_group = temp_node_in_group.next_in_same_group)

		{

			// do nothing, just move through till the end

		}

		temp_node_in_group.next_in_same_group = temp_rest_of_group_one;

	}

	private void recursive_grow_node(Node the_node)

	{

		// safety check

		if (null == the_node)

			return;

		recursive_grow_node(the_node.next_on_top_level);

		recursive_grow_node(the_node.next_in_same_group);

		support.output_grow(the_node.node_number);

		the_node.total_radius += nearest_distance_so_far / 2.0;

	}

	private void recursive_grow_moat(Node the_node)

	{

		// safety check

		if (null == the_node)

			return;

		support.output_add_disc(the_node.node_number);

		support.output_set_outer_colour(the_node.node_number, current_colour);

		recursive_grow_moat(the_node.next_on_top_level);

		recursive_grow_moat(the_node.next_in_same_group);

	}

	private void add_node()

	// node numbering starts at 1

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

			temp_node = temp_node.next_on_top_level;

		}

		temp_node = new Node();

		// take special action if the new node is the first node to be added to
		// the graph

		if (null == first_node)

			first_node = temp_node;

		else
			// the graph already has nodes, so make the node before the new node
			// point to that new node

			previous_node.next_on_top_level = temp_node;

		// initialize the new node

		temp_node.node_number = node_number_counter;

		temp_node.total_radius = support.input_initial_disc_size(node_number_counter);

		temp_node.next_on_top_level = null;

		temp_node.next_in_same_group = null;

		support.output_set_outer_colour(temp_node.node_number, current_colour);

	}

}
