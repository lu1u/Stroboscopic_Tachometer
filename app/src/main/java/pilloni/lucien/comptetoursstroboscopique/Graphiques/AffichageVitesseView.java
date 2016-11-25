package pilloni.lucien.comptetoursstroboscopique.Graphiques;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Classe de base pour les histogrammes
 */
public abstract class AffichageVitesseView extends View {
	protected String _titre = "";

	public AffichageVitesseView(Context context)
	{
		super(context);
		init(null, 0);
	}

	public AffichageVitesseView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public AffichageVitesseView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	protected void init(AttributeSet attrs, int defStyle)
	{
/*		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(
				attrs, R.styleable.AffichageVitesseView, defStyle, 0);


		a.recycle();
	*/
		if (isInEditMode())
			_titre = "Exemple";
	}

	public void setTitle(String s)
	{
		_titre = s;
	}

	public abstract void addValue(float v);

	public abstract void resetValues();

	@SuppressLint("DefaultLocale")
	protected String format(float valeur)
	{
		if (valeur == 0)
			return "0";

		float val = valeur < 0 ? -valeur : valeur;
		if (val < 0.001f)
			return String.format("%1.4f", valeur);
		if (val < 0.01f)
			return String.format("%1.3f", valeur);
		if (val < 0.1f)
			return String.format("%1.2f", valeur);
		if (val < 10)
			return String.format("%1.1f", valeur);
		return String.format("%1.0f", valeur);
	}
}
