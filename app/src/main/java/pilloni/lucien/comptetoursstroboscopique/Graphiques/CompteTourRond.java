package pilloni.lucien.comptetoursstroboscopique.Graphiques;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import pilloni.lucien.comptetoursstroboscopique.R;

/**
 * Compteur avec aiguilles
 */
public class CompteTourRond extends AffichageVitesseView {
	float _angleDepart = 0;
	float _angle = 180;

	Drawable _fond;
	float _largeurFond;
	float _ratioTexteAiguille;

	Paint _paintFond, _paintValeur, _paintMoyenne, _paintTexteValeur, _paintTexteMoyenne;

	float _min = 50;
	float _max = 200 ;
	float _moyenne = 0.5f;
	float _valeur = 1;
	float _total = 0;
	int _nbValeurs = 0;

	float _posAiguilleMoyenne = _moyenne;
	float _posAiguilleValeur = _moyenne ;

	int _paddingLeft, _paddingTop, _paddingRight, _paddingBottom;

	final RectF r = new RectF();
	public CompteTourRond(Context context)
	{
		super(context);
		init(null, 0);
	}

	public CompteTourRond(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public CompteTourRond(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	protected void init(AttributeSet attrs, int defStyle)
	{
		super.init(attrs, defStyle);
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.CompteTourRond, defStyle, 0);
		_largeurFond = a.getFloat(R.styleable.CompteTourRond_CTR_largeurFond, 20);
		_ratioTexteAiguille = a.getFloat(R.styleable.CompteTourRond_CTR_deltaTextAiguille, 1.0f);
		_angle = a.getFloat(R.styleable.CompteTourRond_CTR_angle, 180);
		_angleDepart = a.getFloat(R.styleable.CompteTourRond_CTR_angleDepart, 0);

		_paintFond = new Paint();
		_paintFond.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintFond.setStrokeWidth(_largeurFond);
		_paintFond.setStyle(Paint.Style.STROKE);
		_paintFond.setPathEffect(new DashPathEffect(new float[]{5, 10, 15, 20}, 5));
		_paintFond.setStrokeCap(Paint.Cap.SQUARE);
		_paintFond.setColor(a.getColor(R.styleable.CompteTourRond_CTR_couleurFond, Color.GRAY));

		_paintValeur = new Paint();
		_paintValeur.setStrokeWidth(a.getFloat(R.styleable.CompteTourRond_CTR_largeurAiguilleValeur, 20));
		_paintValeur.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintValeur.setStyle(Paint.Style.STROKE);
		_paintValeur.setStrokeCap(Paint.Cap.ROUND);
		_paintValeur.setColor(a.getColor(R.styleable.CompteTourRond_CTR_couleurValeur, Color.WHITE));

		_paintMoyenne = new Paint();
		_paintMoyenne.setStrokeWidth(a.getFloat(R.styleable.CompteTourRond_CTR_largeurAiguilleMoyenne, 20));
		_paintMoyenne.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintMoyenne.setStyle(Paint.Style.STROKE);
		_paintMoyenne.setStrokeCap(Paint.Cap.ROUND);
		_paintMoyenne.setColor(a.getColor(R.styleable.CompteTourRond_CTR_couleurMoyenne, Color.WHITE));

		_paintTexteValeur = new Paint();
		_paintTexteValeur.setColor(a.getColor(R.styleable.CompteTourRond_CTR_couleurValeur, Color.WHITE));
		_paintTexteValeur.setTextSize(a.getInt(R.styleable.CompteTourRond_CTR_tailleTexteAiguille, 20));

		_paintTexteMoyenne = new Paint();
		_paintTexteMoyenne.setColor(a.getColor(R.styleable.CompteTourRond_CTR_couleurMoyenne, Color.WHITE));
		_paintTexteMoyenne.setTextSize(a.getInt(R.styleable.CompteTourRond_CTR_tailleTexteAiguille, 20));

		_paddingLeft = getPaddingLeft();
		_paddingTop = getPaddingTop();
		_paddingRight = getPaddingRight();
		_paddingBottom = getPaddingBottom();

		a.recycle();

		if (isInEditMode())
		{
			_max = 1.0f;
			_min = 0.0f;
			_valeur = 0.75f;
			_moyenne = 0.25f;
		}

		invalidate();
	}

	public void addValue(float f)
	{
		_valeur = f;
		if (_min > f)
		{
			_min = f;
		}

		if (_max < f)
		{
			_max = f;
		}

		if ( _max == _min)
		{
			_max *= 1.1f;
			_min *= 0.9f;
		}
		_nbValeurs++;
		_total += f;
		_moyenne = _total / _nbValeurs;
		invalidate();
	}

	public void resetValues()
	{
		_min = 0;
		_max = 200;
		_total = 0;
		_moyenne = 0;
		_nbValeurs = 0;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (_fond == null)
		{
			_fond = getBackground();
		}
		if (_fond != null)
		{
			_fond.draw(canvas);
		}

		int largeur = getWidth();

		r.set(getLeft() + _paddingLeft, getTop() + _paddingTop, getRight() - _paddingRight, getBottom() - _paddingBottom);

		/*if (r.height() > r.width())
		{
			r.bottom = r.top + r.width();
		}*/
		//canvas.drawRect(r, _paintMoyenne);

		// Le fond du compte tours
		float largeurTrait = largeur * 0.2f;
		r.inset(largeurTrait * 0.5f, largeurTrait * 0.5f);
		//r.offset(-largeurTrait * 0.25f, largeurTrait * 0.25f);

		canvas.drawArc(r, 180 + _angleDepart - 2, _angle + 4, false, _paintFond);

		double angle = (float) (_angleDepart * Math.PI / 180.0f);
		drawTextCentre(canvas, r.centerX() - r.width()* (float)Math.cos(angle) * 0.7f, r.centerY() - r.height() * (float)Math.sin(angle) * 0.7f, format(_min), _paintTexteValeur);
		angle = (float) ((_angleDepart+_angle) * Math.PI / 180.0f);
		drawTextCentre(canvas, r.centerX() - r.width()* (float)Math.cos(angle) * 0.7f, r.centerY() - r.height() * (float)Math.sin(angle) * 0.7f, format(_max), _paintTexteValeur);

		// Moyenne
		dessineAiguille(canvas, _paintMoyenne, _paintTexteMoyenne, r, _posAiguilleMoyenne);

		// Valeur actuelle
		dessineAiguille(canvas, _paintValeur, _paintTexteValeur, r, _posAiguilleValeur);

		bougeAiguilles();
	}

	/***
	 * Bouge les aiguilles pour qu'elles se rapprochent progressivement de la valeur exacte
	 */
	private void bougeAiguilles()
	{
		double seuilPosition = (_max-_min) * 0.01;
		// Aiguille valeur
		if ( Math.abs(_posAiguilleValeur-_valeur) > seuilPosition)
		{
			_posAiguilleValeur += 0.1f * (_valeur-_posAiguilleValeur);
			invalidate();
		}

		// Aiguille moyenne
		if ( Math.abs(_posAiguilleMoyenne-_moyenne) > seuilPosition)
		{
			_posAiguilleMoyenne+= 0.1f * (_moyenne-_posAiguilleMoyenne);
			invalidate();
		}
	}

	private void dessineAiguille(Canvas canvas, Paint paintAiguille, Paint paintTexte, RectF r, float valeur)
	{
		String texte = format(valeur);
		// Mise a l'echelle de la valeur
		valeur = (valeur - _min) / (_max - _min);
		valeur = _angleDepart + (_angle * valeur);

		// Deg -> rad
		float angle = (float) (valeur * Math.PI / 180.0f);

		float l = Math.max(r.width(), r.height()) * 0.5f;

		float x = r.centerX();
		float y = r.centerY();

		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		canvas.drawLine(x, y, x - cos * l, y - sin * l, paintAiguille);

		x -= cos * l * _ratioTexteAiguille;
		y -= sin * l * _ratioTexteAiguille;

		drawTextCentre(canvas, x, y, texte, paintTexte);
	}


	public static void drawTextCentre( Canvas canvas, float x, float y, String texte, Paint paint)
	{
		Rect textBounds = new Rect();
		paint.getTextBounds(texte, 0, texte.length(), textBounds);
		canvas.drawText(texte, x - textBounds.exactCenterX(), y - textBounds.exactCenterY(), paint);
		canvas.restore();
	}
}
