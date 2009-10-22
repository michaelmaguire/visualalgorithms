//implements a combinatorial graph object with disk

package matching;

import matching.non_bipartite_perfect_matching.*;
import matching.bipartite_perfect_matching.*;
import matching.bipartite_vertex_cover.*;
import matching.non_bipartite_spanning_tree.*;


import java.applet.*;    

import java.awt.*; 

import java.util.Vector;
import java.util.Enumeration;


class Graph_Panel extends Panel implements Runnable, Algorithm_Support

{



	private Image offscreen;

	private Dimension offscreensize;

	private Graphics offgraphics;

	private static final short initial_number_of_nodes = 40;

	private Vector nodes = new Vector(initial_number_of_nodes);

	private Edges edges = new Edges(initial_number_of_nodes);

	private public boolean still_adding_nodes = true;

	private Algorithms_Provide the_algorithm;

	private int distance_to_grow = 0;

	private boolean algorithm_done = false;

	private boolean animation_to_do = false;

	private Checkbox blue_Checkbox;

	private String algorithm_string = "Non-Bipartite Perfect Matching";

	private boolean algorithm_is_bipartite = true;

	private short moving_node = 0;

	private Thread animator = null;

	private Font font = new Font("Helvetica", Font.PLAIN, 6);


	void add_node( int x, int y)
	{

		nodes.addElement( new node(x,y, blue_Checkbox.getState()) );

	}


	void move_node( short node_number, int x, int y)
	{

		((node) nodes.elementAt( (int) node_number-1 )).save( x,y );
	}




	public void run()

	{

		while (true)

		{

			/* do stuff here */

			if( animation_to_do )
			{

				go();

			}




			try

			{

				Thread.sleep(100);

			}

			catch (InterruptedException e)

			{

				break;

			}

		}

	}





	public synchronized void step()

	{



		if( still_adding_nodes )

		{

			still_adding_nodes = false;

			if( algorithm_string.equals("Bipartite Perfect Matching") )

			{

				algorithm_is_bipartite = true;

				the_algorithm = new matching.bipartite_perfect_matching.Algorithm( (Algorithm_Support) this, (short) nodes.size());		



			}

			else if( algorithm_string.equals("Spanning Tree") )

			{

				algorithm_is_bipartite = false;

				the_algorithm = new matching.non_bipartite_spanning_tree.Algorithm( (Algorithm_Support) this, (short) nodes.size());		

			}

			else if( algorithm_string.equals("Non-Bipartite Perfect Matching") )

			{

				algorithm_is_bipartite = false;

				the_algorithm = new matching.non_bipartite_perfect_matching.Algorithm( (Algorithm_Support) this, (short) nodes.size());		

			}

			else if( algorithm_string.equals("Bipartite Vertex Cover") )

			{

				algorithm_is_bipartite = true;

				the_algorithm = new matching.bipartite_vertex_cover.Algorithm( (Algorithm_Support) this, (short) nodes.size());		

			}

			the_algorithm.step((short)0);

			grow_nodes();

		}

		else

		{

			if( ! algorithm_done )

			{

				the_algorithm.step((short)0);

				grow_nodes();

			}



		}



		repaint();

	} 	



	public synchronized void go()

	{



		if( still_adding_nodes )

		{

			still_adding_nodes = false;

			if( algorithm_string.equals("Bipartite Perfect Matching") )

			{

				algorithm_is_bipartite = true;

				the_algorithm = new matching.bipartite_perfect_matching.Algorithm( (Algorithm_Support) this, (short) nodes.size());		



			}

			else if( algorithm_string.equals("Spanning Tree") )

			{

				algorithm_is_bipartite = false;

				the_algorithm = new matching.non_bipartite_spanning_tree.Algorithm( (Algorithm_Support) this, (short) nodes.size());		

			}

			else if( algorithm_string.equals("Non-Bipartite Perfect Matching") )

			{

				algorithm_is_bipartite = false;

				the_algorithm = new matching.non_bipartite_perfect_matching.Algorithm( (Algorithm_Support) this, (short) nodes.size());		

			}

			else if( algorithm_string.equals("Bipartite Vertex Cover") )

			{

				algorithm_is_bipartite = true;

				the_algorithm = new matching.bipartite_vertex_cover.Algorithm( (Algorithm_Support) this, (short) nodes.size());		

			}


		}



		if( ! algorithm_done )

		{

			animation_to_do = true;

			if( animator == null )
			{
				start();
			}

			the_algorithm.step((short)0);

			grow_nodes();

			repaint();

		}
		else
		{
			animation_to_do = false;
		}


	} 	



	void reset()

	{



		nodes = new Vector(initial_number_of_nodes);


		edges = new Edges(initial_number_of_nodes);

		node.reset_nodes();

		disk.reset_disks();

		still_adding_nodes = true;

		distance_to_grow = 0;

		algorithm_done = false;

		animation_to_do = false;


		repaint();

	}



	void grow_nodes()

	{



		for(int counter = 0; counter < nodes.size(); counter++)

		{



			//System.out.println("in grow_nodes " + "counter " + counter + " " + ((node) nodes.elementAt(counter)).should_grow);

			//System.out.println("in grow_nodes " + "distance_to_grow " + distance_to_grow);

			if( ((node) nodes.elementAt(counter)).should_grow )

			{

				((node) nodes.elementAt(counter)).outermost_disk().augment_radius(distance_to_grow);

				((node) nodes.elementAt(counter)).should_grow = false;

			}

			if( ((node) nodes.elementAt(counter)).should_shrink )

			{

				((node) nodes.elementAt(counter)).outermost_disk().augment_radius(-distance_to_grow);

				((node) nodes.elementAt(counter)).should_shrink = false;

			}

		}



	}





	public synchronized void update(Graphics g)

	{

		paint( g ); 

	}



	public synchronized void paint(Graphics g)

	{

		Dimension d = size();

		if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height))

		{

			offscreen = createImage(d.width, d.height);

			offscreensize = d;

			offgraphics = offscreen.getGraphics();

		}


		offgraphics.setFont( font );

		offgraphics.setColor(getBackground());

		offgraphics.fillRect(0, 0, d.width, d.height);







		if( algorithm_is_bipartite )

		{


 			disk.paint_bipartite_disks_in_correct_order(offgraphics);

		}

		else

		{

			disk.paint_non_bipartite_disks_in_correct_order(offgraphics);

		}



		offgraphics.setColor( Color.black );

		for( int counter = 0; counter < nodes.size(); counter++)

		{

			((node) nodes.elementAt(counter)).paint(offgraphics);

		}



		offgraphics.setColor( Color.black );


		edges.paint(offgraphics);


		g.drawImage(offscreen, 0, 0, null);





	}


	short find_node_at_position( int x, int y)
	{

		node	temp_node;

		for (Enumeration e = nodes.elements() ; e.hasMoreElements() ;) 
		{

			if( (temp_node = (node) e.nextElement()) != null )
			{
				if( temp_node.close_to( x,y) )
				{

					return( temp_node.node_number );
				}
			}

		}


		return( (short) 0 );

	}


	public synchronized boolean mouseDown( Event evt, int x, int y)
	{

		if( (moving_node = find_node_at_position( x, y ) ) != 0)
		{
			//somehow we will have to update constantly as we move
		}



		return(true);
	}


	public synchronized boolean mouseUp(Event evt, int x, int y)

	{

		short node_number;


		if( still_adding_nodes )

		{

			if( moving_node != 0 )
			{
				move_node( moving_node, x, y);
				moving_node = 0;
			}
			else
			{
				add_node( x, y);
			}

		}

		else

		{

			if( ! algorithm_done )

			{

				if( (node_number = find_node_at_position(x,y)) != 0 )
				{
					the_algorithm.step(node_number);
				}
				else
				{
					the_algorithm.step((short)0);
				}

				grow_nodes();


			}


		}

		repaint();

		return true;

	}



	public void start()

	{

		animator = new Thread(this);
		animator.start();

	}



	public void stop()

	{

		if(animator!=null)
		{
			animator.stop();
			animator = null;
		}

	}





	public float input_graph_diameter()

	{

		return( 1000.0f );

	}



	public float input_initial_disc_size( short node_number )

	{

		return( 0.0f );

	}



	public void output_augmenting_now( boolean status )

	{

		;

	}



	public float input_distance( short node_number1, short node_number2)

	{

		return( ((node) nodes.elementAt((int)node_number1-1)).distance_to( ((node) nodes.elementAt((int)node_number2-1) ) ) );

	}



	public boolean input_is_node_selected( short node_number )

	{

		return(false);

	}



	public boolean input_is_node_blue( short node_number )

	{

		return(((node) nodes.elementAt((int)node_number-1)).is_blue());

	}



	public boolean input_is_zero_growth_being_shown()

	{

		return(false);

	}



	public void output_waiting_for_next_node( boolean status )

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



	public void output_grow( short node_number)

	{

		((node) nodes.elementAt((int)node_number-1)).should_grow = true;

	}



	public void output_shrink( short node_number)

	{

		((node) nodes.elementAt((int)node_number-1)).should_shrink = true;

	}



	public void output_change_by( float distance )

	{


		distance_to_grow = (int) distance;
		//System.out.println("in output_change " + distance + " " + distance_to_grow);

	}



	public void output_add_tight_line( short node_number1, short node_number2)
	{

		edges.add_tight_edge( (node) nodes.elementAt( (int)node_number1-1 ), (node) nodes.elementAt( (int)node_number2-1 ) );

	}



	public void output_remove_tight_line( short node_number1, short node_number2)
	{

		edges.remove_tight_edge( (node) nodes.elementAt( (int)node_number1-1 ), (node) nodes.elementAt( (int)node_number2-1 ) );

	}


	public void output_marry( short node_number1, short node_number2 )
	{

		edges.marry_edge( (node) nodes.elementAt( (int)node_number1-1 ), (node) nodes.elementAt( (int)node_number2-1 ) );

	}



	public void output_divorce(  short node_number1, short node_number2 )
	{

		edges.divorce_edge( (node) nodes.elementAt( (int)node_number1-1 ), (node) nodes.elementAt( (int)node_number2-1 ) );

	}



	public void output_highlite_node(  short node_number, boolean new_status )

	{

	}





	public void output_set_outer_colour( short node_number, short colour)

	{

		//System.out.println("in set_out_colour" + "node " + node_number +" " + colour );

		((node) nodes.elementAt((int)node_number-1)).outermost_disk().change_colour( Natural_Number_Colour.convert(colour) );

	}



	public void output_add_disc( short node_number )
	{
		((node) nodes.elementAt((int)node_number-1)).add_radius();

	}



	public void output_remove_disc( short node_number )

	{

		((node) nodes.elementAt((int)node_number-1)).remove_radius();

	}







	public void bipartite_colour( Checkbox blue )

	{

		blue_Checkbox = blue;

	}





	public void change_algorithm( String new_algorithm_string )

	{

		algorithm_string = new_algorithm_string;

		reset();

	}





}





























