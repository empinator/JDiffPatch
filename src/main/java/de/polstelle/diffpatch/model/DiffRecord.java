package de.polstelle.diffpatch.model;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiffRecord {

    private Date timestamp;

	private String refBaseId;

	private String editor;

	private List<DiffDetail> details = new ArrayList<DiffDetail>();

	private DiffType type;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<DiffDetail> getDetails() {
		return details;
	}

	public void setDetails(List<DiffDetail> pDetails) {
		details = pDetails;
	}

	public void setType(DiffType pType) {
		type = pType;
	}

	public DiffType getType() {
		return type;
	}

    public String getRefBaseId() {
        return refBaseId;
    }

    public void setRefBaseId(String refId) {
        this.refBaseId = refId;
    }

    public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public boolean isNotEmpty() {
		return CollectionUtils.isNotEmpty(getDetails());
	}
}
