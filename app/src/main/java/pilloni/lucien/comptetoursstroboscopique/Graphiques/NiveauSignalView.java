package pilloni.lucien.comptetoursstroboscopique.Graphiques;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import pilloni.lucien.comptetoursstroboscopique.R;

/**
 * Affichage du niveau de signal
 */
public class NiveauSignalView extends View {
	static final float PAS_GRAPHE = 1.5f;
	ArrayList<Float> _valeurs = new ArrayList<>();
	int _paddingLeft;
	int _paddingTop;
	int _paddingRight;
	int _paddingBottom;

	Paint _paintLigne, _paintFond;
	final RectF r = new RectF();

	public NiveauSignalView(Context context)
	{
		super(context);
		init(null, 0);
	}

	public NiveauSignalView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public NiveauSignalView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}


	protected void init(AttributeSet attrs, int defStyle)
	{
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.NiveauSignalView, defStyle, 0);


		_paddingLeft = getPaddingLeft();
		_paddingTop = getPaddingTop();
		_paddingRight = getPaddingRight();
		_paddingBottom = getPaddingBottom();

		_paintLigne = new Paint();
		_paintLigne.setStyle(Paint.Style.STROKE);
		_paintLigne.setStrokeWidth(4.0f);
		_paintLigne.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintLigne.setAntiAlias(true);
		_paintLigne.setColor(Color.RED);


		_paintFond = new Paint();
		_paintFond.setStyle(Paint.Style.FILL);
		a.recycle();
		if (isInEditMode())
		{
			_valeurs = new ArrayList<>();
			_valeurs.add(0.1f);
			_valeurs.add(0.2f);
			_valeurs.add(0.2f);
			_valeurs.add(0.4f);
			_valeurs.add(0.6f);
			_valeurs.add(0.1f);
		}
	}

	public void addValue(float v)
	{
		if (v < 0)
			v = 0;
		if (v > 1.0f)
			v = 1.0f;
		_valeurs.add(v);
		setBackgroundColor(getColor(v));
		invalidate();
	}


	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		r.set(_paddingLeft, _paddingTop, getWidth() - _paddingRight, getHeight() - _paddingBottom);

		if (_valeurs.size() > 0)
		{
			int largeurMax = (int)(getWidth() / PAS_GRAPHE);
			while (_valeurs.size()> largeurMax)
				_valeurs.remove(0);

			_paintLigne.setColor(_valeurs.get(_valeurs.size()-1) < 0.5f ? Color.WHITE : Color.BLACK );
			float dx = 0;
			float dy = _valeurs.get(0);
			for (int i = 0; i < _valeurs.size(); i++)
			{
				float v = _valeurs.get(i);
				float y = calculeY(r, v);

				canvas.drawLine(dx, dy, dx + PAS_GRAPHE, y, _paintLigne);
				dx+= PAS_GRAPHE;
				dy = y;
			}
		}


	}

	private int getColor(Float valeur)
	{
		int composante = (int) (valeur * 255);
		return Color.rgb(composante, composante, composante);
	}

	private static float calculeY(RectF r, float v)
	{
		return r.bottom - (r.height() * v);
	}


}
