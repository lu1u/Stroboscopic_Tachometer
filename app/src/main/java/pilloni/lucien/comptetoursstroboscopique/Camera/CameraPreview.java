package pilloni.lucien.comptetoursstroboscopique.Camera;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by lucien on 20/11/2016.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	static final int JPEG_QUALITY = 25;
	final private SurfaceHolder mHolder;
	private Camera mCamera;
	private static final String CAMERA_PARAM_ORIENTATION = "orientation";
	private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
	private static final String CAMERA_PARAM_PORTRAIT = "portrait";
	int _exposureMin, _exposureMax, _exposure  ;
	final PrewiewFrameListener _listener;

	public void onPause()
	{
		mCamera.stopPreview();
		mCamera.setPreviewCallback(null);
		mHolder.removeCallback(this);
		mCamera.release();
	}

	/***
	 * change l'exposition d'un delta
	 * @param delta
	 * @return
	 */
	public int changeExposition(int delta)
	{
		int v = _exposure + delta;
		if ( v >= _exposureMin || v <= _exposureMax)
		{
			_exposure = v;
			Camera.Parameters cameraParams = mCamera.getParameters();
			cameraParams.setExposureCompensation(_exposure);
			mCamera.setParameters(cameraParams);
		}

		return _exposure;
	}

	public interface PrewiewFrameListener {
		void onNewPreviewFrame(Bitmap b);
	}

	public CameraPreview(Context context, Camera camera, PrewiewFrameListener prewiewFrameListener, @Nullable  ParametresCamera parametres, int id) {
		super(context);
		this.setId(id);
		_listener = prewiewFrameListener;
		mCamera = camera;
		//get the holder and set this class as the callback, so we can get camera data here
		mHolder = getHolder();
		mHolder.addCallback(this);
		mCamera.setPreviewCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

		// Taille d'image preview
		configureCamera(parametres);

	}

	private void configureCamera(@Nullable ParametresCamera parametres ) {
		Camera.Parameters cameraParams = mCamera.getParameters();
		_exposureMin = cameraParams.getMinExposureCompensation();
		_exposureMax = cameraParams.getMaxExposureCompensation();
		_exposure = cameraParams.getExposureCompensation();
		configureCameraParameters(cameraParams, true);
		//cameraParams.setAutoExposureLock(true);

		if ( parametres == null)
		{
			cameraParams.setExposureCompensation(cameraParams.getMinExposureCompensation());
		}
		else
		{
			cameraParams.setExposureCompensation(parametres.niveauExposition);
		}
		mCamera.setParameters(cameraParams);
	}

	protected void configureCameraParameters(Camera.Parameters cameraParams, boolean portrait) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
			if (portrait) {
				cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
			}
			else {
				cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
			}
		}
		else { // for 2.2 and later
			int angle;
			Display display = getActivity().getWindowManager().getDefaultDisplay();
			switch (display.getRotation()) {
				case Surface.ROTATION_0: // This is display orientation
					angle = 90; // This is camera orientation
					break;
				case Surface.ROTATION_90:
					angle = 0;
					break;
				case Surface.ROTATION_180:
					angle = 270;
					break;
				case Surface.ROTATION_270:
					angle = 180;
					break;
				default:
					angle = 90;
					break;
			}
			mCamera.setDisplayOrientation(angle);
		}

		Camera.Size size = determinePreviewSize(cameraParams, portrait, getWidth(),getHeight());
		cameraParams.setPreviewSize(size.width, size.height);
		cameraParams.setPictureSize(size.width, size.height);

	}

	/**
	 * @param portrait
	 * @param reqWidth  must be the value of the parameter passed in surfaceChanged
	 * @param reqHeight must be the value of the parameter passed in surfaceChanged
	 * @return Camera.Size object that is an element of the list returned from Camera.Parameters.getSupportedPreviewSizes.
	 */
	protected Camera.Size determinePreviewSize(Camera.Parameters cameraParams, boolean portrait, int reqWidth, int reqHeight) {
		Camera.Size retSize = null;
		long taille = Long.MAX_VALUE;

		List<Camera.Size> PreviewSizeList = cameraParams.getSupportedPreviewSizes();
		for (Camera.Size size : PreviewSizeList) {
			long t = size.width * size.height;
			// Prendre la plus petite taille
			if (t < taille) {
				taille = t;
				retSize = size;
			}
		}

		return retSize;
	}
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		try {
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.e("ERROR", "Camera error on surfaceCreated " + e.getMessage());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		//before changing the application orientation, you need to stop the preview, rotate and then start it again
		if (mHolder.getSurface() == null)//check if the surface is ready to receive camera data
		{
			return;
		}

		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			//this will happen when you are trying the camera if it's not running
		}

		//now, recreate the camera preview
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.setPreviewCallback(null);
		mHolder.removeCallback(this);
		mCamera.release();
		mCamera = null;
	}


	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setExposureCompensation(_exposure);
		mCamera.setParameters(parameters);

		int width = parameters.getPreviewSize().width;
		int height = parameters.getPreviewSize().height;

		YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		yuv.compressToJpeg(new Rect(0, 0, width, height), JPEG_QUALITY, out);

		byte[] bytes = out.toByteArray();
		final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

		if  (_listener!=null)
		(getActivity()).runOnUiThread(new Runnable() {
			@Override
			public void run() {

				_listener.onNewPreviewFrame(bitmap);
			}
		});
	}

	/***
	 * Retrouve l'Activity qui englobe cette view
	 * @return
	 */
	private Activity getActivity() {
		Activity result = null;
		Context context = getContext();

		while (context instanceof ContextWrapper)
		{
			if (context instanceof Activity) {
				result = (Activity) context;
				break;
			}
			context = ((ContextWrapper)context).getBaseContext();
		}
		return result;
	}
}