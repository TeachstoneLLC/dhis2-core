package org.hisp.dhis.tracker.bundle;

/*
 * Copyright (c) 2004-2020, University of Oslo
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

import org.hisp.dhis.IntegrationTestBase;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundle;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleMode;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleParams;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleService;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundleValidationService;
import org.hisp.dhis.dxf2.metadata.objectbundle.feedback.ObjectBundleValidationReport;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceStore;
import org.hisp.dhis.render.RenderFormat;
import org.hisp.dhis.render.RenderService;
import org.hisp.dhis.tracker.TrackerImportStrategy;
import org.hisp.dhis.user.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class TrackerEventBundleServiceTest
    extends IntegrationTestBase
{
    @Autowired
    private ObjectBundleService objectBundleService;

    @Autowired
    private ObjectBundleValidationService objectBundleValidationService;

    @Autowired
    private RenderService _renderService;

    @Autowired
    private UserService _userService;

    @Autowired
    private TrackerBundleService trackerBundleService;

    @Autowired
    private ProgramStageInstanceStore programStageInstanceStore;

    @Override
    protected void setUpTest() throws IOException
    {
        renderService = _renderService;
        userService = _userService;

        Map<Class<? extends IdentifiableObject>, List<IdentifiableObject>> metadata = renderService.fromMetadata(
            new ClassPathResource( "tracker/event_metadata.json" ).getInputStream(), RenderFormat.JSON );

        ObjectBundleParams params = new ObjectBundleParams();
        params.setObjectBundleMode( ObjectBundleMode.COMMIT );
        params.setImportStrategy( ImportStrategy.CREATE );
        params.setObjects( metadata );

        ObjectBundle bundle = objectBundleService.create( params );
        ObjectBundleValidationReport validationReport = objectBundleValidationService.validate( bundle );
        assertTrue( validationReport.getErrorReports().isEmpty() );

        objectBundleService.commit( bundle );
    }

    @Override
    public boolean emptyDatabaseAfterTest()
    {
        return true;
    }

    @Test
    public void testCreateSingleEventData() throws IOException
    {
        TrackerBundle trackerBundle = renderService.fromJson( new ClassPathResource( "tracker/event_events.json" ).getInputStream(),
            TrackerBundleParams.class ).toTrackerBundle();

        assertEquals( 8, trackerBundle.getEvents().size() );

        List<TrackerBundle> trackerBundles = trackerBundleService.create( TrackerBundleParams.builder()
            .events( trackerBundle.getEvents() )
            .build() );

        assertEquals( 1, trackerBundles.size() );

        trackerBundleService.commit( trackerBundles.get( 0 ) );

        List<ProgramStageInstance> programStageInstances = programStageInstanceStore.getAll();
        assertEquals( 8, programStageInstances.size() );
    }

    @Test
    public void testUpdateSingleEventData() throws IOException
    {
        TrackerBundle trackerBundle = renderService.fromJson( new ClassPathResource( "tracker/event_events.json" ).getInputStream(),
            TrackerBundleParams.class ).toTrackerBundle();

        List<TrackerBundle> trackerBundles = trackerBundleService.create( TrackerBundleParams.builder()
            .importStrategy( TrackerImportStrategy.CREATE_AND_UPDATE )
            .events( trackerBundle.getEvents() )
            .build() );

        trackerBundleService.commit( trackerBundles.get( 0 ) );
        assertEquals( 8, programStageInstanceStore.getAll().size() );

        trackerBundles = trackerBundleService.create( TrackerBundleParams.builder()
            .events( trackerBundle.getEvents() )
            .build() );

        trackerBundleService.commit( trackerBundles.get( 0 ) );
        assertEquals( 8, programStageInstanceStore.getAll().size() );
    }
}
