package de.polstelle.diffpatch;

import de.polstelle.diffpatch.model.DiffRecord;
import de.polstelle.diffpatch.model.FakeAuditObjekt;
import de.polstelle.diffpatch.model.FakeReference;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import static org.fest.assertions.Assertions.assertThat;

public class JPatchTest
{
	private JDiff mCR = new JDiff();


	private FakeAuditObjekt mOldDst;

	private FakeAuditObjekt mNewDst;

	private DiffRecord lUpdate;

	@Test
	public void testCreateNoOld() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, ParseException {

		JPatch JPatch = new JPatch(mOldDst);
		JPatch.patch(lUpdate);

		assertThat(mOldDst.getKurzName()).isEqualTo(mNewDst.getKurzName());
        assertThat(mOldDst.getStatus()).isEqualTo(mNewDst.getStatus());
        assertThat(mOldDst.getSum()).isEqualTo(mNewDst.getSum());
        assertThat(mOldDst.isTruth()).isEqualTo(mNewDst.isTruth());
        assertThat(mOldDst.isTruth()).isEqualTo(mNewDst.isTruth());
        assertThat(mOldDst.getToday()).isEqualTo(mNewDst.getToday());
        assertThat(mOldDst.getElemente().get(0)).isEqualTo(mNewDst.getElemente().get(0));
        assertThat(mOldDst.getAddress().getPostfach()).isEqualTo(mNewDst.getAddress().getPostfach());
        assertThat(mOldDst.getRefList().get(0).getBla()).isEqualTo(mNewDst.getRefList().get(0).getBla());
        assertThat(mOldDst.getRefList().get(1).getBla()).isEqualTo(mNewDst.getRefList().get(1).getBla());
        assertThat(mOldDst.getRefList().get(2).getBla()).isEqualTo(mNewDst.getRefList().get(2).getBla());

    }

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

        FakeReference oldFr = new FakeReference();
        oldFr.setBla("blaOld");
        mOldDst.setReference(oldFr);


        FakeReference newFr = new FakeReference();
        newFr.setBla("blaNew");
        mNewDst.setReference(newFr);

		lUpdate = mCR.compare(mOldDst, mNewDst);



    }

}
