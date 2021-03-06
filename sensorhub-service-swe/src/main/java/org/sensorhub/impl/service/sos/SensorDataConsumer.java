/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2015 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sos;

import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import java.io.IOException;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.sensor.ISensorDataInterface;
import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.sensor.swe.SWETransactionalSensor;
import org.sensorhub.impl.service.swe.Template;
import org.sensorhub.impl.service.swe.TransactionUtils;
import org.vast.ogc.om.IObservation;
import org.vast.ows.sos.SOSException;


/**
 * <p>
 * Wrapper data consumer for updating only a virtual sensor
 * </p>
 *
 * @author Alex Robin>
 * @since Feb 13, 2015
 */
public class SensorDataConsumer implements ISOSDataConsumer
{
    SensorConsumerConfig config;
    SWETransactionalSensor sensor;
    
    
    public SensorDataConsumer(SensorConsumerConfig config) throws SensorHubException
    {
        this.config = config;
        ModuleRegistry moduleReg = SensorHub.getInstance().getModuleRegistry();
        this.sensor = (SWETransactionalSensor)moduleReg.getModuleById(config.sensorID);
    }
    
    
    @Override
    public void updateSensor(AbstractProcess newSensorDescription) throws IOException
    {
        TransactionUtils.updateSensorDescription(sensor, newSensorDescription);
    }


    @Override
    public String newResultTemplate(DataComponent component, DataEncoding encoding, IObservation obsTemplate) throws IOException
    {
        try
        {
            // get new/existing sensor output
            String outputName = sensor.newOutput(component, encoding);
            
            // register current foi if needed
            if (obsTemplate != null)
                sensor.newFeatureOfInterest(outputName, obsTemplate.getFeatureOfInterest());
            
            // return template ID
            return generateTemplateID(outputName);
        }
        catch (SensorException e)
        {
            throw new SOSException(SOSException.invalid_param_code, "ResultTemplate", e);
        }        
    }


    @Override
    public void newResultRecord(String templateID, DataBlock... dataBlocks) throws IOException
    {
        try
        {
            String outputName = getOutputNameFromTemplateID(templateID);
            sensor.newResultRecord(outputName, dataBlocks);
        }
        catch (SensorException e)
        {
            throw new IOException("Cannot ingest new data record", e);
        }
    }


    @Override
    public void newObservation(IObservation... observations) throws IOException
    {
        try
        {
            sensor.newObservation(observations);
        }
        catch (SensorException e)
        {
            throw new IOException("Cannot ingest new observation", e);
        }
    }


    @Override
    public Template getTemplate(String templateID) throws IOException
    {
        for (ISensorDataInterface output: sensor.getAllOutputs().values())
        {
            if (templateID.endsWith(output.getName()))
            {
                Template template = new Template();
                template.component = output.getRecordDescription();
                template.encoding = output.getRecommendedEncoding();
                return template;
            }
        }
        
        return null;
    }
    
    
    protected final String generateTemplateID(String outputName)
    {
        return sensor.getLocalID() + '#' + outputName;
    }
    
    
    protected final String getOutputNameFromTemplateID(String templateID)
    {
        return templateID.substring(templateID.lastIndexOf('#')+1);
    }
    
    
    @Override
    public SensorConsumerConfig getConfig()
    {
        return this.config;
    }


    @Override
    public void cleanup()
    {        
    }
}
