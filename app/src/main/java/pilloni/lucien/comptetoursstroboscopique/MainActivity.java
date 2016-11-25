package pilloni.lucien.comptetoursstroboscopique;

import android.app.KeyguardManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

import pilloni.lucien.comptetoursstroboscopique.Camera.ActivityWithCameraPreview;
import pilloni.lucien.comptetoursstroboscopique.Camera.CameraPreview;
import pilloni.lucien.comptetoursstroboscopique.Graphiques.AffichageVitesseView;
import pilloni.lucien.comptetoursstroboscopique.Graphiques.NiveauSignalView;
import pilloni.lucien.comptetoursstroboscopique.Graphiques.PreferencesUtils;

public class MainActivity extends ActivityWithCameraPreview implements CameraPreview.PrewiewFrameListener {
	ViewFlipper _viewFlipper;
	NiveauSignalView _niveauSignal;
	private float lastX;
	SignalManager _signalManager;
	FloatingActionButton _floatingActionButton;

	enum STATUS {
		PAS_DE_MESURE, MESURE_EN_COURS
	}

	STATUS _status = STATUS.PAS_DE_MESURE;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		_floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
		_floatingActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				switch (_status)
				{
					case MESURE_EN_COURS:
						StopMesure();
						Toast.makeText(MainActivity.this, "Arrêt de la mesure", Toast.LENGTH_SHORT).show();
						break;

					case PAS_DE_MESURE:
						StartMesure();
						Toast.makeText(MainActivity.this, "Démarrage de la mesure", Toast.LENGTH_SHORT).show();
						break;

				}
			}
		});

		_viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		_niveauSignal = (NiveauSignalView) findViewById(R.id.niveauSignalView);
		_signalManager = new SignalManager(SignalManager.TYPE_SEUIL.SEUIL_HAUT, 50, 100, 0.75f);

		_status = STATUS.PAS_DE_MESURE;
		creerCamera(R.id.buttonCameraPreviewReplace);
	}


	@Override
	protected void onPause()
	{
		StopMesure();
		super.onPause();
	}

	private void StartMesure()
	{
		_status = STATUS.MESURE_EN_COURS;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			_floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_circle_white_48dp, this.getTheme()));
		}
		else
		{
			_floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_circle_white_48dp));
		}

		_signalManager.reset();
		resetGraphiques();

		// Inhibe le verouillage automatique
		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
		lock.disableKeyguard();
	}

	/***
	 * Demande aux affichages graphiques de reinitialiser leurs valeurs
	 */
	private void resetGraphiques()
	{
		int nb = _viewFlipper.getChildCount();
		for (int i = 0; i < nb; i++)
		{
			View v = _viewFlipper.getChildAt(i);
			if (v instanceof AffichageVitesseView)
				((AffichageVitesseView) v).resetValues();
		}
	}

	private void StopMesure()
	{
		_status = STATUS.PAS_DE_MESURE;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			_floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_white_48dp, this.getTheme()));
		}
		else
		{
			_floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_white_48dp));
		}

		// Inhibe le verouillage automatique
		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
		lock.reenableKeyguard();
	}


	/***
	 * Reception d'une nouvelle valeur de luminosite
	 *
	 * @param b
	 */
	@Override
	public void onNewPreviewFrame(Bitmap b)
	{
		float luminosite = getLuminosite(b);
		_niveauSignal.addValue(luminosite);

		if (_status == STATUS.MESURE_EN_COURS)
		{
			switch (_signalManager.nouveauPic(luminosite))
			{
				case NOUVEAU_PIC:
				{
					beep();
					float frequence = _signalManager.frequenceToursMinute();
					int nb = _viewFlipper.getChildCount();
					for (int i = 0; i < nb; i++)
					{
						View v = _viewFlipper.getChildAt(i);
						if (v instanceof AffichageVitesseView)
						{
							((AffichageVitesseView) v).setTitle("Tours minute");
							((AffichageVitesseView) v).addValue(frequence);
						}
					}

					break;
				}

				default:
					// Rien
					break;
			}
		}
	}

	private void beep()
	{
		ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 60);
		toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
	}

	/***
	 * Calcule la luminosite moyenne d'une bitmap
	 *
	 * @param bitmap
	 * @return 0(tout noir)..1(tout blanc)
	 */
	private static float getLuminosite(Bitmap bitmap)
	{
		// Laisser le systeme nous fabriquer une bitmap d'un seul pixel
		bitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, false);
		int R = 0;
		int G = 0;
		int B = 0;
		int height = 1;//bitmap.getHeight();
		int width = 1;//bitmap.getWidth();
		int n = 0;
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int color : pixels)
		{
			R += Color.red(color);
			G += Color.green(color);
			B += Color.blue(color);
			n++;
		}
		return (R + B + G) / (n * 3.0f * 255.0f);
	}

	// Using the following method, we will handle all screen swaps.
	public boolean onTouchEvent(MotionEvent touchevent)
	{
		switch (touchevent.getAction())
		{

			case MotionEvent.ACTION_DOWN:
				lastX = touchevent.getX();
				break;
			case MotionEvent.ACTION_UP:
				float currentX = touchevent.getX();

				// Handling left to right screen swap.
				if (lastX >= currentX)
				{

					_viewFlipper.setOutAnimation(this, R.anim.slide_out_from_right);
// Next screen comes in from right.
					_viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);
					// Current screen goes out from left.
					_viewFlipper.setOutAnimation(this, R.anim.slide_out_from_left);

					// Display next screen.
					_viewFlipper.showNext();
				}
				else
				// Handling right to left screen swap.
				{
					// Next screen comes in from left.
					_viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);
					// Current screen goes out from right.
					_viewFlipper.setOutAnimation(this, R.anim.slide_out_from_right);

					// Display previous screen.
					_viewFlipper.showPrevious();
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id)
		{
			case R.id.action_exposition_moins:
			{
				int expo = _CameraView.changeExposition(-1);
				PreferencesUtils.saveExposure(this, expo);
				break;
			}

			case R.id.action_exposition_plus:
			{
				int expo = _CameraView.changeExposition(+1);
				PreferencesUtils.saveExposure(this, expo);
				break;
			}
		}

		return super.onOptionsItemSelected(item);
	}

}
