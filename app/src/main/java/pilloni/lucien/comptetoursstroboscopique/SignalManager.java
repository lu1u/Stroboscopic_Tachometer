package pilloni.lucien.comptetoursstroboscopique;

import java.util.ArrayList;

/**
 * Created by lucien on 21/11/2016.
 */

public class SignalManager {

	public enum TYPE_SEUIL {SEUIL_HAUT, SEUIL_BAS}

	public enum RESULT {CALIBRAGE, OK, NOUVEAU_PIC}

	final TYPE_SEUIL _typeSeuil;
	final int _nbMinValeurs, _nbMaxValeurs;
	float _total = 0;
	float _min = Float.MAX_VALUE;
	float _max = Float.MIN_VALUE;
	float _moyenne;
	final float _ratioSeuil;
	long _dernierPic = 0;
	float _frequence;
	final ArrayList<Float> _valeurs = new ArrayList<Float>();

	boolean _etaitDessousSeuil = false;

	public SignalManager(TYPE_SEUIL type, int nbMinValeurs, int nbMaxValeurs, float seuil)
	{
		_typeSeuil = type;
		_nbMinValeurs = nbMinValeurs;
		_nbMaxValeurs = nbMaxValeurs;
		_ratioSeuil = seuil;
	}

	public void reset()
	{
		_total = 0;
		_min = Float.MAX_VALUE;
		_max = Float.MIN_VALUE;
		_moyenne = 0;
		_dernierPic = 0;
		_valeurs.clear();
	}

	/***
	 * detecte si la nouvelle valeur represente un nouveau pic
	 *
	 * @param valeur
	 * @return
	 */
	public RESULT nouveauPic(float valeur)
	{
		_valeurs.add(valeur);
		_total += valeur;
		int nbValeurs = _valeurs.size();

		if (nbValeurs < _nbMinValeurs)
			return RESULT.CALIBRAGE;

		if (nbValeurs > _nbMaxValeurs)
		{
			_total -= _valeurs.get(0);
			_valeurs.remove(0);
			nbValeurs = _valeurs.size();
		}

		_moyenne = _total / (float) nbValeurs;

		if (_min > valeur)
			_min = valeur;

		if (_max < valeur)
			_max = valeur;


		RESULT result = _typeSeuil == TYPE_SEUIL.SEUIL_HAUT ?  nouveauPicHaut(valeur):nouveauPicBas(valeur);

		if (result != RESULT.NOUVEAU_PIC)
			return result;

		if (_dernierPic == 0)
		{
			// Premier pic: on ne peut pas encore calculer de frequence
			_dernierPic = System.currentTimeMillis();
			return RESULT.OK;
		}

		long maintenant = System.currentTimeMillis();
		_frequence = 1000.0f / (maintenant - _dernierPic) ;
		_dernierPic = maintenant;
		return RESULT.NOUVEAU_PIC;
	}

	private RESULT nouveauPicBas(float valeur)
	{
		RESULT result = RESULT.OK;
		// Est-ce qu'on etait en dessus du seuil bas ?
		float seuil = _moyenne + (_max - _moyenne) * _ratioSeuil;

		if (_etaitDessousSeuil)
		{
			if (valeur < seuil)
			{
				// On etait en dessos on passe au dessous
				result = RESULT.NOUVEAU_PIC;
				_etaitDessousSeuil = false;
			}
		}
		else if (valeur > seuil)
			_etaitDessousSeuil = true;

		return result;
	}

	private RESULT nouveauPicHaut(float valeur)
	{
		RESULT result = RESULT.OK;
		float seuil = _moyenne + (_min - _moyenne) * _ratioSeuil;

		// Est-ce qu'on etait en dessous du seuil haut ?
		if (_etaitDessousSeuil)
		{
			if (valeur > seuil)
			{
				// On etait en dessous on passe au dessus
				result = RESULT.NOUVEAU_PIC;
				_etaitDessousSeuil = false;
			}
		}
		else if (valeur < seuil)
			_etaitDessousSeuil = true;

		return result;
	}

	/***
	 * Retourne la frequenceHertz en hertz
	 *
	 * @return
	 */
	public float frequenceHertz()
	{
		return _frequence;
	}

	public float frequenceToursMinute()
	{
		return _frequence * 60.0f;
	}

}
