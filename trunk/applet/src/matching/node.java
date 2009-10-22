package matching;

import java.awt.*; 



class node extends Object
{

	int x, y;
	int number_of_disks=0;
	disk disks[] = new disk[500];
	boolean married;
	boolean should_grow=false;
	boolean should_shrink=false;
	boolean blue=false;

	node()
	{
		x = 0;
		y = 0;

		disks[number_of_disks] = new disk(0);
		number_of_disks++;
	}

	node( int x1, int y1 )
	{
		x = x1;
		y = y1;

		disks[number_of_disks] = new disk(0);
		number_of_disks++;
	}

	node( int x1, int y1, boolean isblue )
	{
		x = x1;
		y = y1;
		blue = isblue;

		if( isblue )
		{ 
			disks[number_of_disks] = new disk(0, Color.blue);
		}
		else
		{
			disks[number_of_disks] = new disk(0, Color.red);
		}
		number_of_disks++;
	}

	int add_radius()
	{
		disks[number_of_disks] = new disk(disks[number_of_disks-1].radius);
		return(++number_of_disks);
	}


	disk outermost_disk()
	{
		return( disks[number_of_disks-1] );
	}


	void save( int x1, int y1 )
	{
		x = x1;
		y = y1;
	}


	void paint_disk(Graphics g, int level)
	{
		if( level < number_of_disks )
			disks[level].paint( g, x, y );
	}

        void paint_bipartite_disk(Graphics g, int level)
        {
                if( level < number_of_disks )
                        disks[level].bipartite_paint( g, x, y );
        }

	void paint(Graphics g)
	{

		g.setColor(Color.black);

		g.fillOval(x - 3, y - 3, 6, 6 );

		if( blue )
		{
			g.setColor(Color.blue);
		}
		else
		{
			g.setColor(Color.red);
		}

		g.fillOval(x - 2, y - 2, 4, 4 );
	}

	int x()
	{
		return( x );
	}

	int y()
	{
		return( y );
	}


	float distance_to( node other_node )
	{

		return( (float)(  Math.sqrt( (x - other_node.x)*(x - other_node.x) +
				(y - other_node.y)*(y - other_node.y) ) ) );

	}


	boolean is_blue()
	{
		return(blue);
	}

}
