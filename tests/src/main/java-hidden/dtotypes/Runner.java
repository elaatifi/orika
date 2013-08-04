/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

package dtotypes;


import types.BookHidden;
import types.AuthorHidden;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class Runner {

	public static void run(MapperFactory mapperFactory) {
		
		mapperFactory.classMap(BookHidden.class, BookHiddenDto.class).byDefault().register();
		
		AuthorHidden author = new AuthorHidden();
		author.setName("Chuck Testa");
		
		BookHidden book = new BookHidden();
		book.setAuthor(author);
		
		mapperFactory.getMapperFacade().map(book, BookHiddenDto.class);
		
	}
	
	
	@Test
	public void test() {
		run(MappingUtil.getMapperFactory());
	}
	
}
