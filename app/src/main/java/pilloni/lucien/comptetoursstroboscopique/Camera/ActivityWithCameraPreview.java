package pilloni.lucien.comptetoursstroboscopique.Camera;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import pilloni.lucien.comptetoursstroboscopique.Camera.CameraPreview;
import pilloni.lucien.comptetoursstroboscopique.Camera.ParametresCamera;
import pilloni.lucien.comptetoursstroboscopique.Graphiques.PreferencesUtils;
import pilloni.lucien.comptetoursstroboscopique.R;

/**
 * Created by lucien on 24/11/2016.
 */

public abstract class ActivityWithCameraPreview extends AppCompatActivity implements CameraPreview.PrewiewFrameListener{
	protected Camera _Camera = null;
	protected CameraPreview _CameraView = null;
	protected int _CameraViewId = -1;


	@Override
	protected void onResume()
	{
		if ( _CameraViewId !=-1)
			creerCameraPreview(_CameraViewId);
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		if ( _CameraView !=null)
			_CameraView.onPause();
		super.onPause();
	}

	protected void creerCamera( int viewToReplace )
	{
		_CameraViewId = viewToReplace;
	}
	/***
	 * Creation de la View de controle de la camera
	 */
	private void creerCameraPreview(int idViewToReplace)
	{
		try
		{
			_Camera = Camera.open();//you can use open(int) to use different cameras
		} catch (Exception e)
		{
			Log.d("ERROR", "Failed to get camera: " + e.getMessage());
		}

		if (_Camera != null)
		{
			ParametresCamera parametresCamera = new ParametresCamera();
			parametresCamera.niveauExposition = PreferencesUtils.getExposure(this);

			_CameraView = new CameraPreview(this, _Camera, this, parametresCamera, idViewToReplace);
			replaceView(R.id.buttonCameraPreviewReplace, _CameraView);
		}
	}
		/***
		 * Remplace une View par une autre
		 *
		 * @param currentView
		 * @param newView
		 */
	public static void replaceView(@Nullable View currentView, @NonNull  View newView)
	{
		if (currentView != null)
		{
			ViewGroup g = (ViewGroup) currentView.getParent();
			final int index = g.indexOfChild(currentView);
			android.view.ViewGroup.LayoutParams params = currentView.getLayoutParams();
			g.removeView(currentView);
			g.removeView(newView);
			g.addView(newView, index, params);
		}
	}

	public void replaceView(int viewId, View newView)
	{
		replaceView(findViewById(viewId), newView);
	}

	public void onNewPreviewFrame(Bitmap b)
	{
		// Rien: surcharger la fonction
	}

}
