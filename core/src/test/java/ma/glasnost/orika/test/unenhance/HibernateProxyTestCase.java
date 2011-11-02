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

package ma.glasnost.orika.test.unenhance;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.HibernateUtil;
import ma.glasnost.orika.test.MappingUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;

public class HibernateProxyTestCase {

	@Test
	public void testMappigProxyObject() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
		Transaction t = session.beginTransaction();

		{
			Author author = new Author();
			author.setName("Khalil Gebran");
			session.save(author);

			Book book = new Book();
			book.setTitle("The Prophet");
			book.setAuthor(author);

			session.save(book);
		}
		{
			Author author = new Author();
			author.setName("Mohamed CHAOUKI");
			session.save(author);

			Book book = new Book();
			book.setTitle("Le pain nu");
			book.setAuthor(author);

			session.save(book);

		}
		session.flush();
		t.commit();
		session.clear();

		AuthorDTO author = mapper.map((Author) session.load(Author.class, 1L), AuthorDTO.class);

		Assert.assertEquals("Khalil Gebran", author.getName());
	}
}
