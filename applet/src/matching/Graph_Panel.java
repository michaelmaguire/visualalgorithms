//implements a combinatorial graph object with disk

package matching;

// do not import these or we get a name conflict
//import matching.non_bipartite_perfect_matching.*;
//import matching.bipartite_perfect_matching.*;
//import matching.bipartite_vertex_cover.*;
//import matching.non_bipartite_spanning_tree.*;

import java.applet.*;

import java.awt.*;

import java.util.Vector;
import java.util.Enumeration;

class Graph_Panel extends Panel implements Runnable, Algorithm_Support

{

	private boolean				growth_to_do				= false;

	private Image				offscreen;

	private Dimension			offscreensize;

	private Graphics			offgraphics;

	private static final short	initial_number_of_nodes		= 25;

	private Vector				nodes						= new Vector(initial_number_of_nodes);

	private Edges				edges						= new Edges(initial_number_of_nodes);

	private boolean				still_adding_nodes			= true;

	private Algorithms_Provide	the_algorithm;

	private float				distance_to_grow			= 0;

	private boolean				algorithm_done				= false;

	private boolean				animation_to_do				= false;

	private Checkbox			blue_Checkbox;

	private String				algorithm_string			= "Non-Bipartite Perfect Matching";

	private boolean				algorithm_is_bipartite		= true;

	private short				moving_node					= 0;

	private Thread				animator					= null;

	private Font				font						= new Font("Helvetica", Font.PLAIN, 8);

	private boolean				please_stop_animator;

	private boolean				single_stepping;
	private boolean				single_step_to_do;

	private short				node_to_step_algorithm_with	= 0;

	static final int			close_enough				= 5;
	private int					original_moving_x;
	private int					original_moving_y;

	public boolean				drag_node					= false;
	public int					drag_node_x;
	public int					drag_node_y;

	/** goddyn */
	public int					animation_delay;
	public float				growth_increment			= 10.0f;

	private void draw_drag_node(Graphics g)
	{

		if (drag_node)
		{

			g.setColor(Color.black);

			g.drawOval(drag_node_x - 3, drag_node_y - 3, 6, 6);

		}

	}

	public void run()

	{

		// System.out.println("In run, before while loop ");

		while (!please_stop_animator)

		{

			// System.out.println( "number of active threads: " +
			// animator.activeCount() );

			/* do stuff here */

			if (growth_to_do)
			{

				grow_nodes();

				repaint();

			}
			else if (animation_to_do && !algorithm_done && (!single_stepping || single_step_to_do))
			{

				single_step_to_do = false;

				the_algorithm.step(node_to_step_algorithm_with);

				growth_to_do = true;

				grow_nodes();

				repaint();

				// System.out.println("In run, after go ");

			}

			try

			{

				animator.sleep(animation_delay);

			}

			catch (InterruptedException e)

			{

				break;

			}

		}

		animator = null;

	}

	public synchronized void step()

	{

		single_stepping = true;

		single_step_to_do = true;

		node_to_step_algorithm_with = 0;

		if (still_adding_nodes)

		{

			still_adding_nodes = false;

			setup_algorithm();

			animation_to_do = true;

		}

		if (null == animator)
		{
			start();
		}

	}

	public synchronized void go()

	{

		single_stepping = false;

		node_to_step_algorithm_with = 0;

		if (still_adding_nodes)

		{

			still_adding_nodes = false;

			setup_algorithm();

			animation_to_do = true;

			algorithm_done = false;

		}

		if (null == animator)
		{
			start();
		}

	}

	void reset_everything()

	{

		stop();

		nodes = new Vector(initial_number_of_nodes);

		edges = new Edges((short) nodes.size());

		disk.reset_disks();

		still_adding_nodes = true;

		distance_to_grow = 0;

		algorithm_done = false;

		animation_to_do = false;

		repaint();

	}

	void reset_to_nodes_only()
	{

		node temp_node;

		stop();

		edges = new Edges((short) nodes.size());

		still_adding_nodes = true;

		distance_to_grow = 0;

		algorithm_done = false;

		animation_to_do = false;

		disk.reset_disks();

		for (Enumeration e = nodes.elements(); e.hasMoreElements();)
		{

			if ((temp_node = (node) e.nextElement()) != null)
			{

				temp_node.reset();

			}

		}

		repaint();

	}

	// private void add_node( int x, int y, boolean
	// opposite_colour_of_checkbox_state)
	public void add_node(int x, int y, boolean opposite_colour_of_checkbox_state)
	{

		nodes.addElement(new node(x, y, (opposite_colour_of_checkbox_state ^ blue_Checkbox.getState()), (short) (nodes
				.size() + 1)));

	}

	private void move_node(short node_number, int x, int y)
	{

		((node) nodes.elementAt((int) node_number - 1)).save(x, y);
	}

	private void toggle_select_status_of_node(short node_number)
	{

		((node) nodes.elementAt((int) node_number - 1)).selected = !((node) nodes.elementAt((int) node_number - 1)).selected;
	}

	private synchronized void setup_algorithm()
	{

		if (algorithm_string.equals("Bipartite Perfect Matching"))

		{

			algorithm_is_bipartite = true;

			the_algorithm = new matching.bipartite_perfect_matching.Algorithm((Algorithm_Support) this, (short) nodes
					.size());

		}

		else if (algorithm_string.equals("Spanning Tree"))

		{

			algorithm_is_bipartite = false;

			the_algorithm = new matching.non_bipartite_spanning_tree.Algorithm((Algorithm_Support) this, (short) nodes
					.size());

		}

		else if (algorithm_string.equals("Non-Bipartite Perfect Matching"))

		{

			algorithm_is_bipartite = false;

			the_algorithm = new matching.non_bipartite_perfect_matching.Algorithm((Algorithm_Support) this,
					(short) nodes.size());

		}

		else if (algorithm_string.equals("Bipartite Vertex Cover"))

		{

			algorithm_is_bipartite = true;

			the_algorithm = new matching.bipartite_vertex_cover.Algorithm((Algorithm_Support) this, (short) nodes
					.size());

		}

	}

	private void grow_nodes()

	{

		// /System.out.println("distance_to_grow: " + distance_to_grow);

		node temp_node;

		float growth_this_time = distance_to_grow;

		/** float growth_increment = 1.0f; */

		if (distance_to_grow == 0f)
		{
			if (growth_to_do)
			{
				edges.make_ready_for_last_paint_in_animation();
				growth_to_do = false;
			}
		}
		else
		{

			if (growth_increment < distance_to_grow)
			{
				growth_this_time = growth_increment;
				distance_to_grow -= growth_increment;
			}
			else
			{
				growth_this_time = distance_to_grow;
				distance_to_grow = 0f;
			}

			growth_to_do = true;
		}

		// System.out.println("growth_this_time: "+ growth_this_time);

		for (Enumeration e = nodes.elements(); e.hasMoreElements();)
		{

			if ((temp_node = (node) e.nextElement()) != null)
			{

				if (temp_node.should_grow)

				{

					temp_node.outermost_disk().augment_radius(growth_this_time);

					temp_node.should_grow = growth_to_do;

				}

				if (temp_node.should_shrink)

				{

					temp_node.outermost_disk().augment_radius(-growth_this_time);

					temp_node.should_shrink = growth_to_do;

				}
			}

		}

	}

	public synchronized void update(Graphics g)

	{

		paint(g);

	}

	public synchronized void paint(Graphics g)

	{

		Dimension d = size();

		node temp_node;

		if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height))

		{

			offscreen = createImage(d.width, d.height);

			offscreensize = d;

			offgraphics = offscreen.getGraphics();

		}

		offgraphics.setFont(font);

		offgraphics.setColor(getBackground());

		offgraphics.fillRect(0, 0, d.width, d.height);

		if (algorithm_is_bipartite)

		{

			disk.paint_bipartite_disks_in_correct_order(offgraphics);

		}

		else

		{

			disk.paint_non_bipartite_disks_in_correct_order(offgraphics);

		}

		offgraphics.setColor(Color.black);

		for (Enumeration e = nodes.elements(); e.hasMoreElements();)
		{

			if ((temp_node = (node) e.nextElement()) != null)
			{
				temp_node.paint(offgraphics);
			}

		}

		draw_drag_node(offgraphics);

		offgraphics.setColor(Color.black);

		edges.paint(offgraphics);

		g.drawImage(offscreen, 0, 0, null);

	}

	short find_node_at_position(int x, int y)
	{

		node temp_node;

		for (Enumeration e = nodes.elements(); e.hasMoreElements();)
		{

			if ((temp_node = (node) e.nextElement()) != null)
			{
				if (temp_node.close_to(x, y))
				{

					return (temp_node.node_number);
				}
			}

		}

		return ((short) 0);

	}

	/**
	 * compares (x,y) parameters to values stored in original_moving_x, and
	 * original_moving_y to see if a move should be counted as valid
	 */
	private boolean no_real_move(int x, int y)
	{

		return ((java.lang.Math.abs(x - original_moving_x) < close_enough) && (java.lang.Math
				.abs(y - original_moving_y) < close_enough));

	}

	public synchronized boolean mouseDown(Event evt, int x, int y)
	{

		if ((moving_node = find_node_at_position(x, y)) != 0)
		{
			original_moving_x = x;
			original_moving_y = y;

			// somehow we will have to update constantly as we move
		}

		return (true);
	}

	public synchronized boolean mouseDrag(Event evt, int x, int y)
	{

		if (still_adding_nodes)
		{
			if (moving_node != 0)
			{

				drag_node = true;
				drag_node_x = x;
				drag_node_y = y;

				repaint();

			}
		}

		return (true);
	}

	public synchronized boolean mouseUp(Event evt, int x, int y)

	{

		short node_number = 0;

		drag_node = false;

		if (still_adding_nodes)

		{

			if (moving_node != 0)
			{
				if (no_real_move(x, y))
				{
					toggle_select_status_of_node(moving_node);
				}
				else
				{

					move_node(moving_node, x, y);
					moving_node = 0;

				}
			}
			else
			{
				add_node(x, y, 0 != (evt.modifiers & Event.SHIFT_MASK));
			}

		}

		else

		{

			if (!algorithm_done)

			{

				single_stepping = true;

				single_step_to_do = true;

				if ((node_number = find_node_at_position(x, y)) != 0)
				{
					node_to_step_algorithm_with = node_number;
				}
				else
				{
					node_to_step_algorithm_with = 0;
				}

				if (still_adding_nodes)

				{

					still_adding_nodes = false;

					setup_algorithm();

					animation_to_do = true;

				}

				if (null == animator)
				{
					start();
				}

			}

		}

		repaint();

		return true;

	}

	public void start()

	{

		animator = new Thread(this);
		animator.start();
		please_stop_animator = false;

	}

	/** kills the thread right away */

	public void stop()

	{

		if (animator != null)
		{
			animator.stop();
			animator = null;
		}

	}

	/**
	 * should be used for input by user pressing stop button -- lets the
	 * algorithm gracefully finish what it was doing before killing th thread
	 */

	public void please_stop()
	{

		please_stop_animator = true;

	}

	/**
	 * this should be called to change the node_number fields in the Vector of
	 * nodes. In order for other parts of Graph_Panel to work, nodes should be
	 * stored contiguously in the Vector, with no null elements
	 */

	private void renumber_all_nodes()
	{

		node temp_node;
		short counter = 0;

		for (Enumeration e = nodes.elements(); e.hasMoreElements();)
		{

			if ((temp_node = (node) e.nextElement()) != null)
			{

				temp_node.node_number = ++counter;

			}

		}

	}

	public void delete()
	{

		node temp_node;

		boolean not_done;

		if (still_adding_nodes)
		{

			// this seems like a really dicky way to do this, but trying to
			// delete
			// all the nodes with just one pass through them always seems to
			// produce problems when more than one consecutive node is selected

			do
			{

				not_done = false;

				for (Enumeration e = nodes.elements(); e.hasMoreElements();)
				{

					if ((temp_node = (node) e.nextElement()) != null)
					{
						if (temp_node.selected)
						{
							temp_node.prepare_to_remove();
							nodes.removeElement(temp_node);

							not_done = true;

							break;
						}
					}

				}

			}
			while (not_done);

			renumber_all_nodes();

			repaint();
		}

	}

	public void bipartite_colour(Checkbox blue)

	{

		blue_Checkbox = blue;

	}

	public void change_algorithm(String new_algorithm_string)

	{

		algorithm_string = new_algorithm_string;

		reset_to_nodes_only();

	}

	public float input_graph_diameter()

	{

		return (1000.0f);

	}

	public float input_initial_disc_size(short node_number)

	{

		return (0.0f);

	}

	public void output_augmenting_now(boolean status)

	{

		;

	}

	public float input_distance(short node_number1, short node_number2)

	{

		return (((node) nodes.elementAt((int) node_number1 - 1)).distance_to(((node) nodes
				.elementAt((int) node_number2 - 1))));

	}

	public boolean input_is_node_selected(short node_number)

	{

		return (false);

	}

	public boolean input_is_node_blue(short node_number)

	{

		return (((node) nodes.elementAt((int) node_number - 1)).is_blue());

	}

	public boolean input_is_zero_growth_being_shown()

	{

		return (false);

	}

	public void output_waiting_for_next_node(boolean status)

	{

		;

	}

	public void output_ready_for_post_opt()

	{

		;

	}

	public void output_done()

	{

		algorithm_done = true;

	}

	public void output_grow(short node_number)

	{

		((node) nodes.elementAt((int) node_number - 1)).should_grow = true;

	}

	public void output_shrink(short node_number)

	{

		((node) nodes.elementAt((int) node_number - 1)).should_shrink = true;

	}

	public void output_change_by(float distance)

	{

		distance_to_grow = distance;
		// System.out.println("in output_change " + distance + " " +
		// distance_to_grow);

	}

	public void output_add_tight_line(short node_number1, short node_number2)
	{

		edges.add_tight_edge((node) nodes.elementAt((int) node_number1 - 1), (node) nodes
				.elementAt((int) node_number2 - 1));

	}

	public void output_remove_tight_line(short node_number1, short node_number2)
	{

		edges.remove_tight_edge((node) nodes.elementAt((int) node_number1 - 1), (node) nodes
				.elementAt((int) node_number2 - 1));

	}

	public void output_marry(short node_number1, short node_number2)
	{

		edges
				.marry_edge((node) nodes.elementAt((int) node_number1 - 1), (node) nodes
						.elementAt((int) node_number2 - 1));

	}

	public void output_divorce(short node_number1, short node_number2)
	{

		edges.divorce_edge((node) nodes.elementAt((int) node_number1 - 1), (node) nodes
				.elementAt((int) node_number2 - 1));

	}

	public void output_highlite_node(short node_number, boolean new_status)

	{

	}

	public void output_set_outer_colour(short node_number, short colour)

	{

		// System.out.println("in set_out_colour" + "node " + node_number +" " +
		// colour );

		((node) nodes.elementAt((int) node_number - 1)).outermost_disk().change_colour(
				Natural_Number_Colour.convert(colour));

	}

	public void output_add_disc(short node_number)
	{
		((node) nodes.elementAt((int) node_number - 1)).add_radius();

	}

	public void output_remove_disc(short node_number)

	{

		((node) nodes.elementAt((int) node_number - 1)).remove_radius();

	}

	/** goddyn */
	public void print_points()
	{
		node temp_node;
		for (Enumeration e = nodes.elements(); e.hasMoreElements();)
		{
			if ((temp_node = (node) e.nextElement()) != null)
			{
				System.out.println((int) temp_node.x + " " + (int) temp_node.y + " " + (temp_node.blue ? 1 : 0));
			}
		}
	}

}
