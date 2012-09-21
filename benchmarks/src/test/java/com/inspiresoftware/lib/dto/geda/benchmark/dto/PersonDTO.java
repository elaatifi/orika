/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package com.inspiresoftware.lib.dto.geda.benchmark.dto;

import com.inspiresoftware.lib.dto.geda.annotations.Dto;
import com.inspiresoftware.lib.dto.geda.annotations.DtoField;

/**
 * .
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 8:47:35 AM
 */
@Dto
public class PersonDTO {

    @DtoField(value = "name.firstname",
              entityBeanKeys = "nameEntity")
    private String firstName;
    @DtoField(value = "name.surname",
              entityBeanKeys = "nameEntity")
    private String lastName;

    @DtoField(value = "currentAddress",
              dtoBeanKey = "addressDto",
              entityBeanKeys = "addressEntity")
    private AddressDTO currentAddress;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public AddressDTO getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(final AddressDTO currentAddress) {
        this.currentAddress = currentAddress;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonDTO)) {
            return false;
        }

        PersonDTO personDTO = (PersonDTO) o;

        if (currentAddress != null ? !currentAddress.equals(personDTO.currentAddress) : personDTO.currentAddress != null) {
            return false;
        }
        if (firstName != null ? !firstName.equals(personDTO.firstName) : personDTO.firstName != null) {
            return false;
        }
        if (lastName != null ? !lastName.equals(personDTO.lastName) : personDTO.lastName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (currentAddress != null ? currentAddress.hashCode() : 0);
        return result;
    }
}
