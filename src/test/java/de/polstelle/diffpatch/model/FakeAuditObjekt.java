package de.polstelle.diffpatch.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class FakeAuditObjekt
{

	private String kurzName;

	private String status;

    private Integer sum;

    private boolean truth;

    private int index;

	private List<String> elemente = new ArrayList<String>();

	private FakeReference reference = new FakeReference();

	private List<FakeReference> refList = new ArrayList<FakeReference>();

    private Date today = new Date();

	public Address getAddress()
	{
		return address;
	}

	public void setAddress(Address address)
	{
		this.address = address;
	}

    private Address address = new Address();

	public FakeReference getReference()
	{
		return reference;
	}

	public void setReference(FakeReference pReference)
	{
		reference = pReference;
	}


    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public boolean isTruth() {
        return truth;
    }

    public void setTruth(boolean truth) {
        this.truth = truth;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setElemente(List<String> elemente) {
        this.elemente = elemente;
    }

    public List<FakeReference> getRefList()
	{
		return refList;
	}

	public void setRefList(List<FakeReference> pRefList)
	{
		refList = pRefList;
	}

	public String getKurzName()
	{
		return kurzName;
	}

	public void setKurzName(String pKurzName)
	{
		kurzName = pKurzName;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String pStatus)
	{
		status = pStatus;
	}

	public List<String> getElemente()
	{
		return elemente;
	}

    public String getWithoutSet() {
        return "" + new Random().nextInt();
    }

    public Date getToday() {
        return today;
    }

    public void setToday(Date today) {
        this.today = today;
    }
}
