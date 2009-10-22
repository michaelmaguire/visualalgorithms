package matching;

import java.awt.*; 


class disk extends Object
{

	int radius;
	Color colour;

	disk()
	{
		radius = 0;
		colour = Color.red;
	}

	disk( int radius )
	{
		this.radius = radius;
		colour = Color.red;
	}

	disk( int radius, Color the_colour )
	{
		this.radius = radius;
		colour = the_colour;
	}
	

	void change_colour(Color the_colour)
	{
		colour = the_colour;
	}


	void paint(Graphics g, int x, int y)
	{
		g.setColor( colour );
		g.fillOval(x - radius, y - radius, radius*2, radius*2 );
	}

        void bipartite_paint(Graphics g, int x, int y)
        {

		if(radius > 0)
		{
			g.setColor( Color.black );
        	        g.fillOval(x - radius, y - radius, radius*2, radius*2 );
                	g.setColor( colour );
                	g.fillOval(x - radius+1, y - radius+1, radius*2-2, radius*2-2 );
		}
		else
		{
			g.setColor( Color.black );
			g.fillOval(x + radius, y + radius, -radius*2, -radius*2 );
			g.setColor( colour );
			g.fillOval(x + radius+1, y + radius+1, -radius*2-2, -radius*2-2);
		}


        }

	void augment_radius( int amount_to_augment_by )
	{
		radius += amount_to_augment_by;
	}

	int radius()
	{
		return( radius );
	}

}


