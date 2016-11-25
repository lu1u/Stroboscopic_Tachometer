package pilloni.lucien.comptetoursstroboscopique.Graphiques;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Random;

import pilloni.lucien.comptetoursstroboscopique.R;

/**
 * Affichage linechart
 */
public class GraphiqueView extends AffichageVitesseView {
	private static final String STATE_VALEURS = "lpi.CompteTours.GraphiqueView.valeurs";
	private static final String STATE_MIN = "lpi.CompteTours.GraphiqueView.min";
	private static final String STATE_MAX = "lpi.CompteTours.GraphiqueView.max";
	private static final String STATE_TOTAL = "lpi.CompteTours.GraphiqueView.total";
	private static final String STATE_MOYENNE = "lpi.CompteTours.GraphiqueView.moyenne";

	ArrayList<Float> _valeurs = new ArrayList<>();
	float _min = Float.MAX_VALUE;
	float _max = Float.MIN_NORMAL;
	float _total = 0;
	float _moyenne = 0;
	private float echelleY;
	Paint _paintMoyenne, _paintMin, _paintMax, _paintTrait, _paintTexte;
	int _couleurHaut, _couleurBas, _couleurMoyen, _couleurMoyenne, _couleurAxes;
	int _paddingLeft, _paddingTop, _paddingRight, _paddingBottom;
	final RectF r = new RectF();

	public void restoreState(Bundle savedInstanceState)
	{
		_valeurs = toList(savedInstanceState.getFloatArray(STATE_VALEURS));
		_min = savedInstanceState.getFloat(STATE_MIN);
		_max = savedInstanceState.getFloat(STATE_MAX);
		_total = savedInstanceState.getFloat(STATE_TOTAL);
		_moyenne = savedInstanceState.getFloat(STATE_MOYENNE);
		invalidate();
	}

	private ArrayList<Float> toList(float[] in)
	{
		ArrayList<Float> result = new ArrayList<Float>(in.length);
		for (float f : in)
			result.add(f);

		return result;
	}

	public void saveState(Bundle outState)
	{
		outState.putFloatArray(STATE_VALEURS, toFloatArray(_valeurs));
		outState.putFloat(STATE_MIN, _min);
		outState.putFloat(STATE_MAX, _max);
		outState.putFloat(STATE_TOTAL, _total);
		outState.putFloat(STATE_MOYENNE, _moyenne);
	}

	private float[] toFloatArray(ArrayList<Float> in)
	{
		float[] result = new float[in.size()];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = in.get(i);
		}
		return result;
	}


	public void addValue(float f)
	{
		_valeurs.add(f);
		if (_min > f)
		{
			_min = f;
		}

		if (_max < f)
		{
			_max = f;
		}

		_total += f;
		_moyenne = _total / _valeurs.size();
		invalidate();
	}

	public void resetValues()
	{
		if (_valeurs == null)
			_valeurs = new ArrayList<>();
		else
			_valeurs.clear();
		_min = Float.MAX_VALUE;
		_max = Float.MIN_NORMAL;
		_total = 0;
		_moyenne = 0;
		invalidate();
	}

	public GraphiqueView(Context context)
	{
		super(context);
		init(null, 0);
	}

	public GraphiqueView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public GraphiqueView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	protected void init(AttributeSet attrs, int defStyle)
	{
		super.init(attrs, defStyle);
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.GraphiqueView, defStyle, 0);
		_couleurBas = a.getColor(R.styleable.GraphiqueView_GV_couleurBas, Color.RED);
		_couleurMoyen = a.getColor(R.styleable.GraphiqueView_GV_couleurMoyen, Color.YELLOW);
		_couleurHaut = a.getColor(R.styleable.GraphiqueView_GV_couleurHaut, Color.GREEN);
		_couleurAxes = a.getColor(R.styleable.GraphiqueView_GV_couleurAxes, Color.GRAY);
		_couleurMoyenne = a.getColor(R.styleable.GraphiqueView_GV_couleurMoyenne, Color.GRAY);

		_paintMoyenne = new Paint();
		_paintMoyenne.setStyle(Paint.Style.STROKE);
		_paintMoyenne.setColor(_couleurMoyenne);
		_paintMoyenne.setFlags(Paint.ANTI_ALIAS_FLAG);

		_paintMin = new Paint();
		_paintMin.setStyle(Paint.Style.STROKE);
		_paintMin.setColor(a.getColor(R.styleable.GraphiqueView_GV_couleurMin, Color.RED));

		_paintMax = new Paint();
		_paintMax.setStyle(Paint.Style.STROKE);
		_paintMax.setColor(a.getColor(R.styleable.GraphiqueView_GV_couleurMax, Color.RED));

		_paintTrait = new Paint();
		_paintTrait.setStyle(Paint.Style.STROKE);
		_paintTrait.setStrokeWidth(a.getFloat(R.styleable.GraphiqueView_GV_largeurTrait, 2.0f));
		_paintTrait.setFlags(Paint.ANTI_ALIAS_FLAG);

		_paintTexte = new Paint();
		_paintTexte.setStyle(Paint.Style.STROKE);
		_paintTexte.setColor(_couleurAxes);
		_paintTexte.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintTexte.setTextSize(a.getFloat(R.styleable.GraphiqueView_GV_tailleTexte, 20.0f));

		_paddingLeft = getPaddingLeft();
		_paddingTop = getPaddingTop();
		_paddingRight = getPaddingRight();
		_paddingBottom = getPaddingBottom();

		a.recycle();

		if (isInEditMode())
			randomValues();
	}

	private void randomValues()
	{
		resetValues();
		_min = -3.0f;
		_max = -3.0f;
		_total = 0;
		_moyenne = 0;
		_valeurs.clear();
		Random r = new Random();
		for (int i = 0; i < 200; i++)
			addValue((float) (Math.sin(i * 0.05) * 2 + r.nextFloat() - 0.5f));
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		ColorDrawable background = (ColorDrawable) getBackground();
		if (background != null)
			background.draw(canvas);

		if (_valeurs.size() > 0)
		{
			r.set(getLeft() + _paddingLeft, getTop() + _paddingTop, getRight() - _paddingRight, getBottom() - _paddingBottom);

			echelleY = r.height() / (_max - _min);
			float echelleX = r.width() / (float) _valeurs.size();

			int debut = 0;
			int nb = _valeurs.size();
			if (nb > r.width())
			{
				debut = (int) (nb - r.width());
				echelleX = 1.0f;
				_valeurs.remove(0);
				nb = _valeurs.size();
			}

			float dx = r.left;
			float dy = calculeY(r, _valeurs.get(debut));

			for (int i = debut + 1; i <nb; i++)
			{
				float v = _valeurs.get(i);
				float y = calculeY(r, v);
				_paintTrait.setColor(calculeCouleur(_valeurs.get(i - 1), v));
				canvas.drawLine(dx, dy, dx + echelleX, y, _paintTrait);
				dx += echelleX;
				dy = y;

			}

			_paintMoyenne.setColor(_couleurMoyenne);
			traceLigne(canvas, r, _moyenne, _paintMoyenne);

			_paintMoyenne.setColor(_couleurAxes);
			traceLigne(canvas, r, (_min + (_max - _min) * 0.25f), _paintMoyenne);
			traceLigne(canvas, r, (_min + (_max - _min) * 0.75f), _paintMoyenne);
			traceLigne(canvas, r, (_min + (_max - _min) * 0.05f), _paintMoyenne);
			traceLigne(canvas, r, (_min + (_max - _min) * 0.95f), _paintMoyenne);
		}
	}

	private void traceLigne(Canvas canvas, RectF r, float valeur, Paint paint)
	{
		float y = calculeY(r, valeur);
		canvas.drawLine(r.left, y, r.width(), y, paint);
		canvas.drawText(String.format(valeur > 1.0 ? "%1.2f" : "%2.1f", valeur), r.left, y, _paintTexte);
	}

	private int calculeCouleur(float dy, float y)
	{
		float milieu = (dy + y) / 2.0f;

		if (milieu > _min + (_max - _min) * 0.666f)
			return _couleurHaut;
		if (milieu < _min + (_max - _min) * 0.333f)
			return _couleurBas;
		return _couleurMoyen;
	}

	private float calculeY(RectF r, float y)
	{
		return r.top + ((r.height() * 0.5f) - (y - (_min + (_max - _min) * 0.5f)) * echelleY);
	}


}
