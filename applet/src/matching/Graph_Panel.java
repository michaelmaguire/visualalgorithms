//implements a combinatorial graph object with disk

package matching;


import java.applet.*;    
import java.awt.*; 


class Graph_Panel extends Panel implements Runnable, Algorithm_Support
{

Image offscreen;
Dimension offscreensize;
Graphics offgraphics;
int number_of_nodes;
int number_of_edges;
int max_nodes = 40;
node nodes[] = new node[max_nodes];
int max_edges = max_nodes*max_nodes;
edge edges[] = new edge[max_edges];
public boolean still_adding_nodes = true;
Algorithms_Provide the_algorithm;
int most_disks_so_far = 1;
int distance_to_grow = 0;
boolean algorithm_done = false;
Checkbox blue_Checkbox;
String algorithm_string = "Bipartite Perfect Matching";
boolean algorithm_is_bipartite = true;

int add_node( int x, int y)
{
	node n = new node(x,y, blue_Checkbox.getState());
	nodes[number_of_nodes] = n;
	return(++number_of_nodes);
}


public void run()
{
	while (true)
	{
		/* do stuff here */

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


public void step()
{

	if( still_adding_nodes )
	{
		still_adding_nodes = false;
		if( algorithm_string.equals("Bipartite Perfect Matching") )
		{
			algorithm_is_bipartite = true;
			the_algorithm = new Bipartite_Perfect_Matching( (Algorithm_Support) this, (short) number_of_nodes);		
	
		}
		else if( algorithm_string.equals("Spanning Tree") )
		{
			algorithm_is_bipartite = false;
			the_algorithm = new Nonbipartite_Spanning_Tree( (Algorithm_Support) this, (short) number_of_nodes);		
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


void reset()
{

	number_of_nodes = 0;
	number_of_edges = 0;
	nodes = new node[max_nodes];
	edges = new edge[max_edges];
	still_adding_nodes = true;
	most_disks_so_far = 1;
	distance_to_grow = 0;
	algorithm_done = false;

	repaint();
}

void grow_nodes()
{

	for(int counter = 0; counter < number_of_nodes; counter++)
	{

		//System.out.println("in grow_nodes " + "counter " + counter + " " + nodes[counter].should_grow);
		//System.out.println("in grow_nodes " + "distance_to_grow " + distance_to_grow);
		if( nodes[counter].should_grow )
		{
			nodes[counter].outermost_disk().augment_radius(distance_to_grow);
			nodes[counter].should_grow = false;
		}
		if( nodes[counter].should_shrink )
		{
			nodes[counter].outermost_disk().augment_radius(-distance_to_grow);
			nodes[counter].should_shrink = false;
		}
	}

}




public synchronized void update(Graphics g)
{
	Dimension d = size();
	if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height))
	{
		offscreen = createImage(d.width, d.height);
		offscreensize = d;
		offgraphics = offscreen.getGraphics();
	}

	offgraphics.setColor(getBackground());
	offgraphics.fillRect(0, 0, d.width, d.height);



	if( algorithm_is_bipartite )
	{
              for( int level = most_disks_so_far-1; level >= 0; level--)
                {
                        for( int counter = 0; counter < number_of_nodes; counter++)
                        {
                                nodes[counter].paint_bipartite_disk(offgraphics, level);
                        }
                }
	}
	else
	{
		for( int level = most_disks_so_far-1; level >= 0; level--)
		{
			for( int counter = 0; counter < number_of_nodes; counter++)
			{
				nodes[counter].paint_disk(offgraphics, level);
			}
		}
	}

	offgraphics.setColor( Color.black );
	for( int counter = 0; counter < number_of_nodes; counter++)
	{
		nodes[counter].paint(offgraphics);
	}

	offgraphics.setColor( Color.black );
	for( int edge_counter=0; edge_counter < max_edges; edge_counter++)
	{

		if( edges[edge_counter] != null )
		{
			edges[edge_counter].paint(offgraphics);
		}
	}

	g.drawImage(offscreen, 0, 0, null);


}

public synchronized boolean mouseUp(Event evt, int x, int y)
{

	if( still_adding_nodes )
	{
		add_node( x, y);
	}
	else
	{

	}
	repaint();
	return true;
}

public void start()
{
	;
}

public void stop()
{
	;
}


public double input_graph_diameter()
{
	return( 0.0 );
}

public double input_initial_disc_size( short node_number )
{
	return( 0.0 );
}

public void output_augmenting_now( boolean status )
{
	;
}

public double input_distance( short node_number1, short node_number2)
{
	return( nodes[node_number1 -1].distance_to( nodes[node_number2 -1] ) );
}

public boolean input_is_node_selected( short node_number )
{
	return(false);
}

public boolean input_is_node_blue( short node_number )
{
	return(nodes[node_number-1].is_blue());
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
	nodes[node_number-1].should_grow = true;
}

public void output_shrink( short node_number)
{
	nodes[node_number-1].should_shrink = true;
}

public void output_change_by( double distance )
{
	distance_to_grow = (int) distance;
}

public void output_add_tight_line( short node_number1, short node_number2)
{
	int temp_lookup;

	temp_lookup = lookup( node_number1, node_number2 );

	if (edges[temp_lookup] != null )
	{
		edges[temp_lookup].tight = true;
	}
	else
	{
		edge the_edge = new edge( nodes[node_number1-1].x, nodes[node_number1-1].y, 
					nodes[node_number2-1].x, nodes[node_number2-1].y, false );
		edges[temp_lookup] = the_edge;
		number_of_edges++;
	}
}

public void output_remove_tight_line( short node_number1, short node_number2)
{
	int temp_lookup;

	temp_lookup = lookup( node_number1, node_number2 );

	if (edges[temp_lookup] != null )
	{
		edges[temp_lookup].tight = false;
	}
}

private int lookup( short num1, short num2 )
{

	return( ( Math.max(num1,num2) - 3) * Math.max(num1,num2) / 2 + Math.min(num1,num2)  + 1);

}

public void output_marry( short node_number1, short node_number2 )
{
	int temp_lookup;

	temp_lookup = lookup( node_number1, node_number2 );

	if (edges[temp_lookup] != null )
	{
		edges[temp_lookup].married = true;
	}
	else
	{
		edge the_edge = new edge( nodes[node_number1-1].x, nodes[node_number1-1].y, 
					nodes[node_number2-1].x, nodes[node_number2-1].y, true );
		edges[temp_lookup] = the_edge;
		number_of_edges++;
	}

}

public void output_divorce(  short node_number1, short node_number2 )
{
	int temp_lookup;

	temp_lookup = lookup( node_number1, node_number2 );

	if (edges[temp_lookup] != null )
	{
		edges[temp_lookup].married = false;
	}
}

public void output_highlite_node(  short node_number, boolean new_status )
{
}


public void output_set_outer_colour( short node_number, short colour)
{
	//System.out.println("in set_out_colour" + "node " + node_number +" " + colour );
	nodes[node_number-1].outermost_disk().change_colour( Natural_Number_Colour.convert(colour) );
}

public void output_add_disc( short node_number )
{
	int number_of_disks;

	number_of_disks = nodes[node_number-1].add_radius();

	if( number_of_disks >= most_disks_so_far )
	{
		most_disks_so_far = number_of_disks;
	}
}

public void output_remove_disc( short node_number )
{
}



public void bipartite_colour( Checkbox blue )
{
	blue_Checkbox = blue;
}


public void change_algorithm( String new_algorithm_string )
{
	algorithm_string = new_algorithm_string;
}


}

















