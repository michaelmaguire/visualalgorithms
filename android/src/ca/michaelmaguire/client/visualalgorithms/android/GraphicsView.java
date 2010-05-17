package ca.michaelmaguire.client.visualalgorithms.android;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GraphicsView extends SurfaceView implements SurfaceHolder.Callback
{

	class GraphicsTestViewThread extends Thread
	{
		private SurfaceHolder	iSurfaceHolder;
		private boolean			iIsRunning	= false;

		public GraphicsTestViewThread( SurfaceHolder aSurfaceHolder )
		{
			iSurfaceHolder = aSurfaceHolder;
		}

		public SurfaceHolder getSurfaceHolder()
		{
			return iSurfaceHolder;
		}

		public void setRunning( boolean aIsRunning )
		{
			iIsRunning = aIsRunning;
		}

		@Override
		public void run()
		{
			Canvas c;
			while( iIsRunning )
			{
				c = null;
				try
				{
					c = iSurfaceHolder.lockCanvas( null );
					synchronized( iSurfaceHolder )
					{
						/* GraphicsTestView. */updatePhysics();
						/* GraphicsTestView. */onDraw( c );
					}
				}
				finally
				{
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if( null != c )
					{
						iSurfaceHolder.unlockCanvasAndPost( c );
					}
				}
			}
		}
	}

	private GraphicsTestViewThread		iThread;
	private ArrayList<GraphicObject>	iGraphics	= new ArrayList<GraphicObject>();
	private GraphicObject				iCurrentGraphic;

	public GraphicsView( Context aContext )
	{
		super( aContext );
		getHolder().addCallback( this );
		iThread = new GraphicsTestViewThread( getHolder() );
		setFocusable( true );
	}

	@Override
	public boolean onTouchEvent( MotionEvent aMotionEvent )
	{
		synchronized( iThread.getSurfaceHolder() )
		{
			GraphicObject graphic = null;
			if( aMotionEvent.getAction() == MotionEvent.ACTION_DOWN )
			{
				graphic = new GraphicObject( BitmapFactory.decodeResource( getResources(), R.drawable.icon ) );
				graphic.getCoordinates().setX( (int) aMotionEvent.getX() - graphic.getGraphic().getWidth() / 2 );
				graphic.getCoordinates().setY( (int) aMotionEvent.getY() - graphic.getGraphic().getHeight() / 2 );
				iCurrentGraphic = graphic;
			}
			else if( aMotionEvent.getAction() == MotionEvent.ACTION_MOVE )
			{
				iCurrentGraphic.getCoordinates().setX(
						(int) aMotionEvent.getX() - iCurrentGraphic.getGraphic().getWidth() / 2 );
				iCurrentGraphic.getCoordinates().setY(
						(int) aMotionEvent.getY() - iCurrentGraphic.getGraphic().getHeight() / 2 );
			}
			else if( aMotionEvent.getAction() == MotionEvent.ACTION_UP )
			{
				iGraphics.add( iCurrentGraphic );
				iCurrentGraphic = null;
			}
			return true;
		}
	}

	@Override
	public void onDraw( Canvas aCanvas )
	{
		aCanvas.drawColor( Color.BLACK );
		Bitmap bitmap;
		GraphicObject.Coordinates coords;
		for( GraphicObject graphic : iGraphics )
		{
			bitmap = graphic.getGraphic();
			coords = graphic.getCoordinates();
			aCanvas.drawBitmap( bitmap, coords.getX(), coords.getY(), null );
		}
		// draw current graphic at last...
		if( null != iCurrentGraphic )
		{
			bitmap = iCurrentGraphic.getGraphic();
			coords = iCurrentGraphic.getCoordinates();
			aCanvas.drawBitmap( bitmap, coords.getX(), coords.getY(), null );
		}
	}

	public void updatePhysics()
	{
		GraphicObject.Coordinates coord;
		GraphicObject.Speed speed;
		for( GraphicObject graphicObject : iGraphics )
		{
			coord = graphicObject.getCoordinates();
			speed = graphicObject.getSpeed();

			// Direction
			if( speed.getXDirection() == GraphicObject.Speed.X_DIRECTION_RIGHT )
			{
				coord.setX( coord.getX() + speed.getX() );
			}
			else
			{
				coord.setX( coord.getX() - speed.getX() );
			}
			if( speed.getYDirection() == GraphicObject.Speed.Y_DIRECTION_DOWN )
			{
				coord.setY( coord.getY() + speed.getY() );
			}
			else
			{
				coord.setY( coord.getY() - speed.getY() );
			}

			// borders for x...
			if( coord.getX() < 0 )
			{
				speed.toggleXDirection();
				coord.setX( -coord.getX() );
			}
			else if( coord.getX() + graphicObject.getGraphic().getWidth() > getWidth() )
			{
				speed.toggleXDirection();
				coord.setX( coord.getX() + getWidth() - (coord.getX() + graphicObject.getGraphic().getWidth()) );
			}

			// borders for y...
			if( coord.getY() < 0 )
			{
				speed.toggleYDirection();
				coord.setY( -coord.getY() );
			}
			else if( coord.getY() + graphicObject.getGraphic().getHeight() > getHeight() )
			{
				speed.toggleYDirection();
				coord.setY( coord.getY() + getHeight() - (coord.getY() + graphicObject.getGraphic().getHeight()) );
			}
		}
	}

	@Override
	public void surfaceChanged( SurfaceHolder arg0, int arg1, int arg2, int arg3 )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated( SurfaceHolder aHolder )
	{
		iThread.setRunning( true );
		iThread.start();
	}

	@Override
	public void surfaceDestroyed( SurfaceHolder aHolder )
	{
		boolean retry = true;
		iThread.setRunning( false );
		while( retry )
		{
			try
			{
				iThread.join();
				retry = false;
			}
			catch( InterruptedException e )
			{
				// Keep trying.
			}
		}
	}
}
