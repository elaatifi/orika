/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package ma.glasnost.orika.test.benchmarks.domain.destination;

import java.util.List;

/**
 * .
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 8:38:37 AM
 */
public class PersonDto {

    private NameDto name;

    private AddressDto currentAddress;
    private List<AddressDto> previousAddresses;

    public PersonDto() {
    }

    public PersonDto(final NameDto name, final AddressDto currentAddress) {
        this.name = name;
        this.currentAddress = currentAddress;
    }

    public PersonDto(final NameDto name, final AddressDto currentAddress, final List<AddressDto> previousAddresses) {
        this.name = name;
        this.currentAddress = currentAddress;
        this.previousAddresses = previousAddresses;
    }

    public NameDto getName() {
        return name;
    }

    public void setName(final NameDto name) {
        this.name = name;
    }

    public AddressDto getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(final AddressDto currentAddress) {
        this.currentAddress = currentAddress;
    }

    public List<AddressDto> getPreviousAddresses() {
        return previousAddresses;
    }

    public void setPreviousAddresses(final List<AddressDto> previousAddresses) {
        this.previousAddresses = previousAddresses;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonDto)) {
            return false;
        }

        PersonDto person = (PersonDto) o;

        if (currentAddress != null ? !currentAddress.equals(person.currentAddress) : person.currentAddress != null) {
            return false;
        }
        if (name != null ? !name.equals(person.name) : person.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (currentAddress != null ? currentAddress.hashCode() : 0);
        return result;
    }
}
