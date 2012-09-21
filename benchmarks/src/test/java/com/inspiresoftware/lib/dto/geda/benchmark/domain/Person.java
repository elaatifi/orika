/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package com.inspiresoftware.lib.dto.geda.benchmark.domain;

import java.util.List;

/**
 * .
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 8:38:37 AM
 */
public class Person {

    private Name name;

    private Address currentAddress;
    private List<Address> previousAddresses;

    public Person() {
    }

    public Person(final Name name, final Address currentAddress) {
        this.name = name;
        this.currentAddress = currentAddress;
    }

    public Person(final Name name, final Address currentAddress, final List<Address> previousAddresses) {
        this.name = name;
        this.currentAddress = currentAddress;
        this.previousAddresses = previousAddresses;
    }

    public Name getName() {
        return name;
    }

    public void setName(final Name name) {
        this.name = name;
    }

    public Address getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(final Address currentAddress) {
        this.currentAddress = currentAddress;
    }

    public List<Address> getPreviousAddresses() {
        return previousAddresses;
    }

    public void setPreviousAddresses(final List<Address> previousAddresses) {
        this.previousAddresses = previousAddresses;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person)) {
            return false;
        }

        Person person = (Person) o;

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
