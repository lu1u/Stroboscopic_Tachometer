package pilloni.lucien.comptetoursstroboscopique.Graphiques;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import pilloni.lucien.comptetoursstroboscopique.R;

/**
 * Affichage de la mesure courante sous forme de texte
 */
public class AffichageTextView extends AffichageVitesseView {

	Drawable _fond;
	Paint _paintTitre, _paintValeur, _paintMoyenne, _paintMax, _paintMin;

	float _min = 0;
	float _max = 1;
	float _moyenne = 0.5f;
	float _valeur = 1;
	float _total = 0;
	int _nbValeurs = 0;
	final RectF r = new RectF();
	final Rect rTexte = new Rect();

	int _paddingLeft, _paddingTop, _paddingRight, _paddingBottom;public AffichageTextView(Context context)
	{
		super(context);
		init(null, 0);
	}

	public AffichageTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public AffichageTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	protected void init(AttributeSet attrs, int defStyle)
	{
		resetValues();
		super.init(attrs, defStyle);
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes( attrs, R.styleable.AffichageTextView, defStyle, 0);

		_paintTitre = new Paint();
		_paintTitre.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintTitre.setColor(a.getColor(R.styleable.AffichageTextView_AT_couleurTitre, Color.GRAY));
		_paintTitre.setTextSize(a.getFloat(R.styleable.AffichageTextView_AT_TailleTitre, 30));

		_paintValeur = new Paint();
		_paintValeur.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintValeur.setColor(a.getColor(R.styleable.AffichageTextView_AT_couleurValeur, Color.GRAY));
		_paintValeur.setTextSize(a.getFloat(R.styleable.AffichageTextView_AT_TailleValeur, 30));

		_paintMoyenne = new Paint();
		_paintMoyenne.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintMoyenne.setColor(a.getColor(R.styleable.AffichageTextView_AT_couleurMoyenne, Color.GRAY));
		_paintMoyenne.setTextSize(a.getFloat(R.styleable.AffichageTextView_AT_TailleMoyenne, 30));

		_paintMin = new Paint();
		_paintMin.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintMin.setColor(a.getColor(R.styleable.AffichageTextView_AT_couleurMin, Color.GRAY));
		_paintMin.setTextSize(a.getFloat(R.styleable.AffichageTextView_AT_TailleMin, 30));

		_paintMax = new Paint();
		_paintMax.setFlags(Paint.ANTI_ALIAS_FLAG);
		_paintMax.setColor(a.getColor(R.styleable.AffichageTextView_AT_couleurMax, Color.GRAY));
		_paintMax.setTextSize(a.getFloat(R.styleable.AffichageTextView_AT_TailleMax, 30));


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
		if ( _nbValeurs==0)
		{
			_max = f;
			_min = f;
		}

		_valeur = f;
		if (_min > f)
			_min = f;

		if (_max < f)
			_max = f;

		_nbValeurs++;
		_total += f;
		_moyenne = _total / _nbValeurs;
		invalidate();
	}

	public void resetValues()
	{
		_min = 50;
		_max = 100;
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
			_fond = getBackground();

		if (_fond != null)
			_fond.draw(canvas);

		r.set(getLeft() + _paddingLeft, getTop() + _paddingTop, getRight() - _paddingRight, getBottom() - _paddingBottom);

		// Min a droite
		String texte = format(_min);
		_paintMin.getTextBounds(texte, 0, texte.length(), rTexte);
		canvas.drawText(texte, r.right-rTexte.width(), r.centerY(), _paintMin);

		// Max a gauche
		texte = format(_max);
		_paintMax.getTextBounds(texte, 0, texte.length(), rTexte);
		canvas.drawText(texte, r.left, r.centerY(), _paintMax);

		// Moyenne en bas
		texte = format(_moyenne);
		_paintMoyenne.getTextBounds(texte, 0, texte.length(), rTexte);
		canvas.drawText(texte, r.centerX()-rTexte.width()/2.0f, r.bottom - rTexte.height()/2, _paintMoyenne);

		// Valeur en haut
		 texte = format(_valeur);
		_paintValeur.getTextBounds(texte, 0, texte.length(), rTexte);
		canvas.drawText(texte, r.centerX()-rTexte.width()/2.0f, r.top + rTexte.height(), _paintValeur);

		// Titre au centre
		_paintTitre.getTextBounds(_titre, 0, _titre.length(), rTexte);
		canvas.drawText(_titre, r.centerX()-rTexte.width()/2.0f, r.centerY(), _paintTitre);
	}
}
