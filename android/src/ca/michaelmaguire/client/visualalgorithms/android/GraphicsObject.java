package ca.michaelmaguire.client.visualalgorithms.android;

import android.graphics.Bitmap;

class GraphicObject
{
	private Bitmap		iBitmap;
	private Coordinates	iCoordinates;
	private Speed		iSpeed;

	public GraphicObject( Bitmap aBitmap )
	{
		iBitmap = aBitmap;
		iCoordinates = new Coordinates();
		iSpeed = new Speed();
	}

	public Speed getSpeed()
	{
		return iSpeed;
	}

	public Bitmap getBitmap()
	{
		return iBitmap;
	}

	public Coordinates getCoordinates()
	{
		return iCoordinates;
	}

	public class Coordinates
	{
		private int	iX	= 0;
		private int	iY	= 0;

		public int getX()
		{
			return iX + iBitmap.getWidth() / 2;
		}

		public void setX( int aValue )
		{
			iX = aValue - iBitmap.getWidth() / 2;
		}

		public int getY()
		{
			return iY + iBitmap.getHeight() / 2;
		}

		public void setY( int aValue )
		{
			iY = aValue - iBitmap.getHeight() / 2;
		}

		public String toString()
		{
			return "Coordinates: (" + iX + "/" + iY + ")";
		}
	}

	public class Speed
	{
		public static final int	X_DIRECTION_RIGHT	= 1;
		public static final int	X_DIRECTION_LEFT	= -1;
		public static final int	Y_DIRECTION_DOWN	= 1;
		public static final int	Y_DIRECTION_UP		= -1;

		private int				iXSpeed				= 1;
		private int				iYSpeed				= 1;

		private int				iXDirection			= X_DIRECTION_RIGHT;
		private int				iYDirection			= Y_DIRECTION_DOWN;

		public int getXDirection()
		{
			return iXDirection;
		}

		public void setXDirection( int aDirection )
		{
			iXDirection = aDirection;
		}

		public void toggleXDirection()
		{
			if( iXDirection == X_DIRECTION_RIGHT )
			{
				iXDirection = X_DIRECTION_LEFT;
			}
			else
			{
				iXDirection = X_DIRECTION_RIGHT;
			}
		}

		public int getYDirection()
		{
			return iYDirection;
		}

		public void setYDirection( int aDirection )
		{
			iYDirection = aDirection;
		}

		public void toggleYDirection()
		{
			if( iYDirection == Y_DIRECTION_DOWN )
			{
				iYDirection = Y_DIRECTION_UP;
			}
			else
			{
				iYDirection = Y_DIRECTION_DOWN;
			}
		}

		public int getX()
		{
			return iXSpeed;
		}

		public void setX( int aSpeed )
		{
			iXSpeed = aSpeed;
		}

		public int getY()
		{
			return iYSpeed;
		}

		public void setY( int aSpeed )
		{
			iYSpeed = aSpeed;
		}

		public String toString()
		{
			String xDirection;
			if( iXDirection == X_DIRECTION_RIGHT )
			{
				xDirection = "right";
			}
			else
			{
				xDirection = "left";
			}
			return "Speed: x: " + iXSpeed + " | y: " + iYSpeed + " | xDirection: " + xDirection;
		}
	}
}