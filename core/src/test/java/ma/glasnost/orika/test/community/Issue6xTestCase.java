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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class Issue6xTestCase {
    
    
    
    @Test
    public void testMapping() {
        
        
        PeriodBudgetType periodBudget = new PeriodBudgetType();
        periodBudget.setLocationNumber(110);
        periodBudget.setPeriod(2);
        periodBudget.setRegionCode("NW");
        periodBudget.setWeeklyBudgets(new ArrayList<WeeklyBudgetType>());
        WeeklyBudgetType weeklyBudget = new WeeklyBudgetType();
        weeklyBudget.setWeekNumber(1);
        weeklyBudget.setKPIBudgets(new LinkedHashMap<Integer, KPIBudgetType>());
        KPIBudgetType kpiBudget = new KPIBudgetType();
        kpiBudget.setDay1Actual(BigDecimal.valueOf(22.2));
        
        PeriodBudget result = MappingUtil.getMapperFactory().getMapperFacade().map(periodBudget, PeriodBudget.class);
        
        Assert.assertNotNull(result);
        
    }
    
    
    
    public static class KPIBudget {
        
        private static final long serialVersionUID = -3532089924423164881L;

        private String lockId;
        
        private int kpiId;
        
        private int locationNumber;
        private KPIRegionRuleType category;
        private BigDecimal day1Plan;
        private BigDecimal day1Actual;
        private BigDecimal day2Plan;
        private BigDecimal day2Actual;
        private BigDecimal day3Plan;
        private BigDecimal day3Actual;
        private BigDecimal day4Plan;
        private BigDecimal day4Actual;
        private BigDecimal day5Plan;
        private BigDecimal day5Actual;
        private BigDecimal day6Plan;
        private BigDecimal day6Actual;
        private BigDecimal day7Plan;
        private BigDecimal day7Actual;
        private BigDecimal day8Actual;
        private BigDecimal weeklyPlan;
        private BigDecimal weeklyActual;
        
        public int getKpiId() {
            return kpiId;
        }

        public void setKpiId(int kpiId) {
            this.kpiId = kpiId;
        }

        public int getLocationNumber() {
            return locationNumber;
        }
        
        public void setLocationNumber(int locationNumber) {
            this.locationNumber = locationNumber;
        }
        
        public KPIRegionRuleType getCategory() {
            return category;
        }
        
        public void setCategory(KPIRegionRuleType category) {
            this.category = category;
        }
        
        public BigDecimal getDay1Plan() {
            return day1Plan;
        }
        
        public void setDay1Plan(BigDecimal day1Plan) {
            this.day1Plan = day1Plan;
        }
        
        public BigDecimal getDay1Actual() {
            return day1Actual;
        }
        
        public void setDay1Actual(BigDecimal day1Actual) {
            this.day1Actual = day1Actual;
        }
        
        public BigDecimal getDay2Plan() {
            return day2Plan;
        }
        
        public void setDay2Plan(BigDecimal day2Plan) {
            this.day2Plan = day2Plan;
        }
        
        public BigDecimal getDay2Actual() {
            return day2Actual;
        }
        
        public void setDay2Actual(BigDecimal day2Actual) {
            this.day2Actual = day2Actual;
        }
        
        public BigDecimal getDay3Plan() {
            return day3Plan;
        }
        
        public void setDay3Plan(BigDecimal day3Plan) {
            this.day3Plan = day3Plan;
        }
        
        public BigDecimal getDay3Actual() {
            return day3Actual;
        }
        
        public void setDay3Actual(BigDecimal day3Actual) {
            this.day3Actual = day3Actual;
        }
        public BigDecimal getDay4Plan() {
            return day4Plan;
        }
        
        public void setDay4Plan(BigDecimal day4Plan) {
            this.day4Plan = day4Plan;
        }
        
        public BigDecimal getDay4Actual() {
            return day4Actual;
        }
        
        public void setDay4Actual(BigDecimal day4Actual) {
            this.day4Actual = day4Actual;
        }
        
        public BigDecimal getDay5Plan() {
            return day5Plan;
        }
        
        public void setDay5Plan(BigDecimal day5Plan) {
            this.day5Plan = day5Plan;
        }
        
        public BigDecimal getDay5Actual() {
            return day5Actual;
        }
        
        public void setDay5Actual(BigDecimal day5Actual) {
            this.day5Actual = day5Actual;
        }
        
        public BigDecimal getDay6Plan() {
            return day6Plan;
        }
        
        public void setDay6Plan(BigDecimal day6Plan) {
            this.day6Plan = day6Plan;
        }
        
        public BigDecimal getDay6Actual() {
            return day6Actual;
        }
        
        public void setDay6Actual(BigDecimal day6Actual) {
            this.day6Actual = day6Actual;
        }
        
        public BigDecimal getDay7Plan() {
            return day7Plan;
        }
        
        public void setDay7Plan(BigDecimal day7Plan) {
            this.day7Plan = day7Plan;
        }

        public BigDecimal getDay7Actual() {
            return day7Actual;
        }

        public void setDay7Actual(BigDecimal day7Actual) {
            this.day7Actual = day7Actual;
        }

        public BigDecimal getDay8Actual() {
            return day8Actual;
        }
        
        public void setDay8Actual(BigDecimal day8Actual) {
            this.day8Actual = day8Actual;
        }
        
        public BigDecimal getWeeklyPlan() {
            return weeklyPlan;
        }
        
        public void setWeeklyPlan(BigDecimal weeklyPlan) {
            this.weeklyPlan = weeklyPlan;
        }
        
        public BigDecimal getWeeklyActual() {
            return weeklyActual;
        }
        
        public void setWeeklyActual(BigDecimal weeklyActual) {
            this.weeklyActual = weeklyActual;
        }

        public String getLockId() {
            return lockId;
        }

        
        public void setLockId(String lockId) {
            this.lockId = lockId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((category == null) ? 0 : category.hashCode());
            result = prime * result + ((day1Actual == null) ? 0 : day1Actual.hashCode());
            result = prime * result + ((day1Plan == null) ? 0 : day1Plan.hashCode());
            result = prime * result + ((day2Actual == null) ? 0 : day2Actual.hashCode());
            result = prime * result + ((day2Plan == null) ? 0 : day2Plan.hashCode());
            result = prime * result + ((day3Actual == null) ? 0 : day3Actual.hashCode());
            result = prime * result + ((day3Plan == null) ? 0 : day3Plan.hashCode());
            result = prime * result + ((day4Actual == null) ? 0 : day4Actual.hashCode());
            result = prime * result + ((day4Plan == null) ? 0 : day4Plan.hashCode());
            result = prime * result + ((day5Actual == null) ? 0 : day5Actual.hashCode());
            result = prime * result + ((day5Plan == null) ? 0 : day5Plan.hashCode());
            result = prime * result + ((day6Actual == null) ? 0 : day6Actual.hashCode());
            result = prime * result + ((day6Plan == null) ? 0 : day6Plan.hashCode());
            result = prime * result + ((day7Actual == null) ? 0 : day7Actual.hashCode());
            result = prime * result + ((day7Plan == null) ? 0 : day7Plan.hashCode());
            result = prime * result + ((day8Actual == null) ? 0 : day8Actual.hashCode());
            result = prime * result + kpiId;
            result = prime * result + locationNumber;
            result = prime * result + ((lockId == null) ? 0 : lockId.hashCode());
            result = prime * result + ((weeklyActual == null) ? 0 : weeklyActual.hashCode());
            result = prime * result + ((weeklyPlan == null) ? 0 : weeklyPlan.hashCode());
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
            KPIBudget other = (KPIBudget) obj;
            if (category == null) {
                if (other.category != null)
                    return false;
            } else if (!category.equals(other.category))
                return false;
            if (day1Actual == null) {
                if (other.day1Actual != null)
                    return false;
            } else if (!day1Actual.equals(other.day1Actual))
                return false;
            if (day1Plan == null) {
                if (other.day1Plan != null)
                    return false;
            } else if (!day1Plan.equals(other.day1Plan))
                return false;
            if (day2Actual == null) {
                if (other.day2Actual != null)
                    return false;
            } else if (!day2Actual.equals(other.day2Actual))
                return false;
            if (day2Plan == null) {
                if (other.day2Plan != null)
                    return false;
            } else if (!day2Plan.equals(other.day2Plan))
                return false;
            if (day3Actual == null) {
                if (other.day3Actual != null)
                    return false;
            } else if (!day3Actual.equals(other.day3Actual))
                return false;
            if (day3Plan == null) {
                if (other.day3Plan != null)
                    return false;
            } else if (!day3Plan.equals(other.day3Plan))
                return false;
            if (day4Actual == null) {
                if (other.day4Actual != null)
                    return false;
            } else if (!day4Actual.equals(other.day4Actual))
                return false;
            if (day4Plan == null) {
                if (other.day4Plan != null)
                    return false;
            } else if (!day4Plan.equals(other.day4Plan))
                return false;
            if (day5Actual == null) {
                if (other.day5Actual != null)
                    return false;
            } else if (!day5Actual.equals(other.day5Actual))
                return false;
            if (day5Plan == null) {
                if (other.day5Plan != null)
                    return false;
            } else if (!day5Plan.equals(other.day5Plan))
                return false;
            if (day6Actual == null) {
                if (other.day6Actual != null)
                    return false;
            } else if (!day6Actual.equals(other.day6Actual))
                return false;
            if (day6Plan == null) {
                if (other.day6Plan != null)
                    return false;
            } else if (!day6Plan.equals(other.day6Plan))
                return false;
            if (day7Actual == null) {
                if (other.day7Actual != null)
                    return false;
            } else if (!day7Actual.equals(other.day7Actual))
                return false;
            if (day7Plan == null) {
                if (other.day7Plan != null)
                    return false;
            } else if (!day7Plan.equals(other.day7Plan))
                return false;
            if (day8Actual == null) {
                if (other.day8Actual != null)
                    return false;
            } else if (!day8Actual.equals(other.day8Actual))
                return false;
            if (kpiId != other.kpiId)
                return false;
            if (locationNumber != other.locationNumber)
                return false;
            if (lockId == null) {
                if (other.lockId != null)
                    return false;
            } else if (!lockId.equals(other.lockId))
                return false;
            if (weeklyActual == null) {
                if (other.weeklyActual != null)
                    return false;
            } else if (!weeklyActual.equals(other.weeklyActual))
                return false;
            if (weeklyPlan == null) {
                if (other.weeklyPlan != null)
                    return false;
            } else if (!weeklyPlan.equals(other.weeklyPlan))
                return false;
            return true;
        }
    
    }
    
    public static class KPIRegionRule implements Serializable{

        private static final long serialVersionUID = 3183055228661611544L;
        
        private String kpiId;
        private String kpiCode;
        private String regionCode;
        private String description;
        private boolean isEnteredByDay;
        private boolean isActualsEntered;
        private boolean isPlannedEntered;
        private boolean isRuleActive;
        
        private int decimalPositions;

        public boolean isRuleActive() {
            return isRuleActive;
        }

        public void setRuleActive(boolean isRuleActive) {
            this.isRuleActive = isRuleActive;
        }
        
        public boolean isPlannedEntered() {
            return isPlannedEntered;
        }

        public void setPlannedEntered(boolean isPlannedEntered) {
            this.isPlannedEntered = isPlannedEntered;
        }
        
        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        public String getKpiId() {
            return kpiId;
        }

        public void setKpiId(String kpiId) {
            this.kpiId = kpiId;
        }

        public String getKpiCode() {
            return kpiCode;
        }

        public void setKpiCode(String kpiCode) {
            this.kpiCode = kpiCode;
        }
        
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isEnteredByDay() {
            return isEnteredByDay;
        }
        
        public void setEnteredByDay(boolean isEnteredByDay) {
            this.isEnteredByDay = isEnteredByDay;
        }
        
        public boolean isActualsEntered() {
            return isActualsEntered;
        }
        
        public void setActualsEntered(boolean isActualsEntered) {
            this.isActualsEntered = isActualsEntered;
        }
        
        public int getDecimalPositions() {
            return decimalPositions;
        }
        
        public void setDecimalPositions(int decimalPositions) {
            this.decimalPositions = decimalPositions;
        }
    }
    
    public static class PeriodBudget {

        private String lockId;

        private int year;
        private int period;
        private int locationNumber;
        private String regionCode;
        
        private List<WeeklyBudget> weeklyList;
        
        public List<WeeklyBudget> getWeeklyBudgets() {
            return weeklyList;
        }
        
        public void setWeeklyBudgets(List<WeeklyBudget> weeklyList) {
            this.weeklyList = weeklyList;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        public int getLocationNumber() {
            return locationNumber;
        }

        public void setLocationNumber(int locationNumber) {
            this.locationNumber = locationNumber;
        }
        
        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        
        public String getLockId() {
            return lockId;
        }

        
        public void setLockId(String lockId) {
            this.lockId = lockId;
        }
    }
    
    public static class WeeklyBudget {

        private String lockId;
        
        private int weekNumber;
        private Map<Integer,KPIBudget> kpiList;

        public void setKPIBudgets(Map<Integer,KPIBudget> kpiList){  
            this.kpiList = kpiList;
        }  
        public Map<Integer,KPIBudget> getKPIBudgets(){  
            return kpiList;  
        } 

        public int getWeekNumber() {
            return weekNumber;
        }

        public void setWeekNumber(int weekNumber) {
            this.weekNumber = weekNumber;
        }

        public String getLockId() {
            return lockId;
        }

        public void setLockId(String lockId) {
            this.lockId = lockId;
        }
    }
    
    public static class WeeklyBudgetType implements Serializable{

        private static final long serialVersionUID = 2844688067754959505L;

        private String lockId;
        
        private int weekNumber;
        
        private Map<Integer,KPIBudgetType> kpiList;
        public void setKPIBudgets(Map<Integer,KPIBudgetType> kpiList){  
            this.kpiList = kpiList;
        }  
        public Map<Integer,KPIBudgetType> getKPIBudgets(){  
            return kpiList;  
        } 

        public int getWeekNumber() {
            return weekNumber;
        }

        public void setWeekNumber(int weekNumber) {
            this.weekNumber = weekNumber;
        }

        public String getLockId() {
            return lockId;
        }

        public void setLockId(String lockId) {
            this.lockId = lockId;
        }
    }
    
    public static class PeriodBudgetType implements Serializable {

        private static final long serialVersionUID = 2979291385931973016L;

        private KPIRegionRuleType category;
        private String lockId;

        private int year;
        private int period;
        private int locationNumber;
        private String regionCode;
        
        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        private List<WeeklyBudgetType> weeklyList;
        
        public List<WeeklyBudgetType> getWeeklyBudgets() {
            return weeklyList;
        }
        
        public void setWeeklyBudgets(List<WeeklyBudgetType> weeklyList) {
            this.weeklyList = weeklyList;
        }
        
        public int getLocationNumber() {
            return locationNumber;
        }

        public void setLocationNumber(int locationNumber) {
            this.locationNumber = locationNumber;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        public String getLockId() {
            return lockId;
        }

        public void setLockId(String lockId) {
            this.lockId = lockId;
        }

        public KPIRegionRuleType getCategory() {
            return category;
        }
        
        public void setCategory(KPIRegionRuleType category) {
            this.category = category;
        }
    }
    
    public static class KPIRegionRuleType implements Serializable{

        private static final long serialVersionUID = 3183055228661611544L;
        
        private int kpiId;
        private String kpiCode;
        private String regionCode;
        private String description;
        private boolean isEnteredByDay;
        private boolean isActualsEntered;
        private boolean isPlannedEntered;
        private int decimalPositions;
        private boolean isRuleActive;

        public boolean isRuleActive() {
            return isRuleActive;
        }

        public void setRuleActive(boolean isRuleActive) {
            this.isRuleActive = isRuleActive;
        }
        
        public boolean isPlannedEntered() {
            return isPlannedEntered;
        }

        public void setPlannedEntered(boolean isPlannedEntered) {
            this.isPlannedEntered = isPlannedEntered;
        }
        
        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        public int getKpiId() {
            return kpiId;
        }

        public void setKpiId(int kpiId) {
            this.kpiId = kpiId;
        }

        public String getKpiCode() {
            return kpiCode;
        }

        public void setKpiCode(String kpiCode) {
            this.kpiCode = kpiCode;
        }
        
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isEnteredByDay() {
            return isEnteredByDay;
        }
        
        public void setEnteredByDay(boolean isEnteredByDay) {
            this.isEnteredByDay = isEnteredByDay;
        }
        
        public boolean isActualsEntered() {
            return isActualsEntered;
        }
        
        public void setActualsEntered(boolean isActualsEntered) {
            this.isActualsEntered = isActualsEntered;
        }
        
        public int getDecimalPositions() {
            return decimalPositions;
        }
        
        public void setDecimalPositions(int decimalPositions) {
            this.decimalPositions = decimalPositions;
        }
    }
    
    public static class KPIBudgetType implements Serializable {
        
        private static final long serialVersionUID = -3532089924423164881L;

        private String lockId;
        
        private int locationNumber;
        private int kpiId;
        private KPIRegionRuleType regionRule;
        private BigDecimal day1Plan;
        private BigDecimal day1Actual;
        private BigDecimal day2Plan;
        private BigDecimal day2Actual;
        private BigDecimal day3Plan;
        private BigDecimal day3Actual;
        private BigDecimal day4Plan;
        private BigDecimal day4Actual;
        private BigDecimal day5Plan;
        private BigDecimal day5Actual;
        private BigDecimal day6Plan;
        private BigDecimal day6Actual;
        private BigDecimal day7Plan;
        private BigDecimal day7Actual;
        private BigDecimal day8Actual;
        private BigDecimal weeklyPlan;
        private BigDecimal weeklyActual;
        private String regionCode;
        
        public int getLocationNumber() {
            return locationNumber;
        }
        
        public void setLocationNumber(int locationNumber) {
            this.locationNumber = locationNumber;
        }
        
        public int getKpiId() {
            return kpiId;
        }

        public void setKpiId(int kpiId) {
            this.kpiId = kpiId;
        }

        public KPIRegionRuleType getRegionRule() {
            return regionRule;
        }
        
        public void setRegionRule(KPIRegionRuleType regionRule) {
            this.regionRule = regionRule;
        }
        
        public BigDecimal getDay1Plan() {
            return day1Plan;
        }
        
        public void setDay1Plan(BigDecimal day1Plan) {
            this.day1Plan = day1Plan;
        }
        
        public BigDecimal getDay1Actual() {
            return day1Actual;
        }
        
        public void setDay1Actual(BigDecimal day1Actual) {
            this.day1Actual = day1Actual;
        }
        
        public BigDecimal getDay2Plan() {
            return day2Plan;
        }
        
        public void setDay2Plan(BigDecimal day2Plan) {
            this.day2Plan = day2Plan;
        }
        
        public BigDecimal getDay2Actual() {
            return day2Actual;
        }
        
        public void setDay2Actual(BigDecimal day2Actual) {
            this.day2Actual = day2Actual;
        }
        
        public BigDecimal getDay3Plan() {
            return day3Plan;
        }
        
        public void setDay3Plan(BigDecimal day3Plan) {
            this.day3Plan = day3Plan;
        }
        
        public BigDecimal getDay3Actual() {
            return day3Actual;
        }
        
        public void setDay3Actual(BigDecimal day3Actual) {
            this.day3Actual = day3Actual;
        }
        public BigDecimal getDay4Plan() {
            return day4Plan;
        }
        
        public void setDay4Plan(BigDecimal day4Plan) {
            this.day4Plan = day4Plan;
        }
        
        public BigDecimal getDay4Actual() {
            return day4Actual;
        }
        
        public void setDay4Actual(BigDecimal day4Actual) {
            this.day4Actual = day4Actual;
        }
        
        public BigDecimal getDay5Plan() {
            return day5Plan;
        }
        
        public void setDay5Plan(BigDecimal day5Plan) {
            this.day5Plan = day5Plan;
        }
        
        public BigDecimal getDay5Actual() {
            return day5Actual;
        }
        
        public void setDay5Actual(BigDecimal day5Actual) {
            this.day5Actual = day5Actual;
        }
        
        public BigDecimal getDay6Plan() {
            return day6Plan;
        }
        
        public void setDay6Plan(BigDecimal day6Plan) {
            this.day6Plan = day6Plan;
        }
        
        public BigDecimal getDay6Actual() {
            return day6Actual;
        }
        
        public void setDay6Actual(BigDecimal day6Actual) {
            this.day6Actual = day6Actual;
        }
        
        public BigDecimal getDay7Plan() {
            return day7Plan;
        }
        
        public void setDay7Plan(BigDecimal day7Plan) {
            this.day7Plan = day7Plan;
        }

        public BigDecimal getDay7Actual() {
            return day7Actual;
        }

        public void setDay7Actual(BigDecimal day7Actual) {
            this.day7Actual = day7Actual;
        }

        public BigDecimal getDay8Actual() {
            return day8Actual;
        }
        
        public void setDay8Actual(BigDecimal day8Actual) {
            this.day8Actual = day8Actual;
        }
        
        public BigDecimal getWeeklyPlan() {
            return weeklyPlan;
        }
        
        public void setWeeklyPlan(BigDecimal weeklyPlan) {
            this.weeklyPlan = weeklyPlan;
        }
        
        public BigDecimal getWeeklyActual() {
            return weeklyActual;
        }
        
        public void setWeeklyActual(BigDecimal weeklyActual) {
            this.weeklyActual = weeklyActual;
        }

        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        public String getLockId() {
            return lockId;
        }

        public void setLockId(String lockId) {
            this.lockId = lockId;
        }   
    }
}
