
package matching;

import java.util.Vector;
import java.util.Enumeration;

import java.awt.*; 



class edge extends Object

{



	node node1, node2;
	
	boolean married = false;

	boolean tight = true;

	boolean just_changed = true;


	edge( node node1, node node2 )
	{

		this.node1 = node1;
		this.node2 = node2;
		married = false;
		tight = false;
	}


	void divorce()
	{

		married = false;

	}



	void ditight()
	{

		tight = false;

	}





	void paint(Graphics g)

	{

		if( married )

		{


			g.drawLine( (int)node1.x, (int)node1.y+1, (int)node2.x, (int)node2.y+1 );
			g.drawLine( (int)node1.x, (int)node1.y-1, (int)node2.x, (int)node2.y-1 );
			g.drawLine( (int)node1.x+1, (int)node1.y, (int)node2.x+1, (int)node2.y );
			g.drawLine( (int)node1.x-1, (int)node1.y, (int)node2.x-1, (int)node2.y );


		}

		if( (married || tight) && ! just_changed )

		{

			g.drawLine( (int)node1.x, (int)node1.y, (int)node2.x, (int)node2.y );

		}

	}



}









class Edges extends Vector
{

	Edges( short number_of_nodes_to_be_able_to_connect )
	{
	
		//call the superclass' constructor
		super( 1 + lookup( number_of_nodes_to_be_able_to_connect, (short) (number_of_nodes_to_be_able_to_connect-1) ) );
		
		setSize( 1+ lookup( number_of_nodes_to_be_able_to_connect, (short) (number_of_nodes_to_be_able_to_connect-1) ) );
	
		//System.out.println( "number_of_nodes_to_be_able_to_connect " + number_of_nodes_to_be_able_to_connect );
		//System.out.println( "size "+size() );
		
	}

	private static int lookup( short num1, short num2 )
	{

		return( ( Math.max(num1,num2) - 3) * Math.max(num1,num2) / 2 + Math.min(num1,num2) );

	}


	void make_ready_for_last_paint_in_animation()
	{
	
		edge	temp_edge;
		
		for (Enumeration e = elements() ; e.hasMoreElements() ;) 
		{
		
			if( (temp_edge = (edge) e.nextElement()) != null )
			{
				temp_edge.just_changed = false;
			}
				
		}	
	
	}



	void paint(Graphics g)
	{
		
		edge	temp_edge;
		
		for (Enumeration e = elements() ; e.hasMoreElements() ;) 
		{
		
			if( (temp_edge = (edge) e.nextElement()) != null )
			{
				temp_edge.paint(g);
			}
				
		}
	
	}

	edge add_edge( node node1, node node2 )
	{
		edge	current_edge;
		int	current_index;
		
		
		//System.out.println( "Size" + size() );
		
		current_index = lookup( node1.node_number, node2.node_number );

		//System.out.println( "current_index " + current_index );
		//System.out.println( "node1.node_number " + node1.node_number );
		//System.out.println( "node2.node_number " + node2.node_number );
		

		try
		{
			//System.out.println( "in try" );

			current_edge = (edge) elementAt( current_index );
		}
		catch( ArrayIndexOutOfBoundsException e )
		{
			setSize( 1+ current_index );
			current_edge = (edge) elementAt( current_index );
		}

		if( current_edge == null )
		{	
			setElementAt(  (Object) (current_edge = new edge( node1,  node2 )), current_index );
		}
		
		current_edge.just_changed = true;
				
		return( current_edge );
	}
	
	void add_tight_edge( node node1, node node2 )
	{
		add_edge(node1, node2).tight = true;
	}


	void marry_edge( node node1, node node2 )
	{
		add_edge(node1, node2).married = true;
	}

	void remove_tight_edge( node node1, node node2 )
	{
		edge current_edge;
		int current_index = lookup( node1.node_number, node2.node_number );
		
		
		try
		{
			current_edge = (edge) elementAt( current_index );
			current_edge.tight = false;
		}
		catch( ArrayIndexOutOfBoundsException e )
		{
			//do nothing			
		}
	}

	void divorce_edge( node node1, node node2 )
	{
		edge current_edge;
		
		try
		{
			current_edge = (edge) elementAt( lookup( node1.node_number, node2.node_number ) );
			current_edge.married = false;
		}
		catch( ArrayIndexOutOfBoundsException e )
		{
			// do nothing		
		}
	}
	
}
