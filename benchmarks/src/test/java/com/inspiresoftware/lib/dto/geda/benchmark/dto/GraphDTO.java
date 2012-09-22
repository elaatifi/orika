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
package com.inspiresoftware.lib.dto.geda.benchmark.dto;

import java.util.Set;

import com.inspiresoftware.lib.dto.geda.annotations.Dto;

/**
 * @author matt.deboer@gmail.com
 *
 */
@Dto
public class GraphDTO {
    
    private Set<SegmentDTO> segments;
    private Set<PointDTO> points;
    
    public Set<SegmentDTO> getSegments() {
        return segments;
    }
    public void setSegments(Set<SegmentDTO> segments) {
        this.segments = segments;
    }
    public Set<PointDTO> getPoints() {
        return points;
    }
    public void setPoints(Set<PointDTO> points) {
        this.points = points;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((points == null) ? 0 : points.hashCode());
        result = prime * result + ((segments == null) ? 0 : segments.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GraphDTO other = (GraphDTO) obj;
        if (points == null) {
            if (other.points != null)
                return false;
        } else if (!points.equals(other.points))
            return false;
        if (segments == null) {
            if (other.segments != null)
                return false;
        } else if (!segments.equals(other.segments))
            return false;
        return true;
    }
}
