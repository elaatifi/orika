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
package ma.glasnost.orika.converter.builtin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ma.glasnost.orika.converter.BidirectionConverter;

public class DateToStringConverter extends BidirectionConverter<Date, String> {
    
    private final String pattern;
    
    public DateToStringConverter(String format) {
        this.pattern = format;
    }
    
    @Override
    public String convertTo(Date source, Class<String> destinationClass) {
        return new SimpleDateFormat(pattern).format(source);
    }
    
    @Override
    public Date convertFrom(String source, Class<Date> destinationClass) {
        try {
            return new SimpleDateFormat(pattern).parse(source);
        } catch (ParseException e) {
            return null;
        }
    }
    
}
