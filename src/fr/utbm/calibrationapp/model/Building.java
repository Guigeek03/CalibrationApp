package fr.utbm.calibrationapp.model;

public class Building {
	private int m_id;
	private String m_name;
	private int m_nbFloors;

	public Building() {
		m_id = 0;
		m_name = "";
		m_nbFloors = 0;
	}

	public Building(int id, String name, int nbFloors) {
		m_id = id;
		m_name = name;
		m_nbFloors = nbFloors;
	}
	
	
	public int getId() {
		return m_id;
	}

	public void setId(int id) {
		this.m_id = id;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Building other = (Building) obj;
		if (m_id != other.m_id)
			return false;
		return true;
	}
}
