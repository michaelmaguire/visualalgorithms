package ca.michaelmaguire.client.visualalgorithms.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class GraphicsActivity extends Activity
{
	@Override
	public void onCreate( Bundle aSavedInstanceState )
	{
		super.onCreate( aSavedInstanceState );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView( new GraphicsView( this ) );
	}
}