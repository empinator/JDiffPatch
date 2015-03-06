package de.polstelle.diffpatch.model;

public class DiffDetail {
    private String oldValue;

    private String newValue;

    private String property;

	private String parentClazzType;

	private String propertyClazzType;

	private boolean listEntry = false;

	private int listIndex = 0;

	public DiffDetail() {
	}

    public String getProperty() {
		return property;
	}

	public void setProperty(String pProperty) {
		property = pProperty;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String pOldValue) {
		oldValue = pOldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String pNewValue) {
		newValue = pNewValue;
	}

	public void setParentClazzType(String parentClazzType) {
		this.parentClazzType = parentClazzType;
	}

	public String getParentClazzType() {
		return parentClazzType;
	}

    public String getPropertyClazzType() {
        return propertyClazzType;
    }

    public void setPropertyClazzType(String propertyClazzType) {
        this.propertyClazzType = propertyClazzType;
    }

    public boolean isListEntry() {
		return listEntry;
	}

	public void setListEntry(boolean listEntry) {
		this.listEntry = listEntry;
	}

	public int getListIndex() {
		return listIndex;
	}

	public void setListIndex(int listIndex) {
		this.listIndex = listIndex;
	}

    @Override
    public String toString() {
        return String.format("%s%s: %s -> %s | %s@%s", getProperty(), ( listEntry ? "["+listIndex+"]" : "" ) ,getOldValue(), getNewValue(), getPropertyClazzType(), getParentClazzType());
    }

}
