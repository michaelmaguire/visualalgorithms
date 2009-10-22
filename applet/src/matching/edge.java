package matching;

import java.awt.*; 


class edge extends Object
{

	int x1, y1, x2, y2;

	boolean married = false;
	boolean tight = true;

	edge( int tx1, int ty1, int tx2, int ty2 )
	{
		x1 = tx1;
		y1 = ty1;
		x2 = tx2;
		y2 = ty2;
	}

	edge( int tx1, int ty1, int tx2, int ty2, boolean is_married )
	{
		x1 = tx1;
		y1 = ty1;
		x2 = tx2;
		y2 = ty2;

		married = is_married;
		tight = ! is_married;
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
			g.drawLine( x1-1, y1-1, x2-1, y2-1 );
			g.drawLine( x1+1, y1+1, x2+1, y2+1 );
		}
		if( married || tight )
		{
			g.drawLine( x1, y1, x2, y2 );
		}
	}

}


