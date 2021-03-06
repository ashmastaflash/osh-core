/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.test.persistence.perst;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.sensorhub.impl.persistence.perst.BasicStorageConfig;
import org.sensorhub.impl.persistence.perst.MultiEntityStorageImpl;
import org.sensorhub.test.persistence.AbstractTestMultiObsStorage;


public class TestPerstMultiStorage extends AbstractTestMultiObsStorage<MultiEntityStorageImpl>
{
    File dbFile;
    
    
    @Before
    public void init() throws Exception
    {
        BasicStorageConfig config = new BasicStorageConfig();
        config.autoStart = true;
        config.memoryCacheSize = 20;
        dbFile = File.createTempFile("testdb", ".dat");
        dbFile.deleteOnExit();
        config.storagePath = dbFile.getAbsolutePath();
        
        storage = new MultiEntityStorageImpl();
        storage.init(config);
        storage.start();
    }
    

    @Override
    protected void forceReadBackFromStorage() throws Exception
    {
        storage.stop();
        storage.start();
    }
    
    
    @After
    public void cleanup() throws Exception
    {
        storage.stop();
        System.out.println("DB file size was " + dbFile.length()/1024 + "KB");
        dbFile.delete();
    }
    
}
