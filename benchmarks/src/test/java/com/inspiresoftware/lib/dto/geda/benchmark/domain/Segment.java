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
package com.inspiresoftware.lib.dto.geda.benchmark.domain;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class Segment {
    
    private Point point1;
    private Point point2;
    
    public Point getPoint1() {
        return point1;
    }
    public void setPoint1(Point point1) {
        this.point1 = point1;
    }
    public Point getPoint2() {
        return point2;
    }
    public void setPoint2(Point point2) {
        this.point2 = point2;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((point1 == null) ? 0 : point1.hashCode());
        result = prime * result + ((point2 == null) ? 0 : point2.hashCode());
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
        Segment other = (Segment) obj;
        if (point1 == null) {
            if (other.point1 != null)
                return false;
        } else if (!point1.equals(other.point1))
            return false;
        if (point2 == null) {
            if (other.point2 != null)
                return false;
        } else if (!point2.equals(other.point2))
            return false;
        return true;
    }
    
    
}
