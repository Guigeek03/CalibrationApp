package fr.utbm.calibrationapp.model;

public class Building {
	private Integer m_id;
	private String m_name;
	private Integer m_nbFloors;

	public Building() {
		m_id = 0;
		m_name = "";
		m_nbFloors = 0;
	}

	public Building(Integer id, String name, Integer nbFloors) {
		m_id = id;
		m_name = name;
		m_nbFloors = nbFloors;
	}
	
	
	public Integer getId() {
		return m_id;
	}

	public void setId(Integer id) {
		this.m_id = id;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public Integer getNbFloors() {
		return m_nbFloors;
	}

	public void setNbFloors(Integer nbFloors) {
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
