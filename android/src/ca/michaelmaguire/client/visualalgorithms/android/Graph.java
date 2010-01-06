package ca.michaelmaguire.client.visualalgorithms.android;

import android.app.Activity;
import android.os.Bundle;
import android.opengl.GLSurfaceView;

public class Graph extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Create our Preview view and set it as the content of our Activity
		iGLSurfaceView = new GLSurfaceView(this);
		iGLSurfaceView.setRenderer(new CubeRenderer(false));
		setContentView(iGLSurfaceView);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		iGLSurfaceView.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		iGLSurfaceView.onPause();
	}

	private GLSurfaceView	iGLSurfaceView;

}