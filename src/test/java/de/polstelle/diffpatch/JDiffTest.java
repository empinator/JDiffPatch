package de.polstelle.diffpatch;

import de.polstelle.diffpatch.model.*;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class JDiffTest
{

	private JDiff mCR = new JDiff();

	private FakeAuditObjekt mOldDst;

	private FakeAuditObjekt mNewDst;

    @Before
    public void setUp() throws ParseException {
		mOldDst = new FakeAuditObjekt();
		mOldDst.setKurzName("alt");
		mOldDst.setStatus("Status");
        mOldDst.setIndex(1);
        mOldDst.setSum(4);
        mOldDst.setTruth(true);
        mOldDst.getElemente().add("old");
        mOldDst.setToday(DateUtils.parseDate("01.05.1983 05:45", JDiff.DATE_PATTERN));
		mOldDst.getAddress().setPostfach("pfAlt");
        mOldDst.getRefList().add(new FakeReference());
        mOldDst.getRefList().get(0).setBla("old");
        mOldDst.getRefList().add(new FakeReference());
        mOldDst.getRefList().get(1).setBla("old");
        mOldDst.getRefList().add(new FakeReference());
        mOldDst.getRefList().get(2).setBla("old");
        mOldDst.getRefList().add(new FakeReference());
        mOldDst.getRefList().get(3).setBla("alone");

		mNewDst = new FakeAuditObjekt();
		mNewDst.setKurzName("neu");
		mNewDst.setStatus("neuStatus");
        mNewDst.setIndex(2);
        mNewDst.setSum(23);
        mNewDst.setTruth(false);
        mNewDst.setToday(DateUtils.addDays(mOldDst.getToday(), 1));
        mNewDst.getElemente().add("new");
		mNewDst.getAddress().setPostfach("pfNeu");
        mNewDst.getRefList().add(new FakeReference());
        mNewDst.getRefList().get(0).setBla("new");
        mNewDst.getRefList().add(new FakeReference());
        mNewDst.getRefList().get(1).setBla("old");
        mNewDst.getRefList().add(new FakeReference());
        mNewDst.getRefList().get(2).setBla("new");
	}

    @Test
    public void testCreateNoOld()
	{
		DiffRecord lRecord = mCR.compare(null, mNewDst);

		assertThat(DiffType.CREATED).isEqualTo(lRecord.getType());
        assertThat(lRecord.getDetails().size()).isEqualTo(11);
	}

	@Test
	public void testCreateNoNew()
	{
		DiffRecord lRecord = mCR.compare(mOldDst, null);

		assertThat(DiffType.DELETED).isEqualTo(lRecord.getType());
		assertThat(lRecord.getDetails().size()).isEqualTo(12);
	}

	@Test
	public void testNoChange()
	{
		DiffRecord lRecord = mCR.compare(mNewDst, mNewDst);
		assertThat(DiffType.UNCHANGED).isEqualTo(lRecord.getType());
		assertThat(0).isEqualTo(lRecord.getDetails().size());
	}

	@Test
	public void testUpdate()
	{
		List<String> lChanges = new ArrayList<String>();

        FakeReference oldFr = new FakeReference();
        oldFr.setBla("blaOld");
        mOldDst.setReference(oldFr);


        FakeReference newFr = new FakeReference();
        newFr.setBla("blaNew");
        mNewDst.setReference(newFr);

		DiffRecord lUpdate = mCR.compare(mOldDst, mNewDst);

		DiffDetail lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeAuditObjekt");
		lDetail.setProperty("kurzName");
        lDetail.setPropertyClazzType(String.class.getName());
		lDetail.setNewValue(mNewDst.getKurzName());
		lDetail.setOldValue(mOldDst.getKurzName());
		lChanges.add(lDetail.toString());

		lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeAuditObjekt");
		lDetail.setProperty("status");
        lDetail.setPropertyClazzType(String.class.getName());
        lDetail.setNewValue(mNewDst.getStatus());
		lDetail.setOldValue(mOldDst.getStatus());
		lChanges.add(lDetail.toString());

        lDetail = new DiffDetail();
        lDetail.setParentClazzType("FakeAuditObjekt");
        lDetail.setProperty("today");
        lDetail.setPropertyClazzType(Date.class.getName());
        lDetail.setNewValue("02.05.1983 05:45");
        lDetail.setOldValue("01.05.1983 05:45");
        lChanges.add(lDetail.toString());

		lDetail = new DiffDetail();
		lDetail.setParentClazzType("Address");
		lDetail.setProperty("address.postfach");
        lDetail.setPropertyClazzType(String.class.getName());
        lDetail.setNewValue(mNewDst.getAddress().getPostfach());
		lDetail.setOldValue(mOldDst.getAddress().getPostfach());
		lChanges.add(lDetail.toString());

        lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeAuditObjekt");
		lDetail.setProperty("index");
        lDetail.setPropertyClazzType(int.class.getName());
        lDetail.setNewValue("" + mNewDst.getIndex());
        lDetail.setOldValue("" + mOldDst.getIndex());
        lChanges.add(lDetail.toString());

        lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeAuditObjekt");
		lDetail.setProperty("sum");
        lDetail.setPropertyClazzType(Integer.class.getName());
        lDetail.setNewValue(""+ mNewDst.getSum());
        lDetail.setOldValue(""+ mOldDst.getSum());
        lChanges.add(lDetail.toString());

        lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeAuditObjekt");
		lDetail.setProperty("truth");
        lDetail.setPropertyClazzType(boolean.class.getName());
        lDetail.setNewValue(mNewDst.isTruth() ? "true" : "false");
        lDetail.setOldValue(mOldDst.isTruth() ? "true" : "false");
        lChanges.add(lDetail.toString());

        lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeReference");
		lDetail.setProperty("reference.bla");
        lDetail.setPropertyClazzType(String.class.getName());
        lDetail.setNewValue(mNewDst.getReference().getBla());
        lDetail.setOldValue(mOldDst.getReference().getBla());
        lChanges.add(lDetail.toString());

        lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeAuditObjekt");
		lDetail.setProperty("elemente");
		lDetail.setListEntry(true);
		lDetail.setListIndex(0);
        lDetail.setPropertyClazzType(String.class.getName());
        lDetail.setNewValue(mNewDst.getElemente().get(0));
        lDetail.setOldValue(mOldDst.getElemente().get(0));
		lChanges.add(lDetail.toString());


        lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeReference");
		lDetail.setProperty("refList[0].bla");
        lDetail.setPropertyClazzType(String.class.getName());
        lDetail.setNewValue(mNewDst.getRefList().get(0).getBla());
        lDetail.setOldValue(mOldDst.getRefList().get(0).getBla());
		lChanges.add(lDetail.toString());

        lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeReference");
		lDetail.setProperty("refList[2].bla");
        lDetail.setPropertyClazzType(String.class.getName());
        lDetail.setNewValue(mNewDst.getRefList().get(2).getBla());
        lDetail.setOldValue(mOldDst.getRefList().get(2).getBla());
		lChanges.add(lDetail.toString());

		lDetail = new DiffDetail();
		lDetail.setParentClazzType("FakeReference");
		lDetail.setProperty("refList[3].bla");
        lDetail.setPropertyClazzType(String.class.getName());
        lDetail.setNewValue("");
		lDetail.setOldValue(mOldDst.getRefList().get(3).getBla());
		lChanges.add(lDetail.toString());

        List<String> lUpdates = new ArrayList<>();
		for (DiffDetail lD : lUpdate.getDetails()) {
            lUpdates.add(lD.toString());
		}
        for(String expected : lChanges) {
            assertThat(lUpdates).contains(expected);
        }


        assertThat(lUpdate.getDetails().size() ).isEqualTo( lChanges.size() );
        assertThat(lUpdate.getType()).isEqualTo(DiffType.UPDATED);

    }

}
