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

package ma.glasnost.orika.test.property;

import java.util.ArrayList;
import java.util.List;

public interface TestCaseClasses {

	public class Book {

		public String title;
		
		private Author author;

		public Author getAuthor() {
		    return author;
		}
        
        public void setAuthor(Author author) {
            this.author = author;
        }
	}
	
	public class Author {

		private String name;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	public class Library {
		
		private String title;

		public List<Book> books;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	public class BookChild extends Book {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	public class AuthorChild extends Author {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	public class LibraryChild extends Library {
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}
	}
	
	public class AuthorDTO {
		
		private String name;
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
	
	public class BookDTO {

		private String title;
		public AuthorDTO author;
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public AuthorDTO getAuthor() {
			return author;
		}

		public void setAuthor(AuthorDTO author) {
			this.author = author;
		}
	}
	
	public class LibraryDTO {
		
		public String title;
		private List<BookDTO> books;
		private String additionalValue;
		
		public String getAdditionalValue() {
			return additionalValue;
		}

		public void setAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public List<BookDTO> getBooks() {
			if (books==null) {
				books = new ArrayList<BookDTO>();
			}
			return books;
		}
	}
	
	
	public class AuthorMyDTO {
	
		private String name;
		private String additionalValue;
		
		public String getMyAdditionalValue() {
			return additionalValue;
		}

		public void setMyAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public String getMyName() {
			return name;
		}

		public void setMyName(String name) {
			this.name = name;
		}
	}
	
	public class BookMyDTO {

		private String title;
		private AuthorMyDTO author;
		public String additionalValue;

		public String getMyTitle() {
			return title;
		}

		public void setMyTitle(String title) {
			this.title = title;
		}

		public AuthorMyDTO getMyAuthor() {
			return author;
		}

		public void setMyAuthor(AuthorMyDTO author) {
			this.author = author;
		}
	}
	
	public class LibraryMyDTO {
		
		public String myTitle;
		private List<BookMyDTO> books;
		private String additionalValue;
		
		public String getMyAdditionalValue() {
			return additionalValue;
		}

		public void setMyAdditionalValue(String additionalValue) {
			this.additionalValue = additionalValue;
		}

		public List<BookMyDTO> getMyBooks() {
			if (books==null) {
				books = new ArrayList<BookMyDTO>();
			}
			return books;
		}
	}
}
