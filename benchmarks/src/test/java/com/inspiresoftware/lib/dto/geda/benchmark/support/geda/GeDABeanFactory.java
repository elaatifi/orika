/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package com.inspiresoftware.lib.dto.geda.benchmark.support.geda;

import com.inspiresoftware.lib.dto.geda.adapter.BeanFactory;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Address;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Country;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Name;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.AddressDTO;

/**
 * .
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 9:27:58 AM
 */
public class GeDABeanFactory implements BeanFactory {

    /** {@inheritDoc} */
    public Object get(final String entityBeanKey) {
        if ("addressDto".equals(entityBeanKey)) {
            return new AddressDTO();
        } else if ("countryEntity".equals(entityBeanKey)) {
            return new Country();
        } else if ("nameEntity".equals(entityBeanKey)) {
            return new Name();
        } else if ("addressEntity".equals(entityBeanKey)) {
            return new Address();
        }

        return null;
    }
}
