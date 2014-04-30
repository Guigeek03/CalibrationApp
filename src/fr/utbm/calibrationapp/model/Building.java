package fr.utbm.calibrationapp.model;

public class Building {
	private String m_name;
	private int m_nbFloors;

	public Building() {
		m_name = "";
		m_nbFloors = 0;
	}

	public Building(String name, int nbFloors) {
		m_name = name;
		m_nbFloors = nbFloors;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public int getNbFloors() {
		return m_nbFloors;
	}

	public void setNbFloors(int nbFloors) {
		this.m_nbFloors = nbFloors;
	}
}
