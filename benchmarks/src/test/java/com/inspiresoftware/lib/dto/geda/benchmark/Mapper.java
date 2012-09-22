/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package com.inspiresoftware.lib.dto.geda.benchmark;

/**
 * Simple adapter to unify transfer process.
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 10:22:37 AM
 */
public interface Mapper {

    /**
     * Convert entity to DTO.
     *
     * @param entity entity object
     * @return dto
     */
    Object fromEntity(Object entity);

    /**
     * Assemble entity from DTO
     *
     * @param dto dto
     * @return entity with data
     */
    Object fromDto(Object dto);
    
    /**
     * Convert the nested entity to DTO.
     * 
     * @param dto
     * @return
     */
    Object fromEntityNested(Object entity);

}
