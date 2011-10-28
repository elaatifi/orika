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

package ma.glasnost.orika.test.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.ConverterException;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.Author;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.AuthorChild;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.AuthorMyDTO;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.AuthorParent;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.Book;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.BookChild;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.BookMyDTO;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.BookParent;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.Library;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.LibraryChild;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.LibraryMyDTO;
import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.LibraryParent;

import org.junit.Assert;
import org.junit.Test;

public class SuperTypeMappingTestCase {

	
	private Author createAuthor(Class<? extends AuthorParent> type) throws InstantiationException, IllegalAccessException {
		AuthorParent author = (AuthorParent) type.newInstance();
		author.setName("Khalil Gebran");
		
		return author;
	}
	
	private Book createBook(Class<? extends BookParent> type) throws InstantiationException, IllegalAccessException {
		BookParent book = (BookParent)type.newInstance();
		book.setTitle("The Prophet");
		
		return book;
	}
	
	private Library createLibrary(Class<? extends LibraryParent> type) throws InstantiationException, IllegalAccessException {
		LibraryParent lib = (LibraryParent)type.newInstance();
		lib.setTitle("Test Library");
		
		return lib;
	}
	
	@Test
	public void testMappingInterfaceImplementationNoExistingMapping() throws Exception {
		
		MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
		
		Book book = createBook(BookChild.class);
		book.setAuthor(createAuthor(AuthorChild.class));
		
		BookMyDTO mappedBook = mapper.map(book, BookMyDTO.class);
		
		Assert.assertNotNull(mappedBook);
		Assert.assertNull(mappedBook.getMyTitle());
		Assert.assertNull(mappedBook.getMyAuthor());
	}
	
	@Test
	public void testMappingInterfaceImplementationWithExistingDirectMapping() throws Exception {
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		
		factory.registerClassMap(ClassMapBuilder.map(Library.class, LibraryMyDTO.class)
				.field("title","myTitle")
				.field("books","myBooks")
				.byDefault().toClassMap());
		
		factory.registerClassMap(ClassMapBuilder.map(Author.class, AuthorMyDTO.class)
				.field("name","myName")
				.byDefault().toClassMap());
		factory.registerClassMap(ClassMapBuilder.map(Book.class, BookMyDTO.class)
				.field("title","myTitle")
				.field("author","myAuthor")
				.byDefault().toClassMap());
		factory.build();
		
		MapperFacade mapper = factory.getMapperFacade();
		
		
		Book book = createBook(BookParent.class);
		book.setAuthor(createAuthor(AuthorParent.class));
		Library lib = createLibrary(LibraryParent.class);
		lib.getBooks().add(book);
		
		LibraryMyDTO mappedLib = mapper.map(lib, LibraryMyDTO.class);
		
		Assert.assertEquals(lib.getTitle(),mappedLib.getMyTitle());
		Assert.assertEquals(book.getTitle(),mappedLib.getMyBooks().get(0).getMyTitle());
		Assert.assertEquals(book.getAuthor().getName(),mappedLib.getMyBooks().get(0).getMyAuthor().getMyName());
	}
	
	@Test
	public void testMappingInterfaceImplementationWithExistingInheritedMapping() throws Exception  {
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerClassMap(ClassMapBuilder.map(Library.class, LibraryMyDTO.class)
				.field("title","myTitle")
				.field("books","myBooks")
				.byDefault().toClassMap());
		
		factory.registerClassMap(ClassMapBuilder.map(Author.class, AuthorMyDTO.class)
				.field("name","myName")
				.byDefault().toClassMap());
		factory.registerClassMap(ClassMapBuilder.map(Book.class, BookMyDTO.class)
				.field("title","myTitle")
				.field("author","myAuthor")
				.byDefault().toClassMap());
		factory.build();
		
		MapperFacade mapper = factory.getMapperFacade();
		
		// BookChild, AuthorChild, LibraryChild don't directly 
		// implement Book, Author and Library
		Book book = createBook(BookChild.class);
		book.setAuthor(createAuthor(AuthorChild.class));
		Library lib = createLibrary(LibraryChild.class);
		lib.getBooks().add(book);
		
		LibraryMyDTO mappedLib = mapper.map(lib, LibraryMyDTO.class);
		
		Assert.assertEquals(lib.getTitle(),mappedLib.getMyTitle());
		Assert.assertEquals(book.getTitle(),mappedLib.getMyBooks().get(0).getMyTitle());
		Assert.assertEquals(book.getAuthor().getName(),mappedLib.getMyBooks().get(0).getMyAuthor().getMyName());
	}
	
	@Test
	public void testMappingSubclassImplementationWithExistingMapping() throws Exception  {
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		
		factory.registerClassMap(ClassMapBuilder.map(AuthorParent.class, AuthorMyDTO.class)
				.field("name","myName")
				.byDefault().toClassMap());
		factory.registerClassMap(ClassMapBuilder.map(BookParent.class, BookMyDTO.class)
				.field("title","myTitle")
				.field("author","myAuthor")
				.byDefault().toClassMap());
		factory.build();
		
		MapperFacade mapper = factory.getMapperFacade();
		
		Book book = createBook(BookChild.class);
		book.setAuthor(createAuthor(AuthorChild.class));
		
		BookMyDTO mappedBook = mapper.map(book, BookMyDTO.class);
		
		Assert.assertEquals(book.getTitle(),mappedBook.getMyTitle());
		Assert.assertEquals(book.getAuthor().getName(),mappedBook.getMyAuthor().getMyName());
	}
	
	public static class A {
        XMLGregorianCalendar time ;

        public A() {
        }

        public XMLGregorianCalendar getTime() {
                return time;
        }

        public void setTime(XMLGregorianCalendar time) {
                this.time = time;
        }
	}

	public static class B {
        Date time;

        public B() {
        }

        public Date getTime() {
                return time;
        }

        public void setTime(Date time) {
                this.time = time;
        }
	}
	
	@Test
	public void testSuperTypeConverterMapping() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		
		factory.registerConverter(
				new Converter<XMLGregorianCalendar,Date>() {
				                        
					public Date convert(XMLGregorianCalendar source) throws ConverterException {
                            Date target = null;
                            if (source != null) {
                                    target = source.toGregorianCalendar().getTime();
                            }
                            return target;
                    }
				}, 
				XMLGregorianCalendar.class, 
				Date.class);

		factory.build();
		
		A a = new A();
		Date date = new Date();
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		a.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
		
		B mapped = factory.getMapperFacade().map(a, B.class);
		
		assertNotNull(mapped);
		assertEquals(date,mapped.getTime());
		
	}
	
}
