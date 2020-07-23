package org.hisp.dhis.reservedvalue.hibernate;

/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.IntegrationTestBase;
import org.hisp.dhis.reservedvalue.SequentialNumberCounterStore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HibernateSequentialNumberCounterStoreTest
    extends IntegrationTestBase
{
    @Autowired
    private SequentialNumberCounterStore store;

    @Test
    public void getNextValues()
    {

        List<Integer> result = store.getNextValues( "ABC", "ABC-#", 3 );

        assertEquals( 3, result.size() );
        assertTrue( result.contains( 1 ) );
        assertTrue( result.contains( 2 ) );
        assertTrue( result.contains( 3 ) );

        result = store.getNextValues( "ABC", "ABC-#", 50 );

        assertEquals( 50, result.size() );
        assertTrue( result.contains( 4 ) );
        assertTrue( result.contains( 5 ) );
        assertTrue( result.contains( 52 ) );
        assertTrue( result.contains( 53 ) );

    }

    @Test
    public void deleteCounter()
    {
        assertTrue( store.getNextValues( "ABC", "ABC-#", 3 ).contains( 1 ) );

        store.deleteCounter( "ABC" );

        assertTrue( store.getNextValues( "ABC", "ABC-#", 3 ).contains( 1 ) );
        assertTrue( store.getNextValues( "ABC", "ABC-##", 3 ).contains( 1 ) );
        assertTrue( store.getNextValues( "ABC", "ABC-###", 3 ).contains( 1 ) );

        store.deleteCounter( "ABC" );

        assertTrue( store.getNextValues( "ABC", "ABC-#", 3 ).contains( 1 ) );
        assertTrue( store.getNextValues( "ABC", "ABC-##", 3 ).contains( 1 ) );
        assertTrue( store.getNextValues( "ABC", "ABC-###", 3 ).contains( 1 ) );
    }

    @Override
    public boolean emptyDatabaseAfterTest()
    {
        return true;
    }
}