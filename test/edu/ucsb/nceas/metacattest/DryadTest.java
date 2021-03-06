/**
 *  '$RCSfile$'
 *  Copyright: 2004 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *  Purpose: To test the Access Controls in metacat by JUnit
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ucsb.nceas.metacattest;

import java.io.InputStream;
import java.math.BigInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.io.IOUtils;
import org.dataone.service.exceptions.ServiceFailure;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.Session;
import org.dataone.service.types.v2.SystemMetadata;

import edu.ucsb.nceas.metacat.dataone.D1NodeServiceTest;
import edu.ucsb.nceas.metacat.dataone.MNodeService;

/**
 * A JUnit test for testing Dryad documents
 */
public class DryadTest
    extends D1NodeServiceTest {
    
    private static final String DRYAD_TEST_DOC = "test/dryad-metadata-profile-sample.xml";
    
    private static final String DRYAD_TEST_DOC_INVALID = "test/dryad-metadata-profile-invalid.xml";

    /**
     * Constructor to build the test
     *
     * @param name the name of the test method
     */
    public DryadTest(String name) {
        super(name);
    }

    /**
     * Create a suite of tests to be run together
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new DryadTest("initialize"));
        // Test basic functions
        suite.addTest(new DryadTest("d1InsertDoc"));
        suite.addTest(new DryadTest("d1InsertInvalidDoc"));

        return suite;
    }

    /**
     * Run an initial test that always passes to check that the test
     * harness is working.
     */
    public void initialize() {
        assertTrue(1 == 1);
    }
    
    /**
     * Insert test doc using D1 API
     */
    public void d1InsertDoc() {
		try {
	    	String docid = this.generateDocumentId();
			String documentContents = this.getTestDocFromFile(DRYAD_TEST_DOC);
			Session session = getTestSession();
			Identifier pid = new Identifier();
			pid.setValue(docid);
			SystemMetadata sysmeta = this.createSystemMetadata(pid, session.getSubject(), IOUtils.toInputStream(documentContents, "UTF-8"));
			ObjectFormatIdentifier formatId = new ObjectFormatIdentifier();
			formatId.setValue("http://datadryad.org/profile/v3.1");
			sysmeta.setFormatId(formatId);
			sysmeta.setSize(BigInteger.valueOf(IOUtils.toByteArray(documentContents).length));
			MNodeService.getInstance(request).create(session, pid, IOUtils.toInputStream(documentContents, "UTF-8"), sysmeta);
			InputStream results = MNodeService.getInstance(request).get(session, pid);
			String resultString = IOUtils.toString(results);
			assertEquals(documentContents, resultString);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    	
    }
    
    /**
     * Insert test doc using D1 API
     */
    public void d1InsertInvalidDoc() {
		try {
	    	String docid = this.generateDocumentId();
			String documentContents = this.getTestDocFromFile(DRYAD_TEST_DOC_INVALID);
			Session session = getTestSession();
			Identifier pid = new Identifier();
			pid.setValue(docid);
			SystemMetadata sysmeta = this.createSystemMetadata(pid, session.getSubject(), IOUtils.toInputStream(documentContents, "UTF-8"));
			ObjectFormatIdentifier formatId = new ObjectFormatIdentifier();
			formatId.setValue("http://datadryad.org/profile/v3.1");
			sysmeta.setFormatId(formatId);
			sysmeta.setSize(BigInteger.valueOf(IOUtils.toByteArray(documentContents).length));
			try {
				MNodeService.getInstance(request).create(session, pid, IOUtils.toInputStream(documentContents, "UTF-8"), sysmeta);
			} catch (Exception expectedException) {

				// expected exception
				assertTrue(expectedException instanceof ServiceFailure);
				return;
			}
			fail("Should not allow inserting invalid Dryad content");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    	
    }
    
}
