package fr.utbm.calibrationapp.model;

import android.graphics.PointF;

public class Floor {
	private String m_name;
	private int m_nbPoints;
	private PointF[] m_points = null;
	
	public Floor() {
		m_name = "";
		m_nbPoints = 0;
		m_points = null;
	}

	public Floor(String name, PointF[] points) {
		m_name = name;
		m_nbPoints = points.length;
		m_points = new PointF[m_nbPoints];
		m_points = points;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public int getNbPoints() {
		return m_nbPoints;
	}

	public PointF[] getPoints() {
		return m_points;
	}

	public void setPoints(PointF[] points) {
		m_nbPoints = points.length;
		m_points = points;
	}
	
	
	
	
}
