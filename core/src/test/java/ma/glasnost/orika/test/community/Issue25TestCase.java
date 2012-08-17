/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.test.community;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.test.community.issue25.BaseManufacturingFacilityTest;
import ma.glasnost.orika.test.community.issue25.CustomOrikaMapper;
import ma.glasnost.orika.test.community.issue25.modelA.Address;
import ma.glasnost.orika.test.community.issue25.modelA.ManufacturingFacility;
import ma.glasnost.orika.test.community.issue25.modelB.AddressDTO;
import ma.glasnost.orika.test.community.issue25.modelB.ManufacturingFacilityDTS;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class Issue25TestCase extends BaseManufacturingFacilityTest{

	private MapperFacade mapper = null; 
	
    public Issue25TestCase() {
    }

    @Before
    public void setUp() throws Exception {
    	System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY,EclipseJdtCompilerStrategy.class.getName());
        
        mapper = new CustomOrikaMapper();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testMapManufacuringFacilityToDTS() {
        Map<String, Object> betriebsstaetteMap =
            getManufacturingFacility(1L,"First");
        
        ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITY_KEY);
        ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITYDTS_KEY);
        
        addAddressToManufacturingFacility(betriebsstaetteMap, 1L, "Beispielstra�e 2", 815L, "This is a comment.", 'D');

        ManufacturingFacilityDTS betriebsstaetteDTSMapped = mapper.map(manufacturingFacility, ManufacturingFacilityDTS.class);
        ManufacturingFacility betriebsstaetteMappedBack = mapper.map(betriebsstaetteDTSMapped, ManufacturingFacility.class);
        assertTrue(manufacturingFacility.equals(betriebsstaetteMappedBack));
    }
    
    @Test
    public void testMapManufacuringFacilityToDTSNullValues() {
        Map<String, Object> betriebsstaetteMap =
                getManufacturingFacility(2L,"Second");
        
        ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITY_KEY);
        ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITYDTS_KEY);
        
        addAddressToManufacturingFacility(betriebsstaetteMap, 1L, null, 815L, "This is a comment.", 'D');

        ManufacturingFacilityDTS betriebsstaetteDTSMapped = mapper.map(manufacturingFacility, ManufacturingFacilityDTS.class);
        ManufacturingFacility betriebsstaetteMappedBack = mapper.map(betriebsstaetteDTSMapped, ManufacturingFacility.class);
        assertTrue(manufacturingFacility.equals(betriebsstaetteMappedBack));
    }
    
    @Test
    public void testMapManufacuringFacilityToDTSMultipleAdr() {
        Map<String, Object> betriebsstaetteMap =
                getManufacturingFacility(3L,"First");
        
        ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITY_KEY);
        ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITYDTS_KEY);
        
        addAddressToManufacturingFacility(betriebsstaetteMap, 10L, "StreetA", 815L, "This is a comment. 10", 'D');
        addAddressToManufacturingFacility(betriebsstaetteMap, 11L, "StreetB", 816L, "This is a comment. 11", 'E');
        addAddressToManufacturingFacility(betriebsstaetteMap, 12L, "StreetC", 817L, "This is a comment. 12", 'F');

        ManufacturingFacilityDTS betriebsstaetteDTSMapped = mapper.map(manufacturingFacility, ManufacturingFacilityDTS.class);
        ManufacturingFacility betriebsstaetteMappedBack = mapper.map(betriebsstaetteDTSMapped, ManufacturingFacility.class);
        assertTrue(manufacturingFacility.equals(betriebsstaetteMappedBack));
    }
    
    @Test
    public void testMapManufacuringFacilityToDTSMerge() {
        Map<String, Object> betriebsstaetteMap =
            getManufacturingFacility(4L,"Manufacturing Facility Description.");
        
        ManufacturingFacility manufacturingFacility = (ManufacturingFacility)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITY_KEY);
        ManufacturingFacilityDTS manufacturingFacilityDTS = (ManufacturingFacilityDTS)betriebsstaetteMap.get(BaseManufacturingFacilityTest.MANUFACTURINGFACILITYDTS_KEY);
        
        addAddressToManufacturingFacility(betriebsstaetteMap, 10L, "StreetA", 815L, "This is a comment. 10", 'D');
        addAddressToManufacturingFacility(betriebsstaetteMap, 11L, "StreetB", 816L, "This is a comment. 11", 'E');
        addAddressToManufacturingFacility(betriebsstaetteMap, 12L, "StreetC", 817L, "This is a comment. 12", 'F');

        // Edit some values on "orginal" bean. 
        // This is because we want to see if values are kept which are not mapped.
        
        // With the AddressMergingMapper this variable does not contain any addresses after merge. 
        // The entity mapped from contains 3 addresses.
        // Without the AddressMergingMapper there are contained 3 adresses.
        // But then the check for the land value fails.
        ManufacturingFacilityDTS manufacturingFacilityDTSServer = mapper.map(manufacturingFacility, ManufacturingFacilityDTS.class);
        manufacturingFacilityDTSServer.setIdNumber(4L);
        Character vLand = 'D';
        for(AddressDTO currAnschrift: manufacturingFacilityDTSServer.getAddressL()){
            currAnschrift.setLand(vLand);
        }

        ManufacturingFacility manufacturingFacilityToEdit = mapper.map(manufacturingFacilityDTSServer,ManufacturingFacility.class);
        manufacturingFacilityToEdit.putPrototype(manufacturingFacilityDTSServer);
        
        // Now the bean will be edited to see if the mapped values are correctly merged.
        List<Address> anschriftenGUI = manufacturingFacilityToEdit.getAddresses();
        anschriftenGUI.remove(1);

        manufacturingFacilityToEdit.setDescription("Description after merge.");
        
        Address firstAdr = anschriftenGUI.get(0);
        firstAdr.setStreet("Street new");
        
        // merge
        ManufacturingFacilityDTS prototype = manufacturingFacilityToEdit.returnPrototype();
        mapper.map(manufacturingFacilityToEdit, prototype);
        
        // Do some checks.
        assertTrue("IdNumber was not kept after merge.", 
        			4L == prototype.getIdNumber());
        
        // Check name of street
        assertTrue("Street new".equals(prototype.getAddressL().get(0).getStreet()));
        
        // Amount of addresses
        List<AddressDTO> addressesFromPrototype = prototype.getAddressL();
        assertTrue("An address was removed. In the merged DS this address does still exist.", 
                   addressesFromPrototype.size() == 2);
        
        // land check
        List<AddressDTO> addressesAfterMerge = prototype.getAddressL();
        AddressDTO addressOne = addressesAfterMerge.get(0);
        AddressDTO addressTwo = addressesAfterMerge.get(1);

        assertTrue("Land after merge is wrong.", Character.valueOf('D').equals(addressOne.getLand()));
        assertTrue("Land after merge is wrong.", Character.valueOf('D').equals(addressTwo.getLand()));
    }
    
}

