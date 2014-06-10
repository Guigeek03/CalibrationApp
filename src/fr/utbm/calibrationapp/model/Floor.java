package fr.utbm.calibrationapp.model;

public class Floor {
	private Integer m_id;
	private String m_name;
	private Integer m_nbPoints = 0;
	
	public Floor() {
		m_id = 0;
		m_name = "";
		m_nbPoints = 0;
	}

	public Floor(Integer id, String name, Integer nbPoints) {
		m_id = id;
		m_name = name;
		m_nbPoints = nbPoints;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	
	public Integer getId() {
		return m_id;
	}

	public void setId(Integer id) {
		m_id = id;
	}

	public Integer getNbPoints() {
		return m_nbPoints;
	}

	public void setNbPoints(Integer m_nbPoints) {
		this.m_nbPoints = m_nbPoints;
	}
	
	
}
